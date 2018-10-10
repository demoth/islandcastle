package org.demoth.ktxtest

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.World
import ktx.ashley.entity
import ktx.ashley.get
import ktx.box2d.body
import java.util.*

fun createPlayerEntity(engine: Engine, world: World, location: Vector2) {
    engine.entity().apply {
        add(Textured(Texture(Gdx.files.internal("knight32.png"))))
        add(Player())
        add(Named("player"))
        add(HasHealth(9000))
        add(Physical(
                body = world.body {
                    userData = this@apply
                    position.x = location.x
                    position.y = location.y
                    type = BodyDef.BodyType.DynamicBody
                    linearDamping = SPEED_DECEL
                    fixedRotation = true
                    circle(0.5f)
                },
                collide = { self, other -> damageHealth(engine, self, other) },
                collisionClass = RECEIVE_DAMAGE,
                collidesWith = DEAL_DAMAGE or TRIGGER))
    }
    println("spawned player at (${location.x}, ${location.y})")

}

fun createEyeMonster(engine: Engine, world: World, x: Float, y: Float) {
    engine.entity().apply {
        add(Named("eyelander"))
        add(MonsterStationaryRanged())
        add(HasHealth(1000))
        add(Textured(Texture(Gdx.files.internal("eye_monsters/eyelander.png"))))
        add(Physical(
                body = world.body {
                    userData = this@apply
                    position.x = x
                    position.y = y
                    type = BodyDef.BodyType.DynamicBody
                    linearDamping = SPEED_DECEL
                    fixedRotation = true
                    circle(0.5f)
                },
                collide = { self, other -> damageHealth(engine, self, other) },
                collisionClass = RECEIVE_DAMAGE,
                collidesWith = DEAL_DAMAGE))
    }
    println("spawned eye lander at ($x, $y)")
}

/**
 * Creates walls, solid objects, also named objects (like starting positions)
 */
fun createMapObject(engine: Engine, world: World, layer: String, name: String?, rect: Rectangle, finishTrigger: (Int) -> Unit) {
    if (name == "spawn_eyelander") {
        createEyeMonster(engine, world, rect.x / PPM, rect.y / PPM)
    } else if (name == "exit") {
        createTrigger("exit", engine, world, rect, finishTrigger)
    } else if (layer.startsWith("solid_"))
        createWall(engine, layer, world, rect, name)
}

private fun createWall(engine: Engine, layer: String, world: World, rect: Rectangle, name: String?) {
    engine.entity().apply {
        add(Physical(
                body = world.body {
                    userData = this@apply
                    type = BodyDef.BodyType.StaticBody
                    position.set(rect.getCentralPoint())
                    box(width = rect.width / PPM, height = rect.height / PPM)
                }, collisionClass = if (name.isNullOrBlank()) SOLID_INVISIBLE else SOLID))

        if (!name.isNullOrBlank()) {
            add(Named(name!!))
        }
    }
}

fun createFireBall(engine: Engine, world: World, velocity: Vector2, origin: Vector2, owner: Entity) {
    engine.entity().apply {
        add(Named("fireball"))
        add(Textured(Texture(Gdx.files.internal("Ardentryst-MagicSpriteEffects/Ardentryst-rfireball.png"))))
        add(Physical(
                body = world.body {
                    userData = this@apply
                    type = BodyDef.BodyType.DynamicBody
                    this.linearVelocity.set(velocity)
                    this.position.set(origin)
                    circle(0.5f) {
                        isSensor = true
                    }
                },
                collide = ::destroyFireball,
                collisionClass = DEAL_DAMAGE,
                collidesWith = RECEIVE_DAMAGE or SOLID))
        add(HasDamage(3070, owner))
        add(HasSound(Sounds.FIREBALL))
    }
}

fun createRotatingFireBall(engine: Engine, world: World, velocity: Vector2, origin: Vector2, owner: Entity) {
    engine.entity().apply {
        add(Named("fireball"))
        add(Animated(createAnimation(
                Texture(Gdx.files.internal("sprites/Sprite_FX_Fire_0004_FIX.png")),
                4, 1, 0.1f, Animation.PlayMode.LOOP)))
        add(Physical(
                body = world.body {
                    userData = this@apply
                    type = BodyDef.BodyType.DynamicBody
                    linearVelocity.set(velocity)
                    position.set(origin)
                    circle(0.5f) {
                        isSensor = true
                    }
                },
                collide = ::destroyFireball,
                collisionClass = DEAL_DAMAGE,
                collidesWith = RECEIVE_DAMAGE or SOLID))
        add(HasDamage(1000, owner))
    }
}

fun createFloatingLabel(engine: Engine, value: String, location: Vector2) {
    engine.entity().apply {
        add(Named(value))
        add(FloatingUpLabel())
        add(Positioned(location))
    }
}

fun createTrigger(name: String, engine: Engine, world: World, rect: Rectangle, action: (Int) -> Unit) {
    engine.entity().apply {
        add(Named(name))
        add(Physical(
                body = world.body {
                    userData = this@apply
                    type = BodyDef.BodyType.StaticBody
                    position.set(rect.getCentralPoint())
                    box(width = rect.width / PPM, height = rect.height / PPM) {
                        isSensor = true
                    }
                },
                collide = { _, other ->
                    val player = other.get<Player>()
                    if (player != null) {
                        Gdx.audio.newSound(Gdx.files.internal("sounds/" +
                                if (player.score > 0)
                                    Sounds.VICTORY.filename
                                else Sounds.GAME_OVER.filename
                        )).play()
                        action.invoke(player.score)
                    }
                },
                collisionClass = TRIGGER,
                collidesWith = RECEIVE_DAMAGE))
    }
}

private fun damageHealth(engine: Engine, self: Entity, other: Entity) {
    val health = self.get<HasHealth>()
    val damage = other.get<HasDamage>()
    val position = self.get<Physical>()?.body?.position
    if (damage != null && damage.owner !== self && health != null) {
        health.value -= damage.value
        if (position != null) {
            createFloatingLabel(engine, damage.value.toString(), position.cpy())
        }
        engine.entity().add(HasSound(HURT[Random().nextInt(4)]))
    }
}

private fun destroyFireball(self: Entity, other: Entity) {
    val damage = self.get<HasDamage>()
    val physical = self.get<Physical>()
    if (physical != null && damage != null && other !== damage.owner) {
        physical.toBeRemoved = true
    }
}


package org.demoth.ktxtest.ecs

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.World
import ktx.ashley.entity
import ktx.ashley.get
import ktx.box2d.body
import org.demoth.ktxtest.DEAL_DAMAGE
import org.demoth.ktxtest.HURT
import org.demoth.ktxtest.PPM
import org.demoth.ktxtest.RECEIVE_DAMAGE
import org.demoth.ktxtest.SOLID
import org.demoth.ktxtest.SOLID_INVISIBLE
import org.demoth.ktxtest.SPEED_DECEL
import org.demoth.ktxtest.Sounds
import org.demoth.ktxtest.SpriteSheets
import org.demoth.ktxtest.Sprites
import org.demoth.ktxtest.TRIGGER
import org.demoth.ktxtest.getCentralPoint
import java.util.*

class EntityFactory(private val engine: Engine, private val world: World) {

    fun createPlayerEntity(location: Vector2) {
        engine.entity().apply {
            add(Textured(Sprites.KNIGHT))
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
                    collide = { self, other -> damageHealth(self, other) },
                    collisionClass = RECEIVE_DAMAGE,
                    collidesWith = DEAL_DAMAGE or TRIGGER))
        }
        println("spawned player at (${location.x}, ${location.y})")

    }

    fun createEyeMonster(x: Float, y: Float) {
        engine.entity().apply {
            add(Named("eyelander"))
            add(MonsterStationaryRanged())
            add(HasHealth(1000))
            add(Textured(Sprites.EYE_BOT))
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
                    collide = { self, other -> damageHealth(self, other) },
                    collisionClass = RECEIVE_DAMAGE,
                    collidesWith = DEAL_DAMAGE))
        }
        println("spawned eye lander at ($x, $y)")
    }

    /**
     * Creates walls, solid objects, also named objects (like starting positions)
     */
    fun createMapObject(layer: String, name: String?, rect: Rectangle, finishTrigger: (Int) -> Unit) {
        if (name == "spawn_eyelander") {
            createEyeMonster(rect.x / PPM, rect.y / PPM)
        } else if (name == "exit") {
            createTrigger("exit", rect, finishTrigger)
        } else if (layer.startsWith("solid_"))
            createWall(world, rect, name)
    }

    private fun createWall(world: World, rect: Rectangle, name: String?) {
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

    fun createFireBall(velocity: Vector2, origin: Vector2, owner: Entity) {
        engine.entity().apply {
            add(Named("fireball"))
            add(Textured(Sprites.FIREBALL))
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
                    collide = ::destroyFireballWithExplosion,
                    collisionClass = DEAL_DAMAGE,
                    collidesWith = RECEIVE_DAMAGE or SOLID))
            add(HasDamage(3070, owner))
            add(HasSound(Sounds.FIREBALL))
        }
    }

    fun createRotatingFireBall(velocity: Vector2, origin: Vector2, owner: Entity) {
        engine.entity().apply {
            add(Named("fireball"))
            add(Animated(SpriteSheets.FIRE_SPIRALS, 0.1f, Animation.PlayMode.LOOP, 0f))
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

    fun createFloatingLabel(text: String, location: Vector2) {
        engine.entity().apply {
            add(Named(text))
            add(FloatingUpLabel())
            add(Positioned(location))
        }
    }

    fun createTrigger(name: String, rect: Rectangle, action: (Int) -> Unit) {
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

    private fun damageHealth(self: Entity, other: Entity) {
        val health = self.get<HasHealth>()
        val damage = other.get<HasDamage>()
        val position = self.get<Physical>()?.body?.position
        if (damage != null && damage.owner !== self && health != null) {
            health.value -= damage.value
            if (position != null) {
                createFloatingLabel(damage.value.toString(), position.cpy())
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

    private fun destroyFireballWithExplosion(self: Entity, other: Entity) {
        val damage = self.get<HasDamage>()
        val physical = self.get<Physical>()
        if (physical != null && damage != null && other !== damage.owner) {
            physical.toBeRemoved = true

            engine.entity().apply {
                add(Positioned(physical.body.position.cpy()))
                add(Animated(SpriteSheets.FIRE_EXPLOSION, 0.1f, Animation.PlayMode.NORMAL, 0f))
                add(HasSound(Sounds.EXPLOSION))
            }
        }
    }

}
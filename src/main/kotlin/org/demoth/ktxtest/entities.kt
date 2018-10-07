package org.demoth.ktxtest

import com.badlogic.ashley.core.Engine
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.World
import ktx.ashley.entity
import ktx.box2d.body
import java.util.*

fun createPlayerEntity(engine: Engine, world: World, location: Vector2) {
    engine.entity().apply {
        add(Textured(Texture(Gdx.files.internal("knight32.png"))))
        add(PlayerControlled())
        add(Named("player"))
        add(Physical(world.body {
            position.x = location.x
            position.y = location.y
            type = BodyDef.BodyType.DynamicBody
            linearDamping = SPEED_DECEL
            fixedRotation = true
            circle(0.5f)
        }, CollisionClass.RECEIVE_DAMAGE, "player"))
    }
}

fun createEyeMonster(engine: Engine, world: World, x: Float, y: Float) {
    engine.entity().apply {
        add(Named("eyelander"))
        add(MonsterStationaryRanged())
        add(Textured(Texture(Gdx.files.internal("eye_monsters/eyelander.png"))))
        add(Physical(world.body {
            position.x = x
            position.y = y
            type = BodyDef.BodyType.DynamicBody
            linearDamping = SPEED_DECEL
            fixedRotation = true
            circle(0.5f)
        }, CollisionClass.RECEIVE_DAMAGE, UUID.randomUUID().toString()))
    }

    println("spawned eye lander")
}

/**
 * Creates walls, solid objects, also named objects (like starting positions)
 */
fun createMapObject(engine: Engine, world: World, layer: String, name: String?, rect: Rectangle) {
    if (name == "spawn_eyelander") {
        createEyeMonster(engine, world, rect.x / PPM, rect.y / PPM)
    } else if (layer.startsWith("solid_") || !name.isNullOrBlank())
        engine.entity().apply {
            if (layer.startsWith("solid_")) {
                add(Physical(world.body {
                    type = BodyDef.BodyType.StaticBody
                    position.set(rect.getCentralPoint())
                    box(width = rect.width / PPM, height = rect.height / PPM)
                }, if (name.isNullOrBlank()) {
                    CollisionClass.SOLID_INVISIBLE
                } else {
                    CollisionClass.SOLID
                }))

            }
            if (!name.isNullOrBlank()) {
                add(Named(name!!))
                add(Positioned(rect.getCentralPoint()))
            }
        }
}

fun createFireBall(engine: Engine, world: World, velocity: Vector2, origin: Vector2, owner: String) {
    engine.entity().apply {
        add(Textured(Texture(Gdx.files.internal("Ardentryst-MagicSpriteEffects/Ardentryst-rfireball.png"))))
        add(Physical(world.body {
            type = BodyDef.BodyType.DynamicBody
            this.linearVelocity.set(velocity)
            this.position.set(origin)
            circle(0.5f) {
                isSensor = true
            }
        }, CollisionClass.DEAL_DAMAGE, owner))
        add(Named("fireball"))
    }
}

fun createRotatingFireBall(engine: Engine, world: World, velocity: Vector2, origin: Vector2, owner: String) {
    engine.entity().apply {
        add(Animated(createAnimation(
                Texture(Gdx.files.internal("sprites/Sprite_FX_Fire_0004_FIX.png")),
                4, 1, 0.1f, Animation.PlayMode.LOOP)))
        add(Physical(world.body {
            type = BodyDef.BodyType.DynamicBody
            this.linearVelocity.set(velocity)
            this.position.set(origin)
            circle(0.5f) {
                isSensor = true
            }
        }, CollisionClass.DEAL_DAMAGE, owner))
        add(Named("fireball"))

    }
}

fun createFloatingLabel(engine: Engine, value: String, location: Vector2) {
    val label = engine.entity()
    label.add(Named(value))
    label.add(FloatingUpLabel())
    label.add(Positioned(location))
}

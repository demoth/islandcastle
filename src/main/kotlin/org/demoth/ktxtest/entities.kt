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

fun createPlayerEntity(engine: Engine, world: World, startPosition: Vector2) {
    engine.entity {
        with<Textured> {
            texture = Texture(Gdx.files.internal("knight32.png"))
        }
        with<PlayerControlled>()
        with<Named> {
            name = "player"
        }
        with<Physical> {
            owner = UUID.randomUUID().toString()
            collisionClass = CollisionClass.RECEIVE_DAMAGE
            body = world.body {
                position.x = startPosition.x
                position.y = startPosition.y
                type = BodyDef.BodyType.DynamicBody
                linearDamping = SPEED_DECEL
                fixedRotation = true
                userData = this@with
                circle(0.5f)
            }
        }
    }
}

/**
 * Creates walls, solid objects, also named objects (like starting positions)
 */
fun createMapObject(engine: Engine, world: World, layer: String, name: String?, rect: Rectangle) {
    if (layer.startsWith("solid_") || !name.isNullOrBlank())
        engine.entity {
            if (layer.startsWith("solid_")) {
                with<Physical> {
                    body = world.body {
                        type = BodyDef.BodyType.StaticBody
                        position.set(rect.getCentralPoint())
                        userData = this@with
                        box(width = rect.width / PPM, height = rect.height / PPM)
                    }
                    collisionClass = if (name.isNullOrBlank()) {
                        CollisionClass.SOLID_INVISIBLE
                    } else {
                        CollisionClass.SOLID
                    }
                }
            }
            if (!name.isNullOrBlank()) {
                with<Named> {
                    this.name = name
                }
                with<Positioned> {
                    position = rect.getCentralPoint()
                }
            }
        }
}

fun createFireBall(engine: Engine, world: World, actionLocation: Vector2, origin: Vector2, owner: String) {
    engine.entity {
        with<Animated> {
            animation = createAnimation(
                    Texture(Gdx.files.internal("sprites/Sprite_FX_Fire_0004_FIX.png")),
                    4, 1, 0.1f, Animation.PlayMode.LOOP
            )
        }
        with<Physical> {
            this.owner = owner
            toBeRemoved = false
            collisionClass = CollisionClass.DEAL_DAMAGE
            this.body = world.body {
                type = BodyDef.BodyType.DynamicBody
                this.linearVelocity.set(actionLocation)
                this.position.set(origin)
                userData = this@with
                circle(0.5f) {
                    isSensor = true
                }
            }
        }
        with<Named> {
            name = "fireball"
        }

    }
}
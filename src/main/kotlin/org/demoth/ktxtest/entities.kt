package org.demoth.ktxtest

import com.badlogic.ashley.core.Engine
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.World
import ktx.ashley.entity
import ktx.box2d.body

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
            body = world.body {
                position.x = startPosition.x
                position.y = startPosition.y
                type = BodyDef.BodyType.DynamicBody
                linearDamping = SPEED_DECEL
                fixedRotation = true
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
                        box(width = rect.width / PPM, height = rect.height / PPM)
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
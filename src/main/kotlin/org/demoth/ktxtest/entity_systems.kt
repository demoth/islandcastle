package org.demoth.ktxtest

import com.badlogic.ashley.core.EntitySystem
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.World
import ktx.ashley.allOf
import ktx.ashley.entity
import ktx.ashley.mapperFor
import ktx.ashley.oneOf
import ktx.box2d.body
import ktx.math.plus

class PlayerControlSystem(private val world: World) : EntitySystem() {
    /**
     * location relative to player (center)
     */
    var actionLocation: Vector2? = null
    private val physicMapper = mapperFor<Physical>()
    private val playerCtrlMapper = mapperFor<PlayerControlled>()

    override fun update(deltaTime: Float) {
        engine.getEntitiesFor(allOf(Physical::class, PlayerControlled::class).get()).forEach { player ->
            // this may be used later to affect how controls are used
            val control = playerCtrlMapper[player]
            physicMapper[player]?.body?.let { body ->
                if (Gdx.input.isKeyPressed(Input.Keys.W) && body.linearVelocity.y < MAX_SPEED) {
                    body.applyForceToCenter(0f, WALK_FORCE, true)
                }
                if (Gdx.input.isKeyPressed(Input.Keys.A) && body.linearVelocity.x > -MAX_SPEED) {
                    body.applyForceToCenter(-WALK_FORCE, 0f, true)
                }
                if (Gdx.input.isKeyPressed(Input.Keys.S) && body.linearVelocity.y > -MAX_SPEED) {
                    body.applyForceToCenter(0f, -WALK_FORCE, true)
                }
                if (Gdx.input.isKeyPressed(Input.Keys.D) && body.linearVelocity.x < MAX_SPEED) {
                    body.applyForceToCenter(WALK_FORCE, 0f, true)
                }

                if (actionLocation != null) {
                    println("actionLocation: $actionLocation")
                    println("player: ${body.position}")
                    engine.entity {
                        with<Textured> {
                            texture = Texture(Gdx.files.internal("Ardentryst-MagicSpriteEffects/Ardentryst-rfireball.png"))
                        }
                        with<Physical> {
                            this.body = world.body {
                                type = BodyDef.BodyType.DynamicBody
                                this.linearVelocity.set(actionLocation)
                                this.position.set(body.position + actionLocation!!.nor())
                                circle(0.3f) {
                                    isSensor = true
                                }
                            }
                        }
                        with<Named> {
                            name = "fireball"
                        }
                    }
                    actionLocation = null
                }
            }
        }
    }
}

class BatchDrawSystem(
        private val batch: SpriteBatch,
        var drawSprites: Boolean = true,
        var drawNames: Boolean = false
) : EntitySystem() {
    private val texMapper = mapperFor<Textured>()
    private val physicMapper = mapperFor<Physical>()
    private val positionMapper = mapperFor<Positioned>()
    private val namedMapper = mapperFor<Named>()

    private val font = BitmapFont()

    override fun update(deltaTime: Float) {
        if (drawSprites) {
            engine.getEntitiesFor(allOf(Textured::class).oneOf(Physical::class, Positioned::class).get()).forEach { e ->
                val texture = texMapper[e]?.texture
                val position = physicMapper[e]?.body?.position ?: positionMapper[e].position
                if (position != null) {
                    if (texture != null) {
                        // TODO check if object is visible from current viewport
                        batch.draw(texture,
                                position.x * PPM - texture.width / 2,
                                position.y * PPM - texture.height / 2)
                    }
                }
            }
        }
        if (drawNames) {
            engine.getEntitiesFor(allOf(Named::class).oneOf(Physical::class, Positioned::class).get()).forEach { e ->
                val name = namedMapper[e].name
                val position = physicMapper[e]?.body?.position ?: positionMapper[e].position
                if (!name.isNullOrBlank() && position != null) {
                    // TODO check if object is visible from current viewport
                    font.draw(batch, name, position.x * PPM, position.y * PPM)
                }
            }

        }
    }
}

class CameraSystem(private val camera: Camera) : EntitySystem() {
    private val physicMapper = mapperFor<Physical>()

    override fun update(deltaTime: Float) {
        engine.getEntitiesFor(allOf(Physical::class, PlayerControlled::class).get()).forEach { e ->
            physicMapper[e]?.body?.let { body ->
                camera.position.set(body.position.x * PPM, body.position.y * PPM, 0f)
            }
        }
    }
}
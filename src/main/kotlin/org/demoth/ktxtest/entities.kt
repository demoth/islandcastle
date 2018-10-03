package org.demoth.ktxtest

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.EntitySystem
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.physics.box2d.Body
import ktx.ashley.allOf
import ktx.ashley.mapperFor

class PlayerControlled : Component

class Physical(var body: Body? = null) : Component

class Textured(var texture: Texture? = null) : Component

class PlayerControlSystem : EntitySystem() {
    private val physicMapper = mapperFor<Physical>()
    private val playerCtrlMapper = mapperFor<PlayerControlled>()

    override fun update(deltaTime: Float) {
        engine.getEntitiesFor(allOf(Physical::class, PlayerControlled::class).get()).forEach { e ->
            // this may be used later to affect how controls are used
            val control = playerCtrlMapper[e]
            physicMapper[e]?.body?.let { body ->
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
            }
        }
    }
}

class BatchDrawSystem(val batch: SpriteBatch) : EntitySystem() {
    private val texMapper = mapperFor<Textured>()
    private val physicMapper = mapperFor<Physical>()
    override fun update(deltaTime: Float) {
        engine.getEntitiesFor(allOf(Textured::class, Physical::class).get()).forEach { e ->
            val texture = texMapper[e]?.texture
            val position = physicMapper[e]?.body?.position
            if (texture != null && position != null)
                batch.draw(texture,
                        position.x * PPM - texture.width / 2,
                        position.y * PPM - texture.height / 2)
        }
    }
}

class CameraSystem(val camera: Camera) : EntitySystem() {
    private val physicMapper = mapperFor<Physical>()

    override fun update(deltaTime: Float) {
        engine.getEntitiesFor(allOf(Physical::class, PlayerControlled::class).get()).forEach { e ->
            physicMapper[e]?.body?.let { body ->
                camera.position.set(body.position.x * PPM, body.position.y * PPM, 0f)
            }
        }
    }
}
package org.demoth.icastle.ecs.systems

import com.badlogic.ashley.core.EntitySystem
import com.badlogic.gdx.graphics.Camera
import org.demoth.icastle.PPM
import org.demoth.icastle.ecs.physicMapper
import org.demoth.icastle.ecs.playerPosition

/**
 * Moves camera view to center on player
 */
class CameraSystem(private val camera: Camera) : EntitySystem() {
    override fun update(deltaTime: Float) {
        engine.getEntitiesFor(playerPosition).forEach { e ->
            physicMapper[e]?.body?.let { body ->
                camera.position.set(body.position.x * PPM, body.position.y * PPM, 0f)
            }
        }
    }
}
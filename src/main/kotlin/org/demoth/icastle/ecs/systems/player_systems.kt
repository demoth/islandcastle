package org.demoth.icastle.ecs.systems

import com.badlogic.ashley.core.EntitySystem
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import org.demoth.icastle.IngameHud
import org.demoth.icastle.WALK_FORCE
import org.demoth.icastle.ecs.*

/**
 * Moves player in the physical world
 */
class PlayerControlSystem(private val entityFactory: EntityFactory) : EntitySystem() {
    /**
     * location relative to player (center)
     */
    var leftClick: Vector2? = null

    override fun update(deltaTime: Float) {
        engine.getEntitiesFor(playerEntities).firstOrNull()?.let { playerEntity ->
            // this may be used later to affect how controls are used
            val player = playerMapper[playerEntity]
            val physics = physicMapper[playerEntity]

            physics?.body?.let { body ->
                // Walking code
                updateWalkDirection(playerEntity, getPlayerTargetLocation(body))

                // Action code
                leftClick = leftClick?.let { click ->
                    firePlayerAction(click, player, body.position, playerEntity, entityFactory)
                    null
                }
            }
        }
    }

    /**
     * calculate where player should move based on the user input
     */
    private fun getPlayerTargetLocation(body: Body): Vector2? {
        var movementX: Float? = null
        var movementY: Float? = null

        if (Gdx.input.isKeyPressed(Input.Keys.W)) {
            movementY = WALK_FORCE
        }
        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            movementX = -WALK_FORCE
        }
        if (Gdx.input.isKeyPressed(Input.Keys.S)) {
            movementY = -WALK_FORCE
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            movementX = WALK_FORCE
        }
        val direction = if (movementX != null) {
            if (movementY != null) {
                Vector2(movementX, movementY)
            } else {
                Vector2(movementX, 0f)
            }
        } else {
            if (movementY != null) {
                Vector2(0f, movementY)
            } else {
                null
            }
        }
        val targetLocation = direction?.add(body.position)
        return targetLocation
    }
}

class PlayerHudUpdateSystem(private val hud: IngameHud) : EntitySystem() {
    override fun update(deltaTime: Float) {
        engine.getEntitiesFor(playerHealthAndScore).firstOrNull()?.let {
            val player = playerMapper[it]
            val health = healthMapper[it]

            hud.setValues(health.value, player.score)

            player.score-- // TODO: stats change should be done separately!!!
        }
    }
}

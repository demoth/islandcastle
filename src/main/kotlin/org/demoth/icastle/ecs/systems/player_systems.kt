package org.demoth.icastle.ecs.systems

import com.badlogic.ashley.core.EntitySystem
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.math.Vector2
import org.demoth.icastle.ecs.*
import org.demoth.icastle.ui.screens.IngameHud
import org.demoth.icastle.ui.screens.WALK_FORCE

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

            val playerPosition = physics?.body?.position ?: throw IllegalStateException("No player body")

            // Walking code
            updateWalkDirection(playerEntity, getPlayerTargetLocation(playerPosition))

            // Action code
            leftClick = leftClick?.let { click ->
                player.selectedAction?.fire(click, playerEntity, entityFactory)
                null
            }

        }
    }

    /**
     * calculate where player should move based on the user input
     */
    private fun getPlayerTargetLocation(playerPosition: Vector2): Vector2? {
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
        return direction?.add(playerPosition)
    }
}

class PlayerHudUpdateSystem(private val hud: IngameHud) : EntitySystem() {
    override fun update(deltaTime: Float) {
        engine.getEntitiesFor(playerHealthAndScore).firstOrNull()?.let {
            hud.setValues(it)
        }
    }
}

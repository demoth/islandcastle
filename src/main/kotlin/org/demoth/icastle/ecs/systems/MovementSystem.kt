package org.demoth.icastle.ecs.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.EntitySystem
import com.badlogic.gdx.math.Vector2
import ktx.math.minus
import org.demoth.icastle.ecs.*

/**
 * Moves entities based on movement value and type
 */
class MovementSystem : EntitySystem() {
    override fun update(deltaTime: Float) {
        engine.getEntitiesFor(movables).forEach {
            val movement = movementMapper[it]
            val physical = physicMapper[it] ?: throw IllegalStateException("Missing ")
            when (movement.type) {
                MovementType.LINEAR_VELOCITY -> TODO()
                MovementType.FORCE -> {
                    physical.body.applyForceToCenter(movement.value, true)
                }
            }
        }
    }
}

/**
 * Checks that entity is not closer that `minDistance`, normalizes speed to max speed and updates animation direction.
 * Required Components: Phisycal, Movement.
 *
 * If targetLocation is null then movement is reset.
 */

internal fun updateWalkDirection(entity: Entity, targetLocation: Vector2?) {
    val movement = movementMapper[entity]
    val currentLocation = physicMapper[entity].body.position
    movement.value = if (targetLocation != null) {
        val charAnimation = characterAnimationMapper[entity]
        val distanceVector = targetLocation.minus(currentLocation)
        if (movement.minDistance > 0f && distanceVector.len() > movement.minDistance || movement.minDistance <= 0f) {
            val walkVector = distanceVector.setLength(movement.maxSpeed)
            charAnimation?.currentDirection = getDirectionFromVector(walkVector.cpy())
            Vector2(walkVector.x, walkVector.y)
        } else {
            // close enough
            Vector2.Zero
        }
    } else {
        // no target to follow
        Vector2.Zero
    }
}

private fun getDirectionFromVector(dir: Vector2): Direction {
    return if (dir.x == 0f && dir.y == 0f)
        Direction.UP
    else if (dir.y > dir.x && dir.y > -dir.x)
        Direction.UP
    else if (dir.y < dir.x && dir.y < -dir.x)
        Direction.DOWN
    else if (dir.x >= dir.y && dir.x >= -dir.y)
        Direction.RIGHT
    else Direction.LEFT
}

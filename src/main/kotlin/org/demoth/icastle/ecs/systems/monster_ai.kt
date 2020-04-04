package org.demoth.icastle.ecs.systems

import com.badlogic.ashley.core.EntitySystem
import ktx.ashley.allOf
import ktx.math.minus
import org.demoth.icastle.debug
import org.demoth.icastle.ecs.*

/**
 * Searches for player and tries to direct monsters towards the player
 */
class MonsterWalkSystem : EntitySystem() {
    override fun update(deltaTime: Float) {
        val playerEntity = engine.getEntitiesFor(allOf(Player::class, Physical::class).get()).firstOrNull()

        engine.getEntitiesFor(monstersWalking).forEach { entity ->
            val targetLocation = if (playerEntity != null) {
                physicMapper[playerEntity].body.position.cpy()
            } else {
                null
            }
            updateWalkDirection(entity, targetLocation)
        }
    }
}

/**
 * If not in a cooldown, find a player and shoot towards player's position
 */
class MonsterFiringSystem(private val entityFactory: EntityFactory) : EntitySystem() {
    override fun update(deltaTime: Float) {
        val playerEntity = engine.getEntitiesFor(allOf(Player::class, Physical::class).get()).firstOrNull()

        engine.getEntitiesFor(monstersFiring).forEach { monsterEntity ->
            val monster = monsterMapper[monsterEntity]
            val monsterPhysics = physicMapper[monsterEntity]
            monster.currentTime += deltaTime

            if (monster.currentTime > monster.fireRate) {
                monster.currentTime = 0f
                if (playerEntity != null) {
                    val playerPhysical = physicMapper[playerEntity]
                    val monsterPosition = monsterPhysics.body.position
                    val playerPosition = playerPhysical.body.position.cpy()
                    if (playerPosition.minus(monsterPosition).len() < 10f)   //TODO move constant to monster
                        entityFactory.createRotatingFireBall(playerPosition - monsterPosition,
                                monsterPosition,
                                monsterEntity)
                    debug("spawned fireball to player at (${playerPosition.x}, ${playerPosition.y})")
                }
            }
        }
    }
}

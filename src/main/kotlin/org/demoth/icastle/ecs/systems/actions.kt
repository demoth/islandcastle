package org.demoth.icastle.ecs.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.math.Vector2
import ktx.math.minus
import org.demoth.icastle.debug
import org.demoth.icastle.ecs.EntityFactory
import org.demoth.icastle.ecs.MonsterFiring
import org.demoth.icastle.ecs.Player

fun firePlayerAction(click: Vector2, player: Player, position: Vector2, playerEntity: Entity, entityFactory: EntityFactory) {
    player.score -= 3070
    entityFactory.createFireBall(click, position, playerEntity)
    debug("actionLocation: $click, player: $position")
}

fun fireMonsterAction(monster: MonsterFiring, playerPosition: Vector2?, monsterPosition: Vector2, monsterEntity: Entity, entityFactory: EntityFactory) {
    monster.currentTime = 0f
    if (playerPosition != null) {
        if (playerPosition.minus(monsterPosition).len() < monster.firingDistance)
            entityFactory.createRotatingFireBall(playerPosition - monsterPosition,
                    monsterPosition,
                    monsterEntity)
        debug("spawned fireball to player at (${playerPosition.x}, ${playerPosition.y})")
    }
}

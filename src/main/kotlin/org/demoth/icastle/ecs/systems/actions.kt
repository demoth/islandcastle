package org.demoth.icastle.ecs.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.math.Vector2
import ktx.ashley.get
import ktx.math.minus
import org.demoth.icastle.ACTION_FIREBALL
import org.demoth.icastle.debug
import org.demoth.icastle.ecs.EntityFactory
import org.demoth.icastle.ecs.monsterMapper
import org.demoth.icastle.ecs.physicMapper
import org.demoth.icastle.ecs.playerMapper

abstract class Action(val name: String, val icon: String) {
    abstract fun fire(position: Vector2, owner: Entity, entityFactory: EntityFactory): Unit
}

class FireballAction : Action("Fireball", ACTION_FIREBALL) {
    override fun fire(position: Vector2, owner: Entity, entityFactory: EntityFactory) {
        owner[playerMapper]?.score?.minus(3070)
        entityFactory.createFireBall(position, owner[physicMapper]?.body?.position!!, owner)
    }
}

class MonsterFireballAction : Action("MonsterFireball", ACTION_FIREBALL) {
    override fun fire(position: Vector2, owner: Entity, entityFactory: EntityFactory) {
        val monster = owner[monsterMapper] ?: return
        monster.currentTime = 0f
        val ownerPosition = owner[physicMapper]?.body?.position ?: return
        if (position.minus(ownerPosition).len() < monster.firingDistance)
            entityFactory.createRotatingFireBall(position - ownerPosition, ownerPosition, owner)
        debug("spawned fireball to player at ($position)")
    }
}

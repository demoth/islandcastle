package org.demoth.icastle.ecs.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.math.Vector2
import ktx.ashley.get
import ktx.math.minus
import org.demoth.icastle.*
import org.demoth.icastle.ecs.*

abstract class Action(val name: String, val icon: String) {
    abstract fun fire(position: Vector2, owner: Entity, entityFactory: EntityFactory): Unit
}

class HealAction : Action("Heal", ACTION_HEAL) {
    override fun fire(position: Vector2, owner: Entity, entityFactory: EntityFactory) {
        val health = owner[healthMapper] ?: return
        val playerPosition = owner[physicMapper]?.body?.position ?: return
        health.value += 1000
        entityFactory.createFloatingLabel("+1000 hp", playerPosition, 3f)
        entityFactory.createSound(Sounds.HEAL)
    }
}

class FireballAction : Action("Fireball", ACTION_FIREBALL) {
    override fun fire(position: Vector2, owner: Entity, entityFactory: EntityFactory) {
        owner[playerMapper]?.score?.minus(3070)
        entityFactory.createFireBall(position, owner[physicMapper]?.body?.position!!, owner)
    }
}

class MonsterFireballAction : Action("MonsterFireball", ACTION_ATTACK) {
    override fun fire(position: Vector2, owner: Entity, entityFactory: EntityFactory) {
        val monster = owner[monsterMapper] ?: return
        monster.currentTime = 0f
        val ownerPosition = owner[physicMapper]?.body?.position ?: return
        if (position.minus(ownerPosition).len() < monster.firingDistance)
            entityFactory.createRotatingFireBall(position - ownerPosition, ownerPosition, owner)
        debug("spawned fireball to player at ($position)")
    }
}

package org.demoth.ktxtest

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.physics.box2d.Contact
import ktx.ashley.get
import org.demoth.ktxtest.CollisionClass.*

class CollisionProcessor(private val engine: Engine) : ContactAdapter() {
    override fun beginContact(contact: Contact?) {
        if (contact != null && contact.isTouching) {

            val entityA = contact.fixtureA?.body?.userData
            val entityB = contact.fixtureB?.body?.userData
            if (entityA is Entity && entityB is Entity) {
                val physicalA = entityA.getComponent(Physical::class.java)
                val physicalB = entityB.getComponent(Physical::class.java)
                if (physicalA is Physical && physicalB is Physical) {
                    if (physicalA.collisionClass == DEAL_DAMAGE
                            && physicalB.collisionClass in setOf(RECEIVE_DAMAGE, SOLID)) {
                        if (physicalA.owner != physicalB.owner)
                            removeMissileAndApplyDamage(physicalA, entityB)
                    }
                    if (physicalB.collisionClass == DEAL_DAMAGE
                            && physicalA.collisionClass in setOf(RECEIVE_DAMAGE, SOLID)) {
                        if (physicalA.owner != physicalB.owner)
                            removeMissileAndApplyDamage(physicalB, entityA)
                    }
                    if (physicalA.collisionClass == TRIGGER && entityB.getComponent(Player::class.java) != null
                            || physicalB.collisionClass == TRIGGER && entityA.getComponent(Player::class.java) != null) {
                        val trigger = entityA.getComponent(Trigger::class.java)
                                ?: entityB.getComponent(Trigger::class.java)
                        val player = entityA.getComponent(Player::class.java)
                                ?: entityB.getComponent(Player::class.java)
                        trigger.action.invoke(player.score)
                    }
                }
            }
        }
    }

    private fun removeMissileAndApplyDamage(missile: Physical, receiverEntity: Entity) {
        missile.toBeRemoved = true
        val receiverPhysical = receiverEntity.getComponent(Physical::class.java)
        receiverPhysical.body.applyLinearImpulse(missile.body.linearVelocity.cpy().scl(missile.force), receiverPhysical.body.localCenter, true)
        if (receiverPhysical.collisionClass == RECEIVE_DAMAGE)
            createFloatingLabel(engine, "-3070", receiverPhysical.body.position.cpy())
        val player = receiverEntity.getComponent(Player::class.java)
        if (player != null) {
            player.health -= 3070
            player.score -= 3070
        } else {
            val monster = receiverEntity.getComponent(MonsterStationaryRanged::class.java)
            if (monster != null) {
                monster.health -= 3070
            }

        }

    }
}
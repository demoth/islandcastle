package org.demoth.ktxtest

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.physics.box2d.Contact
import ktx.ashley.get
import org.demoth.ktxtest.CollisionClass.RECEIVE_DAMAGE

class CollisionProcessor(private val engine: Engine) : ContactAdapter() {
    override fun beginContact(contact: Contact?) {
        if (contact != null && contact.isTouching) {

            val entityA = contact.fixtureA?.body?.userData
            val entityB = contact.fixtureB?.body?.userData
            if (entityA is Entity && entityB is Entity) {
                entityA.get<Physical>()?.collision?.invoke(entityA, entityB)
                entityB.get<Physical>()?.collision?.invoke(entityB, entityA)
//
//
//
//                val physicalA = entityA.get<Physical>()
//                val physicalB = entityB.get<Physical>()
//                if (physicalA is Physical && physicalB is Physical) {
//                    if (physicalA.collisionMask == DEAL_DAMAGE
//                            && physicalB.collisionMask in setOf(RECEIVE_DAMAGE, SOLID)) {
//                        if (physicalA.owner != physicalB.owner)
//                            removeMissileAndApplyDamage(physicalA, entityB)
//                    }
//                    if (physicalB.collisionMask == DEAL_DAMAGE
//                            && physicalA.collisionMask in setOf(RECEIVE_DAMAGE, SOLID)) {
//                        if (physicalA.owner != physicalB.owner)
//                            removeMissileAndApplyDamage(physicalB, entityA)
//                    }
//                    if (physicalA.collisionMask == TRIGGER && entityB.get<Player>() != null
//                            || physicalB.collisionMask == TRIGGER && entityA.get<Player>() != null) {
//                        val trigger: Trigger? = entityA.get() ?: entityB.get()
//                        val player: Player? = entityA.get() ?: entityB.get()
//                        player?.let {
//                            trigger?.action?.invoke(it.score)
//                        }
//                    }
//                }
            }
        }
    }

//    private fun removeMissileAndApplyDamage(missile: Physical, receiverEntity: Entity) {
//        missile.toBeRemoved = true
//        val receiverPhysical = receiverEntity.getComponent(Physical::class.java)
//        receiverPhysical.body.applyLinearImpulse(missile.body.linearVelocity.cpy().scl(missile.force), receiverPhysical.body.localCenter, true)
//        if (receiverPhysical.collisionMask == RECEIVE_DAMAGE)
//            createFloatingLabel(engine, "-3070", receiverPhysical.body.position.cpy())
//        val player = receiverEntity.getComponent(Player::class.java)
//        if (player != null) {
//            player.health -= 3070
//            player.score -= 3070
//        } else {
//            val monster = receiverEntity.getComponent(MonsterStationaryRanged::class.java)
//            if (monster != null) {
//                monster.health -= 3070
//            }
//
//        }
//
//    }
}
package org.demoth.ktxtest

import com.badlogic.gdx.physics.box2d.Contact

class CollisionProcessor : ContactAdapter() {
    override fun beginContact(contact: Contact?) {
        if (contact != null && contact.isTouching) {
            val dataA = contact.fixtureA?.body?.userData
            val dataB = contact.fixtureB?.body?.userData
            if (dataA is Physical && dataB is Physical) {
                if (dataA.collisionClass == CollisionClass.DEAL_DAMAGE
                        && dataB.collisionClass!! in setOf(CollisionClass.RECEIVE_DAMAGE, CollisionClass.SOLID)) {
                    if (dataA.owner != dataB.owner)
                        removeMissileAndApplyDamage(dataA, dataB)
                }
                if (dataB.collisionClass == CollisionClass.DEAL_DAMAGE
                        && dataA.collisionClass!! in setOf(CollisionClass.RECEIVE_DAMAGE, CollisionClass.SOLID)) {
                    if (dataA.owner != dataB.owner)
                        removeMissileAndApplyDamage(dataB, dataA)
                }
            }
        }
    }

    fun removeMissileAndApplyDamage(missile: Physical, receiver: Physical) {
        missile.toBeRemoved = true
        receiver.body?.applyLinearImpulse(missile.body!!.linearVelocity.cpy().scl(missile.force), receiver.body!!.localCenter, true)
    }
}
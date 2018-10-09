package org.demoth.ktxtest

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.physics.box2d.Contact
import ktx.ashley.get

class CollisionProcessor : ContactAdapter() {
    override fun beginContact(contact: Contact?) {
        if (contact != null && contact.isTouching) {
            val entityA = contact.fixtureA?.body?.userData
            val entityB = contact.fixtureB?.body?.userData
            if (entityA is Entity && entityB is Entity) {
                val physicalA = entityA.get<Physical>()
                val physicalB = entityB.get<Physical>()
                if (physicalA != null && physicalB != null) {

                    if (physicalA.collidesWith and physicalB.collisionClass > 0)
                        physicalA.collide?.invoke(entityA, entityB)

                    if (physicalB.collidesWith and physicalA.collisionClass > 0)
                        physicalB.collide?.invoke(entityB, entityA)
                }
            }
        }
    }
}
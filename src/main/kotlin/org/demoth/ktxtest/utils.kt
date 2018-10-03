package org.demoth.ktxtest

import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Contact
import com.badlogic.gdx.physics.box2d.ContactImpulse
import com.badlogic.gdx.physics.box2d.ContactListener
import com.badlogic.gdx.physics.box2d.Manifold

/**
 * assumes rectangle dimensions and location are in pixels.
 * returns location in physical space
 */
fun Rectangle.getCentralPoint(): Vector2 {
    val vector2 = Vector2()
    getCenter(vector2)
    return vector2.scl(1 / PPM)
}

open class ContactAdapter : ContactListener {
    override fun endContact(contact: Contact?) {
    }

    override fun beginContact(contact: Contact?) {
    }

    override fun preSolve(contact: Contact?, oldManifold: Manifold?) {
    }

    override fun postSolve(contact: Contact?, impulse: ContactImpulse?) {
    }
}
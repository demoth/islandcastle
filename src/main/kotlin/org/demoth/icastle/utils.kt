package org.demoth.icastle

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.TextureRegion
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

fun createSimpleAnimation(spriteSheet: Texture, cols: Int, rows: Int, duration: Float, mode: Animation.PlayMode): Animation<TextureRegion> {
    val flameFrames = TextureRegion.split(
            spriteSheet,
            spriteSheet.width / cols,
            spriteSheet.height / rows).flatten()
    val animation = Animation(duration, *flameFrames.toTypedArray())
    animation.playMode = mode
    return animation
}

fun createAnimationFromFrames(regions: List<TextureRegion>, duration: Float, frameIndices: List<Int>): Animation<TextureRegion> {
    val animation = Animation(duration, *frameIndices.map { regions[it] }.toTypedArray())
    animation.playMode = Animation.PlayMode.LOOP
    return animation
}


fun debug(message: String) {
    println(message)
}
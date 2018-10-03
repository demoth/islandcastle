package org.demoth.ktxtest

import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2

/**
 * assumes rectangle dimensions and location are in pixels.
 * returns location in physical space
 */
fun Rectangle.getCentralPoint(): Vector2 {
    val vector2 = Vector2()
    getCenter(vector2)
    return vector2.scl(1 / PPM)
}
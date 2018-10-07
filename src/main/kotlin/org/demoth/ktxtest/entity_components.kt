package org.demoth.ktxtest

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import java.util.*

/**
 * Marker used by PlayerControlSystem to control player movements
 */
class PlayerControlled : Component

/**
 * Position in physical space units
 */
class Positioned(val position: Vector2) : Component

class Physical(
        val body: Body,
        val collisionClass: CollisionClass,
        val owner: String = "",
        var force: Float = 1f, // how strong fireballs can push
        var toBeRemoved: Boolean = false
) : Component {
    init {
        body.userData = this
    }
}

class Animated(val animation: Animation<TextureRegion>) : Component

class Textured(val texture: Texture) : Component

class Named(val name: String) : Component

/**
 * will fire fireRate/sec towards player
 */
class MonsterStationaryRanged(var fireRate: Float = 1f, var currentTime: Float = Random().nextFloat()) : Component

/**
 * Used for damage labels - they float up a bit then disappear
 */
class FloatingUpLabel(var ttl: Float = 2f) : Component
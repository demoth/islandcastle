package org.demoth.ktxtest

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body

/**
 * Marker used by PlayerControlSystem to control player movements
 */
class PlayerControlled : Component

/**
 * Position in physical space units
 */
class Positioned(var position: Vector2? = null) : Component

class Physical(
        var body: Body? = null,
        var toBeRemoved: Boolean = false,
        var collisionClass: CollisionClass? = null,
        var force: Float = 1f,
        var owner: String? = null
) : Component

class Animated(var animation: Animation<TextureRegion>? = null) : Component

class Textured(var texture: Texture? = null) : Component

class Named(var name: String? = null) : Component
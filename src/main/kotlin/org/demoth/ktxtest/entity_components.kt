package org.demoth.ktxtest

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import java.util.*

/**
 * Marker used by PlayerControlSystem to control player movements
 */
class Player(var score: Int = 666 * 30) : Component

/**
 * Position in physical space units
 */
class Positioned(val position: Vector2) : Component

class Physical(
        val body: Body,
        var toBeRemoved: Boolean = false,
        val collision: ((self: Entity, other: Entity) -> Unit)? = null
) : Component

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

class Trigger(val action: (Int) -> Unit) : Component

class Damage(val value: Int, val owner: Entity) : Component

class Health(var value: Int) : Component
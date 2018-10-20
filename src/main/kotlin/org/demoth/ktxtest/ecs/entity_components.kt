package org.demoth.ktxtest.ecs

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import org.demoth.ktxtest.NO_COLLISION
import org.demoth.ktxtest.Sounds
import org.demoth.ktxtest.SpriteSheets
import org.demoth.ktxtest.Sprites
import org.demoth.ktxtest.createAnimation
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
        val collisionClass: Long = NO_COLLISION,
        val collidesWith: Long = NO_COLLISION,
        val collide: ((self: Entity, other: Entity) -> Unit)? = null
) : Component

class Animated(
        val sheets: SpriteSheets,
        private val duration: Float,
        private val mode: Animation.PlayMode,
        var currentTime: Float = 0f,
        private var animation: Animation<TextureRegion>? = null) : Component {

    fun isInitialized(): Boolean {
        return animation != null
    }

    fun initialize(texture: Texture?) {
        animation = createAnimation(texture!!, sheets.cols, sheets.rows, duration, mode)
    }

    /**
     * For one time animations, when it is finished, it will be disposed
     */
    fun isExpired(): Boolean {
        if (animation == null)
            return false
        return mode == Animation.PlayMode.NORMAL
                && animation!!.isAnimationFinished(currentTime)
    }

    fun getKeyFrame(): TextureRegion? {
        return animation?.getKeyFrame(currentTime)
    }

}

class Textured(val texture: Sprites) : Component

class Named(val name: String) : Component

/**
 * will fire fireRate/sec towards player
 */
class MonsterStationaryRanged(var fireRate: Float = 1f, var currentTime: Float = Random().nextFloat()) : Component

/**
 * Used for damage labels - they float up a bit then disappear
 */
class FloatingUpLabel(val speed: Vector2) : Component

class HasDamage(val value: Int, val owner: Entity) : Component

class HasHealth(var value: Int) : Component

class HasSound(val name: Sounds, var id: Long = 0) : Component

class TTL(var ttl: Float) : Component
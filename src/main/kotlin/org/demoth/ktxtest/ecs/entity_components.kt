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
import java.util.Random


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

interface Animated : Component {
    fun isInitialized(): Boolean
    fun initialize(texture: Texture)
    /**
     * For one time animations, when it is finished, it will be disposed
     */
    fun isExpired(): Boolean

    fun getKeyFrame(): TextureRegion?
    var currentTime: Float
    val sheets: SpriteSheets
}

/**
 * Animations that have no state
 */
class SimpleAnimation(
        override val sheets: SpriteSheets,
        private val frameDuration: Float,
        private val mode: Animation.PlayMode,
        override var currentTime: Float = 0f) : Animated {

    private var animation: Animation<TextureRegion>? = null

    override fun isInitialized(): Boolean {
        return animation != null
    }

    override fun initialize(texture: Texture) {
        animation = createSimpleAnimation(texture, sheets.cols, sheets.rows, frameDuration, mode)
    }

    override fun isExpired(): Boolean {
        if (animation == null)
            return false
        return mode == Animation.PlayMode.NORMAL
                && animation!!.isAnimationFinished(currentTime)
    }

    override fun getKeyFrame(): TextureRegion? {
        return animation?.getKeyFrame(currentTime)
    }

}

enum class AnimationSequences(val frames: Int) {
    CAST(7),
    THRUST(8),
    WALK(9),
    SLASH(6),
    SHOOT(13),
    //HURT(6)
}

enum class Direction {
    UP, LEFT, DOWN, RIGHT
}

class CharacterAnimation(
        override val sheets: SpriteSheets,
        override var currentTime: Float = 0f) : Animated {

    private val sequences: EnumMap<AnimationSequences, EnumMap<Direction, Animation<TextureRegion>>> = EnumMap(AnimationSequences::class.java)

    var currentDirection = Direction.DOWN

    var currentSequence: AnimationSequences = AnimationSequences.WALK
        set(value) {
            field = value
            currentTime = 0f
        }

    private fun getCurrentAnimation(): Animation<TextureRegion> {
        return sequences[currentSequence]!![currentDirection]!!
    }

    override fun isInitialized(): Boolean {
        return !sequences.isEmpty()
    }

    override fun initialize(texture: Texture) {
        // iterate over sequences
        // iterate over directions
        val regions = TextureRegion.split(
                texture,
                texture.width / sheets.cols,
                texture.height / sheets.rows).flatten()
        AnimationSequences.values().forEach { sequence ->
            sequences[sequence] = EnumMap(Direction::class.java)
            Direction.values().forEach { dir ->
                val startIndex = (sequence.ordinal * 4 + dir.ordinal) * sheets.cols
                val frames = startIndex..(startIndex + sequence.frames - 1)
                sequences[sequence]!![dir] = createAnimationFromFrames(regions, 0.1f, frames.toList())
            }
        }
    }

    override fun isExpired(): Boolean {
        return false
    }

    override fun getKeyFrame(): TextureRegion? {
        return getCurrentAnimation().getKeyFrame(currentTime)
    }
}

class Textured(val texture: Sprites) : Component

class Named(val name: String) : Component

/**
 * will fire fireRate/sec towards player
 */
class MonsterFiring(var fireRate: Float = 1f, var currentTime: Float = Random().nextFloat()) : Component

/**
 * will magnetize monster towards player
 */
class MonsterWalking(var speed: Float = 3f, var distance: Float = 2f) : Component

/**
 * Used for damage labels - they float up a bit then disappear
 */
class FloatingUpLabel(val speed: Vector2) : Component

class HasDamage(val value: Int, val owner: Entity) : Component

class HasHealth(var value: Int) : Component

class HasSound(val name: Sounds, var id: Long = 0) : Component

class TTL(var ttl: Float) : Component
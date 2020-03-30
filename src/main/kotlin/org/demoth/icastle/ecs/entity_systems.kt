package org.demoth.icastle.ecs

import com.badlogic.ashley.core.EntitySystem
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.utils.Disposable
import ktx.ashley.allOf
import ktx.ashley.entity
import ktx.ashley.mapperFor
import ktx.math.minus
import org.demoth.icastle.*
import java.util.*

val physicMapper = mapperFor<Physical>()
val playerMapper = mapperFor<Player>()
val texMapper = mapperFor<Textured>()
val positionMapper = mapperFor<Positioned>()
val namedMapper = mapperFor<Named>()
val animatedMapper = mapperFor<SimpleAnimation>()
val characterAnimationMapper = mapperFor<CharacterAnimation>()
val monsterMapper = mapperFor<MonsterFiring>()
val walkMapper = mapperFor<MonsterWalking>()
val floatingUpLabelMapper = mapperFor<FloatingUpLabel>()
val healthMapper = mapperFor<HasHealth>()
val soundMapper = mapperFor<HasSound>()
val ttlMapper = mapperFor<TTL>()
val movementMapper = mapperFor<Movement>()

/**
 * Moves player in the physical world
 */
class PlayerControlSystem(private val entityFactory: EntityFactory) : EntitySystem() {
    /**
     * location relative to player (center)
     */
    var actionLocation: Vector2? = null

    override fun update(deltaTime: Float) {
        engine.getEntitiesFor(playerEntities).firstOrNull()?.let { playerEntity ->
            // this may be used later to affect how controls are used
            val player = playerMapper[playerEntity]
            val playerPhysics = physicMapper[playerEntity]
            val playerAnimation = characterAnimationMapper[playerEntity]
            val movement = movementMapper[playerEntity]

            playerPhysics?.body?.let { body ->
                var movementX = 0f
                var movementY = 0f

                if (Gdx.input.isKeyPressed(Input.Keys.W) && body.linearVelocity.y < MAX_SPEED) {
                    movementY = WALK_FORCE
                    playerAnimation.currentDirection = Direction.UP
                }
                if (Gdx.input.isKeyPressed(Input.Keys.A) && body.linearVelocity.x > -MAX_SPEED) {
                    movementX = -WALK_FORCE
                    playerAnimation.currentDirection = Direction.LEFT
                }
                if (Gdx.input.isKeyPressed(Input.Keys.S) && body.linearVelocity.y > -MAX_SPEED) {
                    movementY = -WALK_FORCE
                    playerAnimation.currentDirection = Direction.DOWN
                }
                if (Gdx.input.isKeyPressed(Input.Keys.D) && body.linearVelocity.x < MAX_SPEED) {
                    movementX = WALK_FORCE
                    playerAnimation.currentDirection = Direction.RIGHT
                }
                movement.value = Vector2(movementX, movementY)
                if (actionLocation != null) {
                    player.score -= 3070
                    entityFactory.createFireBall(actionLocation!!, body.position, playerEntity)
                    println("actionLocation: $actionLocation, player: ${body.position}")
                    actionLocation = null
                }
            }
        }
    }
}

class MovementSystem : EntitySystem() {
    override fun update(deltaTime: Float) {
        engine.getEntitiesFor(movables).forEach {
            val movement = movementMapper[it]
            val physical = physicMapper[it]
            when (movement.type) {
                MovementType.LINEAR_VELOCITY -> TODO()
                MovementType.FORCE -> {
                    physical.body.applyForceToCenter(movement.value, true)
                }
            }
        }
    }
}

class PlayerHudUpdateSystem(private val hud: IngameHud) : EntitySystem() {
    override fun update(deltaTime: Float) {
        engine.getEntitiesFor(playerHealthAndScore).firstOrNull()?.let {
            val player = playerMapper[it]
            val health = healthMapper[it]

            hud.setValues(health.value, player.score)

            player.score-- // TODO: stats change should be done separately!!!
        }
    }
}

/**
 * Draw everything that has a position, and texture or name
 */
class BatchDrawSystem(
        private val batch: SpriteBatch,
        var drawSprites: Boolean = true,
        var drawNames: Boolean = false
) : EntitySystem(), Disposable {
    private val font = BitmapFont()
    var time = 0f

    private val spriteMap = Sprites.values().map { it to Texture(Gdx.files.internal("sprites/${it.filename}")) }.toMap()
    private val spriteSheetMap = SpriteSheets.values().map { it to Texture(Gdx.files.internal("spriteSheets/${it.filename}")) }.toMap()

    override fun update(deltaTime: Float) {
        time += deltaTime

        if (drawSprites) {
            engine.getEntitiesFor(drawables).forEach { e ->
                val texture = spriteMap[texMapper[e]?.texture]
                val animated: Animated? = animatedMapper[e] ?: characterAnimationMapper[e]
                val position = physicMapper[e]?.body?.position ?: positionMapper[e].position
                if (texture != null) {
                    batch.draw(texture,
                            position.x * PPM - texture.width / 2,
                            position.y * PPM - texture.height / 2)
                } else {
                    if (animated != null) {
                        if (!animated.isInitialized()) {
                            animated.initialize(spriteSheetMap[animated.sheets]!!)
                        }
                        animated.currentTime += deltaTime
                        if (animated.isExpired()) {
                            engine.removeEntity(e)
                        } else {
                            animated.getKeyFrame()?.let {
                                batch.draw(it,
                                        position.x * PPM - it.regionWidth / 2,
                                        position.y * PPM - it.regionHeight / 2)
                            }
                        }
                    }
                }
            }
        }

        if (drawNames) {
            engine.getEntitiesFor(drawableNames).forEach { e ->
                val name = namedMapper[e].name
                val position = physicMapper[e]?.body?.position ?: positionMapper[e].position
                if (name.isNotBlank()) {
                    val g = GlyphLayout(font, name)
                    font.draw(batch, g, position.x * PPM - g.width / 2, position.y * PPM)
                }
            }
        }

        // todo: split into named, ttl and has speed
        // todo: moving should be done separately
        // todo: removing should be done separately
        engine.getEntitiesFor(floatingLabels).forEach {
            val floating = floatingUpLabelMapper[it]
            val positioned = positionMapper[it]
            val named = namedMapper[it]
            val ttl = ttlMapper[it]
            if (floating != null && positioned != null && named != null && ttl != null) {
                ttl.ttl -= deltaTime
                if (ttl.ttl > 0) {
                    positioned.position.add(floating.speed)
                    val g = GlyphLayout(font, named.name)
                    font.draw(batch, g, positioned.position.x * PPM - g.width / 2, positioned.position.y * PPM)
                } else {
                    engine.removeEntity(it)
                }
            }
        }

    }

    override fun dispose() {
        spriteMap.values.forEach { it.dispose() }
        spriteSheetMap.values.forEach { it.dispose() }
    }
}

/**
 * Cleans up entities marked as to be removed or temporary entites
 */
class EntitiesCleanupSystem(private val world: World) : EntitySystem() {
    override fun update(deltaTime: Float) {
        engine.getEntitiesFor(entityCleanup).forEach { e ->
            val physical = physicMapper[e]
            val ttl = ttlMapper[e]
            if (physical != null && physical.toBeRemoved) {
                world.destroyBody(physical.body)
                engine.removeEntity(e)
            }
            if (ttl != null) {
                ttl.ttl -= deltaTime
                if (ttl.ttl < 0) {
                    engine.removeEntity(e)
                    if (physical != null)
                        world.destroyBody(physical.body)
                }
            }
        }
    }
}

/**
 * If not in a cooldown, find a player and shoot towards player's position
 */
class MonsterFiringSystem(private val entityFactory: EntityFactory) : EntitySystem() {
    override fun update(deltaTime: Float) {
        val playerEntity = engine.getEntitiesFor(allOf(Player::class, Physical::class).get()).firstOrNull()

        engine.getEntitiesFor(monstersFiring).forEach { monsterEntity ->
            val monster = monsterMapper[monsterEntity]
            val monsterPhysics = physicMapper[monsterEntity]
            monster.currentTime += deltaTime

            if (monster.currentTime > monster.fireRate) {
                monster.currentTime = 0f
                if (playerEntity != null) {
                    val playerPhysical = physicMapper[playerEntity]
                    val monsterPosition = monsterPhysics.body.position
                    val playerPosition = playerPhysical.body.position.cpy()
                    if (playerPosition.minus(monsterPosition).len() < 10f)   //TODO move constant to monster
                        entityFactory.createRotatingFireBall(playerPosition - monsterPosition,
                                monsterPosition,
                                monsterEntity)
                    debug("spawned fireball to player at (${playerPosition.x}, ${playerPosition.y})")
                }
            }
        }
    }
}

/**
 * Checks health and if <= 0 - kill
 */
class DeathSystem(private val world: World, private val entityFactory: EntityFactory) : EntitySystem() {
    override fun update(deltaTime: Float) {
        engine.getEntitiesFor(monstersMortal).forEach { e ->
            val physics = physicMapper[e]
            val health = healthMapper[e]
            val player = playerMapper[e]
            if (health.value <= 0) {
                engine.removeEntity(e)
                world.destroyBody(physics.body)
                if (player != null) {
                    engine.entity().add(HasSound(Sounds.PLAYER_DIE))
                    entityFactory.createFloatingLabel("You have died! Press F5 to restart. Score: ${player.score}", physics.body.position.cpy(), 10f)
                } else {
                    // monster
                    engine.entity().add(HasSound(Sounds.MONSTER_DIE))
                }
            }
        }
    }
}

/**
 * Pushes monster towards player's position. Updates animation
 */
class MonsterWalkSystem : EntitySystem() {
    override fun update(deltaTime: Float) {
        val playerEntity = engine.getEntitiesFor(allOf(Player::class, Physical::class).get()).firstOrNull()

        engine.getEntitiesFor(monstersWalking).forEach { monsterEntity ->
            val walk = walkMapper[monsterEntity]
            if (playerEntity != null) {
                val monsterPhysics = physicMapper[monsterEntity]
                val playerPhysical = physicMapper[playerEntity]
                val monsterAnimation = characterAnimationMapper[monsterEntity]
                val monsterLocation = monsterPhysics.body.position
                val distanceVector = playerPhysical.body.position.cpy().minus(monsterLocation)
                val movement = movementMapper[monsterEntity]

                if (distanceVector.len() > walk.distance) {
                    val distance = distanceVector.setLength(walk.speed)
                    movement.value = Vector2(distance.x, distance.y)
                    if (monsterAnimation != null) {
                        distance.rotate(45f)
                        monsterAnimation.currentDirection =
                                if (distance.x > 0 && distance.y > 0)
                                    Direction.RIGHT
                                else if (distance.x < 0 && distance.y > 0)
                                    Direction.UP
                                else if (distance.x < 0 && distance.y < 0)
                                    Direction.LEFT
                                else
                                    Direction.DOWN
                    }
                } else {
                    movement.value = Vector2.Zero
                }
            }
        }
    }
}

/**
 * Plays sounds and removes them
 */
class SoundSystem : EntitySystem(), Disposable {
    override fun dispose() {
        soundMap.values.forEach { it.dispose() }
    }

    private val soundMap = Sounds.values().map { it to Gdx.audio.newSound(Gdx.files.internal("sounds/${it.filename}")) }.toMap()

    override fun update(deltaTime: Float) {
        engine.getEntitiesFor(allSounds).forEach {
            val sound = soundMapper[it]
            val soundFile = soundMap[sound.name] ?: return
            // todo pan sound depending on location
            sound.id = soundFile.play(1f, 1f + Random().nextFloat() * 0.2f, 0f)
            it.remove(HasSound::class.java)
            // FIXME
            if (it.components.size() == 0)
                engine.removeEntity(it)
        }
    }
}
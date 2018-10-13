package org.demoth.ktxtest.ecs

import com.badlogic.ashley.core.EntitySystem
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.utils.Disposable
import com.badlogic.gdx.utils.viewport.Viewport
import ktx.ashley.allOf
import ktx.ashley.entity
import ktx.ashley.mapperFor
import ktx.math.minus
import org.demoth.ktxtest.MAX_SPEED
import org.demoth.ktxtest.PPM
import org.demoth.ktxtest.Sounds
import org.demoth.ktxtest.Sprites
import org.demoth.ktxtest.WALK_FORCE
import java.util.*

val physicMapper = mapperFor<Physical>()
val playerMapper = mapperFor<Player>()
val texMapper = mapperFor<Textured>()
val positionMapper = mapperFor<Positioned>()
val namedMapper = mapperFor<Named>()
val animatedMapper = mapperFor<Animated>()
val monsterMapper = mapperFor<MonsterStationaryRanged>()
val floatingUpLabelMapper = mapperFor<FloatingUpLabel>()
val healthMapper = mapperFor<HasHealth>()
val soundMapper = mapperFor<HasSound>()

/**
 * Moves player in the physical world
 */
class PlayerControlSystem(private val world: World) : EntitySystem() {
    /**
     * location relative to player (center)
     */
    var actionLocation: Vector2? = null

    override fun update(deltaTime: Float) {
        engine.getEntitiesFor(playerEntities).firstOrNull()?.let { playerEntity ->
            // this may be used later to affect how controls are used
            val player = playerMapper[playerEntity]
            val health = healthMapper[playerEntity]
            val playerPhysics = physicMapper[playerEntity]

            if (health.value < 0) {
                engine.entity().add(HasSound(Sounds.PLAYER_DIE))
                engine.entity().apply {
                    add(FloatingUpLabel(10f))
                    add(Named("You have died! Press F5 to restart. Score: ${player.score}"))
                    add(Positioned(playerPhysics.body.position.cpy()))
                }
                engine.removeEntity(playerEntity)
                world.destroyBody(playerPhysics.body)
                return
            }

            playerPhysics?.body?.let { body ->
                if (Gdx.input.isKeyPressed(Input.Keys.W) && body.linearVelocity.y < MAX_SPEED) {
                    body.applyForceToCenter(0f, WALK_FORCE, true)
                }
                if (Gdx.input.isKeyPressed(Input.Keys.A) && body.linearVelocity.x > -MAX_SPEED) {
                    body.applyForceToCenter(-WALK_FORCE, 0f, true)
                }
                if (Gdx.input.isKeyPressed(Input.Keys.S) && body.linearVelocity.y > -MAX_SPEED) {
                    body.applyForceToCenter(0f, -WALK_FORCE, true)
                }
                if (Gdx.input.isKeyPressed(Input.Keys.D) && body.linearVelocity.x < MAX_SPEED) {
                    body.applyForceToCenter(WALK_FORCE, 0f, true)
                }

                if (actionLocation != null) {
                    player.score -= 3070
                    createFireBall(engine, world, actionLocation!!, body.position, playerEntity)
                    println("actionLocation: $actionLocation")
                    println("player: ${body.position}")
                    actionLocation = null
                }
            }
        }
    }
}

/**
 * Draw everything that has a position, and texture or name
 */
class BatchDrawSystem(
        private val batch: SpriteBatch,
        private val viewport: Viewport,
        var drawSprites: Boolean = true,
        var drawNames: Boolean = false
) : EntitySystem(), Disposable {
    private val font = BitmapFont()
    var time = 0f

    private val spriteMap = Sprites.values().map { it to Texture(Gdx.files.internal(it.filename)) }.toMap()

    override fun update(deltaTime: Float) {
        time += deltaTime

        if (drawSprites) {
            engine.getEntitiesFor(drawables).forEach { e ->
                val texture = spriteMap[texMapper[e]?.texture]
                val animated = animatedMapper[e]
                val position = physicMapper[e]?.body?.position ?: positionMapper[e].position
                if (texture != null) {
                    // TODO check if object is visible from current viewport
                    batch.draw(texture,
                            position.x * PPM - texture.width / 2,
                            position.y * PPM - texture.height / 2)
                }
                val keyFrame = animated?.animation?.getKeyFrame(time)
                if (keyFrame != null) {
                    batch.draw(keyFrame,
                            position.x * PPM - keyFrame.regionWidth / 2,
                            position.y * PPM - keyFrame.regionHeight / 2)
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
        engine.getEntitiesFor(floatingLabels).forEach {
            val floating = floatingUpLabelMapper[it]
            val positioned = positionMapper[it]
            val named = namedMapper[it]
            floating.ttl -= deltaTime
            if (floating.ttl > 0) {
                positioned.position.y += 0.01f
                val g = GlyphLayout(font, named.name)
                font.draw(batch, g, positioned.position.x * PPM - g.width / 2, positioned.position.y * PPM)
            } else {
                engine.removeEntity(it)
            }
        }

        engine.getEntitiesFor(playerHealthAndScore).firstOrNull()?.let {
            val player = playerMapper[it]
            val health = healthMapper[it]
            val g = GlyphLayout(font, "Score: ${player.score}")
            val h = GlyphLayout(font, "HasHealth: ${health.value}")

            font.draw(batch, g, viewport.camera.position.x - g.width / 2, viewport.camera.position.y - viewport.screenHeight * 0.2f)
            font.draw(batch, h, viewport.camera.position.x - h.width / 2, viewport.camera.position.y - viewport.screenHeight * 0.23f)
            player.score--
        }
    }

    override fun dispose() {
        spriteMap.values.forEach { it.dispose() }
    }
}

/**
 * Moves camera view to center on player
 */
class CameraSystem(private val camera: Camera) : EntitySystem() {
    override fun update(deltaTime: Float) {
        engine.getEntitiesFor(playerPosition).forEach { e ->
            physicMapper[e]?.body?.let { body ->
                camera.position.set(body.position.x * PPM, body.position.y * PPM, 0f)
            }
        }
    }
}

class PhysicalSystem(private val world: World) : EntitySystem() {
    override fun update(deltaTime: Float) {
        engine.getEntitiesFor(physicalBodiesCleanup).forEach { e ->
            val physical = physicMapper[e]
            if (physical.toBeRemoved) {
                world.destroyBody(physical.body)
                engine.removeEntity(e)
            }
        }
    }
}

class MonsterAiSystem(private val world: World) : EntitySystem() {
    override fun update(deltaTime: Float) {
        engine.getEntitiesFor(stationaryMonsters).forEach { monsterEntity ->
            val monster = monsterMapper[monsterEntity]
            val monsterPhysics = physicMapper[monsterEntity]
            val health = healthMapper[monsterEntity]
            monster.currentTime += deltaTime
            if (health.value < 0) {
                engine.entity().add(HasSound(Sounds.MONSTER_DIE))
                engine.removeEntity(monsterEntity)
                world.destroyBody(monsterPhysics.body)
            } else if (monster.currentTime > monster.fireRate) {
                monster.currentTime = 0f
                val playerEntity = engine.getEntitiesFor(allOf(Player::class, Physical::class).get()).firstOrNull()
                if (playerEntity != null) {
                    val playerPhysical = physicMapper[playerEntity]
                    val monsterLocation = monsterPhysics.body.position
                    createRotatingFireBall(engine, world,
                            playerPhysical.body.position.cpy() - monsterLocation,
                            monsterLocation,
                            monsterEntity)
                }
            }
        }
    }
}

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
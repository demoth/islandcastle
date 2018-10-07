package org.demoth.ktxtest

import com.badlogic.ashley.core.EntitySystem
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.utils.viewport.Viewport
import ktx.ashley.allOf
import ktx.ashley.entity
import ktx.ashley.mapperFor
import ktx.ashley.oneOf
import ktx.math.minus

val physicMapper = mapperFor<Physical>()
val playerMapper = mapperFor<Player>()
val texMapper = mapperFor<Textured>()
val positionMapper = mapperFor<Positioned>()
val namedMapper = mapperFor<Named>()
val animatedMapper = mapperFor<Animated>()
val monsterMapper = mapperFor<MonsterStationaryRanged>()
val floatingUpLabelMapper = mapperFor<FloatingUpLabel>()

/**
 * Moves player in the physical world
 */
class PlayerControlSystem(private val world: World) : EntitySystem() {
    /**
     * location relative to player (center)
     */
    var actionLocation: Vector2? = null

    override fun update(deltaTime: Float) {
        engine.getEntitiesFor(allOf(Physical::class, Player::class).get()).firstOrNull()?.let { it ->
            // this may be used later to affect how controls are used
            val player = playerMapper[it]
            val playerPhysics = physicMapper[it]

            if (player.health < 0) {
                engine.entity().apply {
                    add(FloatingUpLabel(10f))
                    add(Named("You have died! Press F5 to restart. Score: ${player.score}"))
                    add(Positioned(playerPhysics.body.position.cpy()))
                }
                engine.removeEntity(it)
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
                    createFireBall(engine, world, actionLocation!!, body.position, playerPhysics.owner)
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
) : EntitySystem() {
    private val font = BitmapFont()
    var time = 0f

    override fun update(deltaTime: Float) {
        time += deltaTime

        if (drawSprites) {
            engine.getEntitiesFor(oneOf(Textured::class, Animated::class).oneOf(Physical::class, Positioned::class).get()).forEach { e ->
                val texture = texMapper[e]?.texture
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
            engine.getEntitiesFor(allOf(Named::class).oneOf(Physical::class, Positioned::class).get()).forEach { e ->
                val name = namedMapper[e].name
                val position = physicMapper[e]?.body?.position ?: positionMapper[e].position
                if (name.isNotBlank()) {
                    // TODO check if object is visible from current viewport
                    val g = GlyphLayout(font, name)
                    font.draw(batch, g, position.x * PPM - g.width / 2, position.y * PPM)
                }
            }

        }
        engine.getEntitiesFor(allOf(FloatingUpLabel::class, Positioned::class, Named::class).get()).forEach {
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

        engine.getEntitiesFor(oneOf(Player::class).get()).firstOrNull()?.let {
            val player = playerMapper[it]
            val g = GlyphLayout(font, "Score: ${player.score}")
            val h = GlyphLayout(font, "Health: ${player.health}")

            font.draw(batch, g, viewport.camera.position.x - g.width / 2, viewport.camera.position.y - viewport.screenHeight * 0.2f)
            font.draw(batch, h, viewport.camera.position.x - h.width / 2, viewport.camera.position.y - viewport.screenHeight * 0.23f)
            player.score--
        }
    }
}

/**
 * Moves camera view to center on player
 */
class CameraSystem(private val camera: Camera) : EntitySystem() {
    override fun update(deltaTime: Float) {
        engine.getEntitiesFor(allOf(Physical::class, Player::class).get()).forEach { e ->
            physicMapper[e]?.body?.let { body ->
                camera.position.set(body.position.x * PPM, body.position.y * PPM, 0f)
            }
        }
    }
}

class PhysicalSystem(private val world: World) : EntitySystem() {
    override fun update(deltaTime: Float) {
        engine.getEntitiesFor(allOf(Physical::class).get()).forEach { e ->
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
        engine.getEntitiesFor(allOf(MonsterStationaryRanged::class, Physical::class).get()).forEach {
            val monster = monsterMapper[it]
            val monsterPhysics = physicMapper[it]
            monster.currentTime += deltaTime
            if (monster.health < 0) {
                engine.removeEntity(it)
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
                            monsterPhysics.owner)
                }
            }
        }
    }
}
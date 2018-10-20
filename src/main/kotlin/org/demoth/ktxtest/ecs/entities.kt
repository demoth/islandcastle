package org.demoth.ktxtest.ecs

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.maps.objects.RectangleMapObject
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.World
import ktx.ashley.entity
import ktx.ashley.get
import ktx.box2d.body
import org.demoth.ktxtest.*
import java.util.*

class EntityFactory(private val engine: Engine, private val world: World) {

    fun createPlayerEntity(location: Vector2) {
        engine.entity().apply {
            add(Textured(Sprites.KNIGHT))
            add(Player())
            add(Named("player"))
            add(HasHealth(9000))
            add(Physical(
                    body = world.body {
                        userData = this@apply
                        position.x = location.x
                        position.y = location.y
                        type = BodyDef.BodyType.DynamicBody
                        linearDamping = SPEED_DECEL
                        fixedRotation = true
                        circle(0.5f)
                    },
                    collide = { self, other -> damageHealth(self, other) },
                    collisionClass = RECEIVE_DAMAGE,
                    collidesWith = DEAL_DAMAGE or TRIGGER))
        }
        println("spawned player at (${location.x}, ${location.y})")

    }

    fun createEyeMonster(x: Float, y: Float) {
        debug("Spawning eye monster at: ($x, $y)")
        engine.entity().apply {
            add(Named("eyelander"))
            add(MonsterFiring())
            add(MonsterWalking())
            add(HasHealth(1000))
            add(Textured(Sprites.EYE_BOT))
            add(Physical(
                    body = world.body {
                        userData = this@apply
                        position.x = x
                        position.y = y
                        type = BodyDef.BodyType.DynamicBody
                        linearDamping = SPEED_DECEL
                        fixedRotation = true
                        circle(0.5f)
                    },
                    collide = { self, other -> damageHealth(self, other) },
                    collisionClass = RECEIVE_DAMAGE,
                    collidesWith = DEAL_DAMAGE))
        }
    }

    fun createDummyMonster(x: Float, y: Float) {
        debug("Spawning dummy at: ($x, $y)")
        engine.entity().apply {
            add(Named("dummy"))
            add(HasHealth(1000))
            add(Textured(Sprites.SKELETON))
            add(Physical(
                    body = world.body {
                        userData = this@apply
                        position.x = x
                        position.y = y
                        type = BodyDef.BodyType.DynamicBody
                        linearDamping = SPEED_DECEL
                        fixedRotation = true
                        circle(0.7f)
                    },
                    collide = { self, other -> damageHealth(self, other) },
                    collisionClass = RECEIVE_DAMAGE,
                    collidesWith = DEAL_DAMAGE))
        }
    }

    fun loadMap(map: TiledMap, previousMapName: String?, transition: (String) -> Unit) {
        debug("Creating entities:")
        val startPosition =
                if (previousMapName == null) {
                    debug("No previous map, looking for 'start' object...")
                    map.layers["entities"].objects["start"] as RectangleMapObject
                } else {
                    debug("Changing map from $previousMapName, looking for corresponding 'entrance_from'")
                    map.layers["entities"].objects
                            .filter { it.name == "entrance_from" }
                            .map { it as RectangleMapObject }
                            .find { it.properties["from"] == previousMapName }
                }
        debug("Player start position is ${startPosition?.rectangle?.getCentralPoint()}")
        if (startPosition == null)
            throw IllegalStateException("Could not find entrance from $previousMapName!dd")
        map.layers.forEach { layer ->
            layer.objects.getByType<RectangleMapObject>(RectangleMapObject::class.java).forEach { obj ->
                if (obj.name == "spawn_eyelander") {
                    createEyeMonster(obj.rectangle.x / PPM, obj.rectangle.y / PPM)
                } else if (obj.name == "dummy") {
                    createDummyMonster(obj.rectangle.x / PPM, obj.rectangle.y / PPM)
                } else if (obj.name == "exit_to") {
                    val nextMap = obj.properties["to"] as String
                    createTransition("exit to $nextMap", nextMap, obj.rectangle, transition)
                } else if (layer.name.startsWith("solid_"))
                    createWall(world, obj.rectangle, obj.name)
            }

        }
        // add player
        createPlayerEntity(startPosition.rectangle.getCentralPoint())
    }

    private fun createWall(world: World, rect: Rectangle, name: String?) {
        engine.entity().apply {
            add(Physical(
                    body = world.body {
                        userData = this@apply
                        type = BodyDef.BodyType.StaticBody
                        position.set(rect.getCentralPoint())
                        box(width = rect.width / PPM, height = rect.height / PPM)
                    }, collisionClass = if (name.isNullOrBlank()) SOLID_INVISIBLE else SOLID))

            if (!name.isNullOrBlank()) {
                add(Named(name!!))
            }
        }
    }

    fun createFireBall(velocity: Vector2, origin: Vector2, owner: Entity) {
        engine.entity().apply {
            add(Named("fireball"))
            add(Textured(Sprites.FIREBALL))
            add(Physical(
                    body = world.body {
                        userData = this@apply
                        type = BodyDef.BodyType.DynamicBody
                        this.linearVelocity.set(velocity)
                        this.position.set(origin)
                        circle(0.5f) {
                            isSensor = true
                        }
                    },
                    collide = ::destroyFireballWithExplosion,
                    collisionClass = DEAL_DAMAGE,
                    collidesWith = RECEIVE_DAMAGE or SOLID))
            add(HasDamage(3070, owner))
            add(HasSound(Sounds.FIREBALL))
            add(TTL(2f))
        }
    }

    fun createRotatingFireBall(velocity: Vector2, origin: Vector2, owner: Entity) {
        engine.entity().apply {
            add(Named("fire-sprirals"))
            add(Animated(SpriteSheets.FIRE_SPIRALS, 0.1f, Animation.PlayMode.LOOP))
            add(Physical(
                    body = world.body {
                        userData = this@apply
                        type = BodyDef.BodyType.DynamicBody
                        linearVelocity.set(velocity)
                        position.set(origin)
                        circle(0.5f) {
                            isSensor = true
                        }
                    },
                    collide = ::destroyFireball,
                    collisionClass = DEAL_DAMAGE,
                    collidesWith = RECEIVE_DAMAGE or SOLID))
            add(HasDamage(1000, owner))
            add(TTL(2f))
        }
    }

    fun createFloatingLabel(text: String, location: Vector2, ttl: Float) {
        engine.entity().apply {
            add(Named(text))
            add(FloatingUpLabel(Vector2(0f, 0.01f)))
            add(TTL(ttl))
            add(Positioned(location))
        }
    }

    fun createTransition(name: String, nextMap: String, rect: Rectangle, action: (String) -> Unit) {
        debug("Creating transition to $nextMap")
        engine.entity().apply {
            add(Named(name))
            add(Physical(
                    body = world.body {
                        userData = this@apply
                        type = BodyDef.BodyType.StaticBody
                        position.set(rect.getCentralPoint())
                        box(width = rect.width / PPM, height = rect.height / PPM) {
                            isSensor = true
                        }
                    },
                    collide = { _, other ->
                        val player = other.get<Player>()
                        if (player != null) {
                            action.invoke(nextMap)
                        }
                    },
                    collisionClass = TRIGGER,
                    collidesWith = RECEIVE_DAMAGE))
        }
    }

    private fun damageHealth(self: Entity, other: Entity) {
        val health = self.get<HasHealth>()
        val damage = other.get<HasDamage>()
        val position = self.get<Physical>()?.body?.position
        if (damage != null && damage.owner !== self && health != null) {
            health.value -= damage.value
            if (position != null) {
                createFloatingLabel(damage.value.toString(), position.cpy(), 2f)
            }
            engine.entity().add(HasSound(HURT[Random().nextInt(HURT.size)]))
        }
    }

    private fun destroyFireball(self: Entity, other: Entity) {
        val damage = self.get<HasDamage>()
        val physical = self.get<Physical>()
        if (physical != null && damage != null && other !== damage.owner) {
            physical.toBeRemoved = true
        }
    }

    private fun destroyFireballWithExplosion(self: Entity, other: Entity) {
        val damage = self.get<HasDamage>()
        val physical = self.get<Physical>()
        if (physical != null && damage != null && other !== damage.owner) {
            physical.toBeRemoved = true

            createExplosion(physical.body.position.cpy())
        }
    }

    private fun createExplosion(position: Vector2) {
        engine.entity().apply {
            add(Positioned(position))
            add(Animated(SpriteSheets.FIRE_EXPLOSION, 0.1f, Animation.PlayMode.NORMAL))
            add(HasSound(Sounds.EXPLOSION))
        }
    }

}
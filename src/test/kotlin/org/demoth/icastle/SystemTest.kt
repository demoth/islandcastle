package org.demoth.icastle

import com.badlogic.ashley.core.PooledEngine
import com.badlogic.gdx.math.Vector2
import ktx.ashley.allOf
import ktx.ashley.entity
import ktx.box2d.body
import ktx.box2d.createWorld
import org.demoth.icastle.ecs.*
import org.junit.Assert
import org.junit.Test

val fireball = allOf(HasDamage::class, TTL::class).get()


class SystemTest {
    @Test
    fun `death system removes dead monster`() {
        val engine = PooledEngine()
        val world = createWorld()
        engine.addSystem(DeathSystem(world, EntityFactory(engine, world)))

        engine.entity().apply {
            add(Named("eyelander"))
            add(MonsterFiring())
            add(MonsterWalking())
            add(HasHealth(-1))
            add(Physical(
                    body = world.body { },
                    collide = { _, _ -> run {} },
                    collisionClass = RECEIVE_DAMAGE,
                    collidesWith = DEAL_DAMAGE or TRIGGER))
        }
        Assert.assertTrue("Monster is not found in the engine", engine.getEntitiesFor(monstersMortal).size() != 0)
        engine.update(1f)
        Assert.assertTrue("Monster has not died", engine.getEntitiesFor(monstersMortal).size() == 0)
    }

    @Test
    fun `entity factory creates dead monsters that will be removed by system`() {
        val engine = PooledEngine()
        val world = createWorld()
        val entityFactory = EntityFactory(engine, world)
        engine.addSystem(DeathSystem(world, entityFactory))

        entityFactory.createEyeMonster(0f, 0f, -1)
        Assert.assertTrue("Monster is not found in the engine", engine.getEntitiesFor(monstersMortal).size() != 0)

        engine.update(1f)
        Assert.assertTrue("Dead monster has not been removed", engine.getEntitiesFor(monstersMortal).size() == 0)
    }

    @Test
    fun `monsters do not strike ball when nobody is nearby`() {
        val engine = PooledEngine()
        val world = createWorld()
        val entityFactory = EntityFactory(engine, world)
        engine.addSystem(MonsterFiringSystem(entityFactory))

        entityFactory.createEyeMonster(0f, 0f, 1000)
        engine.update(1f)
        Assert.assertTrue("Fireball is fired without player", engine.getEntitiesFor(fireball).size() == 0)

        entityFactory.createPlayerEntity(Vector2(20f, 0f))
        engine.update(1f)
        Assert.assertTrue("Fireball is fired but player is too far", engine.getEntitiesFor(fireball).size() == 0)

    }

    @Test
    fun `entity factory creates monsters that can strike a fireball`() {
        val engine = PooledEngine()
        val world = createWorld()
        val entityFactory = EntityFactory(engine, world)
        engine.addSystem(MonsterFiringSystem(entityFactory))
        entityFactory.createEyeMonster(0f, 0f, 1000)
        entityFactory.createPlayerEntity(Vector2(9f, 0f))
        engine.update(1f)

        Assert.assertTrue("Fireball is not fired", engine.getEntitiesFor(fireball).size() != 0)
    }

    @Test
    fun `monster walks towards the player`() {
        val engine = PooledEngine()
        val world = createWorld()
        val entityFactory = EntityFactory(engine, world)
        engine.addSystem(MonsterWalkSystem())
        entityFactory.createEyeMonster(0f, 0f, 1000)
        entityFactory.createPlayerEntity(Vector2(10f, 0f))

        val monsterEntity = engine.getEntitiesFor(monstersWalking).firstOrNull()
        val monsterPhysics = physicMapper[monsterEntity]
        val monsterLocation = monsterPhysics.body.position

        engine.update(1f)
        world.step(1f, 6, 2)
        engine.update(1f) // TODO cannot be understood why it is needed again

        Assert.assertTrue("Monster should walk towards the player",
                monsterLocation.x > 0f && monsterLocation.y == 0f)
    }

    @Test
    fun `monster stays still when nobody in the map`() {
        val engine = PooledEngine()
        val world = createWorld()
        val entityFactory = EntityFactory(engine, world)
        engine.addSystem(MonsterWalkSystem())
        entityFactory.createEyeMonster(0f, 0f, 1000)
        engine.update(1f)
        val monsterEntity = engine.getEntitiesFor(monstersWalking).firstOrNull()
        val monsterPhysics = physicMapper[monsterEntity]
        val monsterLocation = monsterPhysics.body.position
        Assert.assertTrue("Monster should not walk", monsterLocation == Vector2(0f, 0f))
    }
}
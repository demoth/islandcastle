package org.demoth.ktxtest

import com.badlogic.ashley.core.PooledEngine
import ktx.ashley.entity
import ktx.box2d.body
import ktx.box2d.createWorld
import org.demoth.ktxtest.ecs.EntityFactory
import org.demoth.ktxtest.ecs.HasHealth
import org.demoth.ktxtest.ecs.MonsterDeathSystem
import org.demoth.ktxtest.ecs.MonsterFiring
import org.demoth.ktxtest.ecs.MonsterWalking
import org.demoth.ktxtest.ecs.Named
import org.demoth.ktxtest.ecs.Physical
import org.demoth.ktxtest.ecs.monstersMortal
import org.junit.Assert
import org.junit.Test


class SystemTest {
    @Test
    fun `death system removes dead monster`() {
        val engine = PooledEngine()
        val world = createWorld()
        engine.addSystem(MonsterDeathSystem(world))

        engine.entity().apply {
            add(Named("eyelander"))
            add(MonsterFiring())
            add(MonsterWalking())
            add(HasHealth(-1))
            add(Physical(
                    body = world.body { },
                    collide = { self, other -> {} },
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
        engine.addSystem(MonsterDeathSystem(world))

        entityFactory.createEyeMonster(0f, 0f, -1)
        Assert.assertTrue("Monster is not found in the engine", engine.getEntitiesFor(monstersMortal).size() != 0)

        engine.update(1f)
        Assert.assertTrue("Dead monster has not been removed", engine.getEntitiesFor(monstersMortal).size() == 0)
    }
}
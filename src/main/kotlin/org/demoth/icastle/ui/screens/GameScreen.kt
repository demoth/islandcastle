package org.demoth.icastle.ui.screens

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.PooledEngine
import com.badlogic.gdx.*
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.tiled.TmxMapLoader
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.Box2D
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.utils.viewport.FitViewport
import com.badlogic.gdx.utils.viewport.Viewport
import ktx.ashley.entity
import ktx.ashley.get
import ktx.box2d.body
import ktx.box2d.createWorld
import ktx.graphics.use
import org.demoth.icastle.*
import org.demoth.icastle.ecs.*
import org.demoth.icastle.ecs.systems.*


const val MAX_SPEED = 10f
const val SPEED_DECEL = 25f

// how many tiles we see around
const val SIGHT_RADIUS = 16

// size of tiles in pixels
const val TILE_SIZE = 32f
const val WALK_FORCE = 100f
const val PPM = 32f // 1 meter - 32 pixels

/**
 * Screen with the gameplay - player controls, monsters etc
 */
class GameScreen : ScreenAdapter() {

    // fixme lateinit
    lateinit var goToMainMenu: () -> Unit

    private lateinit var world: World
    private lateinit var batch: SpriteBatch
    private lateinit var viewport: Viewport
    private lateinit var camera: OrthographicCamera
    private lateinit var box2dRenderer: Box2DDebugRenderer
    private var map: TiledMap? = null
    private var tileRenderer: OrthogonalTiledMapRenderer? = null
    private lateinit var batchDrawSystem: BatchDrawSystem
    private lateinit var playerControlSystem: PlayerControlSystem
    private lateinit var engine: PooledEngine
    private lateinit var soundSystem: SoundSystem
    private lateinit var entityFactory: EntityFactory

    private lateinit var ingameHud: IngameHud

    var drawDebug = false
    var drawTiles = true
    var time = 0f
    var nextLevel: String? = null
    var previousLevel: String? = null

    fun initialize() {
        debug("Initializing box2d")
        Box2D.init()

        debug("Initializing box2d renderer")
        box2dRenderer = Box2DDebugRenderer()

        debug("Creating world")
        world = createWorld().apply {
            setContactListener(CollisionProcessor())
        }

        debug("Creating sprite batch")
        batch = SpriteBatch()

        debug("Creating camera")
        camera = OrthographicCamera(TILE_SIZE, TILE_SIZE)

        debug("Creating viewport")
        viewport = FitViewport(TILE_SIZE * SIGHT_RADIUS, TILE_SIZE * SIGHT_RADIUS, camera)

        debug("Creating ECS engine")
        engine = PooledEngine()
        entityFactory = EntityFactory(engine, world)
        playerControlSystem = PlayerControlSystem(entityFactory)


        batchDrawSystem = BatchDrawSystem(batch)
        soundSystem = SoundSystem()

        debug("Adding playerControlSystem")
        engine.addSystem(playerControlSystem)
        debug("Adding batchDrawSystem")
        engine.addSystem(batchDrawSystem)
        debug("Adding CameraSystem")
        engine.addSystem(CameraSystem(camera))
        debug("Adding EntitiesCleanupSystem")
        engine.addSystem(EntitiesCleanupSystem(world))
        debug("Adding MonsterWalkSystem")
        engine.addSystem(MonsterWalkSystem())
        debug("Adding MonsterFiringSystem")
        engine.addSystem(MonsterFiringSystem(entityFactory))
        debug("Adding DeathSystem")
        engine.addSystem(DeathSystem(world, entityFactory))
        debug("Adding soundSystem")
        engine.addSystem(soundSystem)
        debug("Adding MovementSystem")
        engine.addSystem(MovementSystem())

        debug("Creating IngameHud")
        ingameHud = IngameHud()

        debug("Adding PlayerHudUpdateSystem")
        engine.addSystem(PlayerHudUpdateSystem(ingameHud))
    }

    fun changeLevel(currentMap: String, previousMapName: String?) {

        previousLevel = currentMap

        map = TmxMapLoader().load("maps/$currentMap")
        debug("TmxMapLoader: loaded tiled map: maps/$currentMap")
        tileRenderer = OrthogonalTiledMapRenderer(map, 1f)

        entityFactory.loadMap(map!!, previousMapName) { nextMap ->
            previousLevel = currentMap
            nextLevel = nextMap
        }

        // todo multiplex with ui
        val gameMouseListener = object : InputAdapter() {
            override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
                if (button == Input.Buttons.LEFT) {
                    println("clicked! screen coords:  $screenX, $screenY")
                    playerControlSystem.leftClick = screenToWorld(screenX, screenY)
                }
                return true
            }
        }

        // hijack inputprocessor
        Gdx.input.inputProcessor = InputMultiplexer(ingameHud.getInputProcessor(), gameMouseListener)

        // fixme why it is not called automatically
        resize(Gdx.graphics.width, Gdx.graphics.height)

        engine.entity().apply {
            add(Named("hp +25"))
            add(Textured(Sprites.FIREBALL))
            add(HealthOrb(25))
            add(
                Physical(
                    body = world.body {
                        userData = this@apply
                        type = BodyDef.BodyType.DynamicBody
                        this.position.set(Vector2(25f,25f))
                        circle(0.5f) {
                            isSensor = true
                        }
                    },
                    collide = ::markAsReachable,
                    uncollide = ::unmarkAsReachable,
                    collisionClass = DEAL_DAMAGE,
                    collidesWith = RECEIVE_DAMAGE or SOLID
                )
            )
        }


    }

    fun unmarkAsReachable(self: Entity, other: Entity) {
        for (e in listOf(self, other)) {
            if (e.get<HealthOrb>() != null) {
                println("Can no longer pick up")
                e.remove(ReachableByPlayer::class.java);
            }
        }
    }

    fun markAsReachable(self: Entity, other: Entity) {
        for (e in listOf(self, other)) {
            if (e.get<HealthOrb>() != null) {
                println("Can now pick up")
                e.add(ReachableByPlayer());
            }
        }
    }

    private fun screenToWorld(screenX: Int, screenY: Int): Vector2 {
        return Vector2(
            1f * screenX - viewport.screenWidth / 2f,
            -1f * screenY + viewport.screenHeight / 2f
        ).scl(0.5f / PPM)
    }

    override fun dispose() {
        //fixme: init is called manually but dispose is called by the framework!

        debug("Removing all entities")
        engine.removeAllEntities()
        debug("Removing box2dRenderer")
        box2dRenderer.dispose()
        debug("Removing box2d world")
        world.dispose()
        debug("Removing tileRenderer")
        tileRenderer?.dispose()
        debug("Removing map")
        map?.dispose()
        debug("Removing sprite batch")
        batch.dispose()
        debug("Removing soundSystem")
        soundSystem.dispose()
        debug("Removing batchDrawSystem")
        batchDrawSystem.dispose()
    }

    override fun render(delta: Float) {
        if (nextLevel != null) {
            changeLevel(nextLevel!!, previousLevel)
            // todo remove hardcoded size
            viewport.update(1000, 1000)
            nextLevel = null
            previousLevel = null
        }
        time += delta
        handleGlobalInput()
        // update physical world
        world.step(1 / 60f, 6, 2)

        clearScreen()
        camera.update()

        if (drawTiles) {
            tileRenderer!!.setView(camera)
            tileRenderer!!.render()
        }

        batch.projectionMatrix = camera.combined
        batch.use {
            engine.update(Gdx.graphics.deltaTime)
        }
        if (drawDebug) {
            // scale with PPM
            box2dRenderer.render(world, camera.combined.scl(PPM))
        }
        ingameHud.render(time)
    }

    private fun handleGlobalInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.F1))
            drawDebug = !drawDebug
        if (Gdx.input.isKeyJustPressed(Input.Keys.F2))
            drawTiles = !drawTiles
        if (Gdx.input.isKeyJustPressed(Input.Keys.F3))
            batchDrawSystem.drawSprites = !batchDrawSystem.drawSprites
        if (Gdx.input.isKeyJustPressed(Input.Keys.F4))
            batchDrawSystem.drawNames = !batchDrawSystem.drawNames

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            goToMainMenu()
        }
    }

    private fun clearScreen() {
        Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT or GL20.GL_DEPTH_BUFFER_BIT)
    }

    override fun resize(width: Int, height: Int) {
        viewport.update(width, height)
    }

}


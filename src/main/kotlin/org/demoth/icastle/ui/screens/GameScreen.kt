package org.demoth.icastle.ui.screens

import com.badlogic.ashley.core.PooledEngine
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.tiled.TmxMapLoader
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Box2D
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer
import com.badlogic.gdx.physics.box2d.ContactListener
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.utils.viewport.FitViewport
import com.badlogic.gdx.utils.viewport.Viewport
import ktx.box2d.createWorld
import ktx.graphics.use
import org.demoth.icastle.CollisionProcessor
import org.demoth.icastle.debug
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
class GameScreen(startMap: String?) : ScreenAdapter() {
    private val startMapName = startMap ?: "grassmap.tmx"
    private lateinit var world: World
    private lateinit var batch: SpriteBatch
    private lateinit var viewport: Viewport
    private lateinit var camera: OrthographicCamera
    private lateinit var map: TiledMap
    private lateinit var box2dRenderer: Box2DDebugRenderer
    private lateinit var tileRenderer: OrthogonalTiledMapRenderer
    private lateinit var batchDrawSystem: BatchDrawSystem
    private lateinit var playerControlSystem: PlayerControlSystem
    private lateinit var engine: PooledEngine
    private lateinit var collisionListener: ContactListener
    private lateinit var soundSystem: SoundSystem
    private lateinit var entityFactory: EntityFactory

    private lateinit var ingameHud: IngameHud

    var drawDebug = false
    var drawTiles = true
    var time = 0f
    var nextLevel: String? = null
    var previousLevel: String? = null

    fun initialize() {
        Box2D.init()
        debug("Box2D initialized")
        debug("Starting game in $startMapName")
        changeLevel(startMapName, null)
        // fixme why it is not called automatically
        resize(Gdx.graphics.width, Gdx.graphics.height)
    }

    fun changeLevel(currentMap: String, previousMapName: String?) {
        previousLevel = currentMap
        debug("Changing level to $currentMap...")
        world = createWorld()
        debug("Box2 world created")
        box2dRenderer = Box2DDebugRenderer()

        batch = SpriteBatch()

        map = TmxMapLoader().load("maps/$currentMap")
        debug("TmxMapLoader: loaded tiled map: maps/$currentMap")
        tileRenderer = OrthogonalTiledMapRenderer(map, 1f)

        camera = OrthographicCamera(TILE_SIZE, TILE_SIZE)
        viewport = FitViewport(TILE_SIZE * SIGHT_RADIUS, TILE_SIZE * SIGHT_RADIUS, camera)

        engine = PooledEngine()
        entityFactory = EntityFactory(engine, world)
        collisionListener = CollisionProcessor()
        world.setContactListener(collisionListener)

        playerControlSystem = PlayerControlSystem(entityFactory)
        batchDrawSystem = BatchDrawSystem(batch)
        soundSystem = SoundSystem()

        engine.addSystem(playerControlSystem)
        engine.addSystem(batchDrawSystem)
        engine.addSystem(CameraSystem(camera))
        engine.addSystem(EntitiesCleanupSystem(world))
        engine.addSystem(MonsterWalkSystem())
        engine.addSystem(MonsterFiringSystem(entityFactory))
        engine.addSystem(DeathSystem(world, entityFactory))
        engine.addSystem(soundSystem)
        engine.addSystem(MovementSystem())

        ingameHud = IngameHud()

        engine.addSystem(PlayerHudUpdateSystem(ingameHud))

        entityFactory.loadMap(map, previousMapName) { nextMap ->
            previousLevel = currentMap
            nextLevel = nextMap
        }

        // todo multiplex with ui
        Gdx.input.inputProcessor = object : InputAdapter() {
            override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
                if (button == Input.Buttons.LEFT) {
                    println("clicked! screen coords:  $screenX, $screenY")
                    playerControlSystem.leftClick = screenToWorld(screenX, screenY)
                }
                return true
            }
        }
    }

    private fun screenToWorld(screenX: Int, screenY: Int): Vector2 {
        return Vector2(
                1f * screenX - viewport.screenWidth / 2f,
                -1f * screenY + viewport.screenHeight / 2f).scl(0.5f / PPM)
    }

    override fun dispose() {
        //fixme: init is called manually but dispose is called by the framework!
        world.dispose()
        box2dRenderer.dispose()
        tileRenderer.dispose()
        map.dispose()
        batch.dispose()
        engine.removeAllEntities()
        soundSystem.dispose()
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
            tileRenderer.setView(camera)
            tileRenderer.render()
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
        if (Gdx.input.isKeyJustPressed(Input.Keys.F5)) {
            dispose()
            changeLevel(startMapName, null)
            // todo remove hardcoded size
            viewport.update(1000, 1000)
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE))
            Gdx.app.exit()
    }

    private fun clearScreen() {
        Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT or GL20.GL_DEPTH_BUFFER_BIT)
    }

    override fun resize(width: Int, height: Int) {
        viewport.update(width, height)
    }

}


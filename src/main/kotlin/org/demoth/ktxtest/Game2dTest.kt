package org.demoth.ktxtest

import com.badlogic.ashley.core.PooledEngine
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.maps.objects.RectangleMapObject
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.tiled.TmxMapLoader
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Box2D
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.utils.viewport.FitViewport
import com.badlogic.gdx.utils.viewport.Viewport
import ktx.app.KtxApplicationAdapter
import ktx.ashley.entity
import ktx.box2d.createWorld
import ktx.graphics.use


const val MAX_SPEED = 10f
const val SPEED_DECEL = 5f
// how many tiles we see around
const val SIGHT_RADIUS = 16
// size of tiles in pixels
const val TILE_SIZE = 32f
const val WALK_FORCE = 20f
const val PPM = 32f // 1 meter - 32 pixels

class Game2dTest : KtxApplicationAdapter {
    lateinit var world: World
    lateinit var batch: SpriteBatch
    lateinit var viewport: Viewport
    lateinit var camera: OrthographicCamera
    lateinit var map: TiledMap
    lateinit var box2dRenderer: Box2DDebugRenderer
    lateinit var tileRenderer: OrthogonalTiledMapRenderer
    lateinit var batchDrawSystem: BatchDrawSystem
    lateinit var playerControlSystem: PlayerControlSystem
    lateinit var engine: PooledEngine
    var drawDebug = false
    var drawTiles = true

    override fun create() {
        super.create()
        Box2D.init()
        world = createWorld()

        box2dRenderer = Box2DDebugRenderer()

        batch = SpriteBatch()

        map = TmxMapLoader().load("grassmap.tmx")
        tileRenderer = OrthogonalTiledMapRenderer(map, 1f)

        camera = OrthographicCamera(TILE_SIZE, TILE_SIZE)
        viewport = FitViewport(TILE_SIZE * SIGHT_RADIUS, TILE_SIZE * SIGHT_RADIUS, camera)
        val startPosition = map.layers["entities"].objects["start"] as RectangleMapObject

        engine = PooledEngine()
        playerControlSystem = PlayerControlSystem(world)
        batchDrawSystem = BatchDrawSystem(batch)

        engine.addSystem(playerControlSystem)
        engine.addSystem(batchDrawSystem)
        engine.addSystem(CameraSystem(camera))

        // add player
        engine.entity {
            createPlayerEntity(engine, world, startPosition.rectangle.getCentralPoint())
        }


        map.layers.forEach { layer ->
            layer.objects.getByType<RectangleMapObject>(RectangleMapObject::class.java).forEach { obj ->
                createMapObject(engine, world, layer.name, obj.name, obj.rectangle)
            }

        }

        Gdx.input.inputProcessor = object : InputAdapter() {
            override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
                if (button == Input.Buttons.LEFT) {
                    println("touched! $screenX, $screenY")
                    playerControlSystem.actionLocation = Vector2(
                            1f * screenX - viewport.screenWidth / 2f,
                            -1f * screenY + viewport.screenHeight / 2f).scl(0.5f / PPM)
                }
                return true
            }
        }
    }

    override fun dispose() {
        // TODO dispose of used textures
        world.dispose()
        box2dRenderer.dispose()
        tileRenderer.dispose()
        map.dispose()
        batch.dispose()
    }

    override fun render() {

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
            engine.update(0f)
        }
        if (drawDebug) {
            // scale with PPM
            box2dRenderer.render(world, camera.combined.scl(PPM))
        }

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
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE))
            Gdx.app.exit()
    }

    private fun clearScreen() {
        Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT or GL20.GL_DEPTH_BUFFER_BIT)
    }

    override fun resize(width: Int, height: Int) {
        super.resize(width, height)
        viewport.update(width, height)
    }
}
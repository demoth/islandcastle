package org.demoth.ktxtest

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.tiled.TmxMapLoader
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer
import com.badlogic.gdx.physics.box2d.*
import com.badlogic.gdx.utils.viewport.FitViewport
import com.badlogic.gdx.utils.viewport.Viewport
import ktx.app.KtxApplicationAdapter
import ktx.box2d.body
import ktx.box2d.createWorld
import ktx.graphics.use


const val MIN_SPEED = 0.01f
const val MAX_SPEED = 10f
const val SPEED_DECEL = 2f
const val SIGHT_RADIUS = 16
const val TILE_SIZE = 32f
const val WALK_FORCE = 20f
const val PPM = 32f // 1 meter - 32 pixels

class Game2dTest : KtxApplicationAdapter {
    lateinit var world: World
    lateinit var batch: SpriteBatch
    lateinit var playerTex: Texture
    lateinit var viewport: Viewport
    lateinit var camera: OrthographicCamera
    lateinit var playerBody: Body
    lateinit var map: TiledMap
    lateinit var box2dRenderer: Box2DDebugRenderer
    lateinit var tileRenderer: OrthogonalTiledMapRenderer

    override fun create() {
        super.create()
        Box2D.init()
        world = createWorld()
        box2dRenderer = Box2DDebugRenderer()
        batch = SpriteBatch()
        playerTex = Texture(Gdx.files.internal("knight32.png"))
        map = TmxMapLoader().load("grassmap.tmx")
        tileRenderer = OrthogonalTiledMapRenderer(map, 1f)
        camera = OrthographicCamera(TILE_SIZE, TILE_SIZE)
        viewport = FitViewport(TILE_SIZE * SIGHT_RADIUS, TILE_SIZE * SIGHT_RADIUS, camera)

        playerBody = world.body {
            // todo use start position
            position.x = 16f * TILE_SIZE / PPM
            position.y = 16f * TILE_SIZE / PPM
            type = BodyDef.BodyType.DynamicBody
            linearDamping = 5f
            fixedRotation = true
            circle(0.5f)
        }
    }

    override fun dispose() {
        world.dispose()
        box2dRenderer.dispose()
        tileRenderer.dispose()
        map.dispose()
        batch.dispose()
        playerTex.dispose()
    }

    override fun render() {

        handleInput()
        world.step(1 / 60f, 6, 2)

        clearScreen()
        camera.position.set(playerBody.position.x * PPM, playerBody.position.y * PPM, 0f)
        camera.update()

        tileRenderer.setView(camera)
        tileRenderer.render()

        batch.projectionMatrix = camera.combined
        batch.use {
            it.draw(playerTex, playerBody.position.x * PPM - playerTex.width / 2f, playerBody.position.y * PPM - playerTex.height / 2)
        }
        box2dRenderer.render(world, camera.combined.scl(PPM))

    }

    private fun handleInput() {
        if (Gdx.input.isKeyPressed(Input.Keys.W) && playerBody.linearVelocity.y < MAX_SPEED) {
            playerBody.applyForceToCenter(0f, WALK_FORCE, true)
        }
        if (Gdx.input.isKeyPressed(Input.Keys.A) && playerBody.linearVelocity.x > -MAX_SPEED) {
            playerBody.applyForceToCenter(-WALK_FORCE, 0f, true)
        }
        if (Gdx.input.isKeyPressed(Input.Keys.S) && playerBody.linearVelocity.y > -MAX_SPEED) {
            playerBody.applyForceToCenter(0f, -WALK_FORCE, true)
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D) && playerBody.linearVelocity.x < MAX_SPEED) {
            playerBody.applyForceToCenter(WALK_FORCE, 0f, true)
        }

        if (Gdx.input.isKeyPressed(Input.Keys.SPACE))
            println("pos: ${playerBody.position}, speed: ${playerBody.linearVelocity}")
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
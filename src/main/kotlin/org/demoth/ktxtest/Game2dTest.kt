package org.demoth.ktxtest

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.physics.box2d.*
import ktx.app.KtxApplicationAdapter
import ktx.box2d.createWorld
import com.badlogic.gdx.utils.viewport.ScreenViewport
import com.badlogic.gdx.utils.viewport.Viewport
import ktx.box2d.body
import ktx.graphics.use


class Game2dTest : KtxApplicationAdapter {
    lateinit var world: World
    lateinit var debugRenderer: Box2DDebugRenderer
    lateinit var batch: SpriteBatch
    lateinit var playerTex: Texture
    lateinit var grassTex: Texture
    lateinit var viewport: Viewport
    lateinit var orthographicCamera: OrthographicCamera
    lateinit var playerBody: Body

    override fun create() {
        super.create()
        Box2D.init()
        world = createWorld()
        debugRenderer = Box2DDebugRenderer()
        batch = SpriteBatch()
        playerTex = Texture(Gdx.files.internal("knight2.png"))
        grassTex = Texture(Gdx.files.internal("grass1.png"))

        orthographicCamera = OrthographicCamera(32f, 24f)
        viewport = ScreenViewport(orthographicCamera)

        playerBody = world.body {
            type = BodyDef.BodyType.DynamicBody
            circle(64f) {
                density = 0.0001f
                restitution = 0.5f
                friction = 100f
            }
        }

    }

    override fun dispose() {
        world.dispose()
        debugRenderer.dispose()
        batch.dispose()
        playerTex.dispose()
    }

    override fun render() {

        handleInput()
        world.step(1 / 30f, 6, 2)

        clearScreen()
        batch.projectionMatrix = orthographicCamera.combined
        batch.use {
            val size = 4
            repeat(size) { y ->
                repeat(size) { x ->
                    it.draw(grassTex, (x - size / 2f) * grassTex.width, (y - size / 2f) * grassTex.height)
                }
            }
            it.draw(playerTex, playerBody.position.x - playerTex.width / 2f, playerBody.position.y - playerTex.height / 2)
        }
        debugRenderer.render(world, orthographicCamera.combined)
        orthographicCamera.update()

    }

    private fun handleInput() {
        val f = 1000f
        if (Gdx.input.isKeyPressed(Input.Keys.W)) {
            playerBody.applyForceToCenter(0f, f, true)
        }
        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            playerBody.applyForceToCenter(-f, 0f, true)
        }
        if (Gdx.input.isKeyPressed(Input.Keys.S)) {
            playerBody.applyForceToCenter(0f, -f, true)
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            playerBody.applyForceToCenter(f, 0f, true)
        }
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
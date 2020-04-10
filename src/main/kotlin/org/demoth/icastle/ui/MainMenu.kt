package org.demoth.icastle.ui

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.badlogic.gdx.backends.lwjgl.LwjglApplication
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup
import com.badlogic.gdx.scenes.scene2d.ui.Window
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import ktx.actors.onClick
import ktx.app.KtxGame
import org.demoth.icastle.createConfiguration

class MainMenu : KtxGame<Screen>() {
    private lateinit var stage: Stage

    override fun create() {

        val skin = getTestSkin()

        stage = Stage()
        Gdx.input.inputProcessor = stage

        stage.addActor(Window("IslandCastle", skin).apply {
            setFillParent(true)
            background = TextureRegionDrawable(
                    TextureRegion(Texture(Gdx.files.internal("sprites/background2.jpg"))))
            addActor(VerticalGroup().apply {
                debug = true
                setFillParent(true)
                addActor(TextButton("New game", skin).apply {
                    onClick { println("Started new game") }
                })
                addActor(TextButton("Options", skin).apply {
                    onClick { println("Clicked options menu") }
                })
                addActor(TextButton("Exit", skin).apply {
                    onClick { Gdx.app.exit() }
                })
            })
        })
    }

    override fun resize(width: Int, height: Int) {
        stage.viewport.update(width, height, true)
    }

    override fun render() {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        stage.act(Gdx.graphics.deltaTime)
        stage.draw()
    }

    override fun dispose() {
        stage.dispose()
    }
}

fun main() {
    LwjglApplication(MainMenu(), createConfiguration())
}
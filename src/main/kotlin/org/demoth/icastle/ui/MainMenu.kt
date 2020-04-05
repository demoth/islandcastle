package org.demoth.icastle.ui

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.badlogic.gdx.backends.lwjgl.LwjglApplication
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Table
import ktx.app.KtxGame
import org.demoth.icastle.createConfiguration

class MainMenu : KtxGame<Screen>() {
    private lateinit var stage: Stage
    private lateinit var table: Table

    override fun create() {
        stage = Stage()
        Gdx.input.inputProcessor = stage
        table = Table()
        table.setFillParent(true)
        stage.addActor(table)
        table.debug = true // This is optional, but enables debug lines for tables.
        val background = Image(Texture(Gdx.files.internal("sprites/background2.jpg")))
        table.addActor(background)
        // Add widgets to the table here.
    }

    override fun resize(width: Int, height: Int) {
        stage!!.viewport.update(width, height, true)
    }

    override fun render() {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        stage!!.act(Gdx.graphics.deltaTime)
        stage!!.draw()
    }

    override fun dispose() {
        stage!!.dispose()
    }
}

fun main() {
    LwjglApplication(MainMenu(), createConfiguration())
}
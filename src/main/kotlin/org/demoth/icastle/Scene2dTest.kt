package org.demoth.icastle

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.backends.lwjgl.LwjglApplication
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Label
import ktx.app.KtxGame
import ktx.freetype.loadFreeTypeFont
import ktx.freetype.registerFreeTypeFontLoaders

class Scene2dTest : KtxGame<Screen>() {

    lateinit var stage: Stage
    lateinit var assetManager: AssetManager

    private val font = "assets/fonts/CinzelDecorative-Regular.ttf"

    override fun create() {
        super.create()

        assetManager = AssetManager()

        assetManager.registerFreeTypeFontLoaders()
        assetManager.loadFreeTypeFont(font) {
            size = 28
        }
        assetManager.finishLoading()
        val labelFont = assetManager.get<BitmapFont>(font)

        // Label style
        val labelStyle = Label.LabelStyle(labelFont, Color.LIGHT_GRAY)


        // Default settings:
        stage = Stage()
        stage.root.addActor(Label("hello world", labelStyle))
        Gdx.input.inputProcessor = stage
    }

    override fun render() {
        super.render()
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(Gdx.graphics.deltaTime)
        stage.draw()
    }
}

fun main() {
    LwjglApplication(Scene2dTest(), createConfiguration())
}
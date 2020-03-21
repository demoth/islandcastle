package org.demoth.icastle

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Label
import ktx.freetype.loadFreeTypeFont
import ktx.freetype.registerFreeTypeFontLoaders

class IngameHud : ScreenAdapter() {
    private var stage: Stage
    private var assetManager: AssetManager = AssetManager()

    private val font = "fonts/CinzelDecorative-Regular.ttf"

    private var scoreLabel: Label
    private var healthLabel: Label

    init {

        assetManager.registerFreeTypeFontLoaders()
        assetManager.loadFreeTypeFont(font) {
            size = 28
        }
        assetManager.finishLoading()
        val labelFont = assetManager.get<BitmapFont>(font)

        // Label style
        val labelStyle = Label.LabelStyle(labelFont, Color.LIGHT_GRAY)
        scoreLabel = Label("Score: 0", labelStyle)
        scoreLabel.setPosition(Gdx.graphics.width / 2f, Gdx.graphics.height / 2f - Gdx.graphics.height / 4f)

        healthLabel = Label("Health: 0", labelStyle)
        healthLabel.setPosition(Gdx.graphics.width / 2f, Gdx.graphics.height / 2f - Gdx.graphics.height / 4f - 48)

        // Default settings:
        stage = Stage()

        stage.root.addActor(scoreLabel)
        stage.root.addActor(healthLabel)

    }

    fun setValues(health: Int, score: Int) {
        scoreLabel.setText("Score: $score")
        healthLabel.setText("Health: $health")
    }

    fun render() {
        stage.act(Gdx.graphics.deltaTime)
        stage.draw()
    }
}

package org.demoth.icastle.ui.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable
import org.demoth.icastle.ui.getTestSkin

/**
 * Shows stats for player, action bar and similar widgets
 */
class IngameHud : ScreenAdapter() {
    private var stage: Stage
//    private var assetManager: AssetManager = AssetManager()
//
//    private val font = "fonts/CinzelDecorative-Regular.ttf"
//
//    private var scoreLabel: Label
//    private var healthLabel: Label

    init {
        val skin = getTestSkin()
//        assetManager.registerFreeTypeFontLoaders()
//        assetManager.loadFreeTypeFont(font) {
//            size = 28
//        }
//        assetManager.finishLoading()
//        val labelFont = assetManager.get<BitmapFont>(font)
//
//        // Label style
//        val labelStyle = Label.LabelStyle(labelFont, Color.LIGHT_GRAY)
//        scoreLabel = Label("Score: 0", labelStyle)
//        scoreLabel.setPosition(Gdx.graphics.width / 2f, Gdx.graphics.height / 2f - Gdx.graphics.height / 4f)
//
//        healthLabel = Label("Health: 0", labelStyle)
//        healthLabel.setPosition(Gdx.graphics.width / 2f, Gdx.graphics.height / 2f - Gdx.graphics.height / 4f - 48)
//
//        // Default settings:
        stage = Stage()
//
//        stage.root.addActor(scoreLabel)
//        stage.root.addActor(healthLabel)

        stage.root.addActor(Table().apply {
            debug = true
            setFillParent(true)
            bottom()
            add(Table().apply {
                background = SpriteDrawable(Sprite(Texture(Gdx.files.internal("sprites/actions_bar.png"))))
                add(HorizontalGroup().apply {
                    addActor(TextButton("1", skin))
                    addActor(TextButton("2", skin))
                    addActor(TextButton("3", skin))
                    addActor(TextButton("4", skin))
                    addActor(TextButton("5", skin))
                    addActor(TextButton("6", skin))
                })
            }).fillX()
        })

    }

    fun setValues(health: Int, score: Int) {
//        scoreLabel.setText("Score: $score")
//        healthLabel.setText("Health: $health")
    }

    override fun render(delta: Float) {
        stage.act(Gdx.graphics.deltaTime)
        stage.draw()
    }
}

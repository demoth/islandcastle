package org.demoth.icastle.ui.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import ktx.actors.onClick
import org.demoth.icastle.*
import org.demoth.icastle.ui.getTestSkin

/**
 * Shows stats for player, action bar and similar widgets
 */
class IngameHud : ScreenAdapter() {
    private var stage: Stage
    private var assetManager: AssetManager = AssetManager()
//
//    private val font = "fonts/CinzelDecorative-Regular.ttf"
//
//    private var scoreLabel: Label
//    private var healthLabel: Label

    init {
        val skin = getTestSkin()
        assetManager.load(ACTION_BAR, Texture::class.java)
        assetManager.load(ACTION_ATTACK, Texture::class.java)
        assetManager.load(ACTION_BOW, Texture::class.java)
        assetManager.load(ACTION_FIREBALL, Texture::class.java)
        assetManager.load(ACTION_HEAL, Texture::class.java)

        assetManager.finishLoading()
        val actionBar = assetManager.get<Texture>(ACTION_BAR)
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
            //debug = true
            setFillParent(true)
            bottom()
            add(Table().apply {
                background = SpriteDrawable(Sprite(actionBar))
                add(HorizontalGroup().apply {
                    space(16f)
                    left()
                    addActor(ImageButton(TextureRegionDrawable(TextureRegion(assetManager.get<Texture>(ACTION_ATTACK)))).apply {
                        onClick {
                            debug("Attack selected")
                        }
                    })
                    addActor(ImageButton(TextureRegionDrawable(TextureRegion(assetManager.get<Texture>(ACTION_BOW)))).apply {
                        onClick {
                            debug("Bow selected")
                        }
                    })
                    addActor(ImageButton(TextureRegionDrawable(TextureRegion(assetManager.get<Texture>(ACTION_FIREBALL)))).apply {
                        onClick {
                            debug("Fireball selected")
                        }
                    })
                    addActor(ImageButton(TextureRegionDrawable(TextureRegion(assetManager.get<Texture>(ACTION_HEAL)))).apply {
                        onClick {
                            debug("Heal selected")
                        }
                    })
                })
            }).fillX()
        })

    }

    fun setValues(health: Int, score: Int) {
//        scoreLabel.setText("Score: $score")
//        healthLabel.setText("Health: $health")

        // todo: set available actions
    }

    override fun render(delta: Float) {
        stage.act(Gdx.graphics.deltaTime)
        stage.draw()
    }

    fun getInputProcessor(): InputProcessor {
        return stage
    }
}

package org.demoth.icastle.ui.screens

import com.badlogic.ashley.core.Entity
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
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import ktx.actors.onClick
import ktx.ashley.get
import org.demoth.icastle.*
import org.demoth.icastle.ecs.healthMapper
import org.demoth.icastle.ecs.playerMapper
import org.demoth.icastle.ui.getTestSkin

/**
 * Shows stats for player, action bar and similar widgets
 */
class IngameHud : ScreenAdapter() {
    private var stage: Stage
    private var assetManager: AssetManager = AssetManager()
    private lateinit var actionsBar: HorizontalGroup

    //
//    private val font = "fonts/CinzelDecorative-Regular.ttf"
//
//    private var scoreLabel: Label
    private var healthLabel: Label

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
                actionsBar = HorizontalGroup().apply { space(16f) }
                add(actionsBar)
            }).fillX()
        })

        healthLabel = Label("0", skin)
        healthLabel.setPosition(64f, 64f)

        stage.root.addActor(healthLabel)

    }

    fun setValues(playerEntity: Entity) {
//        scoreLabel.setText("Score: $score")
        val health = playerEntity[healthMapper] ?: return
        healthLabel.setText("${health.value}")

        // todo: set available actions
        val player = playerEntity[playerMapper] ?: return

        if (player.actionsChanged) {
            actionsBar.clear()
            player.actions.forEach {
                actionsBar.addActor(ImageButton(TextureRegionDrawable(TextureRegion(assetManager.get<Texture>(it.icon)))).apply {
                    onClick {
                        debug("Action selected: ${it.name}")
                        player.selectedAction = it
                    }
                })
            }
            player.actionsChanged = false
        }
    }

    override fun render(delta: Float) {
        stage.act(Gdx.graphics.deltaTime)
        stage.draw()
    }

    fun getInputProcessor(): InputProcessor {
        return stage
    }
}

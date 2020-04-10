package org.demoth.icastle.ui.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup
import com.badlogic.gdx.scenes.scene2d.ui.Window
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import ktx.actors.onClick
import org.demoth.icastle.debug
import org.demoth.icastle.ui.getTestSkin

class MainMenu(startNewGame: () -> Unit) : ScreenAdapter() {
    private lateinit var currentStage: Stage
    private val mainMenu: Stage
    private val optionsMenu: Stage

    init {

        val skin = getTestSkin()

        optionsMenu = Stage()

        mainMenu = Stage().apply {
            addActor(Window("IslandCastle", skin).apply {
                setFillParent(true)
                background = TextureRegionDrawable(
                        TextureRegion(Texture(Gdx.files.internal("sprites/background2.jpg"))))
                addActor(VerticalGroup().apply {
                    debug = true
                    setFillParent(true)
                    addActor(TextButton("New game", skin).apply {
                        onClick {
                            debug("Started new game")
                            startNewGame()
                        }
                    })
                    addActor(TextButton("Options", skin).apply {
                        onClick {
                            changeStage(optionsMenu)
                        }
                    })
                    addActor(TextButton("Exit", skin).apply {
                        onClick { Gdx.app.exit() }
                    })
                })
            })
        }

        optionsMenu.apply {
            addActor(Window("Options", skin).apply {
                setFillParent(true)
                background = TextureRegionDrawable(
                        TextureRegion(Texture(Gdx.files.internal("sprites/background2.jpg"))))
                addActor(VerticalGroup().apply {
                    setFillParent(true)
                    addActor(TextButton("Back", skin).apply {
                        onClick {
                            changeStage(mainMenu)
                        }
                    })
                })
            })
        }

        changeStage(mainMenu)
    }

    override fun resize(width: Int, height: Int) {
        currentStage.viewport.update(width, height, true)
    }

    override fun render(delta: Float) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        currentStage.act(Gdx.graphics.deltaTime)
        currentStage.draw()
    }

    override fun dispose() {
        currentStage.dispose()
    }

    fun changeStage(stage: Stage) {
        currentStage = stage
        Gdx.input.inputProcessor = stage
    }
}

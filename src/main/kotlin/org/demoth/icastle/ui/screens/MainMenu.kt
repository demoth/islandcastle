package org.demoth.icastle.ui.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import ktx.actors.onClick
import org.demoth.icastle.debug
import org.demoth.icastle.ui.getTestSkin

class MainMenu(startNewGame: (mapName: String) -> Unit) : ScreenAdapter() {
    private lateinit var currentStage: Stage
    private val mainMenu: Stage
    private val aboutMenu: Stage
    private val selectLevel: Stage

    init {

        val skin = getTestSkin()

        aboutMenu = Stage()

        selectLevel = Stage()

        mainMenu = Stage().apply {
            addActor(Window("IslandCastle", skin).apply {
                setFillParent(true)
                background = TextureRegionDrawable(
                        TextureRegion(Texture(Gdx.files.internal("sprites/background2.jpg"))))
                addActor(Table().apply {
                    setFillParent(true)
                    defaults().width(128f).height(48f).padBottom(16f).fill()
                    add(TextButton("New game", skin).apply {
                        onClick {
                            debug("Started new game")
                            startNewGame("grassmap.tmx")
                        }
                    })

                    row()
                    add(TextButton("About", skin).apply {
                        onClick {
                            changeStage(aboutMenu)
                        }
                    })

                    row()
                    add(TextButton("Select level", skin).apply {
                        onClick {
                            changeStage(selectLevel)
                        }
                    })

                    row()
                    add(TextButton("Exit", skin).apply {
                        onClick { Gdx.app.exit() }
                    })
                })
            })
        }

        aboutMenu.apply {
            addActor(Window("About", skin).apply {
                setFillParent(true)
                background = TextureRegionDrawable(
                        TextureRegion(Texture(Gdx.files.internal("sprites/background2.jpg"))))
                addActor(Table().apply {
                    setFillParent(true)
                    defaults().width(128f).height(48f).padBottom(16f).fill()
                    add(Label("About", skin))

                    row()
                    add(TextArea("Hello, This is the game yet not finished" +
                            "\nHope you enjoy this project" +
                            "\n\nhttps://demoth.itch.io/topory7/devlog" +
                            "\n\nDemoth 2020", skin).apply { isDisabled = true })
                            .height(256f)
                    row()
                    add(TextButton("Back", skin).apply {
                        onClick {
                            changeStage(mainMenu)
                        }
                    })
                })
            })
        }

        selectLevel.apply {
            addActor(Window("Select level", skin).apply {
                setFillParent(true)
                background = TextureRegionDrawable(
                        TextureRegion(Texture(Gdx.files.internal("sprites/background2.jpg"))))
                addActor(Table().apply {
                    setFillParent(true)
                    defaults().width(128f).height(48f).padBottom(16f).fill()
                    add(Label("Select Level", skin))

                    row()
                    Gdx.files.internal("maps").list().forEach { map ->
                        if (map.name().endsWith("tmx")) {
                            add(TextButton(map.name(), skin).apply {
                                onClick {
                                    startNewGame(map.name())
                                }
                            })
                            row()

                        }
                    }

                    add(TextButton("Back", skin).apply {
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

    fun enableInput() {
        Gdx.input.inputProcessor = currentStage
    }
}

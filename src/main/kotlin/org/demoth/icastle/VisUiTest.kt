package org.demoth.icastle

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.backends.lwjgl.LwjglApplication
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.viewport.ScreenViewport
import com.badlogic.gdx.utils.viewport.Viewport
import com.kotcrab.vis.ui.VisUI
import com.kotcrab.vis.ui.widget.VisWindow
import ktx.actors.onClick
import ktx.app.KtxApplicationAdapter
import ktx.freetype.loadFreeTypeFont
import ktx.freetype.registerFreeTypeFontLoaders
import ktx.vis.window

class GameUiTest : KtxApplicationAdapter {

    lateinit var stage: Stage
    lateinit var view: Viewport

    lateinit var loginWindow: VisWindow
    lateinit var chooseCharacterWindow: VisWindow
    lateinit var titleWindow: VisWindow

    lateinit var assetManager: AssetManager

    private val font = "assets/fonts/CinzelDecorative-Regular.ttf"

    override fun create() {
        VisUI.load(VisUI.SkinScale.X2)
        view = ScreenViewport()
        stage = Stage(view)
        assetManager = AssetManager()

        assetManager.registerFreeTypeFontLoaders()
        assetManager.loadFreeTypeFont(font)
        assetManager.finishLoading()
        assetManager.get<BitmapFont>(font)
        titleWindow = window("") {
            sizeBy(800f, 800f)
            centerWindow()
            center()
            label("Island Castle")
//            bottom().right()
//            label("Version 0.1.2")

        }
        chooseCharacterWindow = window("Choose your character") {
            sizeBy(800f, 500f)
            centerWindow()
            bottom().left()
            table { t ->
                t.padTop(16f)
                debug = true
                defaults().pad(16f)
                setFillParent(true)
                list<String> {
                    it.width(140f)
                    setItems("wichka", "templar", "molekula")
                }
                label("Character details") {
                    it.expand()
                }
                row()
                button {
                    label("Disconnect")
                    onClick {
                        stage.actors.removeValue(chooseCharacterWindow, true)
                        stage.addActor(loginWindow)
                    }
                }
                button {
                    label("Start game")
                }
            }
        }
        loginWindow = window("Login screen") {
            sizeBy(800f, 500f)
            centerWindow()
            bottom().left()
            table { _ ->
                debug = true
                defaults().pad(16f)
                setFillParent(true)

                label("Server url:") {
                    it.right()
                }
                val url = textField {
                    text = "localhost:8080/action.json"
                    it.width(400f)
                }
                row()

                label("Login:") {
                    it.right()
                }
                val login = textField {
                    text = "test"
                    it.fill()
                }
                row()

                label("Password:") {
                    it.right()
                }
                val password = textField {
                    isPasswordMode = true
                    text = "test"
                    it.fill()
                }
                row()

                button {
                    label("Exit")
                    onClick {
                        Gdx.app.exit()
                    }
                }
                button {
                    label("Connect")
                    onClick {
                        println("Connected!: ${url.text}, ${login.text}, ${password.text}")
                        stage.actors.removeValue(loginWindow, true)
                        stage.addActor(chooseCharacterWindow)
                    }
                }
            }
        }
        stage.addActor(titleWindow)
        Gdx.input.inputProcessor = stage

    }

    override fun resize(width: Int, height: Int) {
        view.update(width, height)
    }

    override fun render() {
        Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT or GL20.GL_DEPTH_BUFFER_BIT)
        stage.draw()
    }

    override fun dispose() {
        stage.dispose()
    }
}

fun main() {
    LwjglApplication(GameUiTest(), createConfiguration())
}


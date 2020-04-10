package org.demoth.icastle

import com.badlogic.gdx.Screen
import com.badlogic.gdx.backends.lwjgl.LwjglApplication
import ktx.app.KtxGame
import org.demoth.icastle.ui.screens.GameScreen
import org.demoth.icastle.ui.screens.MainMenu

class Application : KtxGame<Screen>() {
    override fun create() {
        val gameScreen = GameScreen(null)
        screens.put(GameScreen::class.java, gameScreen)
        val mainMenu = MainMenu {
            gameScreen.initialize()
            currentScreen = screens[GameScreen::class.java]
        }
        screens.put(MainMenu::class.java, mainMenu)
        currentScreen = screens[MainMenu::class.java]
    }
}

fun main(args: Array<String>) {
    LwjglApplication(Application(), createConfiguration())
}

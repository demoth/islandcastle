package org.demoth.icastle

import com.badlogic.gdx.Screen
import com.badlogic.gdx.backends.lwjgl.LwjglApplication
import ktx.app.KtxGame
import org.demoth.icastle.ui.screens.GameScreen
import org.demoth.icastle.ui.screens.MainMenu

class Application : KtxGame<Screen>() {
    override fun create() {
        val gameScreen = GameScreen()
        gameScreen.initialize()
        screens.put(GameScreen::class.java, gameScreen)

        val mainMenu = MainMenu { mapName ->
            gameScreen.changeLevel(mapName, null)
            currentScreen = screens[GameScreen::class.java]
        }

        gameScreen.goToMainMenu = {
            currentScreen = mainMenu
            mainMenu.enableInput()
        }

        screens.put(MainMenu::class.java, mainMenu)
        currentScreen = mainMenu
    }
}

fun main(args: Array<String>) {
    LwjglApplication(Application(), createConfiguration())
}

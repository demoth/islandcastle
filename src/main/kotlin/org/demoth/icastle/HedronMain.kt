package org.demoth.icastle

import com.badlogic.gdx.backends.lwjgl.LwjglApplication
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration

fun main(args: Array<String>) {
    val config = LwjglApplicationConfiguration()
    config.title = "IslandCastle 0.1.1"
    config.width = 1000
    config.height = 1000
    config.forceExit = false
    config.resizable = false
    LwjglApplication(Game2dTest(args.getOrNull(0)), config)
}


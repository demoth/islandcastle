package org.demoth.ktxtest

import com.badlogic.gdx.backends.lwjgl.LwjglApplication
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration

fun main(args: Array<String>) {
    val config = LwjglApplicationConfiguration()
    config.title = "Topory7 0.0.2"
    config.width = 1000
    config.height = 1000
    config.forceExit = false
    config.resizable = false
    LwjglApplication(Game2dTest(), config)
}


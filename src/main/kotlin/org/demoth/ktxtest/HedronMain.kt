package org.demoth.ktxtest

import com.badlogic.gdx.backends.lwjgl.LwjglApplication
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration

fun main(args: Array<String>) {
    val config = LwjglApplicationConfiguration()
    config.title = "Hello Daniil"
    config.width = 2000
    config.height = 1000
    config.forceExit = false
    LwjglApplication(Game2dTest(), config)
}


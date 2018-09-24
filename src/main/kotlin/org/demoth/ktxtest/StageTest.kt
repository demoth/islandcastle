package org.demoth.ktxtest

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextField


class StageTest : ApplicationAdapter() {
    lateinit var stage: Stage
    lateinit var table: Table

    override fun create() {
        stage = Stage()
        Gdx.input.inputProcessor = stage

        table = Table()
        table.setFillParent(true)
        stage.addActor(table)

        table.setDebug(true) // This is optional, but enables debug lines for tables.

        val skin = Skin()

        // Add widgets to the table here.
        val nameLabel = Label("Name:", skin)
        val nameText = TextField("", skin)
        val addressLabel = Label("Address:", skin)
        val addressText = TextField("", skin)

        table.add<Actor>(nameLabel)
        table.add<Actor>(nameText).width(100f)
        table.row()
        table.add<Actor>(addressLabel)
        table.add<Actor>(addressText).width(100f)
    }

    override fun resize(width: Int, height: Int) {
        stage.viewport.update(width, height, true)
    }

    override fun render() {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        stage.act(Gdx.graphics.deltaTime)
        stage.draw()
    }

    override fun dispose() {
        stage.dispose()
    }
}
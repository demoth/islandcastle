package org.demoth.ktxtest

/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/


import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Pixmap.Format
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener

class UISimpleTest : ApplicationAdapter() {
    lateinit var skin: Skin
    lateinit var stage: Stage
    lateinit var batch: SpriteBatch

    override fun create() {
        batch = SpriteBatch()
        stage = Stage()
        Gdx.input.inputProcessor = stage

        // A skin can be loaded via JSON or defined programmatically, either is fine. Using a skin is optional but strongly
        // recommended solely for the convenience of getting a texture, region, etc as a drawable, tinted drawable, etc.
        skin = Skin()

        // Generate a 1x1 white texture and store it in the skin named "white".
        val pixmap = Pixmap(1, 1, Format.RGBA8888)
        pixmap.setColor(Color.WHITE)
        pixmap.fill()
        skin.add("white", Texture(pixmap))

        // Store the default libgdx font under the name "default".
        skin.add("default", BitmapFont())

        // Configure a TextButtonStyle and name it "default". Skin resources are stored by type, so this doesn't overwrite the font.
        val textButtonStyle = TextButtonStyle()
        textButtonStyle.up = skin.newDrawable("white", Color.DARK_GRAY)
        textButtonStyle.down = skin.newDrawable("white", Color.DARK_GRAY)
        textButtonStyle.checked = skin.newDrawable("white", Color.BLUE)
        textButtonStyle.over = skin.newDrawable("white", Color.LIGHT_GRAY)
        textButtonStyle.font = skin.getFont("default")
        skin.add("default", textButtonStyle)

        // Create a table that fills the screen. Everything else will go inside this table.
        val table = Table()
        table.setFillParent(true)
        stage.addActor(table)

        // Create a button with the "default" TextButtonStyle. A 3rd parameter can be used to specify a name other than "default".
        val button = TextButton("Click me!", skin)
        table.add(button)

        // Add a listener to the button. ChangeListener is fired when the button's checked state changes, eg when clicked,
        // Button#setChecked() is called, via a key press, etc. If the event.cancel() is called, the checked state will be reverted.
        // ClickListener could have been used, but would only fire when clicked. Also, canceling a ClickListener event won't
        // revert the checked state.
        button.addListener(object : ChangeListener() {
            override fun changed(event: ChangeListener.ChangeEvent, actor: Actor) {
                println("Clicked! Is checked: " + button.isChecked)
                button.setText("Good job!")
            }
        })

        // Add an playerTex actor. Have to set the size, else it would be the size of the drawable (which is the 1x1 texture).
        table.add(Image(skin.newDrawable("white", Color.RED))).size(64f)
    }

    override fun render() {
        Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        stage.act(Math.min(Gdx.graphics.deltaTime, 1 / 30f))
        stage.draw()
    }

    override fun resize(width: Int, height: Int) {
        stage.viewport.update(width, height, true)
    }

    override fun dispose() {
        stage.dispose()
        skin.dispose()
    }
}

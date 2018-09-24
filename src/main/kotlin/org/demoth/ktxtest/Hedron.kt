package org.demoth.ktxtest

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.audio.AudioDevice
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch

class Hedron : ApplicationAdapter() {
    lateinit var batch: SpriteBatch
    lateinit var img: Texture
    lateinit var audio: AudioDevice
    lateinit var audioFile: Sound
    var played = false


    override fun create() {
        batch = SpriteBatch()
        img = Texture("badlogic.jpg")
        audio = Gdx.audio.newAudioDevice(44100, false)
        audioFile = Gdx.audio.newSound(Gdx.files.internal("camera1.wav"))
    }

    override fun render() {
        Gdx.gl.glClearColor(1f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        batch.begin()
        batch.draw(img, 0f, 0f)
        batch.end()
        if (!played) {
            audioFile.play()
            played = true
        }

    }

    override fun dispose() {
        batch.dispose()
        img.dispose()
    }
}
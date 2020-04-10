package org.demoth.icastle.ui

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import ktx.freetype.loadFreeTypeFont
import ktx.freetype.registerFreeTypeFontLoaders
import ktx.style.set

fun createSkin(assetManager: AssetManager): Skin {
    val font = "fonts/CinzelDecorative-Regular.ttf"
    assetManager.registerFreeTypeFontLoaders()
    assetManager.loadFreeTypeFont(font) {
        size = 28
    }
    assetManager.finishLoading()
    val labelFont = assetManager.get<BitmapFont>(font)
    val skin = Skin()
    skin["main-menu-label"] = Label.LabelStyle(labelFont, Color.WHITE)
    return skin
}

fun getTestSkin(): Skin {
    return Skin(Gdx.files.internal("skins/default/uiskin.json"))
}
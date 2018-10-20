package org.demoth.ktxtest

import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.io.File


@RunWith(Parameterized::class)
class AssetsTest(private val filename: String) {
    companion object {
        @Parameterized.Parameters(name = "{0}")
        @JvmStatic
        fun data(): Collection<Array<String>> {
            return Sprites.values().map { arrayOf("assets/sprites/${it.filename}") }.union(
                    Sounds.values().map { arrayOf("assets/sounds/${it.filename}") }).union(
                    SpriteSheets.values().map { arrayOf("assets/spriteSheets/${it.filename}") }
            )
        }
    }

    @Test
    fun `asset exists`() {
        assertTrue("Asset [$filename] does not exist!", File(filename).exists())
    }
}
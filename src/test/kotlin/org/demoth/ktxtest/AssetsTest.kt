package org.demoth.ktxtest

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File


class AssetsTest {

    @Test
    fun `sprites exist`() {
        Sprites.values().forEach {
            assertTrue("Sprite [${it.filename}] does not exist!", File("assets/sprites/${it.filename}").exists())
        }
    }


}
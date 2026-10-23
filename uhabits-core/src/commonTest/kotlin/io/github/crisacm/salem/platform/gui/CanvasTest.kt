package io.github.crisacm.salem.platform.gui

import io.github.crisacm.salem.platform.io.createTestCanvas
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class CanvasTest {
    @Test
    fun testDrawTestImage() = runTest {
        val canvas = createTestCanvas(500, 400)
        canvas.drawTestImage()
        assertRenders("views/CanvasTest.png", canvas)
    }
}

package io.github.crisacm.salem.platform.io

import io.github.crisacm.salem.platform.gui.Canvas
import io.github.crisacm.salem.platform.gui.JavaCanvas
import io.github.crisacm.salem.platform.time.JavaLocalDateFormatter
import io.github.crisacm.salem.platform.time.LocalDateFormatter
import java.awt.image.BufferedImage
import java.awt.image.BufferedImage.TYPE_INT_ARGB
import java.util.Locale

actual fun createTestFileOpener(): FileOpener = JavaFileOpener()
actual fun createTestDatabaseOpener(): DatabaseOpener = JavaDatabaseOpener()

actual fun createTestCanvas(width: Int, height: Int): Canvas {
    return JavaCanvas(BufferedImage(2 * width, 2 * height, TYPE_INT_ARGB), 2.0)
}

actual fun createTestDateFormatter(): LocalDateFormatter {
    return JavaLocalDateFormatter(Locale.US)
}

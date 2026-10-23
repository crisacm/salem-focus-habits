package io.github.crisacm.salem.platform.io

import io.github.crisacm.salem.platform.gui.Canvas
import io.github.crisacm.salem.platform.time.LocalDateFormatter

expect fun createTestFileOpener(): FileOpener
expect fun createTestDatabaseOpener(): DatabaseOpener
expect fun createTestCanvas(width: Int, height: Int): Canvas
expect fun createTestDateFormatter(): LocalDateFormatter

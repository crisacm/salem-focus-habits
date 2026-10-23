package io.github.crisacm.salem.core.utils

import io.github.crisacm.salem.core.BaseUnitTest
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertTrue

class FileExtensionsTest : BaseUnitTest() {

    @Test
    fun testIsSQLite3File() = runTest {
        val userFile = copyResourceToTempFile("loop.db")
        val isSqlite3File = isSQLite3File(userFile)
        assertTrue(isSqlite3File)
    }
}

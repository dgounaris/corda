package gradle.dependx.library

import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.File
import kotlin.test.assertEquals

internal class DependantsEnhancedFileReaderTest {
  @Before
  fun initDirs() {
    val testFile = File(DependxLibrarian.DEPENDANTS_FILE)
    testFile.createNewFile()
    testFile.writeText("""
      project: ':'
      dependants:
        - :node
        - ':'
      nonDependants: []
      ---
      project: :client
      dependants:
        - :client
      nonDependants:
        - :node
      ---
    """.trimIndent())
  }

  @After
  fun cleanupDirs() {
    File(DependxLibrarian.DEPENDANTS_FILE).delete()
  }

  @Test
  fun `can read file properly`() {
    val parsedList = DependantsEnhancedFileReader.parseFile()
    assertEquals(2, parsedList.size)
  }
}
package gradle.dependx.library

import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.File
import kotlin.test.assertEquals

internal class DependxiesFileReaderTest {
  @Before
  fun initDirs() {
    val testFile = File(DependxLibrarian.DEPENDXIES_FILE)
    testFile.createNewFile()
    testFile.writeText("""
      project: ':'
      autoDependencies:
        - :node
        - ':'
      manualDependencies: []
      ---
      project: :client
      autoDependencies:
        - :client
      manualDependencies: []
      ---
    """.trimIndent())
  }

  @After
  fun cleanupDirs() {
    File(DependxLibrarian.DEPENDXIES_FILE).delete()
  }

  @Test
  fun `can read file properly`() {
    val parsedList = DependxiesFileReader.parseFile()
    assertEquals(2, parsedList.size)
  }
}
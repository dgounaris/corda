package gradle.dependx.analysis

import gradle.dependx.library.DependxLibrarian
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.File
import kotlin.test.assertEquals

internal class DependantsEnhancedResolverTest {
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
      project: :node
      autoDependencies:
        - :client
      manualDependencies: []
      ---
      project: :client
      autoDependencies: []
      manualDependencies: []
    """.trimIndent())
  }

  @After
  fun cleanupDirs() {
    File(DependxLibrarian.DEPENDXIES_FILE).delete()
  }

  @Test
  fun `Should properly return dependants and non dependants`() {
    val resolved = DependantsEnhancedResolver.resolve()
    assertEquals(3, resolved.size)
    assertEquals(1, resolved[0].dependants.size)
    assertEquals(2, resolved[0].nonDependants.size)
    assertEquals(2, resolved[1].dependants.size)
    assertEquals(1, resolved[1].nonDependants.size)
    assertEquals(3, resolved[2].dependants.size)
    assertEquals(0, resolved[2].nonDependants.size)
  }
}
package gradle.dependx.consulting

import gradle.dependx.library.DependxLibrarian
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.File
import kotlin.test.assertEquals

internal class IrrelevantProjectsConsultantTest {
  @Before
  fun initDirs() {
    val testFile = File(DependxLibrarian.DEPENDANTS_FILE)
    testFile.createNewFile()
    testFile.writeText("""
      project: ':'
      dependants:
        - :node
        - ':'
      nonDependants:
        - :foo1
        - :foo2
        - :bar
      ---
      project: :client
      dependants:
        - :client
      nonDependants:
        - :node
        - :foo3
        - :foo2
        - :bar
      ---
    """.trimIndent())
  }

  @After
  fun deleteDirs() {
    File(DependxLibrarian.DEPENDANTS_FILE).delete()
  }

  @Test
  fun `should return the smallest common set`() {
    val irrelevant = IrrelevantProjectsConsultant.consult(listOf(":", ":client"))
    assertEquals(2, irrelevant.size)
    assert(irrelevant.containsAll(listOf(":foo2", ":bar")))
  }
}
package gradle.dependx.library

import gradle.dependx.models.DependantsEnhancedInfo
import org.junit.After
import org.junit.Test
import java.io.File
import kotlin.test.assertEquals

internal class DependantsEnhancedFileAuthorTest {
  @After
  fun cleanupDirs() {
    File(DependxLibrarian.DEPENDANTS_FILE).delete()
  }

  @Test
  fun `writes the correct auto info to file`() {
    val infoList = listOf(
        DependantsEnhancedInfo(
            "parent",
            listOf("child1", "child2", "parent"),
            emptyList()
        ),
        DependantsEnhancedInfo(
            "child1",
            listOf("child1"),
            listOf("child2")
        ),
        DependantsEnhancedInfo(
            "child2",
            listOf("child2"),
            listOf()
        )
    )
    DependantsEnhancedFileAuthor.write(infoList)

    val expected = """
      project: parent
      dependants:
        - child1
        - child2
        - parent
      nonDependants: []
      ---
      project: child1
      dependants:
        - child1
      nonDependants:
        - child2
      ---
      project: child2
      dependants:
        - child2
      nonDependants: []
    """.trimIndent()
    val lines = File(DependxLibrarian.DEPENDANTS_FILE).readLines()
    lines.joinToString("\n")
    assertEquals(expected, lines.joinToString("\n"))
  }
}
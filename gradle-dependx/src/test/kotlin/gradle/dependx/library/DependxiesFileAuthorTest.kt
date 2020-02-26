package gradle.dependx.library

import gradle.dependx.models.DependencyInfo
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.File
import kotlin.test.assertEquals

internal class DependxiesFileAuthorTest {

  @After
  fun cleanupDirs() {
    File(DependxLibrarian.DEPENDXIES_FILE).delete()
  }

  @Test
  fun `writes the correct auto info to fresh file`() {
    val infoList = listOf(
        DependencyInfo(
            "parent",
            listOf("child1", "child2"),
            emptyList()
        ),
        DependencyInfo(
            "child1",
            listOf("nested1"),
            emptyList()
        ),
        DependencyInfo(
            "child2",
            listOf(),
            listOf()
        )
    )
    DependxiesFileAuthor.write(infoList)

    val expected = """
      project: parent
      autoDependencies:
        - child1
        - child2
        - parent
      manualDependencies: []
      ---
      project: child1
      autoDependencies:
        - nested1
        - child1
      manualDependencies: []
      ---
      project: child2
      autoDependencies:
        - child2
      manualDependencies: []
    """.trimIndent()
    val lines = File(DependxLibrarian.DEPENDXIES_FILE).readLines()
    lines.joinToString("\n")
    assertEquals(expected, lines.joinToString("\n"))
  }

  @Test
  fun `writes the correct auto info to old file`() {
    val file = File(DependxLibrarian.DEPENDXIES_FILE)
    file.createNewFile()

    val oldInfoList = listOf(
        DependencyInfo(
            "child1",
            emptyList(),
            listOf("custom1")
        )
    )
    DependxiesFileAuthor.write(oldInfoList)

    val infoList = listOf(
        DependencyInfo(
            "parent",
            listOf("child1", "child2"),
            emptyList()
        ),
        DependencyInfo(
            "child1",
            listOf("nested1"),
            emptyList()
        ),
        DependencyInfo(
            "child2",
            listOf(),
            listOf()
        )
    )
    DependxiesFileAuthor.write(infoList)

    val expected = """
      project: parent
      autoDependencies:
        - child1
        - child2
        - parent
      manualDependencies: []
      ---
      project: child1
      autoDependencies:
        - nested1
        - child1
      manualDependencies:
        - custom1
      ---
      project: child2
      autoDependencies:
        - child2
      manualDependencies: []
    """.trimIndent()
    val lines = File(DependxLibrarian.DEPENDXIES_FILE).readLines()
    lines.joinToString("\n")
    assertEquals(expected, lines.joinToString("\n"))
  }

}
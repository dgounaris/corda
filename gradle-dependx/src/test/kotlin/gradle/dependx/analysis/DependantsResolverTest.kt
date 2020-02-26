package gradle.dependx.analysis

import gradle.dependx.models.DependencyInfo
import org.junit.Test
import kotlin.test.assertEquals

internal class DependantsResolverTest {
  @Test
  fun `Should properly return dependants and ignore dependencies`() {
    val infoList = listOf(
        DependencyInfo(
            "test",
            listOf("test1", "test2"),
            emptyList()
        ),
        DependencyInfo(
            "test1",
            listOf("test3"),
            emptyList()
        ),
        DependencyInfo(
            "test2",
            emptyList(),
            emptyList()
        ),
        DependencyInfo(
            "test3",
            emptyList(),
            emptyList()
        ),
        DependencyInfo(
            "test4",
            emptyList(),
            listOf("test2")
        )
    )
    var resolved = DependantsResolver.resolve("test", infoList)
    assertEquals(1, resolved.size)
    resolved = DependantsResolver.resolve("test1", infoList)
    assertEquals(2, resolved.size)
    resolved = DependantsResolver.resolve("test2", infoList)
    assertEquals(3, resolved.size)
    resolved = DependantsResolver.resolve("test3", infoList)
    assertEquals(3, resolved.size)
  }
}
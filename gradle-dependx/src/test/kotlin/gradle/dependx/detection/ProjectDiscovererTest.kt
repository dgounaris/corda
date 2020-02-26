package gradle.dependx.detection

import gradle.dependx.detection.ProjectDiscoverer
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Test
import kotlin.test.assertEquals

internal class ProjectDiscovererTest {
  @Test
  fun `reads project children properly`() {
    // Create a test project and apply the plugin
    val parent = ProjectBuilder.builder().withName("test").build()
    val child1 = ProjectBuilder.builder().withName("child1").withParent(parent).build()
    val child2 = ProjectBuilder.builder().withName("child2").withParent(parent).build()
    val nestedchild = ProjectBuilder.builder().withName("nestedchild1").withParent(child2).build()

    val discovered = ProjectDiscoverer.discover(parent)
    assertEquals(4, discovered.size)
    assert(discovered.contains(":"))
    assert(discovered.contains(":child1"))
    assert(discovered.contains(":child2"))
    assert(discovered.contains(":child2:nestedchild1"))
  }
}
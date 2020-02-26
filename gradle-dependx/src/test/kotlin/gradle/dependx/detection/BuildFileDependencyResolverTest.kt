package gradle.dependx.detection

import gradle.dependx.library.DependxLibrarian
import org.gradle.testfixtures.ProjectBuilder
import org.junit.After
import org.junit.Test
import java.io.File
import kotlin.test.assertEquals

internal class BuildFileDependencyResolverTest {
  @After
  fun cleanupDirs() {
    File("build/test/").deleteRecursively()
    File(DependxLibrarian.CIRCLES_FILE).delete()
  }

  @Test
  fun `reads one-level dependencies properly`() {
    // Create a test project and apply the plugin
    val testDir = createBuildDir("test", listOf(":child1", ":child2:"))
    val child1Dir = createBuildDir("child1", listOf(), delim = "\"")
    val child2Dir = createBuildDir("child2", listOf())
    val parent = ProjectBuilder.builder().withProjectDir(testDir).withName("test").build()
    val child1 = ProjectBuilder.builder().withProjectDir(child1Dir).withName("child1").withParent(parent).build()
    val child2 = ProjectBuilder.builder().withProjectDir(child2Dir).withName("child2").withParent(parent).build()
    val nestedchild = ProjectBuilder.builder().withName("nestedchild1").withParent(child2).build()
    val input = mapOf(
        parent.path to parent,
        child1.path to child1,
        child2.path to child2,
        nestedchild.path to nestedchild
    )

    val resolver = BuildFileDependencyResolver(input)
    val resolved = resolver.resolve(":")
    assertEquals(2, resolved.size)
    assertEquals(":child1", resolved[0])
    assertEquals(":child2", resolved[1])
  }

  @Test
  fun `reads one-level dependencies with path format properly`() {
    // Create a test project and apply the plugin
    val testDir = createBuildDir("test", listOf(":child1"))
    val child1Dir = createBuildDir("child1", listOf("path: ':child2', configuration: 'fooconfig'"), delim = "")
    val child2Dir = createBuildDir("child2", listOf())
    val parent = ProjectBuilder.builder().withProjectDir(testDir).withName("test").build()
    val child1 = ProjectBuilder.builder().withProjectDir(child1Dir).withName("child1").withParent(parent).build()
    val child2 = ProjectBuilder.builder().withProjectDir(child2Dir).withName("child2").withParent(parent).build()
    val nestedchild = ProjectBuilder.builder().withName("nestedchild1").withParent(child2).build()
    val input = mapOf(
        parent.path to parent,
        child1.path to child1,
        child2.path to child2,
        nestedchild.path to nestedchild
    )

    val resolver = BuildFileDependencyResolver(input)
    val resolved = resolver.resolve(":")
    assertEquals(2, resolved.size)
    assertEquals(":child1", resolved[0])
    assertEquals(":child2", resolved[1])
  }

  @Test
  fun `reads multi-level dependencies properly`() {
    // Create a test project and apply the plugin
    val testDir = createBuildDir("test", listOf(":child1", ":child2"))
    val child1Dir = createBuildDir("child1", listOf())
    val child2Dir = createBuildDir("child2", listOf(":child2:nestedchild1"))
    val nestedChildDir = createBuildDir("nestedchild1", listOf(":child1"))
    val parent = ProjectBuilder.builder().withProjectDir(testDir).withName("test").build()
    val child1 = ProjectBuilder.builder().withProjectDir(child1Dir).withName("child1").withParent(parent).build()
    val child2 = ProjectBuilder.builder().withProjectDir(child2Dir).withName("child2").withParent(parent).build()
    val nestedchild = ProjectBuilder.builder().withProjectDir(nestedChildDir).withName("nestedchild1").withParent(child2).build()
    val input = mapOf(
        parent.path to parent,
        child1.path to child1,
        child2.path to child2,
        nestedchild.path to nestedchild
    )

    val resolver = BuildFileDependencyResolver(input)
    val resolved = resolver.resolve(":")
    assertEquals(3, resolved.size)
    assertEquals(":child1", resolved[0])
    assertEquals(":child2", resolved[1])
    assertEquals(":child2:nestedchild1", resolved[2])
  }

  @Test
  fun `reads multi-level dependencies with circles properly`() {
    // Create a test project and apply the plugin
    val testDir = createBuildDir("test", listOf(":child1", ":child2"))
    val child1Dir = createBuildDir("child1", listOf(":child2:nestedchild1"))
    val child2Dir = createBuildDir("child2", listOf(":child2:nestedchild1"))
    val nestedChildDir = createBuildDir("nestedchild1", listOf(":child1"))
    val parent = ProjectBuilder.builder().withProjectDir(testDir).withName("test").build()
    val child1 = ProjectBuilder.builder().withProjectDir(child1Dir).withName("child1").withParent(parent).build()
    val child2 = ProjectBuilder.builder().withProjectDir(child2Dir).withName("child2").withParent(parent).build()
    val nestedchild = ProjectBuilder.builder().withProjectDir(nestedChildDir).withName("nestedchild1").withParent(child2).build()
    val input = mapOf(
        parent.path to parent,
        child1.path to child1,
        child2.path to child2,
        nestedchild.path to nestedchild
    )

    val resolver = BuildFileDependencyResolver(input)
    val resolved = resolver.resolve(":")
    assertEquals(3, resolved.size)
    assertEquals(":child1", resolved[0])
    assertEquals(":child2", resolved[1])
    assertEquals(":child2:nestedchild1", resolved[2])
  }

  private fun createBuildDir(projectName: String, dependencies: List<String>, delim: String = "'"): File {
    val dependenciesText = dependencies.map { "compile project($delim$it$delim)" }.joinToString("\n")
    val projectDir = File("build/test/$projectName")
    projectDir.mkdirs()
    projectDir.resolve("build.gradle").writeText("""
            plugins {
                id('gradle.dependx.greeting')
            }
            
            dependencies {
              $dependenciesText
            }
        """)

    return projectDir
  }
}
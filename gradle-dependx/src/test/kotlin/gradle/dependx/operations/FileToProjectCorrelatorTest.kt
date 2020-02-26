package gradle.dependx.operations

import org.gradle.testfixtures.ProjectBuilder
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.File
import kotlin.test.assertEquals

internal class FileToProjectCorrelatorTest {
  private val projectDir = File("./src/test/resources/mydir/")
  private val project1Dir = File("./src/test/resources/mydir1/")
  private val file1Dir = File("./src/test/resources/mydir/foo.txt")
  private val file2Dir = File("./src/test/resources/mydir/foo/bar.txt")
  private val file3Dir = File("./src/test/resources/mydir/foo/bar/foo.txt")
  private val file4Dir = File("./src/test/resources/mydir1/foo/bar.txt")

  @Before
  fun createDirs() {
    projectDir.mkdirs()
    project1Dir.mkdirs()
    file1Dir.mkdirs()
    file2Dir.mkdirs()
    file3Dir.mkdirs()
    file4Dir.mkdirs()
  }

  @After
  fun deleteDirs() {
    projectDir.deleteRecursively()
    project1Dir.deleteRecursively()
    file1Dir.deleteRecursively()
    file2Dir.deleteRecursively()
    file3Dir.deleteRecursively()
    file4Dir.deleteRecursively()
  }

  @Test
  fun `should correlate the files to the project properly`() {
    val parent = ProjectBuilder.builder().withProjectDir(projectDir).withName("test").build()
    val child = ProjectBuilder.builder().withProjectDir(project1Dir).withName("test1").withParent(parent).build()
    val correlated1 = FileToProjectCorrelator.correlate(file1Dir, parent)
    assertEquals(parent, correlated1)
    val correlated2 = FileToProjectCorrelator.correlate(file2Dir, parent)
    assertEquals(parent, correlated2)
    val correlated3 = FileToProjectCorrelator.correlate(file3Dir, parent)
    assertEquals(parent, correlated3)
    val correlated4 = FileToProjectCorrelator.correlate(file4Dir, parent)
    assertEquals(child, correlated4)
  }

  @Test
  fun `should correlate the files to distinct projects when calling the public api`() {
    val parent = ProjectBuilder.builder().withProjectDir(projectDir).withName("test").build()
    val child = ProjectBuilder.builder().withProjectDir(project1Dir).withName("test1").withParent(parent).build()
    val correlated = FileToProjectCorrelator.correlate(listOf(file1Dir, file2Dir, file3Dir, file4Dir), parent)
    assertEquals(2, correlated.size)
    assertEquals(parent, correlated[0])
    assertEquals(child, correlated[1])
  }
}
package gradle.dependx.operations

import org.gradle.api.Project
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.logging.Logger

class GitChangesRetriever {
  companion object {
    private val logger = Logger.getAnonymousLogger()

    @JvmStatic
    fun retrievePaths(project: Project, depth: Int = 2): List<File> {
      val byteOut = ByteArrayOutputStream()
      project.exec {
        it.executable = "git"
        it.args = listOf(
            "--git-dir", "${project.projectDir.canonicalPath}/.git",
            "diff-tree",
            "--no-commit-id",
            "--name-only",
            "-r", "HEAD..HEAD~$depth"
        )
        it.standardOutput = byteOut
      }
      return byteOut.toString().split("\n").map { getFileWithEnforcedProjectPathPrefix(it, project) }
    }

    @JvmStatic
    private fun getFileWithEnforcedProjectPathPrefix(path: String, project: Project): File {
      return File("${project.projectDir.canonicalPath}/$path")
    }
  }
}
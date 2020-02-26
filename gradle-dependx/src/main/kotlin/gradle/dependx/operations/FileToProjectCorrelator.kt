package gradle.dependx.operations

import org.gradle.api.Project
import java.io.File

class FileToProjectCorrelator {
  companion object {
    @JvmStatic
    fun correlate(files: List<File>, project: Project): List<Project> {
      val changedProjects = files.map { FileToProjectCorrelator.correlate(it, project) }
      // filternotnull is not really necessary, but kotlin seems to not recognize it
      return if (changedProjects.contains(null)) project.allprojects.toList() else changedProjects.filterNotNull().distinct()
    }

    @JvmStatic
    fun correlate(file: File, project: Project): Project? {
      val found = project.allprojects
          .sortedByDescending { it.projectDir.canonicalPath } // bigger path means nested (more relevant) project
          .find {
            val projectPath = it.projectDir.canonicalPath
            return@find file.canonicalPath.startsWith(projectPath)
          }
      return found
    }
  }
}
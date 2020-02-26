package gradle.dependx.detection

import org.gradle.api.Project

class ProjectDiscoverer private constructor() {
  companion object {
    @JvmStatic
    fun discover(project: Project): Map<String, Project> {
      return project.allprojects.map { it.path to it }.toMap()
    }
  }
}
package gradle.dependx.analysis

import gradle.dependx.library.DependxiesFileReader
import gradle.dependx.models.DependencyInfo

/**
 * Uses the reader to get the yml file,
 * and then memorizes it in a convenient format for operations
 */
class DependxiesFileMemorizer {
  companion object {
    private val dependxiesMap: Map<String, DependencyInfo> by lazy {
      DependxiesFileReader.parseFile()
          .map {
            it.project to it
          }.toMap()
    }

    @JvmStatic
    fun recallAll(): Map<String, DependencyInfo> {
      return dependxiesMap
    }

    @JvmStatic
    fun recall(projectName: String): DependencyInfo {
      return dependxiesMap[projectName] ?: throw Exception("No project name $projectName memorized")
    }
  }
}
package gradle.dependx.consulting

import gradle.dependx.library.DependantsEnhancedFileReader
import gradle.dependx.library.DependxiesFileReader
import gradle.dependx.models.DependantsEnhancedInfo
import gradle.dependx.models.DependencyInfo

/**
 * Uses the reader to get the yml file,
 * and then memorizes it in a convenient format for operations
 */
class DependantsFileMemorizer {
  companion object {
    private val dependantsMap: Map<String, DependantsEnhancedInfo> by lazy {
      DependantsEnhancedFileReader.parseFile()
          .map {
            it.project to it
          }.toMap()
    }

    @JvmStatic
    fun recallAll(): Map<String, DependantsEnhancedInfo> {
      return dependantsMap
    }

    @JvmStatic
    fun recall(projectName: String): DependantsEnhancedInfo {
      return dependantsMap[projectName] ?: throw Exception("No project name $projectName memorized")
    }
  }
}
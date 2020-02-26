package gradle.dependx.analysis

import gradle.dependx.models.DependantsEnhancedInfo
import gradle.dependx.models.DependencyInfo

/**
 * Reports dependants and non-dependants on all projects included in yml
 */
class DependantsEnhancedResolver {
  companion object {
    @JvmStatic
    fun resolve(): List<DependantsEnhancedInfo> {
      val infoList = listOf(*DependxiesFileMemorizer.recallAll().values.toTypedArray())
      val dependantsByProjectName = getDependantsByProjectName(infoList)
      val nonDependantsByProjectName = getNonDependantsByProjectName(infoList, dependantsByProjectName)
      return infoList.map { it.project }
          .map {
            DependantsEnhancedInfo(
                it,
                dependantsByProjectName.getOrDefault(it, emptyList()),
                nonDependantsByProjectName.getOrDefault(it, emptyList())
            )
          }
    }

    @JvmStatic
    private fun getDependantsByProjectName(infoList: List<DependencyInfo>): Map<String, List<String>> {
      return infoList.map {
        it.project to DependantsResolver.resolve(it.project, infoList)
      }.toMap()
    }

    @JvmStatic
    private fun getNonDependantsByProjectName(infoList: List<DependencyInfo>, dependantsByProjectName: Map<String, List<String>>): Map<String, List<String>> {
      val allProjectNames = infoList.map { it.project }
      return infoList.map {
        it.project to allProjectNames - dependantsByProjectName[it.project]!!
      }.toMap()
    }
  }
}
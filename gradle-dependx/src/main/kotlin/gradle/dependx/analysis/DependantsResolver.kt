package gradle.dependx.analysis

import gradle.dependx.models.DependencyInfo
import java.util.logging.Logger

/**
 * This class reports which are the dependants on a project,
 * using the yml file generated
 */
class DependantsResolver {
  companion object {
    private val logger = Logger.getAnonymousLogger()

    @JvmStatic
    fun resolve(projectName: String, infoList: List<DependencyInfo>): List<String> {
      return resolve(projectName, infoList, emptyList()).map { it.project }
    }

    @JvmStatic
    private fun resolve(projectName: String, infoList: List<DependencyInfo>, resolved: List<String>): List<DependencyInfo> {
      val directlyDependant = infoList.filter {
        (it.project == projectName || projectName in it.autoDependencies || projectName in it.manualDependencies)
      }

      val updatedResolved = resolved + directlyDependant.map { it.project }

      val indirectlyDependant = directlyDependant
          .filterNot { it.project in resolved }
          .map { resolve(it.project, infoList, updatedResolved) }.flatten()

      return (directlyDependant + indirectlyDependant).distinct()
    }
  }
}
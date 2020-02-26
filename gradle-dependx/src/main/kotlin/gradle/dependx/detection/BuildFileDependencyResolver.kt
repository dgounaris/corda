package gradle.dependx.detection

import org.gradle.api.Project
import java.io.FileNotFoundException
import java.util.logging.Logger

class BuildFileDependencyResolver(
    private val projectsByName: Map<String, Project>
) {
  companion object {
    val logger = Logger.getAnonymousLogger()
  }

  fun resolve(projectNames: List<String>): Map<String, List<String>> {
    return projectNames.map {
      it to resolve(it)
    }.toMap()
  }

  fun resolve(projectName: String): List<String> {
    return resolve(projectName, emptyList(), listOf(projectName))
  }

  private fun resolve(projectName: String, resolved: List<String>, localResolutionHistory: List<String>): List<String> {
    require(projectsByName.contains(projectName)) { "Error: Project name $projectName not contained in context map" }

    val directDependencies = getDirectDependencies(projectName)
    val directDependenciesWithoutCircles = getDirectDependenciesWithoutCircles(projectName, directDependencies, localResolutionHistory)

    val directDependenciesWithoutAlreadyTraversed = directDependenciesWithoutCircles.filterNot { it in resolved }

    val updatedResolved = resolved + directDependenciesWithoutAlreadyTraversed
    return (
        directDependenciesWithoutAlreadyTraversed +
            (
                directDependenciesWithoutAlreadyTraversed
                .map { resolve(it, updatedResolved, localResolutionHistory + it) }
                .flatten()
            )
        ).distinct()
  }

  private fun getDirectDependencies(projectName: String): List<String> {
    logger.fine("Resolving build file dependencies of $projectName")
    val directDependencies = getDirectBuildFileDependencies(projectsByName[projectName]!!)
    logger.fine("Direct dependencies of $projectName: ${directDependencies.joinToString(", ")}")
    return directDependencies
  }

  private fun getDirectDependenciesWithoutCircles(projectName: String, directDependencies: List<String>, localResolutionHistory: List<String>): List<String> {
    val circularDependencies = CircleDependencyDetector.detect(directDependencies, localResolutionHistory)
    val directDependenciesWithoutCircles = directDependencies.filterNot { it in circularDependencies }
    logger.fine("Direct dependencies without circles of $projectName: ${directDependenciesWithoutCircles.joinToString(", ")}")
    return directDependenciesWithoutCircles
  }

  private fun getDirectBuildFileDependencies(project: Project): List<String> {
    val buildFile = project.buildFile
    try {
      return buildFile.useLines {
        lines -> return@useLines lines.map {
        // regex matches all project('...') or project(path: '...', ...), in order to find project names in the file
        line -> return@map "project\\((['\"](.*?)['\"]\\)|path: ['\"](.*?)['\"].*?\\))".toRegex().findAll(line).toList()
      }
          .flatten()
          .map { if (it.groupValues[2] != "") it.groupValues[2] else it.groupValues[3]}
          .map { it.dropLastWhile { lastChar -> lastChar == ':' } }
          .toList()
      }
    } catch (ex: FileNotFoundException) {
      logger.warning("Build file not found for ${project.path}")
      return emptyList()
    }
  }
}
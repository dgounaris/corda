package gradle.dependx.consulting

import org.gradle.api.Project

/**
 * This class impersonates a consultant,
 * picking up the info generated from an analysis
 * and providing useful insight regarding irrelevant
 * projects given a project list
 */
class IrrelevantProjectsConsultant {
  companion object {
    @JvmStatic
    fun consult(projects: List<String>): List<String> {
      var intersection = setOf<String>()
      val nonDependantsOfAll = projects.map { DependantsFileMemorizer.recall(it) }
          .map { it.nonDependants }
      nonDependantsOfAll.forEachIndexed { index, list ->
        intersection = if (index == 0) intersection.plus(list) else intersection.intersect(list)
      }
      return intersection.toList()
    }
  }
}
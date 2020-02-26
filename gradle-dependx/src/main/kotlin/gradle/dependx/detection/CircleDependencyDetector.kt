package gradle.dependx.detection

import gradle.dependx.library.DependxLibrarian
import java.io.File
import java.util.logging.Logger

class CircleDependencyDetector private constructor() {
  companion object {
    private val logger = Logger.getAnonymousLogger()
    private val circleReportFile : File by lazy {
      val newFile = File(DependxLibrarian.CIRCLES_FILE)
      if (!newFile.createNewFile()) {
        newFile.writeText("") // erase previous content
      }
      return@lazy newFile
    }

    @JvmStatic
    fun detect(directDependencies: List<String>, previousPathHistory: List<String>): List<String> {
      val circularDependencies = mutableListOf<String>()

      directDependencies.forEach {
        if (previousPathHistory.firstOrNull() == it) {
          notifyCircle(previousPathHistory + it)
          circularDependencies.add(it)
        }
      }

      return circularDependencies
    }

    @JvmStatic
    private fun notifyCircle(circle: List<String>) {
      try {
        circleReportFile.appendText("Circular dependency found in the following build route: ${circle.joinToString(" -> ")}\n")
      } catch (ex: Exception) {
        logger.warning("Circular dependency found in the following build route: ${circle.joinToString(" -> ")}")
        logger.severe("Exception when trying to write circle to file")
        ex.printStackTrace()
      }
    }
  }
}
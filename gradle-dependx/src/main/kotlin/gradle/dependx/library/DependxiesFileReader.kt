package gradle.dependx.library

import gradle.dependx.models.DependencyInfo
import org.yaml.snakeyaml.Yaml
import java.io.File

class DependxiesFileReader {
  companion object {
    private const val PROJECT_KEY = "project"
    private const val AUTO_DEPENDENCIES_KEY = "autoDependencies"
    private const val MANUAL_DEPENDENCIES_KEY = "manualDependencies"

    @JvmStatic
    fun parseFile(): List<DependencyInfo> {
      val yaml = Yaml()
      val stream = File(DependxLibrarian.DEPENDXIES_FILE).inputStream()
      val dependencyInfoList = mutableListOf<DependencyInfo>()
      stream.use {
        val allInfoIterator = yaml.loadAll(it).iterator()
        dependencyInfoList.addAll(parseIteratorData(allInfoIterator))
      }
      return dependencyInfoList
    }

    @JvmStatic
    private fun parseIteratorData(iterator: MutableIterator<Any>): List<DependencyInfo> {
      val dependencyInfoList = mutableListOf<DependencyInfo>()
      iterator.forEach {
        if (it != null) { // required if trailing `---` in yml file
          val infoAsMap = it as Map<*, *>
          dependencyInfoList.add(
              DependencyInfo(
                  infoAsMap[PROJECT_KEY]!! as String,
                  infoAsMap[AUTO_DEPENDENCIES_KEY] as List<String>,
                  infoAsMap[MANUAL_DEPENDENCIES_KEY] as List<String>
              )
          )
        }
      }
      return dependencyInfoList
    }
  }
}
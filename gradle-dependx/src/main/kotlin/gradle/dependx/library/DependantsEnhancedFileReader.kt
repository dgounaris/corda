package gradle.dependx.library

import gradle.dependx.models.DependantsEnhancedInfo
import org.yaml.snakeyaml.Yaml
import java.io.File

class DependantsEnhancedFileReader {
  companion object {
    private const val PROJECT_KEY = "project"
    private const val DEPENDANTS_KEY = "dependants"
    private const val NON_DEPENDANTS_KEY = "nonDependants"

    @JvmStatic
    fun parseFile(): List<DependantsEnhancedInfo> {
      val yaml = Yaml()
      val stream = File(DependxLibrarian.DEPENDANTS_FILE).inputStream()
      val dependantsEnhancedInfo = mutableListOf<DependantsEnhancedInfo>()
      stream.use {
        val allInfoIterator = yaml.loadAll(it).iterator()
        dependantsEnhancedInfo.addAll(parseIteratorData(allInfoIterator))
      }
      return dependantsEnhancedInfo
    }

    @JvmStatic
    private fun parseIteratorData(iterator: MutableIterator<Any>): List<DependantsEnhancedInfo> {
      val dependantsEnhancedList = mutableListOf<DependantsEnhancedInfo>()
      iterator.forEach {
        if (it != null) { // required if trailing `---` in yml file
          val infoAsMap = it as Map<*, *>
          dependantsEnhancedList.add(
              DependantsEnhancedInfo(
                  infoAsMap[PROJECT_KEY]!! as String,
                  infoAsMap[DEPENDANTS_KEY] as List<String>,
                  infoAsMap[NON_DEPENDANTS_KEY] as List<String>
              )
          )
        }
      }
      return dependantsEnhancedList
    }
  }
}
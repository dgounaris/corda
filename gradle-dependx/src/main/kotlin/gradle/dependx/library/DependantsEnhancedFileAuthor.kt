package gradle.dependx.library

import gradle.dependx.models.DependantsEnhancedInfo
import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.Yaml
import java.io.File

class DependantsEnhancedFileAuthor {
  companion object {
    @JvmStatic
    fun write(info: List<DependantsEnhancedInfo>) {
      val file = File(DependxLibrarian.DEPENDANTS_FILE)
      file.createNewFile()
      val writer = file.bufferedWriter()
      writer.use {
        val options = DumperOptions()
        options.indent = 4
        options.defaultFlowStyle = DumperOptions.FlowStyle.BLOCK
        options.indicatorIndent = 2
        val yaml = Yaml(options)
        yaml.dumpAll(constructEndObject(info).listIterator(), writer)
      }
    }

    @JvmStatic
    private fun constructEndObject(info: List<DependantsEnhancedInfo>): List<Map<String, Any>> {
      return info.map {
        mapOf(
            "project" to it.project,
            "dependants" to it.dependants,
            "nonDependants" to it.nonDependants
        )
      }
    }
  }
}
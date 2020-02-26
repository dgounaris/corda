package gradle.dependx.library

import gradle.dependx.models.DependencyInfo
import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.Yaml
import java.io.File
import java.io.Writer

class DependxiesFileAuthor {
  companion object {
    @JvmStatic
    fun write(info: List<DependencyInfo>) {
      val file = File(DependxLibrarian.DEPENDXIES_FILE)
      file.createNewFile()
      val existingInfo = DependxiesFileReader.parseFile()
      val writer = file.bufferedWriter()
      val endObj = constructEndObject(info, existingInfo)
      writeEndObject(writer, endObj)
    }

    @JvmStatic
    private fun constructEndObject(info: List<DependencyInfo>, existingInfo: List<DependencyInfo>): List<Map<String, Any>> {
      return info.map {
        val existingManualDependencies = existingInfo
            .find { existingInfoItem -> existingInfoItem.project == it.project }
            ?.manualDependencies ?: emptyList()
        mapOf(
            "project" to it.project,
            "autoDependencies" to (it.autoDependencies + it.project),
            "manualDependencies" to (it.manualDependencies + existingManualDependencies).distinct()
        )
      }
    }

    @JvmStatic
    private fun writeEndObject(writer: Writer, endObj: List<Map<String, Any>>) {
      writer.use {
        val options = DumperOptions()
        options.indent = 4
        options.defaultFlowStyle = DumperOptions.FlowStyle.BLOCK
        options.indicatorIndent = 2
        val yaml = Yaml(options)
        yaml.dumpAll(endObj.listIterator(), writer)
      }
    }
  }
}
package gradle.dependx.models

data class DependencyInfo(
    val project: String,
    val autoDependencies: List<String>,
    val manualDependencies: List<String>
)
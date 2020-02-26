package gradle.dependx.models

data class DependantsEnhancedInfo(
    val project: String,
    val dependants: List<String>,
    val nonDependants: List<String>
)
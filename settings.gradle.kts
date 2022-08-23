rootProject.name = "AliucordPlugins"

include(
    "EditUsers"
)

rootProject.children.forEach {
    it.projectDir = file("plugins/kotlin/${it.name}")
}

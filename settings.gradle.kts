pluginManagement {
    repositories {
        mavenLocal()
        mavenCentral()
        maven { setUrl("https://oss.sonatype.org/content/repositories/snapshots/") }
        gradlePluginPortal()
    }
}

rootProject.name = "sudoscan"

include("sudoscan-core")
include("sudoscan-solver-choco")
include("sudoscan-recognizer-nd4j", "sudoscan-recognizer-djl")
include("sudoscan-deskapp")

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
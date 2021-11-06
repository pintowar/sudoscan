pluginManagement {
    repositories {
        mavenLocal()
        mavenCentral()
        maven { setUrl("https://oss.sonatype.org/content/repositories/snapshots/") }
        gradlePluginPortal()
    }
}

rootProject.name = "sudoscan"

include("sudoscan-pages")
include("sudoscan-api")
include("sudoscan-solver-choco", "sudoscan-solver-ojalgo")
include("sudoscan-recognizer-dl4j", "sudoscan-recognizer-djl")
include("sudoscan-cli")

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
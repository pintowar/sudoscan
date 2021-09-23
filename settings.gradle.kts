pluginManagement {
    repositories {
        mavenLocal()
        mavenCentral()
        maven { setUrl("https://oss.sonatype.org/content/repositories/snapshots/") }
        gradlePluginPortal()
    }
}

rootProject.name = "sudoscan"

include("sudoscan-api")
include("sudoscan-solver-choco")
include("sudoscan-recognizer-dl4j", "sudoscan-recognizer-djl")
include("sudoscan-deskapp")
include("sudoscan-webserver", "sudoscan-webclient")

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

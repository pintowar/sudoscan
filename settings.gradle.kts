pluginManagement {
    repositories {
        mavenLocal()
        mavenCentral()
        maven { setUrl("https://oss.sonatype.org/content/repositories/snapshots/") }
        gradlePluginPortal()
    }
}

rootProject.name = "sudoscan"

include("sudoscan-core", "sudoscan-deskapp")

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
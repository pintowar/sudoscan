buildscript {
    repositories {
        mavenLocal()
        mavenCentral()
    }

    dependencies {
        classpath("org.asciidoctor:asciidoctorj-diagram:2.2.1")
    }
}

plugins {
    id("org.jbake.site") version "5.5.0"
    id("org.ajoberstar.git-publish") version "3.0.0"
}

jbake {
    srcDirName = "src/jbake"
    destDirName = "jbake"
    clearCache = true
}

gitPublish {
    repoUri.set("git@github.com:pintowar/sudoscan.git")

    branch.set("gh-pages")

    contents {
        from("build/jbake")
    }

    commitMessage.set("Publishing a new page")
}
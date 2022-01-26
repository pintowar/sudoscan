plugins {
    id("org.jbake.site")
    id("org.ajoberstar.git-publish")
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
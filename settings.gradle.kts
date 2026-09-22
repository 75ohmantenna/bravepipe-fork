/*
 * SPDX-FileCopyrightText: 2025 NewPipe e.V. <https://newpipe-ev.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}
rootProject.name = "org.seventyfiveohmantenna.pvcpipe"

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven(url = "https://jitpack.io")
        maven(url = "https://repo.clojars.org")
    }
}
include(":app")

includeBuild("pvcpipe-extractor") {
    dependencySubstitution {
        substitute(module("com.github.75ohmantenna:pvcpipe-extractor"))
            .using(project(":extractor"))
    }
}

//includeBuild("../logcat-toolkit") {
//    dependencySubstitution {
//        substitute(module("com.github.evermind-zz:logcat-toolkit"))
//            .using(project(":"))
//    }
//}

//includeBuild("../challengeFloatsAway") {
//    dependencySubstitution {
//        substitute(module("com.github.evermind-zz:challengeFloatsAway"))
//            .using(project(":library"))
//    }
//}

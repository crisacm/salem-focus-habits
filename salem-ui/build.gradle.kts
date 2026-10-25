/*
 * Copyright (C) 2016-2025 Álinson Santos Xavier <git@axavier.org>
 *
 * This file is part of Loop Habit Tracker.
 *
 * Loop Habit Tracker is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Loop Habit Tracker is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program. If not, see <http://www.gnu.org/licenses/>.
 */

plugins {
    kotlin("multiplatform")
    alias(libs.plugins.agp.library)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.ktlint.plugin)
}

kotlin {
    // The desktop target only exists to run the rendering tests on the JVM (Skia); no desktop app ships
    jvm("desktop")
    androidTarget()
    iosArm64()
    iosSimulatorArm64()
    // The iOS app links this framework
    targets.withType<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget>().configureEach {
        binaries.framework {
            baseName = "SalemUi"
            isStatic = true
        }
    }
    // sqliter (iOS database, through salem-core) needs the system SQLite library when a native binary is linked
    targets.withType<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget>().configureEach {
        binaries.all { linkerOpts("-lsqlite3") }
    }
    jvmToolchain(17)

    sourceSets {
        val desktopTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(kotlin("test-junit"))
                implementation(compose.desktop.currentOs)
                @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
                implementation(compose.uiTest)
                implementation(libs.kotlinx.coroutines.test)
            }
        }

        val commonMain by getting {
            dependencies {
                api(project(":salem-core"))
                implementation(libs.compose.mp.runtime)
                implementation(libs.compose.mp.foundation)
                implementation(libs.compose.mp.ui)
                implementation(libs.compose.mp.resources)
            }
        }
    }
}

android {
    namespace = "io.github.crisacm.salem.ui"
    compileSdk = 36

    defaultConfig {
        minSdk = 28
    }

    lint {
        abortOnError = false
    }
}

compose.resources {
    packageOfResClass = "io.github.crisacm.salem.ui.resources"
    generateResClass = always
}

// Compose Multiplatform writes Kotlin files into build/generated (Res.kt, the resource accessors) that do not follow
// ktlint, and the plugin adds them to the source sets of its check and format tasks (excludes and .editorconfig globs
// do not reach them). Limit each task to the files under src/<source set>. The check fails if the plugin renames its
// tasks, so the check can never pass because it silently looked at nothing
tasks.withType<org.jlleitschuh.gradle.ktlint.tasks.BaseKtLintCheckTask>().configureEach {
    // The tasks for the Gradle scripts have no generated files
    if (name.endsWith("KotlinScripts")) return@configureEach
    val prefix = listOf("runKtlintCheckOver", "runKtlintFormatOver").firstOrNull { name.startsWith(it) }
    if (prefix == null || !name.endsWith("SourceSet")) {
        throw GradleException("Unexpected ktlint task name '$name': update the filter of the sources in salem-ui/build.gradle.kts")
    }
    val sourceSet = name.removePrefix(prefix).removeSuffix("SourceSet").replaceFirstChar { it.lowercase() }
    setSource(fileTree("src/$sourceSet") { include("**/*.kt") })
}

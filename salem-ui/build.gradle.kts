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
    alias(libs.plugins.mokkery)
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
                implementation(libs.compose.mp.material3)
                implementation(libs.compose.mp.icons.core)
                implementation(libs.compose.mp.backhandler)
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

mokkery {
    defaultMockMode.set(dev.mokkery.MockMode.autofill)
    stubs.allowClassInheritance.set(true)
    stubs.allowConcreteClassInstantiation.set(true)
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

// The strings of the app live in salem-android (Crowdin writes them there and the Android Views, notifications and
// widgets need R.string). Copy them into the Compose resources of this module at build time; nothing is committed.
// The copy adapts what Compose resources read differently from Android:
// - the escapes \' and \" are plain characters, and the placeholders %s, %d and %1$d become %1$s, %2$s...
//   (Compose substitutes only the positional form);
// - Android keeps legacy language codes in its folders, Compose resources match the codes of java.util.Locale and
//   NSLocale: in -> id, iw -> he, and Norwegian is both nb and no (the JVM reports no, iOS reports nb).
// Items of a string-array that reference other strings (@string/...) are not resolved by Compose resources.
// The fonts of the core are copied too (one copy): fontawesome, notosans_regular and notosans_bold
val sharedStringsDir = layout.buildDirectory.dir("generated/sharedStrings")
val syncSharedStrings by tasks.registering(Sync::class) {
    val placeholder = Regex("%(\\d+\\$)?[sd]")
    fun CopySpec.adaptStrings() {
        filter { line ->
            var next = 0
            line.replace("\\'", "'").replace("\\\"", "\"").replace(placeholder) { match ->
                val position = match.groupValues[1]
                if (position.isNotEmpty()) "%${position}s" else "%${++next}\$s"
            }
        }
    }
    fun renamed(folder: String, language: String, to: String) = folder.replace("values-$language", "values-$to")

    from("../salem-android/src/main/res") {
        include("values*/strings.xml")
        eachFile {
            val folder = relativePath.parent.lastName
            val target = when {
                folder.startsWith("values-in-") -> renamed(folder, "in", "id")
                folder.startsWith("values-iw-") -> renamed(folder, "iw", "he")
                folder.startsWith("values-no-") -> renamed(folder, "no", "nb")
                else -> folder
            }
            if (target != folder) relativePath = RelativePath(true, target, name)
        }
        adaptStrings()
        includeEmptyDirs = false
    }
    from("../salem-android/src/main/res") {
        include("values-no-*/strings.xml")
        adaptStrings()
        includeEmptyDirs = false
    }
    from("../salem-core/assets/main/fonts") {
        include("*.ttf")
        into("font")
        rename { it.replace("-", "_").lowercase() }
    }
    into(sharedStringsDir.map { it.dir("composeResources") })
}

compose.resources {
    customDirectory("commonMain", syncSharedStrings.map { sharedStringsDir.get().dir("composeResources") })
}

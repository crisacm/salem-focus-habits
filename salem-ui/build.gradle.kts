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
                implementation(libs.navigationevent)
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
// The fonts of the core are copied too (one copy): fontawesome, notosans_regular and notosans_bold. The images of the
// intro and the About screen (intro_icon_*.png) come from the drawables of salem-android
val sharedStringsDir = layout.buildDirectory.dir("generated/sharedStrings")
val syncSharedStrings by tasks.registering(Sync::class) {
    filteringCharset = "UTF-8"
    val legacyFolder = Regex("^values-(in|iw|no)(?=-|$)")
    val legacyLanguages = mapOf("in" to "id", "iw" to "he", "no" to "nb")

    from("../salem-android/src/main/res") {
        include("values*/strings.xml")
        eachFile {
            val folder = relativePath.parent.lastName
            val target = legacyFolder.replace(folder) { "values-" + legacyLanguages.getValue(it.groupValues[1]) }
            if (target != folder) relativePath = RelativePath(true, target, name)
        }
        includeEmptyDirs = false
    }
    // Norwegian is both nb (iOS) and no (the JVM): keep the original folder too
    from("../salem-android/src/main/res") {
        include("values-no/strings.xml", "values-no-*/strings.xml")
        includeEmptyDirs = false
    }
    // The images of the intro and the About screen
    from("../salem-android/src/main/res/drawable") {
        include("intro_icon_*.png")
        into("drawable")
    }
    from("../salem-core/assets/main/fonts") {
        include("*.ttf")
        into("font")
        rename { it.replace("-", "_").lowercase() }
    }
    into(sharedStringsDir.map { it.dir("composeResources") })

    // Adapt the copied strings once they are in place, whole file at a time, element by element
    doLast {
        val element = Regex("(<(?:string|item)\\b[^>]*>)(.*?)(</(?:string|item)>)", RegexOption.DOT_MATCHES_ALL)
        // Anything that looks like a printf specification: %s, %d, %1$s, %.1f, %5d, %x, %%
        val specification = Regex("%(?:\\d+\\$)?[-#+0,(]*\\d*(?:\\.\\d+)?[a-zA-Z%]")
        val supported = Regex("%(\\d+\\$)?[sd]")
        fileTree(destinationDir).matching { include("**/strings.xml") }.forEach { file ->
            val adapted = element.replace(file.readText(Charsets.UTF_8)) { match ->
                var next = 0
                val body = match.groupValues[2].replace("\\'", "'").replace("\\\"", "\"").replace(specification) { spec ->
                    val parts = supported.matchEntire(spec.value)
                        ?: throw GradleException("Unsupported format ${spec.value} in ${file.parentFile.name}/${file.name}: ${match.value}")
                    // Compose substitutes only the positional form
                    val position = parts.groupValues[1]
                    if (position.isNotEmpty()) "%${position}s" else "%${++next}\$s"
                }
                match.groupValues[1] + body + match.groupValues[3]
            }
            file.writeText(adapted, Charsets.UTF_8)
        }
    }
}

compose.resources {
    customDirectory("commonMain", syncSharedStrings.map { sharedStringsDir.get().dir("composeResources") })
}

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
    alias(libs.plugins.ktlint.plugin)
}

// What the iOS extensions (the widget and the shortcuts) link instead of the whole app: the core without Compose, with a
// small API for Swift. The app itself does not link it (it already has the core inside SalemUi)
kotlin {
    iosArm64()
    iosSimulatorArm64()
    // The tests run on the JVM, over the same database code the apps use
    jvm()
    targets.withType<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget>().configureEach {
        binaries.framework {
            baseName = "SalemExtras"
            isStatic = true
        }
        // sqliter (iOS database, through salem-core) needs the system SQLite library when a native binary is linked
        binaries.all { linkerOpts("-lsqlite3") }
    }
    jvmToolchain(17)

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":salem-core"))
            }
        }

        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(kotlin("test-junit"))
                implementation(libs.sqlite.jdbc)
                implementation(libs.kotlinx.coroutines.core)
            }
        }
    }
}

// The tests read the migrations of the core from its assets folder
tasks.withType<Test>().configureEach {
    workingDir = project(":salem-core").projectDir
}

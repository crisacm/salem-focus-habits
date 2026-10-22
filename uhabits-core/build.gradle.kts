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

import com.android.build.api.variant.HasHostTestsBuilder
import com.android.build.api.variant.HostTestBuilder
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    kotlin("multiplatform")
    alias(libs.plugins.agp.library)
    alias(libs.plugins.ktlint.plugin)
    alias(libs.plugins.mokkery)
}

kotlin {
    jvm()
    androidTarget()
    jvmToolchain(17)

    // Code that only needs java.* APIs and is shared by the jvm and android targets
    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    applyDefaultHierarchyTemplate {
        common {
            group("jvmAndroid") {
                withJvm()
                withAndroidTarget()
            }
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(kotlin("stdlib-common"))
                implementation(libs.kotlinx.coroutines.core)
                compileOnly(libs.kotlin.inject.runtime)
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
                implementation(libs.kotlinx.coroutines.test)
            }
        }

        val jvmMain by getting {
            dependencies {
                implementation(kotlin("stdlib-jdk8"))
                implementation(libs.guava)
                implementation(libs.kotlinx.coroutines.core.jvm)
                implementation(libs.annotation)
                implementation(libs.jsr305)
                implementation(libs.opencsv)
                implementation(libs.commons.codec)
            }
        }

        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(kotlin("test-junit"))
                implementation(libs.sqlite.jdbc)
                implementation(libs.hamcrest)
                implementation(libs.commons.io)
                implementation(libs.junit.jupiter)
            }
        }
    }
}

android {
    namespace = "org.isoron.uhabits.core"
    compileSdk = 36

    defaultConfig {
        minSdk = 28
    }

    lint {
        abortOnError = false
    }
}

androidComponents {
    // Core tests run on the jvm target: they need java.awt and JDBC, which Android does not have
    beforeVariants { variant ->
        (variant as HasHostTestsBuilder).hostTests[HostTestBuilder.UNIT_TEST_TYPE]?.enable = false
    }
}

mokkery {
    defaultMockMode.set(dev.mokkery.MockMode.autofill)
    stubs.allowConcreteClassInstantiation.set(true)
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile> {
    compilerOptions {
        freeCompilerArgs.add("-jvm-default=enable")
    }
}

tasks.withType<ProcessResources> {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}

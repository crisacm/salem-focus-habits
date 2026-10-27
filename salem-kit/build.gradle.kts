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

// The iOS app links this framework instead of SalemUi: the core without Compose, with a Swift-facing session.
// The widget and the shortcuts keep SalemExtras (another process). One process must not load both.
kotlin {
    iosArm64()
    iosSimulatorArm64()
    jvm()
    targets.withType<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget>().configureEach {
        binaries.framework {
            baseName = "SalemKit"
            isStatic = true
        }
        binaries.all { linkerOpts("-lsqlite3") }
    }
    jvmToolchain(17)

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":salem-core"))
                implementation(libs.kotlinx.coroutines.core)
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

tasks.withType<Test>().configureEach {
    workingDir = project(":salem-core").projectDir
}

// The strings of the app live in salem-android. Write the iOS string catalog from those files so SwiftUI
// does not keep a second translation. Xcode runs this before copying resources.
tasks.register("syncIosStrings") {
    val res = rootProject.file("salem-android/src/main/res")
    val output = rootProject.file("iosApp/iosApp/Localizable.xcstrings")
    inputs.dir(res)
    outputs.file(output)
    doLast {
        val stringTag = Regex("""<string\s+name="([^"]+)"[^>]*>(.*?)</string>""", RegexOption.DOT_MATCHES_ALL)
        val legacy = mapOf("in" to "id", "iw" to "he", "no" to "nb")
        fun localeOf(folder: String): String {
            if (folder == "values") return "en"
            var code = folder.removePrefix("values-")
            if (code.startsWith("b+")) {
                return code.removePrefix("b+").replace('+', '-')
            }
            val parts = code.split("-")
            val language = legacy[parts[0]] ?: parts[0]
            val region = parts.getOrNull(1)?.removePrefix("r")
            return if (region != null) "$language-$region" else language
        }
        fun unescape(raw: String): String {
            val text = raw
                .replace("\\'", "'")
                .replace("\\\"", "\"")
                .replace("\\n", "\n")
                .replace("\\t", "\t")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&amp;", "&")
                .replace("&apos;", "'")
                .replace("&quot;", "\"")
            return text.replace(Regex("%(\\d+\\$)?s"), "%$1@")
        }
        class CatalogEntry(
            val strings: MutableMap<String, String> = linkedMapOf(),
            val plurals: MutableMap<String, MutableMap<String, String>> = linkedMapOf()
        )
        val pluralTag = Regex("""<plurals\s+name="([^"]+)"[^>]*>(.*?)</plurals>""", RegexOption.DOT_MATCHES_ALL)
        val pluralItemTag = Regex("""<item\s+quantity="([^"]+)"[^>]*>(.*?)</item>""", RegexOption.DOT_MATCHES_ALL)
        val pluralOrder = listOf("zero", "one", "two", "few", "many", "other")
        val numberInString = Regex("""%(\d+\$)?[df@]""")
        fun habitsSingularKey(name: String): String? {
            val marker = "_habits"
            val index = name.indexOf(marker)
            if (index < 0) return null
            return name.substring(0, index) + "_habit" + name.substring(index + marker.length)
        }
        val catalog = linkedMapOf<String, CatalogEntry>()
        res.walkTopDown().filter { it.name == "strings.xml" }.forEach { file ->
            val folder = file.parentFile.name
            if (!folder.startsWith("values")) return@forEach
            val locale = localeOf(folder)
            val text = file.readText()
            stringTag.findAll(text).forEach { match ->
                catalog.getOrPut(match.groupValues[1]) { CatalogEntry() }.strings[locale] =
                    unescape(match.groupValues[2])
            }
            pluralTag.findAll(text).forEach { match ->
                val name = match.groupValues[1]
                val byQuantity = linkedMapOf<String, String>()
                pluralItemTag.findAll(match.groupValues[2]).forEach { item ->
                    byQuantity[item.groupValues[1]] = unescape(item.groupValues[2])
                }
                if (byQuantity.values.any { numberInString.containsMatchIn(it) }) {
                    val entry = catalog.getOrPut(name) { CatalogEntry() }
                    entry.plurals.getOrPut(locale) { linkedMapOf() }.putAll(byQuantity)
                } else {
                    val one = byQuantity["one"]
                    val other = byQuantity["other"] ?: byQuantity.values.lastOrNull()
                    val singularKey = habitsSingularKey(name)
                    if (singularKey != null && one != null) {
                        catalog.getOrPut(singularKey) { CatalogEntry() }.strings[locale] = one
                    }
                    if (other != null) {
                        catalog.getOrPut(name) { CatalogEntry() }.strings[locale] = other
                    }
                }
            }
        }
        fun json(value: String) = buildString {
            append('"')
            value.forEach { char ->
                when (char) {
                    '\\' -> append("\\\\")
                    '"' -> append("\\\"")
                    '\n' -> append("\\n")
                    '\r' -> append("\\r")
                    '\t' -> append("\\t")
                    else -> append(char)
                }
            }
            append('"')
        }
        val body = buildString {
            appendLine("{")
            appendLine("  \"sourceLanguage\" : \"en\",")
            appendLine("  \"strings\" : {")
            catalog.entries.forEachIndexed { index, (key, entry) ->
                val localeKeys = (entry.strings.keys + entry.plurals.keys).toSortedSet()
                appendLine("    ${json(key)} : {")
                appendLine("      \"localizations\" : {")
                localeKeys.forEachIndexed { localeIndex, locale ->
                    val comma = if (localeIndex == localeKeys.size - 1) "" else ","
                    val pluralForms = entry.plurals[locale]
                    if (pluralForms != null) {
                        val ordered = pluralForms.entries.sortedBy {
                            pluralOrder.indexOf(it.key).let { rank -> if (rank < 0) 99 else rank }
                        }
                        appendLine("        ${json(locale)} : {")
                        appendLine("          \"variations\" : {")
                        appendLine("            \"plural\" : {")
                        ordered.forEachIndexed { qi, (quantity, value) ->
                            val qComma = if (qi == ordered.size - 1) "" else ","
                            appendLine(
                                "              ${json(quantity)} : { \"stringUnit\" : { \"state\" : \"translated\", \"value\" : ${json(value)} } }$qComma"
                            )
                        }
                        appendLine("            }")
                        appendLine("          }")
                        appendLine("        }$comma")
                    } else {
                        val value = entry.strings[locale]!!
                        appendLine(
                            "        ${json(locale)} : { \"stringUnit\" : { \"state\" : \"translated\", \"value\" : ${json(value)} } }$comma"
                        )
                    }
                }
                appendLine("      }")
                appendLine("    }${if (index == catalog.size - 1) "" else ","}")
            }
            appendLine("  },")
            appendLine("  \"version\" : \"1.0\"")
            appendLine("}")
        }
        output.parentFile.mkdirs()
        output.writeText(body)
    }
}

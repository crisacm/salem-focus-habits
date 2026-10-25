import com.opencsv.CSVReaderHeaderAware
import java.io.FileReader

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        "classpath"(group = "com.opencsv", name = "opencsv", version = "5.4")
    }
}

task("updateTranslators") {
    doLast {
        fun updateTranslatorList() {
            val translators = mutableMapOf<String, MutableList<String>>()

            // Classic
            var csv = CSVReaderHeaderAware(FileReader("translators-classic.csv"))
            while (true) {
                val entry = csv.readMap() ?: break
                val lang = entry["Language"]!!
                val name = entry["Name"]!!
                if (!translators.containsKey(lang)) {
                    translators[lang] = mutableListOf()
                }
                translators[lang]!!.add(name)
                translators[lang]!!.sort()
            }

            // Crowdin
            val languageMap = mapOf(
                "Arabic" to "العَرَبِية\u200E",
                "Basque" to "Euskara",
                "Bulgarian" to "Български",
                "Catalan" to "Català",
                "Chinese Simplified" to "中文",
                "Chinese Traditional" to "中文",
                "Croatian" to "Hrvatski",
                "Czech" to "Čeština",
                "Danish" to "Dansk",
                "Dutch" to "Nederlands",
                "Finnish" to "Suomen kieli",
                "French" to "Français",
                "German" to "Deutsch",
                "Greek" to "Ελληνικά",
                "Hebrew" to "עברית\u200E",
                "Hungarian" to "Magyar",
                "Indonesian" to "Bahasa Indonesia",
                "Italian" to "Italiano",
                "Japanese" to "日本語",
                "Korean" to "한국어",
                "Persian" to "العَرَبِية\u200E",
                "Polish" to "Polski",
                "Portuguese" to "Português",
                "Portuguese, Brazilian" to "Português",
                "Romanian" to "Română",
                "Russian" to "Русский",
                "Serbian (Cyrillic)" to "српски",
                "Serbian (Latin)" to "српски",
                "Spanish" to "Español",
                "Swedish" to "Svenska",
                "Tamil" to "தமிழ்\u200E",
                "Telegu" to "తెలుగు",
                "Turkish" to "Türkçe",
                "Ukrainian" to "Українська",
                "Vietnamese" to "Tiếng Việt"
            )
            csv = CSVReaderHeaderAware(FileReader("translators-crowdin.csv"))
            while (true) {
                val entry = csv.readMap() ?: break
                var lang = entry["Languages"]!!.split(";")[0]
                if (languageMap.containsKey(lang)) {
                    lang = languageMap[lang]!!
                }
                val name = entry["Name"]!!.replace(Regex(" *\\(.*\\) *"), "")
                if (name.contains("REMOVED")) continue
                if (entry["Winning (Words)"]!!.toInt() < 10 &&
                    entry["Translated (Words)"]!!.toInt() < 100 &&
                    entry["Approved (Words)"]!!.toInt() <= 0
                ) {
                    continue
                }
                if (!translators.containsKey(lang)) {
                    translators[lang] = mutableListOf()
                }
                if (translators[lang]!!.contains(name)) continue
                translators[lang]!!.add(name)
                translators[lang]!!.sort()
            }

            fun literal(text: String) = buildString {
                append('"')
                for (c in text) {
                    when {
                        c == '"' || c == '\\' || c == '$' -> append('\\').append(c)
                        c.code < 0x20 || c == '\u200E' -> append("\\u%04X".format(c.code))
                        else -> append(c)
                    }
                }
                append('"')
            }
            // The license header of every source file of the project
            val header = "/*\n * Copyright (C) 2016-2025 Álinson Santos Xavier <git@axavier.org>\n *\n * This file is part of Loop Habit Tracker.\n *\n * Loop Habit Tracker is free software: you can redistribute it and/or modify\n * it under the terms of the GNU General Public License as published by the\n * Free Software Foundation, either version 3 of the License, or (at your\n * option) any later version.\n *\n * Loop Habit Tracker is distributed in the hope that it will be useful, but\n * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY\n * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for\n * more details.\n *\n * You should have received a copy of the GNU General Public License along\n * with this program. If not, see <http://www.gnu.org/licenses/>.\n */"
            val out = StringBuilder(header).append("\n\n")
            out.append("package io.github.crisacm.salem.ui.about\n\n")
            out.append("// This list is automatically generated by the updateTranslators Gradle task, do not edit manually.\n")
            out.append("val aboutTranslators = listOf(\n")
            out.append(
                translators.toSortedMap().entries.joinToString(",\n") { (lang, names) ->
                    "    TranslatorLanguage(\n        ${literal(lang)},\n        listOf(\n" +
                        names.joinToString(",\n") { "            ${literal(it)}" } +
                        "\n        )\n    )"
                }
            )
            out.append("\n)\n")
            val newContent = out.toString()
            val path = "salem-ui/src/commonMain/kotlin/io/github/crisacm/salem/ui/about/AboutTranslators.kt"
            val target = file(path)
            if (!target.exists() || target.readText() != newContent) {
                target.writeText(newContent)
            }
        }
        updateTranslatorList()
    }
}

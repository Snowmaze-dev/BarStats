package ru.snowmaze.barstats

class SectionsPrinter {

    fun printSections(section: Section) {
        printSectionsRecursive(Section(subSubsections = listOf(section)), "")
    }

    private fun printSectionsRecursive(section: Section, whitespace: String = "") {
        for (value in section.values ?: emptyList()) {
            if (value is SectionValue.KeyValueSectionValue) {
                value.value ?: continue
            }
            println(buildString {
                append(whitespace)
                if (value is SectionValue.KeyValueSectionValue) {
                    append(value.key, ": ", value.value.toString())
                } else if (value is SectionValue.StringSectionValue) {
                    append(value.value)
                }
            })
        }
        if (section.subSubsections.isNullOrEmpty()) return
        for (subSection in section.subSubsections) {
            if (subSection.subSubsections.isNullOrEmpty() && subSection.values.isNullOrEmpty()) continue
            println(buildString {
                append(whitespace, subSection.name, ":")
            })
            printSectionsRecursive(subSection, "$whitespace ")
        }
    }
}
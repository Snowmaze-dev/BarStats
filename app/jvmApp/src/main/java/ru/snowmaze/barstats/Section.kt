package ru.snowmaze.barstats

class Section(
    val name: String? = null,
    val values: List<SectionValue>? = null,
    val subSubsections: List<Section>? = null
)

sealed class SectionValue {

    class KeyValueSectionValue(val key: String, val value: Any?) : SectionValue()

    class StringSectionValue(val value: String) : SectionValue()
}
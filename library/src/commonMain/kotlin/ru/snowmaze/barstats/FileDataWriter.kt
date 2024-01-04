package ru.snowmaze.barstats

import kotlinx.serialization.KSerializer
import kotlinx.serialization.StringFormat
import kotlinx.serialization.serializer
import okio.Path
import kotlin.reflect.KType
import kotlin.reflect.typeOf

interface FileDataWriter {

    fun <T> parse(type: KType, file: Path): T

    fun <T> write(value: T, type: KType, file: Path)
}

open class StringFormatFileDataWriter(private val stringFormat: StringFormat) : FileDataWriter {

    override fun <T> parse(type: KType, file: Path): T {
        return stringFormat.decodeFromString(getSerializer(type), getText(file))
    }

    override fun <T> write(value: T, type: KType, file: Path) {
        getSystemFileSystem().write(file, false) {
            writeUtf8(stringFormat.encodeToString(getSerializer(type), value))
        }
    }

    protected open fun getText(file: Path) = file.defaultReadText()

    protected fun <T> getSerializer(kType: KType): KSerializer<T> {
        return stringFormat.serializersModule.serializer(kType) as KSerializer<T>
    }
}

inline fun <reified T> FileDataWriter.write(value: T, file: Path) = write(value, typeOf<T>(), file)

inline fun <reified T> FileDataWriter.parse(file: Path): T = parse(typeOf<T>(), file)

private fun Path.defaultReadText() = getSystemFileSystem().read(this) {
    readUtf8()
}
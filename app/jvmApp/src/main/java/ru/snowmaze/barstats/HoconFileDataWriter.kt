package ru.snowmaze.barstats

import com.typesafe.config.ConfigFactory
import kotlinx.serialization.KSerializer
import kotlinx.serialization.hocon.Hocon
import kotlinx.serialization.serializer
import okio.Path
import kotlin.reflect.KType

class HoconFileDataWriter(private val hocon: Hocon = Hocon) : FileDataWriter {

    override fun <T> parse(type: KType, file: Path): T {
        val conf = ConfigFactory.parseFile(file.toFile())
        return hocon.decodeFromConfig(getSerializer(type), conf)
    }

    override fun <T> write(value: T, type: KType, file: Path) {
        throw UnsupportedOperationException()
    }

    protected fun <T> getSerializer(kType: KType): KSerializer<T> {
        return hocon.serializersModule.serializer(kType) as KSerializer<T>
    }
}
package ru.snowmaze.barstats.models.external

import kotlinx.serialization.Serializable

@Serializable
class CachedPlayerModel(val id: Long, val username: String, val countryCode: String? = null)
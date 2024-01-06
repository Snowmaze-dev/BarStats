package ru.snowmaze.barstats.usecases

import ru.snowmaze.barstats.models.external.StartPos

const val FRONT = "front"
const val ECO = "eco"
const val AIR = "air"
const val SEA = "sea"

interface PositionsDeterminer {

    val mapName: String

    fun determinePosition(startPos: StartPos): String?
}

// TODO
class IsthmusPositionsDeterminer : PositionsDeterminer {
    override val mapName = "isthmus"

    val southEco = StartPos(11550f, 265f, 1767f)
    val southAir = StartPos(10066f, 272.09583f, 506f)
    val northEco = StartPos(989f, 261f, 10555f)
    val northAir = StartPos(2206f, 275f, 11821f)

    override fun determinePosition(startPos: StartPos): String? {
        return if (startPos.y > 200f) "backline"
        else "other"
    }
}

class FrontBasedMapPositionsDeterminer(override val mapName: String) : PositionsDeterminer {
    override fun determinePosition(startPos: StartPos) = FRONT
}
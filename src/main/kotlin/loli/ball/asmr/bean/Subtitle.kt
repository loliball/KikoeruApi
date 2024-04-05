package loli.ball.asmr.bean

import kotlinx.serialization.Serializable

typealias SubtitleList = List<Subtitle>

@Serializable
data class Subtitle(
    val title: String,
    val subPath: List<String>,
    val hash: String,
    val ext: String,
    val duration: Float,
    val confidence: Double
)

@Serializable
data class SubtitleQuery(
    val intProductID: Long,
    val mediaDuration: Float,
    val mediaFileName: String
)

package loli.ball.asmr.bean

import kotlinx.serialization.Serializable

@Serializable
data class LrcResult(
    val result: Boolean? = true,
    val message: String? = null,
    val hash: String,
    val lyricExtension: String? = null,
    val lrc: List<LrcItem>? = null
)

@Serializable
data class LrcItem(
    val time: Int,
    val text: String
)
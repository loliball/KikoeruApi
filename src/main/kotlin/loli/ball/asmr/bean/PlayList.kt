package loli.ball.asmr.bean

import kotlinx.serialization.Serializable

@Serializable
data class PlayLists(
    val pagination: Pagination,
    val playlists: List<PlayList>
)

@Serializable
data class PlayList(
    val id: String,
    val user_name: String,
    val privacy: Int,
    val locale: String,
    val playback_count: Int,
    val name: String,
    val description: String,
    val created_at: String,
    val updated_at: String,
    val works_count: Int,

    val exist: Boolean = false,

    val latestWorkID: Long = 0,
    val mainCoverUrl: String? = null
) {
    val isSysDefault get() = name == __SYS_PLAYLIST_LIKED || name == __SYS_PLAYLIST_MARKED
}

const val __SYS_PLAYLIST_MARKED = "__SYS_PLAYLIST_MARKED"
const val __SYS_PLAYLIST_LIKED = "__SYS_PLAYLIST_LIKED"
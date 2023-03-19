package loli.ball.asmr.bean

import kotlinx.serialization.Serializable
import loli.ball.asmr.AsmrOneApi

@Serializable
data class Works(
    val pagination: Pagination,
    val works: List<Work>
)

@Serializable
data class Pagination(
    val currentPage: Int,
    val pageSize: Int,
    val totalCount: Int
)

@Serializable
data class Work(
    val circle: Circle,
    val circle_id: Int,
    val create_date: String = "",
    val dl_count: Int,
    val has_subtitle: Boolean = false,
    val id: Int,
    val mainCoverUrl: String? = null,
    val samCoverUrl: String? = null,
    val thumbnailCoverUrl: String? = null,
    val name: String,
    val nsfw: Boolean,
    val price: Int,
    val progress: String? = null,
    val rank: List<Rank>?,
    val rate_average_2dp: Double,
    val rate_count: Int,
    val rate_count_detail: List<RateCountDetail>,
    val release: String,
    val review_count: Int,
    val review_text: String? = null,
    val tags: List<Tag>,
    val title: String,
    val userRating: Int? = null,
    val vas: List<Va>,
    val user_name: String? = null,
    val updated_at: String? = null
) {

    // need token!
    val cover = mainCoverUrl ?: "${AsmrOneApi.ASMR_BASE_URL}/api/cover/$id"

    @Serializable
    data class Rank(
        val category: String,
        val rank: Int,
        val rank_date: String,
        val term: String
    )

    @Serializable
    data class RateCountDetail(
        val count: Int,
        val ratio: Int,
        val review_point: Int
    )

}

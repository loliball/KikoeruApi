package loli.ball.asmr.bean

import kotlinx.serialization.Serializable

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
    val create_date: String,
    val dl_count: Int,
    val has_subtitle: Boolean,
    val id: Int,
    val mainCoverUrl: String,
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
    val samCoverUrl: String,
    val tags: List<Tag>,
    val thumbnailCoverUrl: String,
    val title: String,
    val userRating: Int? = null,
    val vas: List<Va>
) {

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

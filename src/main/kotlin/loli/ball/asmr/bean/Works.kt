package loli.ball.asmr.bean

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.*
import loli.ball.asmr.AsmrOneApi

@Serializable
data class Works(
    val pagination: Pagination,
    val works: List<Work>
)

@Serializable(with = PageSerializer::class)
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
    @Serializable(with = MixedSerializer::class)
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
    val updated_at: String? = null,
    val age_category_string: String? = null,
    val original_workno: String? = null,
    val other_language_editions_in_db: List<OtherWork>? = null,
    val work_attributes: String? = null
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

    enum class AgeCategory {
        adult, general, r15
    }

    @Serializable
    data class OtherWork(
        val id: Int,
        val lang: String,
        val title: String,
        val is_original: Boolean
    )

}

class MixedSerializer : KSerializer<Boolean> {

    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("MixedSerializer")

    override fun deserialize(decoder: Decoder): Boolean {
        val element = (decoder as JsonDecoder).decodeJsonElement().jsonPrimitive
        return element.booleanOrNull ?: (element.intOrNull != 0)
    }

    override fun serialize(encoder: Encoder, value: Boolean) {
        encoder.encodeBoolean(value)
    }

}

class PageSerializer : KSerializer<Pagination> {

    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("PPP")

    override fun deserialize(decoder: Decoder): Pagination {
        val obj = (decoder as JsonDecoder).decodeJsonElement().jsonObject
        val page = (obj["currentPage"] ?: obj["page"])!!.jsonPrimitive.int
        val pageSize = obj["pageSize"]!!.jsonPrimitive.int
        val totalCount = obj["totalCount"]!!.jsonPrimitive.int
        return Pagination(page, pageSize, totalCount)
    }

    override fun serialize(encoder: Encoder, value: Pagination) {
        encoder.encodeSerializableValue(JsonObject.serializer(), buildJsonObject {
            put("currentPage", value.currentPage)
            put("pageSize", value.pageSize)
            put("totalCount", value.totalCount)
        })
    }

}
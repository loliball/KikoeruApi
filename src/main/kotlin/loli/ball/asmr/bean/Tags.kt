package loli.ball.asmr.bean

import kotlinx.serialization.Serializable

interface QueryAble {
    fun query(): String
}

@Serializable
data class Circle(
    val count: Int? = null,
    val id: Int,
    val name: String
) : QueryAble {
    override fun query() = "circles/$id"
}

@Serializable
data class Tag(
    val count: Int? = null,
    val id: Int?,
    val name: String?,
    val i18n: I18n?
): QueryAble {

    @Serializable
    data class I18n(
        val `en-us`: Language? = null,
        val `ja-jp`: Language? = null,
        val `zh-cn`: Language? = null
    )

    @Serializable
    data class Language(
        val name: String?
    )

    override fun query() = "tags/$id"

}

@Serializable
data class Va(
    val count: Int? = null,
    val id: String,
    val name: String
): QueryAble {

    override fun query() = "vas/$id"

}
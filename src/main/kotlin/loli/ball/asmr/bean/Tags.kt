package loli.ball.asmr.bean

import kotlinx.serialization.Serializable

interface QueryAble {
    fun query(): String
    fun name(): String
}

@Serializable
data class Circle(
    val count: Int? = null,
    val id: Int,
    val name: String
) : QueryAble {
    override fun query() = "circles/$id"
    override fun name() = name
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

    override fun name() = name.orEmpty()

}

@Serializable
data class Va(
    val count: Int? = null,
    val id: String,
    val name: String
): QueryAble {

    override fun query() = "vas/$id"

    override fun name() = name

}
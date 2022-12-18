@file:Suppress("unused")

package loli.ball.asmr

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import loli.ball.asmr.bean.*
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.net.URLEncoder


object AsmrOneApi {

    private const val ASMR_BASE_URL = "https://api.asmr.one"

    var client = OkHttpClient()

    private var json = Json {
        classDiscriminator = "type"
        serializersModule = SerializersModule {
            polymorphic(RemoteFile::class) {
                subclass(RemoteFile.Folder::class)
                subclass(RemoteFile.File.Audio::class)
                subclass(RemoteFile.File.Text::class)
                subclass(RemoteFile.File.Image::class)
                subclass(RemoteFile.File.Other::class)
            }
        }
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    fun guestLogin() = login("guest", "guest")

    fun login(name: String, pwd: String) = loginOrRegister("$ASMR_BASE_URL/api/auth/me", name, pwd)

    fun register(name: String, pwd: String) = loginOrRegister("$ASMR_BASE_URL/api/auth/reg", name, pwd)

    private fun loginOrRegister(url: String, username: String, password: String): Result<String> {
        val body = Json.encodeToString(
            mapOf(
                "name" to username,
                "password" to password
            )
        ).toRequestBody("application/json".toMediaTypeOrNull())
        val newCall = client.newCall(Request.Builder().url(url).post(body).build())
        return kotlin.runCatching {
            val response = newCall.execute()
//            check(response.code == 200) { "http code ${response.code}" }
            val resp = response.body!!.string()
            check(response.code == 200) { resp }
            val json = Json.parseToJsonElement(resp).jsonObject
            json["token"]?.jsonPrimitive?.content ?: error(resp)
        }
    }

    // 媒体库
    fun works(
        token: String,
        page: Int,
        order: WorksOrder = WorksOrder.create_date,
        sort: QuerySort = QuerySort.desc,
        seed: Int = (0..100).random(),
        subtitle: Boolean = false, // 是否有字幕
        extra: QueryAble? = null // 额外特殊的检索标签
    ): Result<Works> {
        val url0 = if (extra == null) {
            "$ASMR_BASE_URL/api/works"
        } else "$ASMR_BASE_URL/api/${extra.query()}/works"
        val url = url0.toHttpUrl().newBuilder().apply {
            addQueryParameter("order", order.name)
            addQueryParameter("sort", sort.name)
            addQueryParameter("page", page.toString())
            addQueryParameter("seed", seed.toString())
            addQueryParameter("subtitle", (if (subtitle) 1 else 0).toString())
        }.build().toString()
        return request(url, token)
    }

    // 搜索
    fun search(
        token: String,
        page: Int,
        keyword: String,
        order: WorksOrder = WorksOrder.create_date,
        sort: QuerySort = QuerySort.desc,
        seed: Int = (0..100).random(),
        subtitle: Boolean = false, // 是否有字幕
    ): Result<Works> {
        val url0 = "$ASMR_BASE_URL/api/search/${keyword.toUrlEncoded()}"
        val url = url0.toHttpUrl().newBuilder().apply {
            addQueryParameter("order", order.name)
            addQueryParameter("sort", sort.name)
            addQueryParameter("page", page.toString())
            addQueryParameter("seed", seed.toString())
            addQueryParameter("subtitle", (if (subtitle) 1 else 0).toString())
        }.build().toString()
        return request(url, token)
    }

    // 收藏页面
    fun review(
        token: String,
        page: Int,
        order: ReviewOrder = ReviewOrder.updated_at,
        sort: QuerySort = QuerySort.desc,
        filter: ListenState? = null
    ): Result<Works> {
        val url = "$ASMR_BASE_URL/api/review".toHttpUrl().newBuilder().apply {
            addQueryParameter("order", order.name)
            addQueryParameter("sort", sort.name)
            addQueryParameter("page", page.toString())
            if (filter != null) {
                addQueryParameter("filter", filter.toString())
            }
        }.build().toString()
        return request(url, token)
    }

    // 不带个人信息 RJ号直达
    fun workInfo(token: String, id: Int): Result<Work> =
        request("$ASMR_BASE_URL/api/workInfo/$id", token)

    // 带个人信息 RJ号直达
    fun work(token: String, id: Int): Result<Work> =
        request("$ASMR_BASE_URL/api/work/$id", token)

    // 详细目录 RJ号直达
    fun tracks(token: String, id: Int): Result<List<RemoteFile>> =
        request("$ASMR_BASE_URL/api/tracks/$id", token)

    // 社团
    fun circles(token: String): Result<List<Circle>> =
        request("$ASMR_BASE_URL/api/circles", token)

    // 标签
    fun tags(token: String): Result<List<Tag>> =
        request("$ASMR_BASE_URL/api/tags", token)

    // 声优
    fun vas(token: String): Result<List<Va>> =
        request("$ASMR_BASE_URL/api/vas", token)

    private inline fun <reified R> request(url: String, token: String): Result<R> {
        val request = Request.Builder()
            .url(url)
            .header("authorization", "Bearer $token")
            .build()
        return runCatching {
            val response = client.newCall(request).execute()
            val bodyString = response.body!!.string()
//            check(response.code == 200) { "http code ${response.code} $bodyString" }
            check(response.code == 200) { bodyString }
            json.decodeFromString(bodyString)
        }
    }

    fun editState(
        token: String,
        progress: ListenState? = null,
        rating: Int? = null,
        review_text: String? = null,
        user_name: String,
        work_id: Int
    ): Result<String> {
        if (progress == null && rating == null && review_text == null) {
            error("progress, rating, review_text all null")
        }
        val body = Json.encodeToString(
            JsonObject(
                mapOf(
                    "progress" to JsonPrimitive(progress?.name),
                    "rating" to JsonPrimitive(rating),
                    "review_text" to JsonPrimitive(review_text),
                    "user_name" to JsonPrimitive(user_name),
                    "work_id" to JsonPrimitive(work_id)
                ).filterValues { it != JsonNull }
            )
        ).toRequestBody("application/json".toMediaTypeOrNull())
        val starOnly = progress == null && rating != null && review_text == null
        val progressOnly = progress != null && rating == null && review_text == null
        val request = Request.Builder()
            .url("$ASMR_BASE_URL/api/review?starOnly=$starOnly&progressOnly=$progressOnly")
            .header("authorization", "Bearer $token")
            .put(body)
            .build()
        return runCatching {
            val response = client.newCall(request).execute()
            val string = response.body!!.string()
//            check(response.code == 200) { "http code ${response.code} $string" }
            string
        }
    }

    fun deleteMark(
        token: String,
        work_id: Int
    ): Result<String> {
        val request = Request.Builder()
            .url("$ASMR_BASE_URL/api/review?work_id=$work_id")
            .header("authorization", "Bearer $token")
            .delete()
            .build()
        return runCatching {
            val response = client.newCall(request).execute()
//            check(response.code == 200) { "http code ${response.code}" }
            response.body!!.string()
        }
    }

    private fun String.toUrlEncoded(): String = URLEncoder.encode(this, Charsets.UTF_8.displayName())

}


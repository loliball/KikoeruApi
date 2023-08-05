@file:Suppress("unused", "LocalVariableName", "PropertyName")

package loli.ball.asmr

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import loli.ball.asmr.bean.*
import okhttp3.CacheControl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.net.URLEncoder


object AsmrOneApi {

    var ASMR_BASE_URL = "https://api.asmr.one"

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

    fun health(domain: String = ASMR_BASE_URL, assertResult: String = "OK"): Result<String> {
        val url = "$domain/api/health"
        return kotlin.runCatching {
            val request = Request.Builder()
                .cacheControl(CacheControl.FORCE_NETWORK)
                .url(url)
                .build()
            client.newCall(request).execute().use {
                val result = it.body!!.string()
                check(it.code == 200 && result == assertResult) { result }
                result
            }
        }
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
        val newCall =
            client.newCall(Request.Builder().url(url).cacheControl(CacheControl.FORCE_NETWORK).post(body).build())
        return kotlin.runCatching {
            newCall.execute().use { response ->
                val resp = response.body!!.string()
                check(response.code == 200) { resp }
                val json = Json.parseToJsonElement(resp).jsonObject
                json["token"]?.jsonPrimitive?.content ?: error(resp)
            }
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
        extra: QueryAble? = null, // 额外特殊的检索标签
        noCache: Boolean = false
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
        return request(url, token, noCache)
    }

    fun search(
        token: String,
        page: Int,
        keyword: String,
        order: WorksOrder = WorksOrder.create_date,
        sort: QuerySort = QuerySort.desc,
        seed: Int = (0..100).random(),
        subtitle: Boolean = false, // 是否有字幕
        noCache: Boolean = false
    ): Result<Works> {
        return searchRaw(token, page, keyword.toUrlEncoded(), order, sort, seed, subtitle, noCache)
    }

    // 搜索
    fun searchRaw(
        token: String,
        page: Int,
        keyword: String,
        order: WorksOrder = WorksOrder.create_date,
        sort: QuerySort = QuerySort.desc,
        seed: Int = (0..100).random(),
        subtitle: Boolean = false, // 是否有字幕
        noCache: Boolean = false
    ): Result<Works> {
        val url0 = "$ASMR_BASE_URL/api/search/$keyword"
        val url = url0.toHttpUrl().newBuilder().apply {
            addQueryParameter("order", order.name)
            addQueryParameter("sort", sort.name)
            addQueryParameter("page", page.toString())
            addQueryParameter("seed", seed.toString())
            addQueryParameter("subtitle", (if (subtitle) 1 else 0).toString())
        }.build().toString()
        return request(url, token, noCache)
    }

    // 收藏页面
    fun review(
        token: String,
        page: Int,
        order: ReviewOrder = ReviewOrder.updated_at,
        sort: QuerySort = QuerySort.desc,
        filter: ListenState? = null,
        noCache: Boolean = false
    ): Result<Works> {
        val url = "$ASMR_BASE_URL/api/review".toHttpUrl().newBuilder().apply {
            addQueryParameter("order", order.name)
            addQueryParameter("sort", sort.name)
            addQueryParameter("page", page.toString())
            if (filter != null) {
                addQueryParameter("filter", filter.toString())
            }
        }.build().toString()
        return request(url, token, noCache)
    }

    // 不带个人信息 RJ号直达
    fun workInfo(token: String, id: Int, noCache: Boolean = false): Result<Work> =
        request("$ASMR_BASE_URL/api/workInfo/$id", token, noCache)

    // 带个人信息 RJ号直达
    fun work(token: String, id: Int, noCache: Boolean = false): Result<Work> =
        request("$ASMR_BASE_URL/api/work/$id", token, noCache)

    // 详细目录 RJ号直达
    fun tracks(token: String, id: Int, noCache: Boolean = false): Result<List<RemoteFile>> =
        request("$ASMR_BASE_URL/api/tracks/$id", token, noCache)

    // 社团
    fun circles(token: String, noCache: Boolean = false): Result<List<Circle>> =
        request("$ASMR_BASE_URL/api/circles", token, noCache)

    // 标签
    fun tags(token: String, noCache: Boolean = false): Result<List<Tag>> =
        request("$ASMR_BASE_URL/api/tags", token, noCache)

    // 声优
    fun vas(token: String, noCache: Boolean = false): Result<List<Va>> =
        request("$ASMR_BASE_URL/api/vas", token, noCache)

    fun random(token: String, noCache: Boolean = false): Result<Works> =
        request("$ASMR_BASE_URL/api/works?order=betterRandom", token, noCache)

    private inline fun <reified R> request(url: String, token: String, noCache: Boolean): Result<R> {
        val request = Request.Builder()
            .url(url)
            .header("authorization", "Bearer $token")
            .let {
                if (noCache) it.cacheControl(CacheControl.FORCE_NETWORK)
                else it
            }
            .build()
        return runCatching {
            client.newCall(request).execute().use { response ->
                val bodyString = response.body!!.string()
                check(response.code == 200) { bodyString }
                json.decodeFromString(bodyString)
            }
        }
    }

    private inline fun <reified R, reified I> requestPost(
        url: String,
        token: String,
        body: I,
        noCache: Boolean = false,
    ): Result<R> {
        val request = Request.Builder()
            .url(url)
            .header("authorization", "Bearer $token")
            .post(Json.encodeToString(body).toRequestBody("application/json".toMediaType()))
            .let {
                if (noCache) it.cacheControl(CacheControl.FORCE_NETWORK)
                else it
            }
            .build()
        return runCatching {
            client.newCall(request).execute().use { response ->
                val bodyString = response.body!!.string()
                check(response.code == 200) { bodyString }
                json.decodeFromString(bodyString)
            }
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
            .cacheControl(CacheControl.FORCE_NETWORK)
            .put(body)
            .build()
        return runCatching {
            client.newCall(request).execute().use { response ->
                response.body!!.string()
            }
        }
    }

    fun deleteMark(
        token: String,
        work_id: Int
    ): Result<String> {
        val request = Request.Builder()
            .url("$ASMR_BASE_URL/api/review?work_id=$work_id")
            .header("authorization", "Bearer $token")
            .cacheControl(CacheControl.FORCE_NETWORK)
            .delete()
            .build()
        return runCatching {
            client.newCall(request).execute().use { response ->
                response.body!!.string()
            }
        }
    }

    fun checkLrc(
        token: String,
        hash: String
    ): Result<String?> {
        val request = Request.Builder()
            .url("$ASMR_BASE_URL/api/media/check-lrc/$hash")
            .header("authorization", "Bearer $token")
            .get()
            .build()
        return kotlin.runCatching {
            val response = client.newCall(request).execute().use { it.body!!.string() }
            val lrcHash = Json.parseToJsonElement(response).jsonObject["hash"]?.jsonPrimitive?.content
            if (lrcHash.isNullOrEmpty()) null else lrcHash
        }
    }

    fun checkLrc2(
        token: String,
        query: SubtitleQuery
    ): Result<SubtitleList> {
        return requestPost("$ASMR_BASE_URL/api/media/check-lrc-v2", token, query)
    }

    fun worksV2(
        token: String,
        page: Int,
        order: WorksOrder = WorksOrder.create_date,
        sort: QuerySort = QuerySort.desc,
        seed: Int = (0..100).random(),
        subtitle: Boolean = false, // 是否有字幕
        extra: QueryAble? = null, // 额外特殊的检索标签
        noCache: Boolean = false,
        localSubtitle: List<String> = listOf()
    ): Result<Works> {
        if (extra != null) {
            return searchV2(token, page, keyword = extra.name(), order, sort, seed, subtitle, noCache, localSubtitle)
        }
        val params = WorksV2Params(page, order, sort, seed, if (subtitle) 1 else 0, localSubtitle)
        return requestPost("$ASMR_BASE_URL/api/works", token, params, noCache)
    }

    fun searchV2(
        token: String,
        page: Int,
        keyword: String,
        order: WorksOrder = WorksOrder.create_date,
        sort: QuerySort = QuerySort.desc,
        seed: Int = (0..100).random(),
        subtitle: Boolean = false, // 是否有字幕
        noCache: Boolean = false,
        localSubtitle: List<String> = listOf()
    ): Result<Works> {
        return searchRawV2(token, page, keyword.toUrlEncoded(), order, sort, seed, subtitle, noCache, localSubtitle)
    }

    private fun searchRawV2(
        token: String,
        page: Int,
        keyword: String,
        order: WorksOrder = WorksOrder.create_date,
        sort: QuerySort = QuerySort.desc,
        seed: Int = (0..100).random(),
        subtitle: Boolean = false, // 是否有字幕
        noCache: Boolean = false,
        localSubtitle: List<String> = listOf()
    ): Result<Works> {
        val params = WorksV2Params(page, order, sort, seed, if (subtitle) 1 else 0, localSubtitle)
        return requestPost("$ASMR_BASE_URL/api/search/$keyword", token, params, noCache)
    }

    @Serializable
    private data class WorksV2Params(
        val page: Int,
        val order: WorksOrder,
        val sort: QuerySort,
        val seed: Int,
        val subtitle: Int,
        val localSubtitledWorks: List<String>
    )

    fun downloadLrc(
        token: String,
        hash: String
    ): Result<String> {
        val request = Request.Builder()
            .url("$ASMR_BASE_URL/api/media/stream/$hash")
            .header("authorization", "Bearer $token")
            .get()
            .build()
        return kotlin.runCatching {
            client.newCall(request).execute().use { response ->
                response.body!!.string()
            }
        }
    }

    private fun String.toUrlEncoded(): String = URLEncoder
        .encode(this, Charsets.UTF_8.displayName())
        .replace("+", "%20")
        .replace("%7E", "~")
        .replace("%24", "$")
        .replace("%3A", ":")

    fun playlist(
        token: String,
        page: Int = 1,
        pageSize: Int = 12,
        filterBy: PlaylistFilter = PlaylistFilter.all,
        noCache: Boolean = false
    ): Result<PlayLists> {
        val url = "$ASMR_BASE_URL/api/playlist/get-playlists".toHttpUrl().newBuilder()
            .addQueryParameter("page", page.toString())
            .addQueryParameter("pageSize", pageSize.toString())
            .addQueryParameter("filterBy", filterBy.name)
            .build()
            .toString()
        return request(url, token, noCache)
    }

    fun playlistWorks(
        token: String,
        id: String,
        page: Int = 1,
        pageSize: Int = 12,
        noCache: Boolean = false
    ): Result<Works> {
        val url = "$ASMR_BASE_URL/api/playlist/get-playlist-works".toHttpUrl().newBuilder()
            .addQueryParameter("page", page.toString())
            .addQueryParameter("pageSize", pageSize.toString())
            .addQueryParameter("id", id)
            .build()
            .toString()
        return request(url, token, noCache)
    }

    fun playlistMetadata(
        token: String,
        id: String,
        noCache: Boolean = false
    ): Result<PlayList> {
        val url = "$ASMR_BASE_URL/api/playlist/get-playlist-metadata".toHttpUrl().newBuilder()
            .addQueryParameter("id", id)
            .build()
            .toString()
        return request(url, token, noCache)
    }

    fun playlistCreate(
        token: String,
        name: String,
        privacy: Int = 2, // 0 public, 1 url, 2 private
        locale: String = "zh-CN",
        description: String = "",
        works: List<String> = emptyList()
    ): Result<PlayList> {
        val params = buildJsonObject {
            put("name", name)
            put("privacy", privacy)
            put("locale", locale)
            put("description", description)
            put("works", JsonArray(works.map { JsonPrimitive(it) }))
        }
        return requestPost("$ASMR_BASE_URL/api/playlist/create-playlist", token, params)
    }

    fun playlistMetadataEdit(
        token: String,
        playlist: PlayList
    ): Result<PlayList> {
        return playlistMetadataEdit(token, playlist.id, playlist.name, playlist.privacy, playlist.description)
    }

    fun playlistMetadataEdit(
        token: String,
        id: String,
        name: String,
        privacy: Int,
        description: String
    ): Result<PlayList> {
        val params = buildJsonObject {
            put("id", id)
            putJsonObject("data") {
                put("name", name)
                put("privacy", privacy)
                put("description", description)
            }
        }
        return requestPost("$ASMR_BASE_URL/api/playlist/edit-playlist-metadata", token, params)
    }

    fun playlistDelete(
        token: String,
        id: String
    ): Result<PlaylistBean> {
        return requestPost("$ASMR_BASE_URL/api/playlist/delete-playlist", token, PlaylistBean(id))
    }

    @Serializable
    data class PlaylistBean(val id: String, val liked: Boolean = false)

    fun playlistCheck(
        token: String,
        workID: String,
        page: Int = 1,
        pageSize: Int = 12,
        noCache: Boolean = false
    ): Result<PlayLists> {
        val url = "$ASMR_BASE_URL/api/playlist/get-work-exist-status-in-my-playlists".toHttpUrl().newBuilder()
            .addQueryParameter("page", page.toString())
            .addQueryParameter("pageSize", pageSize.toString())
            .addQueryParameter("id", workID)
            .addQueryParameter("version", "2")
            .build()
            .toString()
        return request(url, token, noCache)
    }

    // TODO /api/playlist/get-default-mark-target-playlist

    fun playlistStatus(token: String, playlistId: String, noCache: Boolean = false): Result<PlaylistBean> {
        val url = "$ASMR_BASE_URL/api/playlist/get-liked-status?id=$playlistId"
        return request(url, token, noCache)
    }

    fun playlistAdd(
        token: String,
        playlistId: String,
        works: List<String>
    ): Result<PlaylistBean> {
        val url = "$ASMR_BASE_URL/api/playlist/add-works-to-playlist"
        val body = buildJsonObject {
            put("id", playlistId)
            put("works", JsonArray(works.map { JsonPrimitive(it) }))
        }
        return requestPost(url, token, body)
    }

    fun playlistRemove(
        token: String,
        playlistId: String,
        works: List<String>
    ): Result<PlaylistBean> {
        val url = "$ASMR_BASE_URL/api/playlist/remove-works-to-playlist"
        val body = buildJsonObject {
            put("id", playlistId)
            put("works", JsonArray(works.map { JsonPrimitive(it) }))
        }
        return requestPost(url, token, body)
    }

    fun playlistFavorite(
        token: String,
        playlistId: String
    ): Result<PlaylistBean> {
        val url = "$ASMR_BASE_URL/api/playlist/like-playlist"
        return requestPost(url, token, PlaylistBean(playlistId))
    }

    fun playlistUnFavorite(
        token: String,
        playlistId: String
    ): Result<PlaylistBean> {
        val url = "$ASMR_BASE_URL/api/playlist/remove-like-playlist"
        return requestPost(url, token, PlaylistBean(playlistId))
    }

}

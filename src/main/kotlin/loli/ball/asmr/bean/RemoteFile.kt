package loli.ball.asmr.bean

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class RemoteFile {

    abstract val title: String

    @Serializable
    @SerialName("folder")
    data class Folder(
        override val title: String,
        val children: List<RemoteFile>
    ) : RemoteFile()

    @Serializable
    sealed class File : RemoteFile() {

        abstract val hash: String
        abstract val workTitle: String
        abstract val mediaStreamUrl: String
        abstract val mediaDownloadUrl: String

        @Serializable
        @SerialName("audio")
        data class Audio(
            override val title: String,
            override val hash: String,
            override val workTitle: String,
            override val mediaStreamUrl: String,
            override val mediaDownloadUrl: String,
            val streamLowQualityUrl: String? = null,
            val duration: Float = 0f
        ) : File()

        @Serializable
        @SerialName("text")
        data class Text(
            override val title: String,
            override val hash: String,
            override val workTitle: String,
            override val mediaStreamUrl: String,
            override val mediaDownloadUrl: String,
        ) : File()

        @Serializable
        @SerialName("image")
        data class Image(
            override val title: String,
            override val hash: String,
            override val workTitle: String,
            override val mediaStreamUrl: String,
            override val mediaDownloadUrl: String,
        ) : File()

        @Serializable
        @SerialName("other")
        data class Other(
            override val title: String,
            override val hash: String,
            override val workTitle: String,
            override val mediaStreamUrl: String,
            override val mediaDownloadUrl: String,
        ) : File()

    }
}
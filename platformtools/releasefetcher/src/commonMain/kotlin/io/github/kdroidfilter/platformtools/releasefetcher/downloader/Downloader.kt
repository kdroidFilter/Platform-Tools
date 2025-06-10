package io.github.kdroidfilter.platformtools.releasefetcher.downloader

/** Represents a file downloaded by [Downloader]. */
expect class PlatformFile

/** Platform-agnostic downloader interface. */
expect class Downloader() {
    /**
     * Downloads a file from [downloadUrl].
     * [onProgress] is called with percentage progress and the resulting file when complete.
     * Returns `true` on success.
     */
    suspend fun downloadApp(downloadUrl: String, onProgress: (percentage: Double, file: PlatformFile?) -> Unit): Boolean
}

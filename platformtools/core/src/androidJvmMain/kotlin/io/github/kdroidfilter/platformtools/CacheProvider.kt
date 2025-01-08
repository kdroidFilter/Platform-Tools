package io.github.kdroidfilter.platformtools

import java.io.File

/**
 * Provides the directory used for storing cached data.
 *
 * @return A File object representing the cache directory.
 */
expect fun getCacheDir(): File

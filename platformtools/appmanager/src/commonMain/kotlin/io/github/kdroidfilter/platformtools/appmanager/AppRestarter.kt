package io.github.kdroidfilter.platformtools.appmanager

/**
 * Restarts the application.
 *
 * This function triggers a full application restart. It is generally used in scenarios where
 * a restart is necessary, such as after applying critical updates or configuration changes.
 *
 * Implementation is platform-specific and may utilize different mechanisms to restart the
 * application depending on the operating system and environment.
 */
expect fun restartApplication()
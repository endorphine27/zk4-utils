package com.nuvio.app.features.tv

import com.nuvio.app.core.storage.DesktopStorage
import com.nuvio.app.core.storage.ProfileScopedKey

internal actual object TvPreferencesStorage {
    private val store = DesktopStorage.store("nuvio_tv_preferences")

    actual fun loadPayload(): String? =
        store.getString(ProfileScopedKey.of("tv_preferences"))

    actual fun savePayload(payload: String) {
        store.putString(ProfileScopedKey.of("tv_preferences"), payload)
    }
}

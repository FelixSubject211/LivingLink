package felix.livinglink.common.store

import io.github.xxfast.kstore.KStore
import io.github.xxfast.kstore.storage.storeOf

actual inline fun <reified T : Any> createStore(path: String, defaultValue: T): KStore<T> = storeOf(
    key = path,
    default = defaultValue
)
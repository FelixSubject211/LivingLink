package felix.livinglink.common.store

import io.github.xxfast.kstore.KStore

expect inline fun <reified T : Any> createStore(path: String, defaultValue: T): KStore<T>
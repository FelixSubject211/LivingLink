package felix.livinglink.common.store

import android.content.Context
import felix.livinglink.AppContext
import felix.livinglink.json
import io.github.xxfast.kstore.KStore
import io.github.xxfast.kstore.file.storeOf
import kotlinx.io.files.Path
import java.io.File

actual inline fun <reified T : Any> createStore(path: String, defaultValue: T): KStore<T> {
    val context: Context = AppContext.applicationContext
    val file = File(context.filesDir, path)

    return storeOf(
        file = Path(file.absolutePath),
        default = defaultValue,
        json = json
    )
}
package felix.livinglink.common.store

import felix.livinglink.json
import io.github.xxfast.kstore.KStore
import io.github.xxfast.kstore.file.storeOf
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.io.files.Path
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask

@OptIn(ExperimentalForeignApi::class)
actual inline fun <reified T : Any> createStore(path: String, defaultValue: T): KStore<T> {
    val fileManager: NSFileManager = NSFileManager.defaultManager
    val documentsUrl: NSURL = fileManager.URLForDirectory(
        directory = NSDocumentDirectory,
        appropriateForURL = null,
        create = true,
        inDomain = NSUserDomainMask,
        error = null
    )!!

    val filePath = "${documentsUrl.path!!}/${path}"

    return storeOf(
        file = Path(filePath),
        default = defaultValue,
        json = json
    )
}
package felix.livinglink.ui

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import de.comahe.i18n4k.config.I18n4kConfigImmutable
import de.comahe.i18n4k.i18n4kInitCldrPluralRules
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@OptIn(DelicateCoroutinesApi::class)
fun initI18n4k(): MutableState<I18n4kConfigImmutable> {
    val i18n4kConfig = mutableStateOf(I18n4kConfigImmutable())
    GlobalScope.launch {
        i18n4kInitCldrPluralRules()
    }
    return i18n4kConfig
}
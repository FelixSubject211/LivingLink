package felix.projekt.livinglink.server.core.di

import felix.projekt.livinglink.server.core.config.CoreConfig
import felix.projekt.livinglink.server.core.config.coreDefaultConfig
import org.koin.dsl.module
import java.util.UUID

val coreModule = module {
    single<CoreConfig> { coreDefaultConfig() }
    single { { UUID.randomUUID().toString() } }
}
package felix.projekt.livinglink.composeApp.core.di

import felix.projekt.livinglink.composeApp.AppConfig
import felix.projekt.livinglink.composeApp.core.Database
import felix.projekt.livinglink.composeApp.core.infrastructure.createSqlDriver
import felix.projekt.livinglink.shared.json
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.koin.dsl.module

val coreModule = module {
    single {
        Database(driver = createSqlDriver())
    }

    single { CoroutineScope(Dispatchers.Default) }

    single {
        HttpClient {
            install(ContentNegotiation) { json(json) }
            defaultRequest {
                url {
                    protocol = AppConfig.serverUrlProtocol
                    host = AppConfig.serverHost
                    port = AppConfig.serverPort
                }
            }
        }
    }
}
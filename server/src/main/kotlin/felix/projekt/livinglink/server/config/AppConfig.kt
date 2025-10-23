package felix.projekt.livinglink.server.config

import io.github.cdimascio.dotenv.dotenv

interface AppConfig {

    val serverPort: Int
}

fun appDefaultConfig(): AppConfig {
    return object : AppConfig {
        private val dotenv = dotenv()
        override val serverPort: Int = dotenv["SERVER_PORT"]!!.toInt()
    }
}
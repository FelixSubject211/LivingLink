package felix.projekt.livinglink.server.core.config

import io.github.cdimascio.dotenv.dotenv

interface CoreConfig {

    val serverPort: Int
}

fun coreDefaultConfig(): CoreConfig {
    return object : CoreConfig {
        private val dotenv = dotenv()
        override val serverPort: Int = dotenv["SERVER_PORT"]!!.toInt()
    }
}
package felix.projekt.livinglink

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
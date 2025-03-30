package felix.livinglink.common.model

interface LivingLinkError {
    fun title(): String
    fun message(): String? {
        return null
    }
}
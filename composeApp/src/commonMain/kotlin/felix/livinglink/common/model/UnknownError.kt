package felix.livinglink.common.model

import CommonLocalizables

data class UnknownError(val error: Exception) : LivingLinkError {
    override fun title() = CommonLocalizables.unknownErrorTitle()
    override fun message(): String = error.toString()
}
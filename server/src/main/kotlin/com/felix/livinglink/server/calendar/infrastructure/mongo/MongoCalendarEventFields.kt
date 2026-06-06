package com.felix.livinglink.server.calendar.infrastructure.mongo

object MongoCalendarEventFields {
    const val GROUP_ID = "groupId"
    const val TITLE = "title"
    const val DESCRIPTION = "description"
    const val CREATED_BY_USER_ID = "createdByUserId"
    const val SPAN = "span"
    const val RECURRENCE = "recurrence"
    const val PARTICIPANTS = "participants"
    const val CATEGORY = "category"
    const val EFFECTIVE_FROM = "effectiveFrom"
    const val EFFECTIVE_TO = "effectiveTo"
    const val CREATED_AT = "createdAt"
    const val UPDATED_AT = "updatedAt"
    const val VERSION = "version"

    object Span {
        const val TYPE = "type"
        const val START = "start"
        const val END = "end"
        const val START_DATE = "startDate"
        const val END_DATE = "endDate"
    }

    object Recurrence {
        const val FREQUENCY = "frequency"
        const val INTERVAL = "interval"
        const val END = "end"

        object End {
            const val TYPE = "type"
            const val AT = "at"
            const val OCCURRENCES = "occurrences"
        }
    }

    object Participant {
        const val USER_ID = "userId"
        const val RSVP_EVENTS = "rsvpEvents"

        object RsvpEvent {
            const val STATUS = "status"
            const val AT = "at"
        }
    }

    object Category {
        const val TYPE = "type"
        const val LABEL = "label"
        const val SHOPPING_LIST_ITEM_IDS = "shoppingListItemIds"
    }

    const val PARTICIPANT_USER_ID_PATH = "$PARTICIPANTS.${Participant.USER_ID}"
}

package com.felix.livinglink.server.calendar.infrastructure.mongo

import com.felix.livinglink.server.calendar.domain.CalendarEvent
import com.felix.livinglink.server.calendar.domain.CalendarEventQuery
import com.felix.livinglink.server.calendar.domain.CalendarEventRepository
import com.felix.livinglink.server.core.domain.CrudRepository
import com.felix.livinglink.server.core.domain.MappedCrudRepository
import com.felix.livinglink.server.core.infrastructure.mongo.MongoCrudRepository
import com.mongodb.client.model.Filters
import com.mongodb.kotlin.client.coroutine.MongoCollection
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.bson.conversions.Bson
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

@Single(binds = [CalendarEventRepository::class])
class MongoCalendarEventRepository(
    @Named("calendarEvents")
    private val collection: MongoCollection<MongoCalendarEventDocument>,
) : CalendarEventRepository,
    CrudRepository<CalendarEvent> by MappedCrudRepository(
        storageRepository =
            MongoCrudRepository(
                collection = collection,
                entityName = "Calendar event",
            ),
        toStorage = MongoCalendarEventDocument::fromDomain,
        toDomain = MongoCalendarEventDocument::toDomain,
    ) {
    override suspend fun find(query: CalendarEventQuery): Flow<CalendarEvent> {
        val filters =
            buildList<Bson> {
                add(
                    Filters.and(
                        Filters.lte(MongoCalendarEventFields.EFFECTIVE_FROM, query.to),
                        Filters.gte(MongoCalendarEventFields.EFFECTIVE_TO, query.from),
                    ),
                )

                query.participantUserIds?.takeIf { it.isNotEmpty() }?.let { ids ->
                    add(Filters.`in`(MongoCalendarEventFields.PARTICIPANT_USER_ID_PATH, ids))
                }

                query.createdByUserIds?.takeIf { it.isNotEmpty() }?.let { ids ->
                    add(Filters.`in`(MongoCalendarEventFields.CREATED_BY_USER_ID, ids))
                }
            }

        return collection
            .find(Filters.and(filters))
            .map { it.toDomain() }
    }

    override suspend fun findDistinctCustomCategoryLabels(): List<String> =
        collection
            .distinct<String>(
                fieldName = "${MongoCalendarEventFields.CATEGORY}.${MongoCalendarEventFields.Category.LABEL}",
                filter = Filters.eq("${MongoCalendarEventFields.CATEGORY}.${MongoCalendarEventFields.Category.TYPE}", MongoCalendarEventDocument.Category.TYPE_CUSTOM),
            ).toList()
            .sorted()
}

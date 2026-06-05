package com.felix.livinglink.server.calendar.infrastructure.mongo

import com.felix.livinglink.server.core.infrastructure.mongo.MongoClientProvider
import com.mongodb.kotlin.client.coroutine.MongoCollection
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

@Single
@Named("calendarEvents")
fun calendarEventMongoCollection(
    mongoClientProvider: MongoClientProvider,
): MongoCollection<MongoCalendarEventDocument> =
    mongoClientProvider
        .database()
        .getCollection<MongoCalendarEventDocument>("calendar_events")

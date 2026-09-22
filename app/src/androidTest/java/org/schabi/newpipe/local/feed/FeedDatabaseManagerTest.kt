/*
 * SPDX-FileCopyrightText: 2026 PVCPipe contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.schabi.newpipe.local.feed

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import java.time.OffsetDateTime
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.schabi.newpipe.database.AppDatabase
import org.schabi.newpipe.extractor.stream.StreamInfoItem
import org.schabi.newpipe.extractor.stream.StreamType

class FeedDatabaseManagerTest {
    private lateinit var database: AppDatabase
    private lateinit var manager: FeedDatabaseManager

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext<Context>(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        manager = FeedDatabaseManager(database)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun upsertAllRollsBackStreamsWhenSubscriptionDoesNotExist() {
        val item = StreamInfoItem(
            0,
            "https://example.com/watch?v=transaction-test",
            "transaction test",
            StreamType.LIVE_STREAM
        )

        val result = runCatching {
            manager.upsertAll(
                subscriptionId = Long.MAX_VALUE,
                items = listOf(item),
                oldestAllowedDate = OffsetDateTime.MIN
            )
        }

        assertTrue(result.isFailure)
        assertFalse(database.streamDAO().exists(item.serviceId, item.url))
    }
}

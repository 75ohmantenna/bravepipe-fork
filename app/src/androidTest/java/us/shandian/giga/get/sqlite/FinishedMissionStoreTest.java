/*
 * SPDX-FileCopyrightText: 2026 BravePipe contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package us.shandian.giga.get.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;

import androidx.test.core.app.ApplicationProvider;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class FinishedMissionStoreTest {
    private static final String DATABASE_NAME = "downloads.db";
    private static final String OLD_TABLE_NAME = "download_missions";

    private Context context;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        context.deleteDatabase(DATABASE_NAME);
    }

    @After
    public void tearDown() {
        context.deleteDatabase(DATABASE_NAME);
    }

    @Test
    public void failedUpgradeKeepsLegacyMissions() {
        try (SQLiteDatabase database = context.openOrCreateDatabase(
                DATABASE_NAME, Context.MODE_PRIVATE, null)) {
            database.execSQL("CREATE TABLE " + OLD_TABLE_NAME + " ("
                    + "location TEXT NOT NULL, name TEXT NOT NULL, url TEXT NOT NULL, "
                    + "bytes_downloaded INTEGER NOT NULL, timestamp INTEGER NOT NULL, "
                    + "kind TEXT)");

            insertLegacyMission(database, "first-url");
            insertLegacyMission(database, "second-url");
            database.setVersion(3);
        }

        try (FinishedMissionStore store = new FinishedMissionStore(context)) {
            assertThrows(SQLiteConstraintException.class, store::getWritableDatabase);
        }

        try (SQLiteDatabase database = context.openOrCreateDatabase(
                DATABASE_NAME, Context.MODE_PRIVATE, null);
             Cursor cursor = database.rawQuery(
                     "SELECT COUNT(*) FROM " + OLD_TABLE_NAME, null)) {
            cursor.moveToFirst();
            assertEquals(2, cursor.getInt(0));
        }
    }

    private static void insertLegacyMission(final SQLiteDatabase database, final String url) {
        final ContentValues values = new ContentValues();
        values.put("location", "/tmp");
        values.put("name", "duplicate.mp4");
        values.put("url", url);
        values.put("bytes_downloaded", 1);
        values.put("timestamp", 1);
        values.put("kind", "v");
        database.insertOrThrow(OLD_TABLE_NAME, null, values);
    }
}

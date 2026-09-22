/*
 * SPDX-FileCopyrightText: 2026 PVCPipe contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.schabi.newpipe.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.util.Set;

public class StateSaverFileCleanupTest {
    @Rule
    public final TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void deleteInactiveFilesPreservesActiveState() throws IOException {
        final File cacheDir = temporaryFolder.newFolder("state_cache");
        final File staleState = new File(cacheDir, "stale.cache");
        final File activeState = new File(cacheDir, "active.cache");
        assertTrue(staleState.createNewFile());
        assertTrue(activeState.createNewFile());

        StateSaverFileCleanup.deleteInactiveFiles(
                cacheDir, Set.of(activeState.getAbsolutePath()));

        assertFalse(staleState.exists());
        assertTrue(activeState.exists());
    }

    @Test
    public void deleteInactiveFilesAcceptsMissingDirectory() {
        final File missingCacheDir = new File(temporaryFolder.getRoot(), "missing");

        StateSaverFileCleanup.deleteInactiveFiles(missingCacheDir, Set.of());

        assertFalse(missingCacheDir.exists());
    }
}

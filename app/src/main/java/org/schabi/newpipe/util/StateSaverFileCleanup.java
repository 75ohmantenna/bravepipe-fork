/*
 * SPDX-FileCopyrightText: 2026 BravePipe-fork contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.schabi.newpipe.util;

import java.io.File;
import java.util.Set;

final class StateSaverFileCleanup {
    private StateSaverFileCleanup() {
    }

    static void deleteInactiveFiles(final File cacheDir, final Set<String> activeStateFiles) {
        final File[] files = cacheDir.listFiles();
        if (files == null) {
            return;
        }

        for (final File file : files) {
            if (!activeStateFiles.contains(file.getAbsolutePath())) {
                delete(file);
            }
        }
    }

    static void delete(final File file) {
        try {
            //noinspection ResultOfMethodCallIgnored
            file.delete();
        } catch (final SecurityException ignored) {
            // Cache cleanup is best effort.
        }
    }
}

package org.schabi.newpipe.streams.io;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.net.Uri;

import org.junit.Test;

public class StoredFileHelperTest {
    private static final String MIME_TYPE = "video/mp4";

    @Test
    public void invalidFilesWithTheSameParentAndMetadataAreEqual() {
        assertTrue(invalidFile("content://provider/tree/Parent")
                .equals(invalidFile("content://provider/tree/Parent")));
    }

    @Test
    public void invalidFilesWithDifferentParentsAreNotEqual() {
        assertFalse(invalidFile("content://provider/tree/first")
                .equals(invalidFile("content://provider/tree/second")));
    }

    @Test
    public void parentDocumentIdsRemainCaseSensitive() {
        assertFalse(invalidFile("content://provider/tree/Parent")
                .equals(invalidFile("content://provider/tree/parent")));
    }

    private static StoredFileHelper invalidFile(final String parentValue) {
        final Uri parent = mock(Uri.class);
        when(parent.toString()).thenReturn(parentValue);
        return new StoredFileHelper(parent, "video.mp4", MIME_TYPE, "tag");
    }
}

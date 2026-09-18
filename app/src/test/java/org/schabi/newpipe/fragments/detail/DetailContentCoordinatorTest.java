package org.schabi.newpipe.fragments.detail;

import org.junit.Test;
import org.schabi.newpipe.brave.fragments.BraveHostFragment;
import org.schabi.newpipe.fragments.list.comments.CommentsFragment;

import androidx.fragment.app.Fragment;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DetailContentCoordinatorTest {

    @Test
    public void hostReturnsPendingFragmentBeforeAttachment() {
        final CommentsFragment comments = mock(CommentsFragment.class);

        assertSame(comments, BraveHostFragment.newInstance(comments).getHostedFragment());
    }

    @Test
    public void resolvesCommentsFragmentFromHost() {
        final BraveHostFragment host = mock(BraveHostFragment.class);
        final CommentsFragment comments = mock(CommentsFragment.class);
        when(host.getHostedFragment()).thenReturn(comments);

        assertSame(comments, DetailContentCoordinator.resolveCommentsFragment(host));
    }

    @Test
    public void acceptsUnwrappedCommentsFragment() {
        final CommentsFragment comments = mock(CommentsFragment.class);

        assertSame(comments, DetailContentCoordinator.resolveCommentsFragment(comments));
    }

    @Test
    public void rejectsNonCommentsFragment() {
        assertNull(DetailContentCoordinator.resolveCommentsFragment(mock(Fragment.class)));
    }
}

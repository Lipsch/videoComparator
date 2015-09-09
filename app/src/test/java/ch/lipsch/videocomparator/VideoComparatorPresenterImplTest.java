package ch.lipsch.videocomparator;

import android.net.Uri;

import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertTrue;

public class VideoComparatorPresenterImplTest {

    /**
     * Verifies that the video time text is made visible after every touch.
     */
    @Test
    public void videoTimeVisibleAfterTouch() {
        VideoComparatorView viewMock = Mockito.mock(VideoComparatorView.class);
        Uri videoUriMock = Mockito.mock(Uri.class);

        VideoComparatorPresenterImpl target = new VideoComparatorPresenterImpl(viewMock);

        //Load videos
        target.onLoadVideoRequested(videoUriMock, CommonDefinitions.VIDEOVIEW1);
        target.onLoadVideoRequested(videoUriMock, CommonDefinitions.VIDEOVIEW2);

        target.onTouch();

        Mockito.verify(viewMock).setVideoTimeVisibility(CommonDefinitions.VIDEOVIEW1, true);
        Mockito.verify(viewMock).setVideoTimeVisibility(CommonDefinitions.VIDEOVIEW2, true);

        assertTrue(true);
    }
}
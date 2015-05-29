package ch.lipsch.videocomparator;

import android.net.Uri;

import junit.framework.TestCase;

/**
 * Test for the class VideoPlayState.
 */

public class VideoPlayStateTest extends TestCase {

    public void testShowPlayButtonNoVideoLoaded() {
        VideoPlayState target = new VideoPlayState();

        target.setVideo1(null);
        target.setVideo2(null);

        assertFalse(target.shouldShowPlayButton());
    }

    public void testShowPlayButtonOneVideoLoaded() {
        VideoPlayState target = new VideoPlayState();

        Uri fakeVideoUri = Uri.parse("file:///somefile.avi");
        target.setVideo1(fakeVideoUri);
        target.setVideo2(null);

        assertTrue(target.shouldShowPlayButton());
    }

    public void testShowPlayButtonTwoVideosLoaded() {
        VideoPlayState target = new VideoPlayState();

        Uri fakeVideoUri1 = Uri.parse("file:///somefile.avi");
        Uri fakeVideoUri2 = Uri.parse("file:///somefile2.avi");
        target.setVideo1(fakeVideoUri1);
        target.setVideo2(fakeVideoUri2);

        assertTrue(target.shouldShowPlayButton());
    }

    public void testShowPlayButtonOnePlaying() {
        VideoPlayState target = new VideoPlayState();

        Uri fakeVideoUri1 = Uri.parse("file:///somefile.avi");
        Uri fakeVideoUri2 = Uri.parse("file:///somefile2.avi");
        target.setVideo1(fakeVideoUri1);
        target.setVideo2(fakeVideoUri2);

        target.setVideo1State(VideoPlayState.State.PLAYING);

        assertFalse(target.shouldShowPlayButton());
    }

    public void testShowPlayButtonAllPlaying() {
        VideoPlayState target = new VideoPlayState();

        Uri fakeVideoUri1 = Uri.parse("file:///somefile.avi");
        Uri fakeVideoUri2 = Uri.parse("file:///somefile2.avi");
        target.setVideo1(fakeVideoUri1);
        target.setVideo2(fakeVideoUri2);

        target.setVideo1State(VideoPlayState.State.PLAYING);
        target.setVideo2State(VideoPlayState.State.PLAYING);

        assertFalse(target.shouldShowPlayButton());
    }

    public void testShowPauseButtonNoVideos() {
        VideoPlayState target = new VideoPlayState();

        target.setVideo1(null);
        target.setVideo2(null);

        assertFalse(target.shouldShowPauseButton());
    }

    public void testShowPauseButtonVideoLoadedNotPlaying() {
        VideoPlayState target = new VideoPlayState();

        Uri fakeVideoUri = Uri.parse("file:///somefile.avi");
        target.setVideo1(fakeVideoUri);
        target.setVideo2(null);

        assertFalse(target.shouldShowPauseButton());
    }

    public void testShowPauseButtonVideoPlaying() {
        VideoPlayState target = new VideoPlayState();

        Uri fakeVideoUri = Uri.parse("file:///somefile.avi");
        target.setVideo1(fakeVideoUri);
        target.setVideo2(null);

        target.setVideo1State(VideoPlayState.State.PLAYING);

        assertTrue(target.shouldShowPauseButton());
    }

    public void testShowStopButtonNoVideos() {
        VideoPlayState target = new VideoPlayState();

        target.setVideo1(null);
        target.setVideo2(null);

        assertFalse(target.shouldShowStopButton());
    }

    public void testShowStopButtonVideoLoaded() {
        VideoPlayState target = new VideoPlayState();

        Uri fakeVideoUri = Uri.parse("file:///somefile.avi");
        target.setVideo1(fakeVideoUri);
        target.setVideo2(null);

        assertFalse(target.shouldShowStopButton());
    }

    public void testShowStopButtonVideoPlaying() {
        VideoPlayState target = new VideoPlayState();

        Uri fakeVideoUri = Uri.parse("file:///somefile.avi");
        target.setVideo1(fakeVideoUri);
        target.setVideo2(null);

        target.setVideo1State(VideoPlayState.State.PLAYING);

        assertTrue(target.shouldShowStopButton());
    }

    public void testShowStopButtonVideoPaused() {
        VideoPlayState target = new VideoPlayState();

        Uri fakeVideoUri = Uri.parse("file:///somefile.avi");
        target.setVideo1(fakeVideoUri);
        target.setVideo2(null);

        target.setVideo1State(VideoPlayState.State.PAUSING);
        target.pauseVideo1(1.23);

        assertTrue(target.shouldShowStopButton());
    }

    public void testShowPlayButtonVideoPaused() {
        VideoPlayState target = new VideoPlayState();

        Uri fakeVideoUri = Uri.parse("file:///somefile.avi");
        target.setVideo1(fakeVideoUri);
        target.setVideo2(null);

        target.setVideo1State(VideoPlayState.State.PLAYING);
        target.pauseVideo1(1.23);

        assertTrue(target.shouldShowPlayButton());
    }
}

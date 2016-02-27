package ch.lipsch.videocomparator;

import android.app.Application;

/**
 * The application singleton.
 */
public class VideoComparatorApplication extends Application {

    /**
     * Stores the current state of the videos. In order to restore it in case of app going to background or device orientation
     */
    private final VideoPlayState videoPlayState = new VideoPlayState();

    /**
     * Delivers the state of the application. That is which videos are loaded, at which time...
     *
     * @return The video play state.
     */
    public VideoPlayState getVideoPlayState() {
        return videoPlayState;
    }
}

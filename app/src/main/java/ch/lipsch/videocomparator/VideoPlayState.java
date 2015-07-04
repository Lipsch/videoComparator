/*
 * Copyright (C) 2015 Erwin Betschart
 *
 * This file is part of Video Comparator.
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; If not, see <http://www.gnu.org/licenses/>.
 */
package ch.lipsch.videocomparator;

import android.net.Uri;
import android.os.Bundle;

/**
 * The video play state stores all information about the current state of the playing videos. E.g. are the videos playing, current time, what videos are loaded...
 * This class is not thread-safe: All reads/writes must be done in the main thread.
 */
class VideoPlayState {

    /**
     * This enum denote the states in which a video view can be.
     */
    public enum State {
        EMPTY, LOADED, PLAYING, PAUSING, ERROR
    }

    private static final String URI_VIDEO1_KEY = "VideoPlayState.uriVideo1";
    private static final String URI_VIDEO2_KEY = "VideoPlayState.uriVideo2";
    private static final String PAUSE_VIDEO1_KEY = "VideoPlayState.pauseVideo1";
    private static final String PAUSE_VIDEO2_KEY = "VideoPlayState.pauseVideo1";
    private static final String STATE_VIDEO1_KEY = "VideoPlayState.stateVideo1";
    private static final String STATE_VIDEO2_KEY = "VideoPlayState.stateVideo2";
    private static final String VIDEO_MUTED_KEY = "VideoPlayState.videoMuted";

    /**
     * A key in the saved state to know that the state has been saved.
     * Otherwise we do not know if null was saved or there was nothing saved at all.
     */
    private static final String HAS_VIDEO_STATE_KEY = "VideoPlayState.hasVideoState";

    private Uri video1 = null;
    private Uri video2 = null;

    private State video1State = State.EMPTY;
    private State video2State = State.EMPTY;

    private boolean video1Seekable = true;
    private boolean video2Seekable = true;

    private boolean videoMuted = true;

    /**
     * Time when the video 1 was paused. Duration in seconds.
     */
    private Double video1PausedAtInSec = null;

    /**
     * Time when the video 1 was paused. Duration in seconds.
     */
    private Double video2PausedAtInSec = null;

    /**
     * The video 1 is paused at the given time in seconds.
     *
     * @param pauseTimeInSeconds The pause time in seconds (duration from the start).
     */
    public void pauseVideo1(double pauseTimeInSeconds) {
        video1PausedAtInSec = pauseTimeInSeconds;
        setVideo1State(State.PAUSING);
    }

    /**
     * The video 2 is paused at the given time in seconds.
     *
     * @param pauseTimeInSeconds The pause time in seconds (duration from the start).
     */
    public void pauseVideo2(double pauseTimeInSeconds) {
        video2PausedAtInSec = pauseTimeInSeconds;
        setVideo2State(State.PAUSING);
    }

    /**
     * Delivers the pause time of video 1.
     *
     * @return The time in seconds where the video is paused (duration since start). Returns null if the video is not paused.
     */
    public Double getVideo1PauseTime() {
        return video1PausedAtInSec;
    }

    /**
     * Delivers the pause time of video 2.
     *
     * @return The time in seconds where the video is paused (duration since start). Returns null if the video is not paused.
     */
    public Double getVideo2PauseTime() {
        return video2PausedAtInSec;
    }

    public boolean isVideo1Playing() {
        return video1State.equals(State.PLAYING);
    }

    public void setVideo1State(State state) {
        video1State = state;
    }

    public void setVideo2State(State state) {
        video2State = state;
    }

    public boolean isVideo2Playing() {
        return video2State.equals(State.PLAYING);
    }

    public void setVideoMuted(boolean muted) {
        videoMuted = muted;
    }

    public boolean isVideoMuted() {
        return videoMuted;
    }

    public Uri getVideo1() {
        return video1;
    }

    /**
     * Sets the uri of the first video. As a side effect all states of the video1 are set to loaded and not playing.
     *
     * @param video1 The uri of the video.
     */
    public void setVideo1(Uri video1) {
        this.video1 = video1;
        setVideo1State(State.LOADED);
        video1PausedAtInSec = null;
    }

    public Uri getVideo2() {
        return video2;
    }

    /**
     * Sets the uri of the second video. As a side effect all states of the video 2 are set to loaded and not playing.
     *
     * @param video2 The uri of the video.
     */
    public void setVideo2(Uri video2) {
        this.video2 = video2;
        setVideo2State(State.LOADED);
        video2PausedAtInSec = null;
    }

    public void saveState(Bundle bundle) {
        bundle.putBoolean(VIDEO_MUTED_KEY, videoMuted);
        bundle.putBoolean(HAS_VIDEO_STATE_KEY, true);

        if (video1 == null) {
            bundle.putString(URI_VIDEO1_KEY, null);
        } else {
            bundle.putString(URI_VIDEO1_KEY, video1.toString());
            bundle.putInt(STATE_VIDEO1_KEY, video1State.ordinal());

            if (video1PausedAtInSec != null) {
                bundle.putDouble(PAUSE_VIDEO1_KEY, video1PausedAtInSec);
            }
        }

        if (video2 == null) {
            bundle.putString(URI_VIDEO2_KEY, null);
        } else {
            bundle.putString(URI_VIDEO2_KEY, video2.toString());
            bundle.putInt(STATE_VIDEO2_KEY, video1State.ordinal());

            if (video2PausedAtInSec != null) {
                bundle.putDouble(PAUSE_VIDEO2_KEY, video2PausedAtInSec);
            }
        }
    }

    public void loadState(Bundle bundle) {
        if (bundle == null || !bundle.getBoolean(HAS_VIDEO_STATE_KEY)) {
            return;
        }

        videoMuted = bundle.getBoolean(VIDEO_MUTED_KEY);

        String video1Uri = bundle.getString(URI_VIDEO1_KEY);
        if (video1Uri == null) {
            setVideo1(null);
        } else {
            setVideo1(Uri.parse(video1Uri));

            double pauseTimeVideo1 = bundle.getDouble(PAUSE_VIDEO1_KEY);
            if (pauseTimeVideo1 != 0.0) {
                pauseVideo1(pauseTimeVideo1);
            }

            video1State = State.values()[bundle.getInt(STATE_VIDEO1_KEY)];
        }

        String video2Uri = bundle.getString(URI_VIDEO2_KEY);

        if (video2Uri == null) {
            setVideo2(null);
        } else {
            setVideo2(Uri.parse(video2Uri));

            double pauseTimeVideo2 = bundle.getDouble(PAUSE_VIDEO2_KEY);
            if (pauseTimeVideo2 != 0.0) {
                pauseVideo2(pauseTimeVideo2);
            }

            video2State = State.values()[bundle.getInt(STATE_VIDEO2_KEY)];
        }
    }

    /**
     * Determines if the play button should be shown. That is a video is loaded and not playing atm.
     *
     * @return true If the application should show the button.
     */
    public boolean shouldShowPlayButton() {
        if (getVideo1() != null || getVideo2() != null) {
            return !(isVideo1Playing() || isVideo2Playing());
        } else {
            return false;
        }
    }

    public boolean shouldShowPauseButton() {
        return isVideo1Playing() || isVideo2Playing();
    }

    public boolean shouldShowStopButton() {
        boolean isPlaying = isVideo1Playing() || isVideo2Playing();
        boolean isPaused = getVideo1PauseTime() != null || getVideo2PauseTime() != null;

        return isPlaying || isPaused;
    }

    public void setVideo1Seekable(boolean seekable) {
        video1Seekable = seekable;
    }

    public boolean isVideo1Seekable() {
        return video1Seekable;
    }

    public void setVideo2Seekable(boolean seekable) {
        video2Seekable = seekable;
    }

    public boolean isVideo2Seekable() {
        return video2Seekable;
    }
}

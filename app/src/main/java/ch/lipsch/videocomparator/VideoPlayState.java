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

/**
 * The video play state stores all information about the current state of the playing videos. E.g. are the videos playing, current time, what videos are loaded...
 * This class is not thread-safe: All reads/writes must be done in the main thread.
 */
class VideoPlayState {

    /**
     * A key in the saved state to know that the state has been saved.
     * Otherwise we do not know if null was saved or there was nothing saved at all.
     */
    private static final String HAS_VIDEO_STATE_KEY = "VideoPlayState.hasVideoState";

    private Uri video1 = null;
    private Uri video2 = null;

    @CommonDefinitions.VideoViewState
    private int video1State = CommonDefinitions.VIDEO_VIEW_STATE_EMPTY;
    @CommonDefinitions.VideoViewState
    private int video2State = CommonDefinitions.VIDEO_VIEW_STATE_EMPTY;

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

    private boolean mirrorDrawings = true;
    private boolean drawingEnabled = false;

    /**
     * The video 1 is paused at the given time in seconds.
     *
     * @param pauseTimeInSeconds The pause time in seconds (duration from the start).
     */
    public void pauseVideo1(double pauseTimeInSeconds) {
        video1PausedAtInSec = pauseTimeInSeconds;
        setVideoState(CommonDefinitions.VIDEOVIEW1, CommonDefinitions.VIDEO_VIEW_STATE_PAUSING);
    }

    /**
     * The video 2 is paused at the given time in seconds.
     *
     * @param pauseTimeInSeconds The pause time in seconds (duration from the start).
     */
    public void pauseVideo2(double pauseTimeInSeconds) {
        video2PausedAtInSec = pauseTimeInSeconds;
        setVideoState(CommonDefinitions.VIDEOVIEW2, CommonDefinitions.VIDEO_VIEW_STATE_PAUSING);
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
        return video1State == CommonDefinitions.VIDEO_VIEW_STATE_PLAYING;
    }

    public void setVideoState(@CommonDefinitions.VideoViewIdentifier int videoViewIdentifier, int state) {
        if (videoViewIdentifier == CommonDefinitions.VIDEOVIEW1) {
            video1State = state;
        } else {
            video2State = state;
        }

    }

    public boolean isVideo2Playing() {
        return video2State == CommonDefinitions.VIDEO_VIEW_STATE_PLAYING;
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
        setVideoState(CommonDefinitions.VIDEOVIEW1, CommonDefinitions.VIDEO_VIEW_STATE_LOADED);
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
        setVideoState(CommonDefinitions.VIDEOVIEW2, CommonDefinitions.VIDEO_VIEW_STATE_LOADED);
        video2PausedAtInSec = null;
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

    public void setShouldShowMirrorDrawings(boolean mirrorDrawings) {
        this.mirrorDrawings = mirrorDrawings;
    }

    public boolean shouldShowMirrorDrawings() {
        return this.mirrorDrawings;
    }

    public void setDrawingEnabled(boolean drawingEnabled) {
        this.drawingEnabled = drawingEnabled;
    }

    public boolean isDrawingEnabled() {
        return drawingEnabled;
    }

    public boolean isVideoLoaded(@CommonDefinitions.VideoViewIdentifier int videoViewIdentifier) {
        if (videoViewIdentifier == CommonDefinitions.VIDEOVIEW1) {
            return getVideo1() != null;
        } else {
            return getVideo2() != null;
        }
    }
}

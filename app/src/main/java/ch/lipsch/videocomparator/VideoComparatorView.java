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
 * The view interface of the video comparator (MVP).
 */
public interface VideoComparatorView {
    /**
     * Starts to play the two video views.
     */
    void playVideos();

    /**
     * Pauses both video views.
     */
    void pauseVideos();

    /**
     * Stops both video views.
     */
    void stopVideos();

    /**
     * Sets the mute button state: muted or unmuted.
     *
     * @param muted If true the new state is muted and the button is set to unmute.
     */
    void setMuteButtonState(boolean muted);

    /**
     * Sets the mirror drawings button.
     *
     * @param mirrored If true the button will show a do not mirror image / text.
     */
    void setDrawingsMirrorButtonState(boolean mirrored);

    /**
     * Sets the play button visibilty.
     *
     * @param visible If true the button is visible.
     */
    void setPlayButtonVisibility(boolean visible);

    /**
     * Sets the pause button visibilty.
     *
     * @param visible If true the button is visible.
     */
    void setPauseButtonVisibility(boolean visible);

    /**
     * Sets the stop button visibilty.
     *
     * @param visible If true the button is visible.
     */
    void setStopButtonVisibility(boolean visible);

    /**
     * Sets the visibility of a seekbar.
     *
     * @param videoViewIdentifier Identifies the seekbar (video view) whose visibility will be changed.
     * @param visible             If true the seekbar will be visible.
     */
    void setSeekbarVisibility(@CommonDefinitions.VideoViewIdentifier int videoViewIdentifier, boolean visible);

    /**
     * Sets the visibility of a video time text.
     *
     * @param videoViewIdentifier Identifies the time text (video view) whose visibility will be changed.
     * @param visible             If true the video time text will be visible.
     */
    void setVideoTimeVisibility(@CommonDefinitions.VideoViewIdentifier int videoViewIdentifier, boolean visible);

    /**
     * Sets the visibility of a load video button.
     *
     * @param videoViewIdentifier Identifies the load button (video view) whose visibility will be changed.
     * @param visible             If true the load button will be visible.
     */
    void setLoadVideoButtonVisibility(@CommonDefinitions.VideoViewIdentifier int videoViewIdentifier, boolean visible);

    /**
     * Sets the visibility of the en/disable drawing buttons.
     *
     * @param visible If true the enable drawings button is visible else the disable drawings button is visible.
     */
    void setDrawingsEnabledButtonState(boolean visible);

    /**
     * Mutes / unmutes the videos.
     *
     * @param muted If true videos will be muted.
     */
    void muteVideos(boolean muted);

    /**
     * (Un)loads a video in a video view.
     * As a side effect the currently loaded video is stored in VIDEO_PLAY_STATE
     *
     * @param videoToPlay         The uri to be played. In case the uri is null the video is unloaded.
     * @param videoViewIdentifier The video view in which to load the video.
     */
    void loadVideo(Uri videoToPlay, @CommonDefinitions.VideoViewIdentifier int videoViewIdentifier);
}

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
 * This is the presenter interface (MVP).
 */
public interface VideoComparatorPresenter {

    /**
     * Called when the gui requests to load a the provided video.
     *
     * @param videoToPlay         The uri to play.
     * @param videoViewIdentifier Identifies the video view in which the video must be played.
     */
    void onLoadVideoRequested(Uri videoToPlay, @CommonDefinitions.VideoViewIdentifier int videoViewIdentifier);

    /**
     * Called when from the view when the user pressed the play button.
     */
    void onPlayRequested();

    /**
     * Called when from the view when the user pressed the pause button.
     */
    void onPauseRequested();

    /**
     * Called when from the view when the user pressed the stop button.
     */
    void onStopRequested();

    /**
     * Called when the user request to mute or unmute the videos.
     *
     * @param muted If true the videos will be muted else the videos will be unmuted.
     */
    void onMuteRequested(boolean muted);

    /**
     * In case a video is not seekable (e.g. streamed) this method is called.
     *
     * @param videoViewIdentifier The identifier for the video view which shows the unseekable video.
     */
    void onVideoNotSeekable(@CommonDefinitions.VideoViewIdentifier int videoViewIdentifier);

    /**
     * There are certain cases when the gui request that the visibility of all the buttons should be reevaluated.
     */
    void onShouldUpdateGuiState();

    /**
     * In some cases it is necessary that the presenter redistributes the current mute state.
     * E.g. when a new video is loaded. The view can then mute/unmute the video player.
     */
    void onShouldUpdateMuteState();

    /**
     * Is called when the user touches the video views. This will bring back the controls.
     */
    void onTouch();

    /**
     * Callback after the presenter calls VideoComparatorView.loadVideo(...) and the video could be loaded successfully.
     *
     * @param videoViewIdentifier The identifier of the video view which loaded the video successfully.
     */
    void onVideoLoadSuccess(@CommonDefinitions.VideoViewIdentifier int videoViewIdentifier);

    /**
     * Callback after the presenter calls VideoComparatorView.loadVideo(...) and the video could be loaded successfully.
     *
     * @param videoViewIdentifier The identifier of the video view which failed to load  the video.
     */
    void onVideoLoadError(@CommonDefinitions.VideoViewIdentifier int videoViewIdentifier);

    /**
     * Called when the user wants to en-/disalbe the drawing.
     *
     * @param drawingEnabled If true the user wants to enable the drawing else diable the drawing.
     */
    void onDrawingEnabled(boolean drawingEnabled);
}
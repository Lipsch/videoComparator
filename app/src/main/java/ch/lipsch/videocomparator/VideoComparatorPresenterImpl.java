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
import android.os.Handler;
import android.support.annotation.NonNull;

/**
 * This is the presenter (MVP). This class holds the logic to run the gui part (view -> VideoComparatorView).
 */
public class VideoComparatorPresenterImpl implements VideoComparatorPresenter {

    /**
     * Duration in milliseconds after which the controls become invisible when the videos are playing.
     */
    private static final long INVISIBILITY_TOGGLER_DELAY_MS = 2000;

    /**
     * Stores the current state of the videos. In order to restore it in case of app going to background or device orientation
     */
    private static final VideoPlayState VIDEO_PLAY_STATE = new VideoPlayState();

    /**
     * The view (MVP).
     */
    private VideoComparatorView view;

    /**
     * A handler which makes some ui controls invisible.
     */
    private final Handler invisibilityTogglerHandler = new Handler();
    private final Runnable invisibilityToggler = new Runnable() {
        @Override
        public void run() {
            //When adding a UI control don't forget to add it in makeUiVisible.
            view.setLoadVideoButtonVisibility(CommonDefinitions.VIDEOVIEW1, false);
            view.setLoadVideoButtonVisibility(CommonDefinitions.VIDEOVIEW2, false);
            view.setSeekbarVisibility(CommonDefinitions.VIDEOVIEW1, false);
            view.setSeekbarVisibility(CommonDefinitions.VIDEOVIEW2, false);
            view.setVideoTimeVisibility(CommonDefinitions.VIDEOVIEW1, false);
            view.setVideoTimeVisibility(CommonDefinitions.VIDEOVIEW2, false);
        }
    };

    /**
     * Constructs the presenter.
     *
     * @param view The view which is managed by this presenter.
     */
    public VideoComparatorPresenterImpl(@NonNull VideoComparatorView view) {
        this.view = view;

        restoreState();
        makeUiVisible();
    }

    private void play() {
        VIDEO_PLAY_STATE.setVideo1State(CommonDefinitions.VIDEO_VIEW_STATE_PLAYING);
        VIDEO_PLAY_STATE.setVideo2State(CommonDefinitions.VIDEO_VIEW_STATE_PLAYING);

        view.playVideos();

        updateGuiState();
        triggerUiInvisible();
    }

    private void pause() {
        VIDEO_PLAY_STATE.setVideo1State(CommonDefinitions.VIDEO_VIEW_STATE_PAUSING);
        VIDEO_PLAY_STATE.setVideo2State(CommonDefinitions.VIDEO_VIEW_STATE_PAUSING);

        view.pauseVideos();

        updateGuiState();
        makeUiVisible();
    }

    private void stop() {
        view.stopVideos();

        updateGuiState();
        makeUiVisible();
    }

    private void muteVideos(boolean muted) {
        view.muteVideos(muted);

        VIDEO_PLAY_STATE.setVideoMuted(muted);
        updateGuiState();
    }

    /**
     * Updates the state of widgets. E.g. enables / disables the play button according to current video plays.
     */
    private void updateGuiState() {
        //Action button visibility
        view.setMuteButtonState(VIDEO_PLAY_STATE.isVideoMuted());
        view.setPlayButtonVisibility(VIDEO_PLAY_STATE.shouldShowPlayButton());
        view.setPauseButtonVisibility(VIDEO_PLAY_STATE.shouldShowPauseButton());
        view.setStopButtonVisibility(VIDEO_PLAY_STATE.shouldShowStopButton());
        view.setDrawingsMirrorButtonState(VIDEO_PLAY_STATE.shouldShowMirrorDrawings());

        //Seekbar visibility: Do not show a seek bar where no video is loaded.
        view.setSeekbarVisibility(CommonDefinitions.VIDEOVIEW1, canSeekBarBecomeVisible(CommonDefinitions.VIDEOVIEW1));
        view.setSeekbarVisibility(CommonDefinitions.VIDEOVIEW2, canSeekBarBecomeVisible(CommonDefinitions.VIDEOVIEW2));
    }

    @Override
    public void onLoadVideoRequested(Uri videoToPlay, @CommonDefinitions.VideoViewIdentifier int videoViewIdentifier) {
        //Remember current video
        if (videoViewIdentifier == CommonDefinitions.VIDEOVIEW1) {
            VIDEO_PLAY_STATE.setVideo1(videoToPlay);
            //initially a video is seekable. The media player will push an info in case this is not true.
            VIDEO_PLAY_STATE.setVideo1Seekable(videoToPlay != null);
        } else {
            VIDEO_PLAY_STATE.setVideo2(videoToPlay);

            //initially a video is seekable. The media player will push an info in case this is not true.
            VIDEO_PLAY_STATE.setVideo2Seekable(videoToPlay != null);
        }

        view.loadVideo(videoToPlay, videoViewIdentifier);

        updateGuiState();
    }

    private void makeUiVisible() {
        //Removes any triggerUiInvisible calls.
        invisibilityTogglerHandler.removeCallbacks(invisibilityToggler);

        //When adding new ui controls don't forget to add them in the invisibilityToggler.
        view.setLoadVideoButtonVisibility(CommonDefinitions.VIDEOVIEW1, true);
        view.setLoadVideoButtonVisibility(CommonDefinitions.VIDEOVIEW2, true);

        if (canSeekBarBecomeVisible(CommonDefinitions.VIDEOVIEW1)) {
            view.setSeekbarVisibility(CommonDefinitions.VIDEOVIEW1, true);
            view.setVideoTimeVisibility(CommonDefinitions.VIDEOVIEW1, true);
        }
        if (canSeekBarBecomeVisible(CommonDefinitions.VIDEOVIEW2)) {
            view.setSeekbarVisibility(CommonDefinitions.VIDEOVIEW1, true);
            view.setVideoTimeVisibility(CommonDefinitions.VIDEOVIEW1, true);
        }
    }

    private boolean canSeekBarBecomeVisible(@CommonDefinitions.VideoViewIdentifier int videoViewIdentifier) {
        if (videoViewIdentifier == CommonDefinitions.VIDEOVIEW1) {
            return VIDEO_PLAY_STATE.getVideo1() != null && VIDEO_PLAY_STATE.isVideo1Seekable();
        } else {
            return VIDEO_PLAY_STATE.getVideo2() != null && VIDEO_PLAY_STATE.isVideo2Seekable();
        }

    }

    /**
     * Makes the UI (menu, load buttons, ...) invisible in a short amount of time.
     * This action can be cancelled by calling makeUiVisible.
     * A possible previous call of this method is cancelled.
     */
    private void triggerUiInvisible() {
        //Removes previous calls
        invisibilityTogglerHandler.removeCallbacks(invisibilityToggler);

        invisibilityTogglerHandler.postDelayed(invisibilityToggler, INVISIBILITY_TOGGLER_DELAY_MS);
    }

    /**
     * Restores the video state saved in VIDEO_PLAY_STATE.
     */
    private void restoreState() {
        onLoadVideoRequested(VIDEO_PLAY_STATE.getVideo1(), CommonDefinitions.VIDEOVIEW1);
        onLoadVideoRequested(VIDEO_PLAY_STATE.getVideo2(), CommonDefinitions.VIDEOVIEW2);

        muteVideos(VIDEO_PLAY_STATE.isVideoMuted());

        updateGuiState();
    }

    @Override
    public void onPlayRequested() {
        play();
    }

    @Override
    public void onPauseRequested() {
        pause();
    }

    @Override
    public void onStopRequested() {
        stop();
    }

    @Override
    public void onMuteRequested(boolean muted) {
        view.muteVideos(muted);
    }

    @Override
    public void onVideoNotSeekable(@CommonDefinitions.VideoViewIdentifier int videoViewIdentifier) {
        //Seek should be disabled
        if (videoViewIdentifier == CommonDefinitions.VIDEOVIEW1) {
            VIDEO_PLAY_STATE.setVideo1Seekable(false);
        } else {
            VIDEO_PLAY_STATE.setVideo2Seekable(false);
        }

        updateGuiState();
    }

    @Override
    public void onShouldUpdateGuiState() {
        updateGuiState();
    }

    @Override
    public void onShouldUpdateMuteState() {
        view.muteVideos(VIDEO_PLAY_STATE.isVideoMuted());
    }

    @Override
    public void onTouch() {
        makeUiVisible();
        if (VIDEO_PLAY_STATE.isVideo1Playing() || VIDEO_PLAY_STATE.isVideo2Playing()) {
            triggerUiInvisible();
        }
    }

    @Override
    public void onVideoLoadSuccess(@CommonDefinitions.VideoViewIdentifier int videoViewIdentifier) {
        if (videoViewIdentifier == CommonDefinitions.VIDEOVIEW1) {
            VIDEO_PLAY_STATE.setVideo1State(CommonDefinitions.VIDEO_VIEW_STATE_LOADED);
            makeUiVisible();
        } else {
            VIDEO_PLAY_STATE.setVideo2State(CommonDefinitions.VIDEO_VIEW_STATE_LOADED);
            makeUiVisible();
        }

    }

    @Override
    public void onVideoLoadError(@CommonDefinitions.VideoViewIdentifier int videoViewIdentifier) {
        if (videoViewIdentifier == CommonDefinitions.VIDEOVIEW1) {
            VIDEO_PLAY_STATE.setVideo1State(CommonDefinitions.VIDEO_VIEW_STATE_ERROR);
            makeUiVisible();
        } else {
            VIDEO_PLAY_STATE.setVideo1State(CommonDefinitions.VIDEO_VIEW_STATE_ERROR);
            makeUiVisible();
        }
    }
}

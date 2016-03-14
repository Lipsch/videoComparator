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
     * The view (MVP).
     */
    private VideoComparatorView view;

    private VideoPlayState videoPlayState;

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
    public VideoComparatorPresenterImpl(@NonNull VideoComparatorView view, @NonNull VideoPlayState videoPlayState) {
        this.view = view;
        this.videoPlayState = videoPlayState;

        restoreState();
        makeUiVisible();
    }

    private void play() {
        videoPlayState.setVideoState(CommonDefinitions.VIDEOVIEW1, CommonDefinitions.VIDEO_VIEW_STATE_PLAYING);
        videoPlayState.setVideoState(CommonDefinitions.VIDEOVIEW2, CommonDefinitions.VIDEO_VIEW_STATE_PLAYING);

        view.playVideos();

        updateGuiState();
        triggerUiInvisible();
    }

    private void pause() {
        videoPlayState.setVideoState(CommonDefinitions.VIDEOVIEW1, CommonDefinitions.VIDEO_VIEW_STATE_PAUSING);
        videoPlayState.setVideoState(CommonDefinitions.VIDEOVIEW2, CommonDefinitions.VIDEO_VIEW_STATE_PAUSING);

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

        videoPlayState.setVideoMuted(muted);
        updateGuiState();
    }

    /**
     * Updates the state of widgets. E.g. enables / disables the play button according to current video plays.
     */
    private void updateGuiState() {
        //Action button visibility
        view.setMuteButtonState(videoPlayState.isVideoMuted());
        view.setPlayButtonVisibility(videoPlayState.shouldShowPlayButton());
        view.setPauseButtonVisibility(videoPlayState.shouldShowPauseButton());
        view.setStopButtonVisibility(videoPlayState.shouldShowStopButton());
        view.setDrawingsMirrorButtonState(videoPlayState.shouldShowMirrorDrawings());
        view.setDrawingsEnabledButtonState(videoPlayState.isDrawingEnabled());

        //Seekbar visibility: Do not show a seek bar where no video is loaded.
        view.setSeekbarVisibility(CommonDefinitions.VIDEOVIEW1, canSeekBarBecomeVisible(CommonDefinitions.VIDEOVIEW1));
        view.setSeekbarVisibility(CommonDefinitions.VIDEOVIEW2, canSeekBarBecomeVisible(CommonDefinitions.VIDEOVIEW2));
    }

    @Override
    public void onLoadVideoRequested(Uri videoToPlay, @CommonDefinitions.VideoViewIdentifier int videoViewIdentifier) {
        //Remember current video
        if (videoViewIdentifier == CommonDefinitions.VIDEOVIEW1) {
            videoPlayState.setVideo1(videoToPlay);
            //initially a video is seekable. The media player will push an info in case this is not true.
            videoPlayState.setVideo1Seekable(videoToPlay != null);
        } else {
            videoPlayState.setVideo2(videoToPlay);

            //initially a video is seekable. The media player will push an info in case this is not true.
            videoPlayState.setVideo2Seekable(videoToPlay != null);
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
        }
        if (canSeekBarBecomeVisible(CommonDefinitions.VIDEOVIEW2)) {
            view.setSeekbarVisibility(CommonDefinitions.VIDEOVIEW2, true);
        }

        if (videoPlayState.isVideoLoaded(CommonDefinitions.VIDEOVIEW1)) {
            view.setVideoTimeVisibility(CommonDefinitions.VIDEOVIEW1, true);
        }
        if (videoPlayState.isVideoLoaded(CommonDefinitions.VIDEOVIEW2)) {
            view.setVideoTimeVisibility(CommonDefinitions.VIDEOVIEW2, true);
        }
    }

    private boolean canSeekBarBecomeVisible(@CommonDefinitions.VideoViewIdentifier int videoViewIdentifier) {
        if (videoViewIdentifier == CommonDefinitions.VIDEOVIEW1) {
            return videoPlayState.getVideo1() != null && videoPlayState.isVideo1Seekable();
        } else {
            return videoPlayState.getVideo2() != null && videoPlayState.isVideo2Seekable();
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
     * Restores the video state saved in videoPlayState.
     */
    private void restoreState() {
        onLoadVideoRequested(videoPlayState.getVideo1(), CommonDefinitions.VIDEOVIEW1);
        onLoadVideoRequested(videoPlayState.getVideo2(), CommonDefinitions.VIDEOVIEW2);

        muteVideos(videoPlayState.isVideoMuted());

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
            videoPlayState.setVideo1Seekable(false);
        } else {
            videoPlayState.setVideo2Seekable(false);
        }

        updateGuiState();
    }

    @Override
    public void onShouldUpdateGuiState() {
        updateGuiState();
    }

    @Override
    public void onShouldUpdateMuteState() {
        view.muteVideos(videoPlayState.isVideoMuted());
    }

    @Override
    public void onTouch() {
        makeUiVisible();
        if (videoPlayState.isVideo1Playing() || videoPlayState.isVideo2Playing()) {
            triggerUiInvisible();
        }
    }

    @Override
    public void onVideoLoadSuccess(@CommonDefinitions.VideoViewIdentifier int videoViewIdentifier) {
        if (videoViewIdentifier == CommonDefinitions.VIDEOVIEW1) {
            videoPlayState.setVideoState(CommonDefinitions.VIDEOVIEW1, CommonDefinitions.VIDEO_VIEW_STATE_LOADED);
            makeUiVisible();
        } else {
            videoPlayState.setVideoState(CommonDefinitions.VIDEOVIEW2, CommonDefinitions.VIDEO_VIEW_STATE_LOADED);
            makeUiVisible();
        }

    }

    @Override
    public void onVideoLoadError(@CommonDefinitions.VideoViewIdentifier int videoViewIdentifier) {
        if (videoViewIdentifier == CommonDefinitions.VIDEOVIEW1) {
            videoPlayState.setVideoState(CommonDefinitions.VIDEOVIEW1, CommonDefinitions.VIDEO_VIEW_STATE_ERROR);
            makeUiVisible();
        } else {
            videoPlayState.setVideoState(CommonDefinitions.VIDEOVIEW2, CommonDefinitions.VIDEO_VIEW_STATE_ERROR);
            makeUiVisible();
        }
    }

    @Override
    public void onDrawingEnabled(boolean drawingEnabled) {
        videoPlayState.setDrawingEnabled(drawingEnabled);
        updateGuiState();
    }

    @Override
    public void onVideoEnded(@CommonDefinitions.VideoViewIdentifier int videoViewIdentifier) {
        videoPlayState.setVideoState(videoViewIdentifier, CommonDefinitions.VIDEO_VIEW_STATE_LOADED);

        if (!videoPlayState.isVideo1Playing() && !videoPlayState.isVideo2Playing()) {
            //Both videos are at the end
            makeUiVisible();

            view.setPlayButtonVisibility(true);
            view.setPauseButtonVisibility(false);
            view.setStopButtonVisibility(false);

            //Seek to beginning. In order to be able to play the video again.
            if (videoPlayState.isVideo1Seekable()) {
                view.seekVideoTo(CommonDefinitions.VIDEOVIEW1, 0);
            }
            if (videoPlayState.isVideo2Seekable()) {
                view.seekVideoTo(CommonDefinitions.VIDEOVIEW2, 0);
            }
        }
    }
}

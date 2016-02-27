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

import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class VideoComparatorPresenterImplTest {

    /**
     * Verifies that the video time text is made visible after every touch.
     */
    @Test
    public void videoTimeVisibleAfterTouch() {
        VideoComparatorView viewMock = Mockito.mock(VideoComparatorView.class);
        Uri videoUriMock = Mockito.mock(Uri.class);
        VideoPlayState playState = new VideoPlayState();

        //Can't set the boolean directly in the anonymous class
        final boolean[] lastVideoTimeVisibility1 = {false};
        final boolean[] lastVideoTimeVisibility2 = {false};

        //noinspection ResourceType
        Mockito.doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();

                //Just interested in the last call therefore overwriting the results always.
                if (((Integer) args[0]) == CommonDefinitions.VIDEOVIEW1) {
                    lastVideoTimeVisibility1[0] = (boolean) args[1];
                } else {
                    lastVideoTimeVisibility2[0] = (boolean) args[1];
                }

                return null;
            }
        }).when(viewMock).setVideoTimeVisibility(Mockito.anyInt(), Mockito.anyBoolean());

        VideoComparatorPresenterImpl target = new VideoComparatorPresenterImpl(viewMock, playState);

        //Load videos
        target.onLoadVideoRequested(videoUriMock, CommonDefinitions.VIDEOVIEW1);
        target.onLoadVideoRequested(videoUriMock, CommonDefinitions.VIDEOVIEW2);

        target.onTouch();

        assertTrue(lastVideoTimeVisibility1[0]);
        assertTrue(lastVideoTimeVisibility2[0]);
    }

    @Test
    public void onVideosEndedMakesGuiVisible() {
        //setSomethingVisibility may be called multiple times. Just check the last call.
        final boolean[] lastVideoTimeVisibility1 = {false};
        final boolean[] lastVideoTimeVisibility2 = {false};
        final boolean[] lastLoadVideoButtonVisibility1 = {false};
        final boolean[] lastLoadVideoButtonVisibility2 = {false};
        VideoPlayState videoPlayState = new VideoPlayState();
        VideoComparatorView viewStub = new VideoComparatorViewAdapter() {
            @Override
            public void setVideoTimeVisibility(@CommonDefinitions.VideoViewIdentifier int videoViewIdentifier, boolean visible) {
                if (videoViewIdentifier == CommonDefinitions.VIDEOVIEW1) {
                    lastVideoTimeVisibility1[0] = visible;
                } else {
                    lastVideoTimeVisibility2[0] = visible;
                }
            }

            @Override
            public void setLoadVideoButtonVisibility(@CommonDefinitions.VideoViewIdentifier int videoViewIdentifier, boolean visible) {
                if (videoViewIdentifier == CommonDefinitions.VIDEOVIEW1) {
                    lastLoadVideoButtonVisibility1[0] = visible;
                } else {
                    lastLoadVideoButtonVisibility2[0] = visible;
                }
            }
        };

        Uri videoUriMock = Mockito.mock(Uri.class);

        VideoComparatorPresenterImpl target = new VideoComparatorPresenterImpl(viewStub, videoPlayState);

        target.onLoadVideoRequested(videoUriMock, CommonDefinitions.VIDEOVIEW1);
        target.onLoadVideoRequested(videoUriMock, CommonDefinitions.VIDEOVIEW2);

        target.onVideoEnded(CommonDefinitions.VIDEOVIEW1);
        target.onVideoEnded(CommonDefinitions.VIDEOVIEW2);

        //Verify visibility of some gui elements
        assertTrue(lastVideoTimeVisibility1[0]);
        assertTrue(lastVideoTimeVisibility2[0]);
        assertTrue(lastLoadVideoButtonVisibility1[0]);
        assertTrue(lastLoadVideoButtonVisibility2[0]);
    }

    @Test
    public void onVideosEndedDisablesPauseButton() {
        //setPauseButtonVisibility may be called multiple times. Just check the last call.
        final boolean[] lastPauseVisibility = {true};
        VideoPlayState videoPlayState = new VideoPlayState();
        VideoComparatorView viewStub = new VideoComparatorViewAdapter() {
            @Override
            public void setPauseButtonVisibility(boolean visible) {
                lastPauseVisibility[0] = visible;
            }
        };

        VideoComparatorPresenterImpl target = new VideoComparatorPresenterImpl(viewStub, videoPlayState);

        target.onVideoEnded(CommonDefinitions.VIDEOVIEW1);
        target.onVideoEnded(CommonDefinitions.VIDEOVIEW2);

        assertFalse(lastPauseVisibility[0]);
    }

    @Test
    public void onVideosEndedDisablesStopButton() {
        //setStopButtonVisibility may be called multiple times. Just check the last call.
        final boolean[] lastStopVisibility = {true};
        VideoPlayState videoPlayState = new VideoPlayState();
        VideoComparatorView viewStub = new VideoComparatorViewAdapter() {
            @Override
            public void setStopButtonVisibility(boolean visible) {
                lastStopVisibility[0] = visible;
            }
        };

        VideoComparatorPresenterImpl target = new VideoComparatorPresenterImpl(viewStub, videoPlayState);

        target.onVideoEnded(CommonDefinitions.VIDEOVIEW1);
        target.onVideoEnded(CommonDefinitions.VIDEOVIEW2);

        assertFalse(lastStopVisibility[0]);
    }

    @Test
    public void onVideosEndedEnablesPlayButton() {
        //setPlayButtonVisibility may be called multiple times. Just check the last call.
        final boolean[] lastPlayVisibility = {false};
        VideoPlayState videoPlayState = new VideoPlayState();
        VideoComparatorView viewStub = new VideoComparatorViewAdapter() {
            @Override
            public void setPlayButtonVisibility(boolean visible) {
                lastPlayVisibility[0] = visible;
            }
        };

        VideoComparatorPresenterImpl target = new VideoComparatorPresenterImpl(viewStub, videoPlayState);

        target.onVideoEnded(CommonDefinitions.VIDEOVIEW1);
        target.onVideoEnded(CommonDefinitions.VIDEOVIEW2);

        assertTrue(lastPlayVisibility[0]);
    }

    @Test
    public void onVideosEndedPlayStateIsStopped() {
        VideoComparatorView viewMock = Mockito.mock(VideoComparatorView.class);
        Uri videoUriMock = Mockito.mock(Uri.class);
        VideoPlayState videoPlayState = new VideoPlayState();

        VideoComparatorPresenterImpl target = new VideoComparatorPresenterImpl(viewMock, videoPlayState);

        //Load videos and play
        target.onLoadVideoRequested(videoUriMock, CommonDefinitions.VIDEOVIEW1);
        target.onLoadVideoRequested(videoUriMock, CommonDefinitions.VIDEOVIEW2);
        target.onPlayRequested();

        target.onVideoEnded(CommonDefinitions.VIDEOVIEW1);
        target.onVideoEnded(CommonDefinitions.VIDEOVIEW2);

        assertNull(videoPlayState.getVideo1PauseTime());                       //Not paused
        assertFalse(videoPlayState.isVideo1Playing());                         //Not playing
        assertTrue(videoPlayState.isVideoLoaded(CommonDefinitions.VIDEOVIEW1));//video loaded

        assertNull(videoPlayState.getVideo2PauseTime());
        assertFalse(videoPlayState.isVideo2Playing());
        assertTrue(videoPlayState.isVideoLoaded(CommonDefinitions.VIDEOVIEW2));
    }

    @Test
    public void onVideoEndedVidoesAreSeekedToStart() {
        final int[] lastSeekTime = {-1, -1};
        VideoPlayState videoPlayState = new VideoPlayState();

        VideoComparatorView viewStub = new VideoComparatorViewAdapter() {
            @Override
            public void seekVideoTo(@CommonDefinitions.VideoViewIdentifier int videoview, int timeInMilliseconds) {
                if (videoview == CommonDefinitions.VIDEOVIEW1) {
                    lastSeekTime[0] = timeInMilliseconds;
                } else if (videoview == CommonDefinitions.VIDEOVIEW2) {
                    lastSeekTime[1] = timeInMilliseconds;
                }
            }
        };

        VideoComparatorPresenterImpl target = new VideoComparatorPresenterImpl(viewStub, videoPlayState);

        videoPlayState.setVideo1Seekable(true);
        videoPlayState.setVideo2Seekable(true);

        target.onVideoEnded(CommonDefinitions.VIDEOVIEW1);
        target.onVideoEnded(CommonDefinitions.VIDEOVIEW2);

        assertEquals(0, lastSeekTime[0]);
        assertEquals(0, lastSeekTime[1]);
    }
}
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

        target.setVideo1State(CommonDefinitions.VIDEO_VIEW_STATE_PLAYING);

        assertFalse(target.shouldShowPlayButton());
    }

    public void testShowPlayButtonAllPlaying() {
        VideoPlayState target = new VideoPlayState();

        Uri fakeVideoUri1 = Uri.parse("file:///somefile.avi");
        Uri fakeVideoUri2 = Uri.parse("file:///somefile2.avi");
        target.setVideo1(fakeVideoUri1);
        target.setVideo2(fakeVideoUri2);

        target.setVideo1State(CommonDefinitions.VIDEO_VIEW_STATE_PLAYING);
        target.setVideo2State(CommonDefinitions.VIDEO_VIEW_STATE_PLAYING);

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

        target.setVideo1State(CommonDefinitions.VIDEO_VIEW_STATE_PLAYING);

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

        target.setVideo1State(CommonDefinitions.VIDEO_VIEW_STATE_PLAYING);

        assertTrue(target.shouldShowStopButton());
    }

    public void testShowStopButtonVideoPaused() {
        VideoPlayState target = new VideoPlayState();

        Uri fakeVideoUri = Uri.parse("file:///somefile.avi");
        target.setVideo1(fakeVideoUri);
        target.setVideo2(null);

        target.setVideo1State(CommonDefinitions.VIDEO_VIEW_STATE_PAUSING);
        target.pauseVideo1(1.23);

        assertTrue(target.shouldShowStopButton());
    }

    public void testShowPlayButtonVideoPaused() {
        VideoPlayState target = new VideoPlayState();

        Uri fakeVideoUri = Uri.parse("file:///somefile.avi");
        target.setVideo1(fakeVideoUri);
        target.setVideo2(null);

        target.setVideo1State(CommonDefinitions.VIDEO_VIEW_STATE_PLAYING);
        target.pauseVideo1(1.23);

        assertTrue(target.shouldShowPlayButton());
    }
}

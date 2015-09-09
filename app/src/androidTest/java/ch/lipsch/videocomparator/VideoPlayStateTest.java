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
import android.support.test.runner.AndroidJUnit4;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test for the class VideoPlayState.
 */
@RunWith(AndroidJUnit4.class)
public class VideoPlayStateTest {

    @Test
    public void showPlayButtonNoVideoLoaded() {
        VideoPlayState target = new VideoPlayState();

        target.setVideo1(null);
        target.setVideo2(null);

        Assert.assertFalse(target.shouldShowPlayButton());
    }

    @Test
    public void showPlayButtonOneVideoLoaded() {
        VideoPlayState target = new VideoPlayState();

        Uri fakeVideoUri = Uri.parse("file:///somefile.avi");
        target.setVideo1(fakeVideoUri);
        target.setVideo2(null);

        Assert.assertTrue(target.shouldShowPlayButton());
    }

    @Test
    public void showPlayButtonTwoVideosLoaded() {
        VideoPlayState target = new VideoPlayState();

        Uri fakeVideoUri1 = Uri.parse("file:///somefile.avi");
        Uri fakeVideoUri2 = Uri.parse("file:///somefile2.avi");
        target.setVideo1(fakeVideoUri1);
        target.setVideo2(fakeVideoUri2);

        Assert.assertTrue(target.shouldShowPlayButton());
    }

    @Test
    public void showPlayButtonOnePlaying() {
        VideoPlayState target = new VideoPlayState();

        Uri fakeVideoUri1 = Uri.parse("file:///somefile.avi");
        Uri fakeVideoUri2 = Uri.parse("file:///somefile2.avi");
        target.setVideo1(fakeVideoUri1);
        target.setVideo2(fakeVideoUri2);

        target.setVideo1State(CommonDefinitions.VIDEO_VIEW_STATE_PLAYING);

        Assert.assertFalse(target.shouldShowPlayButton());
    }

    @Test
    public void showPlayButtonAllPlaying() {
        VideoPlayState target = new VideoPlayState();

        Uri fakeVideoUri1 = Uri.parse("file:///somefile.avi");
        Uri fakeVideoUri2 = Uri.parse("file:///somefile2.avi");
        target.setVideo1(fakeVideoUri1);
        target.setVideo2(fakeVideoUri2);

        target.setVideo1State(CommonDefinitions.VIDEO_VIEW_STATE_PLAYING);
        target.setVideo2State(CommonDefinitions.VIDEO_VIEW_STATE_PLAYING);

        Assert.assertFalse(target.shouldShowPlayButton());
    }

    @Test
    public void showPauseButtonNoVideos() {
        VideoPlayState target = new VideoPlayState();

        target.setVideo1(null);
        target.setVideo2(null);

        Assert.assertFalse(target.shouldShowPauseButton());
    }

    @Test
    public void showPauseButtonVideoLoadedNotPlaying() {
        VideoPlayState target = new VideoPlayState();

        Uri fakeVideoUri = Uri.parse("file:///somefile.avi");
        target.setVideo1(fakeVideoUri);
        target.setVideo2(null);

        Assert.assertFalse(target.shouldShowPauseButton());
    }

    @Test
    public void showPauseButtonVideoPlaying() {
        VideoPlayState target = new VideoPlayState();

        Uri fakeVideoUri = Uri.parse("file:///somefile.avi");
        target.setVideo1(fakeVideoUri);
        target.setVideo2(null);

        target.setVideo1State(CommonDefinitions.VIDEO_VIEW_STATE_PLAYING);

        Assert.assertTrue(target.shouldShowPauseButton());
    }

    @Test
    public void showStopButtonNoVideos() {
        VideoPlayState target = new VideoPlayState();

        target.setVideo1(null);
        target.setVideo2(null);

        Assert.assertFalse(target.shouldShowStopButton());
    }

    @Test
    public void showStopButtonVideoLoaded() {
        VideoPlayState target = new VideoPlayState();

        Uri fakeVideoUri = Uri.parse("file:///somefile.avi");
        target.setVideo1(fakeVideoUri);
        target.setVideo2(null);

        Assert.assertFalse(target.shouldShowStopButton());
    }

    @Test
    public void showStopButtonVideoPlaying() {
        VideoPlayState target = new VideoPlayState();

        Uri fakeVideoUri = Uri.parse("file:///somefile.avi");
        target.setVideo1(fakeVideoUri);
        target.setVideo2(null);

        target.setVideo1State(CommonDefinitions.VIDEO_VIEW_STATE_PLAYING);

        Assert.assertTrue(target.shouldShowStopButton());
    }

    @Test
    public void showStopButtonVideoPaused() {
        VideoPlayState target = new VideoPlayState();

        Uri fakeVideoUri = Uri.parse("file:///somefile.avi");
        target.setVideo1(fakeVideoUri);
        target.setVideo2(null);

        target.setVideo1State(CommonDefinitions.VIDEO_VIEW_STATE_PAUSING);
        target.pauseVideo1(1.23);

        Assert.assertTrue(target.shouldShowStopButton());
    }

    @Test
    public void showPlayButtonVideoPaused() {
        VideoPlayState target = new VideoPlayState();

        Uri fakeVideoUri = Uri.parse("file:///somefile.avi");
        target.setVideo1(fakeVideoUri);
        target.setVideo2(null);

        target.setVideo1State(CommonDefinitions.VIDEO_VIEW_STATE_PLAYING);
        target.pauseVideo1(1.23);

        Assert.assertTrue(target.shouldShowPlayButton());
    }
}

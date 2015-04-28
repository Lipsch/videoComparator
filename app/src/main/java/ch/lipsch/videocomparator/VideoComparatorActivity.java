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

import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.VideoView;

/**
 * The main activity which contains the two video controls.
 */
//TODO: Remove the action bar completely?
public class VideoComparatorActivity extends ActionBarActivity {

    /** Request identifier when opening video on the left / top side. */
    private static final int PICK_VIDEO1_REQUEST = 1;

    /** Request identifier when opening video on the right / bottom side. */
    private static final int PICK_VIDEO2_REQUEST = 2;

    /**
     * Stores the current state of the videos. In order to restore it in case of app going to background or device orientation
     */
    private static final VideoPlayState VIDEO_PLAY_STATE = new VideoPlayState();

    private Button loadVideo1Button = null;
    private Button loadVideo2Button = null;

    /** The video control on the left / top side (depending on the device orientation. */
    private VideoView video1 = null;

    /** The video control on the right / bottom side (depending on the device orientation. */
    private VideoView video2 = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        VIDEO_PLAY_STATE.loadState(savedInstanceState);

        //Layout differs depending on the rotation of the device.
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setContentView(R.layout.activity_video_comparator_landscape);
        } else {
            setContentView(R.layout.activity_video_comparator_portrait);
        }

        video1 = (VideoView) findViewById(R.id.video1);
        video2 = (VideoView) findViewById(R.id.video2);

        loadVideo1Button = (Button) findViewById(R.id.loadVideo1Button);
        loadVideo2Button = (Button) findViewById(R.id.loadVideo2Button);

        View.OnTouchListener loadVideoTouchListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    Intent intent = new Intent();
                    intent.setType("video/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);

                    int requestNumber = PICK_VIDEO1_REQUEST;
                    if (view == loadVideo2Button) {
                        requestNumber = PICK_VIDEO2_REQUEST;
                    }

                    startActivityForResult(Intent.createChooser(intent, getString(R.string.select_video)), requestNumber);
                    return true;
                } else {
                    return false;
                }
            }
        };

        loadVideo1Button.setOnTouchListener(loadVideoTouchListener);
        loadVideo2Button.setOnTouchListener(loadVideoTouchListener);

        restoreState();
    }

    /**
     * Restores the video state saved in VIDEO_PLAY_STATE.
     */
    private void restoreState() {
        loadVideo(VIDEO_PLAY_STATE.getVideo1(), video1);
        loadVideo(VIDEO_PLAY_STATE.getVideo2(), video2);
    }

    /**
     * (Un)loads a video in a video view.
     * As a side effect the currently loaded video is stored in VIDEO_PLAY_STATE
     *
     * @param videoToPlay The uri to be played. In case the uri is null the video is unloaded.
     * @param videoView   The video view in which to load the video.
     */
    private void loadVideo(Uri videoToPlay, VideoView videoView) {
        if (videoToPlay == null) {
            //Unload video
            videoView.stopPlayback();
            videoView.setVideoURI(null);
        } else {
            //Load video
            videoView.setVideoURI(videoToPlay);

            //TODO remove this a soon as we have a play / stop button
            videoView.setMediaController(new MediaController(this));
        }

        //Remember current video
        if (videoView == video1) {
            VIDEO_PLAY_STATE.setVideo1(videoToPlay);
        } else {
            VIDEO_PLAY_STATE.setVideo2(videoToPlay);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            //Load video intent emitted from a load video button
            VideoView videoView = video1;

            if (requestCode == PICK_VIDEO2_REQUEST) {
                videoView = video2;
            }

            loadVideo(data.getData(), videoView);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);

        VIDEO_PLAY_STATE.saveState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_video_comparator, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

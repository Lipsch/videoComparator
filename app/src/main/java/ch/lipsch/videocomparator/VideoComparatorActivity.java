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
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.SeekBar;
import android.widget.VideoView;

/**
 * The main activity which contains the two video controls.
 */
//TODO: Remove the action bar completely?
public class VideoComparatorActivity extends AppCompatActivity {

    private static final String TAG = VideoComparatorActivity.class.getName();

    /**
     * Request identifier when opening video on the left / top side.
     */
    private static final int PICK_VIDEO1_REQUEST = 1;

    /**
     * Request identifier when opening video on the right / bottom side.
     */
    private static final int PICK_VIDEO2_REQUEST = 2;

    /**
     * Stores the current state of the videos. In order to restore it in case of app going to background or device orientation
     */
    private static final VideoPlayState VIDEO_PLAY_STATE = new VideoPlayState();

    private Button loadVideo1Button = null;
    private Button loadVideo2Button = null;

    /**
     * The video control on the left / top side (depending on the device orientation.
     */
    private VideoView video1 = null;

    /**
     * The video control on the right / bottom side (depending on the device orientation.
     */
    private VideoView video2 = null;

    private MenuItem actionPlay = null;
    private MenuItem actionPause = null;
    private MenuItem actionStop = null;

    private SeekBar video1SeekBar = null;
    private SeekBar video2SeekBar = null;

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

        video1SeekBar = (SeekBar) findViewById(R.id.seekBarVideo1);
        video2SeekBar = (SeekBar) findViewById(R.id.seekBarVideo2);

        registerVideoListeners();

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

    private void registerVideoListeners() {
        video1.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                //Video 1 finished
                VIDEO_PLAY_STATE.setVideo2State(VideoPlayState.State.LOADED);
            }
        });
        video2.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                //Video2 finished
                VIDEO_PLAY_STATE.setVideo2State(VideoPlayState.State.LOADED);
            }
        });

        video1.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                VIDEO_PLAY_STATE.setVideo1State(VideoPlayState.State.ERROR);

                //false -> let the video view inform the user about errors
                return false;
            }
        });
        video2.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                VIDEO_PLAY_STATE.setVideo2State(VideoPlayState.State.ERROR);

                //false -> let the video view inform the user about errors
                return false;
            }
        });

        video1.setOnInfoListener(new MediaPlayer.OnInfoListener() {
            @Override
            public boolean onInfo(MediaPlayer mp, int what, int extra) {
                boolean isHandled = handleVideoInfo(video1, what);
                updateGuiState();

                return isHandled;
            }
        });

        video2.setOnInfoListener(new MediaPlayer.OnInfoListener() {
            @Override
            public boolean onInfo(MediaPlayer mp, int what, int extra) {
                boolean isHandled = handleVideoInfo(video2, what);
                updateGuiState();

                return isHandled;
            }
        });

        //Mute the videos
        MediaPlayer.OnPreparedListener muteListener = new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.setVolume(0.0f, 0.0f);
            }
        };
        video1.setOnPreparedListener(muteListener);
        video2.setOnPreparedListener(muteListener);
    }

    /**
     * Manages all the infos we got from the InfoListener from the two videos.
     * Have a look at registerVideoListeners where the InfoListeners are registered.
     *
     * @param videoView The video view which emitted the info.
     * @param what      The information constant. Corresponds to MediaPlayer.MEDIA_INFO_xxx
     * @return true if the information should be treated as handled (see InfoListener.onInfo).
     */
    private boolean handleVideoInfo(VideoView videoView, int what) {
        boolean isHandled = false;
        switch (what) {
            //TODO: Think about implementing states: MediaPlayer.MEDIA_INFO_BUFFERING_START, MEDIA_INFO_BUFFERING_END
            case MediaPlayer.MEDIA_INFO_NOT_SEEKABLE:
                Log.i(TAG, getVideoViewNameForLogging(videoView) + " is not seekable");

                //Seek should be disabled
                if (videoView == video1) {
                    VIDEO_PLAY_STATE.setVideo1Seekable(false);
                } else if (videoView == video2) {
                    VIDEO_PLAY_STATE.setVideo2Seekable(false);
                }
                isHandled = true;

            default:
                isHandled = false;

        }

        return isHandled;
    }

    private String getVideoViewNameForLogging(VideoView videoView) {
        if (videoView == video1) {
            return "'video 1'";
        } else if (videoView == video2) {
            return "'video 2'";
        } else {
            return "'unknown video'";
        }
    }

    /**
     * Restores the video state saved in VIDEO_PLAY_STATE.
     */
    private void restoreState() {
        loadVideo(VIDEO_PLAY_STATE.getVideo1(), video1);
        loadVideo(VIDEO_PLAY_STATE.getVideo2(), video2);

        updateGuiState();
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
            //initially a video is seekable. The media player will push an info in case this is not true.
            VIDEO_PLAY_STATE.setVideo1Seekable(videoToPlay != null);
        } else {
            VIDEO_PLAY_STATE.setVideo2(videoToPlay);

            //initially a video is seekable. The media player will push an info in case this is not true.
            VIDEO_PLAY_STATE.setVideo2Seekable(videoToPlay != null);
        }

        updateGuiState();
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

        //Need to remember them due to the visibility changes.
        actionPlay = menu.findItem(R.id.action_play);
        actionPause = menu.findItem(R.id.action_pause);
        actionStop = menu.findItem(R.id.action_stop);

        updateGuiState();

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            //TODO show settings.
            return true;
        } else if (id == R.id.action_play) {
            playVideos();
            return true;
        } else if (id == R.id.action_pause) {
            pauseVideos();
            return true;
        } else if (id == R.id.action_stop) {
            stopVideos();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void playVideos() {
        video1.start();
        video2.start();
        VIDEO_PLAY_STATE.setVideo1State(VideoPlayState.State.PLAYING);
        VIDEO_PLAY_STATE.setVideo2State(VideoPlayState.State.PLAYING);

        updateGuiState();
    }

    private void pauseVideos() {
        video1.pause();
        video2.pause();
        VIDEO_PLAY_STATE.setVideo1State(VideoPlayState.State.PAUSING);
        VIDEO_PLAY_STATE.setVideo2State(VideoPlayState.State.PAUSING);

        updateGuiState();
    }

    private void stopVideos() {
        //Stop playback unloads the video...
        video1.stopPlayback();
        video2.stopPlayback();
        loadVideo(VIDEO_PLAY_STATE.getVideo1(), video1);
        loadVideo(VIDEO_PLAY_STATE.getVideo2(), video2);

        VIDEO_PLAY_STATE.setVideo1State(VideoPlayState.State.LOADED);
        VIDEO_PLAY_STATE.setVideo2State(VideoPlayState.State.LOADED);

        updateGuiState();
    }

    /**
     * Updates the state of widgets. E.g. enables / disables the play button according to current video plays.
     */
    private void updateGuiState() {
        //Action button visibility
        if (actionPlay != null) {
            actionPlay.setVisible(VIDEO_PLAY_STATE.shouldShowPlayButton());
        }

        if (actionPause != null) {
            actionPause.setVisible(VIDEO_PLAY_STATE.shouldShowPauseButton());
        }

        if (actionStop != null) {
            actionStop.setVisible(VIDEO_PLAY_STATE.shouldShowStopButton());
        }

        //Seekbar visibility
        if (VIDEO_PLAY_STATE.getVideo1() != null && VIDEO_PLAY_STATE.isVideo1Seekable()) {
            video1SeekBar.setVisibility(View.VISIBLE);
        } else {
            video1SeekBar.setVisibility(View.INVISIBLE);
        }

        if (VIDEO_PLAY_STATE.getVideo2() != null && VIDEO_PLAY_STATE.isVideo2Seekable()) {
            video2SeekBar.setVisibility(View.VISIBLE);
        } else {
            video2SeekBar.setVisibility(View.INVISIBLE);
        }
    }
}

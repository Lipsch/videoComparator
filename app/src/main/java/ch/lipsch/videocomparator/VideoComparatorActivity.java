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
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;

import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ch.lipsch.videocomparator.drawing.DrawingManager;
import ch.lipsch.videocomparator.drawing.DrawingView;

/**
 * The main activity which contains the two video controls.
 */
public class VideoComparatorActivity extends AppCompatActivity implements VideoComparatorView {

    private static final String TAG = VideoComparatorActivity.class.getName();

    /**
     * Request identifier when opening video on the left / top side.
     */
    private static final int PICK_VIDEO1_REQUEST = 1;

    /**
     * Request identifier when opening video on the right / bottom side.
     */
    private static final int PICK_VIDEO2_REQUEST = 2;

    public static final int SEEK_BAR_UPDATE_DELAY_MS = 1000;

    @Bind(R.id.loadVideo1Button)
    protected Button loadVideo1Button = null;

    @Bind(R.id.loadVideo2Button)
    protected Button loadVideo2Button = null;

    /**
     * The video control on the left / top side (depending on the device orientation.
     */
    @Bind(R.id.video1)
    protected VideoView video1 = null;

    @Bind(R.id.drawingView1)
    protected DrawingView drawingView1 = null;

    /**
     * The video control on the right / bottom side (depending on the device orientation.
     */
    @Bind(R.id.video2)
    protected VideoView video2 = null;
    @Bind(R.id.drawingView2)
    protected DrawingView drawingView2 = null;

    private DrawingManager drawingManager = null;

    private MenuItem actionMirrorDrawings = null;
    private MenuItem actionDoNotMirrorDrawings = null;
    private MenuItem actionPlay = null;
    private MenuItem actionPause = null;
    private MenuItem actionStop = null;
    private MenuItem actionMute = null;
    private MenuItem actionUnmute = null;
    private MenuItem actionEnableDrawings = null;
    private MenuItem actionDisableDrawings = null;

    @Bind(R.id.seekBarVideo1)
    protected SeekBar video1SeekBar = null;

    @Bind(R.id.seekBarVideo2)
    protected SeekBar video2SeekBar = null;

    @Bind(R.id.timeVideo1)
    protected TextView videoTime1 = null;

    @Bind(R.id.timeVideo2)
    protected TextView videoTime2 = null;

    //The media players of the two video views. They are needed to mute / unmute the videos.
    // This reference is needed because it is not possible at any moment to get the media player of
    // a video view (see OnPreparedListener).
    private MediaPlayer video1Player = null;
    private MediaPlayer video2Player = null;

    //We need to remember the last played video due to the issue that when a playing video is stopped the video is unloaded.
    //So after stopping a video the original URI must be reloaded again. See loadVideo(...) and stopVideos() method.
    private Uri lastPlayedOnVideo1 = null;
    private Uri lastPlayedOnVideo2 = null;

    /**
     * A handler which updates the position of the video seek bars. In case this variable is set to null no further updates will be done (e.g. on destroy).
     */
    private Handler seekBarUpdater = null;

    /**
     * The presenter which does all the logic. Gui events are propagated to the presenter who processes it.
     */
    private VideoComparatorPresenter presenter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Layout differs depending on the rotation of the device.
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setContentView(R.layout.activity_video_comparator_landscape);
        } else {
            setContentView(R.layout.activity_video_comparator_portrait);
        }

        ButterKnife.bind(this);

        presenter = new VideoComparatorPresenterImpl(this, ((VideoComparatorApplication) getApplication()).getVideoPlayState());

        registerVideoListeners();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //Make the ui visible on any touch and if playing make it invisible again.
        // The presenter will do the needed logic.
        presenter.onTouch();

        return false;
    }

    @Override
    protected void onStart() {
        super.onStart();

        initializeSeekBarsAndTime();
        initializeDrawingViews();
    }

    @Override
    protected void onStop() {
        super.onStop();

        //This will stop updating the seek bars.
        seekBarUpdater = null;

        try {
            drawingManager.close();
        } catch (Exception e) {
            Log.e(TAG, "Could not close drawing manager", e);
        }
        drawingManager = null;
    }

    private void initializeDrawingViews() {
        drawingManager = new DrawingManager(drawingView1, drawingView2);
    }

    /**
     * Starts the seek bar / time updater and listeners for user input
     */
    private void initializeSeekBarsAndTime() {
        SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    double multiplier = (double) progress / seekBar.getMax();

                    VideoView videoView = getVideoViewFor(seekBar);

                    if (videoView != null) {

                        int duration = videoView.getDuration();

                        int seekTo = (int) (duration * multiplier);

                        videoView.seekTo(seekTo);
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //Nothing to do
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //Nothing to do
            }
        };

        video1SeekBar.setOnSeekBarChangeListener(seekBarChangeListener);
        video2SeekBar.setOnSeekBarChangeListener(seekBarChangeListener);

        //This updates the seek bars regularly.
        seekBarUpdater = new Handler();
        seekBarUpdater.postDelayed(new Runnable() {
            @Override
            public void run() {
                //Update the seekbar again in the future
                if (seekBarUpdater != null) {
                    correctVideoSeek(video1SeekBar);
                    correctVideoSeek(video2SeekBar);

                    seekBarUpdater.postDelayed(this, SEEK_BAR_UPDATE_DELAY_MS);
                }
            }
        }, SEEK_BAR_UPDATE_DELAY_MS);
    }

    /**
     * Corrects the progress of the given seek bar according to the current played video.
     * The time field which belongs to the given seek bar is corrected too.
     *
     * @param seekBar The seek bar to adjust.
     */
    private void correctVideoSeek(SeekBar seekBar) {
        VideoView videoView = getVideoViewFor(seekBar);

        if (videoView != null) {
            //Progress is from 0 to 100
            int duration = videoView.getDuration();
            if (duration == -1) { //Seems the duration is -1 if no video is loaded. (not API documented!)
                seekBar.setProgress(0);
            } else {
                int currentPos = videoView.getCurrentPosition();
                double multiplier = (double) currentPos / duration;

                //Math.ceil because otherwise we would never reach 100% and the seek bar jumps back when manually set.
                int seekTo = (int) Math.ceil(seekBar.getMax() * multiplier);

                seekBar.setProgress(seekTo);

                TextView timeField = getTimeFieldFor(seekBar);
                if (timeField != null) {
                    //Formatted milliseconds to 0:21:55 -> h:mm:ss
                    timeField.setText(String.format("%d:%02d:%02d",
                            TimeUnit.MILLISECONDS.toHours(currentPos),
                            TimeUnit.MILLISECONDS.toMinutes(currentPos) % 60,
                            TimeUnit.MILLISECONDS.toSeconds(currentPos) % 60));
                }
            }
        }
    }

    private TextView getTimeFieldFor(SeekBar seekBar) {
        if (seekBar == video1SeekBar) {
            return videoTime1;
        } else if (seekBar == video2SeekBar) {
            return videoTime2;
        }

        return null;
    }

    private VideoView getVideoViewFor(SeekBar seekBar) {
        if (seekBar == video1SeekBar) {
            return video1;
        } else if (seekBar == video2SeekBar) {
            return video2;
        }

        return null;
    }

    private void registerVideoListeners() {
        video1.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                //Video 1 finished
                presenter.onVideoLoadSuccess(CommonDefinitions.VIDEOVIEW1);
            }
        });
        video2.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                //Video2 finished
                presenter.onVideoLoadSuccess(CommonDefinitions.VIDEOVIEW2);
            }
        });

        video1.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                presenter.onVideoLoadError(CommonDefinitions.VIDEOVIEW1);

                //false -> let the video view inform the user about errors
                return false;
            }
        });
        video2.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                presenter.onVideoLoadError(CommonDefinitions.VIDEOVIEW2);

                //false -> let the video view inform the user about errors
                return false;
            }
        });

        video1.setOnInfoListener(new MediaPlayer.OnInfoListener() {
            @Override
            public boolean onInfo(MediaPlayer mp, int what, int extra) {
                return handleVideoInfo(video1, what);
            }
        });

        video2.setOnInfoListener(new MediaPlayer.OnInfoListener() {
            @Override
            public boolean onInfo(MediaPlayer mp, int what, int extra) {
                return handleVideoInfo(video2, what);
            }
        });


        //Mute / unmute the videos before playing.
        //Haven't found a possibility to get the media play to mute / unmute the video.
        //Therefore saving it within the listener

        MediaPlayer.OnPreparedListener muteListenerVideo1 = new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                video1Player = mp;
                presenter.onShouldUpdateMuteState();
            }
        };
        MediaPlayer.OnPreparedListener muteListenerVideo2 = new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                video2Player = mp;
                presenter.onShouldUpdateMuteState();
            }
        };
        video1.setOnPreparedListener(muteListenerVideo1);
        video2.setOnPreparedListener(muteListenerVideo2);
    }

    /**
     * Shows a file selection activitiy when on one the load video buttons if pressed.
     *
     * @param openVideoFileButton The pressed button
     */
    @OnClick({R.id.loadVideo1Button, R.id.loadVideo2Button})
    protected void openVideoFileTouched(Button openVideoFileButton) {
        Intent intent = new Intent();
        intent.setType("video/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);

        int requestNumber = PICK_VIDEO1_REQUEST;
        if (openVideoFileButton == loadVideo2Button) {
            requestNumber = PICK_VIDEO2_REQUEST;
        }

        startActivityForResult(Intent.createChooser(intent, getString(R.string.select_video)), requestNumber);
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
                    presenter.onVideoNotSeekable(CommonDefinitions.VIDEOVIEW1);
                } else if (videoView == video2) {
                    presenter.onVideoNotSeekable(CommonDefinitions.VIDEOVIEW2);
                }
                isHandled = true;
                break;
            case MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START:
                //Remove the background (first frame) the video is now rendered.
                //Which was set during loading the video. -> loadVideo(...)
                videoView.setBackground(null);

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

    @Override
    public void loadVideo(Uri videoToPlay, @CommonDefinitions.VideoViewIdentifier int videoViewIdentifier) {
        VideoView videoView = null;

        //Remember current video
        if (videoViewIdentifier == CommonDefinitions.VIDEOVIEW1) {
            videoView = video1;
            video1Player = null;
            lastPlayedOnVideo1 = videoToPlay;
        } else {
            videoView = video2;
            video2Player = null;
            lastPlayedOnVideo2 = videoToPlay;
        }

        if (videoToPlay == null) {
            //Unload video
            videoView.stopPlayback();
            videoView.setVideoURI(null);
        } else {
            //Load video
            videoView.setVideoURI(videoToPlay);

            // By default the first frame of the loaded video is not shown. Setting the "thumb" as the background image.
            // The background image is removed in the onPrepareListener: See registerVideoListeners().
            Bitmap thumb = ThumbnailUtils.createVideoThumbnail(videoToPlay.getPath(),
                    MediaStore.Images.Thumbnails.FULL_SCREEN_KIND);

            BitmapDrawable bitmapDrawable = new BitmapDrawable(null, thumb);
            videoView.setBackground(bitmapDrawable);
        }
    }

    @Override
    public void seekVideoTo(int videoViewIdentifier, int timeInMilliseconds) {
        VideoView videoView = null;

        //Remember current video
        if (videoViewIdentifier == CommonDefinitions.VIDEOVIEW1) {
            videoView = video1;
        } else {
            videoView = video2;
        }

        videoView.seekTo(timeInMilliseconds);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            //Load video intent emitted from a load video button
            @CommonDefinitions.VideoViewIdentifier int videoViewIdent = CommonDefinitions.VIDEOVIEW1;

            if (requestCode == PICK_VIDEO2_REQUEST) {
                videoViewIdent = CommonDefinitions.VIDEOVIEW2;
            }

            presenter.onLoadVideoRequested(data.getData(), videoViewIdent);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_video_comparator, menu);

        //Need to remember them due to the visibility changes.
        //Can't be bound by butter knife because menu is not available during startup.
        actionPlay = menu.findItem(R.id.action_play);
        actionPause = menu.findItem(R.id.action_pause);
        actionStop = menu.findItem(R.id.action_stop);
        actionMute = menu.findItem(R.id.action_mute);
        actionUnmute = menu.findItem(R.id.action_unmute);
        actionMirrorDrawings = menu.findItem(R.id.action_mirrorDraws);
        actionDoNotMirrorDrawings = menu.findItem(R.id.action_doNotMirrorDraws);
        actionEnableDrawings = menu.findItem(R.id.action_enable_Draws);
        actionDisableDrawings = menu.findItem(R.id.action_disable_Draws);

        presenter.onShouldUpdateGuiState();

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
            presenter.onPlayRequested();
            return true;
        } else if (id == R.id.action_pause) {
            presenter.onPauseRequested();
            return true;
        } else if (id == R.id.action_stop) {
            presenter.onStopRequested();
            return true;
        } else if (id == R.id.action_mute) {
            presenter.onMuteRequested(true);
            return true;
        } else if (id == R.id.action_unmute) {
            presenter.onMuteRequested(false);
            return true;
        } else if (id == R.id.action_clearDraws) {
            if (drawingManager != null) {
                drawingManager.clearDraws();
                return true;
            }
        } else if (id == R.id.action_pickColor) {
            //TODO pick Color dialog
            return true;
        } else if (id == R.id.action_mirrorDraws || id == R.id.action_doNotMirrorDraws) {
            //toggle mirror drawings state
            if (drawingManager != null) {
                drawingManager.setIsMirroring(!drawingManager.isMirroring());
                presenter.onShouldUpdateGuiState();
            }
            return true;
        } else if (id == R.id.action_enable_Draws) {
            //Enable drawing on video screen
            if (drawingManager != null) {
                drawingManager.enableDrawings(true);
            }
            presenter.onDrawingEnabled(true);
        } else if (id == R.id.action_disable_Draws) {
            //Disable drawing on video screen
            //Enable drawing on video screen
            if (drawingManager != null) {
                drawingManager.enableDrawings(false);
            }
            presenter.onDrawingEnabled(false);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void muteVideos(boolean muted) {
        float volume = 0.0f;
        if (!muted) {
            volume = 1.0f;
        }

        if (video1Player != null) {
            video1Player.setVolume(volume, volume);
        }
        if (video2Player != null) {
            video2Player.setVolume(volume, volume);
        }
    }


    @Override
    public void playVideos() {
        video1.start();
        video2.start();
    }

    @Override
    public void pauseVideos() {
        video1.pause();
        video2.pause();
    }

    @Override
    public void stopVideos() {
        //Stop playback unloads the video. Therefore we have to load the video again.
        //In order to be able to press the play button again without having to load the video manually.
        video1.stopPlayback();
        video2.stopPlayback();
        loadVideo(lastPlayedOnVideo1, CommonDefinitions.VIDEOVIEW1);
        loadVideo(lastPlayedOnVideo2, CommonDefinitions.VIDEOVIEW2);
    }

    @Override
    public void setMuteButtonState(boolean muted) {
        if (actionMute != null) {
            actionMute.setVisible(muted);
        }
        if (actionUnmute != null) {
            actionUnmute.setVisible(!muted);
        }
    }

    @Override
    public void setDrawingsMirrorButtonState(boolean mirrored) {
        if (actionMirrorDrawings != null && actionDoNotMirrorDrawings != null) {
            if (drawingManager != null && !drawingManager.isMirroring()) {
                actionMirrorDrawings.setVisible(!mirrored);
                actionDoNotMirrorDrawings.setVisible(mirrored);
            } else {
                actionMirrorDrawings.setVisible(mirrored);
                actionDoNotMirrorDrawings.setVisible(!mirrored);
            }
        }
    }

    @Override
    public void setPlayButtonVisibility(boolean visible) {
        if (actionPlay != null) {
            actionPlay.setVisible(visible);
        }
    }

    @Override
    public void setPauseButtonVisibility(boolean visible) {
        if (actionPause != null) {
            actionPause.setVisible(visible);
        }
    }

    @Override
    public void setStopButtonVisibility(boolean visible) {
        if (actionStop != null) {
            actionStop.setVisible(visible);
        }
    }

    @Override
    public void setSeekbarVisibility(@CommonDefinitions.VideoViewIdentifier int videoViewIdentifier, boolean visible) {
        if (videoViewIdentifier == CommonDefinitions.VIDEOVIEW1) {
            if (visible) {
                video1SeekBar.setVisibility(View.VISIBLE);
            } else {
                video1SeekBar.setVisibility(View.INVISIBLE);
            }
        } else {
            if (visible) {
                video2SeekBar.setVisibility(View.VISIBLE);
            } else {
                video2SeekBar.setVisibility(View.INVISIBLE);
            }
        }
    }

    @Override
    public void setVideoTimeVisibility(@CommonDefinitions.VideoViewIdentifier int videoViewIdentifier, boolean visible) {
        if (videoViewIdentifier == CommonDefinitions.VIDEOVIEW1) {
            if (visible) {
                videoTime1.setVisibility(View.VISIBLE);
            } else {
                videoTime1.setVisibility(View.INVISIBLE);
            }
        } else {
            if (visible) {
                videoTime2.setVisibility(View.VISIBLE);
            } else {
                videoTime2.setVisibility(View.INVISIBLE);
            }
        }
    }

    @Override
    public void setLoadVideoButtonVisibility(@CommonDefinitions.VideoViewIdentifier int videoViewIdentifier, boolean visible) {
        if (videoViewIdentifier == CommonDefinitions.VIDEOVIEW1) {
            if (visible) {
                loadVideo1Button.setVisibility(View.VISIBLE);
            } else {
                loadVideo1Button.setVisibility(View.INVISIBLE);
            }
        } else {
            if (visible) {
                loadVideo2Button.setVisibility(View.VISIBLE);
            } else {
                loadVideo2Button.setVisibility(View.INVISIBLE);
            }
        }
    }

    @Override
    public void setDrawingsEnabledButtonState(boolean visible) {
        if (actionEnableDrawings != null) {
            actionEnableDrawings.setVisible(!visible);
        }
        if (actionDisableDrawings != null) {
            actionDisableDrawings.setVisible(visible);
        }
    }
}

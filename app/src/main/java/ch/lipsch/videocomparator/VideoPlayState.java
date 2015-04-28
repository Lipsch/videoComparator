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
import android.os.Bundle;

/**
 * The video play state stores all information about the current state of the playing videos. E.g. are the videos playing, current time, what videos are loaded...
 * This class is not thread-safe: All reads/writes must be done in the main thread.
 */
class VideoPlayState {

    private static final String URI_VIDEO1_KEY = "VideoPlayState.uriVideo1";
    private static final String URI_VIDEO2_KEY = "VideoPlayState.uriVideo2";

    /**
     * A key in the saved state to know that the state has been saved.
     * Otherwise we do not know if null was saved or there was nothing saved at all.
     */
    private static final String HAS_VIDEO_STATE_KEY = "VideoPlayState.hasVideoState";

    private Uri video1 = null;
    private Uri video2 = null;

    public Uri getVideo1() {
        return video1;
    }

    public void setVideo1(Uri video1) {
        this.video1 = video1;
    }

    public Uri getVideo2() {
        return video2;
    }

    public void setVideo2(Uri video2) {
        this.video2 = video2;
    }

    public void saveState(Bundle bundle) {
        bundle.putBoolean(HAS_VIDEO_STATE_KEY, true);

        if (video1 == null) {
            bundle.putString(URI_VIDEO1_KEY, null);
        } else {
            bundle.putString(URI_VIDEO1_KEY, video1.toString());
        }

        if (video2 == null) {
            bundle.putString(URI_VIDEO2_KEY, null);
        } else {
            bundle.putString(URI_VIDEO2_KEY, video2.toString());
        }
    }

    public void loadState(Bundle bundle) {
        if (bundle == null || !bundle.getBoolean(HAS_VIDEO_STATE_KEY)) {
            return;
        }

        String video1Uri = bundle.getString(URI_VIDEO1_KEY);
        if (video1Uri == null) {
            setVideo1(null);
        } else {
            setVideo1(Uri.parse(video1Uri));
        }

        String video2Uri = bundle.getString(URI_VIDEO2_KEY);

        if (video2Uri == null) {
            setVideo2(null);
        } else {
            setVideo2(Uri.parse(video2Uri));
        }
    }
}

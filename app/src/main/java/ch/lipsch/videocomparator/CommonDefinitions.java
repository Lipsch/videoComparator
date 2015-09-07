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

import android.support.annotation.IntDef;

/**
 * This file contains global definitions, enums (replaced by @IntDef).
 */
public final class CommonDefinitions {

    public static final int VIDEOVIEW1 = 1;
    public static final int VIDEOVIEW2 = 2;

    /**
     * An "enum" which identifies a certain video view.
     * Horizontal layout: left -> VIDEOVIEW1; right -> VIDEOVIEW2
     * Vertical layout: upper -> VIDEOVIEW1; lower -> VIDEOVIEW2
     */
    @IntDef({VIDEOVIEW1, VIDEOVIEW2})
    public @interface VideoViewIdentifier {
    }



    public static final int VIDEO_VIEW_STATE_EMPTY = 0;
    public static final int VIDEO_VIEW_STATE_LOADED = 1;
    public static final int VIDEO_VIEW_STATE_PLAYING = 2;
    public static final int VIDEO_VIEW_STATE_PAUSING = 3;
    public static final int VIDEO_VIEW_STATE_ERROR = 4;

    /**
     * An "enum" replacement. The two video views can only be in one of the following states:
     * EMPTY, LOADED, PLAYING, PAUSING, ERROR
     */
    @IntDef({
            VIDEO_VIEW_STATE_EMPTY,
            VIDEO_VIEW_STATE_LOADED,
            VIDEO_VIEW_STATE_PLAYING,
            VIDEO_VIEW_STATE_PAUSING,
            VIDEO_VIEW_STATE_ERROR})
    public @interface VideoViewState {
    }
}

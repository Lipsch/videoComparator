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

import static org.junit.Assert.assertTrue;

public class VideoComparatorPresenterImplTest {

    /**
     * Verifies that the video time text is made visible after every touch.
     */
    @Test
    public void videoTimeVisibleAfterTouch() {
        VideoComparatorView viewMock = Mockito.mock(VideoComparatorView.class);
        Uri videoUriMock = Mockito.mock(Uri.class);

        VideoComparatorPresenterImpl target = new VideoComparatorPresenterImpl(viewMock);

        //Load videos
        target.onLoadVideoRequested(videoUriMock, CommonDefinitions.VIDEOVIEW1);
        target.onLoadVideoRequested(videoUriMock, CommonDefinitions.VIDEOVIEW2);

        target.onTouch();

        Mockito.verify(viewMock).setVideoTimeVisibility(CommonDefinitions.VIDEOVIEW1, true);
        Mockito.verify(viewMock).setVideoTimeVisibility(CommonDefinitions.VIDEOVIEW2, true);

        assertTrue(true);
    }
}
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

package ch.lipsch.videocomparator.drawing;

import android.graphics.Path;

/**
 * The drawing manager is responsible for the drawing on the two drawing views.
 * E.g. if the drawings are duplicated, the color, ... All this information is
 * provided to the drawing views.
 * All calls must be done within the gui thread.
 */
public final class DrawingManager implements AutoCloseable, DrawingViewObserver {

    private final DrawingView drawingView1;
    private final DrawingView drawingView2;

    private boolean isMirroring = true;

    /**
     * Creates a new drawing manager. The provided drawing views must not be null!
     *
     * @param drawingView1 The first drawing view.
     * @param drawingView2 The second drawing view.
     * @throws NullPointerException In case one of the two drawing views is null.
     */
    public DrawingManager(DrawingView drawingView1, DrawingView drawingView2) {
        if (drawingView1 == null) {
            throw new NullPointerException("drawingView1 must not be null");
        }
        if (drawingView2 == null) {
            throw new NullPointerException("drawingView2 must not be null");
        }

        this.drawingView1 = drawingView1;
        this.drawingView2 = drawingView2;

        this.drawingView1.addObserver(this);
        this.drawingView2.addObserver(this);
    }

    @Override
    public void close() throws Exception {
        drawingView1.deleteObserver(this);
        drawingView2.deleteObserver(this);
    }

    @Override
    public void update(DrawingView observable, Path path) {
        if (isMirroring) {
            drawingView1.drawPath(path);
            drawingView2.drawPath(path);
        } else {
            observable.drawPath(path);
        }
    }

    public void setIsMirroring(boolean isMirroring) {
        this.isMirroring = isMirroring;
        clearDraws();
    }

    public boolean isMirroring() {
        return isMirroring;
    }

    public void clearDraws() {
        drawingView1.clearDraws();
        drawingView2.clearDraws();
    }
}
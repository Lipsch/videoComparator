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

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;

/**
 * A drawing view is used as an overlay to draw lines on a transparent view.
 * All drawing action can be observed by a DrawingViewObserver. The observer then decides if and what is drawn.
 * All calls must be done in the gui thread.
 */
public class DrawingView extends View {

    private final ArrayList<DrawingViewObserver> observers = new ArrayList<>();

    private Path path = new Path();
    private Paint paint = new Paint();

    public DrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);

        paint.setAntiAlias(true);
        paint.setStrokeWidth(5f);
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
    }

    public DrawingView(Context context) {
        this(context, null);
    }


    public void addObserver(DrawingViewObserver observer) {
        observers.add(observer);
    }

    public void deleteObserver(DrawingViewObserver observer) {
        observers.remove(observer);
    }

    private void notifyObservers(Path path) {
        for (DrawingViewObserver observer : observers) {
            observer.update(this, path);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Path newPath = new Path(this.path);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                newPath.moveTo(event.getX(), event.getY());
                break;
            case MotionEvent.ACTION_MOVE:
                newPath.lineTo(event.getX(), event.getY());
                break;
            default:
                return false;
        }

        //deliver new path to observer which decides where it should be painted
        notifyObservers(newPath);

        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawPath(path, paint);
    }

    public void drawPath(Path path) {
        this.path = path;
        invalidate();
    }

    public void clearDraws() {
        this.path = new Path();
        invalidate();
    }
}

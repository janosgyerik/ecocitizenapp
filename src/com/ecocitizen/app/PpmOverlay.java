/*
 * Copyright (C) 2010 Mobile Environmental Sensing For Sustainable Cities
 *
 * This file is part of EcoCitizen.
 *
 * EcoCitizen is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * EcoCitizen is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with EcoCitizen.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.ecocitizen.app;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

public class PpmOverlay extends Overlay {
	private final Bitmap bmp;
	private final GeoPoint gpoint;

	public PpmOverlay(Bitmap bmp, GeoPoint gp) {
		this.bmp = bmp;
		this.gpoint = gp;
	}

	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		Projection pro = mapView.getProjection();
		Point p = pro.toPixels(gpoint, null);
		canvas.drawBitmap(bmp, p.x, p.y, null);
	}
}

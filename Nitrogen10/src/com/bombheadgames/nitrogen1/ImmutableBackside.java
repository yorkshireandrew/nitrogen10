package com.bombheadgames.nitrogen1;

import java.io.Serializable;

public class ImmutableBackside implements Serializable{
	private static final long serialVersionUID = -7547901468152037764L;

	// view-space offset from the Items origin
	final float ix, iy, iz;
	
	// the backsides normal coordinates
	final float inx, iny, inz;
	
	// Causes the backside to perform additional calculations used for lighting
	final boolean calculateLighting;
	
	ImmutableBackside(
			final float ix,
			final float iy,
			final float iz,
			final float inx,
			final float iny,
			final float inz,
			boolean calculateLighting)
			{
				this.ix = ix;
				this.iy = iy;
				this.iz = iz;
				this.inx = inx;
				this.iny = iny;
				this.inz = inz;
				this.calculateLighting = calculateLighting;
			}
}

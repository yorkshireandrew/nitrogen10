package nitrogen1;

public class PolygonClipper {
	
	static final int SH = 20; //limits aux coordinates to the range 0-2048
	static final int NUM = 1 << SH;
	static final float NUM_FLOAT = (float)NUM;
	
	// Enumerated values for the pass
	static final int NEAR_PASS = 0;
	static final int RIGHT_PASS = 1;
	static final int LEFT_PASS = 2;
	static final int TOP_PASS = 3;
	static final int BOTTOM_PASS = 4;
	static final int RENDER_PASS = 5;

	
	/** enumerated value used to define which plane of the view fustrum is being processed */
	static int pass;	
	
	/** constant used for computing clipCase */
	static final int ONE = 1;
	/** constant used for computing clipCase */	
	static final int TWO = 2;
	/** constant used for computing clipCase */	
	static final int FOUR = 4;
	/** constant used for computing clipCase */
	static final int EIGHT = 8;
	
	/** Field used to generate Vertexs that occur at intersect points */
	static Vert[] workingVertexs  = new Vert[12];
	/** Field used to generate Vertexs that occur at intersect points */
	static int workingVertexIndex = 0;
	
	
	/** Clips a polygon against view fustrum. Then passes the clipped polygons on to the nextProcess
	 * 
	 * @param context 	The NitrogenContext the polygons will finally be rendered into
	 * <br/><br/>
	 * @param fustrumTouchCount	Count of the number of clip planes the polygon touches
	 * @param touchedNear The polygon may touch the near plane
	 * @param touchedRight The polygon may touch the right plane
	 * @param touchedLeft The polygon may touch the left plane
	 * @param touchedTop The polygon may touch the top plane
	 * @param touchedBottom The polygon may touch the bottom plane
	 * <br/><br/>
	 * @param vert1 1st polygon vertex. The four Vertexes must be in clockwise order
	 * @param vert2 2nd polygon vertex.
	 * @param vert3 3rd polygon vertex.
	 * @param vert4 4th polygon vertex.
	 * <br/><br/>
	 * @param renderer Renderer to use to render the polygon.
	 * @param polyData Polygon data to pass to the Renderer, such as its colour.
	 * @param texMap TextureMap top pass to the Renderer.
	 * @param lightingValue lighting value that may have been computed by the polygons backside to pass to the Renderer
	 * <br/><br/>
	 * @param v11-v34 The orientation matrix computed by the scene graph (12 floating point values)
	 */	
	static void process(

			NitrogenContext context,
			int fustrumTouchCount, 
			boolean touchedNear,
			boolean touchedRight,
			boolean touchedLeft,
			boolean touchedTop,
			boolean touchedBottom,
			
			Vert vert1, 
			Vert vert2, 
			Vert vert3, 
			Vert vert4,
		
			Renderer renderer,
			int[] polyData,
			TexMap textureMap,
			float lightingValue,
			boolean useHLPBreak
			)
	{
		if(fustrumTouchCount == 0)
		{
			// the polygon does not need clipping so pass it on
			HLPBreaker.process(
					context,
					fustrumTouchCount, touchedNear,touchedRight,touchedLeft,touchedTop,touchedBottom,
					vert1, vert2, vert3, vert4,									
					renderer, polyData, textureMap, lightingValue, useHLPBreak
					);
		}
		else
		{
			// Begin clipping the polygon
			pass = NEAR_PASS;
			clipPolygon(
					pass,
					context,
					fustrumTouchCount, touchedNear, touchedRight, touchedLeft, touchedTop, touchedBottom,
					vert1, vert2, vert3, vert4,					
					renderer, polyData, textureMap, lightingValue, useHLPBreak 
					);
		}
	}
	
	//*********************************************************
	//*********************************************************
	//****                                                 ****
	//****          START OF CLIP POLYGON METHOD           ****
	//****                                                 ****
	//*********************************************************
	//*********************************************************
	
	/** Reentrant method. This method clips a polygon against one plane of the view fustrum (selected by pass)<br /><br /> It then calls itself again to clip any resultant polygons from the clipping process against the next fustrum plane available. 
	 * Then finally when there are no more fustrum planes it passes the polygons on to the next process in the polygon rendering pipeline.
	 * @param pass		 		An enumerated integer used to define which plane of the view fustrum is being processed. It is also related to call deapth in this reentrant method.
	 * @param context 			The NitrogenContext the polygons will finally be rendered into
	 * <br/><br/>
	 * @param fustrumTouchCount	Count of the remaining number of clip planes the polygon may touch
	 * @param touchedNear 		The polygon may touch the near plane
	 * @param touchedRight 		The polygon may touch the right plane
	 * @param touchedLeft 		The polygon may touch the left plane
	 * @param touchedTop 		The polygon may touch the top plane
	 * @param touchedBottom 	The polygon may touch the bottom plane
	 * <br/><br/>
	 * @param vert1 			1st polygon vertex. The four Vertexes must be in clockwise order
	 * @param vert2 			2nd polygon vertex.
	 * @param vert3 			3rd polygon vertex.
	 * @param vert4 			4th polygon vertex.
	 * <br/><br/>
	 * @param renderer			The renderer for the polygon.
	 * @param polyData			Polygon data to pass to the renderer such as its colour.
	 * @param texMap			The texture map for the polygon if used.
	 * @param lightingValue lighting value that may have been computed by the polygons backside to pass to the Renderer
	 */	
	static void clipPolygon(
			int pass,
			NitrogenContext context,
			int fustrumTouchCount, 
			boolean touchedNear,
			boolean touchedRight,
			boolean touchedLeft,
			boolean touchedTop,
			boolean touchedBottom,
			
			Vert vert1, 
			Vert vert2, 
			Vert vert3, 
			Vert vert4,
			
			Renderer renderer,
			int[] polyData,
			TexMap textureMap,
			float lightingValue,
			boolean useHLPBreak
			)
		{
			if((fustrumTouchCount == 0)||(pass == RENDER_PASS))
			{
				// if no further clipping is needed then pass the polygon on to the next process
				HLPBreaker.process(
						context,
						fustrumTouchCount, touchedNear, touchedRight, touchedLeft, touchedTop, touchedBottom,						
						vert1, vert2, vert3, vert4,
						renderer, polyData, textureMap, lightingValue, useHLPBreak
						);
			}
			else
			{
				// Item touched flags acceleration
				if(
						((pass == NEAR_PASS)	&& !touchedNear)	||
						((pass == RIGHT_PASS)	&& !touchedRight) 	||
						((pass == LEFT_PASS)	&& !touchedLeft) 	||
						((pass == TOP_PASS)		&& !touchedTop) 	||
						((pass == BOTTOM_PASS)	&& !touchedBottom)
					)
				{
					// Item flags indicate the polygon cannot be clipping against this plane so do the next reentrant call
					clipPolygon(
							(pass+1),
							context,
							fustrumTouchCount, touchedNear, touchedRight, touchedLeft,touchedTop,touchedBottom,							
							vert1, vert2, vert3, vert4,							
							renderer,polyData,textureMap,lightingValue,useHLPBreak); 							
				}
				else
				{
					// we know we must testing for clipping on this 
					// fustrum plane so decrement fustrumTouchCount
					fustrumTouchCount--;
					
					/** local variable used to calculate clip case */
					int clipCase = 0;
					
					/** local intersect vertex (abcd = clockwise)*/
					Vert verta;
					/** local intersect vertex (abcd = clockwise)*/
					Vert vertb;
					/** local intersect vertex (abcd = clockwise)*/
					Vert vertc;
					/** local intersect vertex (abcd = clockwise)*/
					Vert vertd;
					
					if(isVertexCulled(vert1, context, pass))clipCase |= ONE;
					if(isVertexCulled(vert2, context, pass))clipCase |= TWO;
					if(isVertexCulled(vert3, context, pass))clipCase |= FOUR;
					if(isVertexCulled(vert4, context, pass))clipCase |= EIGHT;
					
					switch(clipCase)
					{
						case 0:
							clipPolygon(
									(pass+1),
									context,
									fustrumTouchCount, touchedNear, touchedRight, touchedLeft, touchedTop, touchedBottom,							
									vert1, vert2, vert3, vert4,							
									renderer, polyData,textureMap,lightingValue,useHLPBreak
									);
							return;
						
						case 1:
							verta = calculateIntersect(vert4, vert1, context, pass);
							vertb = calculateIntersect(vert2, vert1, context, pass);
							clipPolygon(
									(pass+1),
									context,
									fustrumTouchCount, touchedNear, touchedRight, touchedLeft, touchedTop, touchedBottom,							
									vertb, vert2, vert3, verta,							
									renderer, polyData,textureMap,lightingValue,useHLPBreak
									);
							clipPolygon(
									(pass+1),
									context,
									fustrumTouchCount, touchedNear, touchedRight, touchedLeft, touchedTop, touchedBottom,							
									verta, verta, vert3, vert4,							
									renderer, polyData,textureMap,lightingValue,useHLPBreak
									);
							return;
							
						case 2:
							verta = calculateIntersect(vert1, vert2, context, pass);
							vertb = calculateIntersect(vert3, vert2, context, pass);
							clipPolygon(
									(pass+1),
									context,
									fustrumTouchCount, touchedNear, touchedRight, touchedLeft, touchedTop, touchedBottom,							
									verta, vertb, vert3, vert4,							
									renderer, polyData,textureMap,lightingValue,useHLPBreak
									);
							clipPolygon(
									(pass+1),
									context,
									fustrumTouchCount, touchedNear, touchedRight, touchedLeft, touchedTop, touchedBottom,							
									verta, verta, vert4, vert1,							
									renderer, polyData,textureMap,lightingValue,useHLPBreak
									);							
							return;
							
						case 3:
							verta = calculateIntersect(vert4, vert1, context, pass);
							vertb = calculateIntersect(vert3, vert2, context, pass);
							clipPolygon(
									(pass+1),
									context,
									fustrumTouchCount, touchedNear, touchedRight, touchedLeft, touchedTop, touchedBottom,							
									verta, vertb, vert3, vert4,							
									renderer, polyData,textureMap,lightingValue,useHLPBreak
									);					
							return;
							
						case 4:
							verta = calculateIntersect(vert2, vert3, context, pass);
							vertb = calculateIntersect(vert4, vert3, context, pass);
							clipPolygon(
									(pass+1),
									context,
									fustrumTouchCount, touchedNear, touchedRight, touchedLeft, touchedTop, touchedBottom,							
									verta, vertb, vert4, vert1,							
									renderer, polyData,textureMap,lightingValue,useHLPBreak
									);
							clipPolygon(
									(pass+1),
									context,
									fustrumTouchCount, touchedNear, touchedRight, touchedLeft, touchedTop, touchedBottom,							
									verta, verta, vert1, vert2,							
									renderer, polyData,textureMap,lightingValue,useHLPBreak
									);							
							return;
							
						case 5:
							verta = calculateIntersect(vert4, vert1, context, pass);
							vertb = calculateIntersect(vert2, vert1, context, pass);
							vertc = calculateIntersect(vert2, vert3, context, pass);
							vertd = calculateIntersect(vert4, vert3, context, pass);
							clipPolygon(
									(pass+1),
									context,
									fustrumTouchCount, touchedNear, touchedRight, touchedLeft, touchedTop, touchedBottom,							
									verta, vertb, vert2, vertc,							
									renderer, polyData,textureMap,lightingValue,useHLPBreak
									);
							clipPolygon(
									(pass+1),
									context,
									fustrumTouchCount, touchedNear, touchedRight, touchedLeft, touchedTop, touchedBottom,							
									verta, vertc, vertd, vert4,							
									renderer, polyData,textureMap,lightingValue,useHLPBreak
									);							
							return;

						case 6:
							verta = calculateIntersect(vert1, vert2, context, pass);
							vertb = calculateIntersect(vert4, vert3, context, pass);
							clipPolygon(
									(pass+1),
									context,
									fustrumTouchCount, touchedNear, touchedRight, touchedLeft, touchedTop, touchedBottom,							
									vert1, verta, vertb, vert4,							
									renderer, polyData,textureMap,lightingValue,useHLPBreak
									);						
							return;
							
						case 7:
							verta = calculateIntersect(vert4, vert1, context, pass);
							vertb = calculateIntersect(vert4, vert3, context, pass);
							clipPolygon(
									(pass+1),
									context,
									fustrumTouchCount, touchedNear, touchedRight, touchedLeft, touchedTop, touchedBottom,							
									verta, verta, vertb, vert4,							
									renderer, polyData,textureMap,lightingValue,useHLPBreak
									);						
							return;
							
						case 8:
							verta = calculateIntersect(vert3, vert4, context, pass);
							vertb = calculateIntersect(vert1, vert4, context, pass);
							clipPolygon(
									(pass+1),
									context,
									fustrumTouchCount, touchedNear, touchedRight, touchedLeft, touchedTop, touchedBottom,							
									vert1, vert2, vert3, verta,							
									renderer, polyData,textureMap,lightingValue,useHLPBreak
									);
							clipPolygon(
									(pass+1),
									context,
									fustrumTouchCount, touchedNear, touchedRight, touchedLeft, touchedTop, touchedBottom,							
									verta, verta, vertb, vert1,							
									renderer, polyData,textureMap,lightingValue,useHLPBreak
									);							
							return;
							
						case 9:
							verta = calculateIntersect(vert2, vert1, context, pass);
							vertb = calculateIntersect(vert3, vert4, context, pass);
							clipPolygon(
									(pass+1),
									context,
									fustrumTouchCount, touchedNear, touchedRight, touchedLeft, touchedTop, touchedBottom,							
									verta, vert2, vert3, vertb,							
									renderer, polyData,textureMap,lightingValue,useHLPBreak
									);						
							return;
							
						case 10:
							verta = calculateIntersect(vert1, vert2, context, pass);
							vertb = calculateIntersect(vert3, vert2, context, pass);
							vertc = calculateIntersect(vert3, vert4, context, pass);
							vertd = calculateIntersect(vert1, vert4, context, pass);
							clipPolygon(
									(pass+1),
									context,
									fustrumTouchCount, touchedNear, touchedRight, touchedLeft, touchedTop, touchedBottom,							
									vert1, verta, vertb, vertd,							
									renderer, polyData,textureMap,lightingValue,useHLPBreak
									);
							clipPolygon(
									(pass+1),
									context,
									fustrumTouchCount, touchedNear, touchedRight, touchedLeft, touchedTop, touchedBottom,							
									vertd, vertb, vert3, vertc,							
									renderer, polyData,textureMap,lightingValue,useHLPBreak
									);
							return;
						
						case 11:
							verta = calculateIntersect(vert3, vert2, context, pass);
							vertb = calculateIntersect(vert3, vert4, context, pass);
							clipPolygon(
									(pass+1),
									context,
									fustrumTouchCount, touchedNear, touchedRight, touchedLeft, touchedTop, touchedBottom,							
									verta, verta, vert3, vertb,							
									renderer, polyData,textureMap,lightingValue,useHLPBreak
									);						
							return;
							
						case 12:
							verta = calculateIntersect(vert2, vert3, context, pass);
							vertb = calculateIntersect(vert1, vert4, context, pass);
							clipPolygon(
									(pass+1),
									context,
									fustrumTouchCount, touchedNear, touchedRight, touchedLeft, touchedTop, touchedBottom,							
									vert1, vert2, verta, vertb,							
									renderer, polyData,textureMap,lightingValue,useHLPBreak
									);						
							return;
							
						case 13:
							verta = calculateIntersect(vert2, vert1, context, pass);
							vertb = calculateIntersect(vert2, vert3, context, pass);
							clipPolygon(
									(pass+1),
									context,
									fustrumTouchCount, touchedNear, touchedRight, touchedLeft, touchedTop, touchedBottom,							
									verta, verta, vert2, vertb,							
									renderer, polyData,textureMap,lightingValue,useHLPBreak
									);						
							return;
							
						case 14:
							verta = calculateIntersect(vert1, vert2, context, pass);
							vertb = calculateIntersect(vert1, vert4, context, pass);
							clipPolygon(
									(pass+1),
									context,
									fustrumTouchCount, touchedNear, touchedRight, touchedLeft, touchedTop, touchedBottom,							
									vert1, vert1, verta, vertb,							
									renderer, polyData,textureMap,lightingValue,useHLPBreak
									);						
							return;
							
						case 15:
							return;						
					}	
				}// end of Item touched flags if-else
			}//end of RENDER_PASS if-else
		}
	//*********************************************************
	//*********************************************************
	//****                                                 ****
	//****          END OF CLIP POLYGON METHOD             ****
	//****                                                 ****
	//*********************************************************
	//*********************************************************
	
	/** Calculates if a Vertex is culled by one of the view fustrum planes. 
	 * @param	vertex	The vertex object to be checked. 
	 * @param 	context The NitrogenContext that contains the view-fustrum information.
	 * @param 	pass 	An enumerated integer used to define which plane of the view fustrum to clip against.
	 * */
	static final boolean isVertexCulled(Vert vertex, NitrogenContext context, int pass)
	{
		float deapth_from_viewpoint = -vertex.vs_z;
		switch(pass)
		{
			case NEAR_PASS:			
			return((deapth_from_viewpoint - context.nearClip) < 0);
			
			case RIGHT_PASS:
			return(vertex.vs_x > (deapth_from_viewpoint * context.xClip));

			case LEFT_PASS:
			return(-vertex.vs_x > (deapth_from_viewpoint * context.xClip));
			
			case TOP_PASS:
			return(vertex.vs_y > (deapth_from_viewpoint * context.yClip));	
			
			case BOTTOM_PASS:
			return(-vertex.vs_y > (deapth_from_viewpoint * context.yClip));
			
			case RENDER_PASS:
			return(false);
			
			default:
			return(false);			
		}
	}
	
	/** Generates a vertex, that lies on a line between two parameter vertexes that intersects a given plane of the view fustrum. 
	 * @param in 		The vertex that lies inside the given plane of the view fustrum.
	 * @param out 		The vertex that lies outside the given plane of the view fustrum, so is not visible.
	 * @param context 	The NitrogenContext that defines the view fustrum.
	 * @param pass 		An enumerated integer used to define which plane of the view fustrum is being processed.
	 * @return 			A new vertex object that lies on the line where it intersects the view-fustrum plane.
	 */
	static final Vert calculateIntersect(Vert in, Vert out, NitrogenContext context, int pass)
	{
		float in_deapth = -in.vs_z;
		float out_deapth = -out.vs_z;
		
		/** Proportion of the distance toward the out vertex from in vertex */
		float n;
		/** Cached local value related to view fustrum's field-of-view */
		float k;
		/** The generated vertex view-space coordinates */
		float vvsx, vvsy,vvsz;
		/** The generated vertex aux values */
		int va1, va2, va3;
		
		// create a new output vertex
		Vert returnval = workingVertexs[workingVertexIndex];
		workingVertexIndex++;
		
		switch(pass)
		{
			case NEAR_PASS:
				// note any rounding down of n causes resulting vertex to be more in view
				n = (context.nearClip - in_deapth)/(out_deapth - in_deapth);
				return(generateInbetweenVertex(in,out,n));			
			
			case RIGHT_PASS:
				k = context.xClip;
				n = (k * in_deapth - in.vs_x) / ((out.vs_x - in.vs_x) - k * (out_deapth - in_deapth));
				return(generateInbetweenVertex(in,out,n));	
				
			case LEFT_PASS:
				k = context.xClip;
				
				// calculation similar to RIGHT_PASS above, but the x coordinates are inverted
				n = (k * in_deapth + in.vs_x) / ((in.vs_x - out.vs_x) - k * (out_deapth - in_deapth));
				return(generateInbetweenVertex(in,out,n));	
			
			case TOP_PASS:
				k = context.yClip;
				n = (k * in_deapth - in.vs_y) / ((out.vs_y - in.vs_y) - k * (out_deapth - in_deapth));
				return(generateInbetweenVertex(in,out,n));	
			
			case BOTTOM_PASS:
				k = context.yClip;
				
				// calculation similar to TOP_PASS above, but the y coordinates are inverted
				n = (k * in_deapth + in.vs_y) / ((in.vs_y - out.vs_y) - k * (out_deapth - in_deapth));
				return(generateInbetweenVertex(in,out,n));
			
			default:
			return(null);			
		}
	}
	
	/** Returns a working vertex that is situated along a given proportion of the line between first and second vertexes
	 * @param first First vertex
	 * @param second Second vertex
	 * @param n The proportion parameter (expected to be in the range 0 ... 1)
	 * @return The vertex that lies at proportion n along the line between first and second vertex parameters
	 */
	static final Vert generateInbetweenVertex(Vert first, Vert second, float n)
	{
		// create a new output vertex
		Vert retval = workingVertexs[workingVertexIndex];
		workingVertexIndex++;
		
		/** The generated vertex view-space coordinates */
		float vvsx, vvsy,vvsz;
		/** The generated vertex aux values */
		float va1, va2, va3;
		
		vvsx = (second.vs_x - first.vs_x) * n + first.vs_x;
		vvsy = (second.vs_y - first.vs_y) * n + first.vs_y;
		vvsz = (second.vs_z - first.vs_z) * n + first.vs_z;
		va1  = (second.aux1 - first.aux1) * n + first.aux1;
		va2  = (second.aux2 - first.aux2) * n + first.aux2;
		va3  = (second.aux3 - first.aux3) * n + first.aux3;
		retval.setViewSpaceAndAux(vvsx, vvsy, vvsz, va1, va2, va3);
		return retval;	
	}
	
	static void prepareForNewPolygon()
	{
		workingVertexIndex = 0;
	}
}

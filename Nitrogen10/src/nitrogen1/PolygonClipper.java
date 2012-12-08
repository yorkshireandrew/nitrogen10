package nitrogen1;

final class PolygonClipper {
	
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
//	static int pass;	
	
	/** constant used for computing clipCase */
	static final int ONE = 1;
	/** constant used for computing clipCase */	
	static final int TWO = 2;
	/** constant used for computing clipCase */	
	static final int FOUR = 4;
	/** constant used for computing clipCase */
	static final int EIGHT = 8;
	
	/** Field used to generate Vertexes that occur at intersect points */
	static final Vertex[] workingVertexes;
	/** Field used to generate Vertexes that occur at intersect points */
	static int workingVertexIndex = 0;
	
	static{ 
		int BUFFER_SIZE = 64;
		workingVertexes = new Vertex[BUFFER_SIZE];
		for(int i = 0 ; i < BUFFER_SIZE; i++)workingVertexes[i]= new Vertex();
		}
	
	
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
	 * @param vertex1 1st polygon vertex. The four Vertexes must be in clockwise order
	 * @param vertex2 2nd polygon vertex.
	 * @param vertex3 3rd polygon vertex.
	 * @param vertex4 4th polygon vertex.
	 * <br/><br/>
	 * @param renderer Renderer to use to render the polygon.
	 * @param polyData Polygon data to pass to the Renderer, such as its colour.
	 * @param texMap TextureMap top pass to the Renderer.
	 * @param lightingValue lighting value that may have been computed by the polygons backside to pass to the Renderer
	 * <br/><br/>
	 * @param v11-v34 The orientation matrix computed by the scene graph (12 floating point values)
	 */	
	final static void process(

			final NitrogenContext context,
			final int fustrumTouchCount, 
			final boolean touchedNear,
			final boolean touchedRight,
			final boolean touchedLeft,
			final boolean touchedTop,
			final boolean touchedBottom,
			
			final Vertex vertex1, 
			final Vertex vertex2, 
			final Vertex vertex3, 
			final Vertex vertex4,
		
			final Renderer renderer,
			final int[] polyData,
			final TexMap textureMap,
			final float lightingValue,
			final boolean useHLPBreak
			)
	{
		context.polygonsRendered++;
		if(fustrumTouchCount == 0)
		{
			// the polygon does not need clipping so pass it on
			HLPBinaryBreaker.process(
					context,
					fustrumTouchCount, touchedNear,touchedRight,touchedLeft,touchedTop,touchedBottom,
					vertex1, vertex2, vertex3, vertex4,									
					renderer, polyData, textureMap, lightingValue, useHLPBreak
					);
		}
		else
		{
			// Begin clipping the polygon
			clipPolygon(
					NEAR_PASS,
					context,
					fustrumTouchCount, touchedNear, touchedRight, touchedLeft, touchedTop, touchedBottom,
					vertex1, vertex2, vertex3, vertex4,					
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
	 * @param vertex1 			1st polygon vertex. The four Vertexes must be in clockwise order
	 * @param vertex2 			2nd polygon vertex.
	 * @param vertex3 			3rd polygon vertex.
	 * @param vertex4 			4th polygon vertex.
	 * <br/><br/>
	 * @param renderer			The renderer for the polygon.
	 * @param polyData			Polygon data to pass to the renderer such as its colour.
	 * @param texMap			The texture map for the polygon if used.
	 * @param lightingValue lighting value that may have been computed by the polygons backside to pass to the Renderer
	 */	
	final static void clipPolygon(
			final int pass,
			final NitrogenContext context,
				  int fustrumTouchCount, 
			final boolean touchedNear,
			final boolean touchedRight,
			final boolean touchedLeft,
			final boolean touchedTop,
			final boolean touchedBottom,
			
			final Vertex vertex1, 
			final Vertex vertex2, 
			final Vertex vertex3, 
			final Vertex vertex4,
			
			final Renderer renderer,
			final int[] polyData,
			final TexMap textureMap,
			final float lightingValue,
			final boolean useHLPBreak
			)
		{
			if((fustrumTouchCount == 0)||(pass == RENDER_PASS))
			{
				// if no further clipping is needed then pass the polygon on to the next process
				HLPBinaryBreaker.process(
						context,
						fustrumTouchCount, touchedNear, touchedRight, touchedLeft, touchedTop, touchedBottom,						
						vertex1, vertex2, vertex3, vertex4,
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
							vertex1, vertex2, vertex3, vertex4,							
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
					Vertex vertexa;
					/** local intersect vertex (abcd = clockwise)*/
					Vertex vertexb;
					/** local intersect vertex (abcd = clockwise)*/
					Vertex vertexc;
					/** local intersect vertex (abcd = clockwise)*/
					Vertex vertexd;
					
					if(isVertexCulled(vertex1, context, pass))clipCase |= ONE;
					if(isVertexCulled(vertex2, context, pass))clipCase |= TWO;
					if(isVertexCulled(vertex3, context, pass))clipCase |= FOUR;
					if(isVertexCulled(vertex4, context, pass))clipCase |= EIGHT;
					
					System.out.println("CLIP POLYGON");
					System.out.println("PASS:"+pass);
					System.out.println("CLIPCASE:"+clipCase);
				    System.out.println("vert 1 = " + (vertex1.vs_x / -vertex1.vs_z) + "," + (vertex1.vs_y / -vertex1.vs_z));	    
				    System.out.println("vert 2 = " + (vertex2.vs_x / -vertex2.vs_z) + "," + (vertex2.vs_y / -vertex2.vs_z));	    
				    System.out.println("vert 3 = " + (vertex3.vs_x / -vertex3.vs_z) + "," + (vertex3.vs_y / -vertex3.vs_z));	    
				    System.out.println("vert 4 = " + (vertex4.vs_x / -vertex4.vs_z) + "," + (vertex4.vs_y / -vertex4.vs_z));

					
					
					switch(clipCase)
					{
						case 0:
							clipPolygon(
									(pass+1),
									context,
									fustrumTouchCount, touchedNear, touchedRight, touchedLeft, touchedTop, touchedBottom,							
									vertex1, vertex2, vertex3, vertex4,							
									renderer, polyData,textureMap,lightingValue,useHLPBreak
									);
							return;
						
						case 1:
							vertexa = calculateIntersect(vertex4, vertex1, context, pass);
							vertexb = calculateIntersect(vertex2, vertex1, context, pass);
							clipPolygon(
									(pass+1),
									context,
									fustrumTouchCount, touchedNear, touchedRight, touchedLeft, touchedTop, touchedBottom,							
									vertexb, vertex2, vertex3, vertexa,							
									renderer, polyData,textureMap,lightingValue,useHLPBreak
									);
							clipPolygon(
									(pass+1),
									context,
									fustrumTouchCount, touchedNear, touchedRight, touchedLeft, touchedTop, touchedBottom,							
									vertexa, vertexa, vertex3, vertex4,							
									renderer, polyData,textureMap,lightingValue,useHLPBreak
									);
							return;
							
						case 2:
							vertexa = calculateIntersect(vertex1, vertex2, context, pass);
							vertexb = calculateIntersect(vertex3, vertex2, context, pass);
							clipPolygon(
									(pass+1),
									context,
									fustrumTouchCount, touchedNear, touchedRight, touchedLeft, touchedTop, touchedBottom,							
									vertexa, vertexb, vertex3, vertex4,							
									renderer, polyData,textureMap,lightingValue,useHLPBreak
									);
							clipPolygon(
									(pass+1),
									context,
									fustrumTouchCount, touchedNear, touchedRight, touchedLeft, touchedTop, touchedBottom,							
									vertexa, vertexa, vertex4, vertex1,							
									renderer, polyData,textureMap,lightingValue,useHLPBreak
									);							
							return;
							
						case 3:
							vertexa = calculateIntersect(vertex4, vertex1, context, pass);
							vertexb = calculateIntersect(vertex3, vertex2, context, pass);
							clipPolygon(
									(pass+1),
									context,
									fustrumTouchCount, touchedNear, touchedRight, touchedLeft, touchedTop, touchedBottom,							
									vertexa, vertexb, vertex3, vertex4,							
									renderer, polyData,textureMap,lightingValue,useHLPBreak
									);					
							return;
							
						case 4:
							vertexa = calculateIntersect(vertex2, vertex3, context, pass);
							vertexb = calculateIntersect(vertex4, vertex3, context, pass);
							clipPolygon(
									(pass+1),
									context,
									fustrumTouchCount, touchedNear, touchedRight, touchedLeft, touchedTop, touchedBottom,							
									vertexa, vertexb, vertex4, vertex1,							
									renderer, polyData,textureMap,lightingValue,useHLPBreak
									);
							clipPolygon(
									(pass+1),
									context,
									fustrumTouchCount, touchedNear, touchedRight, touchedLeft, touchedTop, touchedBottom,							
									vertexa, vertexa, vertex1, vertex2,							
									renderer, polyData,textureMap,lightingValue,useHLPBreak
									);							
							return;
							
						case 5:
							vertexa = calculateIntersect(vertex4, vertex1, context, pass);
							vertexb = calculateIntersect(vertex2, vertex1, context, pass);
							vertexc = calculateIntersect(vertex2, vertex3, context, pass);
							vertexd = calculateIntersect(vertex4, vertex3, context, pass);
							clipPolygon(
									(pass+1),
									context,
									fustrumTouchCount, touchedNear, touchedRight, touchedLeft, touchedTop, touchedBottom,							
									vertexa, vertexb, vertex2, vertexc,							
									renderer, polyData,textureMap,lightingValue,useHLPBreak
									);
							clipPolygon(
									(pass+1),
									context,
									fustrumTouchCount, touchedNear, touchedRight, touchedLeft, touchedTop, touchedBottom,							
									vertexa, vertexc, vertexd, vertex4,							
									renderer, polyData,textureMap,lightingValue,useHLPBreak
									);							
							return;

						case 6:
							vertexa = calculateIntersect(vertex1, vertex2, context, pass);
							vertexb = calculateIntersect(vertex4, vertex3, context, pass);
							clipPolygon(
									(pass+1),
									context,
									fustrumTouchCount, touchedNear, touchedRight, touchedLeft, touchedTop, touchedBottom,							
									vertex1, vertexa, vertexb, vertex4,							
									renderer, polyData,textureMap,lightingValue,useHLPBreak
									);						
							return;
							
						case 7:
							vertexa = calculateIntersect(vertex4, vertex1, context, pass);
							vertexb = calculateIntersect(vertex4, vertex3, context, pass);
							clipPolygon(
									(pass+1),
									context,
									fustrumTouchCount, touchedNear, touchedRight, touchedLeft, touchedTop, touchedBottom,							
									vertexa, vertexa, vertexb, vertex4,							
									renderer, polyData,textureMap,lightingValue,useHLPBreak
									);						
							return;
							
						case 8:
							vertexa = calculateIntersect(vertex3, vertex4, context, pass);
							vertexb = calculateIntersect(vertex1, vertex4, context, pass);
							clipPolygon(
									(pass+1),
									context,
									fustrumTouchCount, touchedNear, touchedRight, touchedLeft, touchedTop, touchedBottom,							
									vertex1, vertex2, vertex3, vertexa,							
									renderer, polyData,textureMap,lightingValue,useHLPBreak
									);
							clipPolygon(
									(pass+1),
									context,
									fustrumTouchCount, touchedNear, touchedRight, touchedLeft, touchedTop, touchedBottom,							
									vertexa, vertexa, vertexb, vertex1,							
									renderer, polyData,textureMap,lightingValue,useHLPBreak
									);							
							return;
							
						case 9:
							vertexa = calculateIntersect(vertex2, vertex1, context, pass);
							vertexb = calculateIntersect(vertex3, vertex4, context, pass);
							clipPolygon(
									(pass+1),
									context,
									fustrumTouchCount, touchedNear, touchedRight, touchedLeft, touchedTop, touchedBottom,							
									vertexa, vertex2, vertex3, vertexb,							
									renderer, polyData,textureMap,lightingValue,useHLPBreak
									);						
							return;
							
						case 10:
							vertexa = calculateIntersect(vertex1, vertex2, context, pass);
							vertexb = calculateIntersect(vertex3, vertex2, context, pass);
							vertexc = calculateIntersect(vertex3, vertex4, context, pass);
							vertexd = calculateIntersect(vertex1, vertex4, context, pass);
							clipPolygon(
									(pass+1),
									context,
									fustrumTouchCount, touchedNear, touchedRight, touchedLeft, touchedTop, touchedBottom,							
									vertex1, vertexa, vertexb, vertexd,							
									renderer, polyData,textureMap,lightingValue,useHLPBreak
									);
							clipPolygon(
									(pass+1),
									context,
									fustrumTouchCount, touchedNear, touchedRight, touchedLeft, touchedTop, touchedBottom,							
									vertexd, vertexb, vertex3, vertexc,							
									renderer, polyData,textureMap,lightingValue,useHLPBreak
									);
							return;
						
						case 11:
							vertexa = calculateIntersect(vertex3, vertex2, context, pass);
							vertexb = calculateIntersect(vertex3, vertex4, context, pass);
							clipPolygon(
									(pass+1),
									context,
									fustrumTouchCount, touchedNear, touchedRight, touchedLeft, touchedTop, touchedBottom,							
									vertexa, vertexa, vertex3, vertexb,							
									renderer, polyData,textureMap,lightingValue,useHLPBreak
									);						
							return;
							
						case 12:
							vertexa = calculateIntersect(vertex2, vertex3, context, pass);
							vertexb = calculateIntersect(vertex1, vertex4, context, pass);
							clipPolygon(
									(pass+1),
									context,
									fustrumTouchCount, touchedNear, touchedRight, touchedLeft, touchedTop, touchedBottom,							
									vertex1, vertex2, vertexa, vertexb,							
									renderer, polyData,textureMap,lightingValue,useHLPBreak
									);						
							return;
							
						case 13:
							vertexa = calculateIntersect(vertex2, vertex1, context, pass);
							vertexb = calculateIntersect(vertex2, vertex3, context, pass);
							clipPolygon(
									(pass+1),
									context,
									fustrumTouchCount, touchedNear, touchedRight, touchedLeft, touchedTop, touchedBottom,							
									vertexa, vertexa, vertex2, vertexb,							
									renderer, polyData,textureMap,lightingValue,useHLPBreak
									);						
							return;
							
						case 14:
							vertexa = calculateIntersect(vertex1, vertex2, context, pass);
							vertexb = calculateIntersect(vertex1, vertex4, context, pass);
							clipPolygon(
									(pass+1),
									context,
									fustrumTouchCount, touchedNear, touchedRight, touchedLeft, touchedTop, touchedBottom,							
									vertex1, vertex1, vertexa, vertexb,							
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
	static final boolean isVertexCulled(final Vertex vertex, final NitrogenContext context, final int pass)
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
			return(-vertex.vs_y > (deapth_from_viewpoint * context.yClip));	
			
			case BOTTOM_PASS:
			return(vertex.vs_y > (deapth_from_viewpoint * context.yClip));
			
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
	static final Vertex calculateIntersect(Vertex in, Vertex out, NitrogenContext context, int pass)
	{
		float in_deapth = -in.vs_z;
		float out_deapth = -out.vs_z;
		
		/** Proportion of the distance toward the out vertex from in vertex */
		float n;
		/** Cached local value related to view fustrum's field-of-view */
		float k;
		
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
			
			case BOTTOM_PASS:
				k = context.yClip;
				n = (k * in_deapth - in.vs_y) / ((out.vs_y - in.vs_y) - k * (out_deapth - in_deapth));
				return(generateInbetweenVertex(in,out,n));	
			
			case TOP_PASS:
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
	static final Vertex generateInbetweenVertex(Vertex first, Vertex second, float n)
	{
		// create a new output vertex
		Vertex retval = workingVertexes[workingVertexIndex];
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

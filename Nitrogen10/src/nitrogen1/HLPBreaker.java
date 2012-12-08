package nitrogen1;

final class HLPBreaker {
	
	/** constant used for computing hlpBreakCase */
	static final int ONE = 1;
	/** constant used for computing hlpBreakCase */	
	static final int TWO = 2;
	/** constant used for computing hlpBreakCase */	
	static final int THREE = 3;	
	/** constant used for computing hlpBreakCase */	
	static final int FOUR = 4;
	/** constant used for computing hlpBreakCase */
	static final int EIGHT = 8;
	
	/** Field used to generate Vertexes that occur at intersect points */
	static Vertex[] workingVertexes;
	/** Field used to generate Vertexes that occur at intersect points */
	static int workingVertexIndex = 0;
	
	/** nearest point in polygon or sub-polygon being processed
	 * multiplied by the supplied nitrogen context qualityOfHLP value
	 */
	static float thresholdDist;
	private static PolygonRenderer polygonRenderer = new PolygonRenderer();

	/**
	 * This method is the entry point into HLPBreaker. It breaks high level of perspective polygons into lower ones (if needs be) before passing them on to PolygonRenderer class. It is an adapter method to make the process calls to polygonClipper and HLPBreaker similar 
	 * @param context Nitrogen context to render into
	 * @param fustrumTouchCount Total number of view-fustrum planes the Item supplying the polygon may have touched.
	 * @param touchedNear Item supplying the polygon may have touched near view-fustrum plane.
	 * @param touchedRight Item supplying the polygon may have touched right view-fustrum plane.
	 * @param touchedLeft Item supplying the polygon may have touched left view-fustrum plane.
	 * @param touchedTop Item supplying the polygon may have touched top view-fustrum plane.
	 * @param touchedBottom Item supplying the polygon may have touched bottom view-fustrum plane.
	 * <br />
	 * @param vertex1 First Vertex of the polygon. Vertexes parameters must be given in clockwise order.
	 * @param vertex2 Second Vertex of the polygon.
	 * @param vertex3 Third Vertex of the polygon.
	 * @param vertex4 Fourth Vertex of the polygon.
	 * <br />
	 * @param renderer The renderer to use to render the polygon into the supplied context.
	 * @param polyData Polygon data to pass on to the renderer, such as its colour.
	 * @param textureMap TextureMap to pass on to the renderer
	 * @param lightingValue lighting value that may have been computed by the polygons backside to pass to the Renderer
	 */
	
	static{ 
		// DEBUG
		int BUFFER_SIZE = 240;
		workingVertexes = new Vertex[BUFFER_SIZE];
		for(int i = 0 ; i < BUFFER_SIZE; i++)workingVertexes[i]= new Vertex();
		}
	
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
		// DEBUG
		context.clippedPolygonsRendered++;
		
		// skip HLP breaking if Item being rendered
		// or the polygons renderer says we can
		if(!useHLPBreak || !renderer.usesHLPBreak())
		{
			polygonRenderer.process(							
					context,
					vertex1, vertex2, vertex3, vertex4,					
					renderer,
					polyData,
					textureMap, lightingValue 
					);
			return;
		}
		
		
		prepareForNewPolygon();
		subprocess(
				false,					// added parameter
				context.qualityOfHLP,	// dereference this value once
				context,
				fustrumTouchCount, 
				touchedNear,
				touchedRight,
				touchedLeft,
				touchedTop,
				touchedBottom,
				
				vertex1, vertex2, vertex3, vertex4,
				
				renderer,
				polyData,
				textureMap,
				lightingValue				
				);
	}
	
	
	
	
/** processes the polygons breaking them down. If the first two vertexes are known to be closest set accelerate true */	
final static void subprocess(
		final boolean accelerate,
		final float contextQualityOfHLP,
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
		final float lightingValue
		)
	{
		// DEBUG
 //   System.out.println("HLP break subprocess called");
//    if(accelerate)System.out.println("accelerate=true");
//    final Vertex a = vertex1;
//    final Vertex b = vertex2;
//    final Vertex c = vertex3;
//    final Vertex d = vertex4;
//    System.out.println("vert a = " + a.vs_x + "," + a.vs_y + "," + a.vs_z );	    
//    System.out.println("vert b = " + b.vs_x + "," + b.vs_y + "," + b.vs_z );	    
//    System.out.println("vert c = " + c.vs_x + "," + c.vs_y + "," + c.vs_z );	    
//    System.out.println("vert d = " + d.vs_x + "," + d.vs_y + "," + d.vs_z );	
	
	if(needsHLPBreak(accelerate, context, vertex1, vertex2, vertex3, vertex4))
		{
			int hlpBreakCase = 0;
			final float thresholdDistL = thresholdDist; // set by needsHLPBreak
			if(accelerate)
			{
				hlpBreakCase = THREE;
			}
			else
			{
				if(vertex1.vs_z > thresholdDistL) hlpBreakCase |= ONE;
				if(vertex2.vs_z > thresholdDistL) hlpBreakCase |= TWO;
			}
			if(vertex3.vs_z > thresholdDistL) hlpBreakCase |= FOUR;
			if(vertex4.vs_z > thresholdDistL) hlpBreakCase |= EIGHT;
			
			hlpBreakCaseHandler(
					context,
					fustrumTouchCount, 
					touchedNear,
					touchedRight,
					touchedLeft,
					touchedTop,
					touchedBottom,		
					vertex1, vertex2, vertex3, vertex4,	
					renderer,
					polyData,
					textureMap,
					lightingValue,
					hlpBreakCase,
					thresholdDistL,
					contextQualityOfHLP
				);					
		}
		else
		{			
			polygonRenderer.process(
					context,
					vertex1, vertex2, vertex3, vertex4,					
					renderer,
					polyData,
					textureMap, lightingValue 
					);
		}		
	}

/** breaks high level perspective polygons down into lower level perspective polygons. It requires a hlpBreakCase parameter that determines which of the supplied vertexes exceed the high level of perspective threshold. */
final static void hlpBreakCaseHandler(
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
				final int hlpBreakCase,
				float thresholdDist,
				float contextQualityOfHLP
			)
		{
			final Vertex vertexa;
			final Vertex vertexb;	
			System.out.println("hlpBreakCase="+hlpBreakCase);
			switch(hlpBreakCase)
			{
				case 0:
					// This case should not happen
					// for robustness if it does happen resort to low level of perspective rendering
					polygonRenderer.process(							
							context,
							vertex1, vertex2, vertex3, vertex4,					
							renderer,
							polyData,
							textureMap, lightingValue 
							);
					return;
			
				case 1:
					vertexa = calculateIntersect(vertex1, vertex4, thresholdDist);
					vertexb = calculateIntersect(vertex1, vertex2, thresholdDist);
					polygonRenderer.process(
							context,
							vertex1, vertex1, vertexb, vertexa,					
							renderer,
							polyData,
							textureMap, lightingValue 
							);
					thresholdDist = thresholdDist * contextQualityOfHLP;
					subprocess(
							true,
							contextQualityOfHLP,
							context,
							fustrumTouchCount, touchedNear,touchedRight,touchedLeft,touchedTop,touchedBottom,							
							vertexa, vertexb, vertex2, vertex3,
							renderer,polyData,textureMap,lightingValue
							);
					subprocess(
							false,
							contextQualityOfHLP,
							context,
							fustrumTouchCount, touchedNear,touchedRight,touchedLeft,touchedTop,touchedBottom,							
							vertexa, vertexa, vertex3, vertex4,
							renderer,polyData,textureMap, lightingValue
							);
					return;
					
				case 2:
					vertexa = calculateIntersect(vertex2, vertex1, thresholdDist);
					vertexb = calculateIntersect(vertex2, vertex3, thresholdDist);
					polygonRenderer .process(
							context,
							vertex2, vertex2, vertexb, vertexa,					
							renderer,
							polyData,
							textureMap, lightingValue 
							);
					thresholdDist = thresholdDist * contextQualityOfHLP;
					subprocess(
							true,
							contextQualityOfHLP,
							context,
							fustrumTouchCount, touchedNear,touchedRight,touchedLeft,touchedTop,touchedBottom,							
							vertexa, vertexb, vertex4, vertex1,
							renderer, polyData, textureMap, lightingValue
							);
					subprocess(
							false,
							contextQualityOfHLP,
							context,
							fustrumTouchCount, touchedNear,touchedRight,touchedLeft,touchedTop,touchedBottom,							
							vertexb, vertexb, vertex3, vertex4,
							renderer, polyData, textureMap, lightingValue
							);
					return;
					
					
				case 3:
					vertexa = calculateIntersect(vertex1, vertex4, thresholdDist);
					vertexb = calculateIntersect(vertex2, vertex3, thresholdDist);
					polygonRenderer.process(
							context,
							vertex1, vertex2, vertexb, vertexa,					
							renderer,
							polyData,
							textureMap, lightingValue 
							);
					thresholdDist = thresholdDist * contextQualityOfHLP;
					subprocess(
							true,
							contextQualityOfHLP,
							context,
							fustrumTouchCount, touchedNear,touchedRight,touchedLeft,touchedTop,touchedBottom,							
							vertexa, vertexb, vertex3, vertex4,
							renderer, polyData, textureMap, lightingValue
							);
					return;
					
				case 4:
					vertexa = calculateIntersect(vertex3, vertex2, thresholdDist);
					vertexb = calculateIntersect(vertex3, vertex4, thresholdDist);
					polygonRenderer.process(
							context,
							vertex3, vertex3, vertexb, vertexa,					
							renderer,
							polyData,
							textureMap, lightingValue 
							);
					thresholdDist = thresholdDist * contextQualityOfHLP;
					subprocess(
							true,
							contextQualityOfHLP,
							context,
							fustrumTouchCount, touchedNear,touchedRight,touchedLeft,touchedTop,touchedBottom,							
							vertexa, vertexb, vertex4, vertex1,
							renderer, polyData, textureMap, lightingValue
							);
					subprocess(
							false,
							contextQualityOfHLP,
							context,
							fustrumTouchCount, touchedNear,touchedRight,touchedLeft,touchedTop,touchedBottom,							
							vertex1, vertex2, vertexa, vertexa,
							renderer, polyData, textureMap, lightingValue
							);
					return;
					
				case 5:
					// Should only happen rarely if ever, so complete by using two process calls
					subprocess(
							false,
							contextQualityOfHLP,
							context,
							fustrumTouchCount, touchedNear,touchedRight,touchedLeft,touchedTop,touchedBottom,							
							vertex1, vertex1, vertex2, vertex4,
							renderer, polyData, textureMap, lightingValue
							);
					subprocess(
							false,
							contextQualityOfHLP,
							context,
							fustrumTouchCount, touchedNear,touchedRight,touchedLeft,touchedTop,touchedBottom,							
							vertex3, vertex3, vertex4, vertex2,
							renderer, polyData, textureMap, lightingValue
							);				
					return;
					
				case 6:
					vertexa = calculateIntersect(vertex2, vertex1, thresholdDist);
					vertexb = calculateIntersect(vertex3, vertex4, thresholdDist);
					polygonRenderer.process(
							context,
							vertex2, vertex3, vertexb, vertexa,					
							renderer,
							polyData,
							textureMap, lightingValue 
							);
					thresholdDist = thresholdDist * contextQualityOfHLP;
					subprocess(
							true,
							contextQualityOfHLP,
							context,
							fustrumTouchCount, touchedNear,touchedRight,touchedLeft,touchedTop,touchedBottom,							
							vertexa, vertexb, vertex4, vertex1,
							renderer, polyData, textureMap, lightingValue							
							);
					return;
					
				case 7:
					vertexa = calculateIntersect(vertex3, vertex4, thresholdDist);
					vertexb = calculateIntersect(vertex1, vertex4, thresholdDist);
					polygonRenderer.process(
							context,
							vertex1, vertex1, vertex2, vertex3,					
							renderer,
							polyData,
							textureMap, lightingValue 
							);
					polygonRenderer.process(
							context,
							vertex1, vertex3, vertexa, vertexb,					
							renderer,
							polyData,
							textureMap, lightingValue 
							);					
					thresholdDist = thresholdDist * contextQualityOfHLP;
					subprocess(
							true,
							contextQualityOfHLP,
							context,
							fustrumTouchCount, touchedNear,touchedRight,touchedLeft,touchedTop,touchedBottom,							
							vertexb, vertexa, vertex4, vertex4,
							renderer, polyData, textureMap, lightingValue
							);
					return;					
						
				case 8:
					vertexa = calculateIntersect(vertex4, vertex3, thresholdDist);
					vertexb = calculateIntersect(vertex4, vertex1, thresholdDist);
					polygonRenderer.process(
							context,
							vertex4, vertex4, vertexb, vertexa,					
							renderer,
							polyData,
							textureMap, lightingValue 
							);
					thresholdDist = thresholdDist * contextQualityOfHLP;
					subprocess(
							true,
							contextQualityOfHLP,
							context,
							fustrumTouchCount, touchedNear,touchedRight,touchedLeft,touchedTop,touchedBottom,							
							vertexa, vertexb, vertex2, vertex3,
							renderer, polyData, textureMap, lightingValue
							);
					subprocess(
							false,
							contextQualityOfHLP,
							context,
							fustrumTouchCount, touchedNear,touchedRight,touchedLeft,touchedTop,touchedBottom,							
							vertex1, vertex1, vertex2, vertexb,
							renderer,polyData,textureMap, lightingValue
							);
					return;
					
				case 9:
					vertexa = calculateIntersect(vertex1, vertex2, thresholdDist);
					vertexb = calculateIntersect(vertex4, vertex3, thresholdDist);
					polygonRenderer.process(
							context,
							vertex1, vertexa, vertexb, vertex4,					
							renderer,
							polyData,
							textureMap, lightingValue 
							);
					thresholdDist = thresholdDist * contextQualityOfHLP;
					subprocess(
							true,
							contextQualityOfHLP,
							context,
							fustrumTouchCount, touchedNear,touchedRight,touchedLeft,touchedTop,touchedBottom,							
							vertexb, vertexa, vertex2, vertex3,
							renderer, polyData, textureMap, lightingValue
							);
					return;
					
				case 10:
					// Should only happen rarely if ever, so complete by using two process calls
					subprocess(
							false,
							contextQualityOfHLP,
							context,
							fustrumTouchCount, touchedNear,touchedRight,touchedLeft,touchedTop,touchedBottom,							
							vertex2, vertex2, vertex3, vertex1,
							renderer, polyData, textureMap, lightingValue
							);
					subprocess(
							false,
							contextQualityOfHLP,
							context,
							fustrumTouchCount, touchedNear,touchedRight,touchedLeft,touchedTop,touchedBottom,							
							vertex4, vertex4, vertex1, vertex3,
							renderer, polyData, textureMap, lightingValue
							);				
					return;
					
				case 11:
					vertexa = calculateIntersect(vertex2, vertex3, thresholdDist);
					vertexb = calculateIntersect(vertex4, vertex3, thresholdDist);
					polygonRenderer.process(
							context,
							vertex1, vertex1, vertex2, vertex4,					
							renderer,
							polyData,
							textureMap, lightingValue 
							);
					polygonRenderer.process(
							context,
							vertex2, vertexa, vertexb, vertex4,					
							renderer,
							polyData,
							textureMap, lightingValue 
							);					
					thresholdDist = thresholdDist * contextQualityOfHLP;
					subprocess(
							true,
							contextQualityOfHLP,
							context,
							fustrumTouchCount, touchedNear,touchedRight,touchedLeft,touchedTop,touchedBottom,							
							vertexb, vertexa, vertex3, vertex3,
							renderer, polyData, textureMap, lightingValue
							);
					return;
					
				case 12:
					vertexa = calculateIntersect(vertex3, vertex2, thresholdDist);
					vertexb = calculateIntersect(vertex4, vertex1, thresholdDist);
					polygonRenderer.process(
							context,
							vertex3, vertex4, vertexb, vertexa,					
							renderer,
							polyData,
							textureMap, lightingValue 
							);
					thresholdDist = thresholdDist * contextQualityOfHLP;
					subprocess(
							true,
							contextQualityOfHLP,
							context,
							fustrumTouchCount, touchedNear,touchedRight,touchedLeft,touchedTop,touchedBottom,							
							vertexa, vertexb, vertex1, vertex2,
							renderer, polyData, textureMap, lightingValue
							);
					return;
					
				case 13:
					vertexa = calculateIntersect(vertex1, vertex2, thresholdDist);
					vertexb = calculateIntersect(vertex3, vertex2, thresholdDist);
					polygonRenderer.process(
							context,
							vertex4, vertex4, vertex1, vertex3,					
							renderer,
							polyData,
							textureMap, lightingValue 
							);
					polygonRenderer.process(
							context,
							vertex3, vertex1, vertexa, vertexb,					
							renderer,
							polyData,
							textureMap, lightingValue 
							);
					thresholdDist = thresholdDist * contextQualityOfHLP;
					subprocess(
							true,
							contextQualityOfHLP,
							context,
							fustrumTouchCount, touchedNear,touchedRight,touchedLeft,touchedTop,touchedBottom,							
							vertexb, vertexa, vertex2, vertex2,
							renderer, polyData, textureMap, lightingValue
							);
					return;
					
				case 14:
					vertexa = calculateIntersect(vertex2, vertex1, thresholdDist);
					vertexb = calculateIntersect(vertex4, vertex1, thresholdDist);
					polygonRenderer.process(
							context,
							vertex3, vertex3, vertex4, vertex2,					
							renderer,
							polyData,
							textureMap, lightingValue 
							);
					polygonRenderer.process(
							context,
							vertex2, vertex4, vertexb, vertexa,					
							renderer,
							polyData,
							textureMap, lightingValue 
							);
					thresholdDist = thresholdDist * contextQualityOfHLP;
					subprocess(
							true,
							contextQualityOfHLP,
							context,
							fustrumTouchCount, touchedNear,touchedRight,touchedLeft,touchedTop,touchedBottom,							
							vertexa, vertexb, vertex1, vertex1,
							renderer, polyData, textureMap, lightingValue
							);
					return;
					
				case 15:
					polygonRenderer.process(
							context,
							vertex1, vertex2, vertex3, vertex4,					
							renderer,
							polyData,
							textureMap, lightingValue 
							);
					return;
			}
		}

	/** Given four vertexes and a Nitrogen context containing a valid qualityofHLP determines if HLP breaking is required */ 
	final static boolean needsHLPBreak(final boolean accelerate, final NitrogenContext context, final Vertex vertex1, final Vertex vertex2, final Vertex vertex3, final Vertex vertex4)
	{
		float z1,z2,z3,z4;
		float maxaz, maxbz, minaz, minbz, finalmaxz, finalminz;
		float localThresholdDist;
		if(accelerate)
		{
			// we know that z1 and z2 are the closest points with z = thresholdDist
			localThresholdDist = thresholdDist * context.qualityOfHLP;
			z3 = vertex3.vs_z;
			z4 = vertex4.vs_z;
			if(z3 > z4)
			{
				finalmaxz = z4; // most -ve is farthest
			}
			else
			{
				finalmaxz = z3;
			}
			
			// return false if finalmaxz is more +ve (closer) than localThresholdDist
			if(localThresholdDist < finalmaxz)
			{
				return(false);			
			}
			else
			{
				thresholdDist = localThresholdDist; // keep the computation result for other methods 
				return(true);
			}
		}
		else
		{
			z1 = vertex1.vs_z;
			z2 = vertex2.vs_z;
			z3 = vertex3.vs_z;
			z4 = vertex4.vs_z;
			
			// sort first two vertexes (max = farthest from viewpoint)
			if(z1 < z2)
			{
				maxaz = z1; minaz = z2;
			}
			else
			{
				maxaz = z2; minaz = z1;
			}
			
			// sort last two vertexes
			if(z3 < z4)
			{
				maxbz = z3; minbz = z4;
			}
			else
			{
				maxbz = z4; minbz = z3;
			}
			// sort the sorted values to find farthest distance
			if(maxaz < maxbz)
			{
				finalmaxz = maxaz;
			}
			else
			{
				finalmaxz = maxbz;
			}

			// sort the sorted values to find nearest distance
			if(minaz < minbz)
			{
				finalminz = minbz;
			}
			else
			{
				finalminz = minaz;
			}
			localThresholdDist = finalminz * context.qualityOfHLP;
			if(localThresholdDist < finalmaxz)
			{
				return(false);
			}
			else
			{
				thresholdDist = localThresholdDist; // keep the computation result for other methods 
				return(true);
			}
		}
	}
	
	final static Vertex calculateIntersect(final Vertex in, final Vertex out, final float threshold)
	{
		//DEBUG
//		System.out.println("calculatingIntersect");
//		System.out.println("in = " + in.vs_x +"," + in.vs_y +","+ in.vs_z);
//		System.out.println("out = " + out.vs_x +"," + out.vs_y +","+ out.vs_z);
		
		float inz = in.vs_z;
		float n = (threshold - inz)/(out.vs_z - inz);
		System.out.println("n = " + n);
		Vertex retval = generateInbetweenVertex(in, out, n);
//		System.out.println("intersect = " + retval.vs_x +"," + retval.vs_y +","+ retval.vs_z);
		return(retval);		
	}
	
	/** Returns a working vertex that is situated along a given proportion of the line between first and second vertexes
	 * @param first First vertex
	 * @param second Second vertex
	 * @param n The proportion parameter (expected to be in the range 0 ... 1)
	 * @return The vertex that lies at proportion n along the line between first and second vertex parameters
	 */
	final static Vertex generateInbetweenVertex(Vertex first, Vertex second, float n)
	{
		//DEBUG
//		System.out.println("generatingInbetweenVertex at index:"+workingVertexIndex);
		
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
	
	final static void prepareForNewPolygon()
	{
		//DEBUG
//		System.out.println("HLP BREAKER PREPARING FOR NEW POLYGON");
		
		workingVertexIndex = 0;
	}
}

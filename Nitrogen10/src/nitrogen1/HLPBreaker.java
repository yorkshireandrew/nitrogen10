package nitrogen1;

public class HLPBreaker {
	
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
	
	/** Field used to generate Vertexs that occur at intersect points */
	static Vert[] workingVertexs;
	/** Field used to generate Vertexs that occur at intersect points */
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
	 * @param vert1 First Vertex of the polygon. Vertexes parameters must be given in clockwise order.
	 * @param vert2 Second Vertex of the polygon.
	 * @param vert3 Third Vertex of the polygon.
	 * @param vert4 Fourth Vertex of the polygon.
	 * <br />
	 * @param renderer The renderer to use to render the polygon into the supplied context.
	 * @param polyData Polygon data to pass on to the renderer, such as its colour.
	 * @param textureMap TextureMap to pass on to the renderer
	 * @param lightingValue lighting value that may have been computed by the polygons backside to pass to the Renderer
	 */
	
	static{ 
		// DEBUG
		int BUFFER_SIZE = 12;
		workingVertexs = new Vert[BUFFER_SIZE];
		for(int i = 0 ; i < BUFFER_SIZE; i++)workingVertexs[i]= new Vert();
		}
	static void process(	
			NitrogenContext context,
			int fustrumTouchCount, 
			boolean touchedNear,
			boolean touchedRight,
			boolean touchedLeft,
			boolean touchedTop,
			boolean touchedBottom,
			
			Vert vert1, Vert vert2, Vert vert3, Vert vert4,
			
			Renderer renderer,
			int[] polyData,
			TexMap textureMap,
			float lightingValue
			)
	{
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
				
				vert1, vert2, vert3, vert4,
				
				renderer,
				polyData,
				textureMap,
				lightingValue				
				);
	}
	
	
	
	
/** processes the polygons breaking them down. If the first two vertexes are known to be closest set accelerate true */	
static void subprocess(
		boolean accelerate,
		float contextQualityOfHLP,
		NitrogenContext context,
		int fustrumTouchCount, 
		boolean touchedNear,
		boolean touchedRight,
		boolean touchedLeft,
		boolean touchedTop,
		boolean touchedBottom,
		
		Vert vert1, Vert vert2, Vert vert3, Vert vert4,
		
		Renderer renderer,
		int[] polyData,
		TexMap textureMap,
		float lightingValue
		)
	{
    System.out.println("HLP break subprocess called");
//    if(accelerate)System.out.println("accelerate=true");
    Vert a = vert1;
    Vert b = vert2;
    Vert c = vert3;
    Vert d = vert4;
    System.out.println("vert a = " + a.vs_x + "," + a.vs_y + "," + a.vs_z );	    
    System.out.println("vert b = " + b.vs_x + "," + b.vs_y + "," + b.vs_z );	    
    System.out.println("vert c = " + c.vs_x + "," + c.vs_y + "," + c.vs_z );	    
    System.out.println("vert d = " + d.vs_x + "," + d.vs_y + "," + d.vs_z );	
	
	if(needsHLPBreak(accelerate, context, vert1, vert2, vert3, vert4))
		{
			int hlpBreakCase = 0;
			float localThresholdDist = thresholdDist; // set by needsHLPBreak
			if(accelerate)
			{
				hlpBreakCase = THREE;
			}
			else
			{
				if(vert1.vs_z > localThresholdDist) hlpBreakCase |= ONE;
				if(vert2.vs_z > localThresholdDist) hlpBreakCase |= TWO;
			}
			if(vert3.vs_z > localThresholdDist) hlpBreakCase |= FOUR;
			if(vert4.vs_z > localThresholdDist) hlpBreakCase |= EIGHT;
			
			hlpBreakCaseHandler(
					context,
					fustrumTouchCount, 
					touchedNear,
					touchedRight,
					touchedLeft,
					touchedTop,
					touchedBottom,		
					vert1, vert2, vert3, vert4,	
					renderer,
					polyData,
					textureMap,
					lightingValue,
					hlpBreakCase,
					localThresholdDist,
					contextQualityOfHLP
				);					
		}
		else
		{			
			polygonRenderer.process(
					context,
					vert1, vert2, vert3, vert4,					
					renderer,
					polyData,
					textureMap, lightingValue 
					);
		}		
	}

/** breaks high level perspective polygons down into lower level perspective polygons. It requires a hlpBreakCase parameter that determines which of the supplied vertexes exceed the high level of perspective threshold. */
static void hlpBreakCaseHandler(
				NitrogenContext context,
				int fustrumTouchCount, 
				boolean touchedNear,
				boolean touchedRight,
				boolean touchedLeft,
				boolean touchedTop,
				boolean touchedBottom,		
				Vert vert1, Vert vert2, Vert vert3, Vert vert4,	
				Renderer renderer,
				int[] polyData,
				TexMap textureMap,
				float lightingValue,
				int hlpBreakCase,
				float thresholdDist,
				float contextQualityOfHLP
			)
		{
			Vert verta;
			Vert vertb;	
			System.out.println("hlpBreakCase="+hlpBreakCase);
			switch(hlpBreakCase)
			{
				case 0:
					// This case should not happen
					// for robustness if it does happen resort to low level of perspective rendering
					polygonRenderer.process(							
							context,
							vert1, vert2, vert3, vert4,					
							renderer,
							polyData,
							textureMap, lightingValue 
							);
					return;
			
				case 1:
					verta = calculateIntersect(vert1, vert4, thresholdDist);
					vertb = calculateIntersect(vert1, vert2, thresholdDist);
					polygonRenderer.process(
							context,
							vert1, vert1, vertb, verta,					
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
							verta, vertb, vert2, vert4,
							renderer,polyData,textureMap,lightingValue
							);
					subprocess(
							false,
							contextQualityOfHLP,
							context,
							fustrumTouchCount, touchedNear,touchedRight,touchedLeft,touchedTop,touchedBottom,							
							vert2, vert2, vert3, vert4,
							renderer,polyData,textureMap, lightingValue
							);
					return;
					
				case 2:
					verta = calculateIntersect(vert2, vert1, thresholdDist);
					vertb = calculateIntersect(vert2, vert3, thresholdDist);
					polygonRenderer .process(
							context,
							vert2, vert2, vertb, verta,					
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
							verta, vertb, vert3, vert1,
							renderer, polyData, textureMap, lightingValue
							);
					subprocess(
							false,
							contextQualityOfHLP,
							context,
							fustrumTouchCount, touchedNear,touchedRight,touchedLeft,touchedTop,touchedBottom,							
							vert1, vert1, vert3, vert4,
							renderer, polyData, textureMap, lightingValue
							);
					return;
					
				case 3:
					verta = calculateIntersect(vert1, vert4, thresholdDist);
					vertb = calculateIntersect(vert2, vert3, thresholdDist);
					polygonRenderer.process(
							context,
							vert1, vert2, vertb, verta,					
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
							verta, vertb, vert3, vert4,
							renderer, polyData, textureMap, lightingValue
							);
					return;
					
				case 4:
					verta = calculateIntersect(vert3, vert2, thresholdDist);
					vertb = calculateIntersect(vert3, vert4, thresholdDist);
					polygonRenderer.process(
							context,
							vert3, vert3, vertb, verta,					
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
							verta, vertb, vert4, vert2,
							renderer, polyData, textureMap, lightingValue
							);
					subprocess(
							false,
							contextQualityOfHLP,
							context,
							fustrumTouchCount, touchedNear,touchedRight,touchedLeft,touchedTop,touchedBottom,							
							vert2, vert2, vert4, vert1,
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
							vert1, vert1, vert2, vert4,
							renderer, polyData, textureMap, lightingValue
							);
					subprocess(
							false,
							contextQualityOfHLP,
							context,
							fustrumTouchCount, touchedNear,touchedRight,touchedLeft,touchedTop,touchedBottom,							
							vert3, vert3, vert4, vert2,
							renderer, polyData, textureMap, lightingValue
							);				
					return;
					
				case 6:
					verta = calculateIntersect(vert2, vert1, thresholdDist);
					vertb = calculateIntersect(vert3, vert4, thresholdDist);
					polygonRenderer.process(
							context,
							vert2, vert3, vertb, verta,					
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
							verta, vertb, vert4, vert1,
							renderer, polyData, textureMap, lightingValue							
							);
					return;
					
				case 7:
					verta = calculateIntersect(vert3, vert4, thresholdDist);
					vertb = calculateIntersect(vert1, vert4, thresholdDist);
					polygonRenderer.process(
							context,
							vert1, vert1, vert2, vert3,					
							renderer,
							polyData,
							textureMap, lightingValue 
							);
					polygonRenderer.process(
							context,
							vert1, vert3, verta, vertb,					
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
							vertb, verta, vert4, vert4,
							renderer, polyData, textureMap, lightingValue
							);
					return;					
						
				case 8:
					verta = calculateIntersect(vert4, vert3, thresholdDist);
					vertb = calculateIntersect(vert4, vert1, thresholdDist);
					polygonRenderer.process(
							context,
							vert4, vert4, vertb, verta,					
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
							verta, vertb, vert1, vert3,
							renderer, polyData, textureMap, lightingValue
							);
					subprocess(
							false,
							contextQualityOfHLP,
							context,
							fustrumTouchCount, touchedNear,touchedRight,touchedLeft,touchedTop,touchedBottom,							
							vert1, vert1, vert2, vert3,
							renderer,polyData,textureMap, lightingValue
							);
					return;
					
				case 9:
					verta = calculateIntersect(vert1, vert2, thresholdDist);
					vertb = calculateIntersect(vert4, vert3, thresholdDist);
					polygonRenderer.process(
							context,
							vert1, verta, vertb, vert4,					
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
							vertb, verta, vert2, vert3,
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
							vert2, vert2, vert3, vert1,
							renderer, polyData, textureMap, lightingValue
							);
					subprocess(
							false,
							contextQualityOfHLP,
							context,
							fustrumTouchCount, touchedNear,touchedRight,touchedLeft,touchedTop,touchedBottom,							
							vert4, vert4, vert1, vert3,
							renderer, polyData, textureMap, lightingValue
							);				
					return;
					
				case 11:
					verta = calculateIntersect(vert2, vert3, thresholdDist);
					vertb = calculateIntersect(vert4, vert3, thresholdDist);
					polygonRenderer.process(
							context,
							vert1, vert1, vert2, vert4,					
							renderer,
							polyData,
							textureMap, lightingValue 
							);
					polygonRenderer.process(
							context,
							vert2, verta, vertb, vert4,					
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
							vertb, verta, vert3, vert3,
							renderer, polyData, textureMap, lightingValue
							);
					return;
					
				case 12:
					verta = calculateIntersect(vert3, vert2, thresholdDist);
					vertb = calculateIntersect(vert4, vert1, thresholdDist);
					polygonRenderer.process(
							context,
							vert3, vert4, vertb, verta,					
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
							verta, vertb, vert1, vert2,
							renderer, polyData, textureMap, lightingValue
							);
					return;
					
				case 13:
					verta = calculateIntersect(vert1, vert2, thresholdDist);
					vertb = calculateIntersect(vert3, vert2, thresholdDist);
					polygonRenderer.process(
							context,
							vert4, vert4, vert1, vert3,					
							renderer,
							polyData,
							textureMap, lightingValue 
							);
					polygonRenderer.process(
							context,
							vert3, vert1, verta, vertb,					
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
							vertb, verta, vert2, vert2,
							renderer, polyData, textureMap, lightingValue
							);
					return;
					
				case 14:
					verta = calculateIntersect(vert2, vert1, thresholdDist);
					vertb = calculateIntersect(vert4, vert1, thresholdDist);
					polygonRenderer.process(
							context,
							vert3, vert3, vert4, vert2,					
							renderer,
							polyData,
							textureMap, lightingValue 
							);
					polygonRenderer.process(
							context,
							vert2, vert4, vertb, verta,					
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
							verta, vertb, vert1, vert1,
							renderer, polyData, textureMap, lightingValue
							);
					return;
					
				case 15:
					polygonRenderer.process(
							context,
							vert1, vert2, vert3, vert4,					
							renderer,
							polyData,
							textureMap, lightingValue 
							);
					return;
			}
		}

	/** Given four vertexes and a Nitrogen context containing a valid qualityofHLP determines if HLP breaking is required */ 
	static boolean needsHLPBreak(boolean accelerate, NitrogenContext context, Vert vert1, Vert vert2, Vert vert3, Vert vert4)
	{
		float z1,z2,z3,z4;
		float maxaz, maxbz, minaz, minbz, finalmaxz, finalminz;
		float localThresholdDist;
		if(accelerate)
		{
			// we know that z1 and z2 are the closest points with z = thresholdDist
			localThresholdDist = thresholdDist * context.qualityOfHLP;
			z3 = vert3.vs_z;
			z4 = vert4.vs_z;
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
			z1 = vert1.vs_z;
			z2 = vert2.vs_z;
			z3 = vert3.vs_z;
			z4 = vert4.vs_z;
			
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
	
	static Vert calculateIntersect(Vert in, Vert out, float threshold)
	{
		//DEBUG
//		System.out.println("calculatingIntersect");
//		System.out.println("in = " + in.vs_x +"," + in.vs_y +","+ in.vs_z);
//		System.out.println("out = " + out.vs_x +"," + out.vs_y +","+ out.vs_z);
		
		float inz = in.vs_z;
		float n = (threshold - inz)/(out.vs_z - inz);
		System.out.println("n = " + n);
		Vert retval = generateInbetweenVertex(in, out, n);
//		System.out.println("intersect = " + retval.vs_x +"," + retval.vs_y +","+ retval.vs_z);
		return(retval);		
	}
	
	/** Returns a working vertex that is situated along a given proportion of the line between first and second vertexes
	 * @param first First vertex
	 * @param second Second vertex
	 * @param n The proportion parameter (expected to be in the range 0 ... 1)
	 * @return The vertex that lies at proportion n along the line between first and second vertex parameters
	 */
	static final Vert generateInbetweenVertex(Vert first, Vert second, float n)
	{
		//DEBUG
//		System.out.println("generatingInbetweenVertex at index:"+workingVertexIndex);
		
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
		//DEBUG
//		System.out.println("HLP BREAKER PREPARING FOR NEW POLYGON");
		
		workingVertexIndex = 0;
	}
}

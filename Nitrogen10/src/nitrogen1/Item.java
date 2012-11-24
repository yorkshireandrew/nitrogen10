package nitrogen1;
/** An Item comprised of several polygons, TexMaps, Colour data, BSPPlanes 
 * that can be attached to a Transform object and thereby rendered into a 
 * NitrogenContext.
 * @author andrew
 *
 */
public class Item {
	
	// Enumerations for whichRenderer
	static final int NEAR_RENDERER = 0;
	static final int MID_RENDERER = 1;
	static final int FAR_RENDERER = 2;
	
	/** Class used to encapsulate polygon clipping */
	static PolygonClipper polygonClipper = new PolygonClipper();

	/** Class used to encapsulate polygon breaking into lower perspective levels */
	static HLPBreaker  hlpBreaker = new HLPBreaker();
	
	/** Parent transform of this Item in the scene graph */
	Transform parent = null;
	
	/** The immutable part of an Item that can be shared across many identical Items that differ only by scene graph position and visibility. */
	SharedImmutableSubItem sisi;
		
	/** The Items computed Backsides. A Backsides consists of a position, view space coordinate and a direction vector, 
	 * they are used by polygons to determine visibility and which of their two faces is being viewed */
	Backside backsides[];
	
	/** The Items vertexes */
	Vert vertexs[];
	
	// ************************************************
	// ********************** FLAGS *******************
	// ************************************************
	
	/** Set true if the item is visible, or false if the Item is invisible*/
	boolean visibility = false;
	
	/** Set true by scene graph if rotation has occurred since last render call */
	boolean rotationNeedsUpdate = true;
	
	/** Set true by scene graph if rotation or translation has occurred since last render call */
	boolean translationNeedsUpdate = true;
	
	/** Count of how many fustrum planes the item may touch */
	int fustrumTouchCount;
	
	/** Enumerated value that determines which renderer to use based on distance */
	int whichRenderer = NEAR_RENDERER;
	
	
	// Which fustrum planes the item may touch, used to improve efficiency of polygon clipping
	// note: Items remain visible then clip entirely once they cross the fustrum farClip distance
	boolean touchedNear;
	boolean touchedRight;
	boolean touchedLeft;
	boolean touchedTop;
	boolean touchedBottom;
	
	/** Flag to render using improved detail polygons. This is a state field used to apply hysteresis */
	boolean isImprovedDetail = false;
	
	/** Flag to use hlp breaking. This is a state field used to apply hysteresis */
	boolean isUsingHLPBreaking = false;
	
	/** Flag to use billboard Orientation. This is a state field used to apply hysteresis */
	boolean isUsingBillboardOriention = false;
	
	/** Creates a new Item and attaches it to a transform */
	Item(SharedImmutableSubItem in_sisi, Transform t)
	{
		if(t == null)return;
		parent = t;
		t.add(this);
		
		sisi = in_sisi;

		// create a blank backside array
		int backsideMax = in_sisi.immutableBacksides.length;
		backsides = new Backside[backsideMax];
		for(int x = 0; x < backsideMax; x++)backsides[x] = new Backside();

		// create the vertexs array from the sisi ImmutableVertexs
		ImmutableVertex[] iva = in_sisi.immutableVertexs;
		int vertexMax = iva.length;
		vertexs = new Vert[vertexMax];
		for(int x = 0; x < vertexMax; x++)vertexs[x] = new Vert(iva[x]);
	}
	
	/** called to render the Item 
	 * @param context The NitrogenContext to render the Item in
	 * @param v11-v34 The orientation matrix computed by the scene graph (12 floating point values)*/
	void renderItem(

			NitrogenContext context,
			// position vector from scene graph
			float v11, float v12, float v13, float v14,
			float v21, float v22, float v23, float v24,
			float v31, float v32, float v33, float v34)
	{
		// return if the Item is not set visible
		if(visibility == false)return;
		
		// return if the Item is fustrum culled
		if(isItemFustrumCulled(v14,v24,v34,context))return;
		
		//Cache values needed for rendering locally
		SharedImmutableSubItem 	sisiCache 				= sisi;
		int 					fustrumTouchCountCache	= fustrumTouchCount;
		boolean 				touchedNearCache 		= touchedNear;
		boolean 				touchedRightCache 		= touchedRight;
		boolean 				touchedLeftCache		= touchedLeft;
		boolean 				touchedTopCache			= touchedTop;
		boolean 				touchedBottomCache  	= touchedBottom;
		
		// if there is movement determine which renderer to use
		float itemDist = -v34;
		
		if(translationNeedsUpdate)
		{
			if(isUsingBillboardOriention)
			{
				if(itemDist > sisi.billboardOrientationDistPlus)
				{			
					// Item has just become a billboard 
					// so orientate along the view-space axis
					v11=1;v12=0;v13=0;
					v21=0;v22=1;v23=0;
					v31=0;v32=0;v33=1;
					
					// ensure above change gets applied 
					rotationNeedsUpdate = true;
					
					// set the billboard flag
					isUsingBillboardOriention = true;
				}
				else
				{
					// inhibit any rotation notifications from the scene graph
					rotationNeedsUpdate = false;
				}
			}
			else
			{
				if(itemDist < sisi.billboardOrientationDist)
				{
					// ensure the passed in scene graph orientation is applied
					rotationNeedsUpdate = true;
					
					// clear the billboard flag
					isUsingBillboardOriention = false;
					
				}
			}
			
			// see if renderer needs changing
			selectWhichRenderer(itemDist,sisiCache);			
			
			// update other flags used during rendering
			updateRenderingFlags(itemDist,sisiCache);
		}
		
		calculateItemFustrumFlags(v14,v24,v34,context);
		lazyComputeBacksidesAndVertexs();

		// Select the right number of polygons to render
		int polyStart;
		int polyFinish;
		if(isImprovedDetail)
		{
			polyStart = sisiCache.improvedDetailPolyStart;
			polyFinish = sisiCache.improvedDetailPolyFinish;
		}
		else
		{
			polyStart = sisiCache.normalDetailPolyStart;
			polyFinish = sisiCache.normalDetailPolyFinish;
		}
		
		ImmutablePolygon immutablePolygon;
		int backsideIndex;
		Backside backside;
		boolean transparentPolygon;
		boolean transparencyPass = context.transparencyPass;
		
		/** True unless the Items SharedImmutableSubItem nearPlaneCrashBacksideOverride is true and the Item has also crashed into near Plane */
		boolean noBacksideOverride = (!sisi.nearPlaneCrashBacksideOverride) ||(!touchedNear);
		
		for(int x = polyStart; x < polyFinish; x++)
		{
			immutablePolygon = sisi.immutablePolygons[x];
			
			// skip the polygon if its transparency is wrong for the pass
			transparentPolygon = immutablePolygon.isTransparent;
			if(!transparencyPass &&  transparentPolygon)continue;
			if( transparencyPass && !transparentPolygon)continue;
			
			// calculate the polygons backside if necessary
			backsideIndex = immutablePolygon.backsideIndex;
			backside = backsides[backsideIndex];
			
			if(backside.translationNeedsUpdate)
			{
				backside.calculate(
						sisi.immutableBacksides[backsideIndex],
						context,
						v11,v12,v13,v14,
						v21,v22,v23,v24,
						v31,v32,v33,v34);
			}
			
			if(backside.facingViewer())
			{
				// Calculate the vertexes, then Pass the polygon on to the next process.
				Vert v1 = vertexs[immutablePolygon.c1];
				Vert v2 = vertexs[immutablePolygon.c2];
				Vert v3 = vertexs[immutablePolygon.c3];
				Vert v4 = vertexs[immutablePolygon.c4];
				v1.calculateViewSpaceCoordinates(v11,v12,v13,v14,v21,v22,v23,v24,v31,v32,v33,v34);				
				v2.calculateViewSpaceCoordinates(v11,v12,v13,v14,v21,v22,v23,v24,v31,v32,v33,v34);				
				v3.calculateViewSpaceCoordinates(v11,v12,v13,v14,v21,v22,v23,v24,v31,v32,v33,v34);				
				v4.calculateViewSpaceCoordinates(v11,v12,v13,v14,v21,v22,v23,v24,v31,v32,v33,v34);				
				PolygonClipper.prepareForNewPolygon();
				PolygonClipper.process(
						context,
						fustrumTouchCountCache, 
						touchedNearCache,
						touchedRightCache,
						touchedLeftCache,
						touchedTopCache,
						touchedBottomCache,					
						v1, 
						v2, 
						v3, 
						v4, 									

						immutablePolygon.getRenderer(whichRenderer),
						immutablePolygon.polyData,
						immutablePolygon.textureMap,						
						backside.lightingValue
						
					);				
			}
			else
			{
				// Skip rendering the polygon if it is backside culled
				if(immutablePolygon.isBacksideCulled && noBacksideOverride)continue;
				
				// Calculate the vertexes, then Pass the polygon on to the next process.
				Vert v1 = vertexs[immutablePolygon.c1];
				Vert v2 = vertexs[immutablePolygon.c2];
				Vert v3 = vertexs[immutablePolygon.c3];
				Vert v4 = vertexs[immutablePolygon.c4];
				v1.calculateViewSpaceCoordinates(v11,v12,v13,v14,v21,v22,v23,v24,v31,v32,v33,v34);				
				v2.calculateViewSpaceCoordinates(v11,v12,v13,v14,v21,v22,v23,v24,v31,v32,v33,v34);				
				v3.calculateViewSpaceCoordinates(v11,v12,v13,v14,v21,v22,v23,v24,v31,v32,v33,v34);				
				v4.calculateViewSpaceCoordinates(v11,v12,v13,v14,v21,v22,v23,v24,v31,v32,v33,v34);				

				// Pass the polygon on to the next process, but reverse the ordering of the vertexes 
				// because the polygon is facing away, to ensure they occur in a clockwise direction		
				PolygonClipper.prepareForNewPolygon();
				PolygonClipper.process(
					context,
					fustrumTouchCountCache, 
					touchedNearCache,
					touchedRightCache,
					touchedLeftCache,
					touchedTopCache,
					touchedBottomCache,					
					v4, 
					v3, 
					v2, 
					v1,
					immutablePolygon.getRenderer(whichRenderer),					
					immutablePolygon.polyData,
					immutablePolygon.textureMap,
					backside.lightingValue
				);
			} //end of backside facing viewer if-else
		} //end of polygon rendering loop	
	}
	
	//**************************** END OF RENDER *****************************
	
	/** Quick Optimistic Item fustrum culling using boundingRadius. Returns true only if the item lies completely outside the view-fustrum */
	private boolean isItemFustrumCulled(float x, float y, float z, NitrogenContext context)
	{	
		float boundingRadiusCache = sisi.boundingRadius;
		
		// calculate optimistic distance from viewpoint
		float dist = boundingRadiusCache - z;
		float allowedRightness = context.xClip * dist;
		float allowedDownness = context.yClip * dist;
		
		// near clip
		if(dist < context.nearClip)return(true);
		
		// far clip
		if(dist > context.farClip)return(true);
		
		// right clip
		if((x - boundingRadiusCache) > allowedRightness)return (true);

		// left clip
		if((-x - boundingRadiusCache) > allowedRightness)return (true);

		// bottom clip
		if((y - boundingRadiusCache) > allowedDownness)return (true);

		// top clip
		if((-y - boundingRadiusCache) > allowedDownness)return (true);
		
		return  (false);
	}
	
	/** Calculates using boundingRadius if the Item touches any planes of the view-fustrum, then sets flags to improve the efficiency of polygon clipping. A Pessimistic approximation used. 
	 *  @param x x-coordinate of centre of Item
	 *  @param y y-coordinate of centre of Item
	 *  @param z z-coordinate of centre of Item
	 *  @param context The NitrogenContext object that defines the fustrum.
	 *  */
	private void calculateItemFustrumFlags(float x, float y, float z, NitrogenContext context)
	{
		int fustrumTouchCountCache = 0;
		float boundingRadiusCache = sisi.boundingRadius;
		
		// calculate pessimistic distance from viewpoint
		float dist = -z - boundingRadiusCache;
		float allowedRightness = context.xClip * dist;
		float allowedDownness = context.yClip * dist;	
		
		// near clip
		if(dist < context.nearClip)
		{
			fustrumTouchCountCache++;
			touchedNear = true;
		}
		else
		{touchedNear = false;}		
		
		// right clip
		if((x + boundingRadiusCache) > allowedRightness)
		{
			fustrumTouchCountCache++;
			touchedRight = true;			
		}
		else
		{touchedRight = false;}

		// left clip
		if((-x + boundingRadiusCache) > allowedRightness)
		{
			fustrumTouchCountCache++;
			touchedLeft = true;			
		}
		else
		{touchedLeft = false;}
		
		// bottom clip
		if((y + boundingRadiusCache) > allowedDownness)
		{
			fustrumTouchCountCache++;
			touchedBottom = true;			
		}
		else
		{touchedBottom = false;}
		
		// top clip
		if((-y + boundingRadiusCache) > allowedDownness)
		{
			fustrumTouchCountCache++;
			touchedTop = true;			
		}
		else
		{touchedTop = false;}
		
		
		
		this.fustrumTouchCount = fustrumTouchCountCache;	
	}
	
	/** Examines rotationNeedsUpdate and translationNeedsUpdate flags then if necessary informs all the Items backsides and vertexs that they have moved 
	 * . Also as a side-effect it clears the aforementioned flags */
	private void lazyComputeBacksidesAndVertexs()
	{
		Backside b;
		Vert v;
		if(rotationNeedsUpdate)
		{
			int backsidesLength = backsides.length;
			for(int x =0; x < backsidesLength; x++)
			{
				b = backsides[x];
				b.rotationNeedsUpdate = true;
				b.translationNeedsUpdate = true;
			}
			int vertexsLength = backsides.length;
			for(int x =0; x < vertexsLength; x++)
			{
				v = vertexs[x];
				v.rotationNeedsUpdate = true;
				v.translationNeedsUpdate = true;
			}
			// clear the flag
			rotationNeedsUpdate = false;
			translationNeedsUpdate = false;
			return;
		}
		
		// Its just a translation
		if(translationNeedsUpdate)
		{
			int backsidesLength = backsides.length;
			for(int x =0; x < backsidesLength; x++) backsides[x].translationNeedsUpdate = true;

			int vertexsLength = backsides.length;
			for(int x =0; x < vertexsLength; x++)vertexs[x].translationNeedsUpdate = true;
			// clear the flag
			translationNeedsUpdate = false;
		}
	}
	
	/** Updates the Items whichRenderer field, for example a different NEAR_RENDERER could be used up close to add interpolation and a different FAR_RENDERER could
	 * be used at a distance to render using a fixed colour instead of texture for speed and to reduce aliasing artifacts. </br></br>This method also providing some hysteresis to prevent flickering*/
	private void selectWhichRenderer(float dist, SharedImmutableSubItem sisiCache)
	{
		int whichRendererCache = whichRenderer;
		
		if(whichRendererCache == NEAR_RENDERER)
		{
			if(dist > sisiCache.nearRendererDistPlus)
			{
				whichRendererCache = MID_RENDERER;
			}
		}
		
		if(whichRendererCache == MID_RENDERER)
		{
			if(dist < sisiCache.nearRendererDist)whichRendererCache = NEAR_RENDERER;
			if(dist > sisiCache.farRendererDistPlus)whichRendererCache = FAR_RENDERER;
		}
		
		if(whichRendererCache == FAR_RENDERER)
		{
			if(dist < sisiCache.farRendererDist)whichRendererCache = MID_RENDERER;		
		}
		
		// ensure this Item's whichRenderer is updated if there has been a change
		whichRenderer = whichRendererCache;
	}
	
	private final void updateRenderingFlags(float itemDist, SharedImmutableSubItem sisi)
	{
		// see if isImprovedDetail needs changing
		if(isImprovedDetail)
		{
			if(itemDist > sisi.improvedDetailDistPlus)isImprovedDetail = false;
		}
		else
		{
			if(itemDist <sisi.improvedDetailDist)isImprovedDetail = true;
		}
		
		if(isUsingHLPBreaking)
		{
			if(itemDist > sisi.hlpBreakingDistPlus)isUsingHLPBreaking = false;
		}
		else
		{
			if(itemDist > sisi.hlpBreakingDist)isUsingHLPBreaking = true;
		}
	}
	

	
	
	/** Sets a flag on the Item informing it that it has translated (moved) but not rotated, so on next render it will re-compute
	 * all its vertex view-space coordinates etc to account for the translation, then clear the flag.
	 */
	public final void setNeedsTranslationUpdating()
	{
		translationNeedsUpdate = true;
	}
	
	/** Sets flags on the Item informing it that it has rotated and possibly translated (moved) so on next render it will re-compute
	 * all its vertex view-space coordinates etc to account for it, then clear the flags*/
	public final void setNeedsTotallyUpdating()
	{
		// this causes updating of the offset of the vertexes from the Items origin
		// It also results in the tanslationNeedsUpdate flag on all backsides and vertexs being set
		rotationNeedsUpdate = true;		
		translationNeedsUpdate = true;		//lets selectWhichRenderer know that it has to do something
	}
	
	
	

}

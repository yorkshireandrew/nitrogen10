package nitrogen1;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Iterator;

/** An Item comprised of several polygons, TexMaps, Colour data, BSPPlanes 
 * that can be attached to a Transform object and thereby rendered into a 
 * NitrogenContext.
 * @author andrew
 *
 */
final public class Item implements Serializable{
	private static final long serialVersionUID = 1592586975370696804L;
	
	// Enumerations for whichRenderer
	static final int NEAR_RENDERER = 0;
	static final int MID_RENDERER = 1;
	static final int FAR_RENDERER = 2;
	
	/** Class used to encapsulate polygon clipping */
	static final PolygonClipper polygonClipper = new PolygonClipper();

	/** Class used to encapsulate polygon breaking into lower perspective levels */
	static final HLPBreaker  hlpBreaker = new HLPBreaker();
	
	/** Class used to source and sink the internal components of Items*/
	static ItemFactory itemFactory;

	/** Parent transform of this Item in the scene graph */
	private Transform parent = null;
	
	/** The immutable part of an Item that can be shared across many identical Items that differ only by scene graph position and visibility. */
	private SharedImmutableSubItem sisi;
		
	/** The Items computed Backsides. A Backsides consists of a position, view space coordinate and a direction vector, 
	 * they are used by polygons to determine visibility and which of their two faces is being viewed 
	 * serialisation is controlled by writeObject & readObject*/
	private transient Backside backsides[];
	
	/** The Items vertexes 
	 * serialisation is controlled by writeObject & readObject*/

	private transient Vertex vertexes[];
	
	/** The Items collision vertexes 
	 *serialisation is controlled by writeObject & readObject*/
	private boolean hasCollisionVertexes;
	private transient Vertex collisionVertexes[];
	
	// ************************************************
	// ********************** FLAGS *******************
	// ************************************************
	
	/** Set true if the item is visible, or false if the Item is invisible*/
	private boolean visibility = false;
	
	/** Set true by scene graph if rotation has occurred since last render call */
	private boolean rotationNeedsUpdate = true;
	
	/** Set true by scene graph if rotation or translation has occurred since last render call */
	private boolean translationNeedsUpdate = true;
	
	/** Count of how many fustrum planes the item may touch */
	transient private int fustrumTouchCount;
	
	/** Enumerated value that determines which renderer to use based on distance */
	private int whichRendererOld = NEAR_RENDERER;
		
	// Which fustrum planes the item may touch, used to improve efficiency of polygon clipping
	// note: Items remain visible then clip entirely once they cross the fustrum farClip distance
	transient private boolean touchedNear;
	transient private boolean touchedRight;
	transient private boolean touchedLeft;
	transient private boolean touchedTop;
	transient private boolean touchedBottom;
	
	/** Flag to render using improved detail polygons. This is a state field used to apply hysteresis */
	private boolean isImprovedDetail = false;
	
	/** Flag to use hlp breaking. This is a state field used to apply hysteresis */
	private boolean isUsingHLPBreaking = false;
	
	/** Flag to use billboard Orientation. This is a state field used to apply hysteresis */
	private boolean isUsingBillboardOriention = false;
	
	/** name of the Item */
	private String name;
	
	/** package scope flag so we can tell if the Item was rendered if asked */
	boolean wasRendered = false;
	
	/** package scope reference for use in factories LLL*/
	Item nextInList;
	
    /** default constructor used by factories to preallocate an Item */
    Item(){} 

    /** constructor used by factories to generate new Items on request
	 * @param in_sisi The SharedImmutableSubItem used to compose the Item
	 * @param t Transform the Item is to be attached to
	 * @param factory ItemFactory that shall provide the internal parts of the Item
	 * */
    Item(final SharedImmutableSubItem in_sisi, final Transform t)
    {
    	this();
    	this.initializeItem(in_sisi, t);
    }
    
    /** create an Item from the factory */
    final static public Item createItem(final SharedImmutableSubItem in_sisi, final Transform t)
    {
    	return (itemFactory.getItem(in_sisi, t));
    }
    
	public static ItemFactory getItemFactory() {
		return itemFactory;
	}

	public static void setItemFactory(ItemFactory itemFactory) {
		Item.itemFactory = itemFactory;
	}
	
    /** used by factories to re-use an Item
	 * @param in_sisi The SharedImmutableSubItem used to compose the Item
	 * @param t Transform the Item is to be attached to
	 */
	final void initializeItem(final SharedImmutableSubItem in_sisi, final Transform t)
	{
		if(t == null)return;
		
		parent = t;
		t.add(this);
		
		sisi = in_sisi;
		
		ItemFactory itemFactoryL = itemFactory;

		// create a blank backside array
		final ImmutableBackside[] inSISIImmutableBacksides = in_sisi.immutableBacksides;		
		final int backsideMax = inSISIImmutableBacksides.length;
		final Backside[] backsidesL = new Backside[backsideMax];
		for(int x = 0; x < backsideMax; x++)backsidesL[x] = itemFactoryL.getBackside(inSISIImmutableBacksides[x]);
		backsides = backsidesL;

		// create the vertexes array from the sisi ImmutableVertexs
		final ImmutableVertex[] inSISIImmutableVertexes = in_sisi.immutableVertexes;		
		final int vertexMax = inSISIImmutableVertexes.length;
		Vertex[] vertexesL = new Vertex[vertexMax];
		for(int x = 0; x < vertexMax; x++)vertexesL[x] = itemFactoryL.getVertex(inSISIImmutableVertexes[x]);
		vertexes = vertexesL;
		
		// create the collision vertexes array from the sisi ImmutableVertexs
		final ImmutableCollisionVertex[] inSISIImmutableCollisionVertexes = in_sisi.immutableCollisionVertexes;		
		final int collisionVertexMax = inSISIImmutableCollisionVertexes.length;
		final Vertex[] collisionVertexesL = new Vertex[collisionVertexMax];
		for(int x = 0; x < collisionVertexMax; x++)collisionVertexesL[x] = itemFactoryL.getVertex(inSISIImmutableCollisionVertexes[x]);	
		collisionVertexes = collisionVertexesL;
		if(collisionVertexMax > 0){hasCollisionVertexes = true;}
		else{hasCollisionVertexes = false;}
		
		// clear other flags
		visibility = false;
		rotationNeedsUpdate = true;
		translationNeedsUpdate = true;
		whichRendererOld = NEAR_RENDERER;
		isImprovedDetail = false;	
		isUsingHLPBreaking = false;
		isUsingBillboardOriention = false;
		name = null;
		wasRendered = false;
		nextInList = null;
	}
	
	final void copyFrom(Item i)
	{
		parent = i.parent;	
		sisi = i.sisi;
		backsides = i.backsides;
		vertexes = i.vertexes;
		collisionVertexes = i.collisionVertexes;
		visibility = i.visibility;
		rotationNeedsUpdate = i.rotationNeedsUpdate;
		translationNeedsUpdate = i.translationNeedsUpdate;
		whichRendererOld = i.whichRendererOld;
		isImprovedDetail = i.isImprovedDetail;	
		isUsingHLPBreaking = i.isUsingHLPBreaking;
		isUsingBillboardOriention = i.isUsingBillboardOriention;
		name = i.name;
		wasRendered = i.wasRendered;
		nextInList = i.nextInList;		
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
		System.out.println("rendering "+ name);
		// return if the Item is not set visible
		if(visibility == false)return;
		
		// return if the Item is fustrum culled using context
		if(isItemFustrumCulled(v14,v24,v34,context))return;
		
		// far plane cull using the Items own farPlane
		final float itemDist = -v34;
		final SharedImmutableSubItem 	sisiL = sisi;
		if(itemDist > sisiL.farPlane)return;

		// DEBUG
		context.itemsRendered++;
		
		// remember we were rendered in case anyone asks
		wasRendered = true;
		
		// inform context we are being rendered for PickingRenderer
		context.currentItem = this;
		
		//Cache values needed for rendering locally
		final int 		fustrumTouchCountL	= fustrumTouchCount;
		final boolean 	touchedNearL 		= touchedNear;
		final boolean 	touchedRightL 		= touchedRight;
		final boolean 	touchedLeftL		= touchedLeft;
		final boolean 	touchedTopL			= touchedTop;
		final boolean 	touchedBottomL  	= touchedBottom;
		
		// which near-mid-far renderer to use for this render
		int 		whichRendererIsIt = whichRendererOld;	// the near-mid-far renderer to use for rendering this item
		
		if(translationNeedsUpdate)
		{
			if(!isUsingBillboardOriention)
			{
				if(itemDist > sisiL.billboardOrientationDistPlus)
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
			}
			else
			{
				// inhibit rotation updates from scene graph 
				rotationNeedsUpdate = false;
				
				if(itemDist < sisiL.billboardOrientationDist)
				{
					// ensure the passed in scene graph orientation is applied
					rotationNeedsUpdate = true;
					
					// clear the billboard flag
					isUsingBillboardOriention = false;		
				}
			}
			
			// see if renderer needs changing
			whichRendererIsIt = selectWhichRenderer(itemDist,sisiL);			
			
			// update other flags used during rendering
			updateRenderingFlags(itemDist,sisiL);
		}
		
		calculateItemFustrumFlags(v14,v24,v34,context, sisiL);
		lazyComputeBacksidesAndVertexs();

		// Select the right number of polygons to render
		final int polyStart;
		final int polyFinish;
		if(isImprovedDetail)
		{
			polyStart = sisiL.improvedDetailPolyStart;
			polyFinish = sisiL.improvedDetailPolyFinish;
		}
		else
		{
			polyStart = sisiL.normalDetailPolyStart;
			polyFinish = sisiL.normalDetailPolyFinish;
		}
		
		ImmutablePolygon immutablePolygon;
		int backsideIndex;
		Backside backside;
		boolean immutablePolygonIsTransparentL;
		final boolean contextTransparencyPassL = context.transparencyPass;
		final boolean isUsingHLPBreakingL = isUsingHLPBreaking;
		
		/** True unless the Items SharedImmutableSubItem nearPlaneCrashBacksideOverride is true and the Item has also crashed into near Plane */
		final boolean noBacksideCullOverride = (!sisiL.nearPlaneCrashBacksideOverride) ||(!touchedNear);
		
		/** things pulled out of loop for optimisation */
		ImmutablePolygon[] sisiImmutablePolygons = sisiL.immutablePolygons;
		Vertex[] vertexesL = vertexes;
		boolean contextIsPicking = context.isPicking;
		for(int x = polyStart; x < polyFinish; x++)
		{
			//DEBUG
			System.out.println("--- ******************---");			
			System.out.println("--- RENDERING POLYGON ---"+x);
			System.out.println("--- ******************---");	
			
			immutablePolygon = sisiImmutablePolygons[x];
			
			// skip the polygon if its transparency is wrong for the pass
			immutablePolygonIsTransparentL = immutablePolygon.isTransparent;
			if(!contextTransparencyPassL &&  immutablePolygonIsTransparentL)continue;
			if( contextTransparencyPassL && !immutablePolygonIsTransparentL)continue;
			
			// calculate the polygons backside if necessary
			backsideIndex = immutablePolygon.backsideIndex;
			backside = backsides[backsideIndex];
			if(backside.translationNeedsUpdate)
			{
				backside.calculate(
						context,
						v11,v12,v13,v14,
						v21,v22,v23,v24,
						v31,v32,v33,v34);
			}
			
			if(backside.facingViewer())
			{
				// -- optimised to here --
				// Calculate the vertexes, then Pass the polygon on to the next process.
				Vertex v1 = vertexesL[immutablePolygon.c1];
				Vertex v2 = vertexesL[immutablePolygon.c2];
				Vertex v3 = vertexesL[immutablePolygon.c3];
				Vertex v4 = vertexesL[immutablePolygon.c4];				
				v1.calculateViewSpaceCoordinates(v11,v12,v13,v14,v21,v22,v23,v24,v31,v32,v33,v34);				
				v2.calculateViewSpaceCoordinates(v11,v12,v13,v14,v21,v22,v23,v24,v31,v32,v33,v34);				
				v3.calculateViewSpaceCoordinates(v11,v12,v13,v14,v21,v22,v23,v24,v31,v32,v33,v34);				
				v4.calculateViewSpaceCoordinates(v11,v12,v13,v14,v21,v22,v23,v24,v31,v32,v33,v34);				

				// move vertex data across from the immutable polygon
				v1.setAux(immutablePolygon.pvd_c1);
				v2.setAux(immutablePolygon.pvd_c2);
				v3.setAux(immutablePolygon.pvd_c3);
				v4.setAux(immutablePolygon.pvd_c4);
				
				PolygonClipper.prepareForNewPolygon();
				PolygonClipper.process(
						context,
						fustrumTouchCountL, 
						touchedNearL,
						touchedRightL,
						touchedLeftL,
						touchedTopL,
						touchedBottomL,					
						v1, 
						v2, 
						v3, 
						v4, 									

						immutablePolygon.getRenderer(whichRendererIsIt, contextIsPicking),
						immutablePolygon.polyData,
						immutablePolygon.textureMap,						
						backside.lightingValue,
						isUsingHLPBreakingL						
					);				
			}
			else
			{
				// Skip rendering the polygon if it is backside culled
				if(immutablePolygon.isBacksideCulled && noBacksideCullOverride)continue;
				
				// Calculate the vertexes, then Pass the polygon on to the next process.
				Vertex v1 = vertexesL[immutablePolygon.c1];
				Vertex v2 = vertexesL[immutablePolygon.c2];
				Vertex v3 = vertexesL[immutablePolygon.c3];
				Vertex v4 = vertexesL[immutablePolygon.c4];			
				v1.calculateViewSpaceCoordinates(v11,v12,v13,v14,v21,v22,v23,v24,v31,v32,v33,v34);				
				v2.calculateViewSpaceCoordinates(v11,v12,v13,v14,v21,v22,v23,v24,v31,v32,v33,v34);				
				v3.calculateViewSpaceCoordinates(v11,v12,v13,v14,v21,v22,v23,v24,v31,v32,v33,v34);				
				v4.calculateViewSpaceCoordinates(v11,v12,v13,v14,v21,v22,v23,v24,v31,v32,v33,v34);				

				// move vertex data across from the immutable polygon
				v1.setAux(immutablePolygon.pvd_c1);
				v2.setAux(immutablePolygon.pvd_c2);
				v3.setAux(immutablePolygon.pvd_c3);
				v4.setAux(immutablePolygon.pvd_c4);
				// Pass the polygon on to the next process, but reverse the ordering of the vertexes 
				// because the polygon is facing away, to ensure they occur in a clockwise direction		
				PolygonClipper.prepareForNewPolygon();
				PolygonClipper.process(
					context,
					fustrumTouchCountL, 
					touchedNearL,
					touchedRightL,
					touchedLeftL,
					touchedTopL,
					touchedBottomL,					
					v4, 
					v3, 
					v2, 
					v1,
					immutablePolygon.getRenderer(whichRendererIsIt,contextIsPicking),					
					immutablePolygon.polyData,
					immutablePolygon.textureMap,
					backside.lightingValue,
					isUsingHLPBreakingL
				);
			} //end of backside facing viewer if-else
		} //end of polygon rendering loop	
	}
	
	//**************************** END OF RENDER *****************************
	
	/** Quick Optimistic Item fustrum culling using boundingRadius. Returns true only if the item lies completely outside the view-fustrum */
	final private boolean isItemFustrumCulled(final float x, final float y, final float z, final NitrogenContext context)
	{	
		final float boundingRadiusCache = sisi.boundingRadius;
		
		// calculate optimistic distance from viewpoint
		final float dist = boundingRadiusCache - z;
		final float allowedRightness = context.xClip * dist;
		final float allowedDownness = context.yClip * dist;
		
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
	final private void calculateItemFustrumFlags(final float x, final float y, final float z, final NitrogenContext context, final SharedImmutableSubItem sisiL)
	{
		int fustrumTouchCountL = 0;
		final float sisiBoundingRadius = sisiL.boundingRadius;
		
		// calculate pessimistic distance from viewpoint
		final float dist = -z - sisiBoundingRadius;
		final float allowedRightness = context.xClip * dist;
		final float allowedDownness = context.yClip * dist;	
		
		// near clip
		if(dist < context.nearClip)
		{
			fustrumTouchCountL++;
			touchedNear = true;
		}
		else
		{touchedNear = false;}		
		
		// right clip
		if((x + sisiBoundingRadius) > allowedRightness)
		{
			fustrumTouchCountL++;
			touchedRight = true;			
		}
		else
		{touchedRight = false;}

		// left clip
		if((-x + sisiBoundingRadius) > allowedRightness)
		{
			fustrumTouchCountL++;
			touchedLeft = true;			
		}
		else
		{touchedLeft = false;}
		
		// bottom clip
		if((y + sisiBoundingRadius) > allowedDownness)
		{
			fustrumTouchCountL++;
			touchedBottom = true;			
		}
		else
		{touchedBottom = false;}
		
		// top clip
		if((-y + sisiBoundingRadius) > allowedDownness)
		{
			fustrumTouchCountL++;
			touchedTop = true;			
		}
		else
		{touchedTop = false;}
			
		fustrumTouchCount = fustrumTouchCountL;	
	}
	
	/** Examines rotationNeedsUpdate and translationNeedsUpdate flags then if necessary informs all the Items backsides and vertexs that they have moved 
	 * . Also as a side-effect it clears the aforementioned flags */
	final private void lazyComputeBacksidesAndVertexs()
	{
		Backside b;
		Vertex v;
		if(rotationNeedsUpdate)
		{
			final int backsidesLength = backsides.length;
			for(int x = 0; x < backsidesLength; x++)
			{
				b = backsides[x];
				b.rotationNeedsUpdate = true;
				b.translationNeedsUpdate = true;
			}
			
			final int vertexsLength = vertexes.length;
			for(int x = 0; x < vertexsLength; x++)
			{
				v = vertexes[x];
				v.rotationNeedsUpdate = true;
				v.translationNeedsUpdate = true;
			}
			
			if(hasCollisionVertexes)
			{
				Vertex cv;
				final int collisionVertexesLength = collisionVertexes.length;
				for(int x = 0; x < collisionVertexesLength; x++)
				{
					cv = collisionVertexes[x];
					cv.rotationNeedsUpdate = true;
					cv.translationNeedsUpdate = true;
				}
			}
			// clear the flag
			rotationNeedsUpdate = false;
			translationNeedsUpdate = false;
			return;
		}
		
		// Its just a translation
		if(translationNeedsUpdate)
		{
			final int backsidesLength = backsides.length;
			for(int x =0; x < backsidesLength; x++) backsides[x].translationNeedsUpdate = true;

			final int vertexsLength = vertexes.length;
			for(int x =0; x < vertexsLength; x++)vertexes[x].translationNeedsUpdate = true;

			if(hasCollisionVertexes)
			{
				Vertex cv;
				final int collisionVertexesLength = collisionVertexes.length;
				for(int x =0; x < collisionVertexesLength; x++)
				{
					cv = collisionVertexes[x];
					cv.translationNeedsUpdate = true;
				}
			}			
			// clear the flag
			translationNeedsUpdate = false;
		}
	}
	
	/** Updates the Items whichRenderer field, for example a different NEAR_RENDERER could be used up close to add interpolation and a different FAR_RENDERER could
	 * be used at a distance to render using a fixed colour instead of texture for speed and to reduce aliasing artifacts. </br></br>This method also providing some hysteresis to prevent flickering*/
	final private int selectWhichRenderer(final float dist, final SharedImmutableSubItem sisiL)
	{
		
		int whichRendererNew = whichRendererOld; 
		
		switch(whichRendererOld)
		{
			case NEAR_RENDERER:
				if(dist > sisiL.nearRendererDistPlus)whichRendererNew = MID_RENDERER;
				break;

			case MID_RENDERER:
				if(dist < sisiL.nearRendererDist)whichRendererNew = NEAR_RENDERER;
				if(dist > sisiL.farRendererDistPlus)whichRendererNew = FAR_RENDERER;
				break;
				
			case FAR_RENDERER:
				if(dist < sisiL.farRendererDist)whichRendererNew = MID_RENDERER;		
				break;
				
			default: whichRendererNew = NEAR_RENDERER;	
		}
		
		// ensure this Item's whichRenderer is updated if there has been a change
		whichRendererOld = whichRendererNew;
		return whichRendererNew;
	}
	
	final private void updateRenderingFlags(final float itemDist, final SharedImmutableSubItem sisi)
	{
		// see if isImprovedDetail needs changing
		if(isImprovedDetail)
		{
			if(itemDist > sisi.improvedDetailDistPlus)isImprovedDetail = false;
		}
		else
		{
			if(itemDist < sisi.improvedDetailDist)isImprovedDetail = true;
		}
		
		if(isUsingHLPBreaking)
		{
			if(itemDist > sisi.hlpBreakingDistPlus)isUsingHLPBreaking = false;
		}
		else
		{
			if(itemDist < sisi.hlpBreakingDist)isUsingHLPBreaking = true;
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
		// It also results in the translationNeedsUpdate flag on all backsides and vertexs being set
		rotationNeedsUpdate = true;		
		translationNeedsUpdate = true;		//lets selectWhichRenderer know that it has to do something
	}
	
	final public void setVisibility(boolean in)
	{
		if(visibility == in)return;
		if(visibility)
		{
			// we are visible so make invisible
			if(parent != null)parent.decreaseVisibleChildrenBy(1);
			visibility = false;
		}
		else
		{
			// we are invisible so make visible
			if(parent != null)parent.increaseVisibleChildrenBy(1);
			visibility = true;			
		}
	}
	
	/** Access method 
	 * @return The parent of the called  Transform, or null if it is the scenegraph root*/
	final public Transform getParent() {return(parent);}
	
	/** Access method setting the parent of the called Transform, breaking any existing parental bond
	 * if it exists. 
	 * @param new_parent The Transform to be set as the parent of the called Transform*/
	final public void setParent(Transform new_parent) 
	{
		// Detach from existing parent 
		if(parent != null)
		{			
			parent.remove(this);			
		}
		
		if(new_parent != null)
		{
			// Attach to new parent
			parent = new_parent;
			parent.add(this);
		}
		else
		{
			parent = null;
		}
		
		// Ensure the resulting scene-graph branch gets updated on the next render
		setNeedsTotallyUpdating();
	}
	
	final public boolean isVisible(){return visibility;}

	final public String getName() {
		return name;
	}

	final public void setName(String name) {
		this.name = name;
	}
	
	/** returns true if the Item was rendered (not fustrum culled)
	 * since the last call to clearWasRenderedFlags() higher up the scene graph
	 */
	final public boolean wasRendered(){return wasRendered;}
	
	final private void calculateCollisionVertexes()
	{
		if(!hasCollisionVertexes)return;
		if(rotationNeedsUpdate || translationNeedsUpdate)
		{
			Transform parentL = parent;
			// ensure parent Transform is up to date
			parentL.updateViewSpace();			
			
			// mark the collisionVertexes for computation
			lazyComputeBacksidesAndVertexs();
			
			float pc11 = parentL.c11;
			float pc12 = parentL.c12;
			float pc13 = parentL.c13;
			float pc14 = parentL.c14;
			
			float pc21 = parentL.c21;
			float pc22 = parentL.c22;
			float pc23 = parentL.c23;
			float pc24 = parentL.c24;
			
			float pc31 = parentL.c31;
			float pc32 = parentL.c32;
			float pc33 = parentL.c33;
			float pc34 = parentL.c34;
			
			Vertex[] collisionVertexesL = collisionVertexes;
			int collisionVertexLength = collisionVertexesL.length;
			for(int i = 0; i < collisionVertexLength; i++)
			{
				collisionVertexesL[i].calculateViewSpaceCoordinates(pc11,pc12,pc13,pc14,pc21,pc22,pc23,pc24,pc31,pc32,pc33,pc34);
			}
		}
	}
	
	/** package scope helper so that factories can recycle an Items Backsides */
	final Backside[] getBacksides()
	{
		return backsides;
	}
	
	/** package scope helper so that factories can recycle an Items Vertexes */
	final Vertex[] getVertexes()
	{
		return vertexes;
	}
	
	/** package scope helper so that factories can recycle an Items Vertexes */
	final Vertex[] getCollisionVertexes()
	{
		return collisionVertexes;
	}
	
	final public void recycle()
	{
		// tell our parents we are going
		if(parent != null)parent.remove(this);
		itemFactory.recycle(this);
		
		// ensure garbage collector can collect 
		//any none recycled components of this Item
		backsides  = null;
		vertexes = null;
	}
	/*
	final private void writeObject(ObjectOutputStream out) throws IOException, ClassNotFoundException
	{
    	System.out.println("writing Item:"+ this.getName());
    	out.defaultWriteObject();
    	
	}
	*/
	
	final private void writeObject(ObjectOutputStream out) throws IOException, ClassNotFoundException
	{
    	System.out.println("writing Item:"+ this.getName());
    	out.defaultWriteObject();
    	
    	// write the backsides
    	int backsideLength = backsides.length;
    	out.writeInt(backsideLength);

    	// write the vertexes
    	int vertexesLength = vertexes.length;
    	out.writeInt(vertexesLength);
    	
    	// write the collision vertexes
    	int collisionVertexesLength = collisionVertexes.length;
    	out.writeInt(collisionVertexesLength);

	}

    final private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
	{
    	in.defaultReadObject();
    	System.out.println("reading Item:"+ this.getName());
    	
    	SharedImmutableSubItem sisiL = sisi;
    	
    	// read and generate the backsides
    	int backsideLength = in.readInt();
    	backsides = new Backside[backsideLength];
    	ImmutableBackside[] ibs = sisiL.immutableBacksides;
    	for(int i = 0 ; i < backsideLength; i++)
    	{
    		backsides[i] = itemFactory.getBackside(ibs[i]);
    	}
    	
    	// read and generate the vertexes
    	int vertexesLength = in.readInt();
    	vertexes = new Vertex[vertexesLength];
    	ImmutableVertex[] ivs = sisiL.immutableVertexes;
    	for(int i = 0 ; i < vertexesLength; i++)
    	{
    		vertexes[i] = itemFactory.getVertex(ivs[i]);
    	}
    	
    	// read and generate the collisionVertexes
    	int collisonVertexesLength = in.readInt();
    	collisionVertexes = new Vertex[collisonVertexesLength];
    	ImmutableCollisionVertex[] icvs = sisiL.immutableCollisionVertexes;
    	for(int i = 0 ; i < collisonVertexesLength; i++)
    	{
    		collisionVertexes[i] = itemFactory.getVertex(icvs[i]);
    	}   	
	}
/*	
    final private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
    {
    	System.out.println("reading an Item");
    	in.defaultReadObject();
    	System.out.println("The Items name is :" + this.name);
    }  
    */
	
	/** returns an Iterator<Vert> for enumerating the Items collision vertexes */
	final public Iterator<Vertex> getCollisionVertexIterator()
	{
		calculateCollisionVertexes();
		return (new Iterator<Vertex>(){
			private int index = 0;
			private int collisionVertexesMax = collisionVertexes.length;

			@Override
			public Vertex next() {
				return collisionVertexes[index++];
			}
			
			@Override
			public boolean hasNext() {
				
				// we only have vertexes if we are visible
				if(!isVisible())return false;
				if(index < collisionVertexesMax)return true;
				return false;			
			}

			@Override
			public void remove() {
				// TODO Auto-generated method stub		
			}		
		});
	}
}

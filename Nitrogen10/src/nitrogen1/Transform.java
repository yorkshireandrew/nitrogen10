package nitrogen1;
import java.lang.Math;

/** component of the scene graph that encapsulates translation and orientation */
public class Transform{
	private static final float[]	sinTable;
	private static final float[]	cosTable;
	
	/** The actual values of this transform */
	float 			a11,a12,a13,a14;
	float 			a21,a22,a23,a24;
	float 			a31,a32,a33,a34;
	
	/** Calculated view space transform  */
	float 			c11,c12,c13,c14;
	float 			c21,c22,c23,c24;
	float 			c31,c32,c33,c34;
	
	/** Flag indicating if the transforms translation needs to be computed */
	private boolean 		translationNeedsUpdate = true;
	
	/** Flag indicating the transforms rotation needs to be computed */	
	private boolean 		rotationNeedsUpdate = true;
	
	// Counter to ensure the view-space transform only gets calculated
	// if this transform has visible children 
	/** Number of visible children */
	private int 			numberOfVisibleChildren = 0;
	
	/** The Parent Transform of this transform */
	private Transform 		parent;
	
	/** Holds any child transforms of this transform - can be null */
	private TransformVector childTransforms;
	
	/** child items of this transform - always an ItemVector but it may be empty */
	private final ItemVector		childItems = new ItemVector();		
	
	/** outer name */
	private String outerName;
	/** inner name */
	private String innerName;
	
	static{
		sinTable = new float[3601];
		cosTable = new float[3601];
		double degToRad = (Math.PI/1800);
		for(int i = 0; i <=3600; i++)
		{
			sinTable[i] = (float)Math.sin(i*degToRad);
			cosTable[i] = (float)Math.cos(i*degToRad);
		}
	}
	/** Creates a new unity Transform with the parameter Transform as its parent
	 * 
	 * @param parent The parent of the created Transform. Use null if the Transform is the root of a scene graph
	 */
	public Transform(Transform parent)
	{
		setParent(parent);		
	}

	/** Creates a new Transform with the parameter Transform as its parent
	 * 
	 * @param parent The parent of the created Transform. Use null if the Transform is the root of a scene graph.
	 * @param a11-a34 floating point values representing the transform.
	 */
	public Transform(	Transform parent,
			float a11, float a12, float a13, float a14,
			float a21, float a22, float a23, float a24,
			float a31, float a32, float a33, float a34
			)
			{
				setParent(parent);
				this.a11 = a11; this.a12=a12; this.a13=a13; this.a14=a14;
				this.a21 = a21; this.a22=a22; this.a23=a23; this.a24=a24;
				this.a31 = a31; this.a32=a32; this.a33=a33; this.a34=a34;
			}
	
	/** Adds a transform to this transforms child transform list. This is an internal method that does not detach the supplied transform from any parents it may already have; external code should use the setParent method
	 * @param t Transform to be added as a child to the Transform being called */
	private void add(Transform t)
	{
		// add passed in transform to childTransforms, creating it if it is null
		if (childTransforms == null)childTransforms = new TransformVector();
		childTransforms.addElement(t);
		
		// account for visible children
		int novc = t.numberOfVisibleChildren;
		if(novc > 0)increaseVisibleChildrenBy(novc);
	}
	
	/** Causes the called Transform to first update itself using 
	 * the calculated transform values of its parent that must be passed
	 * in as parameters.
	 * 
	 * It then renders any visible Items it has before calling this
	 * method on all of its child Transforms
	 * @param p11 to p12 are the calculated parent transform values that must be passed in.
	 * @param context The NitrogenContext to render Items into. 
	 */
	private final void render(
			NitrogenContext context,
			float p11, float p12, float p13, float p14,
			float p21, float p22, float p23, float p24,
			float p31, float p32, float p33, float p34
			)
	{		
		// avoid doing any work if we can help it!
		if(numberOfVisibleChildren < 1)return;
		
		// copy this transforms a values locally for speed
		float la11 = a11;
		float la12 = a12;
		float la13 = a13;
		float la14 = a14;
		
		float la21 = a21;
		float la22 = a22;
		float la23 = a23;
		float la24 = a24;
		
		float la31 = a31;
		float la32 = a32;
		float la33 = a33;
		float la34 = a34;
		
		if(rotationNeedsUpdate)
		{
			c11 = p11*la11+p12*la21+p13*la31;
			c12 = p11*la12+p12*la22+p13*la32;
			c13 = p11*la13+p12*la23+p13*la33;

			c21 = p21*la11+p22*la21+p23*la31;
			c22 = p21*la12+p22*la22+p23*la32;
			c23 = p21*la13+p22*la23+p23*la33;

			c31 = p31*la11+p32*la21+p33*la31;
			c32 = p31*la12+p32*la22+p33*la32;
			c33 = p31*la13+p32*la23+p33*la33;
			
			rotationNeedsUpdate = false;		
		}
		if(translationNeedsUpdate)
		{
			c14 = p11*la14+p12*la24+p13*la34+p14;
			c24 = p21*la14+p22*la24+p23*la34+p24;
			c34 = p31*la14+p32*la24+p33*la34+p34;
			translationNeedsUpdate = false;
		}

		// copy this transforms c values locally for speed
		// note: reusing laxx local variables
		la11 = c11;
		la12 = c12;
		la13 = c13;
		la14 = c14;
		
		la21 = c21;
		la22 = c22;
		la23 = c23;
		la24 = c24;
		
		la31 = c31;
		la32 = c32;
		la33 = c33;
		la34 = c34;
		
		// first render our own child Items
		int ci = childItems.size();
		for(int i = 0; i < ci; i++)childItems.elementAt(i).renderItem(context,la11,la12,la13,la14,la21,la22,la23,la24,la31,la32,la33,la34);
		
		// now instruct child transforms to calculate themselves and render themselves
		if(childTransforms != null)
		{
			int ct = childTransforms.size();
			for(int i = 0; i < ct; i++)childTransforms.elementAt(i).render(context,la11,la12,la13,la14,la21,la22,la23,la24,la31,la32,la33,la34);
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
			parent.decreaseVisibleChildrenBy(numberOfVisibleChildren);
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
	
	/** removes the given child transform 
	 * @param t The transform to remove
	 * @return Returns true if the transform was found and removed 
	 * */
	final public boolean remove(Transform t)
	{
		if(childTransforms.removeElement(t) == true)
		{
			decreaseVisibleChildrenBy(t.numberOfVisibleChildren);
			return true;
		}
		return false;
	}	


	
	/** Adds an Item to the transforms child item list 
	 *  @param i Item to be added as a child to the Transform being called */
	final public void add(Item i)
	{
		childItems.addElement(i);
		
		if(i.isVisible())increaseVisibleChildrenBy(1);
	}
	
	
	/** removes the given child Item
	 * @param i The transform to remove
	 * @return Returns true if the transform was found and removed 
	 * */
	final public boolean remove(Item i)
	{
		if(childItems.removeElement(i) == true)
		{
			if(i.isVisible())decreaseVisibleChildrenBy(1);
			return true;
		}
		return false;
	}
	
	/** increases the numberOfVisibleChildren in this transform
	 *  as well as any above it in the scenegraph, by n
	 * @param n How much to increase the count by
	 */
	final void increaseVisibleChildrenBy(int n)
	{
		numberOfVisibleChildren += n;
		if(parent != null)parent.increaseVisibleChildrenBy(n);
	}
	
	/** decreases the numberOfVisibleChildren in this transform
	 *  as well as any above it in the scenegraph, by n
	 * @param n How much to decrease the count by
	 */
	final void decreaseVisibleChildrenBy(int n)
	{
		numberOfVisibleChildren -= n;
		if(parent != null)parent.decreaseVisibleChildrenBy(n);
	}
	
	/** Causes the complete viewspace transform for this 
	 * and all its child transforms to be updated on next render.
	 * <br /><br />
	 * Needs to be called if the transform matrix (a) has been replaced or matrix (a)
	 * has both been both rotated and translated.
	 */
	final public void setNeedsTotallyUpdating()
	{
		translationNeedsUpdate = true;
		rotationNeedsUpdate = true;
		
		// inform any child Transforms they need totally updating too
		if(childTransforms != null)
		{
			int ctsize = childTransforms.size();
			for(int i = 0; i < ctsize; i++)childTransforms.elementAt(i).setNeedsTotallyUpdating();
		}
		
		// also inform child items they need totally updating
		int cisize = childItems.size();
		for(int i = 0; i < cisize; i++)childItems.elementAt(i).setNeedsTotallyUpdating();	
	}
	
	/** Causes the viewspace transform for this 
	 * and all its child transforms to be updated on next render
	 * to reflect a rotation change to the (a) matrix
	 * <br /><br />
	 * Needs to be called if the transform matrix (a) has rotated.
	 */
	final public void setNeedsRotationUpdating()
	{
		// this Transform need only update its calculated rotation
		rotationNeedsUpdate = true;
		
		// inform any child Transforms they need totally updating
		if(childTransforms != null)
		{
			int ctsize = childTransforms.size();
			for(int i = 0; i< ctsize; i++)childTransforms.elementAt(i).setNeedsTotallyUpdating();
		}
		
		// also inform child items they need totally updating
		int cisize = childItems.size();
		for(int i = 0; i< cisize; i++)childItems.elementAt(i).setNeedsTotallyUpdating();					
	}
	
	/** Causes the view space transform for this 
	 * and all its child transforms to be updated on next render
	 * to reflect a translation change to the (a) matrix
	 * <br /><br />
	 * Needs to be called if the transform matrix (a) has translated (moved about) but not rotated.
	 */
	final public void setNeedsTranslationUpdating()
	{
		// this Transform need only update its calculated translation
		translationNeedsUpdate = true;
		
		// inform any child Transforms they need their translation updating
		if(childTransforms != null)
		{
			int ctsize = childTransforms.size();
			for(int i = 0; i< ctsize; i++)childTransforms.elementAt(i).setNeedsTranslationUpdating();
		}
		
		// also inform child items they need their translation updating
		int cisize = childItems.size();
		for(int i = 0; i< cisize; i++)childItems.elementAt(i).setNeedsTranslationUpdating();		
	}
	
	//***************************************************************
	//                       RENDER START
	//***************************************************************
	/** Renders everything visible in or above the called transform
	 * using the passed in context
	 * @param context The NitrogenContext to render Items into
	 */
	final public void render(NitrogenContext context){
		updateViewSpace();
		
		// copy this transforms c values locally for speed
		float n11 = c11;
		float n12 = c12;
		float n13 = c13;
		float n14 = c14;
		
		float n21 = c21;
		float n22 = c22;
		float n23 = c23;
		float n24 = c24;
		
		float n31 = c31;
		float n32 = c32;
		float n33 = c33;
		float n34 = c34;
		
		// first render our own child Items
		int ci = childItems.size();
		for(int i = 0; i < ci; i++)childItems.elementAt(i).renderItem(context,n11,n12,n13,n14,n21,n22,n23,n24,n31,n32,n33,n34);
		
		// now instruct child transforms to calculate themselves and render themselves
		if(childTransforms != null)
		{
			int ct = childTransforms.size();
			for(int i = 0; i < ct; i++)childTransforms.elementAt(i).render(context,n11,n12,n13,n14,n21,n22,n23,n24,n31,n32,n33,n34);
		}
	}
	
	/** If rotationNeedsUpdate or translationNeedsUpdate flags are set
	 *  this method causes updates the called transform as well as all 
	 *  (parent)transforms between it and the root of the scene graph
	 */
	final public void updateViewSpace()
	{
		Transform parentL = parent;
		if(parentL != null)
		{
			// ensure parents are uptodate
			parentL.updateViewSpace();
			
			// copy parents c values locally for speed
			float p11 = parentL.c11;
			float p12 = parentL.c12;
			float p13 = parentL.c13;			
			float p14 = parentL.c14;
			
			float p21 = parentL.c21;
			float p22 = parentL.c22;
			float p23 = parentL.c23;
			float p24 = parentL.c24;	
			
			float p31 = parentL.c31;
			float p32 = parentL.c32;
			float p33 = parentL.c33;
			float p34 = parentL.c34;
			
			// copy this transforms a values locally for speed
			float a11L = a11;
			float a12L = a12;
			float a13L = a13;
			float a14L = a14;
			
			float a21L = a21;
			float a22L = a22;
			float a23L = a23;
			float a24L = a24;
			
			float a31L = a31;
			float a32L = a32;
			float a33L = a33;
			float a34L = a34;
			
			if(rotationNeedsUpdate)
			{
				c11 = p11*a11L+p12*a21L+p13*a31L;
				c12 = p11*a12L+p12*a22L+p13*a32L;
				c13 = p11*a13L+p12*a23L+p13*a33L;

				c21 = p21*a11L+p22*a21L+p23*a31L;
				c22 = p21*a12L+p22*a22L+p23*a32L;
				c23 = p21*a13L+p22*a23L+p23*a33L;

				c31 = p31*a11L+p32*a21L+p33*a31L;
				c32 = p31*a12L+p32*a22L+p33*a32L;
				c33 = p31*a13L+p32*a23L+p33*a33L;
				
				rotationNeedsUpdate = false;		
			}
			if(translationNeedsUpdate)
			{
				c14 = p11*a14L+p12*a24L+p13*a34L+p14;
				c24 = p21*a14L+p22*a24L+p23*a34L+p24;
				c34 = p31*a14L+p32*a24L+p33*a34L+p34;
				translationNeedsUpdate = false;
			}	
		}
		else
		{
			setSelfAsRoot();
			rotationNeedsUpdate = false;
			translationNeedsUpdate = false;
			return;			
		}
	}


		
	final private void setSelfAsRoot()
	{
		// This is the root of the scene graph 
		// so its value is the unity matrix at origin
		c11=1;c12=0;c13=0;c14=0;
		c21=0;c22=1;c23=0;c24=0;
		c31=0;c32=0;c33=1;c34=0;
		rotationNeedsUpdate = false;
		translationNeedsUpdate = false;
	}
	
	/** Makes the transform a roll rotation about its z axis
	 * 
	 * @param theta Radian value for roll
	 */
	final public void setRoll(float theta)
	{
		a11 = (float)Math.cos(theta);
		a12 = (float)-Math.sin(theta);
		a13 = 0.0f;
		a21 = (float)Math.sin(theta);
		a22 = (float)Math.cos(theta);
		a23 = 0.0f;
		a31 = 0.0f;
		a32 = 0.0f;
		a33 = 1.0f;
		setNeedsTotallyUpdating();
	}
	
	/** Makes the transform a turn rotation about its y axis
	 * 
	 * @param theta Radian value for turn
	 */
	final public void setTurn(float theta)
	{
		a11 = (float)Math.cos(theta);
		a12 = 0.0f;
		a13 = (float)-Math.sin(theta);
		
		a21 = 0.0f;
		a22 = 1.0f;
		a23 = 0.0f;		
		
		a31 = (float)Math.sin(theta);
		a32 = 0.0f;
		a33 = (float)Math.cos(theta);

		setNeedsTotallyUpdating();
	}
	
	/** Makes the transform a climb rotation about its x axis
	 * 
	 * @param theta Radian value for climb
	 */	
	final public void setClimb(float theta)
	{
		a11 = 1.0f;
		a12 = 0.0f;
		a13 = 0.0f;
		
		a21 = 0.0f;
		a22 = (float)Math.cos(theta);
		a23 = (float)-Math.sin(theta);;		
		
		a31 = 0.0f;
		a32 = (float)Math.sin(theta);
		a33 = (float)Math.cos(theta);

		setNeedsTotallyUpdating();
	}	
	
	
	/** Makes the transform a roll rotation about its z axis, using a lookup table
	 * 
	 * @param theta integer value indicating the roll in degrees x 10
	 */
	final public void setRoll(int theta)
	{
		// normalise the angle
		while(theta < 0)theta +=3600;
		while(theta > 3600)theta -=3600;
		
		a11 = cosTable[theta];
		a12 = -sinTable[theta];
		a13 = 0.0f;
		a21 = sinTable[theta];
		a22 = cosTable[theta];
		a23 = 0.0f;
		a31 = 0.0f;
		a32 = 0.0f;
		a33 = 1.0f;
		setNeedsTotallyUpdating();
	}
	
	/** Makes the transform a turn rotation about its y axis, using a lookup table
	 * 
	 * @param theta integer value indicating the turn in degrees x 10
	 */
	final public void setTurn(int theta)
	{
		// normalise the angle
		while(theta < 0)theta +=3600;
		while(theta > 3600)theta -=3600;
		
		a11 = cosTable[theta];
		a12 = 0.0f;
		a13 = -sinTable[theta];
		
		a21 = 0.0f;
		a22 = 1.0f;
		a23 = 0.0f;		
		
		a31 = sinTable[theta];
		a32 = 0.0f;
		a33 = cosTable[theta];

		setNeedsTotallyUpdating();
	}
	
	/** Makes the transform a climb rotation about its x axis, using a lookup table
	 * 
	 * @param theta integer value indicating the turn in degrees x 10
	 */
	final public void setClimb(int theta)
	{
		// normalise the angle
		while(theta < 0)theta +=3600;
		while(theta > 3600)theta -=3600;
		
		a11 = 1.0f;
		a12 = 0.0f;
		a13 = 0.0f;
		
		a21 = 0.0f;
		a22 = cosTable[theta];
		a23 = -sinTable[theta];		
		
		a31 = 0.0f;
		a32 = sinTable[theta];
		a33 = cosTable[theta];

		setNeedsTotallyUpdating();
	}
	
	final public float xAxisScalarProduct(Transform target)
	{
		updateViewSpace();
		target.updateViewSpace();
		
		float dx = target.c14 - this.c14;
		float dy = target.c24 - this.c24;
		float dz = target.c34 - this.c34;
		
		return ((dx * this.c11)+(dy * this.c21)+ (dz * this.c31));	
	}
	
	final public float yAxisScalarProduct(Transform target)
	{
		updateViewSpace();
		target.updateViewSpace();
		
		float dx = target.c14 - this.c14;
		float dy = target.c24 - this.c24;
		float dz = target.c34 - this.c34;
		
		return ((dx * this.c21)+(dy * this.c22)+ (dz * this.c23));	
	}
	
	final public float zAxisScalarProduct(Transform target)
	{
		updateViewSpace();
		target.updateViewSpace();
		
		float dx = target.c14 - this.c14;
		float dy = target.c24 - this.c24;
		float dz = target.c34 - this.c34;
		
		return ((dx * this.c31)+(dy * this.c32)+ (dz * this.c33));	
	}
	
	final public float dist(Transform target)
	{
		updateViewSpace();
		target.updateViewSpace();
		
		float dx = target.c14 - this.c14;
		float dy = target.c24 - this.c24;
		float dz = target.c34 - this.c34;
		return ((float)Math.sqrt(dx*dx+dy*dy+dz*dz));
	}

	public String getOuterName() {
		return outerName;
	}

	final public void setOuterName(String outerName) {
		this.outerName = outerName;
	}

	final public String getInnerName() {
		return innerName;
	}

	final public void setInnerName(String innerName) {
		this.innerName = innerName;
	}
	
	final public Transform findTransformNamed(String target)
	{
		Transform retval;
		if(this.outerName.equals(target))return(this);
		if(childTransforms != null)
		{
			int cts = childTransforms.size();
			for(int i = 0; i < cts; i++)
			{
				retval = childTransforms.elementAt(i).findTransformNamed(target);
				if(retval != null)return(retval);
			}
		}
		return(null);
	}
	
	final public Transform findTransformInnerNamed(String target)
	{
		Transform retval;
		if(this.innerName.equals(target))return(this);
		if(childTransforms != null)
		{
			int cts = childTransforms.size();
			for(int i = 0; i < cts; i++)
			{
				retval = childTransforms.elementAt(i).findTransformNamed(target);
				if(retval != null)return(retval);
			}
		}
		return(null);
	}
	
	final public Item findItemNamed(String target)
	{
		Item retval;
		
		int maxItems = childItems.size();
		for(int i = 0; i < maxItems; i++)
		{
			retval = childItems.elementAt(i);
			if(retval.getName().equals(target))return(retval);
		}
		
		if(childTransforms != null)
		{
			int cts = childTransforms.size();
			for(int i = 0; i < cts; i++)
			{
				retval = childTransforms.elementAt(i).findItemNamed(target);
				if(retval != null)return(retval);
			}
		}
		return(null);
	}
	
	/** returns view-space X coordinate; to ensure it is not stale call updateViewSpace() */
	final public float getX(){return c14;}
	/** returns view-space Y coordinate; to ensure it is not stale call updateViewSpace() */	
	final public float getY(){return c24;}
	/** returns view-space Z coordinate; to ensure it is not stale call updateViewSpace() */	
	final public float getZ(){return c34;}
	
	/** clear wasRendered flags on Items, this flag could be used to halt animation on Items that are off screen*/
	final public void clearWasRenderedFlags()
	{
		// first clear our own child Items
		int ci = childItems.size();
		for(int i = 0; i < ci; i++)childItems.elementAt(i).wasRendered = false;
		
		// now instruct child transforms to clear themselves
		if(childTransforms != null)
		{
			int ct = childTransforms.size();
			for(int i = 0; i < ct; i++)childTransforms.elementAt(i).clearWasRenderedFlags();
		}
	}
	
	/** seeks up the scene graph for an outerNamed Transform
	 * 
	 * @return Itself if it has an outerName, or the first outerNamed Transform that it finds whilst traversing up the scene graph. If no outerNamed Transform is found it returns null
	 */
	final public Transform seekOuterNamedTransform()
	{
		if(outerName != null)return this;
		Transform parentL = parent;
		Transform retval;
		if(parentL != null)
		{
			retval = parentL.seekOuterNamedTransform();
			return retval;
		}
		else
		{
			return null;
		}
	}
	/** seeks up the scene graph for an outerNamed Transform then returns the outerName
	 * 
	 * @return its own outerName if has one, or the outerName of the first outerNamed Transform that it finds whilst traversing up the scene graph. If no outerNamed Transform is found it returns null
	 */
	final public String seekOuterName()
	{
		Transform t = seekOuterNamedTransform();
		if(t == null){return null;}
		else{return t.outerName;}
	}
	
	/** seeks up the scene graph for an innerNamed Transform
	 * 
	 * @return Itself if it has an innerName, or the first innerNamed Transform that it finds whilst traversing up the scene graph. If no innerNamed Transform is found it returns null
	 */
	final public Transform seekInnerNamedTransform()
	{
		if(innerName != null)return this;
		Transform parentL = parent;
		Transform retval;
		if(parentL != null)
		{
			retval = parentL.seekInnerNamedTransform();
			return retval;
		}
		else
		{
			return null;
		}
	}
	
	/** seeks up the scene graph for an innerNamed Transform then returns the innerName
	 * 
	 * @return its own innerName if has one, or the innerName of the first innerNamed Transform that it finds whilst traversing up the scene graph. If no innerNamed Transform is found it returns null
	 */
	final public String seekInnerName()
	{
		Transform t = seekInnerNamedTransform();
		if(t == null){return null;}
		else{return t.innerName;}
	}
}

package nitrogen1;

/** component of the scene graph that encapsulates translation and orientation */
public class Transform{
	
	/** The actual values of this transform */
	float 			a11,a12,a13,a14;
	float 			a21,a22,a23,a24;
	float 			a31,a32,a33,a34;
	
	/** Calculated view space transform  */
	float 			c11,c12,c13,c14;
	float 			c21,c22,c23,c24;
	float 			c31,c32,c33,c34;
	
	/** Flag indicating if the transforms translation needs to be computed */
	boolean 		translationNeedsUpdate = true;
	
	/** Flag indicating the transforms rotation needs to be computed */	
	boolean 		rotationNeedsUpdate = true;
	
	// Counter to ensure the view-space transform only gets calculated
	// if this transform has visible children 
	/** Number of visible children */
	int 			numberOfVisibleChildren = 0;
	
	/** The Parent Transform of this transform */
	Transform 		parent;
	
	/** Holds any child transforms of this transform - can be null */
	TransformVector childTransforms;
	
	/** child items of this transform - always an ItemVector but it may be empty */
	ItemVector		childItems = new ItemVector();		
	
	/** Creates a new Transform with the parameter Transform as its parent
	 * 
	 * @param parent The parent of the created Transform. Use null if the Transform is the root of a scene graph
	 */
	Transform(Transform parent)
	{
		setParent(parent);
		if(parent != null)parent.add(this);
	}

	/** Creates a new Transform with the parameter Transform as its parent
	 * 
	 * @param parent The parent of the created Transform. Use null if the Transform is the root of a scene graph.
	 * @param a11-a34 floating point values representing the transform.
	 */
	Transform(	Transform parent,
			float a11, float a12, float a13, float a14,
			float a21, float a22, float a23, float a24,
			float a31, float a32, float a33, float a34
			)
			{
				setParent(parent);
				if(parent != null)parent.add(this);
				this.a11 = a11; this.a12=a12; this.a13=a13; this.a14=a14;
				this.a21 = a21; this.a22=a22; this.a23=a13; this.a24=a14;
				this.a31 = a31; this.a32=a32; this.a33=a13; this.a34=a14;
			}
	
	/** Access method 
	 * @return The parent of the called  Transform, or null if it is the scenegraph root*/
	Transform getParent() {return(parent);}
	
	/** Access method setting the parent of the called Transform, breaking any existing parental bond
	 * if it exists. 
	 * @param new_parent The Transform to be set as the parent of the called Transform*/
	void setParent(Transform new_parent) 
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
			new_parent.increaseVisibleChildrenBy(numberOfVisibleChildren);
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
	boolean remove(Transform t)
	{
		if(childTransforms.removeElement(t) == true)
		{
			decreaseVisibleChildrenBy(t.numberOfVisibleChildren);
			return true;
		}
		return false;
	}	

	/** Adds a transform to its child transform list
	 * @param t Transform to be added as a child to the Transform being called */
	void add(Transform t)
	{
		// add passed in transform to childTransforms, creating it if it is null
		if (childTransforms == null)childTransforms = new TransformVector();
		childTransforms.addElement(t);
		
		// account for visible children
		int novc = t.numberOfVisibleChildren;
		if(novc > 0)increaseVisibleChildrenBy(novc);
		
		// tell the supplied transform who its parent is
		// and break any existing parental bonds
		t.setParent(this);
	}
	
	/** Adds an Item to the transforms child item list 
	 *  @param i Item to be added as a child to the Transform being called */
	void add(Item i)
	{
		childItems.addElement(i);
		
		if(i.visibility)increaseVisibleChildrenBy(1);
	}
	
	
	/** removes the given child Item
	 * @param i The transform to remove
	 * @return Returns true if the transform was found and removed 
	 * */
	boolean remove(Item i)
	{
		if(childItems.removeElement(i) == true)
		{
			if(i.visibility)decreaseVisibleChildrenBy(1);
			return true;
		}
		return false;
	}
	
	/** increases the numberOfVisibleChildren in this transform
	 *  as well as any above it in the scenegraph, by n
	 * @param n How much to increase the count by
	 */
	void increaseVisibleChildrenBy(int n)
	{
		numberOfVisibleChildren += n;
		if(parent != null)parent.increaseVisibleChildrenBy(n);
	}
	
	/** decreases the numberOfVisibleChildren in this transform
	 *  as well as any above it in the scenegraph, by n
	 * @param n How much to decrease the count by
	 */
	void decreaseVisibleChildrenBy(int n)
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
	void setNeedsTotallyUpdating()
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
	void setNeedsRotationUpdating()
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
	void setNeedsTranslationUpdating()
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
	void render(NitrogenContext context){
		updateSelf();
		
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
	void updateSelf()
	{
		if(parent != null)
		{
			// ensure parents are uptodate
			parent.updateSelf();
			
			// copy parents c values locally for speed
			float p11 = parent.c11;
			float p12 = parent.c12;
			float p13 = parent.c13;			
			float p14 = parent.c14;
			
			float p21 = parent.c21;
			float p22 = parent.c22;
			float p23 = parent.c23;
			float p24 = parent.c24;	
			
			float p31 = parent.c31;
			float p32 = parent.c32;
			float p33 = parent.c33;
			float p34 = parent.c34;
			
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
				c14 = p31*la14+p32*la24+p33*la34+p34;
				translationNeedsUpdate = false;
			}			
		}
		else
		{
			setSelfAsRoot();
			return;			
		}
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
	void render(
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
			c14 = p31*la14+p32*la24+p33*la34+p34;
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
		
	final void setSelfAsRoot()
	{
		// This is the root of the scene graph 
		// so its value is the unity matrix at origin
		c11=1;c12=0;c13=0;c14=0;
		c21=0;c22=1;c23=0;c24=0;
		c31=0;c32=0;c33=1;c34=0;
		rotationNeedsUpdate = false;
		translationNeedsUpdate = false;
	}
	
	
}

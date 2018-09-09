package nitrogen1;

public class Backside {
		
	/** the backsides normal coordinates, which are recalculated if rotation occurs */
	float nx, ny, nz;
	
	/** Partially computed view-space coordinate, formed from a rotated but not translated ImmutableBackside */
	float rx, ry, rz;
	
	/** the backsides view-space coordinates,  which are recalculated if rotation or translation occurs */
	float vx, vy, vz;
	
	/** Lighting value computed by the backside (if this is enabled in the associated immutable backside) */
	float lightingValue;
	
	/** return value, which is true if the tangent points toward the viewer */
	boolean value;
	
    /** flag indicating the positional offset caused by rotation needs updating */
    boolean rotationNeedsUpdate = true;
    
    /** flag indicating the view-space coordinates and backside value need updating */ 
    boolean translationNeedsUpdate = true;
    
    /** Calculate the backside so that subsequent facingViewer() calls return the correct value, rather than a stale one.
     *  
     * @param ib The ImmutableBackside object needed to calculate this mutable Backside object <br />(i.e. This Backside before any orientation is applied)
     * <br /><br />
     * @param context NitrogenContext used for calculating backsides lighting value
     * <br /><br />
     * @param v11-v34 The orientation matrix computed by the scene graph 
     */
    final void calculate(
    		ImmutableBackside ib,
    		NitrogenContext context,
    		float v11, float v12, float v13, float v14,
    		float v21, float v22, float v23, float v24,
    		float v31, float v32, float v33, float v34
    		)
    		{
    			// if the backside is not stale return its value
    			if(translationNeedsUpdate == false)return;
    			
    			if(rotationNeedsUpdate)
    			{
    				// cache immutable backside values for speed.
    				float ibix = ib.ix;
    				float ibiy = ib.iy;
    				float ibiz = ib.iz;
    				float ibinx = ib.inx;
    				float ibiny = ib.iny;
    				float ibinz = ib.inz;
    				
    				rx = v11 * ibix + v12 * ibiy + v13 * ibiz;
    				ry = v21 * ibix + v22 * ibiy + v23 * ibiz;
    				rz = v31 * ibix + v32 * ibiy + v33 * ibiz;
    				
    				nx = v11 * ibinx + v12 * ibiny + v13 * ibinz;
    				ny = v21 * ibinx + v22 * ibiny + v23 * ibinz;
    				nz = v31 * ibinx + v32 * ibiny + v33 * ibinz;   				
    				rotationNeedsUpdate = false;
    			}
    			
    			// update backsides view-space coordinates
    			vx = rx + v14;
    			vy = ry + v24;
    			vz = rz + v34;
    			
    			// calculate dot product
    			float product = vx * nx + vy * ny + vz * nz;
    			
    			// clear the translation flag
    			translationNeedsUpdate= false;
    			
    			if(product < 0)
    			{
    				value = true;	// tangent toward viewpoint
    			}
    			else
    			{
    				value = false;	// tangent away from viewpoint
    			}
    			
    			if(ib.calculateLighting)lightingValue = calculateLighting(context);
    			
    		}
    
    	/** returns true if the backside is facing the viewer. If the backside might have moved then call the backsides calculate method beforehand. */
    	final boolean facingViewer(){return value;}
    	
    	float calculateLighting(NitrogenContext context)
    	{
    		// ** TO DO **
    		return 0;
    	}
    		
}

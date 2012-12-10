package nitrogen1;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;

public class Backside implements Serializable{
	private static final long serialVersionUID = 5140009704459622721L;

	/** the ImmutableBackside linked to this backside */
	ImmutableBackside linkedImmutableBackside;
		
	/** the backsides normal coordinates, which are recalculated if rotation occurs */
	transient float nx, ny, nz;
	
	/** Partially computed view-space coordinate, formed from a rotated but not translated ImmutableBackside */
	transient float rx, ry, rz;
	
	/** the backsides view-space coordinates,  which are recalculated if rotation or translation occurs */
	transient float vx, vy, vz;
	
	/** Lighting value computed by the backside (if this is enabled in the associated immutable backside) */
	transient float lightingValue;
	
	/** return value, which is true if the tangent points toward the viewer */
	transient boolean value;
	
    /** flag indicating the positional offset caused by rotation needs updating */
	transient boolean rotationNeedsUpdate = true;
    
    /** flag indicating the view-space coordinates and backside value need updating */ 
	transient boolean translationNeedsUpdate = true;
    
	/** package scope reference for use in factories LLL*/
    transient Backside nextInList;
    
    /** default constructor used by factories to preallocate a Backside */
    Backside(){}

    /** constructor used by factories to generate new backsides on request*/
    Backside(ImmutableBackside immutableBackside)
    {
    	this.linkedImmutableBackside = immutableBackside;
    }
    
    /** used by factories when re-using a backside */
    void initializeBackside(ImmutableBackside immutableBackside)
    {
    	linkedImmutableBackside = immutableBackside;
        rotationNeedsUpdate = true;
        translationNeedsUpdate = true;
        nextInList = null;
    }   
    
    /** Calculate the backside so that subsequent facingViewer() calls return the correct value, rather than a stale one.
     *  
     * @param ib The ImmutableBackside object needed to calculate this mutable Backside object <br />(i.e. This Backside before any orientation is applied)
     * <br /><br />
     * @param context NitrogenContext used for calculating backsides lighting value
     * <br /><br />
     * @param v11-v34 The orientation matrix computed by the scene graph 
     */
    final void calculate(
    		final NitrogenContext context,
    		final float v11, final float v12, final float v13, final float v14,
    		final float v21, final float v22, final float v23, final float v24,
    		final float v31, final float v32, final float v33, final float v34
    		)
    		{
    			// if the backside is not stale return its value
    			if(translationNeedsUpdate == false)return;
    			ImmutableBackside lib = linkedImmutableBackside;
    			
    			if(rotationNeedsUpdate)
    			{ 
    				
    				// cache immutable backside values for speed.
    				float ibix = lib.ix;
    				float ibiy = lib.iy;
    				float ibiz = lib.iz;
    				float ibinx = lib.inx;
    				float ibiny = lib.iny;
    				float ibinz = lib.inz;
    				
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
    			
    			if(lib.calculateLighting)lightingValue = calculateLighting(context);
    			
    		}
    
    	/** returns true if the backside is facing the viewer. If the backside might have moved then call the backsides calculate method beforehand. */
    	final boolean facingViewer(){return value;}
    	
    	float calculateLighting(NitrogenContext context)
    	{
    		// ** TO DO **
    		return 0;
    	}
    	
        final private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
        {
        	in.defaultReadObject();
        	translationNeedsUpdate = true;
        	rotationNeedsUpdate = true;
        }
    		
}

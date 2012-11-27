/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package nitrogen1;

/**
 *
 * @author andrew
 */
public final class Vert {
    
	// Item-space coordinates
	/** Item space x coordinate. The containing Items orientation transform gets applied to the (usually fixed) Item space coordinates of the vertex in order to generate the vertex's view-space coordinates. */
	float is_x;
	/** Item space y coordinate. */
	float is_y;
	/** Item space z coordinate. */
	float is_z;
	
    // coordinates 
    /** Partially computed view-space x coordinate, formed from a rotated but not translated item-space coordinate. */
    float rvs_x;
    /** Partially computed view-space y coordinate, formed from a rotated but not translated item-space coordinate. */
    float rvs_y;
    /** Partially computed view-space z coordinate, formed from a rotated but not translated item-space coordinate. */    
    float rvs_z;
	
    // view-space coordinates 
    /** view-space x coordinate. Positive is right-ward */
    float vs_x;
    /** views-pace y coordinate. Positive is up-ward. */
    float vs_y;
    /** view-space z coordinate. Increasing negativity is moving away from the view-point. */
    float vs_z;
    
    /** flag indicating the view-space coordinates are stale due to rotation of the containing Item */
    boolean rotationNeedsUpdate = true;    
    /** flag indicating the view-space coordinates are stale due to rotation of the containing Item  */ 
    boolean translationNeedsUpdate = true;
	
    // screen coordinates
	/** screen x coordinate (not including an offset) */
    int sx;
    /** screen y coordinate (not including an offset) */
    int sy;
    /** screen z coordinate (which may differ from the vertexes view-space z due to perspective projection)*/
    int sz;
    /** flag indicating if the vertex view-space coordinates need updating */
    boolean screenCoordinatesNeedUpdate = true;
    
    /**An auxiliary coordinate used by renderer (i.e. a float representing a texture or lighting coordinate)*/
    float aux1;
    /**An auxiliary coordinate used by renderer (i.e. a float representing a texture or lighting coordinate)*/   
    float aux2;
    /**An auxiliary coordinate used by renderer (i.e. a float representing a texture or lighting coordinate)*/    
    float aux3;

    Vert(int px, int py)
    {
        sx = px;
        sy = py;
    }

    Vert(int px, int py, int pz)
    {
        sx = px;
        sy = py;
        sz = pz;
    }
    
    Vert(ImmutableVertex iv)
    {
    	is_x = iv.is_x;
    	is_y = iv.is_y;
    	is_z = iv.is_z;
    }

    /** default constructor used by static initialiser in HLPBreaker */
    Vert() {}

	void setZ(int pz)
    {
        sz = pz;
    }
    
    /** Set auxiliary variable aux1 e.g. an integer polygon colour */
    void setAux2(float aux1)
    {
    	this.aux1=aux1;

    }    

    /** Set auxiliary variables aux1 and aux2 e.g. texture map coordinates*/
    void setAux2(float aux1, float aux2)
    {
    	this.aux1=aux1;
    	this.aux2=aux2;
    }
    
    /** Set auxiliary variables aux1, aux2 & aux3 e.g. RGB values for gourad shading*/    
    void setAux3(float aux1, float aux2, float aux3)
    {
    	this.aux1=aux1;
    	this.aux2=aux2;
    	this.aux3=aux3;
    }
    
    void setAux(PolygonVertexData pvd)
    {
       	this.aux1 = pvd.aux1;
       	this.aux2 = pvd.aux2;
       	this.aux3 = pvd.aux3;
           }
    /** Calculate the vertex's view-space coordinates 
     * @param v11-v34  The orientation matrix computed by the scene graph (12 floating point values)*/
    void calculateViewSpaceCoordinates(			
    		float v11, float v12, float v13, float v14,
			float v21, float v22, float v23, float v24,
			float v31, float v32, float v33, float v34)
    {		
    	if(rotationNeedsUpdate)
    	{
    		// cache the vertex instance item space coordinate locally for speed
    		float is_xC = is_x;
    		float is_yC = is_y;
    		float is_zC = is_z;
    		rvs_x = v11 * is_xC + v12 * is_yC + v13 * is_zC;
    		rvs_y = v21 * is_xC + v22 * is_yC + v23 * is_zC;
    		rvs_z = v31 * is_xC + v32 * is_yC + v33 * is_zC;
    		rotationNeedsUpdate = false;
    	}
    	if(translationNeedsUpdate)
    	{
    		vs_x = rvs_x + v14;
    		vs_y = rvs_y + v24;
    		vs_z = rvs_z + v34;
    		translationNeedsUpdate = false;
    		
    	}
    	screenCoordinatesNeedUpdate = true;   	
    }
    
    /** Used to set key fields of an existing vertex 
     * @param vs_x view-space x coordinate
     * @param vs_y view-space y coordinate
     * @param vs_z view-space z coordinate
     * @param aux1 Auxiliary value used for texture map coordinates etc (as a scaled-up integer value)
     * @param aux2 Auxiliary value used for texture map coordinates etc (as a scaled-up integer value)
     * @param aux3 Auxiliary value used for texture map coordinates etc (as a scaled-up integer value)
     * */
    final void setViewSpaceAndAux(
    		float vs_x, float vs_y, float vs_z, float aux1, float aux2, float aux3
    		)
    {
    	this.vs_x = vs_x;
    	this.vs_y = vs_y;
    	this.vs_z = vs_z;
    	this.aux1 = aux1;
    	this.aux2 = aux2;
    	this.aux3 = aux3;
    	rotationNeedsUpdate = true;
    	translationNeedsUpdate = true;
    	screenCoordinatesNeedUpdate = true;
    }
    
    final void calculateScreenSpaceCoordinate(NitrogenContext context)
    {
    		float m = context.magnification;
    		if(!screenCoordinatesNeedUpdate)return;
    		
    		// close to view-point is more positive
    		// vs_z gets more -ve as you move away from view-point
    		sz = Integer.MIN_VALUE - (int)(context.zk/vs_z);
    		 		
    		sx = (int)((m * vs_x)/(-vs_z)) + context.midw;
    		
    		// Top of NitrogenContext view window is 0, but view-space y is upward.
    		sy = context.midh - (int)((m * vs_y)/(-vs_z));
    		screenCoordinatesNeedUpdate = false;  		
    }
    		
    

    
    


}

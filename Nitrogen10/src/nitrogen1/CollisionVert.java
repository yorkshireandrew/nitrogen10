/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package nitrogen1;

/**
 *
 * @author andrew
 */
public final class CollisionVert {
    
	// Item-space coordinates
	/** Item space x coordinate. The containing Items orientation transform gets applied to the (usually fixed) Item space coordinates of the vertex in order to generate the vertex's view-space coordinates. */
	float is_x;
	/** Item space y coordinate. */
	float is_y;
	/** Item space z coordinate. */
	float is_z;
	/** Radius of collision */
	float radius;
	
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
    
    CollisionVert(ImmutableCollisionVert icv)
    {	
    	is_x = icv.is_x;
    	is_y = icv.is_y;
    	is_z = icv.is_z;
    	radius = icv.radius;
    }
    
    /** Calculate the vertex's view-space coordinates 
     * @param v11-v34  The orientation matrix computed by the scene graph (12 floating point values)*/
    void calculateViewSpaceCoordinates( 
    		float c11, float c12, float c13, float c14,
			float c21, float c22, float c23, float c24,
    		float c31, float c32, float c33, float c34)
    {		
    	if(rotationNeedsUpdate)
    	{
    		// cache the vertex instance item space coordinate locally for speed
    		float is_xL = is_x;
    		float is_yL = is_y;
    		float is_zL = is_z;
    		rvs_x = c11 * is_xL + c12 * is_yL + c13 * is_zL;
    		rvs_y = c21 * is_xL + c22 * is_yL + c23 * is_zL;
    		rvs_z = c31 * is_xL + c32 * is_yL + c33 * is_zL;
    		rotationNeedsUpdate = false;
    	}
    	
    	if(translationNeedsUpdate)
    	{
    		vs_x = rvs_x + c14;
    		vs_y = rvs_y + c24;
    		vs_z = rvs_z + c34;
    		translationNeedsUpdate = false;  		
    	}
    }
    
    float distanceTo(CollisionVert target)
    {
    	float dx = vs_x - target.vs_x;
    	float dy = vs_y - target.vs_y;
    	float dz = vs_z - target.vs_z;
    	return (float)Math.sqrt(dx*dx+dy*dy+dz*dz);
    }
    
    float distanceToSquared(CollisionVert target)
    {
    	float dx = vs_x - target.vs_x;
    	float dy = vs_y - target.vs_y;
    	float dz = vs_z - target.vs_z;
    	return (dx*dx+dy*dy+dz*dz);
    }
    
    float gapTo(CollisionVert target)
    {
    	return(distanceToSquared(target) - radius - target.radius);
    }
    
    float gapToSquared(CollisionVert target)
    {
    	float radiusL = radius;
    	float targetRadius = target.radius;
    	return(distanceToSquared(target) - radiusL*radiusL - targetRadius*targetRadius);
   	
    }
    
    boolean collidedWith( CollisionVert target)
    {
    	float radiusL = radius;
    	float targetRadius = target.radius;
    	if((distanceToSquared(target) - radiusL*radiusL - targetRadius*targetRadius)>0){return false;}else{return true;}   	
    }	
    
    
}

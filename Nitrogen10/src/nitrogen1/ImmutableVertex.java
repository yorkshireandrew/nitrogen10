package nitrogen1;

public class ImmutableVertex {
	
	// Item-space coordinates
	/** Item space x coordinate. The containing Items orientation transform gets applied to the (usually fixed) Item space coordinates of the vertex in order to generate the vertex's view-space coordinates. */
	float is_x;
	/** Item space y coordinate. */
	float is_y;
	/** Item space z coordinate. */
	float is_z;

    /**An auxiliary coordinate used by renderer (i.e. a float representing a texture or lighting coordinate)*/
    float aux1;
    /**An auxiliary coordinate used by renderer (i.e. a float representing a texture or lighting coordinate)*/   
    float aux2;
    /**An auxiliary coordinate used by renderer (i.e. a float representing a texture or lighting coordinate)*/    
    float aux3;
    
    ImmutableVertex(
    		float is_x,
    		float is_y,
    		float is_z,
    		
    	    float aux1,
    	    float aux2,
    	    float aux3  		
    		)
    {
    	this.is_x = is_x;
    	this.is_y = is_y;

    	this.aux1 = aux1;
    	this.aux2 = aux2;
    	this.aux3 = aux3;
    }
    
	
}

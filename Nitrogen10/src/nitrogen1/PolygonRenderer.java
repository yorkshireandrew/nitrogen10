package nitrogen1;

public class PolygonRenderer {
	  static final int SHIFT = 20;
	  static final int NUM = 1 << SHIFT;  // create a numerator equal to one - used to fast divide
    
	/**
	 * 
	 * @param context NitrogenContext to render the polygon into
	 * @param a First polygon vertex. Vertexes must be in clockwise order.
	 * @param b Second polygon vertex. 
	 * @param c Third polygon vertex. 
	 * @param d Fourth polygon vertex. 
	 * @param renderer Renderer to use to render the polygon.
	 * @param polyData Polygon data to pass to the Renderer, such as its colour.
	 * @param texMap TextureMap top pass to the Renderer.
	 * @param lightingValue lighting value that may have been computed by the polygons backside to pass to the Renderer
	 * @param renderer Renderer to use to render the polygon.
	 */
	static final void process(
			final NitrogenContext context,
			final Vert a, final Vert b, final Vert c, final Vert d, 
			final Renderer ren, 
			final int[] polyData, 
			final TexMap texMap,
			final float lightingValue
			)
	{
	    context.polygonRendererCalls++;
		System.out.println("PolygonRenderer called");
	    System.out.println("vert a = " + a. vs_x + "," + a.vs_y + "," + a.vs_z );	    
	    System.out.println("vert b = " + b. vs_x + "," + b.vs_y + "," + b.vs_z );	    
	    System.out.println("vert c = " + c. vs_x + "," + c.vs_y + "," + c.vs_z );	    
	    System.out.println("vert d = " + d. vs_x + "," + d.vs_y + "," + d.vs_z );
	    
	    if(context.debug){
	    	System.out.println("debug on");
	    }
		
		a.calculateScreenSpaceCoordinate(context);
	    b.calculateScreenSpaceCoordinate(context);
	    c.calculateScreenSpaceCoordinate(context);
	    d.calculateScreenSpaceCoordinate(context);
	    
	    System.out.println("vert a = " + a.sx + "," + a.sy );	    
	    System.out.println("vert b = " + b.sx + "," + b.sy );	    
	    System.out.println("vert c = " + c.sx + "," + c.sy );	    
	    System.out.println("vert d = " + d.sx + "," + d.sy );	    

			
		// create local copies of y coordinates for sorting
	    int ay = a.sy;
	    int by = b.sy;
	    int cy = c.sy;
	    int dy = d.sy;
	    
	    // determine minimum and call next method to find max
	    if(ay >= by)
	    {
	    	if(by >= cy)
	    	{
	    		// must be c or d
	    		if(cy >= dy)
	    		{
	    			min_d(context,a, b, c, d, ren, polyData, texMap,lightingValue);
	    		}
	    		else
	    		{
	    			min_c(context,a, b, c, d, ren, polyData, texMap,lightingValue);
	    		}	    		
	    	}
	    	else
	    	{
	    		// must be b or d
	    		if(by >= dy)
	    		{
	    			min_d(context,a, b, c, d, ren, polyData, texMap,lightingValue);
	    		}
	    		else
	    		{
	    			min_b(context,a, b, c, d, ren, polyData, texMap,lightingValue);
	    		}	  	    		
	    	}
	    		
	    }
	    else
	    {
	    	if(ay >= cy)
	    	{
	    		// must be c or d
	    		if(cy >= dy)
	    		{
	    			min_d(context,a, b, c, d, ren, polyData, texMap,lightingValue);
	    		}
	    		else
	    		{
	    			min_c(context,a, b, c, d, ren, polyData, texMap,lightingValue);
	    		}	 
	    	}
	    	else
	    	{
	    		// must be a or d
	    		if(ay >= dy)
	    		{
	    			min_d(context,a, b, c, d, ren, polyData, texMap,lightingValue);
	    		}
	    		else
	    		{
	    			min_a(context,a, b, c, d, ren, polyData, texMap,lightingValue);
	    		}	 
	    	}	    	
	    }
	}
	
	private static final void min_a(
			final NitrogenContext context,
			final Vert a, final Vert b, final Vert c, final Vert d, 
			final Renderer ren, 
			final int[] polyData, 
			final TexMap texMap,
			final float lightingValue
			)
	{
		// create local copies of y coordinates for sorting
	    int ay = a.sy;
	    int by = b.sy;
	    int cy = c.sy;
	    int dy = d.sy;
	    
	    // now try and find max. we know its not a
	    if(by >= cy)
	    {
	    	// must be by or dy
	    	if(by >= dy)
	    	{
	    		// b is max, a is min
	    		plotCase4(context,a, b, c, d, ren, polyData, texMap,lightingValue);
	    	}
	    	else
	    	{
	    		// d is max, a is min
	    		plotCase1(context,a, b, c, d, ren, polyData, texMap,lightingValue);
	    	}
	    }
	    else
	    {
	    	// must be cy or dy
	    	if(cy >= dy)
	    	{
	    		// c is max, a is min
	    		if(by >= dy)
	    		{
	    			// a < d < b < c
	    			plotCase3(context,a, b, c, d, ren, polyData, texMap,lightingValue);
	    		}
	    		else
	    		{
	    			// a < b < d < c
	    			plotCase2(context,a, b, c, d, ren, polyData, texMap,lightingValue);
	    		}
	    	}
	    	else
	    	{
	    		// d is max, a is min
	    		plotCase1(context,a, b, c, d, ren, polyData, texMap,lightingValue);  		
	    	}
	    }
		
	}
	
	private static final void min_b(
			final NitrogenContext context,
			final Vert a, final Vert b, final Vert c, final Vert d, 
			final Renderer ren, 
			final int[] polyData, 
			final TexMap texMap,
			final float lightingValue
			)
	{
		// create local copies of y coordinates for sorting
	    int ay = a.sy;
	    int by = b.sy;
	    int cy = c.sy;
	    int dy = d.sy;
	    
	    // find max we know its not b
	    
	    if(ay >= cy)
	    {
	    	// must be a or d
	    	if(ay >= dy)
	    	{
	    		// a is max, b is min
	    		plotCase1(context, b,c,d,a, ren, polyData, texMap,lightingValue);

	    	}
	    	else
	    	{
	    		// d is max, b is min
	    		if(ay >= cy)
	    		{
	    			// b < c < a < d
		    		plotCase2(context, b,c,d,a, ren, polyData, texMap,lightingValue);
	    		}
	    		else
	    		{
	    			// b < a < c < d
		    		plotCase3(context, b,c,d,a, ren, polyData, texMap,lightingValue);
	    		}
	    	}
	    }
	    else
	    {
	    	// must be c or d
	    	if(cy >= dy)
	    	{
	    		// c is max, b is min
	    		plotCase4(context, b,c,d,a, ren, polyData, texMap,lightingValue);
	    		
	    	}
	    	else
	    	{
	    		// d is max, b is min
	    		if(ay >= cy)
	    		{
	    			// b < c < a < d
		    		plotCase2(context, b,c,d,a, ren, polyData, texMap,lightingValue);
	    		}
	    		else
	    		{
	    			// b < a < c < d
		    		plotCase3(context, b,c,d,a, ren, polyData, texMap,lightingValue);
	    		}	    		
	    	}
	    }    
	}
	
	private static final void min_c(
			final NitrogenContext context,
			final Vert a, final Vert b, final Vert c, final Vert d, 
			final Renderer ren, 
			final int[] polyData, 
			final TexMap texMap,
			final float lightingValue
			)
	{
		// create local copies of y coordinates for sorting
	    int ay = a.sy;
	    int by = b.sy;
	    int cy = c.sy;
	    int dy = d.sy;
	    // find max we know its not c
	    
	    if(ay >= by)
	    {
	    	// must be a or d
	    	if(ay >= dy)
	    	{
	    		// a is max, c is min
	    		if(by >= dy)
	    		{
		    		// c < d < b < a
	    			plotCase2(context, c,d,a,b, ren, polyData, texMap,lightingValue);	    			
	    		}
	    		else
	    		{
		    		// c < b < d < a
	    			plotCase3(context, c,d,a,b, ren, polyData, texMap,lightingValue);	    				    			
	    		}
	    	}
	    	else
	    	{
	    		// d is max, c is min
    			plotCase4(context, c,d,a,b, ren, polyData, texMap,lightingValue);	    			
	    		
	    	}
	    }
	    else
	    {
	    	// must be b or d
	    	if(by >= dy)
	    	{
	    		// b is max, c is min
    			plotCase1(context, c,d,a,b, ren, polyData, texMap,lightingValue);	    			

	    	}
	    	else
	    	{
	    		// d is max, c is min
    			plotCase4(context, c,d,a,b, ren, polyData, texMap,lightingValue);	    			
	    	}
	    }   
	}
	
	private static final void min_d(
			final NitrogenContext context,
			final Vert a, final Vert b, final Vert c, final Vert d, 
			final Renderer ren, 
			final int[] polyData, 
			final TexMap texMap,
			final float lightingValue
			)
	{
		// create local copies of y coordinates for sorting
	    int ay = a.sy;
	    int by = b.sy;
	    int cy = c.sy;
	    int dy = d.sy;
	    
	    // find max we know its not d
	    if (ay >= by)
	    {
	    	// must be a or c
	    	if(ay >= cy)
	    	{
	    		// max a, min d
    			plotCase4(context, d,a,b,c, ren, polyData, texMap,lightingValue);	    			
	    	}
	    	else
	    	{
	    		// max c, min d
    			plotCase1(context, d,a,b,c, ren, polyData, texMap,lightingValue);	    			
	    	}
	    }
	    else
	    {
	    	// must be b or c
	    	if(by >= cy)
	    	{
	    		// max b, min d
	    		if( cy >= ay)
	    		{
	    			// d < a < c < b
	       			plotCase2(context, d,a,b,c, ren, polyData, texMap,lightingValue);	    				    			
	    		}
	    		else
	    		{
	    			// d < c < a < b
	       			plotCase3(context, d,a,b,c, ren, polyData, texMap,lightingValue);	    				    						
	    		}	    		
	    	}
	    	else
	    	{
	    		// max c, min d
	   			plotCase1(context, d,a,b,c, ren, polyData, texMap,lightingValue);	    				    		
	    	}
	    }
	}


	//------------------------------------------------------------------------------------------------------------------------
	    //*********************** PLOT CASE 1 ************************************
		/** case where vert a < vert b < vert c < vert d */
		private static final void plotCase1(final NitrogenContext context, final Vert a, final Vert b, final Vert c, final Vert d, final Renderer ren, final int[] polyData, final TexMap tm, float lightingValue)
	    {
			System.out.println(" plot case 1 where vert a < vert b < vert c < vert d");
		    System.out.println("vert a = " + a.sx + "," + a.sy );	    
		    System.out.println("vert b = " + b.sx + "," + b.sy );	    
		    System.out.println("vert c = " + c.sx + "," + c.sy );	    
		    System.out.println("vert d = " + d.sx + "," + d.sy );	    

			// these values localised first to catch degenerate polygons early
	        int ay = a.sy;
	        int dy = d.sy;
	
	        // catch degenerate polygons
	        if(ay == dy)return;
			 context.linesRendered += (dy - ay);	
	        // localise a coordinates
	        int ax = a.sx;
	//        int ay = a.sy;
	        long az = a.sz;
	        
	        int a_aux1 = (int)a.aux1;
	        int a_aux2 = (int)a.aux2;
	        int a_aux3 = (int)a.aux3;
	
	        // localise b coordinates
	        int bx = b.sx;
	        int by = b.sy;
	        long bz = b.sz;
	        
	        int b_aux1 = (int)b.aux1;
	        int b_aux2 = (int)b.aux2;
	        int b_aux3 = (int)b.aux3;
	        
	        // localise c coordinates
	        int cx = c.sx;
	        int cy = c.sy;
	        long cz = c.sz;
	        
	        int c_aux1 = (int)c.aux1;
	        int c_aux2 = (int)c.aux2;
	        int c_aux3 = (int)c.aux3;	
	        
	        // localise d coordinates
	        int dx = d.sx;
	//        int dy = d.sy;
	        long dz = d.sz;
	        
	        int d_aux1 = (int)d.aux1;
	        int d_aux2 = (int)d.aux2;
	        int d_aux3 = (int)d.aux3;	        
	
	        // localise pixel buffer and texture and context width and texture width
	        int[] p = context.pix;
	        int[] z = context.zbuff;
	        int bw = context.w;
	        int[] tex = null;
	        int tw = 0;
	        if(tm != null){tex = tm.tex; tw = tm.w;}
	
	        // set start point to a
	        int st_x 	= ax << SHIFT;
	        long st_z 	= az << SHIFT;    // z-buffer is 32bit so need to use a long to scale up
	        
	        int st_aux1 = a_aux1 << SHIFT;
	        int st_aux2 = a_aux2 << SHIFT;
	        int st_aux3 = a_aux3 << SHIFT;
	
	        // set finish point to a
	        int fin_x = ax << SHIFT;
	        long fin_z = az << SHIFT;
	        
	        int fin_aux1 = a_aux1 << SHIFT;
	        int fin_aux2 = a_aux2 << SHIFT;
	        int fin_aux3 = a_aux3 << SHIFT;
	
	        int st_dx, st_daux1, st_daux2, st_daux3;     	// declare start steppers
	        int fin_dx, fin_daux1, fin_daux2, fin_daux3;    // declare finish steppers
	        long st_dz;                                 	// declare start z stepper
	        long fin_dz;                                	// declare finish z stepper
	        int rec;                				// reciprocal value used to divide faster
	        int fustrum_height;
	
	        // initialise finish stepper
	        rec = NUM / (dy - ay);
	        fin_dx = (dx - ax) * rec;
	        fin_dz = (dz - az) * rec;
	        
	        fin_daux1 = (d_aux1 - a_aux1) * rec;
	        fin_daux2 = (d_aux2 - a_aux2) * rec;
	        fin_daux3 = (d_aux3 - a_aux3) * rec;
	        System.out.println("step a to b");
	        // ----------------------- step from a to b -------------------
	        if(by > ay)
	        {
	            // initialise start stepper
	            fustrum_height = by - ay;
	            rec = NUM / fustrum_height;
	            st_dx = (bx - ax) * rec;
	            st_dz = (bz - az) * rec;
	            
	            st_daux1 = (b_aux1 - a_aux1) * rec;
	            st_daux2 = (b_aux2 - a_aux2) * rec;
	            st_daux3 = (b_aux3 - a_aux3) * rec;
	
	         // render from ay to by
	            renderDebugTrapezoid(
	                    st_x,       st_z,
	                    st_aux1, 	st_aux2,	st_aux3,
	                    st_dx,      st_dz,
	                    st_daux1, 	st_daux2, 	st_daux3,                    
	                    fin_x,      fin_z,
	                    fin_aux1, 	fin_aux2, 	fin_aux3,
	                    fin_dx,     fin_dz,
	                    fin_daux1, 	fin_daux2, 	fin_daux3,
	                    ay,         by,         
	                    p,      
	                    z,    	
	                    tex,      
	                    bw,     
	                    tw,      
	                    polyData,
	                    lightingValue
	                    );
	            
	            ren.renderTrapezoid(
	                    st_x,       st_z,
	                    st_aux1, 	st_aux2,	st_aux3,
	                    st_dx,      st_dz,
	                    st_daux1, 	st_daux2, 	st_daux3,                    
	                    fin_x,      fin_z,
	                    fin_aux1, 	fin_aux2, 	fin_aux3,
	                    fin_dx,     fin_dz,
	                    fin_daux1, 	fin_daux2, 	fin_daux3,
	                    ay,         by,         
	                    p,      
	                    z,    	
	                    tex,      
	                    bw,     
	                    tw,      
	                    polyData,
	                    lightingValue
	                    );
	
	            // set start point to b
	            st_x = bx << SHIFT;
	            st_z = bz << SHIFT;
	            
	            st_aux1 = b_aux1 << SHIFT;
	            st_aux2 = b_aux2 << SHIFT;
	            st_aux3 = b_aux3 << SHIFT;
	
	            // set line finish point
	            fin_x += (fin_dx * fustrum_height);
	            fin_z += (fin_dz * fustrum_height);
	            
	            fin_aux1 += (fin_daux1 * fustrum_height);
	            fin_aux2 += (fin_daux2 * fustrum_height);
	            fin_aux3 += (fin_daux3 * fustrum_height);
	        }
	        else
	        {
	            // case of ay == by
	
	            // set start point to b
	            st_x = bx << SHIFT;
	            st_z = bz << SHIFT;
	            
	            st_aux1 = b_aux1 << SHIFT;
	            st_aux2 = b_aux2 << SHIFT;
	            st_aux3 = b_aux3 << SHIFT;
	        }
	
	        // ----------------------- step from b to c -------------------
	        if(cy > by)
	        {
	            // initialise start stepper
	            fustrum_height = cy - by;
	            rec = NUM / fustrum_height;
	            st_dx = (cx - bx) * rec;
	            st_dz = (cz - bz) * rec;
	            
	            st_daux1 = (c_aux1 - b_aux1) * rec;
	            st_daux2 = (c_aux2 - b_aux2) * rec;
	            st_daux3 = (c_aux3 - b_aux3) * rec;
	
	         // render from by to cy
	            System.out.println("render from by to cy");
	            renderDebugTrapezoid(	
	                    st_x, 		st_z,
	                    st_aux1, 	st_aux2,	st_aux3,
	                    st_dx, 		st_dz,
	                    st_daux1, 	st_daux2, 	st_daux3,
	                    fin_x, 		fin_z,
	                    fin_aux1, 	fin_aux2, 	fin_aux3,
	                    fin_dx, 	fin_dz,
	                    fin_daux1, 	fin_daux2, 	fin_daux3,	                    
	                    by, cy,        
	                    p,      
	                    z,    
	                    tex,      
	                    bw,     
	                    tw,      
	                    polyData,
	                    lightingValue
	                    );
	            ren.renderTrapezoid(	
	                    st_x, 		st_z,
	                    st_aux1, 	st_aux2,	st_aux3,
	                    st_dx, 		st_dz,
	                    st_daux1, 	st_daux2, 	st_daux3,
	                    fin_x, 		fin_z,
	                    fin_aux1, 	fin_aux2, 	fin_aux3,
	                    fin_dx, 	fin_dz,
	                    fin_daux1, 	fin_daux2, 	fin_daux3,	                    
	                    by, cy,        
	                    p,      
	                    z,    
	                    tex,      
	                    bw,     
	                    tw,      
	                    polyData,
	                    lightingValue
	                    );
	
	            // set start point to c
	            st_x = cx << SHIFT;
	            st_z = cz << SHIFT;
	            
	            st_aux1 = c_aux1 << SHIFT;
	            st_aux2 = c_aux2 << SHIFT;
	            st_aux3 = c_aux3 << SHIFT;
	
	            // set line finish point
	            fin_x += (fin_dx * fustrum_height);
	            fin_z += (fin_dz * fustrum_height);
	            
	            fin_aux1 += (fin_daux1 * fustrum_height);
	            fin_aux2 += (fin_daux2 * fustrum_height);
	            fin_aux3 += (fin_daux3 * fustrum_height);
	        }
	        else
	        {
	            // case of by == cy
	
	            // set start point to c
	            st_x = cx << SHIFT;
	            st_z = cz << SHIFT;
	            
	            st_aux1 = c_aux1 << SHIFT;
	            st_aux2 = c_aux2 << SHIFT;
	            st_aux3 = c_aux3 << SHIFT;
	        }
	
	        // ----------------------- step from c to d -------------------
	        System.out.println("step from c to d");
	        if(dy > cy)
	        {
	            // initialise start stepper
	            fustrum_height = dy - cy;
	            rec = NUM / fustrum_height;
	            st_dx = (dx - cx) * rec;
	            st_dz = (dz - cz) * rec;
	            
	            st_daux1 = (d_aux1 - c_aux1) * rec;
	            st_daux2 = (d_aux2 - c_aux2) * rec;
	            st_daux3 = (d_aux3 - c_aux3) * rec;
	            renderDebugTrapezoid(
	                    st_x, 		st_z,
	                    st_aux1, 	st_aux2,	st_aux3,
	                    st_dx, 		st_dz,
	                    st_daux1,	st_daux2,	st_daux3,	                    
	                    fin_x, 		fin_z,
	                    fin_aux1, 	fin_aux2, 	fin_aux3,
	                    fin_dx, 	fin_dz,
	                    fin_daux1, 	fin_daux2, 	fin_daux3,
	                    cy, dy,                          
	                    p,      
	                    z,    
	                    tex,      
	                    bw,     
	                    tw,      
	                    polyData,
	                    lightingValue
	                    );	
	            // render from cy to dy	 
	            ren.renderTrapezoid(
	                    st_x, 		st_z,
	                    st_aux1, 	st_aux2,	st_aux3,
	                    st_dx, 		st_dz,
	                    st_daux1,	st_daux2,	st_daux3,	                    
	                    fin_x, 		fin_z,
	                    fin_aux1, 	fin_aux2, 	fin_aux3,
	                    fin_dx, 	fin_dz,
	                    fin_daux1, 	fin_daux2, 	fin_daux3,
	                    cy, dy,                          
	                    p,      
	                    z,    
	                    tex,      
	                    bw,     
	                    tw,      
	                    polyData,
	                    lightingValue
	                    );
	
	            // set start point to d
	            st_x = dx << SHIFT;
	            st_z = dz << SHIFT;
	            
	            st_aux1 = d_aux1 << SHIFT;
	            st_aux2 = d_aux2 << SHIFT;
	            st_aux3 = d_aux3 << SHIFT;
	
	            // set line finish point also to d
	            fin_x = st_x;
	            fin_z = st_z;
	            
	            fin_aux1 = st_aux1;
	            fin_aux2 = st_aux2;
	            fin_aux3 = st_aux3;
	        }
	        else
	        {
	            // case of cy == dy
	            // start is at c and fin is already reached d
	            // so do nothing
	        }
	
	        
	        // render final line. steppers set to 0
	        ren.renderTrapezoid(
	        		st_x, 		st_z,
	        		st_aux1, 	st_aux2,	st_aux3,
	        		0, 			0,              
	        		0, 			0, 			0,	                    
	        		fin_x, 		fin_z,
	        		fin_aux1, 	fin_aux2, 	fin_aux3,
	        		0, 			0,              
	        		0, 			0, 			0,
	        		dy, 		dy+1,		
	        		p,      
	        		z,    
	        		tex,      
	        		bw,     
	        		tw,      
	        		polyData,
	        		lightingValue
	                );
	
	
	    }
	    
		//************************ PLOT CASE 2 ***********************************************
		/** case where vert a < vert b < vert d < vert c */
	    private static final void plotCase2(final NitrogenContext context, final Vert a, final Vert b, final Vert c, final Vert d, final Renderer ren, final int[] polyData, final TexMap tm, float lightingValue)
	    {
			System.out.println("plot case 2 where vert a < vert b < vert d < vert c");
		    System.out.println("vert a = " + a.sx + "," + a.sy );	    
		    System.out.println("vert b = " + b.sx + "," + b.sy );	    
		    System.out.println("vert c = " + c.sx + "," + c.sy );	    
		    System.out.println("vert d = " + d.sx + "," + d.sy );	 
		    
	    	// these values localised first to catch degenerate polygons early
	        int ay = a.sy;
	        int cy = c.sy;
	
	        // catch degenerate polygons
	        if(ay == cy)return;
	        context.linesRendered += (cy - ay);
	        // localise a coordinates
	        int ax = a.sx;
	//        int ay = a.sy;
	        long az = a.sz;
	        
	        int a_aux1 = (int)a.aux1;
	        int a_aux2 = (int)a.aux2;
	        int a_aux3 = (int)a.aux3;
	
	        // localise b coordinates
	        int bx = b.sx;
	        int by = b.sy;
	        long bz = b.sz;
	        
	        int b_aux1 = (int)b.aux1;
	        int b_aux2 = (int)b.aux2;
	        int b_aux3 = (int)b.aux3;
	        
	        // localise c coordinates
	        int cx = c.sx;
//	        int cy = c.sy;
	        long cz = c.sz;
	        
	        int c_aux1 = (int)c.aux1;
	        int c_aux2 = (int)c.aux2;
	        int c_aux3 = (int)c.aux3;	
	        
	        // localise d coordinates
	        int dx = d.sx;
	        int dy = d.sy;
	        long dz = d.sz;
	        
	        int d_aux1 = (int)d.aux1;
	        int d_aux2 = (int)d.aux2;
	        int d_aux3 = (int)d.aux3;	        
	
	        // localise pixel buffer and texture and context width and texture width
	        int[] p = context.pix;
	        int[] z = context.zbuff;
	        int bw = context.w;
	        int[] tex = null;
	        int tw = 0;
	        if(tm != null){tex = tm.tex; tw = tm.w;}
	
	        // set start point to a
	        int st_x = ax << SHIFT;
	        long st_z = az << SHIFT;    // z-buffer is 32bit so need to use a long to scale up
	        
	        int st_aux1 = a_aux1 << SHIFT;
	        int st_aux2 = a_aux2 << SHIFT;
	        int st_aux3 = a_aux3 << SHIFT;
	
	        // set finish point to a
	        int  fin_x = ax << SHIFT;
	        long fin_z = az << SHIFT;
	        
	        int fin_aux1 = a_aux1 << SHIFT;
	        int fin_aux2 = a_aux2 << SHIFT;
	        int fin_aux3 = a_aux3 << SHIFT;
	
	        // declare start stepper
	        int st_dx 		= 0;
	        int st_daux1 	= 0;
	        int st_daux2 	= 0;
	        int st_daux3 	= 0;     	
	        long st_dz 		= 0;
	        
	        // declare finish stepper
	        int fin_dx 		= 0;
	        int fin_daux1 	= 0;
	        int fin_daux2 	= 0; 
	        int fin_daux3 	= 0;          
	        long fin_dz		= 0;
	        
	        int rec;                				// reciprocal value used to divide faster
	        int fustrum_height;	
	        
	        
	        // ---- initialise finish stepper ----
	        if(dy > ay)
	        {
		        rec = NUM / (dy - ay);
		        fin_dx = (dx - ax) * rec;
		        fin_dz = (dz - az) * rec;
		        
		        fin_daux1 = (d_aux1 - a_aux1) * rec;
		        fin_daux2 = (d_aux2 - a_aux2) * rec;
		        fin_daux3 = (d_aux3 - a_aux3) * rec;
	        }
	        
	        // ----------------------- step from a to b -------------------
	        if(by > ay)
	        {
	            // initialise start stepper
	            fustrum_height = by - ay;
	            rec = NUM / fustrum_height;
	            st_dx = (bx - ax) * rec;
	            st_dz = (bz - az) * rec;
	            
	            st_daux1 = (b_aux1 - a_aux1) * rec;
	            st_daux2 = (b_aux2 - a_aux2) * rec;
	            st_daux3 = (b_aux3 - a_aux3) * rec;
	            
	            // render from ay to by
	            System.out.println("render from ay to by");
	            renderDebugTrapezoid(	
	                    st_x,       st_z,
	                    st_aux1, 	st_aux2, 	st_aux3,
	                    st_dx,      st_dz,
	                    st_daux1, 	st_daux2, 	st_daux3,	                    
	                    fin_x,      fin_z,
	                    fin_aux1, 	fin_aux2, 	fin_aux3,
	                    fin_dx,     fin_dz,
	                    fin_daux1, 	fin_daux2, 	fin_daux3,
	                    ay,			by,                            
	                    p,      	
	                    z,    		
	                    tex,		
	                    bw,     	
	                    tw,			
	                    polyData,
	                    lightingValue
	                    );
	            ren.renderTrapezoid(	
	                    st_x,       st_z,
	                    st_aux1, 	st_aux2, 	st_aux3,
	                    st_dx,      st_dz,
	                    st_daux1, 	st_daux2, 	st_daux3,	                    
	                    fin_x,      fin_z,
	                    fin_aux1, 	fin_aux2, 	fin_aux3,
	                    fin_dx,     fin_dz,
	                    fin_daux1, 	fin_daux2, 	fin_daux3,
	                    ay,			by,                            
	                    p,      	
	                    z,    		
	                    tex,		
	                    bw,     	
	                    tw,			
	                    polyData,
	                    lightingValue
	                    );
	
	            // set start point to b
	            st_x = bx << SHIFT;
	            st_z = bz << SHIFT;
	            
	            st_aux1 = b_aux1 << SHIFT;
	            st_aux2 = b_aux2 << SHIFT;
	            st_aux3 = b_aux3 << SHIFT;
	
	            // set line finish point
	            fin_x += (fin_dx * fustrum_height);
	            fin_z += (fin_dz * fustrum_height);
	            
	            fin_aux1 += (fin_daux1 * fustrum_height);
	            fin_aux2 += (fin_daux2 * fustrum_height);
	            fin_aux3 += (fin_daux3 * fustrum_height);
	        }
	        else
	        {
	            // case of ay == by
	
	            // set start point to b
	            st_x = bx << SHIFT;
	            st_z = bz << SHIFT;
	            
	            st_aux1 = b_aux1 << SHIFT;
	            st_aux2 = b_aux2 << SHIFT;
	            st_aux3 = b_aux3 << SHIFT;
	        }
	        
	        // --------------- calculate start stepper -----------
	        if(cy > by)
	        {
	            // initialise start stepper
	            fustrum_height = cy - by;
	            rec = NUM / fustrum_height;
	            st_dx = (cx - bx) * rec;
	            st_dz = (cz - bz) * rec;
	            
	            st_daux1 = (c_aux1 - b_aux1) * rec;
	            st_daux2 = (c_aux2 - b_aux2) * rec;
	            st_daux3 = (c_aux3 - b_aux3) * rec;	        	
	        }
	        
	        // --------------- step from b to d ------------------
	        if(dy > by)
	        {
	            fustrum_height = dy - by;
	            
	            // render from by to dy
	            System.out.println("render from by to dy");
	        	renderDebugTrapezoid(
	                    st_x,       st_z,
	                    st_aux1, 	st_aux2, 	st_aux3,
	                    st_dx,      st_dz,
	                    st_daux1, 	st_daux2, 	st_daux3,	                    
	                    fin_x,      fin_z,
	                    fin_aux1, 	fin_aux2, 	fin_aux3,
	                    fin_dx,     fin_dz,
	                    fin_daux1, 	fin_daux2, 	fin_daux3,
	                    by,         dy,         
	                    p,      
	                    z,    
	                    tex,      
	                    bw,     
	                    tw,     
	                    polyData,
	                    lightingValue
	                    );	            
	        	ren.renderTrapezoid(
	                    st_x,       st_z,
	                    st_aux1, 	st_aux2, 	st_aux3,
	                    st_dx,      st_dz,
	                    st_daux1, 	st_daux2, 	st_daux3,	                    
	                    fin_x,      fin_z,
	                    fin_aux1, 	fin_aux2, 	fin_aux3,
	                    fin_dx,     fin_dz,
	                    fin_daux1, 	fin_daux2, 	fin_daux3,
	                    by,         dy,         
	                    p,      
	                    z,    
	                    tex,      
	                    bw,     
	                    tw,     
	                    polyData,
	                    lightingValue
	                    );
	
	            // set finish point to d
	            fin_x = dx << SHIFT;
	            fin_z = dz << SHIFT;
	            
	            fin_aux1 = d_aux1 << SHIFT;
	            fin_aux2 = d_aux2 << SHIFT;
	            fin_aux3 = d_aux3 << SHIFT;
	
	            // set line start point
	            st_x += (st_dx * fustrum_height);
	            st_z += (st_dz * fustrum_height);
	            
	            st_aux1 += (st_daux1 * fustrum_height);
	            st_aux2 += (st_daux2 * fustrum_height);
	            st_aux3 += (st_daux3 * fustrum_height);
	        }
	        else
	        {
	            // case of by == dy
	
	            // set finish point to d
	            fin_x = dx << SHIFT;
	            fin_z = dz << SHIFT;
	            
	            fin_aux1 = d_aux1 << SHIFT;
	            fin_aux2 = d_aux2 << SHIFT;
	            fin_aux3 = d_aux3 << SHIFT;
	        }
	        
	        // --------------- step from d to c ------------------
	        if(cy > dy)
	        {
	            // initialise finish stepper
	            fustrum_height = cy - dy;
	            rec = NUM / fustrum_height;
	            fin_dx = (cx - dx) * rec;
	            fin_dz = (cz - dz) * rec;
	            
	            fin_daux1 = (c_aux1 - d_aux1) * rec;
	            fin_daux2 = (c_aux2 - d_aux2) * rec;
	            fin_daux3 = (c_aux3 - d_aux3) * rec;
	
	            // render from dy to cy
	            System.out.println("render from dy to cy");
	            renderDebugTrapezoid(
	            		
	                    st_x,		st_z,
	                    st_aux1,	st_aux2,	st_aux3,
	                    st_dx,      st_dz,
	                    st_daux1,	st_daux2,	st_daux3,	                    
	                    fin_x,      fin_z,
	                    fin_aux1, 	fin_aux2, 	fin_aux3,
	                    fin_dx,     fin_dz,
	                    fin_daux1, 	fin_daux2, 	fin_daux3,
	                    dy,         cy,         
	                    p,      
	                    z,    
	                    tex,      
	                    bw,     
	                    tw,     
	                    polyData,
	                    lightingValue
	                    );	            
	            ren.renderTrapezoid(
	
	                    st_x,		st_z,
	                    st_aux1,	st_aux2,	st_aux3,
	                    st_dx,      st_dz,
	                    st_daux1,	st_daux2,	st_daux3,	                    
	                    fin_x,      fin_z,
	                    fin_aux1, 	fin_aux2, 	fin_aux3,
	                    fin_dx,     fin_dz,
	                    fin_daux1, 	fin_daux2, 	fin_daux3,
	                    dy,         cy,         
	                    p,      
	                    z,    
	                    tex,      
	                    bw,     
	                    tw,     
	                    polyData,
	                    lightingValue
	                    );
	
	            // set finish point to c
	            fin_x = cx << SHIFT;
	            fin_z = cz << SHIFT;
	            
	            fin_aux1 = c_aux1 << SHIFT;
	            fin_aux2 = c_aux2 << SHIFT;
	            fin_aux3 = c_aux3 << SHIFT;
	
	            // set line start point to c
	            st_x = fin_x;
	            st_z = fin_z;
	            
	            st_aux1 = fin_aux1;
	            st_aux2 = fin_aux2;
	            st_aux3 = fin_aux3;
	        }
	        else
	        {
	            // case of by == dy
	
	            // set finish point to d
	            fin_x = dx << SHIFT;
	            fin_z = dz << SHIFT;
	            
	            fin_aux1 = d_aux1 << SHIFT;
	            fin_aux2 = d_aux2 << SHIFT;
	            fin_aux3 = d_aux3 << SHIFT;
	        }
	        
	        // render final line
        	ren.renderTrapezoid(
        			
                    st_x,       st_z,
                    st_aux1, 	st_aux2,	st_aux3,
                    0, 			0,
                    0, 			0, 			0,                   
                    fin_x,		fin_z,
                    fin_aux1, 	fin_aux2, 	fin_aux3,
                    0, 			0,
                    0, 			0, 			0,
                    cy,			cy+1,         
                    p,      
                    z,    
                    tex,      
                    bw,     
                    tw,     
                    polyData,
                    lightingValue
                    );
	        
	    }
	    
	    
	    //*********************** PLOT CASE 3 *********************************
	    /** plot case 3 a < d < b < c */
	    private static final void plotCase3(final NitrogenContext context, final Vert a, final Vert b, final Vert c, final Vert d, final Renderer ren, final int[] polyData, final TexMap tm, float lightingValue)
	    {
			System.out.println("plot case 3 where vert a < vert d < vert b < vert c");
		    System.out.println("vert a = " + a.sx + "," + a.sy );	    
		    System.out.println("vert b = " + b.sx + "," + b.sy );	    
		    System.out.println("vert c = " + c.sx + "," + c.sy );	    
		    System.out.println("vert d = " + d.sx + "," + d.sy );	 
		    
		    // these values localised first to catch degenerate polygons early
	        int ay = a.sy;
	        int cy = c.sy;
	
	        // catch degenerate polygons
	        if(ay == cy)return;
	        context.linesRendered += (cy - ay);
	
	        // localise a coordinates
	        int ax = a.sx;
	//        int ay = a.sy;
	        long az = a.sz;
	        
	        int a_aux1 = (int)a.aux1;
	        int a_aux2 = (int)a.aux2;
	        int a_aux3 = (int)a.aux3;
	
	        // localise b coordinates
	        int bx = b.sx;
	        int by = b.sy;
	        long bz = b.sz;
	        
	        int b_aux1 = (int)b.aux1;
	        int b_aux2 = (int)b.aux2;
	        int b_aux3 = (int)b.aux3;
	        
	        // localise c coordinates
	        int cx = c.sx;
//	        int cy = c.sy;
	        long cz = c.sz;
	        
	        int c_aux1 = (int)c.aux1;
	        int c_aux2 = (int)c.aux2;
	        int c_aux3 = (int)c.aux3;	
	        
	        // localise d coordinates
	        int dx = d.sx;
	        int dy = d.sy;
	        long dz = d.sz;
	        
	        int d_aux1 = (int)d.aux1;
	        int d_aux2 = (int)d.aux2;
	        int d_aux3 = (int)d.aux3;	        
	
	        // localise pixel buffer and texture and context width and texture width
	        int[] p 	= context.pix;
	        int[] z 	= context.zbuff;
	        int bw 		= context.w;
	        int[] tex = null;
	        int tw = 0;
	        if(tm != null){tex = tm.tex; tw = tm.w;}	
	        // set start point to a
	        int st_x = ax << SHIFT;
	        long st_z = az << SHIFT;    // z-buffer is 32bit so need to use a long to scale up
	        
	        int st_aux1 = a_aux1 << SHIFT;
	        int st_aux2 = a_aux2 << SHIFT;
	        int st_aux3 = a_aux3 << SHIFT;
	
	        // set finish point to a
	        int  fin_x = ax << SHIFT;
	        long fin_z = az << SHIFT;
	        
	        int fin_aux1 = a_aux1 << SHIFT;
	        int fin_aux2 = a_aux2 << SHIFT;
	        int fin_aux3 = a_aux3 << SHIFT;
	
	        // declare start stepper
	        int st_dx 		= 0;
	        int st_daux1 	= 0;
	        int st_daux2 	= 0;
	        int st_daux3 	= 0;     	
	        long st_dz 		= 0;
	        
	        // declare finish stepper
	        int fin_dx 		= 0;
	        int fin_daux1 	= 0;
	        int fin_daux2 	= 0; 
	        int fin_daux3 	= 0;          
	        long fin_dz		= 0;
	        
	        int rec;                				// reciprocal value used to divide faster
	        int fustrum_height;	
	        
	        
	        // ---- initialise start stepper ----
	        if(by > ay)
	        {
		        rec = NUM / (by - ay);
		        st_dx = (bx - ax) * rec;
		        st_dz = (bz - az) * rec;
		        
		        st_daux1 = (b_aux1 - a_aux1) * rec;
		        st_daux2 = (b_aux2 - a_aux2) * rec;
		        st_daux3 = (b_aux3 - a_aux3) * rec;
	        }
	        
	        // ----------------------- step from a to d -------------------
	        if(dy > ay)
	        {
	            // initialise finish stepper
	            fustrum_height = dy - ay;
	            rec = NUM / fustrum_height;
	            fin_dx = (dx - ax) * rec;
	            fin_dz = (dz - az) * rec;
	            
	            fin_daux1 = (d_aux1 - a_aux1) * rec;
	            fin_daux2 = (d_aux2 - a_aux2) * rec;
	            fin_daux3 = (d_aux3 - a_aux3) * rec;
	            
	            // render from ay to dy
	            System.out.println("render from ay to dy");
	            renderDebugTrapezoid(	
	                    st_x,       st_z,
	                    st_aux1, 	st_aux2, 	st_aux3,
	                    st_dx,      st_dz,
	                    st_daux1, 	st_daux2, 	st_daux3,	                    
	                    fin_x,      fin_z,
	                    fin_aux1, 	fin_aux2, 	fin_aux3,
	                    fin_dx,     fin_dz,
	                    fin_daux1, 	fin_daux2, 	fin_daux3,
	                    ay,			dy,                            
	                    p,      	
	                    z,    		
	                    tex,		
	                    bw,     	
	                    tw,			
	                    polyData,
	                    lightingValue
	                    );	            
	            ren.renderTrapezoid(	
	                    st_x,       st_z,
	                    st_aux1, 	st_aux2, 	st_aux3,
	                    st_dx,      st_dz,
	                    st_daux1, 	st_daux2, 	st_daux3,	                    
	                    fin_x,      fin_z,
	                    fin_aux1, 	fin_aux2, 	fin_aux3,
	                    fin_dx,     fin_dz,
	                    fin_daux1, 	fin_daux2, 	fin_daux3,
	                    ay,			dy,                            
	                    p,      	
	                    z,    		
	                    tex,		
	                    bw,     	
	                    tw,			
	                    polyData,
	                    lightingValue
	                    );
	
	            // set finish point to d
	            fin_x = dx << SHIFT;
	            fin_z = dz << SHIFT;
	            
	            fin_aux1 = d_aux1 << SHIFT;
	            fin_aux2 = d_aux2 << SHIFT;
	            fin_aux3 = d_aux3 << SHIFT;
	
	            // set line start point
	            st_x += (st_dx * fustrum_height);
	            st_z += (st_dz * fustrum_height);
	            
	            st_aux1 += (st_daux1 * fustrum_height);
	            st_aux2 += (st_daux2 * fustrum_height);
	            st_aux3 += (st_daux3 * fustrum_height);
	        }
	        else
	        {
	            // case of ay == dy
	        	
	            // set finish point to d
	            fin_x = dx << SHIFT;
	            fin_z = dz << SHIFT;
	            
	            fin_aux1 = d_aux1 << SHIFT;
	            fin_aux2 = d_aux2 << SHIFT;
	            fin_aux3 = d_aux3 << SHIFT;	

	        }
	        
	        // --------------- calculate finish stepper -----------
	        if(cy > dy)
	        {
	            // initialise finish stepper
	            fustrum_height = cy - dy;
	            rec = NUM / fustrum_height;
	            fin_dx = (cx - dx) * rec;
	            fin_dz = (cz - dz) * rec;
	            
	            fin_daux1 = (c_aux1 - d_aux1) * rec;
	            fin_daux2 = (c_aux2 - d_aux2) * rec;
	            fin_daux3 = (c_aux3 - d_aux3) * rec;	        	
	        }
	        
	        // --------------- step from d to b ------------------
	        if (by > dy)
	        {
	        	fustrum_height = by - dy;
	            // render from dy to by 
	            System.out.println("render from dy to by"); 
	        	renderDebugTrapezoid(
	                    st_x,       st_z,
	                    st_aux1, 	st_aux2, 	st_aux3,
	                    st_dx,      st_dz,
	                    st_daux1, 	st_daux2, 	st_daux3,	                    
	                    fin_x,      fin_z,
	                    fin_aux1, 	fin_aux2, 	fin_aux3,
	                    fin_dx,     fin_dz,
	                    fin_daux1, 	fin_daux2, 	fin_daux3,
	                    dy,         by,         
	                    p,      
	                    z,    
	                    tex,      
	                    bw,     
	                    tw,     
	                    polyData,
	                    lightingValue
	                    );	            
	        	ren.renderTrapezoid(
	                    st_x,       st_z,
	                    st_aux1, 	st_aux2, 	st_aux3,
	                    st_dx,      st_dz,
	                    st_daux1, 	st_daux2, 	st_daux3,	                    
	                    fin_x,      fin_z,
	                    fin_aux1, 	fin_aux2, 	fin_aux3,
	                    fin_dx,     fin_dz,
	                    fin_daux1, 	fin_daux2, 	fin_daux3,
	                    dy,         by,         
	                    p,      
	                    z,    
	                    tex,      
	                    bw,     
	                    tw,     
	                    polyData,
	                    lightingValue
	                    );
	
	            // set start point to b
	            st_x = bx << SHIFT;
	            st_z = bz << SHIFT;
	            
	            st_aux1 = b_aux1 << SHIFT;
	            st_aux2 = b_aux2 << SHIFT;
	            st_aux3 = b_aux3 << SHIFT;
	
	            // set line finish point
	            fin_x += (fin_dx * fustrum_height);
	            fin_z += (fin_dz * fustrum_height);
	            
	            fin_aux1 += (fin_daux1 * fustrum_height);
	            fin_aux2 += (fin_daux2 * fustrum_height);
	            fin_aux3 += (fin_daux3 * fustrum_height);
	        }
	        else
	        {
	            // case of by == dy
	
	            // set start point to b
	            st_x = bx << SHIFT;
	            st_z = bz << SHIFT;
	            
	            st_aux1 = b_aux1 << SHIFT;
	            st_aux2 = b_aux2 << SHIFT;
	            st_aux3 = b_aux3 << SHIFT;
	        }
	        
	        // --------------- step from b to c ------------------
	        if(cy > by)
	        {
	            // initialise finish stepper
	            fustrum_height = cy - by;
	            rec = NUM / fustrum_height;
	            st_dx = (cx - bx) * rec;
	            st_dz = (cz - bz) * rec;
	            
	            st_daux1 = (c_aux1 - b_aux1) * rec;
	            st_daux2 = (c_aux2 - b_aux2) * rec;
	            st_daux3 = (c_aux3 - b_aux3) * rec;
	
	            // render from by to cy
	            System.out.println("render from by to cy");
	            renderDebugTrapezoid(
	            		
	                    st_x,		st_z,
	                    st_aux1,	st_aux2,	st_aux3,
	                    st_dx,      st_dz,
	                    st_daux1,	st_daux2,	st_daux3,	                    
	                    fin_x,      fin_z,
	                    fin_aux1, 	fin_aux2, 	fin_aux3,
	                    fin_dx,     fin_dz,
	                    fin_daux1, 	fin_daux2, 	fin_daux3,
	                    by,         cy,         
	                    p,      
	                    z,    
	                    tex,      
	                    bw,     
	                    tw,     
	                    polyData,
	                    lightingValue
	                    );	            
	            ren.renderTrapezoid(
	
	                    st_x,		st_z,
	                    st_aux1,	st_aux2,	st_aux3,
	                    st_dx,      st_dz,
	                    st_daux1,	st_daux2,	st_daux3,	                    
	                    fin_x,      fin_z,
	                    fin_aux1, 	fin_aux2, 	fin_aux3,
	                    fin_dx,     fin_dz,
	                    fin_daux1, 	fin_daux2, 	fin_daux3,
	                    by,         cy,         
	                    p,      
	                    z,    
	                    tex,      
	                    bw,     
	                    tw,     
	                    polyData,
	                    lightingValue
	                    );
	
	            // set finish point to c
	            fin_x = cx << SHIFT;
	            fin_z = cz << SHIFT;
	            
	            fin_aux1 = c_aux1 << SHIFT;
	            fin_aux2 = c_aux2 << SHIFT;
	            fin_aux3 = c_aux3 << SHIFT;
	
	            // set line start point to c
	            st_x = fin_x;
	            st_z = fin_z;
	            
	            st_aux1 = fin_aux1;
	            st_aux2 = fin_aux2;
	            st_aux3 = fin_aux3;
	        }
	        else
	        {
	            // case of by == dy
	
	            // set start point to b
	            st_x = bx << SHIFT;
	            st_z = bz << SHIFT;
	            
	            st_aux1 = b_aux1 << SHIFT;
	            st_aux2 = b_aux2 << SHIFT;
	            st_aux3 = b_aux3 << SHIFT;
	        }
	        
	        // render final line
        	ren.renderTrapezoid(
        			
                    st_x,       st_z,
                    st_aux1, 	st_aux2,	st_aux3,
                    0, 			0,
                    0, 			0, 			0,                   
                    fin_x,		fin_z,
                    fin_aux1, 	fin_aux2, 	fin_aux3,
                    0, 			0,
                    0, 			0, 			0,
                    cy,			cy+1,         
                    p,      
                    z,    
                    tex,      
                    bw,     
                    tw,     
                    polyData,
                    lightingValue
                    );
	    	
	    }

	    //*********************** PLOT CASE 4 *********************************
	    /** plot case 4 a < d < c < b */	    
	    private static final void plotCase4(final NitrogenContext context, final Vert a, final Vert b, final Vert c, final Vert d, final Renderer ren, final int[] polyData, final TexMap tm, float lightingValue)
	    {
			System.out.println(" plot case 4 where vert a < vert d < vert c < vert b");
		    System.out.println("vert a = " + a.sx + "," + a.sy );	    
		    System.out.println("vert b = " + b.sx + "," + b.sy );	    
		    System.out.println("vert c = " + c.sx + "," + c.sy );	    
		    System.out.println("vert d = " + d.sx + "," + d.sy );	 
	    	
	    	// these values localised first to catch degenerate polygons early
	        int ay = a.sy;
	        int by = b.sy;
	
	        // catch degenerate polygons
	        if(ay == by)return;
	        context.linesRendered += (by - ay);
	
	        // localise a coordinates
	        int ax = a.sx;
	//        int ay = a.sy;
	        long az = a.sz;
	        
	        int a_aux1 = (int)a.aux1;
	        int a_aux2 = (int)a.aux2;
	        int a_aux3 = (int)a.aux3;
	
	        // localise b coordinates
	        int bx = b.sx;
//	        int by = b.sy;
	        long bz = b.sz;
	        
	        int b_aux1 = (int)b.aux1;
	        int b_aux2 = (int)b.aux2;
	        int b_aux3 = (int)b.aux3;
	        
	        // localise c coordinates
	        int cx = c.sx;
	        int cy = c.sy;
	        long cz = c.sz;
	        
	        int c_aux1 = (int)c.aux1;
	        int c_aux2 = (int)c.aux2;
	        int c_aux3 = (int)c.aux3;	
	        
	        // localise d coordinates
	        int dx = d.sx;
	        int dy = d.sy;
	        long dz = d.sz;
	        
	        int d_aux1 = (int)d.aux1;
	        int d_aux2 = (int)d.aux2;
	        int d_aux3 = (int)d.aux3;	        
	
	        // localise pixel buffer and texture and context width and texture width
	        int[] p = context.pix;
	        int[] z = context.zbuff;
	        int bw = context.w;
	        int[] tex = null;
	        int tw = 0;
	        if(tm != null){tex = tm.tex; tw = tm.w;}
	
	        // set start point to a
	        int st_x 	= ax << SHIFT;
	        long st_z 	= az << SHIFT;    // z-buffer is 32bit so need to use a long to scale up
	        
	        int st_aux1 = a_aux1 << SHIFT;
	        int st_aux2 = a_aux2 << SHIFT;
	        int st_aux3 = a_aux3 << SHIFT;
	
	        // set finish point to a
	        int fin_x = ax << SHIFT;
	        long fin_z = az << SHIFT;
	        
	        int fin_aux1 = a_aux1 << SHIFT;
	        int fin_aux2 = a_aux2 << SHIFT;
	        int fin_aux3 = a_aux3 << SHIFT;
	
	        int st_dx, st_daux1, st_daux2, st_daux3;     	// declare start steppers
	        int fin_dx, fin_daux1, fin_daux2, fin_daux3;    // declare finish steppers
	        long st_dz;                                 	// declare start z stepper
	        long fin_dz;                                	// declare finish z stepper
	        int rec;                				// reciprocal value used to divide faster
	        int fustrum_height;
	
	        // initialise start stepper
	        rec = NUM / (by - ay);
	        st_dx = (bx - ax) * rec;
	        st_dz = (bz - az) * rec;
	        
	        st_daux1 = (b_aux1 - a_aux1) * rec;
	        st_daux2 = (b_aux2 - a_aux2) * rec;
	        st_daux3 = (b_aux3 - a_aux3) * rec;
	
	        // ----------------------- step from a to d -------------------
	        if(dy > ay)
	        {
	            // initialise finish stepper
	            fustrum_height = dy - ay;
	            rec = NUM / fustrum_height;
	            fin_dx = (dx - ax) * rec;
	            fin_dz = (dz - az) * rec;
	            
	            fin_daux1 = (d_aux1 - a_aux1) * rec;
	            fin_daux2 = (d_aux2 - a_aux2) * rec;
	            fin_daux3 = (d_aux3 - a_aux3) * rec;
	
		         // render from ay to dy
		         System.out.println("render from ay to dy");
		          renderDebugTrapezoid(
		                    st_x,       st_z,
		                    st_aux1, 	st_aux2,	st_aux3,
		                    st_dx,      st_dz,
		                    st_daux1, 	st_daux2, 	st_daux3,                    
		                    fin_x,      fin_z,
		                    fin_aux1, 	fin_aux2, 	fin_aux3,
		                    fin_dx,     fin_dz,
		                    fin_daux1, 	fin_daux2, 	fin_daux3,
		                    ay,         dy,         
		                    p,      
		                    z,    	
		                    tex,      
		                    bw,     
		                    tw,      
		                    polyData,
		                    lightingValue
		                    );
		
		         ren.renderTrapezoid(
	                    st_x,       st_z,
	                    st_aux1, 	st_aux2,	st_aux3,
	                    st_dx,      st_dz,
	                    st_daux1, 	st_daux2, 	st_daux3,                    
	                    fin_x,      fin_z,
	                    fin_aux1, 	fin_aux2, 	fin_aux3,
	                    fin_dx,     fin_dz,
	                    fin_daux1, 	fin_daux2, 	fin_daux3,
	                    ay,         dy,         
	                    p,      
	                    z,    	
	                    tex,      
	                    bw,     
	                    tw,      
	                    polyData,
	                    lightingValue
	                    );
	
	            // set finish point to d
	            fin_x = dx << SHIFT;
	            fin_z = dz << SHIFT;
	            
	            fin_aux1 = d_aux1 << SHIFT;
	            fin_aux2 = d_aux2 << SHIFT;
	            fin_aux3 = d_aux3 << SHIFT;
	
	            // set line finish point
	            st_x += (st_dx * fustrum_height);
	            st_z += (st_dz * fustrum_height);
	            
	            st_aux1 += (st_daux1 * fustrum_height);
	            st_aux2 += (st_daux2 * fustrum_height);
	            st_aux3 += (st_daux3 * fustrum_height);
	        }
	        else
	        {
	            // case of ay == dy
	
	            // set finish point to d
	            fin_x = dx << SHIFT;
	            fin_z = dz << SHIFT;
	            
	            fin_aux1 = d_aux1 << SHIFT;
	            fin_aux2 = d_aux2 << SHIFT;
	            fin_aux3 = d_aux3 << SHIFT;
	        }
	
	        // ----------------------- step from d to c -------------------
	        if(cy > dy)
	        {
	            // initialise start stepper
	            fustrum_height = cy - dy;
	            rec = NUM / fustrum_height;
	            fin_dx = (cx - dx) * rec;
	            fin_dz = (cz - dz) * rec;
	            
	            fin_daux1 = (c_aux1 - d_aux1) * rec;
	            fin_daux2 = (c_aux2 - d_aux2) * rec;
	            fin_daux3 = (c_aux3 - d_aux3) * rec;
	
		         // render from dy to cy
		         System.out.println("render from dy to cy");
		            renderDebugTrapezoid(	
		                    st_x, 		st_z,
		                    st_aux1, 	st_aux2,	st_aux3,
		                    st_dx, 		st_dz,
		                    st_daux1, 	st_daux2, 	st_daux3,
		                    fin_x, 		fin_z,
		                    fin_aux1, 	fin_aux2, 	fin_aux3,
		                    fin_dx, 	fin_dz,
		                    fin_daux1, 	fin_daux2, 	fin_daux3,	                    
		                    dy, cy,        
		                    p,      
		                    z,    
		                    tex,      
		                    bw,     
		                    tw,      
		                    polyData,
		                    lightingValue
		                    );
		         ren.renderTrapezoid(	
	                    st_x, 		st_z,
	                    st_aux1, 	st_aux2,	st_aux3,
	                    st_dx, 		st_dz,
	                    st_daux1, 	st_daux2, 	st_daux3,
	                    fin_x, 		fin_z,
	                    fin_aux1, 	fin_aux2, 	fin_aux3,
	                    fin_dx, 	fin_dz,
	                    fin_daux1, 	fin_daux2, 	fin_daux3,	                    
	                    dy, cy,        
	                    p,      
	                    z,    
	                    tex,      
	                    bw,     
	                    tw,      
	                    polyData,
	                    lightingValue
	                    );
	
	            // set finish point to c
	            fin_x = cx << SHIFT;
	            fin_z = cz << SHIFT;
	            
	            fin_aux1 = c_aux1 << SHIFT;
	            fin_aux2 = c_aux2 << SHIFT;
	            fin_aux3 = c_aux3 << SHIFT;
	
	            // set line start point
	            st_x += (st_dx * fustrum_height);
	            st_z += (st_dz * fustrum_height);
	            
	            st_aux1 += (st_daux1 * fustrum_height);
	            st_aux2 += (st_daux2 * fustrum_height);
	            st_aux3 += (st_daux3 * fustrum_height);
	        }
	        else
	        {
	            // case of dy == cy
	
	            // set finish point to c
	            fin_x = cx << SHIFT;
	            fin_z = cz << SHIFT;
	            
	            fin_aux1 = c_aux1 << SHIFT;
	            fin_aux2 = c_aux2 << SHIFT;
	            fin_aux3 = c_aux3 << SHIFT;
	        }
	
	        // ----------------------- step from c to b -------------------
	        if(by > cy)
	        {
	            // initialise start stepper
	            fustrum_height = by - cy;
	            rec = NUM / fustrum_height;
	            fin_dx = (bx - cx) * rec;
	            fin_dz = (bz - cz) * rec;
	            
	            fin_daux1 = (b_aux1 - c_aux1) * rec;
	            fin_daux2 = (b_aux2 - c_aux2) * rec;
	            fin_daux3 = (b_aux3 - c_aux3) * rec;
	
	            // render from cy to by	 
	            System.out.println("render from cy to by");
	            renderDebugTrapezoid(
	                    st_x, 		st_z,
	                    st_aux1, 	st_aux2,	st_aux3,
	                    st_dx, 		st_dz,
	                    st_daux1,	st_daux2,	st_daux3,	                    
	                    fin_x, 		fin_z,
	                    fin_aux1, 	fin_aux2, 	fin_aux3,
	                    fin_dx, 	fin_dz,
	                    fin_daux1, 	fin_daux2, 	fin_daux3,
	                    cy, by,                          
	                    p,      
	                    z,    
	                    tex,      
	                    bw,     
	                    tw,      
	                    polyData,
	                    lightingValue
	                    );	            
	            ren.renderTrapezoid(
	                    st_x, 		st_z,
	                    st_aux1, 	st_aux2,	st_aux3,
	                    st_dx, 		st_dz,
	                    st_daux1,	st_daux2,	st_daux3,	                    
	                    fin_x, 		fin_z,
	                    fin_aux1, 	fin_aux2, 	fin_aux3,
	                    fin_dx, 	fin_dz,
	                    fin_daux1, 	fin_daux2, 	fin_daux3,
	                    cy, by,                          
	                    p,      
	                    z,    
	                    tex,      
	                    bw,     
	                    tw,      
	                    polyData,
	                    lightingValue
	                    );
	
	            // set start point to b
	            st_x = bx << SHIFT;
	            st_z = bz << SHIFT;
	            
	            st_aux1 = b_aux1 << SHIFT;
	            st_aux2 = b_aux2 << SHIFT;
	            st_aux3 = b_aux3 << SHIFT;
	
	            // set line finish point also to b
	            fin_x = st_x;
	            fin_z = st_z;
	            
	            fin_aux1 = st_aux1;
	            fin_aux2 = st_aux2;
	            fin_aux3 = st_aux3;
	        }
	        else
	        {
	            // case of cy == by
	            // finish is at c and start is already reached b
	            // so do nothing
	        }
	
	        
	        // render final line. steppers set to 0
	        ren.renderTrapezoid(
	        		st_x, 		st_z,
	        		st_aux1, 	st_aux2,	st_aux3,
	        		0, 			0,              
	        		0, 			0, 			0,	                    
	        		fin_x, 		fin_z,
	        		fin_aux1, 	fin_aux2, 	fin_aux3,
	        		0, 			0,              
	        		0, 			0, 			0,
	        		by, 		by+1,		
	        		p,      
	        		z,    
	        		tex,      
	        		bw,     
	        		tw,      
	        		polyData,
	        		lightingValue
	                );	    	
	    }
	    
	   static final public void renderDebugTrapezoid(

	            // line start point
	            int st_x,   long st_z,
	            int st_aux1,
	            int st_aux2,
	            int st_aux3,
	            
	            // start point increment
	            int st_dx,   long st_dz,
	            int st_daux1,
	            int st_daux2,
	            int st_daux3,
	            
	            // line finish point
	            int fin_x,   long fin_z,
	            int fin_aux1,
	            int fin_aux2,
	            int fin_aux3,                    

	            // finish point increment
	            int fin_dx,   long fin_dz,
	            int fin_daux1,
	            int fin_daux2,
	            int fin_daux3,
	            
	            // start and finish y values
	            // note the last line y_max is not rendered
	            int y_counter,   int y_max,

	            // pixel buffer
	            int[] p,
	            
	            // z buffer
	            int[] z,

	            // texture buffer
	            int[] tex,

	            // output image width
	            int pixelBufferWidth,

	            // input texture width
	            int textureBufferWidth,
	            
	            // global parameters array - eg. the colour for a single colour polygon
	            int[] polyData,
	            float lightingValue
	            )
	    {
	    	System.out.println("Printed polygon");
	    	return;
	    	/*
		    System.out.println("------------------------------");
	    	System.out.println("line start point");
            System.out.println("st_x" + st_x + "(" + (st_x >> 20));  
            System.out.println("st_z" + st_z + "(" + (st_z >> 20));           
            System.out.println("st_aux1" + st_aux1 + "(" + (st_aux1 >> 20));
            System.out.println("st_aux2" + st_aux2 + "(" + (st_aux2 >> 20));
            System.out.println("st_aux3" + st_aux3 + "(" + (st_aux3 >> 20));
            
            System.out.println("start point increment");
            System.out.println("st_dx" + st_dx); 
            System.out.println("st_dz" + st_dz); 
            System.out.println("st_daux1" + st_daux1);             
            System.out.println("st_daux2" + st_daux2);             
            System.out.println("st_daux3" + st_daux3);             
            
	    	System.out.println("line finish point");
            System.out.println("fin_x" + fin_x + "(" + (fin_x >> 20));  
            System.out.println("fin_z" + fin_z + "(" + (fin_z >> 20));           
            System.out.println("fin_aux1" + fin_aux1 + "(" + (fin_aux1 >> 20));
            System.out.println("fin_aux2" + fin_aux2 + "(" + (fin_aux2 >> 20));
            System.out.println("fin_aux3" + fin_aux3 + "(" + (fin_aux3 >> 20));
                  

            System.out.println("finish point increment");
            System.out.println("fin_dx" + fin_dx); 
            System.out.println("fin_dz" + fin_dz); 
            System.out.println("fin_daux1" + fin_daux1);             
            System.out.println("fin_daux2" + fin_daux2);             
            System.out.println("fin_daux3" + fin_daux3); 
            
            System.out.println("start and finish y values");
            System.out.println("start y = " + y_counter);
            System.out.println("y_max = " + y_max);
            System.out.println("pixelBufferWidth = " + pixelBufferWidth);
            System.out.println("textureBufferWidth = " + textureBufferWidth); 	
	    	System.out.println("------------------------------");
	    	*/
	    }
}

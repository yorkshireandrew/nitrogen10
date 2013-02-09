/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.bombheadgames.nitrogen1;

/**
 *
 * @author andrew
 * 
 * 
 */
public interface Renderer {

			/**
			 * Renders a trapezoid with a horizontal top and bottom. 
			 * An implementation of this interface is called at the end of the rendering pipeline in order to finally render part of a polygon into the pixel buffer and z buffer.
			 * It allows polygons to own and apply many different rendering strategies, for example fixed colour or textured pixel rendering. 
		
			 * <br /><br />
			 * The start point is the top left of the trapezoid. The finish point is the top right of the trapezoid.
			 * @param st_x	Start point x coordinate (scaled up by left shift) 
			 * @param st_z  Start point z coordinate (scaled up by left shift).<br />This is a long as the un-scaled z is a 32 bit value.
			 * @param st_aux1 Start point aux1 coordinate (scaled up by left shift)
			 * @param st_aux2 Start point aux2 coordinate (scaled up by left shift)
			 * @param st_aux3 Start point aux3 coordinate (scaled up by left shift)
			 * @param st_dx Change in (scaled up) start x coordinate per step in y
			 * @param st_dz Change in (scaled up) start z coordinate per step in y
			 * @param st_daux1 Change in (scaled up) start aux1 coordinate per step in y
			 * @param st_daux2 Change in (scaled up) start aux2 coordinate per step in y
			 * @param st_daux3 Change in (scaled up) start aux3 coordinate per step in y
			 * <br /><br />
			 * @param fin_x Finish point x coordinate (scaled up by left shift)
			 * @param fin_z Finish point z coordinate (scaled up by left shift).<br />This is a long as the un-scaled z is a 32 bit value. 
			 * @param fin_aux1 Finish point aux1 coordinate (scaled up by left shift)
			 * @param fin_aux2 Finish point aux2 coordinate (scaled up by left shift)
			 * @param fin_aux3 Finish point aux3 coordinate (scaled up by left shift)
			 * @param fin_dx Change in (scaled up) finish x coordinate per step in y
			 * @param fin_dz Change in (scaled up) finish z coordinate per step in y
			 * @param fin_daux1 Change in (scaled up) start aux1 coordinate per step in y
			 * @param fin_daux2 Change in (scaled up) start aux2 coordinate per step in y
			 * @param fin_daux3 Change in (scaled up) start aux3 coordinate per step in y
			 * <br /><br />
			 * @param y_counter The y coordinate of the top of the trapezoid (its lowest y value)
			 * @param y_max The y coordinate of the bottom of the trapezoid PLUS ONE. 
			 * @param p The pixel buffer to render into
			 * @param z The z buffer to render into
			 * @param tex Texture pixel array
			 * @param pixelBufferWidth Width of the pixel buffer (not scaled up)
			 * @param textureBufferWidth Width of the pixel buffer (not scaled up)
			 * @param polyData polygon data from the immutable polygon. Containing colour data etc
			 * @param lightingValue value used for lighting that may have been computed by the polygons associated backside
			 */
            public void renderTrapezoid(

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
                    float lightingValue,
                    NitrogenContext context
                    );
            
            boolean usesHLPBreak();

}

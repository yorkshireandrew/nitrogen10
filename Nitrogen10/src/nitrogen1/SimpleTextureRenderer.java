/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package nitrogen1;

/**
 *
 * @author andrew
 */
public class SimpleTextureRenderer implements Renderer{

static final int SH = 20;
static final int NUM = 1 << SH;

/**
 * 
 * @param st_aux1	texture map x coord for line start point
 * @param st_aux2	texture map y coord for line start point
 * @param st_aux3	not used in SimpleTextureRenderer
 * 
 * @param st_daux1	increment of texture map x coord (for line start point) for a one pixel step along the y axis
 * @param st_daux2	increment of texture map y coord (for line start point) for a one pixel step along the y axis
 * @param st_daux3	not used in SimpleTextureRenderer
 * 
 * @param fin_aux1	texture map x coord for line finish point
 * @param fin_aux2	texture map y coord for line finish point
 * @param fin_aux3	not used in SimpleTextureRenderer
 * 
 * @param fin_daux1	increment of texture map x coord (for line finish point) for a one pixel step along the y axis
 * @param fin_daux2	increment of texture map y coord (for line finish point) for a one pixel step along the y axis
 * @param fin_daux3	not used in SimpleTextureRenderer
 */
final public void renderTrapezoid(

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

        // local variables used to step along a line

        // line rendering fields
        int srl_st_x;
        int srl_fin_x;
        long srl_z;

        // line rendering fields used for stepping
        int srl_dx;
        long srl_dz;

        int linestart;      // value that is constant for a line - gets precalculated

        // fields used for pixel rendering
        int srl_z2;         // integer version of srl_z
        int index;          // index into colour and z buffer arrays

        long temp;          // used to do a faster divide
        int rec;
        int colour = polyData[0];

        while(y_counter < y_max)
        {

            //***********************************************
            //****                                       ****
            //****    START OF CODE TO RENDER A LINE     ****
            //****                                       ****
            //***********************************************

            // set line rendering fields to start of line
            srl_st_x = (st_x >> SH);
            srl_fin_x = (fin_x >> SH);
            srl_dx = srl_fin_x - srl_st_x;
            srl_z = st_z;

            // prevent zero srl_dx case throwing div0
            if(srl_dx == 0) srl_dx = 1;

            // calculate line rendering fields used for stepping
            rec = NUM / srl_dx;

            temp = (fin_z - st_z);
            srl_dz = ((temp * rec) >> SH);

            linestart = y_counter * pixelBufferWidth;         // pre-calculate this constant value

            while(srl_st_x <= srl_fin_x)
            {
                // *******************************************
                // *******************************************
                // ****                                   ****
                // ****     START OF RENDER A PIXEL       ****
                // ****                                   ****
                // *******************************************
                // *******************************************

                srl_z2 = (int)(srl_z >> SH);
                index = (linestart + srl_st_x);

                if(srl_z2 >= z[index])
                {
                    p[index] = colour;
                    z[index] = srl_z2;
                    //p[index] = (int)(0xFF000000 + (srl_z2));    // test
                }

                // *******************************************
                // *******************************************
                // ****                                   ****
                // ****       END OF RENDER A PIXEL       ****
                // ****                                   ****
                // *******************************************
                // *******************************************

                srl_st_x++;
                srl_z   += srl_dz;
            }// end of line rendering loop
            //***********************************************
            //****                                       ****
            //****      END OF CODE TO RENDER A LINE     ****
            //****                                       ****
            //***********************************************

            //increment local st_* and fin_* values by passed in parameters

            // increment local line start point (st_*) using passed in parameters
            st_x    += st_dx;
            st_z    += st_dz;

            // increment local line finish point (fin_*) using passed in parameters
            fin_x    += fin_dx;
            fin_z    += fin_dz;

            // move to next line
            y_counter++;
    }//end of y_counter while loop
}

}// end of class

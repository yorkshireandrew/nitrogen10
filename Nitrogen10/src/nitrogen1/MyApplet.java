/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package nitrogen1;

// 

/**
 *
 * @author andrew
 */

import javax.swing.*;
import java.awt.*;  // needed for Dimension class

final public class MyApplet extends JApplet{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1799576836511527595L;
	static final int APP_WIDTH = 501;
    static final int APP_HEIGHT =501;
    static final Renderer simpleTextureRenderer = new SimpleTextureRenderer();

    	public void init()
	{
            getContentPane().setLayout(new BorderLayout());
            Box box_image = Box.createHorizontalBox();

            NitrogenContext nc = new NitrogenContext(510,510,1,1,1,1000);
            box_image.add(nc);

            getContentPane().add(box_image);
            getContentPane().validate();
            getContentPane().setVisible(true);

            nc.cls(0xFF0000FF);
            
            // start rendering process
            Transform t1, t2;
            SharedImmutableSubItem sisi = null;
           
            t1	= new 	Transform(
            						null,
            						1f, 0f, 0f, 0f,
            						0f, 1f, 0f, 0f,
            						0f, 0f, 1f, 0f);
            
            t2	= new 	Transform(
					t1,
					1f, 0f, 0f, 0f,
					0f, 1f, 0f, 0f,
					0f, 0f, 1f, -20f);          
            
            // add renderers to RendererHelper
            SimpleTextureRenderer str = new SimpleTextureRenderer();
            RendererTriplet rt = new RendererTriplet(str);
            try
            {
            	RendererHelper.addRendererTriplet("str",rt);
            }
            catch(Exception e){System.out.println(e.getMessage());}

            SimpleSingleColourRenderer sscr = new SimpleSingleColourRenderer();           
            RendererTriplet sscrt = new RendererTriplet(sscr);
            try
            {
            	RendererHelper.addRendererTriplet("sscr",sscrt);
            }
            catch(Exception e){System.out.println(e.getMessage());}
            
            try{
            	
            	sisi = new SharedImmutableSubItem("test1.txt");
            }
            catch(NitrogenCreationException e)
            {
            	e.printStackTrace();           	
            }
            
            Item i = new Item(sisi,t2);
            i.setVisibility(true);
            
            t1.render(nc);
            nc.repaint();
            
            
            
            
    /*        

            int wid = 500;
            int high = 500;
            Vert    v_a     = new Vert(wid,0);
            Vert    v_b     = new Vert(0,1);
            Vert    v_c     = new Vert(0,high);
            Vert    v_d     = new Vert(wid,high+1);
            
            // set texture coordinates
            v_a.setAux2(101,1);
            v_b.setAux2(1,1);
            v_c.setAux2(1,101);
            v_d.setAux2(101,101);
            
            v_a.setZ(255);
            v_b.setZ(255);
            v_c.setZ(125);
            v_d.setZ(0);
            
            long t1, t2, test1;
            test1 = 0;

            int maxloops = 1;
            int innerloops = 100;
            
            for(int loop = 0; loop < maxloops; loop++)
            {
                mb.cls(0xFF0000FF);
                t1 = System.currentTimeMillis();
                for(int x = 0; x < innerloops; x++)
                {
                    v_a.setZ(-x);
                    v_b.setZ(-x);
                    v_c.setZ(-x);
                    v_d.setZ(-x);
                    mb.Plot(v_a, v_b, v_c, v_d, hello_world_texture, null, simpleTextureRenderer);
                }
                t2 = System.currentTimeMillis();

                test1 += (t2 - t1);

                mb.repaint();

        }
            System.out.println(" Normal      = " + test1);
*/
    }


    @Override
        public Dimension getMinimumSize()
    {
        System.out.printf("getMinimumSize");
        return new Dimension(APP_WIDTH,APP_HEIGHT);
    }

    @Override
    public Dimension getPreferredSize()
    {
        System.out.printf("getPreferredSize");
        return new Dimension(APP_WIDTH,APP_HEIGHT);
    }

    @Override
    public Dimension getMaximumSize()
    {
        System.out.printf("getMaximumSize");
        return new Dimension(APP_WIDTH,APP_HEIGHT);
    }
}// end of MyApplet

package nitrogen1;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.MemoryImageSource;

/**
 * 
 * @author andrew
 * This holds key information about the screen 
 * and world in which 3D items are rendered
 *
 */

import javax.swing.*;
// import java.awt.*;  // needed for Dimension class
// import java.awt.image.MemoryImageSource; // for memory image source
// import nitrogen1.TexMap;
// import nitrogen1.Vert;

public class NitrogenContext extends JButton{
	static final int SHIFT = 20;	// shift used when rendering textures
	
	/** multiplier used in perspective magnifying the image so that the xclip value equals the right of button */
	final float magnification;
	
	/** value used in mapping view space z values to screen space z values */
	final float zk;
	
	/** value of x/z that the image is clipped at */
	final float xClip;
	
	/** value of y/z that the image is clipped at */
	final float yClip;
	
	/** distance between viewpoint and near clip plane (a positive value) */
	final float nearClip;
	
	/** distance between viewpoint and far clip plane (a positive value) */
	final float farClip;
	
	/** Value setting the quality of high level perspective (HLP) breaking. 
	 * Setting this to a value closer to one improves quality but also slows the rendering of HLP polygons 
	 */
	float qualityOfHLP = 1.2f;
	
	/** width in pixels of the NitrogenContext */
    int w = 0;		// width
    /** height in pixels of the NitrogenContext */
    int h = 0;		// height
    /** Number of pixels in the NitrogenContext */
    int s = 0;		// size = width * height
    /** Middle of width */
    int midw = w/2;
    /** Middle of height */
    int midh = h/2;
    
    /** The pixel buffer */
    int[] pix;	
    
    /** The z buffer */
    int[] zbuff; 
    
    /** Image that monitors source */
    Image im; 
    
    /** A MemoryImageSource that monitors pix */
    MemoryImageSource source;   
    
    /** If true only transparent polygons are rendered, otherwise only non-transparent polygons are rendered. Used for double pass rendering where the scene contains transparent polygons */
    boolean transparencyPass = false;
    
    /** Constructs a Nitrogen Context to render things into 
     * 
     * @param width	Width of the view-port in pixels
     * @param height Height of the view-port in pixels
     * @param xClip The x/z ratio the view-port clips at 
     * @param yClip The y/z ratio the view-port clips at. Caller must ensure yClip/xClip ratio equals the height/width ratio
     * @param nearClip The distance between the viewer and the near Clip plane
     * @param farClip  The distance between the viewer and the far Clip plane
     */
    NitrogenContext(int width, int height, float xClip, float yClip, float nearClip, float farClip)
    {
    	this.xClip = xClip;
    	this.yClip = yClip;
    	this.nearClip = nearClip;
    	this.farClip = farClip;

    	magnification = ((float)(width/2))/yClip;
    	zk = nearClip * (float)(Integer.MAX_VALUE - 2);
        
    	w = width;
    	h = height;
        // create array to store pixels
        s = w * h;
        pix = new int[s];
        zbuff = new int[s];

        // create an MemoryImageSource bound to the pixel array
        source = new MemoryImageSource(w, h, pix, 0, w);
        source.setAnimated(true);           // source will be used for multiframe animation
        source.setFullBufferUpdates(true);  // source uses a complete buffer of pixels for updates
        im = this.createImage(source);  	// this way of creating a BufferedImage avoids calling toolkit which may need permissions     
    }
    
    @Override
    public void paintComponent(Graphics g)
    {
        source.newPixels();         // causes source to send whole new buffer of pixels to listeners ... im
        g.drawImage(im,0,0,null);   // draws an image bound to pix in top-lh corner
    }
    
    final public void cls(int col)
    {
        int[] pixloc = pix;   // local copys
        int[] zloc = zbuff;

        // fill in first line
        for(int x = 0; x < w; x++)   pixloc[x]= col;
        int y = 1;

        // copy the first line over remaining lines
        while(y < h)
        {
           System.arraycopy(pixloc, 0, pixloc, (y * w), w);
           y++;
        }

        // repeat for z buffer setting to most -ve value possible
        for(int x = 0; x < w; x++)   
        {
            
            zloc[x] = Integer.MIN_VALUE;
        }
        y = 1;

        // copy the first line over remaining lines
        while(y < h)
        {
           System.arraycopy(zloc, 0, zloc, (y * w), w);
           y++;
        }

    }
    
@Override
public Dimension getMinimumSize()
{
    //System.out.printf("getMinimumSize");
    return new Dimension(w,h);
}

@Override
public Dimension getPreferredSize()
{
    //System.out.printf("getPreferredSize");
    return new Dimension(w,h);
}

@Override
public Dimension getMaximumSize()
{
    //System.out.printf("getMaximumSize");
    return new Dimension(w,h);
}

}

package nitrogen1;

import java.io.Serializable;

/** A class that contains a near,mid and far renderer */
public class RendererTriplet implements Serializable{
	private static final long serialVersionUID = -8579367069627809713L;
	// Enumerations for whichRenderer
	static final int NEAR_RENDERER = 0;
	static final int MID_RENDERER = 1;
	static final int FAR_RENDERER = 2;
	static Renderer pickingRenderer;
	
	Renderer nearRenderer;
	Renderer midRenderer;
	Renderer farRenderer;
	
	RendererTriplet(Renderer nearRenderer, Renderer midRenderer, Renderer farRenderer)
	{
		this.nearRenderer = nearRenderer;
		this.midRenderer = midRenderer;
		this.farRenderer = farRenderer;
	}
	
	/** Simple constructor that sets near-mid-far renderers all to be the passed in one */
	RendererTriplet(Renderer theRenderer)
	{
		this.nearRenderer = theRenderer;
		this.midRenderer = theRenderer;
		this.farRenderer = theRenderer;
	}
	
	Renderer getRenderer(int whichRenderer)
	{
		switch(whichRenderer)
		{
			case NEAR_RENDERER:
				return(nearRenderer);
			case MID_RENDERER:
				return(midRenderer);				
			case FAR_RENDERER:
				return(farRenderer);
			default:
				return(farRenderer);				
		}
	}
	
	void setPickingRenderer(Renderer in)
	{
		pickingRenderer =in;
	}
	
	

}

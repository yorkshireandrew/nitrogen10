package nitrogen1;

/** A class that contains a near,mid and far renderer */
public class RendererTriplet {
	// Enumerations for whichRenderer
	static final int NEAR_RENDERER = 0;
	static final int MID_RENDERER = 1;
	static final int FAR_RENDERER = 2;
	
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
	
	

}

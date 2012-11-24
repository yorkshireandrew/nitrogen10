package nitrogen1;
/** Encapsulates the immutable details about a polygon so that they can be shared across several Item instances */
public class ImmutablePolygon {
	
	/** index of first vertex of polygon */
	int c1;	
	/** index of second vertex of polygon */
	int c2;	
	/** index of third vertex of polygon */
	int c3;	
	/** index of fourth vertex of polygon */
	int c4;
	
	/** Information to pass to the renderer, for example the polygons colour */
	int[] polyData;
	
	/** The renderer triplet to use to render the polygon */
	RendererTriplet rendererTriplet;
	
	/** The texture map */
	TexMap textureMap;
	
	/** The backside index to use to determine which side is being viewed */
	int backsideIndex;
	
	/** True if the polygon uses backside culling */
	boolean isBacksideCulled;
	
	/** True if the polygon is transparent, and gets rendered during a transparent render pass */
	boolean isTransparent;
	
	ImmutablePolygon(
			int c1,
			int c2,
			int c3,
			int c4,
			int[] polyData,
			RendererTriplet rendererTriplet,
			TexMap textureMap,
			int backsideIndex,
			boolean isBacksideCulled,
			boolean isTransparent)
			{
				this.c1=c1;
				this.c2=c2;
				this.c3=c3;
				this.c4=c4;
				this.polyData=polyData;
				this.textureMap=textureMap;
				this.backsideIndex=backsideIndex;
				this.isBacksideCulled=isBacksideCulled;
				this.isTransparent=isTransparent;
			}
	
	/** Selects a Renderer from the Polygons RendererTriplet based on the whichRenderer parameter 
	 * @return The selected renderer*/
	Renderer getRenderer(int whichRenderer)
	{
		return(rendererTriplet.getRenderer(whichRenderer));
	}
}

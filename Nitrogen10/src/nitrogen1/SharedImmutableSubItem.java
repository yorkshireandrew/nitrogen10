package nitrogen1;

//imports to read input files
import java.io.File;
import java.io.FileNotFoundException;
import java.util.NoSuchElementException;
import java.util.Scanner;

// imports for map
import java.util.Map;
import java.util.HashMap;

/** contains the immutable component data of an Item allowing
 * it to be shared with other Items that are identical 
 * apart from their location, orientation and visibility.
 * <br/><br/>
 * Also contains the default values for an Items mutable fields 
 */
public class SharedImmutableSubItem {
	
	static final float hysteresis = (float) 1.02;
	
	/** The radius of a sphere containing the item completely */
	float boundingRadius;
		
	// *************** VALUES RELATING TO RENDERER SELECTION ************
	/** Distance at which to switch from using normal renderer to (slower)near renderer e.g. an interpolating renderer */
	final float nearRendererDist;	
	/** Distance at which to switch from using (slower)near renderer to normal renderer*/
	final float nearRendererDistPlus;
	
	/** Distance at which to switch from using (faster)far renderer to normal renderer */
	final float farRendererDist;	
	/** Distance at which to switch from using normal renderer to (faster)far renderer e.g. fixed colour renderer */
	final float farRendererDistPlus;

	// ***************** VALUES RELATING TO HLP POLYGON BREAKING ****************
	/** Distance at which  (slower) high level of perspective (HLP) breaking is enabled*/
	final float hlpBreakingDist;
	/** Distance at which  (slower) high level of perspective (HLP) breaking is disabled*/	
	final float hlpBreakingDistPlus;
	
	// ***************** VALUES RELATING TO BILLBOARD ORIENTATION DISTANCE ****************
	/** Distance at which (faster) billboard orientation computation is disabled*/
	final float billboardOrientationDist;
	/** Distance at which (faster) billboard orientation computation is enabled*/	
	final float billboardOrientationDistPlus;
	
	// **************** VALUES RELATING TO LEVEL OF DETAIL ***************
	/** Start index of polygons to render at a typical distance */
	final int normalDetailPolyStart;
	/** Start index of polygons to render if the Item is closer than improveDetailDistance */
	final int improvedDetailPolyStart;
	
	/** End index plus one of polygons to render at a typical distance */
	final int normalDetailPolyFinish;
	/** End index plus one of polygons to render if the Item is closer than improveDetailDistance */
	final int improvedDetailPolyFinish;
	
	/** Distance at which to switch to improved detail */
	final float improvedDetailDist;	
	/** Distance at which to switch to normal detail */
	final float improvedDetailDistPlus;
	
	//******************** VALUES RELATED TO MISCILLANIOUS ITEM THINGS *********************
	/** Backside culling should be overridden if the Item collides with the near plane
	 * so the interior of the item is displayed as black */
	boolean nearPlaneCrashBacksideOverride = true;
	
	/** Polygon data */
	final ImmutablePolygon[] immutablePolygons;
	final ImmutableBackside[] immutableBacksides;
	
	/** Vertex Data */
	final ImmutableVertex[] immutableVertexs;
	
	/**
	 * Constructs a SharedImmutableSubItem from a text file
	 * @param filename The name of text file used to initialise the SharedImmutableSubItem
	 * @param renderMap	A Map that contains instances of all the available Renderer classes mapped to a name String */
	SharedImmutableSubItem(final String filename) throws NitrogenCreationException{
		
        System.out.println("loading SISI from " + filename);
        Scanner in = null;
        
        try{
            File f = new File(filename);
            System.out.println(f.getAbsolutePath());
            in = new Scanner(new File(filename));
            int polygonVertexDataMax; 	// number of PolygonVertexData objects the file contains
            Map<String,PolygonVertexData> polygonVertexDataMap = new HashMap<String,PolygonVertexData>();
            int polygonMax; 	// number of ImmutablePolygons the file indicates it contains
            int textureMapMax; 	// number of texture maps the file indicates it references
            Map<String,TexMap> textureMaps = new HashMap<String,TexMap>();
            int backsideMax;	// number of ImmutableBacksides the file indicates it contains       
            int vertexMax; 		// number of ImmutableVertexs
            
            // read bounding radius
        	if(in.hasNextFloat()){boundingRadius 	= in.nextFloat();}else throw new NitrogenCreationException(" unable to find boundingRadius loading " + filename);

        	// read values related to renderer
        	if(in.hasNextFloat()){nearRendererDist 	= in.nextFloat();}else throw new NitrogenCreationException("unable to find nearRendererDist loading " + filename);
        	if(in.hasNextFloat()){farRendererDist 	= in.nextFloat();}else throw new NitrogenCreationException("unable to find farRendererDist loading " + filename);
        	if(in.hasNextFloat()){hlpBreakingDist 	= in.nextFloat();}else throw new NitrogenCreationException("unable to find hlpBreakingDist loading " + filename);
        	if(in.hasNextFloat()){billboardOrientationDist = in.nextFloat();}else throw new NitrogenCreationException("unable to find billboardOrientationDist loading " + filename);

        	// read values related to level of detail
        	if(in.hasNextInt()){normalDetailPolyStart = in.nextInt();}else throw new NitrogenCreationException("unable to find normalDetailPolyStart loading " + filename);
        	if(in.hasNextInt()){improvedDetailPolyStart = in.nextInt();}else throw new NitrogenCreationException("unable to find improvedDetailPolyStart loading " + filename); 
        	if(in.hasNextInt()){normalDetailPolyFinish = in.nextInt();}else throw new NitrogenCreationException("unable to find normalDetailPolyFinish loading " + filename);
        	if(in.hasNextInt()){improvedDetailPolyFinish = in.nextInt();}else throw new NitrogenCreationException("unable to find improvedDetailPolyFinish loading " + filename);
        	if(in.hasNextFloat()){improvedDetailDist 	= in.nextFloat();}else throw new NitrogenCreationException("unable to find improvedDetailDist loading " + filename);
        	
        	//calculate hysteresis distances from read values
    		nearRendererDistPlus = nearRendererDist * hysteresis;
    		farRendererDistPlus = farRendererDist * hysteresis;	
    		hlpBreakingDistPlus = hlpBreakingDist * hysteresis;
    		billboardOrientationDistPlus = billboardOrientationDist * hysteresis;   
    		improvedDetailDistPlus = improvedDetailDist * hysteresis;
    		
    		// load all the TexMap object referenced by the SISI polygons
    		// and place them in the textureMaps map
    		if(in.hasNextInt()){textureMapMax = in.nextInt();}else throw new NitrogenCreationException("unable to find textureMapMax loading" + filename);
			if(in.hasNextLine())in.nextLine(); // pull in line ending
   		
    		for(int i = 0; i < textureMapMax; i++)
    		{
    			String textureMapName;
    			String textureMapResource;
    			
    			TexMap newTextureMap;    			
    			if(!in.hasNextLine())throw new NitrogenCreationException("unable to find textureMap [" + i + "] name loading " + filename);
    			textureMapName = in.nextLine(); // pulls in the text 
    			if(!in.hasNextLine())throw new NitrogenCreationException("unable to find textureMap [" + i + "] resource name loading " + filename);
    			textureMapResource = in.nextLine();
    			
    			try{
    				newTextureMap = new TexMap(textureMapResource);
    				textureMaps.put(textureMapName, newTextureMap);
    			}
    			catch(NitrogenCreationException e){
    				throw new NitrogenCreationException("unable to find textureMap resource " + textureMapResource + " loading " + filename + "   " + e.getMessage());
    			}
    		}
    		
        	// load all the PolgonVertexData
    		String polygonVertexDataName;
        	if(in.hasNextInt()){polygonVertexDataMax = in.nextInt();}else throw new NitrogenCreationException("unable to find polygonVertexDataMax");    		
        	for(int i = 0; i < polygonVertexDataMax; i++)
        	{		
    			if(in.hasNextLine())in.nextLine(); // pull in line ending
        		if(!in.hasNextLine())throw new NitrogenCreationException("unable to find polygonVertexData [" + i + "] name loading " + filename);
    			polygonVertexDataName = in.nextLine();
        		try
        		{
        			polygonVertexDataMap.put(polygonVertexDataName, buildPolygonVertexData(in));
        		}
        		catch(NitrogenCreationException e)
        		{
        			throw new NitrogenCreationException("Exception occured reading " + filename +" on polygonVertexData" + i + ". " + e.getMessage());
        		}
        	} 
        	
    		// load all the ImmutablePolygons
        	if(in.hasNextInt()){polygonMax = in.nextInt();}else throw new NitrogenCreationException("unable to find polygonMax");    		
        	immutablePolygons = new ImmutablePolygon[polygonMax];
        	for(int i = 0; i < polygonMax; i++)
        	{
        		
        		try
        		{
        			immutablePolygons[i] = buildImmutablePolygon(in , textureMaps, polygonVertexDataMap);
        		}
        		catch(NitrogenCreationException e)
        		{
        			throw new NitrogenCreationException("Exception occured reading " + filename +" on ImmutablePolygon " + i + ". " + e.getMessage());
        		}
        	}
        	
        	// load all the ImmutableBacksides
        	if(in.hasNextInt()){backsideMax = in.nextInt();}else throw new NitrogenCreationException("unable to find backsideMax");    		
        	immutableBacksides = new ImmutableBackside[backsideMax];
        	for(int i = 0; i < backsideMax; i++)
        	{
        		
        		try
        		{
        			immutableBacksides[i] = buildImmutableBackside(in);
        		}
        		catch(NitrogenCreationException e)
        		{
        			throw new NitrogenCreationException("Exception occured reading " + filename +" on ImmutableBackside" + i + ". " + e.getMessage());
        		}
        	} 
        	
        	
        	// load all the ImmutableVertexs
        	if(in.hasNextInt()){vertexMax = in.nextInt();}else throw new NitrogenCreationException("unable to find backsideMax");    		
        	immutableVertexs = new ImmutableVertex[vertexMax];
        	for(int i = 0; i < vertexMax; i++)
        	{		
        		try
        		{
        			immutableVertexs[i] = buildImmutableVertex(in);
        		}
        		catch(NitrogenCreationException e)
        		{
        			throw new NitrogenCreationException("Exception occured reading " + filename +" on ImmutableVertex" + i + ". " + e.getMessage());
        		}
        	} 
        }
        catch(NoSuchElementException nsee)
        {
        	throw new NitrogenCreationException("NoSuchElementException reading:" + filename);
        }
        catch(FileNotFoundException fnfe)
        {
        	throw new NitrogenCreationException("FileNotFoundException reading:" + filename);
        }
        finally
        {
            if(in != null) in.close();
        }       	
	}
	
	/** creates an ImmutablePolygon using text from a Scanner, it also requires a textureMap Map created earlier in the parsing so that it can identify and inject into the polygon a TexMap reference */
	ImmutablePolygon buildImmutablePolygon(Scanner in , Map<String, TexMap> textureMaps, Map<String, PolygonVertexData> polygonVertexDataMap) throws NitrogenCreationException, NoSuchElementException
	{
			int 			temp_c1;	
			int 			temp_c2;	
			int 			temp_c3;	
			int 			temp_c4;
			int[] 			temp_polyData;
			RendererTriplet temp_rendererTriplet;
			String			textureMapName;
			String			polygonVertexDataName;
			String			rendererTripletName;
			TexMap 			temp_textureMap = null;
			
			PolygonVertexData temp_pvd_c1 = null;
			PolygonVertexData temp_pvd_c2 = null;
			PolygonVertexData temp_pvd_c3 = null;
			PolygonVertexData temp_pvd_c4 = null;
			
			int 			temp_backsideIndex;
			String			yes_no;
			boolean 		temp_isBacksideCulled;
			boolean 		temp_isTransparent;
			
			if(in.hasNextInt()){temp_c1 = in.nextInt();}else throw new NitrogenCreationException("Unable to find c1.");
			if(in.hasNextInt()){temp_c2 = in.nextInt();}else throw new NitrogenCreationException("Unable to find c2.");
			if(in.hasNextInt()){temp_c3 = in.nextInt();}else throw new NitrogenCreationException("Unable to find c3.");
			if(in.hasNextInt()){temp_c4 = in.nextInt();}else throw new NitrogenCreationException("Unable to find c4.");

			// read in the polygonVertexData associated with c1
			if(in.hasNextLine())in.nextLine(); // pull in line ending
			if(!in.hasNextLine())throw new NitrogenCreationException("Unable to find polygonVertexData name associated with c1");
			polygonVertexDataName = in.nextLine();
		    if(polygonVertexDataMap.containsKey(polygonVertexDataName)){temp_pvd_c1 = polygonVertexDataMap.get(polygonVertexDataName);}
			else throw new NitrogenCreationException("The PolygonVertexData named " + polygonVertexDataName + "is not loaded by the file.");

		    // read in the polygonVertexData associated with c2
			if(!in.hasNextLine())throw new NitrogenCreationException("Unable to find polygonVertexData name associated with c2");
			polygonVertexDataName = in.nextLine();
		    if(polygonVertexDataMap.containsKey(polygonVertexDataName)){temp_pvd_c2 = polygonVertexDataMap.get(polygonVertexDataName);}
			else throw new NitrogenCreationException("The PolygonVertexData named " + polygonVertexDataName + "is not loaded by the file.");

			// read in the polygonVertexData associated with c3
			if(!in.hasNextLine())throw new NitrogenCreationException("Unable to find polygonVertexData name associated with c3");
			polygonVertexDataName = in.nextLine();
		    if(polygonVertexDataMap.containsKey(polygonVertexDataName)){temp_pvd_c3 = polygonVertexDataMap.get(polygonVertexDataName);}
			else throw new NitrogenCreationException("The PolygonVertexData named " + polygonVertexDataName + "is not loaded by the file.");

			// read in the polygonVertexData associated with c4
			if(!in.hasNextLine())throw new NitrogenCreationException("Unable to find polygonVertexData name associated with c4");
			polygonVertexDataName = in.nextLine();
		    if(polygonVertexDataMap.containsKey(polygonVertexDataName)){temp_pvd_c4 = polygonVertexDataMap.get(polygonVertexDataName);}
			else throw new NitrogenCreationException("The PolygonVertexData named " + polygonVertexDataName + "is not loaded by the file.");
		    
			
			// read in the polygons polyData
			int polyDataMax;
			if(in.hasNextInt()){polyDataMax = in.nextInt();}else throw new NitrogenCreationException("Unable to find polyDataMax.");
			temp_polyData = new int[polyDataMax];
			for(int j = 0; j < polyDataMax; j++)
			{
				if(in.hasNextInt()){temp_polyData[j] = in.nextInt();}else throw new NitrogenCreationException("Problem reading polyData.");
			}
			
			// obtain renderer triplet
			if(in.hasNextLine())in.nextLine(); // pull in line ending
			if(!in.hasNextLine())throw new NitrogenCreationException("Unable to find RenderTriplet name.");
			rendererTripletName = in.nextLine();
			// The Exception that getRenderTriplet() may throw is suitably informative
			try{
				temp_rendererTriplet = RendererHelper.getRendererTriplet(rendererTripletName);
			}
			catch(Exception e)
			{
				throw new NitrogenCreationException("Unable to find RenderTriplet named " + rendererTripletName );
			}

			// obtain the texture map
			if(!in.hasNextLine())throw new NitrogenCreationException("Unable to find TexMap name, should be a line containing \"null\" if no texture map is used.");
			textureMapName = in.nextLine();
			if(!textureMapName.equals("null"))
			{
				if(textureMaps.containsKey(textureMapName)){temp_textureMap = textureMaps.get(textureMapName);}
				else throw new NitrogenCreationException("The TexMap named " + textureMapName + "is not loaded by the file.");
			}
			
			// obtain the backside index			
			if(in.hasNextInt()){temp_backsideIndex = in.nextInt();}else throw new NitrogenCreationException("Unable to find backsideIndex.");
				
			// obtain the isBacksideCulled
			if(in.hasNextLine())in.nextLine(); // pull in line ending
			if(!in.hasNextLine())throw new NitrogenCreationException("Unable to find isBacksideCulled, should be \"yes\" or \"no\".");
			yes_no = in.nextLine();
			if((yes_no.equals("yes"))||(yes_no.equals("no")))
			{
				if(yes_no.equals("yes")){temp_isBacksideCulled = true;}
				else{temp_isBacksideCulled = false;}
			}
			else throw new NitrogenCreationException("Unable to find isBacksideCulled " + yes_no + "found where \"yes\" or \"no\" expected.");
			
			// obtain isTransparent
			if(!in.hasNextLine())throw new NitrogenCreationException("Unable to find isTransparent, should be \"yes\" or \"no\".");
			yes_no = in.nextLine();
			if((yes_no.equals("yes"))||(yes_no.equals("no")))
			{
				if(yes_no.equals("yes")){temp_isTransparent = true;}
				else{temp_isTransparent = false;}
			}
			else throw new NitrogenCreationException("Unable to find isTransparent " + yes_no + "found where \"yes\" or \"no\" expected.");
	
			return new ImmutablePolygon(
					temp_c1,
					temp_c2,
					temp_c3,
					temp_c4,
					temp_pvd_c1,
					temp_pvd_c2,
					temp_pvd_c3,
					temp_pvd_c4,
					temp_polyData,
					temp_rendererTriplet,
					temp_textureMap,
					temp_backsideIndex,
					temp_isBacksideCulled,
					temp_isTransparent
			);
      	}
	
	/** Creates an ImmutableBackside using text from a scanner */ 
	ImmutableBackside buildImmutableBackside(Scanner in) throws NitrogenCreationException
	{
		float temp_ix;
		float temp_iy;
		float temp_iz;
		float temp_inx;
		float temp_iny;
		float temp_inz;
		String yes_no;
		boolean temp_calculateLighting;
		
		if(in.hasNextFloat()){temp_ix = in.nextFloat();}else throw new NitrogenCreationException("Unable to find ix");
		if(in.hasNextFloat()){temp_iy = in.nextFloat();}else throw new NitrogenCreationException("Unable to find iy");
		if(in.hasNextFloat()){temp_iz = in.nextFloat();}else throw new NitrogenCreationException("Unable to find iz");
		
		if(in.hasNextFloat()){temp_inx = in.nextFloat();}else throw new NitrogenCreationException("Unable to find inx");
		if(in.hasNextFloat()){temp_iny = in.nextFloat();}else throw new NitrogenCreationException("Unable to find iny");
		if(in.hasNextFloat()){temp_inz = in.nextFloat();}else throw new NitrogenCreationException("Unable to find inz");

		// obtain calculateLighting
		if(!in.hasNextLine())throw new NitrogenCreationException("Unable to find calculateLighting, should be \"yes\" or \"no\".");
		yes_no = in.nextLine();
		if((yes_no.equals("yes"))||(yes_no.equals("no")))
		{
			if(yes_no.equals("yes")){temp_calculateLighting = true;}
			else{temp_calculateLighting = false;}
		}
		else throw new NitrogenCreationException("Unable to find calculateLighting " + yes_no + "found where \"yes\" or \"no\" expected.");
		
		return new ImmutableBackside(
				temp_ix,
				temp_iy,
				temp_iz,
				temp_inx,
				temp_iny,
				temp_inz,
				temp_calculateLighting
				);
		
	}
	
	/** Creates an ImmutableVertex using text from a scanner */ 
	ImmutableVertex buildImmutableVertex(Scanner in) throws NitrogenCreationException
	{
		float temp_is_x;
		float temp_is_y;
		float temp_is_z;
		
		if(in.hasNextFloat()){temp_is_x = in.nextFloat();}else throw new NitrogenCreationException("Unable to find is_x");
		if(in.hasNextFloat()){temp_is_y = in.nextFloat();}else throw new NitrogenCreationException("Unable to find is_iy");
		if(in.hasNextFloat()){temp_is_z = in.nextFloat();}else throw new NitrogenCreationException("Unable to find is_iz");

		return new ImmutableVertex(
				temp_is_x,
				temp_is_y,
				temp_is_z
				);		
	}
	
	PolygonVertexData buildPolygonVertexData(Scanner in) throws NitrogenCreationException
	{	
	    float temp_aux1;
	    float temp_aux2;
	    float temp_aux3;
		if(in.hasNextFloat()){temp_aux1 = in.nextFloat();}else throw new NitrogenCreationException("Unable to find aux1");
		if(in.hasNextFloat()){temp_aux2 = in.nextFloat();}else throw new NitrogenCreationException("Unable to find aux2");
		if(in.hasNextFloat()){temp_aux3 = in.nextFloat();}else throw new NitrogenCreationException("Unable to find aux3");
		return (new PolygonVertexData(temp_aux1,temp_aux2,temp_aux3));
	}
}

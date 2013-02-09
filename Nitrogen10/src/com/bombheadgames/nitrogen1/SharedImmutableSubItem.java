package com.bombheadgames.nitrogen1;

//imports to read input files
import java.io.File;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
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
public class SharedImmutableSubItem implements Serializable{
	private static final long serialVersionUID = 4481701769213207490L;

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
	
	/** Distance at which to stop rendering */
	final float farPlane;

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
	final ImmutableVertex[] immutableVertexes;
	
	/** Collision Data */
	final boolean hasCollisionVertexes;
	final ImmutableCollisionVertex[] immutableCollisionVertexes;
	
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
            
            /** maps polygonVertexDataName */
            Map<String,PolygonVertexData> polygonVertexDataMap = new HashMap<String,PolygonVertexData>();
            /** maps polyDataName */
            Map<String,int[]> polygonDataMap = new HashMap<String,int[]>();
            /** maps textureMapName */
            Map<String,TexMap> textureMapMap = new HashMap<String,TexMap>(); 
            /** maps for backsideName */
            List<String> immutableBacksideList = new ArrayList<String>(); 
                
            /** number of PolygonVertexData entries the file contains */
            int polygonVertexDataMax; 
            /** number of PolygonData entries the file contains */
            int polygonDataMax;
            /** number of texture map entries the file contains */
            int textureMapMax;
            /** number of ImmutableBackside entries the file contains */
            int backsideMax;
            /** number of ImmutablePolygons */
            int polygonMax;            
            /** number of ImmutableVertex entries the file contains */
            int vertexMax; 
            /** number of ImmutableCollisionVertex entries the file contains */
            int collisionVertexMax;          
            
            // read bounding radius
        	boundingRadius 	= readFloat(in, " unable to find boundingRadius loading " + filename);

        	// read values related to renderer
        	nearRendererDist = readFloat(in, "unable to find nearRendererDist loading " + filename);
        	farRendererDist  = readFloat(in, "unable to find farRendererDist loading " + filename);
        	hlpBreakingDist  = readFloat(in, "unable to find hlpBreakingDist loading " + filename);
        	billboardOrientationDist = readFloat(in, "unable to find billboardOrientationDist loading " + filename);
        	farPlane = readFloat(in, "unable to find farPlane loading " + filename);

        	// read values related to level of detail
        	normalDetailPolyStart 	= readInt(in, "unable to find normalDetailPolyStart loading " + filename);
        	improvedDetailPolyStart = readInt(in, "unable to find improvedDetailPolyStart loading " + filename); 
        	normalDetailPolyFinish 	= readInt(in, "unable to find normalDetailPolyFinish loading " + filename);
        	improvedDetailPolyFinish = readInt(in, "unable to find improvedDetailPolyFinish loading " + filename);
        	improvedDetailDist = readFloat(in, "unable to find improvedDetailDist loading " + filename);
        	
        	//calculate hysteresis distances from read values
    		nearRendererDistPlus = nearRendererDist * hysteresis;
    		farRendererDistPlus = farRendererDist * hysteresis;	
    		hlpBreakingDistPlus = hlpBreakingDist * hysteresis;
    		billboardOrientationDistPlus = billboardOrientationDist * hysteresis;   
    		improvedDetailDistPlus = improvedDetailDist * hysteresis;
    		
        	// load all the PolgonVertexData referenced by the SISI polygons
    		System.out.println("now reading polgonVertexData");
    		String polygonVertexDataName;
        	polygonVertexDataMax = readInt(in, "unable to find polygonVertexDataMax loading " + filename);    		
        	for(int i = 0; i < polygonVertexDataMax; i++)
        	{		
        		polygonVertexDataName = readLine(in, "unable to find polygonVertexData [" + i + "] name loading " + filename);
        		try
        		{
        			polygonVertexDataMap.put(polygonVertexDataName, buildPolygonVertexData(in));
        		}
        		catch(NitrogenCreationException e)
        		{
        			throw new NitrogenCreationException("Exception occured reading " + filename +" on polygonVertexData " + polygonVertexDataName + " caused by: " + e.getMessage());
        		}
        	}
        	
        	System.out.println("now reading polyData");
        	// load all the polygonData referenced by the SISI polygons
    		String polygonDataName;
        	polygonDataMax = readInt(in, "unable to find polygonDataMax loading " + filename);    		
        	for(int i = 0; i < polygonDataMax; i++)
        	{		
        		polygonDataName = readLine(in, "unable to find polygonData [" + i + "] name loading " + filename);
        		try
        		{
        			polygonDataMap.put(polygonDataName, buildPolygonData(in));
        		}
        		catch(NitrogenCreationException e)
        		{
        			throw new NitrogenCreationException("Exception occured reading " + filename +" on polygonData " + polygonDataName + " caused by: " + e.getMessage());
        		}
        	}   
    		
    		// load all the TexMap object referenced by the SISI polygons
    		// and place them in the textureMaps map
    		textureMapMax = readInt(in, "unable to find textureMapMax loading" + filename);		
    		for(int i = 0; i < textureMapMax; i++)
    		{
    			String textureMapName;
    			String textureMapResource;
    			
    			TexMap newTextureMap;    			
    			textureMapName = readLine(in, "unable to find textureMap [" + i + "] name loading " + filename);
    			textureMapResource = readLine(in, "unable to find textureMap " + textureMapName + " resource name loading " + filename);
    			
    			try{
    				newTextureMap = TexMap.getTexture(textureMapResource);
    				textureMapMap.put(textureMapName, newTextureMap);
    			}
    			catch(NitrogenCreationException e){
    				throw new NitrogenCreationException("unable to find textureMap resource " + textureMapResource + " loading " + filename + "   " + e.getMessage());
    			}
    		}
    		
        	// load all the ImmutableBacksides referenced by the SISI polygons
    		String immutableBacksideName;
        	backsideMax = readInt( in, "unable to find backsideMax loading " + filename);    		
        	immutableBacksides = new ImmutableBackside[backsideMax];
        	for(int i = 0; i < backsideMax; i++)
        	{
        		
        		try
        		{
        			immutableBacksideName = readLine(in, "unable to find mmutableBacksideName [" + i + "] loading " + filename);
        			immutableBacksides[i] = buildImmutableBackside(in);
        			immutableBacksideList.add(immutableBacksideName);
        		}
        		catch(NitrogenCreationException e)
        		{
        			throw new NitrogenCreationException("Exception occured reading " + filename +" on ImmutableBackside" + i + " caused by: " + e.getMessage());
        		}
        	} 
      	
    		// load all the ImmutablePolygons
        	polygonMax = readInt(in, "unable to find polygonMax reading " + filename);    		
        	immutablePolygons = new ImmutablePolygon[polygonMax];
        	for(int i = 0; i < polygonMax; i++)
        	{
        		
        		try
        		{
        			immutablePolygons[i] = buildImmutablePolygon(in , polygonVertexDataMap, polygonDataMap, textureMapMap, immutableBacksideList);
        		}
        		catch(NitrogenCreationException e)
        		{
        			throw new NitrogenCreationException("Exception occured reading " + filename +" on ImmutablePolygon " + i + " caused by: " + e.getMessage());
        		}
        	}
        	

        	
        	
        	// load all the ImmutableVertexes
        	vertexMax = readInt( in, "unable to find vertexMax loading " + filename);    		
        	immutableVertexes = new ImmutableVertex[vertexMax];
        	for(int i = 0; i < vertexMax; i++)
        	{		
        		try
        		{
        			immutableVertexes[i] = buildImmutableVertex(in);
        		}
        		catch(NitrogenCreationException e)
        		{
        			throw new NitrogenCreationException("Exception occured reading " + filename +" on ImmutableVertex" + i + " caused by: " + e.getMessage());
        		}
        	}
        	
        	// load all the ImmutableCollisionVertexs
        	collisionVertexMax = readInt( in, "unable to find collisionVertexMax loading " + filename);    		
        	if(collisionVertexMax > 0)
        	{
        		hasCollisionVertexes = true;
            	immutableCollisionVertexes = new ImmutableCollisionVertex[vertexMax];
            	for(int i = 0; i < vertexMax; i++)
            	{		
            		try
            		{
            			immutableCollisionVertexes[i] = buildImmutableCollisionVert(in);
            		}
            		catch(NitrogenCreationException e)
            		{
            			throw new NitrogenCreationException("Exception occured reading " + filename +" on ImmutableVertex" + i + " caused by: " + e.getMessage());
            		}
            	}           
        	}
        	else
        	{ 
        		hasCollisionVertexes = false;
        		immutableCollisionVertexes = new ImmutableCollisionVertex[0];
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
	
	// buildImmutablePolygon(in , polygonVertexDataMap, polygonDataMap, textureMapMap, immutableBacksideList);
	/** creates an ImmutablePolygon using text from a Scanner, it also requires a textureMap Map created earlier in the parsing so that it can identify and inject into the polygon a TexMap reference */
	ImmutablePolygon buildImmutablePolygon(
											Scanner in,
											Map<String,PolygonVertexData> polygonVertexDataMap,
											Map<String,int[]> polygonDataMap,											
											Map<String, TexMap> textureMapMap,
											List<String> immutableBacksideList
										) throws NitrogenCreationException, NoSuchElementException
	{
			// variables used to build the polygon
			int 				temp_c1;	
			int 				temp_c2;	
			int 				temp_c3;	
			int 				temp_c4;
			PolygonVertexData 	temp_pvd_c1 = null;
			PolygonVertexData 	temp_pvd_c2 = null;
			PolygonVertexData 	temp_pvd_c3 = null;
			PolygonVertexData 	temp_pvd_c4 = null;			
			int[] 				temp_polygonData;
			RendererTriplet 	temp_rendererTriplet;
			TexMap 				temp_textureMap = null;	
			int 				temp_backsideIndex;
			boolean 			temp_isBacksideCulled;
			boolean 			temp_isTransparent;			
			
			// strings used to reference things in the passed in Map and List Objects
			String			polygonVertexDataName;
			String 			polygonDataName;
			String			rendererTripletName;			
			String			textureMapName;
			String			backsideName;		
						
			// read in the indexes to the Vertexes
			temp_c1 = readInt( in, "Unable to find c1.");
			temp_c2 = readInt( in, "Unable to find c2.");
			temp_c3 = readInt( in, "Unable to find c3.");
			temp_c4 = readInt( in, "Unable to find c4.");

			// read in the polygonVertexData associated with c1
			polygonVertexDataName = readLine(in, "Unable to find polygonVertexData name associated with c1");
		    if(polygonVertexDataMap.containsKey(polygonVertexDataName)){temp_pvd_c1 = polygonVertexDataMap.get(polygonVertexDataName);}
			else throw new NitrogenCreationException("The PolygonVertexData named " + polygonVertexDataName + "is not loaded by the file.");

			// read in the polygonVertexData associated with c2
			polygonVertexDataName = readLine(in, "Unable to find polygonVertexData name associated with c1");
		    if(polygonVertexDataMap.containsKey(polygonVertexDataName)){temp_pvd_c2 = polygonVertexDataMap.get(polygonVertexDataName);}
			else throw new NitrogenCreationException("The PolygonVertexData named " + polygonVertexDataName + "is not loaded by the file.");

		    // read in the polygonVertexData associated with c3
			polygonVertexDataName = readLine(in, "Unable to find polygonVertexData name associated with c1");
		    if(polygonVertexDataMap.containsKey(polygonVertexDataName)){temp_pvd_c3 = polygonVertexDataMap.get(polygonVertexDataName);}
			else throw new NitrogenCreationException("The PolygonVertexData named " + polygonVertexDataName + "is not loaded by the file.");

		    // read in the polygonVertexData associated with c4
			polygonVertexDataName = readLine(in, "Unable to find polygonVertexData name associated with c1");
		    if(polygonVertexDataMap.containsKey(polygonVertexDataName)){temp_pvd_c4 = polygonVertexDataMap.get(polygonVertexDataName);}
			else throw new NitrogenCreationException("The PolygonVertexData named " + polygonVertexDataName + "is not loaded by the file.");
		
		    // read in the polygonData 
			polygonDataName = readLine(in, "Unable to find polygonData name");
		    if(polygonDataMap.containsKey(polygonDataName)){temp_polygonData = polygonDataMap.get(polygonDataName);}
			else throw new NitrogenCreationException("The PolygonData named " + polygonDataName + "is not loaded by the file.");
				
			// obtain renderer triplet
			rendererTripletName = readLine( in, "Unable to find RenderTriplet name.");
			
			// The Exception that getRenderTriplet() may throw is suitably informative
			try{
				temp_rendererTriplet = RendererHelper.getRendererTriplet(rendererTripletName);
			}
			catch(Exception e)
			{
				throw new NitrogenCreationException("Unable to find a RenderTriplet named " + rendererTripletName + "in RendererHelper" );
			}

			// obtain the texture map
			textureMapName = readLine( in, "Unable to find TexMap name, should be a line containing \"null\" if no texture map is used.");
			if(!textureMapName.equals("null"))
			{
				if(textureMapMap.containsKey(textureMapName)){temp_textureMap = textureMapMap.get(textureMapName);}
				else throw new NitrogenCreationException("The TexMap named " + textureMapName + "is not loaded by the file.");
			}
			
			backsideName = readLine( in, "Unable to find ImmutableBackside name");
		    if(immutableBacksideList.contains(backsideName))
		    {
		    	temp_backsideIndex = immutableBacksideList.indexOf(backsideName);
		    }
		    else
		    {
				throw new NitrogenCreationException("ImmutableBackside " + backsideName + " is not loaded by the file");
		    }

			// obtain the isBacksideCulled
		    temp_isBacksideCulled = readBoolean( in, "Unable to find isBacksideCulled ");

			// obtain isTransparent
		    temp_isTransparent = readBoolean( in, "Unable to find isTransparent");
	
			return new ImmutablePolygon(
					temp_c1,
					temp_c2,
					temp_c3,
					temp_c4,
					temp_pvd_c1,
					temp_pvd_c2,
					temp_pvd_c3,
					temp_pvd_c4,
					temp_polygonData,
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
		boolean temp_calculateLighting;
		
		temp_ix = readFloat(in, "Unable to find ix");
		temp_iy = readFloat(in, "Unable to find iy");
		temp_iz = readFloat(in, "Unable to find iz");
		
		temp_inx = readFloat(in, "Unable to find inx");
		temp_iny = readFloat(in, "Unable to find iny");
		temp_inz = readFloat(in, "Unable to find inz");

		temp_calculateLighting = readBoolean(in, "Unable to find calculateLighting");

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
		
		temp_is_x = readFloat(in, "Unable to find is_x");
		temp_is_y = readFloat(in, "Unable to find is_y");
		temp_is_z = readFloat(in, "Unable to find is_z");

		return new ImmutableVertex(
				temp_is_x,
				temp_is_y,
				temp_is_z
				);		
	}
	
	/** Creates an ImmutableVertex using text from a scanner */ 
	ImmutableCollisionVertex buildImmutableCollisionVert(Scanner in) throws NitrogenCreationException
	{
		float temp_is_x;
		float temp_is_y;
		float temp_is_z;
		float temp_radius;
		
		
		temp_is_x = readFloat(in, "Unable to find is_x");
		temp_is_y = readFloat(in, "Unable to find is_y");
		temp_is_z = readFloat(in, "Unable to find is_z");
		temp_radius = readFloat(in, "Unable to radius");

		return new ImmutableCollisionVertex(
				temp_is_x,
				temp_is_y,
				temp_is_z,
				temp_radius
				);		
	}
	
	PolygonVertexData buildPolygonVertexData(Scanner in) throws NitrogenCreationException
	{	
	    float temp_aux1;
	    float temp_aux2;
	    float temp_aux3;
		temp_aux1 = readFloat(in, "Unable to find aux1");
		temp_aux2 = readFloat(in, "Unable to find aux2");
		temp_aux3 = readFloat(in, "Unable to find aux3");
		return (new PolygonVertexData(temp_aux1,temp_aux2,temp_aux3));
	}
	
	int[] buildPolygonData(Scanner in) throws NitrogenCreationException
	{	
	    int polyDataMax = readInt(in,"Unable to read polyDataMax");
	    int[] retval = new int[polyDataMax];
	    for(int x = 0; x < polyDataMax; x++)retval[x] = readArkwardInt(in,"Unable to read polyData index=[" + x + "]"); 
	    return retval;
	}	
	
	/** reads the next line from the scanner or throws a NitrogenCreationException containing the exception text if not found. skips over if its a line ending */
	final static String readLine(Scanner in, String exceptionText)throws NitrogenCreationException
	{
		String retval;
		do{
			if(in.hasNextLine()){retval = in.nextLine();}
			else throw new NitrogenCreationException(exceptionText);
		}while(retval.isEmpty());
		return retval;	
	}

	/** can deal with big integers such as 0XFFFFFFFF, but must be on its own line */
	final static int readArkwardInt(Scanner in, String exceptionText)throws NitrogenCreationException
	{
		String line = readLine(in, exceptionText);
		int i;
		try{
			i = Integer.decode(line);
			return i;
		}
		catch(NumberFormatException e){ throw new NitrogenCreationException(exceptionText + " - Number Format Exception");}
	}
	final static int readInt(Scanner in, String exceptionText)throws NitrogenCreationException
	{
		if(in.hasNextInt()){return in.nextInt();}
		else throw new NitrogenCreationException(exceptionText);
	}
	
	final static float readFloat(Scanner in, String exceptionText)throws NitrogenCreationException
	{
		if(in.hasNextFloat()){return in.nextFloat();}
		else throw new NitrogenCreationException(exceptionText);
	}

	final static boolean readBoolean(Scanner in, String exceptionText)throws NitrogenCreationException
	{
		String temp = readLine(in,exceptionText);
		if(temp.equals("yes")||temp.equals("YES")||temp.equals("true")||temp.equals("TRUE"))return true;
		if(temp.equals("no")||temp.equals("NO")||temp.equals("false")||temp.equals("FALSE"))return false;
		throw new NitrogenCreationException(exceptionText + " caused by " + temp + " not equating to a boolean");
	}
}

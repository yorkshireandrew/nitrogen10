package nitrogen1;

/** default class that implements the factory methods using the new operator */
final public class ItemFactory_Caching implements ItemFactory{
	
	// create the single instance that exists as long as the JVM runs
	// private static ItemFactory_Caching itemFactoryCachingSingleton = new ItemFactory_Caching(); 
	
	private int maxFreeItems = 0;
	private int maxFreeBacksides = 0;
	private int maxFreeVertexes = 0;
	
	private int currentFreeItems = 0;
	private final Item itemHead = new Item();
	private Item itemTail = itemHead;
	
	private int currentFreeBacksides = 0;	
	private final Backside backsideHead = new Backside();
	private Backside backsideTail = backsideHead;
	
	private int currentFreeVertexes = 0;		
	private final Vert vertHead = new Vert();
	private Vert vertTail = vertHead;	
	
	
	/** generates an Item either from the preallocated pool or constructs a new one */ 
	final public Item getItem(final SharedImmutableSubItem in_sisi, final Transform t)
	{
		Item retval;
		if(currentFreeItems > 0)
		{
			Item available = itemHead.nextInList;
			itemHead.nextInList = available.nextInList;
			retval = available;
			retval.initializeItem(in_sisi, t);
			currentFreeItems--;
			return retval;
		}
		else
		{
			// otherwise allocate from heap
			return new Item(in_sisi, t);
		}
	}
	
	/** generates a Backside either from the preallocated pool or constructs a new one */	
	final public Backside getBackside(final ImmutableBackside ib)
	{
		Backside retval;
		if(currentFreeBacksides > 0)
		{
			Backside available = backsideHead.nextInList;
			backsideHead.nextInList  = available.nextInList;
			retval = available;
			retval.initializeBackside(ib);
			currentFreeBacksides--;
			return retval;
		}
		else
		{
			// otherwise allocate from heap
			return new Backside(ib);
		}
	}

	/** generates a Vertex either from the preallocated pool or constructs a new one */	
	final public Vert getVertex(final ImmutableVertex iv)
	{
		Vert retval;
		if(currentFreeVertexes > 0)
		{
			Vert available = vertHead.nextInList;
			vertHead.nextInList  = available.nextInList;
			retval = available;
			retval.initializeVertex(iv);
			currentFreeVertexes--;
			return retval;
		}
		else
		{
			// otherwise allocate from heap
			return new Vert(iv);
		}
	}

	/** generates a (collision) Vertex either from the preallocated pool or constructs a new one */		
	final public Vert getVertex(final ImmutableCollisionVert icv)
	{
		Vert retval;
		Vert available = vertHead.nextInList;
		if(currentFreeVertexes > 0)
		{
			retval = available;
			retval.initializeVertex(icv);
			vertHead.nextInList = available.nextInList;
			currentFreeVertexes--;
			return retval;
		}
		else
		{
			// otherwise allocate from heap
			return new Vert(icv);
		}
	}

	/** returns the number of Items waiting to be allocated */
	final public int getFreeItemCount()
	{
		return(currentFreeItems);	
	}
	
	/** returns the number of Backsides waiting to be allocated */
	final public int getFreeBacksideCount()
	{
		return(currentFreeBacksides);	
	}
	
	/** returns the number of Vertexes waiting to be allocated */
	final public int getFreeVertexCount()
	{
		return(currentFreeVertexes);	
	}
	
	/** Alters the size of the Item pool */
	final public void setFreeItems(final int free)
	{
		if(free > currentFreeItems)
		{
			// work out how much to add
			int toAdd = free - currentFreeItems;
			
			// add to free
			for(int x = 0; x < toAdd; x++)
			{
				Item tail = itemTail;
				Item newItem = new Item();
				tail.nextInList = newItem;
				itemTail = newItem;
			}			
			// update free
			currentFreeItems = free;
			return;
		}

		if(free < currentFreeItems)
		{
			// traverse in
			Item cursor = itemHead;
			for(int x = 0; x < free; x++)
			{
				cursor = cursor.nextInList;
			}
			
			// chop LLL
			itemTail = cursor;
			cursor.nextInList = null;
			
			// update free
			currentFreeItems = free;
			return;
		}
	}	
	
	/** Alters the size of the Backside pool */
	final public void setFreeBacksides(final int free)
	{	
		if(free > currentFreeBacksides)
		{
			// work out how much to add
			int toAdd = free - currentFreeBacksides;
			
			// add to free
			for(int x = 0; x < toAdd; x++)
			{
				Backside tail = backsideTail;
				Backside newBackside = new Backside();
				tail.nextInList = newBackside;
				backsideTail = newBackside;
			}			
			// update free
			currentFreeBacksides = free;
			return;
		}

		if(free < currentFreeBacksides)
		{
			// traverse in
			Backside cursor = backsideHead;
			for(int x = 0; x < free; x++)
			{
				cursor = cursor.nextInList;
			}
			
			// chop LLL
			backsideTail = cursor;
			cursor.nextInList = null;
			
			// update free
			currentFreeBacksides = free;
			return;
		}
	}

	/** Alters the size of the pool of preallocated Vertexes, adjusting maxFreeItems */
	final public void setFreeVertexes(final int free)
	{	
		if(free > currentFreeVertexes)
		{
			// work out how much to add
			int toAdd = free - currentFreeVertexes;
			
			// add to free
			for(int x = 0; x < toAdd; x++)
			{
				Vert tail = vertTail;
				Vert newVertex = new Vert();
				tail.nextInList = newVertex;
				vertTail = newVertex;
			}			
			// update free
			currentFreeVertexes = free;
			return;
		}

		if(free < currentFreeVertexes)
		{
			// traverse in
			Vert cursor = vertHead;
			for(int x = 0; x < free; x++)
			{
				cursor = cursor.nextInList;
			}
			
			// chop LLL
			vertTail = cursor;
			cursor.nextInList = null;
			
			// update free
			currentFreeVertexes = free;
			return;
		}
	};
	
	/** Trims the size of the pools to what is currently already allocated */
	final public void trimToSize()
	{
		setFreeItems(0);
		setFreeBacksides(0);
		setFreeVertexes(0);	
	}

	/** returns the maximum size of the Item pool */
	final public  int getMaxFreeItems() 
	{
		return maxFreeItems;
	}
	
	/** sets the maximum size of the Item pool can grow too */	
	final public void setMaxFreeItems(final int maxFreeItems)
	{
		this.maxFreeItems = maxFreeItems;
		if(getFreeItemCount() > maxFreeItems) setFreeItems(maxFreeItems);
	}
		
	
	/** returns the maximum size of the Backside pool */	
	final public int getMaxFreeBacksides() 
	{
		return maxFreeBacksides;
	}

	/** sets the max size the Backside pool can grow too */	
	final public void setMaxFreeBacksides(final int maxFreeBackside) {
		this.maxFreeBacksides = maxFreeBackside;
		if(getFreeBacksideCount() > maxFreeBackside)setFreeBacksides(maxFreeBackside);
	}

	/** returns the maximum size of the Vertex pool */
	final public int getMaxFreeVertexes() {
		return maxFreeVertexes;
	}

	/** sets the max size the Vertex pool can grow too */	
	final public void setMaxFreeVertexes(final int maxFreeVertexes) {
		this.maxFreeVertexes = maxFreeVertexes;
		if(getFreeVertexCount() > maxFreeVertexes) setFreeVertexes(maxFreeVertexes);
	}
	
	final public void recycle(final Item item)
	{
		// recycle Item
		if(currentFreeItems < maxFreeItems)
		{
			Item tail = itemTail;
			tail.nextInList = item;
			itemTail = item;
			item.nextInList = null;
			currentFreeItems++;
		}
		
		// recycle Backsides
		Backside[] scrapBacksides = item.getBacksides();
		Backside backsideTailL = backsideTail;
		Backside scrapBackside;
		int maxFreeBacksidesL = maxFreeBacksides;
		int scrapBacksidesLength = scrapBacksides.length;
		int currentFreeBacksidesL = currentFreeBacksides;
		for(int x = 0; x < scrapBacksidesLength ; x++)
		{
			if(currentFreeBacksidesL >= maxFreeBacksidesL)break;
			scrapBackside = scrapBacksides[x];
			backsideTailL.nextInList = scrapBackside;
			backsideTailL = scrapBackside;
			currentFreeBacksidesL++;
		}
		backsideTailL.nextInList = null;
		backsideTail = backsideTailL;
		currentFreeBacksides = currentFreeBacksidesL;
		
		// recycle Vertexes
		Vert[] scrapVertexes = item.getVertexes();
		Vert vertexTailL = vertTail;
		Vert scrapVert;
		int maxFreeVertexL = maxFreeVertexes;
		int scrapVertexLength = scrapVertexes.length;
		int currentFreeVertexesL = currentFreeVertexes;
		for(int x = 0; x < scrapVertexLength ; x++)
		{
			if(currentFreeVertexesL >= maxFreeVertexL)break;
			scrapVert = scrapVertexes[x];
			vertexTailL.nextInList = scrapVert;
			vertexTailL = scrapVert;
			currentFreeVertexesL++;
		}
		vertexTailL.nextInList = null;
		vertTail = vertexTailL;
		currentFreeVertexes = currentFreeVertexesL;
		
		
		// recycle collision Vertexes
		Vert[] scrapCollisionVertexes = item.getCollisionVertexes();
		Vert vertexTailL2 = vertTail;
		Vert scrapCollisionVert;
		int maxFreeVertexL2 = maxFreeVertexes;
		int scrapCollisionVertexLength = scrapCollisionVertexes.length;
		int currentFreeVertexesL2 = currentFreeVertexes;
		for(int x = 0; x < scrapCollisionVertexLength ; x++)
		{
			if(currentFreeVertexesL2 >= maxFreeVertexL2)break;
			scrapCollisionVert = scrapCollisionVertexes[x];
			vertexTailL2.nextInList = scrapCollisionVert;
			vertexTailL2 = scrapCollisionVert;
			currentFreeVertexesL2++;
		}
		vertexTailL2.nextInList = null;
		vertTail = vertexTailL2;
		currentFreeVertexes = currentFreeVertexesL2;	
	}

}

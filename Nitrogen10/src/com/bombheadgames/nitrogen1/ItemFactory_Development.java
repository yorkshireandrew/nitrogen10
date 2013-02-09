package com.bombheadgames.nitrogen1;

/** default class that implements the factory methods using the new operator */
final public class ItemFactory_Development implements ItemFactory{
	
	private int allocatedItems = 0;
	private int allocatedBacksides = 0;
	private int allocatedVertexes = 0;
	
	private int allocatedItemsMax = 0;
	private int allocatedBacksidesMax = 0;
	private int allocatedVertexesMax = 0;
	
	private int maxFreeItems = 0;
	private int maxFreeBackside = 0;
	private int maxFreeVertexes = 0;
	
	private int totalItems = 0;
	private int totalBacksides = 0;
	private int totalVertexes = 0;

	final public Item getItem(final SharedImmutableSubItem in_sisi, final Transform t)
	{
		if(++allocatedItems > allocatedItemsMax)allocatedItemsMax = allocatedItems;
		return new Item(in_sisi, t);
	}
	
	final public Backside getBackside(final ImmutableBackside ib)
	{
		if(++allocatedBacksides > allocatedBacksidesMax)allocatedBacksidesMax = allocatedBacksides;
		return new Backside(ib);
	}
	
	final public Vertex getVertex(final ImmutableVertex iv)
	{
		if(++allocatedVertexes > allocatedVertexesMax)allocatedVertexesMax = allocatedVertexes;
		return new Vertex(iv);
	}
	
	final public Vertex getVertex(final ImmutableCollisionVertex icv)
	{
		if(++allocatedVertexes > allocatedVertexesMax)allocatedVertexesMax = allocatedVertexes;
		return new Vertex(icv);
	}
	
	final public int getAllocatedItemCount(){return allocatedItems;}
	final public int getAllocatedBacksideCount(){return allocatedBacksides;}
	final public int getAllocatedVertexCount(){return allocatedVertexes;}

	final public int getAllocatedItemMax(){return allocatedItemsMax;}	
	final public int getAllocatedBacksideMax(){return allocatedBacksidesMax;}
	final public int getAllocatedVertexMax(){return allocatedVertexesMax;}
	final public void clearAllocatedMaximums()
	{
		allocatedItemsMax = allocatedItems;
		allocatedBacksidesMax = allocatedBacksides;
		allocatedVertexesMax = allocatedVertexes;
	}

	final public int getFreeItemCount()
	{
		if(allocatedItems > totalItems)return(0);
		return totalItems - allocatedItems;
	}
	
	final public int getFreeBacksideCount()
	{
		if(allocatedBacksides > totalBacksides)return(0);
		return totalBacksides - allocatedBacksides;	
	}
	
	final public int getFreeVertexCount()
	{
		if(allocatedVertexes > totalVertexes)return(0);
		return totalVertexes - allocatedVertexes;	
	}

	final public void setFreeItems(final int free)
	{
		totalItems = allocatedItems + free;
	}
	
	final public void setFreeBacksides(final int free)
	{
		totalBacksides = allocatedBacksides + free;
	}
	
	final public void setFreeVertexes(final int free)
	{
		totalVertexes = allocatedVertexes + free;		
	}
	
	final public void trimToSize()
	{
		totalItems = allocatedItems;
		totalBacksides = allocatedBacksides;
		totalVertexes = allocatedVertexes;
	}

	final public int getMaxFreeItems() {
		return maxFreeItems;
	}
	
	final public void setMaxFreeItems(final int maxFreeItems) {
		this.maxFreeItems = maxFreeItems;
	}	
	
	final public int getMaxFreeBacksides() {
		return maxFreeBackside;
	}

	final public void setMaxFreeBacksides(final int maxFreeBackside) {
		this.maxFreeBackside = maxFreeBackside;
	}

	final public int getMaxFreeVertexes() {
		return maxFreeVertexes;
	}

	final public void setMaxFreeVertexes(final int maxFreeVertex) {
		this.maxFreeVertexes = maxFreeVertex;
	}
	
	final public void recycle(final Item item)
	{
		allocatedItems--;
		allocatedBacksides -= item.getVertexes().length;
		allocatedVertexes -= item.getVertexes().length;
		allocatedVertexes -= item.getCollisionVertexes().length;
		if(getFreeItemCount() > maxFreeItems)setFreeItems(maxFreeItems);
		if(getFreeBacksideCount() > maxFreeBackside)setFreeItems(maxFreeBackside);
		if(getFreeVertexCount() > maxFreeVertexes)setFreeItems(maxFreeVertexes);
	};

}

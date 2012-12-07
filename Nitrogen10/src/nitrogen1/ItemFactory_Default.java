package nitrogen1;

/** default class that implements the factory methods using the new operator */
final public class ItemFactory_Default implements ItemFactory{

	final public Item getItem(final SharedImmutableSubItem in_sisi, final Transform t)
	{
		return new Item(in_sisi, t);
	}
	
	final public Backside getBackside(final ImmutableBackside ib)
	{
		return new Backside(ib);
	}
	
	final public Vertex getVertex(final ImmutableVertex iv)
	{
		return new Vertex(iv);
	}
	
	final public Vertex getVertex(final ImmutableCollisionVertex icv)
	{
		return new Vertex(icv);
	}

	final public void setFreeItems(final int free){}
	
	final public void trimToSize(){}

	final public void recycle(Item item){}

	@Override
	final public int getFreeItemCount() {
		return 0;
	}

	@Override
	final public int getFreeBacksideCount() {
		return 0;
	}

	@Override
	final public int getFreeVertexCount() {
		return 0;
	}

	@Override
	final public void setFreeBacksides(final int free) {
		
	}

	@Override
	final public void setFreeVertexes(final int free) {
		
	}

	@Override
	final public int getMaxFreeItems() {
		return 0;
	}

	@Override
	final public void setMaxFreeItems(final int maxFreeItems) {	
	}

	@Override
	final public int getMaxFreeBacksides() {
		return 0;
	}

	@Override
	final public void setMaxFreeBacksides(final int maxFreeBackside) {
		
	}
	
	@Override
	final public int getMaxFreeVertexes() {
		return 0;
	}

	@Override
	final public void setMaxFreeVertexes(int maxFreeVertexes) {
		
	}


}



package nitrogen1;

// used 
// import java.util.Enumeration;

/**
 *
 * @author andrew
 */
    class ItemVector {
    private Item[] vec;
    int capacity;
    int position;
    
    /**
     * Creates a new instance 
     */
    ItemVector() {
        vec = new Item[5];
        capacity = 5;
        position = 0;
    }
    
    /**
     * Creates a new instance of a given size
     */
    ItemVector(int initialcapacity){
        if (initialcapacity < 2) initialcapacity = 2;    // ensures the vector can expand
        vec = new Item[initialcapacity];
        capacity = initialcapacity;
        position = 0;
    }
    
    // add an element onto the end of the vector
    final void addElement(Item element)
    {
        if(position < capacity)
        {
            vec[position] = element;
            position++;
            return;
        }
        
        // if we have fallen through to here we must grow the array
        int cap2 = capacity + (capacity >> 1);      // grow array by 50%
 //       int cap2 = capacity << 1;                   // grow array by 100%
        Item[] vec2 = new Item[cap2];
        System.arraycopy(vec, 0, vec2, 0, position);
        vec = vec2;
        vec[position] = element; 
        capacity = cap2;
        position++;
    }
    
    final void setElementAt(Item element, int index)
    {
        vec[index] = element;
    }
    
    // retrieve an element from a given position in the vector
    final Item elementAt(int index)
    {
        return vec[index];
    }
    
    // return the number of elements inserted in the vector
    final int size()
    {
        return position;
    }
    
    final void trimToSize()
    {
         // shrink the array
        int cap2 = position;
        Item[] vec2 = new Item[cap2];
        System.arraycopy(vec, 0, vec2, 0, position);
        vec = vec2;
        capacity = position;         
    }
    
     final ItemEnumeration elements()
     {
         return(new ItemEnumeration(){
            private int index = 0;
            
            public Item nextElement(){
                if(index < position)
                {
                    index++;
                    return(vec[index-1]);
                }
                else
                {
                   return(null);
                }
            } // end of nextElement
           
            public boolean hasMoreElements()
            {
                if(index < position)
                {
                    return(true);
                }
                else
                {
                    return(false);
                }
            } // end of hasMoreElements
         }); // end of anonymous class and return statement   
     }
     
     final boolean removeElement(Item ob) 
     {
         int x = 0;
         while(x < position)
         {
             if(vec[x] == ob)
             {
                 // ok we have found what we are looking for
                 System.arraycopy(vec,(x+1),vec, x,(position-1-x));
                 position--;
                 
                if(  (position > 8) && (position < (capacity >> 2))  )
                {
                    // currently allocated memory is more 4x what is being used
                    // so reallocate a smaller array and arraycopy the data to it

                    int cap2 = position << 1;               // make array twice what is needed
                    if(cap2 < 4) cap2 = 4;                  // set a minimum size the vector can shrink
                    Item[] vec2 = new Item[cap2];
                    System.arraycopy(vec, 0, vec2, 0, position);
                    vec = vec2;
                    capacity = cap2;         
                }
                return true;
             }
             
            // carry on searching
             x++;
         }
         return false;
     }    
     
     final void removeAllElements()
     {
         // destroy all references in the old vector
         int x = 0;
         while(x < position)
         {
             vec[x] = null;
             x++;
         }
         
        // create an empty vector
        vec = new Item[5];
        capacity = 5;
        position = 0;
     }
}

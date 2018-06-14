package edu.yu.dataStructures.semesterProject;

import java.util.ArrayList;
import java.util.List;

/**
 * @author nannenbe@mail.yu.edu
 * represents BTree, which will be a dictionary that maps cell values to rows
 * copied from Algorithms Fourth Edition, by Robert Sedgewick and Kevin Wayne
 */
public class BTree<Key extends Comparable<Key>, Value>  {
    // max children per B-tree node = M-1
    // (must be even and greater than 2)
    private static final int M = 4;

    private Node root;       // root of the B-tree
    private int height;      // height of the B-tree
    private int size;           // number of key-value pairs in the B-tree

    // helper B-tree node data type
    private static final class Node {
        private int entryCount;                             // number of children
        private Entry[] children = new Entry[M];   // the array of children

        // create a node with k children
        private Node(int k) {
            entryCount = k;
        }
    }

    // internal nodes: only use key and next
    // external nodes: only use key and value
    private static class Entry {
        private Comparable key;
        private List<Object> vals;
        private Node next;     // the next node which corresponds to this entry
        
        public Entry(Comparable key, Object  val, Node next) {
            this.key  = key;
            this.vals  = new ArrayList<>();
            this.vals.add(val);
            this.next = next;
        }
        
        List<Object> getValues() {
        	return vals;
        }
    }

    //creates the BTree
    public BTree() {
        root = new Node(0);
    }

    //gets a value from the BTree, based on its key
    //returns null if the key doesn't exist in the BTree
    List<Value> get(Key key) {
    	Entry entry = getEntry(key);
    	if(entry == null) {
    		return null;
    	}
        return new ArrayList<Value>((List<Value>)entry.getValues());
    }
    //returns a list of entries. This will be used internally to modify entries and will be utilized by the get() method
    //to return a copy of the entry's list of values for the user
    private Entry getEntry(Key key) {
    	 if (key == null) throw new IllegalArgumentException("argument to get() is null");
         Entry entry = search(root, key, height);
         if(entry != null) {
         	return entry;
         }
         return null;
    }

    private Entry search(Node curNode, Key key, int curHeight) {
        Entry[] entries = curNode.children;

        // external node
        if (curHeight == 0) {
            for (int j = 0; j < curNode.entryCount; j++) {
                if (isEqual(key, entries[j].key)) { 
                	return entries[j];               
                }
            }
            return null;
        }

        // internal node
        else {
            for (int j = 0; j < curNode.entryCount; j++) {
            	//if this is the last entry or the next entry's key is greater than this key, search the current entry
                if (j + 1 == curNode.entryCount || less(key, entries[j + 1].key))
                    return search(entries[j].next, key, curHeight - 1);
            }
        }
        return null;
    }
   
   List<Value> getGreaterThanOrEqual(Key key) {
	   if (key == null) throw new IllegalArgumentException("argument to getGreaterThanOrEqual() is null");
       List<Entry> entries = searchGreaterOrLess(root, key, height, true);
       List<Value> values = new ArrayList<Value>();
       for(Entry entry : entries) {
    	   for(Value value : (List<Value>)entry.getValues()) {
    		   values.add(value);
    	   }
       }
       return values;
   }
  List<Value> getGreaterThan(Key key) {
	  if (key == null) throw new IllegalArgumentException("argument to getGreaterThan() is null");
      List<Entry> entries = searchGreaterOrLess(root, key, height, true);
      List<Value> values = new ArrayList<Value>();
      for(Entry entry : entries) {
    	  if(entry.key.equals(key)) {
    		  continue;   
   	  }
    	  for(Value value : (List<Value>)entry.getValues()) {
    		  values.add(value);
    	  }
      }
      return values;
    }
    List<Value> getLessThanOrEqual(Key key) {
	   if (key == null) throw new IllegalArgumentException("argument to getLessThanOrEqual() is null");
       List<Entry> entries = searchGreaterOrLess(root, key, height, false);
       List<Value> values = new ArrayList<Value>();
       for(Entry entry : entries) {
    	   for(Value value : (List<Value>)entry.getValues()) {
    		   values.add(value);
    	   }
       }
       return values;
   }
    List<Value> getLessThan(Key key) {
	   if (key == null) throw new IllegalArgumentException("argument to getLessThan() is null");
       List<Entry> entries = searchGreaterOrLess(root, key, height, false);
       List<Value> values = new ArrayList<Value>();
       for(Entry entry : entries) {
    	   if(entry.key.equals(key)) {
    		continue;   
    	   }
    	   for(Value value : (List<Value>)entry.getValues()) {
    		   values.add(value);
    	   }
       }
       return values;
    }
    //returns a list of entries that are either greater than or equal to the given key, or less than or equal to it,
    //depending on whether or not greater is set to true;
    private List<Entry> searchGreaterOrLess(Node curNode, Key key, int curHeight, boolean greater) {
        Entry[] entries = curNode.children;
        List<Entry> foundEntries = new ArrayList<Entry>();
        //if it's an external node, find all the entries that are greater than/less than/equal to the given key
        if (curHeight == 0) {
            for (int j = 0; j < curNode.entryCount; j++) {
            	if(greater) {
            		if (less(key, entries[j].key) || key.equals(entries[j].key)) { 
                    	foundEntries.add(entries[j]);
                    }
            	}
            	else {
	                if (greater(key, entries[j].key) || key.equals(entries[j].key)) { 
	                	foundEntries.add(entries[j]);
	                }
            	}
            }
            return foundEntries;
        }
        //if it's an internal node, traverse the tree towards entries greater than/less than/equal to the given key
        else {
            for (int j = 0; j < curNode.entryCount; j++) {
            	//if this is the last entry or the next entry's key is greater/less than this key, search the current entry
            	if(greater) {
	                if (j + 1 == curNode.entryCount || less(key, entries[j + 1].key) || key.equals(entries[j].key))
	                    foundEntries.addAll(searchGreaterOrLess(entries[j].next, key, curHeight - 1, greater));
            	}
            	else {
            		if (j == 0 || greater(key, entries[j].key) || key.equals(entries[j].key))
	                    foundEntries.addAll(searchGreaterOrLess(entries[j].next, key, curHeight - 1, greater));
            	}
            }
        }
        return foundEntries;
    }
    
    
  	//inserts the new key-value pair into the symbol
  	//will overwrite a previous value if they key already exists
  	//will delete the entry if the new value is null
    void put(Key key, Value value) {
        if (key == null) {
        	throw new IllegalArgumentException("argument key to put() is null");
        }
        Node newNode = insert(root, key, value, height); 
        
        if(value != null) {
        	size++;
        }
        else {
        	size--;
        }
        
        if (newNode == null) {
        	return;
        }

        // need to split root
        Node newRoot = new Node(2);
        newRoot.children[0] = new Entry(root.children[0].key, null, root);
        newRoot.children[1] = new Entry(newNode.children[0].key, null, newNode);
        root = newRoot;
        height++;
    }

    private Node insert(Node curNode, Key key, Value value, int curHeight) {
        int j;
        Entry newEntry = new Entry(key, value, null);
        //if it's an external node, find the appropriate index to insert the node/value
        if (curHeight == 0) {
        	//will increment j to be the appropriate index to insert the new node/value
            for (j = 0; j < curNode.entryCount; j++) {
            	//if an entry with that key already exists, append this value to that entry's list of values
            	if(curNode.children[j].key.equals(key)) {
            		curNode.children[j].vals.add(value);
            		return null;
            	}
                if (less(key, curNode.children[j].key)) {
                	break;
                }
            }
        }
        //if it's an internal node, traverse the tree until it hits an external node
        else {
            for (j = 0; j < curNode.entryCount; j++) {
            	//if this is the last entry or the next one is greater than it
                if ((j + 1 == curNode.entryCount) || less(key, curNode.children[j + 1].key)) {
                    Node newNode = insert(curNode.children[j++].next, key, value, curHeight - 1);
                    if (newNode == null) {
                    	return null;
                    }
                    newEntry.key = newNode.children[0].key;
                    newEntry.next = newNode;
                    break;
                }
            }
        }
        for (int i = curNode.entryCount; i > j; i--) {
            curNode.children[i] = curNode.children[i-1];
        }
        curNode.children[j] = newEntry;
        curNode.entryCount++;
        if (curNode.entryCount < M) {
        	return null;
        }
        //if the number of entries is equal to the maximum amount, this node needs to be split in two
        else{    
        	return split(curNode);
        }
    }

    // split node in half, adding the later half of its entries to a new node
    private Node split(Node curNode) {
        Node newNode = new Node(M/2);
        curNode.entryCount = M/2;
        for (int j = 0; j < M/2; j++) {
            newNode.children[j] = curNode.children[M/2+j];
        }
        return newNode;    
    }
    //deletes an entire entry by removing all of its values
    void deleteKey(Key key) {
    	List<Value> entryValues = (List<Value>) getEntry(key).getValues();
    	while(entryValues.size() > 0) {
    		entryValues.remove(entryValues.size() - 1);
    	}
    }
    //deletes a specific value
    void deleteValue(Key key, Value value) {
    	List<Value> entryValues = (List<Value>) getEntry(key).getValues();
    	for(int i = 0; i < entryValues.size(); i++) {
    		if(value.equals(entryValues.get(i))) {
    			entryValues.remove(i);
    		}
    	}
    	if(entryValues.size() == 0) {
    		deleteKey(key);
    	}
    }

    //comparison functions - make Comparable instead of Key to avoid casts
    private boolean less(Comparable k1, Comparable k2) {
    	//if the keys are numbers, they need to be handled differently.
    	//String comparison would just compare character by character, making 9000 < 91
    	try {
    		Double k1Double = Double.parseDouble((String) k1);
    		Double k2Double = Double.parseDouble((String) k2);
    		return k1Double.compareTo(k2Double) < 0;
    	}
    	catch(Exception e) {
    		return k1.compareTo(k2) < 0;
    	}
    }
    private boolean greater(Comparable k1, Comparable k2) {
    	//if the keys are numbers, they need to be handled differently.
    	//String comparison would just compare character by character, making 91 > 9000
    	try {
    		Double k1Double = Double.parseDouble((String) k1);
    		Double k2Double = Double.parseDouble((String) k2);
    		return k1Double.compareTo(k2Double) > 0;
    	}
    	catch(Exception e) {
    		return k1.compareTo(k2) > 0;
    	}
    }

    private boolean isEqual(Comparable k1, Comparable k2) {
        return k1.compareTo(k2) == 0;
    }
    
    boolean isEmpty() {
        return getSize() == 0;
    }
    int getSize() {
        return size;
    }
    int getHeight() {
        return height;
    }
}
package implementation;

import java.util.ArrayList;
import java.util.Comparator;

/**
 * Compares two edition based on the given Array listing all MTG editions.
 * @author Milan Nikl
 *
 */
public class EditionComparator implements Comparator<AvailabilityListItem> {
	
	private ArrayList<String> setsArray;
	
	/**
	 * Creates new comparator based on order of items in the given array.
	 * @param setsArray ordered list of all MTG sets
	 */
	public EditionComparator(String[] setsArray){
		this.setsArray = new ArrayList<String>();
		
		for (String set:setsArray){
			this.setsArray.add(set);
		}
	}

	@Override
	public int compare(AvailabilityListItem left, AvailabilityListItem right) {
		String leftEdition = left.getEdition();
		String rightEdition = right.getEdition();
		
		int leftIndex = getFakeIndex(leftEdition);
		int rightIndex = getFakeIndex(rightEdition);
		
	    int returnVal = 0;

	    if(leftIndex < rightIndex){
	        returnVal =  -1;
	    }else if(leftIndex > rightIndex){
	        returnVal =  1;
	    }else if(leftIndex == rightIndex){
	        returnVal =  0;
	    }
	    return returnVal;		
		
	}
	
	/**
	 * If an edition is not in the list, it should be listed last.
	 * So I use this little workaround.
	 * @param edition searched edition
	 * @return real index if listed, Int.MAX_VALUE if not
	 */
	private int getFakeIndex(String edition){
		int fakeIndex = setsArray.indexOf(edition);
		if(fakeIndex == -1){
			fakeIndex = Integer.MAX_VALUE;
		}
		return fakeIndex;
	}

}

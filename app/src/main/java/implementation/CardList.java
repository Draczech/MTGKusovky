package implementation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * ArrayList of the Cards objects.
 * Implements some specific insertion methods.
 * @author Milan Nikl
 *
 */
public class CardList extends ArrayList<Card> {
		
	private static final long serialVersionUID = -2279786551614499359L;

	/**
	 * Creates an empty ArrayList<Card>.
	 */
	public CardList(){
		super();
	}
	
	/**
	 * Doesn't create new objects if Card with the same name already exists.
	 */
	@Override
	public boolean add(Card toAdd){
		
		Card found = this.getItemByName(toAdd.getName());
		if (found == null){
			super.add(toAdd);
		}
		else {
			found.getAvailability().addAll(toAdd.getAvailability());
		}
		
		return true;
	}
	
	/**
	 * Inserts all cards from the given list to this list.
	 * @param cards existing CardList.
	 */
	public void insertCards(CardList cards){
		for (Card c: cards){
			this.add(c);
		}		
	}
	
	/**
	 * Inserts all cards from the given list to this list.
	 * Used by AsyncTasks, may be removed in the future.
	 * @param cards existing CardList.
	 */
	public void insertCards(ArrayList<Card> cards){
		for (Card c: cards){
			this.add(c);
		}		
	}
	
	/**
	 * Searches for a card with the same name.
	 * @param searched Card you are looking for
	 * @return true if contained in the list, false if not
	 */
	public boolean contains(Card searched){		
		for (Object temp : this){
			Card tc = (Card) temp;
			
			if (tc.getName().equalsIgnoreCase(searched.getName())){
				return true;
			}
		}		
		return false;
	}
	
	/**
	 * Searches for a card with the same name.
	 * @param name of the Card you are looking for
	 * @return Card object with the matching name
	 */
	public Card getItemByName(String name){				
		for (Card temp : this){
			Card tc = (Card) temp;
			
			if (tc.getName().equalsIgnoreCase(name)){
				return temp;
			}
		}
		
		return null;
	}
	
	/**
	 * Sorts the list.
	 */
	public void sort(){
		Collections.sort(this, new CardComparator());
	}
	
	@Override
	public String toString(){
		String text = "";
		
		for (Card c : this){
			text += c.toString();
		}
		
		return text;
	}
	
	/**
	 * Comparator for comparing two Card objects based on their names.
	 * @author Milan
	 *
	 */
	public class CardComparator implements Comparator<Card> {
	    @Override
	    public int compare(Card o1, Card o2) {
	        return o1.getName().compareTo(o2.getName());
	    }
	}

}

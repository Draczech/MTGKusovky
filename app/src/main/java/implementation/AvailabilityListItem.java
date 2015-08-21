package implementation;

import java.util.ArrayList;

/**
 * Special item for creating list in the CardDetailActivity
 * Edition is showed in the group (parent) view.
 * Shop, Price, State, In_stock are showed in the child view.
 * @author Milan Nikl
 *
 */
public class AvailabilityListItem {
	
	private String edition;	
	private ArrayList<ShopInfo> availability;
	
	/**
	 * Creates new item with empty availability list and edition attribute.
	 * @param edition edition of the card a shop offers.
	 */
	public AvailabilityListItem (String edition){
		this.edition = edition;
		this.availability = new ArrayList<ShopInfo>();				
	}
	
	public String getEdition() {
		return edition;
	}
	public void setEdition(String edition) {
		this.edition = edition;
	}
	public ArrayList<ShopInfo> getAvailability() {
		return availability;
	}
	public void setAvailability(ArrayList<ShopInfo> availability) {
		this.availability = availability;
	}
	
	/**
	 * Inserts new availability information.
	 * @param si
	 */
	public void insertShopInfo(ShopInfo si){
		if (this.availability.isEmpty()){
			availability.add(si);
			return;
		}
		
		if (this.availability.contains(si)){
			return;
		}
		else{
			this.availability.add(si);
		}
		
	}
	
	

}

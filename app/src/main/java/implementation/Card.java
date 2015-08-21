package implementation;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Object storing all information about a card.
 * Uses serializable in order to be stored and passed between activities. 
 * @author Milan Nikl
 *
 */
public class Card implements Serializable {
	
	private static final long serialVersionUID = 3608522522842871496L;
	
	private String name;
	private String cardText;
	private String type;
	private String manacost;
	private String flavor;
	private String artist;
	private String rulings;
	private String legality;
	private String editions;
	private String picture;
	// last rarity as stated at Magiccards.info
	private String lastRarity;
	
	private ArrayList<ShopInfo> availability;
	
	/**
	 * Creates new Card object with empty ShopInfo list.
	 */
	public Card(){
		this.availability = new ArrayList<ShopInfo>();		
	}	

	public String getName() {
		return name;
	}

	public void setName(String name) {
		if (name.isEmpty()){
			name = "-";
		}
		// removes rubbish from the name
		name = name.replaceAll("´", "'");
		name = name.replaceAll("’", "'");
		name = name.replaceAll("‘", "'");
		name = name.replaceAll("‛", "'");
		name = name.replaceAll("“", "\"");
		name = name.replaceAll("‟", "\"");
		name = name.replaceAll("„", "\"");
		this.name = name;
	}

	public String getCardText() {
		return cardText;
	}

	public void setCardText(String cardText) {
		if (cardText.isEmpty()){
			cardText = "-";
		}
		this.cardText = cardText;
	}
	
	public String getType() {
		return type;
	}

	public void setType(String type) {
		if (type.isEmpty()){
			type = "-";
		}
		this.type = type;
	}
	
	public String getManacost() {
		return manacost;
	}

	public void setManacost(String manacost) {
		if (manacost.isEmpty()){
			manacost = "-";
		}
		this.manacost = manacost;
	}

	public ArrayList<ShopInfo> getAvailability(){
		return availability;
	}
	
	public void setAvailability(ArrayList<ShopInfo> availability){
		this.availability = availability;
	}
		
	public String getFlavor() {
		return flavor;
	}

	public void setFlavor(String flavor) {
		if (flavor.isEmpty()){
			flavor = "-";
		}
		this.flavor = flavor;
	}

	public String getArtist() {
		return artist;
	}

	public void setArtist(String artist) {
		if (artist.isEmpty()){
			artist = "-";
		}
		this.artist = artist;
	}

	public String getRulings() {
		return rulings;
	}

	public void setRulings(String rulings) {
		if (rulings.isEmpty()){
			rulings = "-";
		}
		this.rulings = rulings;
	}

	public String getLegality() {
		return legality;
	}

	public void setLegality(String legality) {
		if (legality.isEmpty()){
			legality = "-";
		}
		this.legality = legality;
	}

	public String getPicture() {
		return picture;
	}

	public void setPicture(String picture) {
		this.picture = picture;
	}

	public String getEditions() {
		return editions;
	}

	public void setEditions(String editions) {
		if (editions.isEmpty()){
			editions = "-";
		}
		this.editions = editions;
	}
	
	public void setLastRarity(String lastRarity) {
		if(lastRarity.isEmpty()){
			lastRarity = "-";
		}
		this.lastRarity = lastRarity;
	}
	
	public String getLastRarity() {
		return lastRarity;
	}

	
	public String toString(){
		String text = "Název: " + this.name;
		text += "\nTyp: " + this.type;
		text += "\nManacost: " + this.manacost;
//		text += "\n> Text: " + this.cardText;
//
//		text += "\n> Flavor: " + this.flavor;
//		text += "\n> Edice: " + this.editions;		
//		text += "\n> Malíř: " + this.artist;
//		text += "\n> Pravidla: " + this.rulings;
//		text += "\n> Legalita: " + this.legality;
//		
//		
//		
//		text += "\n> Dostupnost:";
//		text += "\n" + this.availability.toString();
		return text;
		
	}
	
	/**
	 * Converts a Card object to AvailabilityListItem list,
	 * in order to be used for displaying information in the CardDetailActivity.
	 * @return list of AvailabilityListItem objects
	 */
	public ArrayList<AvailabilityListItem> convertToAvaList(){
		ArrayList<AvailabilityListItem> converted = new ArrayList<AvailabilityListItem>();
		
		for (ShopInfo si:this.availability){
			int position = isListed(si.getEdition(),converted);
			// not in the list
			if (position < 0) {
				AvailabilityListItem temp = new AvailabilityListItem(si.getEdition());
				temp.getAvailability().add(si);
				converted.add(temp);
			}			
			else {
				AvailabilityListItem tmp = converted.get(position);
				
				// add new ShopInfo to the existing item
				tmp.getAvailability().add(si);
				converted.set(position, tmp);
			}				
		}
		
		return converted;
	}
	
	/**
	 * Returns position of an object with the specified edition in the given list.
	 * @param edition specified edition
	 * @param list selected list to be searched
	 * @return position of matching object
	 */
	private int isListed(String edition, ArrayList<AvailabilityListItem> list){
		for (AvailabilityListItem ali:list){
			if (ali.getEdition().equals(edition)){
				return list.indexOf(ali);
			}
		}
		return -1;
	}
	
}

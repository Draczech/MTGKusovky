package implementation;

import java.io.Serializable;

/**
 * Stores information about card availability in particular shop.
 * @author Milan Nikl
 *
 */
public class ShopInfo implements Serializable {
	
	private static final long serialVersionUID = 7992884451953490703L;
	
	private String shop;
	private String edition;
	private String rarity;
	private String version;
	private int price;
	private int count;
	
	/**
	 * Creates object for given shop.
	 * @param shop particular shop
	 */
	public ShopInfo(String shop){
		this.shop = shop;
	}
	
	public String getEdition() {
		return edition;
	}

	public void setEdition(String edition) {
		
		// removes rubbish from the edition
		// also serves for unifying
		edition = edition.replaceFirst("Duel Decks", "DD");
		edition = edition.replaceFirst("Premium Deck Series", "PDS");		
		edition = edition.replaceFirst("Magic: The Gathering-", "");
		edition = edition.replaceFirst("From The Vault:|From the Vault:", "FTV: ");
		edition = edition.replaceFirst("vs. The Coalition|vs. Coalition", "vs. the Coalition");
		edition = edition.replaceFirst(" Core Set", "");
		edition = edition.replaceAll(" +", " ");
		edition = edition.replaceAll("´", "'");
		edition = edition.replaceAll("’", "'");
		edition = edition.replaceAll("‘", "'");
		edition = edition.replaceAll("‛", "'");
		
		this.edition = edition;
	}
	
	public String getRarity() {
		return rarity;
	}

	public void setRarity(String rarity) {
		// Transfers first character to uppercase
		String temp = rarity.substring(0, 1).toUpperCase().concat(rarity.substring(1));
		// Uses shortage for Mythic rare
		if (temp.equalsIgnoreCase("Mythic rare")){												
			temp = "Mythic";
		}
		this.rarity = temp;
	}

	public int getPrice() {
		return price;
	}

	public void setPrice(int price) {
		this.price = price;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}
		
	public String getShop() {
		return shop;
	}

	public void setShop(String shop) {
		this.shop = shop;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		// removes rubbish from the version
		version = version.replaceFirst("v cizím jazyce", "cizojazyčná");
		version = version.replaceFirst("Poznámka|poznámka", "Pozn");
		this.version = version;
	}

	public String toString(){
		String text = "";
		text += "\n Obchod: " + this.shop;
		text += "\n Edice: " + this.edition;
		text += "\n Provedeni: " + this.version;
		text += "\n Rarita: " + this.rarity;
		text += "\n Cena: " + this.price + " Kč";
		text += "\n Kusů skladem: " + this.count;
		text += "\n";
		return text;
	}
		
}

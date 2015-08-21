package parser;

import implementation.Card;
import implementation.CardList;
import implementation.ShopInfo;

import java.io.IOException;
import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


/**
 * 
 * @author Milan Nikl
 *
 */
public class Rishada implements IParser {

	private final String SHOPNAME = "Rishada";
	private final String BASIC_PREFIX = "http://www.rishada.cz/kusovky/vysledky-hledani?searchtype=basic&xxwhichpage=1&xxcardname=";
	private final String BASIC_INTERFIX = "&xxedition=1000000&xxpagesize=";
	private final String BASIC_SUFIX = "&search=Vyhledat";
	private final int CARD_LIMIT = 60;					// lze nastavit libovolne (no, neni to uplne ciste, ale co...)
	
	
	private CardList cards;
	private String cardname;
	
	
	public Rishada(){
		this.cards = new CardList();
	}
	
	public String getShopName(){
		return this.SHOPNAME;
	}
	
	public ArrayList<Card> fetchPrices(String fetched) throws Exception{
		cardname = fetched;
		
		String uri = BASIC_PREFIX;
		uri += cardname;
		uri += BASIC_INTERFIX;
		uri += CARD_LIMIT + BASIC_SUFIX;
		
		
		try{
			parsePage(uri);
		}
		catch (Exception e){
			throw e;
		}
		
		return cards;
	}
	
	public void parsePage(String uri) throws Exception{
		Document doc = null;
		
		try {
			doc = Jsoup.connect(uri)
					.timeout(8000)					// nevim, jestli je to chyba serveru, ale rishada potrebuje delsi cas na nacteni vysledku
					.get();
		} catch (IOException e) {
//			e.printStackTrace();
//			System.out.println(">>> Při přístupu na: " + SHOPNAME + " došlo k chybě:");
//			System.out.println(e.getMessage());
			throw e;
		}
		if (doc != null){
			
			Elements table = doc.select(".buytable > tbody:nth-child(1)");
			
			if ((table == null) || (table.isEmpty())){
//				System.out.println(">>> " + SHOPNAME + " - Nebyla nalezena žádná odpovídající karta...");
				return;
			}
			
			Element tbody = table.first();
					
			Elements rows = tbody.select("tr:not([height=\"20\"])");			// vsechny radky krome headeru
			rows.remove(0);
			
			
			if ((rows == null) || (rows.isEmpty())){
//				System.out.println(">>> " + SHOPNAME + " - Nebyla nalezena žádná odpovídající karta...");
				return;
			}
			
			for (Element row : rows){
				Elements cells = row.select("td");								// kazdy radek ma 8 sloupecku
				
				String name = cells.get(0).text();								// v prvni bunce je jako text nazev karty
				
				String cardTextHtml = cells.get(0).html();						// a v html je ukryty typ a text karty
					cardTextHtml = cardTextHtml.replaceAll("&lt;br /&gt;", "\n");		// opravi konce radek
					cardTextHtml = cardTextHtml.replaceAll("&lt;.*&gt;", "");			// odstrani prebytecne tagy
					cardTextHtml = cardTextHtml.replaceAll("\\]\".*</a>", "");			// odstrani konce </a> tagu
					cardTextHtml = cardTextHtml.replaceAll("<a.*\\)", "");				// odstrani zacatek <a> tagu
					cardTextHtml = cardTextHtml.replaceAll("NULL", "");					// divny, ale je to tam...
					cardTextHtml = cardTextHtml.replaceAll("&amp;.*;", "-");			// obcas tam vznika nejaky bordel
					cardTextHtml = cardTextHtml.replaceAll("[^\\p{ASCII}]+", "");			// obcas tam vznika nejaky bordel
					
					String[] cardTextParts = cardTextHtml.trim().split("\n");
					
				String type = cardTextParts[0];									// na prvnim radku textu je typ karty
				String cardText = "";
					
					if(cardTextParts.length > 1){										// na ostatnich je text karty
						for (int i = 1; i < cardTextParts.length; i ++){
							cardText += cardTextParts[i] + "\n";
						}
					}
													
				String edition = cells.get(1).text();							// ve druhe edice karty
				
				String manaHtml = cells.get(2).html();							// ve treti manacost ve forme obrazku
					manaHtml = manaHtml.replaceAll(" />", "\n");						// oddeli jednotlive obrazky
					manaHtml = manaHtml.replaceAll("<img src=\"/pic/mana/small/", "");	// dostane prislusny symbol na zacatek radku
					manaHtml = manaHtml.replaceAll("\\[.+\\]", "");
					manaHtml = manaHtml.replaceAll("big\\.gif\" width=\"12\" height=\"12\" alt=\"\"","");
					String manacost = "";
					
					String[] manaParts = manaHtml.split("\n");
					for (int i = 0; i <manaParts.length; i ++){
						if (manaParts[i].length() == 1){
							manacost += manaParts[i];
						}
						else if (manaParts[i].length() == 2){
							if (manaParts[i].contains("p")){				//spec. pripad phyrexian many
								manacost += "{" +  manaParts[i] + "}";
							}
							else{											// spec. pripad pulene many
								manacost += "{" +  manaParts[i].charAt(0) + "/" + manaParts[i].charAt(1) + "}";
							}
						}
					}
					
					if (manacost.length() > 0){
						manacost = manacost.toUpperCase();
					}
					else{
						manacost = "-";
					}	
				
				String version = cells.get(3).text();							// ve ctvrte kvalita karty
				
				String rarity = cells.get(4).text();							// v pate rarita
																
				int price = 0;
				String rawPrice = cells.get(5).text();							// v seste cena
				rawPrice = rawPrice.replaceAll("[^0-9]", "");					// odstrani pripadne blbosti, vcetne " Kč" na konci
				rawPrice = rawPrice.trim();
				if (!rawPrice.isEmpty()){
					price = Integer.parseInt(rawPrice);
				}
				
				int count = 0;
				String rawCount = cells.get(6).text();							// v sedme pocet karet skladem
				rawCount = rawCount.replaceAll("[^0-9]", "");					// odstrani pripadne blbosti
				rawCount = rawCount.trim();
				if (!rawCount.isEmpty()){
					count = Integer.parseInt(rawCount);
				}
				
				
				if (name.contains(" (")){										// specialni karty
					String[] artifacts = name.split(" \\(");
					name = artifacts[0];
					version = artifacts[1];
					version = version.replaceFirst("\\)", "");
				}				
				else if(name.contains(" - ")){
					String[] artifacts = name.split(" - ");
					name = artifacts[0];
					version = artifacts[1];					
				}
				
				name = name.replaceAll("´", "'");					// uprava nesrovnalosti
				name = name.replaceFirst("Aether|AEther", "Æther");
				name = name.replaceFirst("Aerathi|AErathi", "Ærathi");
								
				edition = edition.replaceFirst("Alpha", "Limited Edition Alpha");
				edition = edition.replaceFirst("Beta", "Limited Edition Beta");
				edition = edition.replaceFirst("Unlimited", "Unlimited Edition");
				edition = edition.replaceFirst("Revised", "Revised Edition");
				edition = edition.replaceFirst("Blackbordered", "");
				edition = edition.replaceFirst("Whitebordered", "");
				edition = edition.replaceFirst("Basic sets", "Basic Sets");
				edition = edition.replaceFirst("Judge rewards", "Promo");
				edition = edition.replaceFirst("FNM Promo", "Promo");
				
				if (edition.contains("vs.")){
					edition = "DD: " + edition;
				}
				

				Card temp = new Card();								
				temp.setName(name.trim());
				temp.setManacost(manacost.trim());			
				temp.setType(type.trim());
				temp.setCardText(cardText.trim());	
				
				ShopInfo info = new ShopInfo(this.SHOPNAME);
				info.setEdition(edition.trim());
				info.setRarity(rarity.trim());
				info.setVersion(version.trim());
				info.setCount(count);
				info.setPrice(price);
				
				temp.getAvailability().add(info);
				cards.add(temp);	
				
			}
		}
	}

}

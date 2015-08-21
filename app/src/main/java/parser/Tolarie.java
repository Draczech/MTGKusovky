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
public class Tolarie implements IParser{

	private final String SHOPNAME = "Tolarie";
	private final String BASIC_PREFIX = "http://tolarie.cz/koupit_karty/?name=";
	private final String BASIC_SUFIX = "&o=name&od=a";
	private final String ADV_SUFIX = "&p=";
	private final int IPP = 50;			//items per page
	private final int MAX_PAGES = 1;	// omezeni, aby se nenacitalo zbytecne moc vysledku
	
	private boolean firstPage;
	private CardList cards;
	private String cardname;
	
	public Tolarie(){
		this.cards = new CardList();
		this.firstPage = true;
	}
	
	public String getShopName(){
		return this.SHOPNAME;
	}

	public ArrayList<Card> fetchPrices(String fetched) throws Exception{
		cardname = fetched;
		
		String uri = BASIC_PREFIX;
		uri += cardname;
		uri += BASIC_SUFIX;
		
		try{
			parsePage(uri);
		}
		catch (Exception e){
			throw e;
		}
		
		return cards;
	}
	
	public void parsePage(String uri){
		Document doc = null;
		
		try {
			doc = Jsoup.connect(uri).get();
		} catch (IOException e) {
//			e.printStackTrace();
			System.out.println(">>> Při přístupu na: " + SHOPNAME + " došlo k chybě:");
			System.out.println(e.getMessage());
			return;
		}
		if (doc != null){
			
//			System.out.println("doc:");
//			System.out.println(doc.html());
			
			if(firstPage){	
				Elements paging = doc.select("div.pagination > ul"); 
				
				if ((paging != null) && !paging.isEmpty()){				
					String pagingText = paging.first().text();
					pagingText = pagingText.replaceAll("[[^0-9]&&[^ ]]", "");
					
					String[] pagesStr = pagingText.split(" ");
					
					int lastPage = Integer.parseInt(pagesStr[pagesStr.length - 1]);
					
					if (lastPage > MAX_PAGES){
						lastPage = MAX_PAGES;
					}
					
					String nextUri;
					
					firstPage = false;	
					
					for (int i = 2; i < lastPage; i++){
						nextUri = BASIC_PREFIX + cardname + BASIC_SUFIX + ADV_SUFIX + i;
						parsePage(nextUri);
					}
				}
			}
			
			Elements table = doc.select("table.kusovky");
			
			if ((table == null) || (table.isEmpty())){
				System.out.println(">>> " + SHOPNAME + " - Nebyla nalezena žádná odpovídající karta...");
				return;
			}
			
//			System.out.println("table:");
//			System.out.println(table.html());
			
			Elements rows = table.select("tbody");
			
			if ((rows == null) || (rows.isEmpty())){
				System.out.println(">>> " + SHOPNAME + " - Nebyla nalezena žádná odpovídající karta...");
				return;
			}
			
			// rozdeleni na radky
			for (Element row : rows){
																				
				Element td_name = row.select("td.td_name").first();				// prvni sloupecek jmeno a dostupnost
				String name = td_name.child(0).text();
				
				int count = 0;
				String rawCount = td_name.child(1).text();							
				rawCount = rawCount.replaceAll("[^0-9]", "");					// odstrani pripadne blbosti
				rawCount = rawCount.trim();
				if (!rawCount.isEmpty()){
					count = Integer.parseInt(rawCount);
				}
																				// druhy sloupecek barva
				
				Element td_mana = row.select("td.td_mana").first();				// ve tretim manacost ve forme obrazku
				String manaHtml = td_mana.html();							
					manaHtml = manaHtml.replaceAll(".gif\" />", "\n");									// oddeli jednotlive obrazky
					manaHtml = manaHtml.replaceAll("<img src=\"/static/images/mana_symbols/", "");		// dostane prislusny symbol na zacatek radku
					
					String manacost = "";
					String[] manaParts = manaHtml.split("\n");
					for (int i = 0; i <manaParts.length; i ++){
						if (manaParts[i].length() == 1){
							manacost += manaParts[i];
						}
						else if (manaParts[i].length() == 2){												// spec. pripad pulene many
							manacost += "{" +  manaParts[i].charAt(0) + "/" + manaParts[i].charAt(1) + "}";
						}
						else if (manaParts[i].length() == 3){												// spec. pripad phyrexian many - tvar "PhB" apod.
							manacost += "{" +  manaParts[i].charAt(2) + manaParts[i].charAt(0) + "}";
						}
					}
					
					if (manacost.length() > 0){
						manacost = manacost.toUpperCase();
					}
					else{
						manacost = "-";
					}
				
				Element td_rarita = row.select("td.td_rarita").first();				// ve ctvrtem rarita	
				String rarity = td_rarita.text().trim();							
				
				Element td_edice = row.select("td.td_edice").first();				// v patem edice karty
				String edition = td_edice.text().trim();							
				
				
				
				int price = 0;
				Element td_price = row.select("td.td_price").first();
				String rawPrice = td_price.text();
				rawPrice = rawPrice.replaceAll("[^0-9]", "");					// odstrani pripadne blbosti, vcetne " Kč" na konci
				rawPrice = rawPrice.trim();
				if (!rawPrice.isEmpty()){
					price = Integer.parseInt(rawPrice);
				}				
				
				String version = "-";
				
				if (name.contains(" (")){										// specialni karty
					String[] artifacts = name.split(" \\(");
					name = artifacts[0];
					version = artifacts[1];
					version = version.replaceFirst("\\)", "");
				}
				
				// tyto udaje nejsou na strance uvedeny
				String type = "-";
				String cardText = "-";
				
				name = name.replaceAll("´", "'");					// uprava nesrovnalosti
				name = name.replaceAll("Aether|AEther", "Æther");
				name = name.replaceAll("Aerathi|AErathi", "Ærathi");
				
				
				version = version.replaceFirst("3rd", "Revised");
				version = version.replaceFirst("4th", "Fourth");
				version = version.replaceFirst("5th", "Fifth");
				version = version.replaceFirst("6th", "Classic Sixth");
				version = version.replaceFirst("7th", "Seventh");
				version = version.replaceFirst("8th", "Eighth");
				version = version.replaceFirst("9th", "Ninth");
				version = version.replaceFirst("10th", "Tenth");
				
				if (!edition.equalsIgnoreCase("Return to Ravnica")){
					edition = edition.replaceFirst("Ravnica", "Ravnica: City of Guilds");
				}
				edition = edition.replaceFirst("Magic 10", "Magic 2010");
				edition = edition.replaceFirst("Magic 11", "Magic 2011");
				edition = edition.replaceFirst("Magic 12", "Magic 2012");
				edition = edition.replaceFirst("Magic 13", "Magic 2013");
				edition = edition.replaceFirst("Magic 14", "Magic 2014");
				edition = edition.replaceFirst("Magic 15", "Magic 2015");
				
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

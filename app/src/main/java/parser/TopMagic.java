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

public class TopMagic implements IParser{
	private final String SHOPNAME = "TopMagic";
	private final String BASIC_PREFIX = "http://topmagic.cz/vyhledat.aspx?najit=";
	private final String ADV_SUFIX = "&strana=";
	private final int IPP = 15;			// items per page
	private final int MAX_PAGES = 4;
	
	private boolean firstPage;
	private CardList cards;
	private String cardname;
	
	public TopMagic(){
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
					.timeout(8000)
					.get();
		} catch (IOException e) {
//			e.printStackTrace();
//			System.out.println(">>> PĹ™i pĹ™Ă­stupu na: " + SHOPNAME + " doĹˇlo k chybÄ›:");
//			System.out.println(e.getMessage());			
			throw e;
		}
		if (doc != null){
			
			if(firstPage){
				Element footer = doc.select(".obsah > #hlavniObsah > div").last();
				if (footer == null){
//					System.out.println(">>> " + SHOPNAME + " - Nebyla nalezena žádná odpovídající karta...");
					return;
				}
				
				Elements counterElements = footer.select("div > p");
				Element counter = counterElements.select("a, u").last();			// posledni prvek v radce
				
				if (counter == null){
//					System.out.println(">>> " + SHOPNAME + " - Nebyla nalezena žádná odpovídající karta...");
					return;
				}
				String counterText = counter.text();
				counterText = counterText.replaceAll("[^0-9 ]", "");
				counterText = counterText.trim();
				
				int pageCount = 1;
				
				if (!counterText.isEmpty()){
					pageCount = Integer.parseInt(counterText.trim());
				}
				
				if (pageCount > MAX_PAGES){
					pageCount = MAX_PAGES;
				}
									
				String nextUri;					
				firstPage = false;
				
				for (int i = 2; i < (pageCount + 1); i++){
					nextUri = BASIC_PREFIX;
					nextUri += cardname;
					nextUri += ADV_SUFIX;
					nextUri += i;
					
					parsePage(nextUri);
				}				
			}
			
			Elements content = doc.select(".obsah");
			if ((content != null) && !content.isEmpty()){	
				
				Elements rows = content.select("#hlavniObsah > div");
				
				if ((rows == null) || (rows.isEmpty())){
//					System.out.println(">>> " + SHOPNAME + " - Nebyla nalezena žádná odpovídající karta...");
					return;
				}
				
				rows.remove(rows.last());

				
				for (Element row : rows){
					
					Element header = row.select("div:nth-child(2) > p:nth-child(1)").first();	
					
					String version = "";
					String name = header.text();
					
					if (name.contains(" - ")){
						String[] parts = name.split(" - ");
						if (parts.length > 1){
							name = parts[0];
							version = parts[1];
						}
					}
					
					Element editionEl = row.select("div:nth-child(2) > p:nth-child(2)").first();
					String edition = editionEl.text();
					if (edition.isEmpty()){
						edition = "-";
					}
					
					Element rarityEl = row.select("div:nth-child(2) > p:nth-child(3)").first();
					String rarity = rarityEl.text();
					if (rarity.isEmpty()){
						rarity = "-";
					}
					
					Element countEl = row.select("div:nth-child(2) > p").last();					
					int count = 0;
					String rawCount = countEl.text();								
					rawCount = rawCount.replaceAll("[^0-9]", "");					// odstrani "skladem" a pripadne blbosti
					rawCount = rawCount.trim();
					if (!rawCount.isEmpty()){
						count = Integer.parseInt(rawCount);
					}
										
					Element priceEl = row.select("div:nth-child(3) > p:nth-child(1)").first();
					
					int price = 0;
					String rawPrice = priceEl.text();							
					rawPrice = rawPrice.replaceAll("[^0-9]", "");				// odstrani pripadne blbosti, vcetne " Kč" na konci
					rawPrice = rawPrice.trim();
					if (!rawPrice.isEmpty()){
						price = Integer.parseInt(rawPrice);
					}
										
					
					name = name.replaceFirst("´", "'");					// uprava nesrovnalosti
					name = name.replaceFirst("Aether|AEther", "Æther");
					name = name.replaceFirst("Aerathi|AErathi", "Ærathi");
					
					Card temp = new Card();								
					temp.setName(name.trim());
					temp.setCardText("");
					temp.setManacost("");
					temp.setType("");
					ShopInfo info = new ShopInfo(this.SHOPNAME);
					info.setEdition(edition.trim());
					info.setRarity(rarity.trim());
					info.setVersion(version.trim());
					info.setCount(count);
					info.setPrice(price);
					
					temp.getAvailability().add(info);
					
					cards.add(temp);								// ulozeni do vysledku
					
					
					
				}
			}
		}
	}

}

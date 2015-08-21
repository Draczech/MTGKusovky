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
public class Lotus implements IParser {
	private final String SHOPNAME = "Black Lotus";
	private final String BASIC_PREFIX = "http://www.blacklotus.cz/";
	private final String ADV_PREFIX = "http://www.blacklotus.cz/?page=search&pageno=";
	private final String ADV_SUFIX = "&sortmode=7&searchtext=";
	private final int IPP = 20;			//items per page
	private final int MAX_PAGES = 3;
	
	private boolean firstPage;
	private CardList cards;
	private String cardname;
	private String hashPart;										// k tomuhle hashi se neumim dostat, ale jakmile ho najdu je shodny pro vsechny stranky u daneho hledani
	
	public Lotus(){
		this.cards = new CardList();
		this.firstPage = true;
	}
	
	public String getShopName(){
		return this.SHOPNAME;
	}

	public ArrayList<Card> fetchPrices(String fetched) throws Exception {
		cardname = fetched;
		
		String uri = BASIC_PREFIX;
		try {
			String correct = retrieveUri(uri);
			parsePage(correct);
		}
		catch (Exception e){
			throw e;
		}		
		return cards;
	}
	
	public String retrieveUri(String oldUri){
		String correctUri = "";
		try {
			Document pom = Jsoup.connect(oldUri)					// pokusi se pripojit na stranku, ale jelikoz neumim zjistit spravny hash v URL
					.cookie("search[text]",cardname)				// nepovede se ke strance dostat
					.cookie("search[template]", "all")				// nicmene ve vyjimce se objevi spravna adresa, na kterou se potrebuji pripojit
					.cookie("page", "search")
					.cookie("sortmode", "7")
					.cookie("search[only_text_search]", "true")
					.post();
			} catch (IOException e) {
				
				String txt = e.getMessage();			
				correctUri = txt.replaceFirst(".*http", "http");			// adresu z vyjimky pouzijeme pro dalsi hledani
				
				hashPart = correctUri.replaceFirst(".*searchtext=", "");				// ulozeni hashe a hledaneho stringu z URI
			}
		
		return correctUri;
	}
	
	public void parsePage(String uri) throws Exception{
		Document doc;				
			try {
				doc = Jsoup.connect(uri)
						.timeout(8000)
						.get();	
			} catch (IOException e1) {
//				
//				System.out.println(">>> Při přístupu na: " + SHOPNAME + " došlo k chybě:");
//				System.out.println(e1.getMessage());
				
				throw e1;				
			}
				
		if (doc != null){
			
			if(firstPage){	
				Elements paging = doc.select("div.paging:nth-child(4)"); 
				
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
					
					for (int i = 2; i < (lastPage + 1); i++){						// pruchod pripadnych dalsich stranek
						nextUri = ADV_PREFIX + i + ADV_SUFIX;
						nextUri += hashPart;
						parsePage(nextUri);
					}
				}
			}	

			Elements list = doc.select("#list");
			
			if ((list == null) || (list.isEmpty())){
//				System.out.println(">>> " + SHOPNAME + " - Nebyla nalezena žádná odpovídající karta...");
				return;
			}
			
			Elements items = list.select("li:not(.clear)");
			
			if ((items == null) || (items.isEmpty())){
//				System.out.println(">>> " + SHOPNAME + " - Nebyla nalezena žádná odpovídající karta...");
				return;
			}
			
			for (Element item: items){
				Element header = item.select("h2").first();
				String name = header.text().trim();
				
				Elements stockInfo = item.select("div.prices");
				
				Element availabilityInfo = stockInfo.get(0);				// prvni cast obsahuje dostupnost, druha cenu
				Element availability = availabilityInfo.select("dd").first();										
							
				int count = 0;
				String rawCount = availability.text();							
				rawCount = rawCount.replaceAll("[^0-9]", "");					// odstrani pripadne blbosti, treba "Docasne nedostupne"
				rawCount = rawCount.trim();
				if (!rawCount.isEmpty()){
					count = Integer.parseInt(rawCount);
				}
				
				Element priceInfo = stockInfo.get(1);						// druha cast s udaji o cene
				Element pricing = priceInfo.select("dd").first();
											
				int price = 0;
				String rawPrice = pricing.text();							
				rawPrice = rawPrice.replaceAll("[^0-9]", "");				 // odstrani pripadne blbosti, vcetne " Kč" na konci
				rawPrice = rawPrice.trim();
				if (!rawPrice.isEmpty()){									 // cena ve formatu "xxx,99 Kč" zustane jen cena v korunach
					rawPrice = rawPrice.substring(0, rawPrice.length() - 2); // odtrhneme halere
					price = Integer.parseInt(rawPrice);
					price = price + 1;										 // odtrhli jsme 99 haleru, ted pridame celou(!) korunu
				}
				
				// tyto udaje nejsou na strance uvedeny
				String manacost = "-";
				String type = "-";
				String cardText = "-";
				String rarity = "-";
				String version = "";
				String edition = "Neurčeno";
				
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
				
				if (name.contains("FOIL")){										// specialni karty
					String[] artifacts = name.split("FOIL");
					name = artifacts[0];
					version += "foil";
				}
				
				if(version.isEmpty()){
					version = "-";
				}
				
				name = name.replaceAll("´", "'");					// uprava nesrovnalosti
				name = name.replaceFirst("Aether|AEther", "Æther");
				name = name.replaceFirst("Aerathi|AErathi", "Ærathi");
														
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
				
				cards.add(temp);								// ulozeni do vysledku
				
			}			
		}
	}

}

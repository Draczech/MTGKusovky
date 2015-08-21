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
public class Mystic implements IParser {
	
	private final String SHOPNAME = "Mysticshop";
	private final String BASIC_PREFIX = "http://mysticshop.cz/mtgshop.php?name=";
	private final String ADV_SUFIX = "&of=";
	private final int IPP = 20;			//items per page
	private final int MAX_PAGES = 3;
	
	private boolean firstPage;
	private CardList cards;
	private String cardname;
	
	public Mystic(){
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
//			System.out.println(">>> Při přístupu na: " + SHOPNAME + " došlo k chybě:");
//			System.out.println(e.getMessage());
			throw e;
		}
		if (doc != null){
			
			if(firstPage){	
				Elements rcTable = doc.select("#content > p:nth-child(3)"); 
				if ((rcTable != null) && !rcTable.isEmpty()){					
					Element resultCounter = rcTable.first();
					
					if ((resultCounter == null) || (resultCounter.text().isEmpty())){
//						System.out.println(">>> " + SHOPNAME + " - Nebyla nalezena žádná odpovídající karta...");
						return;
					}
					
					String counter = resultCounter.text();						// Napr: Na našem skladě jsme našli podle Vašich kriterií 8 karet.
					
					if(counter.equalsIgnoreCase("Podle Vámi zadaných kriterií nebyly nalezeny žádné karty.")){
//						System.out.println(">>> " + SHOPNAME + " - Nebyla nalezena žádná odpovídající karta...");
						return;
					}
					
					String[] counterParts = counter.split(" ");
					if(counterParts.length < 9){
//						System.out.println(">>> " + SHOPNAME + " - Nebyla nalezena žádná odpovídající karta...");
						return;
					}
					int resultCount = Integer.parseInt(counterParts[8].trim());					// pozice poctu nalezenych
//					System.out.println("> Nalezeno výsledků: " + resultCount);
					int pagesCount = (int) resultCount / IPP;
					if ((resultCount % IPP) != 0){
						pagesCount ++;
					}
					
					String nextUri;
					
					firstPage = false;										// prvni stranka bude vyresena zbytkem metody, resime parsovani dalsich stranek
					
					if (pagesCount > MAX_PAGES){
						pagesCount = MAX_PAGES;										// omezeni poctu vysledku na 60 (3 stranky po 20)
					}
					
					for (int j = 1; j < pagesCount; j++){
						nextUri = BASIC_PREFIX + cardname + ADV_SUFIX + (IPP*j);
						parsePage(nextUri);
					}
				}				
			}			
			
			Elements table = doc.select("#content > form:nth-child(5) > table:nth-child(1) > tbody:nth-child(3)");
			
			if ((table == null) || (table.isEmpty())){
//				System.out.println(">>> " + SHOPNAME + " - Nebyla nalezena žádná odpovídající karta...");
				return;
			}
			
			Element tbody = table.first();
								
			Elements rows = tbody.select("tr");
			
			if ((rows == null) || (rows.isEmpty())){
//				System.out.println(">>> " + SHOPNAME + " - Nebyla nalezena žádná odpovídající karta...");
				return;
			}
			
			for (Element row : rows){
				
				Element detail = row.select("td.detail").first();
				
				Element header = detail.select("b").first();		// nazev karty
				String name = header.text();
								
				String parsedDetail = detail.html();
				parsedDetail = parsedDetail.replaceFirst("</b> <br />", "</b>-<br />");	// u zemi neni manacost, prazdny radek by byl nespravne odstranen
				parsedDetail = parsedDetail.replaceAll("(<br />\\s*)+", "\n");	
				parsedDetail = parsedDetail.replaceFirst(" \\(\\)", "");								// odstrani "()" v typu nekterych karet
				parsedDetail = parsedDetail.replaceAll("<b>.*</b>\\s*", "");							// odstrani nadpisy
				parsedDetail = parsedDetail.replaceAll("<img .* title=.* />\\s*", "");					// odstrani element s obrazkem edice
				parsedDetail = parsedDetail.replaceAll("<i>|</i>", "");									// odstrani oznaceni kurzivy
				parsedDetail = parsedDetail.replaceAll("<img src=\"img/mana/.\\.gif\" alt=\"|\" />", "");	// obrazky manasymbolu nahradi altem
				parsedDetail = parsedDetail.replaceAll("<img src=\"img/mana/..\\.gif\" alt=\"|\" />", "");	// obrazky manasymbolu nahradi altem
				parsedDetail = parsedDetail.replaceAll("<span class=\"mint\">|<span class=\"foil\">|</span>", "");				// odstrani elementy span pro "mint" a "foil", ponecha jen text
				parsedDetail = parsedDetail.replaceAll("[^\\p{ASCII}]+", "");
				parsedDetail = parsedDetail.replaceAll("&iacute;", "í");
				parsedDetail = parsedDetail.replaceAll("&amp;", "and");
				parsedDetail = parsedDetail.replaceAll("&eacute;", "é");
				
				
				String[] dividedDetail = parsedDetail.split("\n");
				String manacost = dividedDetail[0].trim();					// prvni polozka je manacost
					manacost = adjustManaCost(manacost);
					manacost = manacost.replaceAll("\\(", "\\{");			//uprava u pulene many
					manacost = manacost.replaceAll("\\)", "\\}");
				String edition = dividedDetail[1].trim();					// druha edice
				String type = dividedDetail[2].trim();						// treti typ
				String rarity = dividedDetail[3].trim();					// rarita
																			// ctvrta polozka je autor - nezkoumame
				String cardText = "";
				for (int i = 5; i < dividedDetail.length - 1; i ++){		// pata az predposledni je text karty
					cardText += dividedDetail[i] + "\n";
				}
				
				cardText = cardText.replaceAll("\\{\\(", "\\(");			// bordel v popisu karty
				
				int pos = type.indexOf('(');								// "(x/x)" v typu kreatur prehodi do cardText

				if (pos > -1){
					String creatureAttributes = type.substring(pos);
					creatureAttributes += "\n" + cardText;
					cardText = creatureAttributes;
					
					type = type.substring(0,pos);
				}
				
				
				String text = detail.text();
				String[] textParts = text.split("Kvalita:");
				String version = textParts[1].trim();						// posledni radka obsahuje kvalitu a prip. foilove provedeni karty
										
				Element countEl = row.select("td.count2").first();				
				int count = 0;
				String rawCount = countEl.text();								// v sedme pocet karet skladem
				rawCount = rawCount.replaceAll("[^0-9]", "");					// odstrani pripadne blbosti
				rawCount = rawCount.trim();
				if (!rawCount.isEmpty()){
					count = Integer.parseInt(rawCount);
				}
				
				
				Element priceEl = row.select("td.price2").first();				
				int price = 0;
				String rawPrice = priceEl.text();							// v seste cena
				rawPrice = rawPrice.replaceAll("[^0-9]", "");				// odstrani pripadne blbosti, vcetne " ,-" na konci
				rawPrice = rawPrice.trim();
				if (!rawPrice.isEmpty()){
					price = Integer.parseInt(rawPrice);
				}
				
				name = name.replaceAll("´", "'");					// uprava nesrovnalosti
				name = name.replaceFirst("Aether|AEther", "Æther");
				name = name.replaceFirst("Aerathi|AErathi", "Ærathi");
				
				edition = edition.replaceFirst("Buy-a-box", "Promo");
				edition = edition.replaceFirst("Ostatní promo karty", "Promo");
				edition = edition.replaceFirst("Pro Tour promos", "Promo");
				edition = edition.replaceFirst("Release Promos", "Promo");
				edition = edition.replaceFirst("Judge Gifts", "Promo");
				edition = edition.replaceFirst("Magic Rewards", "Promo");
				edition = edition.replaceFirst("FNM", "Promo");
				edition = edition.replaceFirst("Time Spiral Timeshifted", "Time Spiral");
				
				if (edition.startsWith("Duel Decks")){
					edition = edition.replaceFirst("vs ", "vs. ");
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
				
				cards.add(temp);								// ulozeni do vysledku
				
			}			
		}		
	}
	
	private String adjustManaCost(String manacost){
		String adjusted = "";
		String original = manacost;
		String temp = "";
		String part = "";
		
		while (original.contains("P")){							// phyrexian mana - spec. pripad
			int endIndex = original.length() - 2;					
			temp = original.substring(endIndex);				// retezec "XP" prevedeme na "{XP}"
			original = original.substring(0, endIndex);
			part = "{" + temp + "}";
			adjusted = part + adjusted;
		}
		
		adjusted = original + adjusted;	
//		System.out.println("O: " + manacost + ", A: " + adjusted);
		return adjusted.toUpperCase();
	}
	
}
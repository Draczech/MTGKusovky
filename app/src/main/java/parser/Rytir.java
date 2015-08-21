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
public class Rytir implements IParser{
	
	private final String SHOPNAME = "Černý Rytíř";
	private final String BASIC_PREFIX = "http://cernyrytir.cz/index.php3?akce=3&jmenokarty=";
	private final String BASIC_SUFIX = "&submit=Vyhledej";
	private final String ADV_PREFIX_TWO = "http://cernyrytir.cz/index.php3?akce=3&limit=30&jmenokarty=";
	private final String ADV_PREFIX_THREE = "http://cernyrytir.cz/index.php3?akce=3&limit=60&jmenokarty=";
	private final String ADV_SUFIX = "&edice_magic=libovolna&poczob=30&triditpodle=ceny&hledej_pouze_magic=1&submit=Vyhledej";
	private final int RPI = 3; 			//rows per item
	private final int IPP = 30;			//items per page
	private final int MAX_PAGES = 2;
	
	private boolean firstPage;
	private ArrayList<Card> cards;
	private String cardname;
	
	public Rytir(){
		this.cards = new ArrayList<Card>();
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
				
				Elements errorline = doc.select("table.kusovkytext:nth-child(3) > tbody:nth-child(1) > tr:nth-child(1) > td:nth-child(1)");
				if ((errorline != null) && !errorline.isEmpty()){
					String errorText = errorline.first().text();
					if(errorText.equalsIgnoreCase("Zvoleným kritériím neodpovídá žádná karta.")){						// v pripade chyby
//						System.out.println(">>> " + SHOPNAME + " - Nebyla nalezena žádná odpovídající karta...");
						return;
					}
				}
				
				
				Elements rcTable = doc.select("body > table:nth-child(2) > tbody:nth-child(1) > tr:nth-child(2) > td:nth-child(2) > table:nth-child(1) > tbody:nth-child(1) > tr:nth-child(1) > td:nth-child(1) > table:nth-child(3) > tbody:nth-child(1) > tr:nth-child(1) > td:nth-child(1) > span:nth-child(1)"); 
				if ((rcTable != null) && !rcTable.isEmpty()){									// Pokud je nalezeno vice nez 30 polozek, je na strance tabulka s poctem
					Element resultCounter = rcTable.get(0);
					
					if ((resultCounter == null) || (resultCounter.text().isEmpty())){
//						System.out.println(">>> " + SHOPNAME + " - Nebyla nalezena žádná odpovídající karta...");
						return;
					}
					
					String counter = resultCounter.text();		
										
					int resultCount = Integer.parseInt(counter.split(" ")[1]);			// pozice poctu nalezenych
//					System.out.println("> Nalezeno výsledků: " + resultCount);
					int pagesCount = (int) resultCount / IPP;
					if ((resultCount % IPP) != 0){
						pagesCount ++;
					}
									
					String nextUri;
					
					firstPage = false;										// prvni stranka bude vyresena zbytkem metody, resime parsovani dalsich stranek
					
					if (pagesCount > 1){
						nextUri = ADV_PREFIX_TWO + cardname + ADV_SUFIX;
						parsePage(nextUri);
					}
//					else if (pagesCount > MAX_PAGES){								// omezeni poctu stranek, vic vysledku k nicemu nevede
//						nextUri = ADV_PREFIX_TWO + cardname + ADV_SUFIX;
//						parsePage(nextUri);
//						nextUri = ADV_PREFIX_THREE + cardname + ADV_SUFIX;
//						parsePage(nextUri);
//					}
				}				
			}
						
			Elements table = doc.select("html > body > table > tbody > tr > td > table > tbody > tr > td > table.kusovkytext > tbody");
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
			
			int cardsCount = (int) rows.size()/RPI;
			
			for (int i = 0; i < cardsCount; i ++){
				int offset = i * RPI;
				
				Elements cells = rows.get(offset).select("td");		// vyber prvniho radku i-te karty
				Element td = cells.get(1);							// prvni je obrazek => chceme druhy				
				Elements tdContent = td.select("div");				// obsahuje popis karty jako title a nazev jako text elementu
				Element div = tdContent.first();
				String nameText = div.text();
				String name = "";
				String version = "-";
				
				nameText = nameText.replaceAll(" - ", "@");			// v pripade zvlastniho provedeni je toto uvedeno v nazvu karty
																	// a oddeleno pomlckou
				int pom = nameText.indexOf('@');					
				if (pom > -1){										
					name = nameText.substring(0, pom);
					version = nameText.substring(pom + 1);					
				}
				else{
					name = nameText;
				}
								
				String cardText = div.attr("title");
				cardText = cardText.replaceAll("\\(\\(", "\\(");		// cisteni textu
				cardText = cardText.replaceAll("\\)\\)", "\\)");
				cardText = cardText.replaceAll("\n+", "\n");			// odstrani prebytecne prazdne radky
				cardText = cardText.replaceAll("[^\\p{ASCII}]+", "");
				
				
				td = cells.get(2);									// druha cast radku obsahuje manacost karty
				String manaHtml = td.html();
				manaHtml = manaHtml.replaceAll("&nbsp;","");		// odstrani non-breaky
				manaHtml = manaHtml.replaceAll("<img src=\"/images/kusovky/|.gif\" />", "");	// z obrazku manasymbolu vypreparuje typ many 
				
				
				if (manaHtml.trim().length() < 1){
					manaHtml = "-";									// u zemi je manacost prazdny
				}
				String manacost = adjustManaCost(manaHtml);
				
				cells = rows.get(offset+1).select("td");			// druhy radek i-te karty
				td = cells.get(0);									// obsahuje edici karty
				String edition = td.text();
				td = cells.get(1);									// obsahuje typ karty
				String type = td.text();
				
				cells = rows.get(offset+2).select("td");			// treti radek i-te karty
				td = cells.get(0);									// obsahuje raritu karty
				String rarity = td.text();
				td = cells.get(1);									// obsahuje pocet kusu skladem
				
				int count = 0;
				String rawCount = td.text();							
				rawCount = rawCount.replaceAll("[^0-9]", "");		// odstrani pripadne blbosti, vcetne " ks" na konci
				rawCount = rawCount.trim();
				if (!rawCount.isEmpty()){
					count = Integer.parseInt(rawCount);
				}
				
				
				td = cells.get(2);									// obsahuje cenu karty
				int price = 0;
				String rawPrice = td.text();							
				rawPrice = rawPrice.replaceAll("[^0-9]", "");		// odstrani pripadne blbosti, vcetne " Kč" na konci
				rawPrice = rawPrice.trim();
				if (!rawPrice.isEmpty()){
					price = Integer.parseInt(rawPrice);
				}
				
				if (name.contains(" (")){										// specialni karty
					String[] artifacts = name.split(" \\(");
					name = artifacts[0];
					version = artifacts[1];
					version = version.replaceFirst("\\)", "");
				}				
				
																	// uprava nesrovnalosti
				name = name.replaceFirst("Aether|AEther", "Æther");
				name = name.replaceFirst("Aerathi|AErathi", "Ærathi");
				
				edition = edition.replaceFirst("Alpha", "Limited Edition Alpha");
				edition = edition.replaceFirst("Beta", "Limited Edition Beta");
				edition = edition.replaceFirst("Unlimited", "Unlimited Edition");
				edition = edition.replaceFirst("3rd", "Revised");
				edition = edition.replaceFirst("4th", "Fourth");
				edition = edition.replaceFirst("5th", "Fifth");
				edition = edition.replaceFirst("6th", "Classic Sixth");
				edition = edition.replaceFirst("7th", "Seventh");
				edition = edition.replaceFirst("8th", "Eighth");
				edition = edition.replaceFirst("9th", "Ninth");
				edition = edition.replaceFirst("10th", "Tenth");
				if (!edition.equalsIgnoreCase("Return to Ravnica")){
					edition = edition.replaceFirst("Ravnica", "Ravnica: City of Guilds");
				}
				edition = edition.replaceFirst("P - Miscellaneous", "Promo");
				edition = edition.replaceFirst("P - Prerelease, Release", "Promo");
				edition = edition.replaceFirst("P - GP, PT, JSS", "Promo");
				edition = edition.replaceFirst("P - Judge Rewards", "Promo");
				edition = edition.replaceFirst("P - Arena", "Promo");
				edition = edition.replaceFirst("P - Player Rewards", "Promo");
				edition = edition.replaceFirst("P - Friday Night Magic", "Promo");
				edition = edition.replaceFirst("PD: Fire", "PDS: Fire");
				edition = edition.replaceFirst("Timeshifted", "Time Spiral");

								
				Card temp = new Card();								// ulozeni do pole
				temp.setName(name.trim());
				temp.setType(type.trim());
				temp.setManacost(manacost.trim());
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
	
	/**
	 * Uprava manacostu u vybranych karet. Resi tzv. pulene many
	 * @param manacost retezec obsahjici nestandard. zapis
	 * @return upraveny manacost
	 */
	private String adjustManaCost(String manacost){
		String adjusted = "";
		String original = manacost;
		String temp = "";
		String part = "";
		
		while (original.contains("p")){							// phyrexian mana - spec. pripad
			int endIndex = original.length() - 2;					
			temp = original.substring(endIndex);				// retezec "PX" prevedeme na "XP"
			original = original.substring(0, endIndex);
			part = "{" + temp.charAt(1) + temp.charAt(0) + "}";
			adjusted = part + adjusted;
		}
		
		while (original.endsWith("_mana")){
			int endIndex = original.length() - 7;					
			temp = original.substring(endIndex);				// retezec "xx_mana"
			original = original.substring(0, endIndex);
			part = "{" + temp.charAt(0) + "/" + temp.charAt(1) + "}";
			adjusted = part + adjusted;
		}
		
		while (original.contains("/")){
			int endIndex = original.lastIndexOf("/") - 1;					
			temp = original.substring(endIndex);				// retezec "x/x"
			original = original.substring(0, endIndex);
			part = "{" + temp + "}";
			adjusted = part + adjusted;
		}
		
		
		adjusted = original + adjusted;	
//		System.out.println("O: " + manacost + ", A: " + adjusted);
		return adjusted.toUpperCase();
	}
}

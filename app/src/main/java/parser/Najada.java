package parser;

import java.io.IOException;
import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import implementation.Card;
import implementation.CardList;
import implementation.ShopInfo;


/**
 * 
 * @author Milan Nikl
 *
 */
public class Najada implements IParser {
	
	private final String SHOPNAME = "Najáda";
	private final String BASIC_PREFIX = "http://www.najada.cz/cz/kusovky-mtg/omezit-";
	private final String BASIC_SUFIX = "/?Search=";
	private final int CARD_LIMIT = 56;			// mozno nastavit na: 8, 28, 56, 84
												// misto prochazeni nekolika stranek je lepsi nacist jen jednu se zadanym poctem karet
	
	private final String PLAYED = "Played";
	
	private CardList cards;
	private String cardname;
	
	
	public Najada(){
		this.cards = new CardList();
	}
	
	public String getShopName(){
		return this.SHOPNAME;
	}
	
	public ArrayList<Card> fetchPrices(String fetched) throws Exception{
		cardname = fetched;
		
		String uri = BASIC_PREFIX;
		uri += CARD_LIMIT + BASIC_SUFIX;
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
			
			Elements table = doc.select(".tabArt > tbody:nth-child(1)");
			
			if ((table == null) || (table.isEmpty())){
//				System.out.println(">>> " + SHOPNAME + " - Nebyla nalezena žádná odpovídající karta...");
				return;
			}
			
			Element tbody = table.first();
					
			Elements rows = tbody.select("tr:not(.rH)");			// vsechny radky krome headeru
			
			
			
			if ((rows == null) || (rows.isEmpty())){
//				System.out.println(">>> " + SHOPNAME + " - Nebyla nalezena žádná odpovídající karta...");
				return;
			}
			
			for (Element row : rows){
				
//				System.out.println(row.html());
				
				Element tdTitle = row.select("td.tdTitle").first();
					String name = tdTitle.text();
					
				Element tdFoil = row.select("td.tdFoil").first();
					String foil = tdFoil.text();
				
				Element tdManaCost = row.select("td.tdManaCost").first();
					String manaHtml = tdManaCost.html();
					manaHtml = manaHtml.replaceAll(".gif\" />", "\n");							//nahrazeni obrazku many prislusnym znakem
					manaHtml = manaHtml.replaceAll("<img class=\"imgColor\" title=\"", "");
					manaHtml = manaHtml.replaceAll("\" alt=\".*", "");
					manaHtml = manaHtml.replaceAll("\" src=\"http://data.najada.cz/magic/symbol/symbol-", "");
					manaHtml = manaHtml.replaceAll("Variable Colorless|variable colorless","X");	// libovolna bezbarva mana ma symbol "X" - nejjednodussi uprava symboliky
					manaHtml = manaHtml.replaceAll("blue|Blue", "Ultramarine");						// chyba v symbolice - B znamena cernou, U modrou
					manaHtml = manaHtml.replaceAll("two|Two", "2");									// stejny problem
					
					String manacost = "";
					String[] manaParts = manaHtml.split("\n");
					for (int i = 0; i <manaParts.length; i ++){
						String[] multipleWords = manaParts[i].split(" ");
						if (multipleWords.length == 3){											// spec. pripad, kdy je mana "Neco or Neco" - pulene many
							manacost += "{" + multipleWords[0].charAt(0) + "/" + multipleWords[2].charAt(0) + "}";
						}
						else if (multipleWords.length == 2){									// spec pripad typu "Phyrexian Black" apod. - phyrexian mana
							manacost += "{" + multipleWords[1].charAt(0) + multipleWords[0].charAt(0) + "}";
						}
						else{
							if (manaParts[i].length() > 0){
								manacost += manaParts[i].charAt(0);
							}
						}						
					}
					
					if (manacost.length() > 0){
						manacost = manacost.toUpperCase();
					}
					else{
						manacost = "-";
					}
									
				Element tdType = row.select("td.tdType").first();
					String type = tdType.text();
				
				Element tdRarity = row.select("td.tdRarity").first();
					String rarity = tdRarity.text();
					
				Element tdEdition = row.select("td.tdEdition").first();
					String edition = tdEdition.text();
					
				Element tdPrice = row.select("td.tdPrice").first();								// normalni cena a dostupnost
					
					int price = 0;
					String rawPrice = tdPrice.select("div.stateNew > span.v").text();
					rawPrice = rawPrice.replaceAll("[^0-9]", "");		// odstrani pripadne blbosti, vcetne " Kč" na konci
					rawPrice = rawPrice.trim();
					if (!rawPrice.isEmpty()){
						price = Integer.parseInt(rawPrice);
					}
					
					int count = 0;
					String rawCount = tdPrice.select("div.stateNew > span.AviNo, div.stateNew > span.AviYes").text();
					rawCount = rawCount.replaceAll("[^0-9]", "");		// odstrani pripadne blbosti, vcetne " ks" na konci
					rawCount = rawCount.trim();
					if (!rawCount.isEmpty()){
						count = Integer.parseInt(rawCount);
					}
				
				Element playedTdPrice = row.select("td.tdPrice").get(1);										// specialni cena a dostupnost
					String playedRawPrice = playedTdPrice.select("div.statePlayed > span.v").text();
					playedRawPrice = playedRawPrice.replace(" ", "");
					int playedPrice = Integer.parseInt(playedRawPrice.trim());
					
					String playedRawCount = playedTdPrice.select("div.statePlayed > span.AviNo, div.statePlayed > span.AviYes").text();
					int playedCount = Integer.parseInt(playedRawCount.trim());
				
				
				String version = "";
				String playedVersion = PLAYED;
				
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
				
				if (foil.equals("Ne")){
					version = "-";
				}
				else {
					version += "foil";
					playedVersion += "foil";
				}
				
				name = name.replaceAll("´", "'");					// uprava nesrovnalosti
				name = name.replaceFirst("Aether|AEther", "Æther");
				name = name.replaceFirst("Aerathi|AErathi", "Ærathi");
				
				edition = edition.replaceFirst("3rd", "Revised");
				edition = edition.replaceFirst("4th", "Fourth");
				edition = edition.replaceFirst("5th", "Fifth");
				edition = edition.replaceFirst("6th", "Classic Sixth");
				edition = edition.replaceFirst("7th", "Seventh");
				edition = edition.replaceFirst("8th", "Eighth");
				edition = edition.replaceFirst("9th", "Ninth");
				edition = edition.replaceFirst("10th", "Tenth");
				edition = edition.replaceFirst("Core Set", "");
				edition = edition.replaceFirst("Promo karty", "Promo");
				edition = edition.replaceFirst("Time Spiral \"Timeshifted\"", "Time Spiral");
				
				Card temp = new Card();								
				temp.setName(name.trim());
				temp.setManacost(manacost.trim());			
				temp.setType(type.trim());
				
				ShopInfo info = new ShopInfo(this.SHOPNAME);
				info.setEdition(edition.trim());
				info.setRarity(rarity.trim());
				info.setVersion(version.trim());
				info.setCount(count);
				info.setPrice(price);
				
				temp.getAvailability().add(info);
				
				if (playedCount > 0){							// je skladem karta v hranem stavu
					info = new ShopInfo(this.SHOPNAME);
					info.setEdition(edition.trim());
					info.setRarity(rarity.trim());
					info.setVersion(playedVersion.trim());
					info.setCount(playedCount);
					info.setPrice(playedPrice);
					
					temp.getAvailability().add(info);
					
				}
				
				cards.add(temp);								// ulozeni do vysledku
			
			}		
		}
	}

}

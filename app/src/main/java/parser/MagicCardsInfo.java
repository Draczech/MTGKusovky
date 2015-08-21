package parser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.os.Environment;
import android.preference.PreferenceManager;

import implementation.CardList;
import implementation.Card;

/**
 * 
 * @author Milan Nikl
 *
 */
public class MagicCardsInfo  {
	
	private final String SHOPNAME = "MagicCards.info";
	private final String BASIC_PREFIX = "http://magiccards.info/query?q=";
	private final String BASIC_SUFIX = "&v=card&s=cname";
	private final String ADV_SUFIX = "&v=card&s=cname&p=";
	private final int IPP = 20;
	private final int MAX_PAGES = 3;
	
	private final String IMG_EXTENSION = ".jpg";
	private final String IMG_FOLDER = "MTG_Kusovky";
		
	private boolean firstPage;
	private boolean inclImages;
	private CardList cards;
	private String cardname;
	
	public MagicCardsInfo(){
		this.cards = new CardList();
		this.firstPage = true;
	}
	
	public String getShopName(){
		return this.SHOPNAME;
	}
	
	public ArrayList<Card> fetchPrices(String fetched, boolean images) throws Exception {
		cardname = fetched;
		inclImages = images;
		
		String uri = BASIC_PREFIX;
		uri += cardname;
		uri += BASIC_SUFIX;
		
		try{
			parsePage(uri);
		}
		catch (Exception ex){
			throw ex;
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
//			System.out.println(">>> Při přístupu na: " + PAGENAME + " došlo k chybě:");
//			System.out.println(e.getMessage());			
			throw e;
		}
		if (doc != null){
			
			if(firstPage){	
				Elements rcTable = doc.select("body > table:nth-child(5) > tbody:nth-child(1) > tr:nth-child(1) > td:nth-child(3)"); 
				if ((rcTable != null) && !rcTable.isEmpty()){					
					Element resultCounter = rcTable.first();
					
					if ((resultCounter == null) || (resultCounter.text().isEmpty())){
//						System.out.println(">>> " + PAGENAME + " - Nebyla nalezena žádná odpovídající karta...");
						return;
					}
					
					String counter = resultCounter.text();						// Napr: 1500 cards
					
					counter = counter.replaceAll("[[^0-9]&&[^ ]]", "");
					
					counter = counter.trim();
					
					if(!counter.isEmpty()){
						int resultCount = Integer.parseInt(counter);					
//						System.out.println("> Nalezeno výsledků: " + resultCount);
						int pagesCount = (int) resultCount / IPP;
						if ((resultCount % IPP) != 0){
							pagesCount ++;
						}
										
						String nextUri;
						
						firstPage = false;												// prvni stranka bude vyresena zbytkem metody, resime parsovani dalsich stranek
						
						if (pagesCount > MAX_PAGES){
							pagesCount = MAX_PAGES;										// omezeni poctu vysledku na 60 (3 stranky po 20)
						}
						
						for (int j = 2; j < pagesCount + 1; j++){
							nextUri = BASIC_PREFIX + cardname + ADV_SUFIX + j;
							parsePage(nextUri);
						}						
					}
					
				}				
			}
			
			Elements tables = doc.select("body > table");						// prvni dve tabulky jsou navigace, az dalsi jsou karty
																				// posledni je taky navigace
			if ((tables == null) || (tables.isEmpty()) || tables.size() < 3){
//				System.out.println(">>> " + PAGENAME + " - Nebyla nalezena žádná odpovídající karta...");
				return;
			}
						
			for (int i = 2; i < tables.size() - 1; i ++){
				
				Element table = tables.get(i);
				
				Element content = table.select("tbody > tr").first();
								
				Element infoPart = content.select("td").get(1);					// v prvni bunce je obrazek, ve druhe informace o karte
				
				Element header = infoPart.select("span").first();				
				String name = header.text();
				name = name.replaceAll("/", "X");
				
				Element flag = header.select("img").first();
				
				String language = flag.attr("alt");
				
				if (!language.equalsIgnoreCase("English")){						// nalezena karta neni anglicky, takze nejde spravne naparsovat
					break;														// rovnez by mela byt az na konci seznamu, takze muzeme prestat
				}
				
				Elements infos = infoPart.select("p");
				
				Element extendedType = infos.get(0);							// prvni je typ, manacost				
					String[] artifacts = extendedType.text().split(",");				
					String type = artifacts[0];
					String manacost = "-";
					if (artifacts.length > 1){										// u zemi apod. neni manacost
						manacost = artifacts[1];
						manacost = manacost.replaceAll("\\(.*\\)", "");				// odstrani converted manacost a tak
					}
				
				Element cardText = infos.get(1);								// druhy je text karty
					String text = "";
					String pokus = cardText.html();
					pokus = pokus.replaceAll("<br />", "\n");					// nahrazeni odradkovani je trochu slozitejsi
					pokus = pokus.replaceAll("<b>|</b>", "");					// takto vypada vystup lepe a zaroven jsou zachovany vsechny informace
					pokus = pokus.replaceAll("\n+", "\n");
					
					
					String[] lines = pokus.split("\n");							// odstraneni pripadnych html entit
					for (String line: lines){
						text += Jsoup.parse(line).text();
						text += "\n";
					}
					
					text = text.substring(0, text.length() - 1);							// odstrani prebytecne konce radek
					
				Element flavorText = infos.get(2);								// treti je flavor
					String flavor = flavorText.text();
					flavor = flavor.replaceAll("\n+", "\n");
				
				Element artistName = infos.get(3);								// ctvrte je jmeno ilustratora
					String artist = artistName.text();
					artist = artist.replaceFirst("Illus. ", "");				// neni treba
				
				Elements otherInfo = infoPart.select("ul");						// pripadny seznam s dodatky a seznam s legalitou karty
				
					String rulings = "";
					String legality = "";
					
					if (otherInfo.size() > 1){
						Element rules = otherInfo.get(0);
						Elements items = rules.select("li");
						
						for (Element item: items){
							String itemText = item.text().trim();
							rulings += itemText + "\n";
						}
						
						Element legalityList = otherInfo.get(1);
						items = legalityList.select("li");
						
						for (Element item: items){
							String itemText = item.text().trim();
							legality += itemText + "\n";
						}
					}
					else if (otherInfo.size() == 1){
						Element legalityList = otherInfo.get(0);
						Elements items = legalityList.select("li");
						
						for (Element item: items){
							String itemText = item.text().trim();
							legality += itemText + "\n";
						}
					}
				
				String picturePath = "";									//pokud se nebudou nacitat obrazky, nebude se menit
				
				if (inclImages){											// nacitani obrazku
				
					Element picturePart = content.select("td").first();		// v prvni bunce
					Element picture = picturePart.select("img").first();					
					String pictureURI = picture.attr("src");
					String alt = picture.attr("alt");
					
					try {
						picturePath = imageExists(name);
						if (picturePath.isEmpty()){
							picturePath = retrievePicture(pictureURI, alt, name);
						}
					}
					catch (Exception e){
						throw e;
					}
					// nalezeni alternativnich obrazku
					// vybere vsechny variace z aktualni edice, ale pouze prvni z ostatnich edic
					// pro vetsinu karet to staci bohate
					
					/*
					
						Element rightCell = content.select("td").get(2);						// ve tretim sloupecku jsou udaje o ruznych verzich karty
						Element small = rightCell.select("small").first();
						Elements links = small.select("a");
						
						int j = 2;		// u jmen obrazku zaciname od 2, protoze prvni uz je zpracovan			
						
						for (Element link: links){
							String extraName = name + "_" + j;
							String extraURI = link.absUrl("href");
							if (extraURI.contains("/en/")){											// pouze anglicke obrazky karet
								extraURI = extraURI.replaceFirst("/en/", "/");
								extraURI = extraURI.replaceFirst("info/", "info/scans/en/");		// odkaz na stranku a obrazek se lisi - je treba upravit
								extraURI = extraURI.replaceFirst("html", "jpg");
								retrievePicture(extraURI, alt, extraName);
							}
							else{
								break;
							}
							
							j++;
						}
					
					*/
					
				}	
					
				String editions = "";
				
				Element rightCell = content.select("td").get(2);						// ve tretim sloupecku jsou udaje o ruznych verzich karty
				Element small = rightCell.select("small").first();
				
				Element nextSmall = small.nextElementSibling();							// pro specialni vyjimky
				if (nextSmall != null){
					nextSmall = nextSmall.select("small").first();
					if (nextSmall != null) {
						small = nextSmall;
					}
				}
				
				
				String smallText = small.text();
				smallText = smallText.replaceFirst(".* Editions:", "");
				smallText = smallText.replaceFirst("Languages:.*", "");
				smallText = smallText.replaceAll("\\)+", "\\)");
				smallText = smallText.replaceAll(" \\(", " - ");
				smallText = smallText.trim();
				String[] editionItems = smallText.split("\\)");
				
				
				String latestRarity = "-"; 
				if (editionItems.length > 0){
					String[] rarityParts = editionItems[0].split(" - ");
					if (rarityParts.length > 1){
						latestRarity = rarityParts[1];
					}
				}
				
				for (int j = 0; j < editionItems.length; j++){
					editions += editionItems[j].trim() + "\n";
				}
				
				editions = editions.replaceAll("\n+", "\n");
				
				
				Card found = new Card();
				found.setName(name.trim());
				found.setType(type.trim());
				found.setArtist(artist.trim());
				found.setCardText(text.trim());
				found.setFlavor(flavor.trim());
				found.setManacost(manacost.trim());
				found.setPicture(picturePath);
				found.setRulings(rulings.trim());
				found.setLegality(legality.trim());
				found.setEditions(editions.trim());
				found.setLastRarity(latestRarity.trim());
				
				cards.add(found);
				
				
			}
		}
	}
	
	private String retrievePicture(String pictureURI, String alt, String name) throws Exception {
		String path = "";							// cesta k ulozenemu souboru
		Response resultImageResponse = null;
		
		try {
			resultImageResponse = Jsoup.connect(pictureURI).ignoreContentType(true).execute();
		} catch (IOException e) {
//			System.out.println("Nepodařilo se načíst obrázek pro:" + alt);
//			System.out.println("Došlo k chybě:");
//			System.out.println(e.getMessage());
		}
		
		if(resultImageResponse != null){					
			 FileOutputStream out;
			try {
				File folder = new File(Environment.getExternalStorageDirectory() + "/" + IMG_FOLDER);
				boolean success = true;
				if (!folder.exists()) {
				    success = folder.mkdir();
				}
				if (success){
					File img = new File(folder.getAbsolutePath(),name + IMG_EXTENSION);		//jmeno karty a souboru by melo sedet
					out = (new FileOutputStream(img));
					out.write(resultImageResponse.bodyAsBytes());           // resultImageResponse.body() obsahuje obrazek
				    out.close();
				    
				    path = img.getAbsolutePath();
				}
				else{
//					System.out.println("Nepodařilo se vytvořit adresář: " + IMG_FOLDER);
					Exception pom = new Exception("Nepodařilo se vytvořit adresář: " + IMG_FOLDER);
					throw pom;
				}
			} catch (FileNotFoundException e) {
//				System.out.println("Nepodařilo se uložit obrázek pro:" + alt);
//				System.out.println("Došlo k chybě:");
//				System.out.println(e.getMessage());
				throw e;
			} catch (IOException exc) {
//				System.out.println("Nepodařilo se uložit obrázek pro:" + alt);
//				System.out.println("Došlo k chybě:");
//				System.out.println(e.getMessage());
				throw exc;
			}
		}
		
		return path;
	}
	
	private String imageExists(String name) throws FileNotFoundException{
		File folder = new File(Environment.getExternalStorageDirectory() + "/" + IMG_FOLDER);
		if (folder.exists() && folder.isDirectory()) {
			File img = new File(folder.getAbsolutePath(),name + IMG_EXTENSION);		//jmeno karty a souboru by melo sedet
			
			if (img.isFile()){
				return img.getAbsolutePath();
			}
		}
		return "";
	}

}

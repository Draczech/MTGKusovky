package parser;

import java.util.ArrayList;

import implementation.Card;

/**
 * Interface for various parsers for particular shops.
 * @author Milan Nikl
 *
 */
public interface IParser {
	
	public ArrayList<Card> fetchPrices(String fetched) throws Exception;
	
	public String getShopName();

}

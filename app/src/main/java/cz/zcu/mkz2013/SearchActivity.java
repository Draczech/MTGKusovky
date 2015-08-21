package cz.zcu.mkz2013;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import parser.*;
import implementation.CardList;

import android.util.Log;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.Toast;

/**
 * Start activity of the app. Creates queries from input text. Then shows results 
 * @author Milan Nikl
 * 
 */
public class SearchActivity extends Activity {
	// list of results found
	private CardList results;
	// list of results just from shops
	private CardList fromShops;
	// parsers for respective pages
	private ArrayList<IParser> parsers;
	// exception list
	private ArrayList<Exception> exceptions = new ArrayList<Exception>();
	// canceled parsers list
	private ArrayList<String> canceledList = new ArrayList<String>();
	// parser tasks list
	private ArrayList<FetchPricesTask> tasks;
	
	private static String cardName;
	// is currently searching
	private boolean searching = false;
	// all checklists checked
	private boolean allChecked = false;
	// activity paused
	private boolean paused = false;
	// search finished
	private static boolean finished = true;
	// progress display
	public ProgressDialog progress;
	public ProgressBar pb;
	
	private SharedPreferences preferences;
	// history of searched card names
	private Set<String> history;
	private final static int HISTORY_SIZE = 50;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search);
		
		// dismisses dialog
		destroyDialog();		
				
		// if not running for the first time sets progress and checklist states
		if (savedInstanceState != null){
			int pom = savedInstanceState.getInt("currentProgress");
			int max = savedInstanceState.getInt("currentMax");
			pb = (ProgressBar) findViewById(R.id.SearchProgress);
			if (pb != null){
				pb.setVisibility(View.VISIBLE);
				pb.setMax(max);
				if (max > pom){
					pb.setProgress(pom);
				}				
			}
			
			allChecked = savedInstanceState.getBoolean("allChecked");
			searching = savedInstanceState.getBoolean("searching");
			finished = savedInstanceState.getBoolean("finished");
			
			if (searching){
				progress = createDialog();
				progress.show();
			}
			if (finished){
				destroyDialog();
				searching = false;
			}							
		}
		
		// autocomplete with history
		attachAdapterToAutoComplete();			
	}
	
	@Override
	public void onDestroy() {
		
		// Stops running Asynctasks (if any)
		if (searching == true){
			for (FetchPricesTask fpt:tasks){
				fpt.cancel(true);
			}
		}		
		
		destroyDialog();		
	    super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.search, menu);
		return super.onCreateOptionsMenu(menu);

	}
	
	/**
	 * On selecting option in the action bar creates appropriate flag
	 */	
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {		
		case R.id.action_settings:
			startActivity(new Intent(this, SettingsActivity.class));
		}
		return true;
	}
	
	@Override
    public void onSaveInstanceState(Bundle outState) {    
		
		// Saves environment variables and states
        if (pb != null){
        	outState.putInt("currentProgress", pb.getProgress());
        	outState.putInt("currentMax", pb.getMax());
        }
        
        outState.putBoolean("searching", searching);
        outState.putBoolean("finished", finished);
        outState.putBoolean("allChecked", allChecked);
        
        super.onSaveInstanceState(outState);
        
    }

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		destroyDialog();
		
		super.onRestoreInstanceState(savedInstanceState);
		
		// Retrieves environment variables and states
		int pom = savedInstanceState.getInt("currentProgress");
		int max = savedInstanceState.getInt("currentMax");
		pb = (ProgressBar) findViewById(R.id.SearchProgress);
		if (pb != null){
			pb.setMax(max);
			if (max > pom){
				pb.setProgress(pom);
			}				
		}
		allChecked = savedInstanceState.getBoolean("allChecked");
		searching = savedInstanceState.getBoolean("searching");
		finished = savedInstanceState.getBoolean("finished");
		
		// displays dialog if needed, closes it if not
		if (searching){
			progress = createDialog();
			progress.show();
		}
		if (finished){
			searching = false;
			destroyDialog();
		}	
		
		attachAdapterToAutoComplete();
	}
	
	@Override
	public void onPause() {
		
		// Saves environment state
		if (searching && (progress != null)){
			if (progress.isShowing()){
				paused = true;
			}
		}
		destroyDialog();
	    super.onPause();
	}	
	
	@Override
	public void onResume() {
		
		// displays dialog if needed, closes it if not
		if (paused){
			progress = createDialog();
			progress.show();
			paused = false;
		}
		if (finished){
			searching = false;
			destroyDialog();
		}
		
		attachAdapterToAutoComplete();
		
	    super.onResume();
	}
	
	@Override
	public void onStop() {
		destroyDialog();
		
		pb = (ProgressBar) findViewById(R.id.SearchProgress);
		if (pb != null){
			pb.setVisibility(View.INVISIBLE);
		}
				
		searching = false;
		
		// deletes content of the text view
		AutoCompleteTextView actw = (AutoCompleteTextView) findViewById(R.id.CardNameAutoComplete);
		actw.setText("");
	    super.onStop();
	}
	
	/**
	 * Activated by button. Processes input, runs parsers
	 * @param v starting button
	 */
	public void startSearch(View v) {

		results = new CardList();
		fromShops = new CardList();
		
		finished = false;
		
		// creates dialog, blocks UI
		progress = createDialog();

		// initial input
		AutoCompleteTextView actw = (AutoCompleteTextView) findViewById(R.id.CardNameAutoComplete);
		String input = actw.getText().toString().trim();

		// normalizing, transformating to url compatible format
		String decomposed = Normalizer.normalize(input, Normalizer.Form.NFD);
		decomposed = decomposed.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
		decomposed = decomposed.replaceAll("[^A-Za-z0-9 '-]", "");
		input = encodeURIcomponent(decomposed);
		
		// processed input
		cardName = input;
		
		// connection check
		if (!isOnline()){
			Toast.makeText(getApplicationContext(),	getString(R.string.no_access), Toast.LENGTH_SHORT).show();
		}
		
		else {

			// no proper input
			if (cardName.isEmpty()) {
				Toast.makeText(getApplicationContext(),	getString(R.string.no_name), Toast.LENGTH_SHORT).show();
			} else {
				
				// include in search history
				addToHistory(decomposed);
	
				// podle obchodu se prida parser
				parsers = new ArrayList<IParser>(); 
	
				if (((CheckBox) findViewById(R.id.RytirCheckBox)).isChecked()) {
					parsers.add(new Rytir());
				}
				if (((CheckBox) findViewById(R.id.MysticCheckBox)).isChecked()) {
					parsers.add(new Mystic());
				}
				if (((CheckBox) findViewById(R.id.NajadaCheckBox)).isChecked()) {
					parsers.add(new Najada());
				}
				if (((CheckBox) findViewById(R.id.RishadaCheckBox)).isChecked()) {
					parsers.add(new Rishada());
				}
				if (((CheckBox) findViewById(R.id.TolarieCheckBox)).isChecked()) {
					parsers.add(new Tolarie());
				}
				if (((CheckBox) findViewById(R.id.LotusCheckBox)).isChecked()) {
					parsers.add(new Lotus());
				}
				if (((CheckBox) findViewById(R.id.TopMagicCheckBox)).isChecked()) {
					parsers.add(new TopMagic());
				}
	
				// make progressbar visible
				pb = (ProgressBar) findViewById(R.id.SearchProgress);
				pb.setVisibility(View.VISIBLE);
				pb.setMax(parsers.size() + 1);
				pb.setProgress(0);
	
				// task for magiccards info
				FetchCardsTask mciTask = new FetchCardsTask();
				MagicCardsInfo[] pom = { new MagicCardsInfo() };
				mciTask.execute(pom);
	
				// creating Asynctasks for fetching prices
				tasks = new ArrayList<FetchPricesTask>();
	
				for (int i = 0; i < parsers.size(); i++) {
					tasks.add(i, new FetchPricesTask());
					IParser[] temp = { parsers.get(i) };
					tasks.get(i).execute(temp);
				}
	
				progress.show();
				searching = true;
			}
		}
	}

	/**
	 * Once searching is done it's possible to add all items to one list.
	 */
	private void moveOn() {
		// add availability from shops to info from magiccards.info
		results.insertCards(fromShops); 
		results.sort();
		
		// "list" of canceled or error tasks
		String canceled = "";		
		
		if (!canceledList.isEmpty()){
			canceled = getString(R.string.canceled_tasks);
			for (String s: canceledList){
				canceled += s;
				canceled += ", ";
			}
			
			Toast.makeText(getApplicationContext(),	canceled, Toast.LENGTH_LONG).show();
		}
		
		// exception check for debugging
		if (!exceptions.isEmpty()){
			for (Exception es: exceptions){
				Log.e("CHYBA", "V aktivite: " + this.getClass().getName(), es);
			}		
		}
		
		// save results and move to next activity
		Intent i = new Intent(this, ResultsListActivity.class);
		i.putExtra("results", results);	
		searching = false;
		destroyDialog();
		startActivity(i);		
		
		// clearing resources
		canceledList.clear();
		exceptions.clear();
	}
	
	/**
	 * Checks for Internet connection availability.
	 * @return true if connected, false otherwise
	 */
	public boolean isOnline() {
	    ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo netInfo = cm.getActiveNetworkInfo();
	    if (netInfo != null && netInfo.isConnectedOrConnecting()) {
	        return true;
	    }
	    return false;
	}

	/**
	 * Serves for updating progress bar.
	 * But still has some flaws.
	 */
	public synchronized void addProgress() {
		pb = (ProgressBar) findViewById(R.id.SearchProgress);
		if (pb.getProgress() < pb.getMax()) {
			pb.incrementProgressBy(1);
		}

		if (pb.getProgress() == pb.getMax()) {
			destroyDialog();
			searching = false;
			finished = true;
			moveOn();
		}
		pb.refreshDrawableState();
	}
	
	/**
	 * Creates progress dialog to block UIthread.
	 * And inform the user he has to wait (of course).
	 * @return view of the dialog
	 */
	private ProgressDialog createDialog(){
		ProgressDialog temp = new ProgressDialog(this);
		temp.setTitle(getString(R.string.loading));
		temp.setMessage(getString(R.string.data) + "\n"
				+ getString(R.string.warning_slow));
		return temp;
		
	}
	
	/**
	 * Destroys progress dialog view and object.
	 */
	private void destroyDialog(){
		if (progress != null){
			if (progress.isShowing()){
				progress.dismiss();
			}
			progress = null;
		}				
	}

	/**
	 * Transforms normal string to URI safe string.
	 * @param s original string
	 * @return transformaed string
	 */
	private String encodeURIcomponent(String s) {
		StringBuilder o = new StringBuilder();
		for (char ch : s.toCharArray()) {
			if (isUnsafe(ch)) {
				o.append('%');
				o.append(toHex(ch / 16));
				o.append(toHex(ch % 16));
			} else
				o.append(ch);
		}
		return o.toString();
	}

	/**
	 * Returns hexa value of a char.
	 * @param ch char to encode
	 * @return hexadecimal value
	 */
	private char toHex(int ch) {
		return (char) (ch < 10 ? '0' + ch : 'A' + ch - 10);
	}

	/**
	 * Checks for special characters
	 * @param ch examined character
	 * @return true if special char, false if normal
	 */
	private boolean isUnsafe(char ch) {
		if (ch > 128 || ch < 0)
			return true;
		return " %$&+,/:;=?@<>#%'´\"\\".indexOf(ch) >= 0;
	}

	/**
	 * Asynctask for fetching information about cards from Magiccards.info
	 * @author Milan
	 *
	 */
	private class FetchCardsTask extends
			AsyncTask<MagicCardsInfo, Void, AsyncTaskResult<CardList>> {
		private String parserName;

		@Override
		protected AsyncTaskResult<CardList> doInBackground(MagicCardsInfo... parser) {
			CardList temp = new CardList();

			// selects first parser of the array
			MagicCardsInfo p = parser[0];
			parserName = p.getShopName();
			
			// checks if downloading images is enabled
			SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
			boolean inclImages = sharedPrefs.getBoolean("with_images", true);

			try {
				temp.insertCards(p.fetchPrices(cardName, inclImages));
				return new AsyncTaskResult<CardList>(temp);
			} catch (Exception e) {
				return new AsyncTaskResult<CardList>(e);
			}
		}

		@Override
		protected void onPostExecute(AsyncTaskResult<CardList> result) {
			// lists failed task
			if (result.getError() != null) {
				exceptions.add(result.getError());
				canceledList.add(parserName);			
			} 
			// adds results to the list
			else {
				CardList realResult = result.getResult();
				results.insertCards(realResult);
			}
			// updates progress bar
			addProgress();
		}
		
		@Override
		protected void onCancelled() {			
			canceledList.add(parserName);
			super.onCancelled();
		}

	}

	private class FetchPricesTask extends
			AsyncTask<IParser, Void, AsyncTaskResult<CardList>> {
		private String parserName;

		@Override
		protected AsyncTaskResult<CardList> doInBackground(IParser... parser) {
			CardList temp = new CardList();

			// selects first parser of the array
			IParser p = parser[0];
			parserName = p.getShopName();
			
			try {
				temp.insertCards(p.fetchPrices(cardName));
				return new AsyncTaskResult<CardList>(temp);
			} catch (Exception e) {
				return new AsyncTaskResult<CardList>(e);
			}
		}

		@Override
		protected void onPostExecute(AsyncTaskResult<CardList> result) {
			// lists failed task
			if (result.getError() != null) {
				exceptions.add(result.getError());
				canceledList.add(parserName);
			} 
			
			// adds results to the list
			else {
				CardList realResult = result.getResult();
				fromShops.insertCards(realResult);
			}
			// updates progress bar
			addProgress();
		}
		
		@Override
		protected void onCancelled() {
			canceledList.add(parserName);
			super.onCancelled();
		}

	}	

	/**
	 * Generic Asynctask class.
	 * @author Milan
	 *
	 * @param <T> type of result
	 */
	public class AsyncTaskResult<T> {
		private T result;
		private Exception error;

		public T getResult() {
			return result;
		}

		public Exception getError() {
			return error;
		}

		public AsyncTaskResult(T result) {
			super();
			this.result = result;
		}

		public AsyncTaskResult(Exception error) {
			super();
			this.error = error;
		}
	}
			
	/**
	 * Checks all checklists when "check all" is clicked.
	 * @param allCheckBox "check all" view
	 */
	public void checkAll (View allCheckBox){		
		if (allChecked){			
			((CheckBox) findViewById(R.id.RytirCheckBox)).setChecked(false);
			((CheckBox) findViewById(R.id.MysticCheckBox)).setChecked(false);
			((CheckBox) findViewById(R.id.NajadaCheckBox)).setChecked(false);
			((CheckBox) findViewById(R.id.RishadaCheckBox)).setChecked(false);
			((CheckBox) findViewById(R.id.TolarieCheckBox)).setChecked(false);
			((CheckBox) findViewById(R.id.LotusCheckBox)).setChecked(false);
			((CheckBox) findViewById(R.id.TopMagicCheckBox)).setChecked(false);
			
			// updates view text appropriately
			((CheckBox) findViewById(R.id.AllCheckBox)).setText(getResources().getString(R.string.select_all));
			
			allChecked = false;
		}
		else{
			((CheckBox) findViewById(R.id.RytirCheckBox)).setChecked(true);
			((CheckBox) findViewById(R.id.MysticCheckBox)).setChecked(true);
			((CheckBox) findViewById(R.id.NajadaCheckBox)).setChecked(true);
			((CheckBox) findViewById(R.id.RishadaCheckBox)).setChecked(true);
			((CheckBox) findViewById(R.id.TolarieCheckBox)).setChecked(true);
			((CheckBox) findViewById(R.id.LotusCheckBox)).setChecked(true);
			((CheckBox) findViewById(R.id.TopMagicCheckBox)).setChecked(true);
			
			// updates view text appropriately
			((CheckBox) findViewById(R.id.AllCheckBox)).setText(getResources().getString(R.string.deselect_all));
			
			allChecked = true;
		}
	}
	
	/**
	 * Attaches adapter for tracking search history to the view.
	 */
	private void attachAdapterToAutoComplete(){
		preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		history = preferences.getStringSet(getString(R.string.history), null);
		if (history == null){
			history = new HashSet<String>();
		}		
		String[] his = history.toArray(new String[history.size()]);
		AutoCompleteTextView actw = (AutoCompleteTextView) findViewById(R.id.CardNameAutoComplete);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, his);
		actw.setAdapter(adapter);
	}
	
	/**
	 * Adds search input to history
	 * @param input searched expression (card name)
	 */
	public void addToHistory(String input){
		Editor editor = preferences.edit();
		Set<String> set = new HashSet<String>();
		history.add(input);
		set.addAll(history);	
		editor.putStringSet(getString(R.string.history), set);
		editor.commit();
	}
	
}

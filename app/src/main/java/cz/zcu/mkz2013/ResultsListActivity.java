package cz.zcu.mkz2013;

import java.util.ArrayList;

import implementation.Card;
import implementation.CardList;
import implementation.ResultsAdapter;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;


/**
 * Shows list of the cards matching the input string
 * @author Milan Nikl
 *
 */
public class ResultsListActivity extends Activity {
	
	private ArrayList<Card> found;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_results_list);
		this.setTitle(getResources().getString(R.string.results));
		found = (ArrayList<Card>) getIntent().getExtras().getSerializable("results");
		
		// no results		
		if (found.isEmpty()){
			TextView e = (TextView) findViewById(R.id.ResultError);
			e.setText(getString(R.string.empty_result)
		+ "\n\n" + getString(R.string.change_input));
			
		}
		// displays list
		else{
			
			ListView list = (ListView) findViewById(R.id.Seznam);						
			
			ResultsAdapter adapter = new ResultsAdapter(getApplicationContext(), found);
			list.setAdapter(adapter);			
			
			// click on the item starts new activity
			list.setOnItemClickListener(new AdapterView.OnItemClickListener() {			
				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
					Intent intent = new Intent(arg0.getContext(), CardDetailActivity.class);
					Card clicked = found.get(arg2);
					intent.putExtra("clicked", clicked);
					startActivity(intent);					
				}
			});			
		}		
		
	}
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.results_list, menu);
		return true;
	}
	
	/**
	 * On selecting option in the action bar creates appropriate flag
	 */	
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_search:
			  Intent intent = new Intent(this, SearchActivity.class);
			  intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			  startActivity(intent);
			  break;
		case R.id.action_settings:
			startActivity(new Intent(this, SettingsActivity.class));
		}
		return true;
	}

}

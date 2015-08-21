package cz.zcu.mkz2013;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

/**
 * Shows help from the Settings menu
 * @author Milan Nikl
 *
 */
public class HelpActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_help);
		
        this.setTitle(getResources().getString(R.string.help_title));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.help, menu);
		return true;
	}

}

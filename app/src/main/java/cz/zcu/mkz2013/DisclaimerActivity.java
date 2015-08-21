package cz.zcu.mkz2013;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

/**
 * Shows disclaimer from the Settings menu
 * @author Milan Nikl
 *
 */
public class DisclaimerActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_disclaimer);
		
		 this.setTitle(getResources().getString(R.string.disclaimer_title));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.disclaimer, menu);
		return true;
	}

}

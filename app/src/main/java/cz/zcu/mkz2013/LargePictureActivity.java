package cz.zcu.mkz2013;

import implementation.Card;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Shows detailed information about the card and large picture of it.
 * @author Milan Nikl
 *
 */
public class LargePictureActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_large_picture);
		
		// selected card
		Card selected = (Card) getIntent().getExtras().getSerializable("selected");		
		this.setTitle(selected.getName());
		
		// card image
		ImageView large = (ImageView) findViewById(R.id.LargeImageView);
		String pic = selected.getPicture();
		Bitmap big = BitmapFactory.decodeFile(pic);		
		if (big != null){
			large.setImageBitmap(big);
		}
		
		// card information headers and content
		TextView header = (TextView) findViewById(R.id.LargeHeader);
		header.setText(selected.getName());
		
		TextView th = (TextView) findViewById(R.id.LargeCardTextHeader);
		th.setText(getString(R.string.cardText));
		
		TextView text = (TextView) findViewById(R.id.LargeCardText);
		text.setText(selected.getCardText());
		
		TextView fh = (TextView) findViewById(R.id.LargeFlavorHeader);
		fh.setText(getString(R.string.flavor));
		
		TextView flavor = (TextView) findViewById(R.id.FlavorText);
		flavor.setText(selected.getFlavor());
		
		TextView eh = (TextView) findViewById(R.id.LargeEditionsHeader);
		eh.setText(getString(R.string.editions_header));
		
		TextView editions = (TextView) findViewById(R.id.LargeEditions);
		editions.setText(selected.getEditions());
		
		TextView rh = (TextView) findViewById(R.id.LargeRulingsHeader);
		rh.setText(getString(R.string.rulings));
		
		TextView rulings = (TextView) findViewById(R.id.RulingsText);
		rulings.setText(selected.getRulings());
		
		TextView lh = (TextView) findViewById(R.id.LargeLegalityHeader);
		lh.setText(getString(R.string.legality));
		
		TextView legality = (TextView) findViewById(R.id.LegalityText);
		legality.setText(selected.getLegality());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.large_picture, menu);
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

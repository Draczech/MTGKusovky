package cz.zcu.mkz2013;

import implementation.AvailabilityAdapter;
import implementation.AvailabilityListItem;
import implementation.Card;
import implementation.EditionComparator;
import implementation.ShopInfo;

import java.util.ArrayList;
import java.util.Collections;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Activity with brief info about card and list of availability items.
 * @author Milan Nikl
 *
 */
public class CardDetailActivity extends Activity {
		
	private String DASH = "-";
	private Card selected;
	private ArrayList<AvailabilityListItem> avaList;
	private ExpandableListView expList;
	private ExpandableListAdapter expAdapter;
	private TextView availabilityHeader;
	private ImageView rarityIcon;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_card_detail);
		
		// selected card
		selected = (Card) getIntent().getExtras().getSerializable("clicked");		
		this.setTitle(selected.getName());
		
		// header of the activity, includes name, type, thumbnail
		View listHeader = LayoutInflater.from(this).inflate(R.layout.detail_header, null);
		
		TextView header = (TextView) listHeader.findViewById(R.id.CardHeader);
		header.setText(selected.getName());
				
		TextView type = (TextView) listHeader.findViewById(R.id.CardType);
		type.setText(selected.getType());
				
		TextView mana = (TextView) listHeader.findViewById(R.id.Manacost);
		mana.setText(selected.getManacost());
				
		ImageView thumbnail = (ImageView) listHeader.findViewById(R.id.CardThumbnail);
		String pic = selected.getPicture();
		Bitmap thumb = BitmapFactory.decodeFile(pic);
		
		if (thumb != null){
			thumbnail.setImageBitmap(thumb);
		}
		
		// rarity icon
		rarityIcon = (ImageView) listHeader.findViewById(R.id.RarityIcon);
		String currentRarity = selected.getLastRarity();
		setRarityIcon(currentRarity);		
		
		ArrayList<ShopInfo> availability = selected.getAvailability();			
		
		// no availability information
		if(availability.isEmpty()){
			availabilityHeader = (TextView) listHeader.findViewById(R.id.AvailabilityHeader);
			availabilityHeader.setText("\n" + getString(R.string.empty_result)
									+ "\n" + getString(R.string.not_available));
			
			// example item
			ShopInfo temp = new ShopInfo(getString(R.string.sample));
			temp.setEdition(getString(R.string.edition));
			temp.setRarity(DASH);
			temp.setVersion(DASH);
			temp.setCount(0);
			temp.setPrice(0);
			availability.add(temp);
			selected.setAvailability(availability);
			
		}
		
		else{
			availabilityHeader = (TextView) listHeader.findViewById(R.id.AvailabilityHeader);
			availabilityHeader.setText(getString(R.string.availability));
									
		}	
		
		// transformation to a special list and sorting
		avaList = selected.convertToAvaList();
		EditionComparator ec = new EditionComparator(getResources().getStringArray(R.array.Sets));
		Collections.sort(avaList, ec);		
		
		// assigning list to the view
		expList = (ExpandableListView) findViewById(R.id.ExpandableAvailability);				
		expList.addHeaderView(listHeader);
		expAdapter = new AvailabilityAdapter(getApplicationContext(), avaList);				
		expList.setAdapter(expAdapter);
		
	}
	
	/**
	 * Starts activity with more detailed info and large picture
	 * @param v selected card (image)
	 */
	public void ShowImage(View v){
		Intent i = new Intent(v.getContext(), LargePictureActivity.class);
		i.putExtra("selected", selected);
		startActivity(i);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.card_detail, menu);
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
	
	/**
	 * Sets appropriate icon to the view based on rarity
	 * @param rarity card rarity value
	 */
	private void setRarityIcon(String rarity){
		if (rarity == null){
			rarityIcon.setImageDrawable(getResources().getDrawable(R.drawable.question));
		}
		else{

			if(rarity.equalsIgnoreCase("Common")){
				rarityIcon.setImageDrawable(getResources().getDrawable(R.drawable.common));
			}
			else if(rarity.equalsIgnoreCase("Uncommon")){
				rarityIcon.setImageDrawable(getResources().getDrawable(R.drawable.uncommon));
			}
			else if(rarity.equalsIgnoreCase("Rare")){
				rarityIcon.setImageDrawable(getResources().getDrawable(R.drawable.rare));
			}
			else if(rarity.equalsIgnoreCase("Mythic Rare")){
				rarityIcon.setImageDrawable(getResources().getDrawable(R.drawable.mythic));
			}
			else if(rarity.equalsIgnoreCase("Special")){
				rarityIcon.setImageDrawable(getResources().getDrawable(R.drawable.other));
			}
			else{
				rarityIcon.setImageDrawable(getResources().getDrawable(R.drawable.question));
			}
		}
	}
	
}

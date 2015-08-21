package implementation;

import java.util.ArrayList;

import cz.zcu.mkz2013.R;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

/**
 * Adapter for displaying all found cards in the ResultsListActivity.
 * @author Milan Nikl
 *
 */
public class ResultsAdapter extends BaseAdapter {
    private LayoutInflater inflater;
    // do not change, cannot be used as CardList
    private ArrayList<Card> data;
    private Context context;
    
    public ResultsAdapter(Context context, ArrayList<Card> data){
    	this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.data= data;
        }

	@Override
	public int getCount() {
        return this.data.size();
	}

	@Override
	public Card getItem(int pos) {
		return this.data.get(pos);
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	public int getViewTypeCount(){
        return 1;
    }

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Card selected = getItem(position);
				
		if(convertView == null){ // If the View is not cached
            					// Inflates the Common View from XML file
            convertView = this.inflater.inflate(R.layout.result_list_item, null);
        }
		
		// sets different background color for even and odd items
		int[] colors = new int[] { Color.LTGRAY, Color.TRANSPARENT };
		int colorPos = position % colors.length;
		convertView.setBackgroundColor(colors[colorPos]);
		
		TextView nh = (TextView) convertView.findViewById(R.id.ItemNameHeader);
		nh.setText(context.getString(R.string.name));
		
		TextView name = (TextView) convertView.findViewById(R.id.ItemName);
		name.setText(selected.getName());
		
		TextView th = (TextView) convertView.findViewById(R.id.ItemTypeHeader);
		th.setText(context.getString(R.string.type));
		
		TextView type = (TextView) convertView.findViewById(R.id.ItemType);
		type.setText(selected.getType());
		
		TextView mh = (TextView) convertView.findViewById(R.id.ItemManaHeader);
		mh.setText(context.getString(R.string.manacost));
		
		TextView mana = (TextView) convertView.findViewById(R.id.ItemMana);
		mana.setText(selected.getManacost());
		
		return convertView;
	}

}

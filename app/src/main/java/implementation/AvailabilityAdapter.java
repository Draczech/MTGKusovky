package implementation;

import java.util.ArrayList;
import cz.zcu.mkz2013.R;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

/**
 * Adapter for showing availability of cards in the CardDetailActivity
 * @author Milan Nikl
 *
 */
public class AvailabilityAdapter extends BaseExpandableListAdapter  {
    private LayoutInflater inflater;
    private ArrayList<AvailabilityListItem> groups;
    private Context context;
    
    public AvailabilityAdapter(Context context, ArrayList<AvailabilityListItem> groups){
    	this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.groups= groups;
        }

	
    @Override
	public ShopInfo getChild(int groupPosition, int childPosition) {
        ArrayList<ShopInfo> chList = groups.get(groupPosition).getAvailability();
        return chList.get(childPosition);
    }

	@Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }
	
	@Override
    public View getChildView(int groupPosition, int childPosition,
            boolean isLastChild, View convertView, ViewGroup parent) {

		ShopInfo selected = getChild(groupPosition, childPosition);
		
//		int[] colors = new int[] { Color.LTGRAY, Color.TRANSPARENT};
		
		if(convertView == null){ // If the View is not cached
            					// Inflates the Common View from XML file
            convertView = this.inflater.inflate(R.layout.availability_list_item, null);
        }
				
//		int colorPos = childPosition % colors.length;
//		convertView.setBackgroundColor(colors[colorPos]);		
		
		TextView shop = (TextView) convertView.findViewById(R.id.AvaShop);
		shop.setText(selected.getShop());		
				
		TextView state = (TextView) convertView.findViewById(R.id.AvaState);
		state.setText(selected.getVersion());		
		
		TextView stock = (TextView) convertView.findViewById(R.id.AvaStock);
		stock.setText("" + selected.getCount() + " ks");
		
		TextView price = (TextView) convertView.findViewById(R.id.AvaPrice);
		price.setText("" + selected.getPrice() + " Kč");

        return convertView;
    }
	
	@Override
    public int getChildrenCount(int groupPosition) {
        ArrayList<ShopInfo> chList = groups.get(groupPosition).getAvailability();
        return chList.size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return groups.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return groups.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }
    
    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
            View convertView, ViewGroup parent) {
    	AvailabilityListItem group = (AvailabilityListItem) getGroup(groupPosition);
        if (convertView == null) {
            LayoutInflater inf = (LayoutInflater) context
                    .getSystemService(context.LAYOUT_INFLATER_SERVICE);
            convertView = inf.inflate(R.layout.availability_group_item, null);
        }
        TextView edition = (TextView) convertView.findViewById(R.id.AvaGroupEdition);
        edition.setText(group.getEdition());
        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
    
    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
		
}

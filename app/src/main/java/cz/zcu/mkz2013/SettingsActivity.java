package cz.zcu.mkz2013;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import android.os.Bundle;
import android.os.Environment;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.view.Menu;
import android.widget.Toast;

/**
 * Activity for displaying settings
 * To be updated in the future
 * @author Milan Nikl
 *
 */
public class SettingsActivity extends PreferenceActivity {
    
    @Override
    public void onCreate(Bundle savedInstanceState) {        
        super.onCreate(savedInstanceState);        
        addPreferencesFromResource(R.xml.preferences); 
        this.setTitle(getResources().getString(R.string.settings_title));
        
        Preference delete_images_button = (Preference)findPreference(getString(R.string.delete_images_button));
        
        delete_images_button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                        @Override
                        public boolean onPreferenceClick(Preference arg0) {  
                        	
                        	deleteImages(); 
                        	
                            return true;
                        }
                    });
        
        Preference delete_history_button = (Preference)findPreference(getString(R.string.delete_history_button));
        
        delete_history_button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                        @Override
                        public boolean onPreferenceClick(Preference arg0) {  
                        	
                        	deleteHistory();
                        	
                            return true;
                        }
                    });
                    
        Preference info_button = (Preference)findPreference(getString(R.string.info_button));
        
        info_button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                        @Override
                        public boolean onPreferenceClick(Preference arg0) {
                        	
                        	Intent i = new Intent(getApplicationContext(), InfoActivity.class);	
                    		startActivity(i);
                        	  
                            return true;
                        }
                    });
        
        Preference help_button = (Preference)findPreference(getString(R.string.help_button));
        
        help_button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                        @Override
                        public boolean onPreferenceClick(Preference arg0) {
                        	
                        	Intent i = new Intent(getApplicationContext(), HelpActivity.class);	
                    		startActivity(i);
                        	  
                            return true;
                        }
                    });
        
        Preference disclaimer_button = (Preference)findPreference(getString(R.string.disclaimer_button));
        
        disclaimer_button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                        @Override
                        public boolean onPreferenceClick(Preference arg0) {
                        	
                        	Intent i = new Intent(getApplicationContext(), DisclaimerActivity.class);	
                    		startActivity(i);
                        	  
                            return true;
                        }
                    });
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, 0, 0, "Show current settings");
        return super.onCreateOptionsMenu(menu);
    }
       
    /**
     * Deletes all images in the image folder and the folder itself.
     * Shows appropriate toast.
     */
    private void deleteImages(){
    	int count = 0;
    	File folder = new File(Environment.getExternalStorageDirectory() + "/" + getString(R.string.img_folder));
    	
    	if (!folder.exists()){
    		Toast.makeText(getApplicationContext(), getString(R.string.img_zero), Toast.LENGTH_SHORT).show();
    		return;
    	}
    	
    	String[] content = folder.list();    	
    	count = content.length;
    	
    	for (String filename: content){
    		File temp = new File(folder, filename);
    		temp.delete();
    	}
    	
    	folder.delete();   
    	
    	switch (count){
			case 0:{
				Toast.makeText(getApplicationContext(), getString(R.string.img_zero), Toast.LENGTH_SHORT).show();
				break;
			}
			case 1:{
				Toast.makeText(getApplicationContext(), getString(R.string.img_one), Toast.LENGTH_SHORT).show();
				break;
			}
			case 2:
			case 3:
			case 4:
			{
				Toast.makeText(getApplicationContext(), String.format(getString(R.string.img_few), count), Toast.LENGTH_SHORT).show();
				break;
			}
			default:{
				Toast.makeText(getApplicationContext(), String.format(getString(R.string.img_few), count), Toast.LENGTH_SHORT).show();
			}
    	}
    }
    
    /**
     * Deletes all entries in the search history.
     * Shows appropriate toast.
     */
    private void deleteHistory(){
    	int size = 0;
    	SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    	Set<String> history = preferences.getStringSet(getString(R.string.history), null);
    	if (history == null){
    		Toast.makeText(getApplicationContext(), getString(R.string.hist_zero), Toast.LENGTH_SHORT).show();
    		return;
		}
    	
    	size = history.size();
    	
    	history = new HashSet<String>();
    	Editor editor = preferences.edit();
    	editor.putStringSet(getString(R.string.history), history);
		editor.commit();
		
		switch (size){
			case 0:{
				Toast.makeText(getApplicationContext(), getString(R.string.hist_zero), Toast.LENGTH_SHORT).show();
				break;
			}
			case 1:{
				Toast.makeText(getApplicationContext(), getString(R.string.hist_one), Toast.LENGTH_SHORT).show();
				break;
			}
			case 2:
			case 3:
			case 4:
			{
				Toast.makeText(getApplicationContext(), String.format(getString(R.string.hist_few), size), Toast.LENGTH_SHORT).show();
				break;
			}
			default:{
				Toast.makeText(getApplicationContext(), String.format(getString(R.string.hist_few), size), Toast.LENGTH_SHORT).show();
			}
		}		
    }
}



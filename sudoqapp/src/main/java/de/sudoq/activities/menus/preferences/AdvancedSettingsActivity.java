/*
 * SudoQ is a Sudoku-App for Adroid Devices with Version 2.2 at least.
 * Copyright (C) 2012  Heiko Klare, Julian Geppert, Jan-Bernhard Kordaß, Jonathan Kieling, Tim Zeitz, Timo Abele
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package de.sudoq.activities.menus.preferences;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;

import de.sudoq.R;
import de.sudoq.activities.menus.NewSudokuActivity;
import de.sudoq.controller.language.LanguageCode;
import de.sudoq.controller.language.LanguageUtility;
import de.sudoq.model.game.GameSettings;
import de.sudoq.model.profile.Profile;

import static de.sudoq.activities.menus.preferences.AdvancedSettingsActivity.ParentActivity.NOT_SPECIFIED;

/**
 * Activity um Profile zu bearbeiten und zu verwalten
 */
public class AdvancedSettingsActivity extends PreferencesActivity
{
	/** Attributes */
	private static final String LOG_TAG = AdvancedSettingsActivity.class.getSimpleName();
	
	public enum ParentActivity
	{
		PROFILE,
		NEW_SUDOKU,
		NOT_SPECIFIED
	}
	
	/*this is still a hack! this activity can be called in newSudoku-pref and in player(profile)Pref, but has different behaviours*/
	public static ParentActivity caller = NOT_SPECIFIED;
	
	public static GameSettings gameSettings;
	
	CheckBox lefthand;
	Button restricttypes;
	CheckBox helper;
	CheckBox debug;
	
	Byte debugCounter = 0;
	
	private boolean langSpinnerInit = true;
	
	/**
	 * Wird aufgerufen, falls die Activity zum ersten Mal gestartet wird. Läd
	 * die Preferences anhand der zur Zeit aktiven Profil-ID.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		Log.i("lang", "AdvancedPreferencesActivity.onCreate() called.");
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.preferences_advanced);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		
		final ActionBar ab = getSupportActionBar();
		ab.setHomeAsUpIndicator(R.drawable.launcher);
		ab.setDisplayHomeAsUpEnabled(true);
		ab.setDisplayShowTitleEnabled(true);
		//set title explicitly so localization kicks in
		ab.setTitle(R.string.sf_advancedpreferences_title);
		
		lefthand = (CheckBox) findViewById(R.id.checkbox_lefthand_mode);
		restricttypes = (Button) findViewById(R.id.button_provide_restricted_set_of_types);
		helper = (CheckBox) findViewById(R.id.checkbox_hints_provider);
		debug = (CheckBox) findViewById(R.id.checkbox_debug);
		//exporter = (CheckBox) findViewById(R.id.checkbox_exportcrash_trigger);
		
		gameSettings = NewSudokuActivity.gameSettings;
		GameSettings profileGameSettings = Profile.getInstance().getAssistances();
		
		switch(caller)
		{
			case NEW_SUDOKU:
				debug.setChecked(Profile.getInstance().getAppSettings().isDebugSet());
				if(debug.isChecked())
				{
					debug.setVisibility(View.VISIBLE);
				}
				helper.setChecked(gameSettings.isHelperSet());
				lefthand.setChecked(gameSettings.isLefthandModeSet());
				break;
			case PROFILE:
			case NOT_SPECIFIED://not specified souldn't happen, but you never know
				if(debug.isChecked())
				{
					debug.setVisibility(View.VISIBLE);
				}
				debug.setChecked(Profile.getInstance().getAppSettings().isDebugSet());
				helper.setChecked(profileGameSettings.isHelperSet());
				lefthand.setChecked(profileGameSettings.isLefthandModeSet());
		}
		//myCaller.restricttypes.setChecked(a.isreHelperSet());
		
		Profile.getInstance().registerListener(this);
		
		// Language spinner/management:
		
		final Spinner languageSpinner = findViewById(R.id.spinner_language);
		
		ArrayAdapter<CharSequence> languageAdapter = ArrayAdapter.createFromResource(this,
				R.array.language_choice_values,
				android.R.layout.simple_spinner_item);
		languageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		languageSpinner.setAdapter(languageAdapter);
		
		//set language
		LanguageCode languageCode = LanguageUtility.loadLanguageCodeFromPreferences(this);
		
		languageSpinner.setSelection(languageCode.ordinal());
		// nested Listener for languageSpinner
		languageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
		{
			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
			{
				if(langSpinnerInit)
				{
					//onitemselected is called after initialization
					//this flag prevents it
					langSpinnerInit = false;
					return;
				}
				
				// If position is out of bounds set it to the system language index:
				if(pos >= LanguageCode.values().length)
				{
					pos = LanguageCode.system.ordinal();
				}
				
				// Translate the position to enum language value:
				LanguageCode enumCode = LanguageCode.values()[pos];
				
				// Store the chosen language setting to preferences:
				LanguageUtility.saveLanguageCodeToPreferences(AdvancedSettingsActivity.this, enumCode);
				
				// Resolve the enum language if system:
				LanguageCode newLanguageCode = enumCode;
				if(newLanguageCode == LanguageCode.system)
				{
					// Either the real system language (if possible) or english will be applied here:
					newLanguageCode = LanguageUtility.resolveSystemLanguage();
				}
				// Apply the new language choice to the resources (regardless if it is the same):
				LanguageUtility.setResourceLocale(AdvancedSettingsActivity.this, newLanguageCode);
				
				// Restart this activity (if required):
				restartIfWrongLanguage();
			}
			
			public void onNothingSelected(AdapterView<?> parent)
			{
				// do nothing
			}
		});
	}
	
	/**
	 * Aktualisiert die Werte in den Views
	 */
	@Override
	protected void refreshValues()
	{
		//myCaller.lefthand.setChecked(gameSettings.isLefthandModeSet());
		//myCaller.helper.setChecked(  gameSettings.isHelperSet());
	}
	
	/**
	 * Selected by click on button (specified in layout file)
	 * Starts new activity that lets user choose which types are offered in 'new sudoku' menu
	 *
	 * @param view von android xml übergebene View
	 */
	public void selectTypesToRestrict(View view)
	{
		Log.d("gameSettings", "AdvancedPreferencesActivity.selectTypesToRestrict");
		startActivity(new Intent(this, OfferedTypesActivity.class));
	}
	
	public void helperSelected(final View view)
	{
		final CheckBox cb = (CheckBox) view;
		if(cb.isChecked()) //if it is now, after click selected
		{
			askConfirmation(cb);
		}
	}
	
	public void count(View view)
	{
		debugCounter++;
		if(debugCounter >= 10)
		{
			debug.setVisibility(View.VISIBLE);
		}
	}
	
	private void askConfirmation(final CheckBox cb)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		
		builder.setPositiveButton(getString(R.string.dialog_yes), new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int which)
			{
				// pass
			}
		});
		
		builder.setNegativeButton(getString(R.string.dialog_no), new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int which)
			{
				cb.setChecked(false);
			}
		});
		builder.setMessage("This feature is still in development. Are you sure you want to activate it?");
		AlertDialog alertDialog = builder.create();
		alertDialog.show();
	}
	
	@Override
	protected void adjustValuesAndSave()
	{
		switch(caller)
		{
			case NEW_SUDOKU://TODO just have 2 subclasses, one to be called from playerpref, one from newSudokuPref
				saveToGameSettings();
				if(debug != null)
					Profile.getInstance().setDebugActive(debug.isChecked());
				break;
			case PROFILE:
				saveToProfile();
				break;
		}
	}
	
	private void saveToGameSettings()
	{
		if(lefthand != null && helper != null)
		{
			gameSettings.setLefthandMode(lefthand.isChecked());
			gameSettings.setHelper(helper.isChecked());
		}
	}
	
	protected void saveToProfile()
	{
		Profile p = Profile.getInstance();
		if(debug != null)
		{
			p.setDebugActive(debug.isChecked());
		}
		if(helper != null)
		{
			p.setHelperActive(helper.isChecked());
		}
		if(lefthand != null)
		{
			p.setLefthandActive(lefthand.isChecked());
		}
		//restrict types is automatically saved to profile...
		Profile.getInstance().saveChanges();
	}
	
	// Options menu
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.action_bar_standard, menu);
		return true;
	}
	
	/**
	 * Stellt das OptionsMenu bereit
	 *
	 * @param item Das ausgewählte Menü-Item
	 * @return true
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu)
	{
		super.onPrepareOptionsMenu(menu);
		return true;
	}
}

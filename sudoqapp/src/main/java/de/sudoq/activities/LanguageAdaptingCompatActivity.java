package de.sudoq.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import de.sudoq.controller.language.LanguageCode;
import de.sudoq.controller.language.LanguageUtility;

/**
 * This abstract activity can be used as parent activity.
 * It will check, whenever the activity gets created/started/resumed, if the currently locale is still correctly set.
 * If not, it restarts itself.
 * It checks on all 3 stages to capture the changes as early as possible. This adds checking overhead, but removes other possible overhead.
 */
public abstract class LanguageAdaptingCompatActivity extends AppCompatActivity
{
	/**
	 * The language code used when this activity got created.
	 * If the code changes, the activity gets recreated and this field updated.
	 */
	protected LanguageCode currentLanguageCode;
	
	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		//Save the language code of which the resources are currently set to.
		currentLanguageCode = LanguageUtility.getResourceLanguageCode(this);
		restartIfWrongLanguage();
	}
	
	@Override
	protected void onStart()
	{
		super.onStart();
		
		restartIfWrongLanguage();
	}
	
	@Override
	protected void onResume()
	{
		super.onResume();
		
		restartIfWrongLanguage();
	}
	
	/**
	 * Can be called from any child activity to check if the language is still correct.
	 * If the language is no longer correct it will restart the activity.
	 */
	protected void restartIfWrongLanguage()
	{
		// Get the language code which the user would like to see:
		LanguageCode desiredLanguageCode = LanguageUtility.getDesiredLanguage(this);
		// Check if the activity already has that language:
		if(desiredLanguageCode != currentLanguageCode)
		{
			// If not get the current resource locale:
			LanguageCode latestLanguageCode = LanguageUtility.getResourceLanguageCode(this);
			// If that still does not match the desired language, update the resource locale.
			if(latestLanguageCode != desiredLanguageCode)
			{
				// This should not happen, since the resource locale is already updated once changed by the user.
				LanguageUtility.setResourceLocale(this, desiredLanguageCode);
			}
			// The language has changed, restart this activity to apply:
			Log.d("SudoQLanguage", "Restarting activity '" + getClass().getSimpleName() + "', cause the language changed: " + currentLanguageCode + " -> " + desiredLanguageCode);
			restartThisActivity();
		}
	}
	
	/**
	 * Simply restarts this activity, while trying to not show an animation.
	 * This is an extra method, so that child classes can override it. Might be required to pass data along with the {@link Intent}.
	 */
	protected void restartThisActivity()
	{
		Intent intent = new Intent(this, getClass());
		//This flag will remove any instances of the current activity, and any following activities from the back stack.
		// Needed for older Android versions, which do not consider the previous 'finish()' call. (Works for version 9, doesn't for version 7, version 8 is untested).
		// The description of this flag might not allow an activity to exist more than once on the stack, but that is fine for the use case of this application.
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		finish();
		startActivity(intent);
		overridePendingTransition(0, 0);
	}
}

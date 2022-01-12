package de.sudoq.controller.language;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.util.Log;

import java.util.Locale;

/**
 * This class provides several utility functions, for dealing with language management.
 */
public class LanguageUtility
{
	// ### Language preferences: ###
	
	/**
	 * Name of the preferences.
	 */
	private static final String SUDOQ_SHARED_PREFS_FILE = "SudoqSharedPrefs";
	/**
	 * The language key.
	 */
	private static final String LANGUAGE_KEY = "language";
	
	/**
	 * Loads the {@link LanguageCode} from preferences. Defaults to system.
	 *
	 * @param context a {@link Context} of this application (any activity)
	 * @return the {@link LanguageCode} stored in the settings, or system
	 */
	public static LanguageCode loadLanguageCodeFromPreferences(Context context)
	{
		SharedPreferences sharedPreferences = context.getSharedPreferences(SUDOQ_SHARED_PREFS_FILE, Context.MODE_PRIVATE);
		String languageCodeOrSystem = sharedPreferences.getString(LANGUAGE_KEY, LanguageCode.system.name());
		return LanguageCode.getFromString(languageCodeOrSystem);
	}
	
	/**
	 * Stores a {@link LanguageCode} to preferences.
	 *
	 * @param context a {@link Context} of this application (any activity)
	 * @param languageCode the {@link LanguageCode} to store in the preferences
	 */
	public static void saveLanguageCodeToPreferences(Context context, LanguageCode languageCode)
	{
		SharedPreferences sp = context.getSharedPreferences(SUDOQ_SHARED_PREFS_FILE, Context.MODE_PRIVATE);
		sp.edit()
				.putString(LANGUAGE_KEY, languageCode.name())
				.apply();
	}
	
	// ### System language: ###
	
	/**
	 * Finds the {@link LanguageCode} for the current system language. If the system language has no translation/is unknown English is chosen.
	 *
	 * @return the {@link LanguageCode} for the system language, or if unknown for english
	 */
	public static LanguageCode resolveSystemLanguage()
	{
		String code = Locale.getDefault().getLanguage();
		return LanguageCode.getFromLanguageCode(code);
	}
	
	// ### Resource language: ###
	
	/**
	 * Gets the {@link LanguageCode} for the currently chosen resource language.
	 * There are only 3 possible resource languages, if however the resource is something else, this returns English.
	 *
	 * @param context a {@link Context} of this application (any activity)
	 * @return the {@link LanguageCode} which the resource is set to
	 */
	public static LanguageCode getResourceLanguageCode(Context context)
	{
		Resources resources = context.getResources();
		Configuration configuration = resources.getConfiguration();
		String languageCode = configuration.locale.getLanguage();
		return LanguageCode.getFromLanguageCode(languageCode);
	}
	
	/**
	 * Sets the locale of the resources to a provided language.
	 *
	 * @param context a {@link Context} of this application (any activity)
	 * @param languageCode the {@link LanguageCode} which the resources should be set to
	 * @throws IllegalArgumentException if the {@link LanguageCode} for system was supplied
	 */
	public static void setResourceLocale(Context context, LanguageCode languageCode)
	{
		Log.d("SudoQLanguage", "Setting resource from context '" + context.getClass().getSimpleName() + "' to '" + languageCode + "'");
		if(languageCode == LanguageCode.system)
		{
			throw new IllegalArgumentException("The resource locale may never be set to system!");
		}
		Locale newLocale = new Locale(languageCode.name());
		Resources resources = context.getResources();
		DisplayMetrics displayMetrics = resources.getDisplayMetrics();
		Configuration configuration = resources.getConfiguration();
		configuration.locale = newLocale;
		resources.updateConfiguration(configuration, displayMetrics);
	}
	
	// ### App language: ###
	
	/**
	 * Returns the {@link LanguageCode} which the the app should currently be displaying.
	 * If the setting points to system, the system language is resolved, fallback is English.
	 *
	 * @param context a {@link Context} of this application (any activity)
	 * @return the {@link LanguageCode} representing the current language to use, never system
	 */
	public static LanguageCode getDesiredLanguage(Context context)
	{
		LanguageCode languageCode = loadLanguageCodeFromPreferences(context);
		if(languageCode == LanguageCode.system)
		{
			//Load system language:
			return resolveSystemLanguage();
		}
		else
		{
			return languageCode;
		}
	}
}

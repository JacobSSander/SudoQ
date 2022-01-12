package de.sudoq.controller.language;

import android.util.Log;

/**
 * The LanguageCode class represents the language setting which the users chooses.
 * There are three translations of this apps strings: English, German and French.
 * The user may enforce one of these three or follow the system default.
 * If the system default is not supported, the chosen language will be english.
 */
public enum LanguageCode
{
	system,
	de,
	en,
	fr;
	
	/**
	 * Returns the LanguageCode for the given language code, or the language code for english, if the language code is unknown.
	 *
	 * @param code the language code (de, en, fr, ...)
	 * @return the LanguageCode representing this language code, or english if not supported
	 * @throws IllegalArgumentException if the string 'system' was supplied
	 */
	public static LanguageCode getFromLanguageCode(String code)
	{
		if(system.name().equals(code))
		{
			throw new IllegalArgumentException("Invalid language code 'system'!");
		}
		for(LanguageCode languageCode : LanguageCode.values())
		{
			if(languageCode.name().equals(code))
			{
				return languageCode;
			}
		}
		//Default to english, if the language is not supported.
		return LanguageCode.en;
	}
	
	/**
	 * Returns the LanguageCode for the given code, if the code is not supported.
	 * This method is meant to parse the system language preferences.
	 *
	 * @param code the language setting code (system, de, en, fr)
	 * @return the LanguageCode for that code, or system if the code is not supported
	 */
	public static LanguageCode getFromString(String code)
	{
		for(LanguageCode languageCode : LanguageCode.values())
		{
			if(languageCode.name().equals(code))
			{
				return languageCode;
			}
		}
		//Default to system, if the code is not supported.
		Log.d("SudoQLanguage", "Invalid LanguageCode code, defaulting to system. Supplied code: '" + code + "'");
		return LanguageCode.system;
	}
}

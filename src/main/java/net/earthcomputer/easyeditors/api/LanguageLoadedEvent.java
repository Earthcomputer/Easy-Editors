package net.earthcomputer.easyeditors.api;

import java.util.Map;

import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * Fired by {@link SmartTranslationRegistry} when the resource manager is
 * reloaded. Use this event to refresh the values in constants such as those in
 * {@link net.earthcomputer.easyeditors.util.Translate Translate}
 * 
 * @author Joseph
 *
 */
public class LanguageLoadedEvent extends Event {

	private final String language;
	private final Map<String, String> translations;

	public LanguageLoadedEvent(String language, Map<String, String> translations) {
		this.language = language;
		this.translations = translations;
	}

	/**
	 * Gets the language that was loaded
	 * 
	 * @return The language that was loaded
	 */
	public String getLanguage() {
		return language;
	}

	/**
	 * Gets a map of all the translations after the language was reloaded
	 * 
	 * @return An unmodifiable map of all the translations after the language
	 *         was reloaded
	 */
	public Map<String, String> getTranslations() {
		return translations;
	}

}

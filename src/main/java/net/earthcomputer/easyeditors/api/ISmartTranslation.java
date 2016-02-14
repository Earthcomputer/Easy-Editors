package net.earthcomputer.easyeditors.api;

import java.util.Map;

/**
 * A definition of a smart translation.
 * 
 * <b>This class is a member of the Easy Editors API</b>
 * 
 * @author Earthcomputer
 *
 */
public interface ISmartTranslation {

	/**
	 * Called when the resource manager is reloaded
	 * 
	 * @param language
	 * @param translations
	 *            - all the keys and translations that have been translated so
	 *            far, including all the translations from the .lang files
	 * @return The local translation of the key this smart translation was
	 *         registered with
	 */
	String translateToLocal(String language, Map<String, String> translations);

}

package net.earthcomputer.easyeditors.api;

import java.util.Collections;
import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.client.resources.Locale;
import net.minecraft.util.text.translation.LanguageMap;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

/**
 * A registry for creating translations in a smarter way than you can in the
 * language files. For example, you can set aliases.
 * 
 * <b>This class is a member of the Easy Editors API</b>
 * 
 * @author Earthcomputer
 *
 */
public class SmartTranslationRegistry {

	private static final LanguageMap languageMapInstance = ReflectionHelper.getPrivateValue(LanguageMap.class, null,
			"field_74817_a", "instance");
	private static final Map<String, String> translationMap = ReflectionHelper.getPrivateValue(LanguageMap.class,
			languageMapInstance, "field_74816_c", "languageList");
	private static final Map<String, String> i18nProps = ReflectionHelper.getPrivateValue(Locale.class,
			ReflectionHelper.<Locale, I18n>getPrivateValue(I18n.class, null, "field_135054_a", "i18nLocale"),
			"field_135032_a", "properties");
	/**
	 * An <i>unmodifiable</i> map of all the translation keys and translated
	 * values
	 */
	public static final Map<String, String> translations = Collections.unmodifiableMap(translationMap);

	private SmartTranslationRegistry() {
	}

	private static final Map<String, ISmartTranslation> smartTranslations = Maps.newHashMap();

	static {
		((IReloadableResourceManager) Minecraft.getMinecraft().getResourceManager())
				.registerReloadListener(new ResourceManagerReloadListener());
	}

	/**
	 * Utility method for getting the LanguageMap instance (the vanilla getter
	 * is package-private)
	 * 
	 * @return The LanguageMap instance
	 */
	public static LanguageMap getLanguageMapInstance() {
		return languageMapInstance;
	}

	/**
	 * Registers a smart translation for the given key
	 * 
	 * @param key
	 * @param translation
	 */
	public static void registerTranslation(String key, ISmartTranslation translation) {
		smartTranslations.put(key, translation);
	}

	/**
	 * Registers an alias. key1 will be mapped to the translation of key2
	 * 
	 * @param key1
	 * @param key2
	 */
	public static void registerAlias(String key1, String key2) {
		registerTranslation(key1, new AliasTranslation(key2));
	}

	private static class AliasTranslation implements ISmartTranslation {
		private String otherKey;

		public AliasTranslation(String keyToTranslateTo) {
			this.otherKey = keyToTranslateTo;
		}

		@Override
		public String translateToLocal(String language, Map<String, String> translations) {
			return translations.get(otherKey);
		}
	}

	private static class ResourceManagerReloadListener implements IResourceManagerReloadListener {

		@Override
		public void onResourceManagerReload(IResourceManager resourceManager) {
			EasyEditorsApi.logger.info("Reloading resource manager");
			String lang = FMLCommonHandler.instance().getCurrentLanguage();
			for (Map.Entry<String, ISmartTranslation> entry : smartTranslations.entrySet()) {
				if (!translations.containsKey(entry.getKey())) {
					String translation = entry.getValue().translateToLocal(lang, translations);
					translationMap.put(entry.getKey(), translation);
					i18nProps.put(entry.getKey(), translation);
				}
			}
			MinecraftForge.EVENT_BUS.post(new LanguageLoadedEvent(lang, Collections.unmodifiableMap(translationMap)));
		}
	}

}

package net.earthcomputer.easyeditors;

import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class GuiReplacementRegistry {

	private Map<Class<? extends GuiScreen>, Class<? extends GuiScreen>> replacementMap = Maps.newHashMap();

	public void registerReplacement(Class<? extends GuiScreen> old, Class<? extends GuiScreen> _new) {
		replacementMap.put(old, _new);
	}

	@SubscribeEvent
	public void openGui(GuiOpenEvent e) throws Exception {
		if (e.gui != null) {
			Class<? extends GuiScreen> oldClass = e.gui.getClass();
			if (replacementMap.containsKey(oldClass)) {
				e.gui = replacementMap.get(oldClass).getConstructor(oldClass).newInstance(e.gui);
			}
		}
	}

}

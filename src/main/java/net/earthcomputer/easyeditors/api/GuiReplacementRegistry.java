package net.earthcomputer.easyeditors.api;

import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * A class which replaces GUIs ingame, if
 * {@link EasyEditorsApi#isEasyEditorsActive}.
 * 
 * <b>This class is a member of the Easy Editors API</b>
 * 
 * @author Earthcomputer
 *
 */
public class GuiReplacementRegistry {

	private static GuiReplacementRegistry instance = new GuiReplacementRegistry();

	private Map<Class<? extends GuiScreen>, Class<? extends GuiScreen>> replacementMap = Maps.newHashMap();

	private GuiReplacementRegistry() {
		MinecraftForge.EVENT_BUS.register(this);
	}

	/**
	 * 
	 * @return The singleton instance of GuiReplacementRegistry
	 */
	public static GuiReplacementRegistry getInstance() {
		return instance;
	}

	/**
	 * Registers a replacement which replaces all GUIs of type old with type
	 * _new, if {@link EasyEditorsApi#isEasyEditorsActive}
	 * 
	 * @param old
	 *            - the old GUI type. The GUI will be replaced if
	 *            oldGui.getClass() == old
	 * @param _new
	 *            - the GUI type to replace the old GUI with. _new must have a
	 *            public constructor containing a single argument of type old
	 */
	public void registerReplacement(Class<? extends GuiScreen> old, Class<? extends GuiScreen> _new) {
		replacementMap.put(old, _new);
	}

	@SubscribeEvent
	public void openGui(GuiOpenEvent e) throws Exception {
		if (EasyEditorsApi.isEasyEditorsActive && e.gui != null) {
			Class<? extends GuiScreen> oldClass = e.gui.getClass();
			if (replacementMap.containsKey(oldClass)) {
				e.gui = replacementMap.get(oldClass).getConstructor(oldClass).newInstance(e.gui);
			}
		}
	}

}

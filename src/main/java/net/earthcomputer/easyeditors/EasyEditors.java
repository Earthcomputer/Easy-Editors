package net.earthcomputer.easyeditors;

import java.util.Map;
import java.util.regex.Pattern;

import net.earthcomputer.easyeditors.gui.GuiNewCommandBlock;
import net.earthcomputer.easyeditors.util.Colors;
import net.earthcomputer.easyeditors.util.GeneralUtils;
import net.earthcomputer.easyeditors.util.Colors.Color;
import net.minecraft.client.gui.GuiCommandBlock;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkCheckHandler;
import net.minecraftforge.fml.relauncher.Side;

@Mod(modid = EasyEditors.ID, name = EasyEditors.NAME, version = EasyEditors.VERSION, clientSideOnly = true, canBeDeactivated = true, guiFactory = "net.earthcomputer.easyeditors.GuiFactory")
public class EasyEditors {

	public static final String ID = "easyeditors";
	public static final String NAME = "Easy Editors";
	public static final String VERSION = "1.0";

	@Instance(ID)
	public static EasyEditors instance;

	public static final GuiReplacementRegistry GUI_REPLACEMENT_REGISTRY = new GuiReplacementRegistry();

	private Configuration config;
	public boolean active;

	@NetworkCheckHandler
	public boolean acceptsRemote(Map<String, String> mods, Side remoteSide) {
		return true;
	}

	@EventHandler
	public void preinit(FMLPreInitializationEvent e) {
		config = new Configuration(e.getSuggestedConfigurationFile());
		config.load();
		active = config.getBoolean("active", "general", true, "Whether Easy Editors should replace GUIs");
		config.addCustomCategoryComment("colors",
				"This changes all the colors in things like the command editor. " + Configuration.NEW_LINE
						+ "For six-digit colors, the format is RRGGBB, where R is the red " + Configuration.NEW_LINE
						+ "component of the color, G is the green component of the color " + Configuration.NEW_LINE
						+ "and B is the blue component of the color. Where 8 digits appear, " + Configuration.NEW_LINE
						+ "the format is AARRGGBB, where A is the alpha (opacity) component of the color");
	}

	@EventHandler
	public void init(FMLInitializationEvent e) {
		MinecraftForge.EVENT_BUS.register(GUI_REPLACEMENT_REGISTRY);
		GUI_REPLACEMENT_REGISTRY.registerReplacement(GuiCommandBlock.class, GuiNewCommandBlock.class);

		for (Color color : Colors.allColors()) {
			color.color = GeneralUtils.hexToInt((config.getString(color.name, "colors",
					String.format(color.includesAlpha ? "%08X" : "%06X", color.defaultColor), color.description,
					Pattern.compile(color.includesAlpha ? "[0-9a-fA-F]{8}" : "[0-9a-fA-F]{6}"))));
		}
		if (config.hasChanged())
			config.save();
	}

}

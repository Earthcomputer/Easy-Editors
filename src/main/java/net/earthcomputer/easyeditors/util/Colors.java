package net.earthcomputer.easyeditors.util;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;

public class Colors {

	private static final List<Color> colors = Lists.newArrayList();

	public static final Color commandBox = new Color("box.command", 0xff6584c2, true,
			"The color of the box of a command (default a bluish color)");
	public static final Color playerSelectorBox = new Color("box.playerSelector", 0xff21a000, true,
			"The color of the box of a player selector");
	public static final Color label = new Color("label.generic", 0x303080, "The color of a label");
	public static final Color playerSelectorLabel = new Color("label.playerSelector", 0x215500,
			"The color of a label in a player selector");
	public static final Color commandName = new Color("misc.commandName", 0, "The color of the command name");
	public static final Color invalidCommandName = new Color("misc.commandName.invalid", 0xff0000,
			"The color of the command name, if it is invalid");
	public static final Color playerSelectorSelectBy = new Color("playerSelector.selectBy", 0,
			"The color \"Select By:\" appears in a player selector");
	public static final Color radioOutline = new Color("misc.radioOutline", 0xffffffff, true,
			"The color a radio list outlines the selected element (default white)");
	public static final Color itemName = new Color("misc.itemName", 0, "The color of the name of an item");
	public static final Color invalidItemName = new Color("misc.itemName.invalid", 0xff0000,
			"The color of an invalid item name");

	public static List<Color> allColors() {
		return Collections.unmodifiableList(colors);
	}

	public static class Color {
		public String name;
		public int color;
		public final int defaultColor;
		public final boolean includesAlpha;
		public String description;

		public Color(String name, int defaultColor) {
			this(name, defaultColor, false, null);
		}

		public Color(String name, int defaultColor, boolean includeAlpha) {
			this(name, defaultColor, includeAlpha, null);
		}

		public Color(String name, int defaultColor, String description) {
			this(name, defaultColor, false, description);
		}

		public Color(String name, int defaultColor, boolean includeAlpha, String description) {
			this.name = name;
			this.color = this.defaultColor = defaultColor;
			this.includesAlpha = includeAlpha;
			this.description = description;
			colors.add(this);
		}
	}

}

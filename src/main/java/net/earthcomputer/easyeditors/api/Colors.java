package net.earthcomputer.easyeditors.api;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;

/**
 * A class containing all the customizable colors in Easy Editors. Do not expect
 * these colors to have their true displayed colors until after Easy Editors has
 * been initialized.
 * 
 * <b>This class is a member of the Easy Editors API</b>
 * 
 * @author Earthcomputer
 *
 */
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
	public static final Color itemBox = new Color("box.item", 0xffffc923, true, "The color of the box of an item");
	public static final Color itemLabel = new Color("label.item", 0xa88417, "The color of a label in a box of an item");

	/**
	 * 
	 * @return A list of all the {@link Color}s ever instantiated
	 */
	public static List<Color> allColors() {
		return Collections.unmodifiableList(colors);
	}

	/**
	 * A customizable color. Instantiating one of these will automatically add
	 * it to the Easy Editors Configuration. Do not expect these colors to
	 * function properly if Easy Editors is not loaded.
	 * 
	 * <b>This class is a member of the Easy Editors API</b>
	 * 
	 * @author Earthcomputer
	 *
	 */
	public static class Color {
		/**
		 * The name of this color, as it appears in the configuration file
		 */
		public String name;
		/**
		 * The actual color of this color, as will be displayed in GUIs
		 */
		public int color;
		/**
		 * The default color of this color
		 */
		public final int defaultColor;
		/**
		 * Whether this color has an alpha component. Default: false
		 */
		public final boolean includesAlpha;
		/**
		 * A string description, as appears in the comments in the configuration
		 * file
		 */
		public String description;

		/**
		 * Creates a Color with the given {@link #name} and the given
		 * {@link #defaultColor}. {@link #color} will be set to defaultColor
		 * 
		 * @param name
		 * @param defaultColor
		 */
		public Color(String name, int defaultColor) {
			this(name, defaultColor, false, null);
		}

		/**
		 * Creates a Color with the given {@link #name} and the given
		 * {@link #defaultColor}, with {@link #includeAlpha} specified.
		 * {@link #color} will be set to defaultColor
		 * 
		 * @param name
		 * @param defaultColor
		 * @param includeAlpha
		 */
		public Color(String name, int defaultColor, boolean includeAlpha) {
			this(name, defaultColor, includeAlpha, null);
		}

		/**
		 * Creates a Color with the given {@link #name}, the given
		 * {@link #defaultColor} and the given {@link #description}.
		 * {@link #color} will be set to defaultColor
		 * 
		 * @param name
		 * @param defaultColor
		 * @param description
		 */
		public Color(String name, int defaultColor, String description) {
			this(name, defaultColor, false, description);
		}

		/**
		 * Creates a Color with the given {@link #name}, the given
		 * {@link #defaultColor} and the given {@link #defaultColor}, with
		 * {@link #includesAlpha} specified. {@link #color} will be set to
		 * defaultColor
		 * 
		 * @param name
		 * @param defaultColor
		 * @param includeAlpha
		 * @param description
		 */
		public Color(String name, int defaultColor, boolean includeAlpha, String description) {
			this.name = name;
			this.color = this.defaultColor = defaultColor;
			this.includesAlpha = includeAlpha;
			this.description = description;
			colors.add(this);
		}
	}

}

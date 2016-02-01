package net.earthcomputer.easyeditors.gui.command.slot;

import java.util.List;

import net.earthcomputer.easyeditors.api.util.Colors;
import net.earthcomputer.easyeditors.gui.command.GuiCommandEditor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.fml.client.config.HoverChecker;

/**
 * A command slot which simply displays some text
 * 
 * @author Earthcomputer
 *
 */
public class CommandSlotLabel extends GuiCommandSlotImpl {

	private FontRenderer fontRenderer;
	private String text;
	private int color;
	private String hoverText;

	private HoverChecker hoverChecker;

	public CommandSlotLabel(FontRenderer fontRenderer, String text) {
		this(fontRenderer, text, Colors.label.color);
	}

	public CommandSlotLabel(FontRenderer fontRenderer, String text, int color) {
		this(fontRenderer, text, color, null);
	}

	public CommandSlotLabel(FontRenderer fontRenderer, String text, String hoverText) {
		this(fontRenderer, text, Colors.label.color, hoverText);
	}

	public CommandSlotLabel(FontRenderer fontRenderer, String text, int color, String hoverText) {
		super(fontRenderer.getStringWidth(text), fontRenderer.FONT_HEIGHT);
		this.fontRenderer = fontRenderer;
		this.text = text;
		this.color = color;
		this.hoverText = hoverText;
	}

	/**
	 * 
	 * @return The text color
	 */
	public int getColor() {
		return color;
	}

	/**
	 * Sets the text color
	 * 
	 * @param color
	 */
	public void setColor(int color) {
		this.color = color;
	}

	@Override
	public int readFromArgs(String[] args, int index) {
		return 0;
	}

	@Override
	public void addArgs(List<String> args) {
	}

	/**
	 * 
	 * @return The display text
	 */
	public String getText() {
		return text;
	}

	/**
	 * Sets the display text
	 * 
	 * @param text
	 */
	public void setText(String text) {
		this.text = text;
		setWidth(fontRenderer.getStringWidth(text));
	}

	/**
	 * 
	 * @return The text displayed when the mouse hovers over the label
	 */
	public String getHoverText() {
		return hoverText;
	}

	@Override
	public void draw(int x, int y, int mouseX, int mouseY, float partialTicks) {
		GlStateManager.disableLighting();
		GlStateManager.disableFog();
		fontRenderer.drawString(text, x, y, color);

		if (hoverText != null) {
			if (hoverChecker == null)
				hoverChecker = new HoverChecker(y, y + getHeight(), x, x + getWidth(), 1000);
			else
				hoverChecker.updateBounds(y, y + getHeight(), x, x + getWidth());

			if (!GuiCommandEditor.isInBounds(mouseX, mouseY))
				hoverChecker.resetHoverTimer();
			else if (hoverChecker.checkHover(mouseX, mouseY)) {
				drawTooltip(mouseX, mouseY, hoverText, 300);
			}
		}
	}

	/**
	 * Creates a {@link CommandSlotHorizontalArrangement} containing a label
	 * containing the given text, and all the child slots in describing
	 * 
	 * @param text
	 * @param describing
	 * @return
	 */
	public static IGuiCommandSlot createLabel(String text, IGuiCommandSlot... describing) {
		return createLabel(text, Colors.label.color, describing);
	}

	/**
	 * Creates a {@link CommandSlotHorizontalArrangement} containing a label
	 * containing the given text and with the given color, and all the child
	 * slots in describing
	 * 
	 * @param text
	 * @param color
	 * @param describing
	 * @return
	 */
	public static IGuiCommandSlot createLabel(String text, int color, IGuiCommandSlot... describing) {
		return createLabel(text, color, null, describing);
	}

	/**
	 * Creates a {@link CommandSlotHorizontalArrangement} containing a label
	 * which contains the given text and shows the given hover text, and all the
	 * child slots in describing
	 * 
	 * @param text
	 * @param hoverText
	 * @param describing
	 * @return
	 */
	public static IGuiCommandSlot createLabel(String text, String hoverText, IGuiCommandSlot... describing) {
		return createLabel(text, Colors.label.color, hoverText, describing);
	}

	/**
	 * Creates a {@link CommandSlotHorizontalArrangement} containing a label
	 * which contains the given text, with the given color and shows the given
	 * hover text, and all the child slots in describing
	 * 
	 * @param text
	 * @param color
	 * @param hoverText
	 * @param describing
	 * @return
	 */
	public static IGuiCommandSlot createLabel(String text, int color, String hoverText, IGuiCommandSlot... describing) {
		IGuiCommandSlot[] slots = new IGuiCommandSlot[describing.length + 1];
		slots[0] = new CommandSlotLabel(Minecraft.getMinecraft().fontRendererObj, text, color, hoverText);
		System.arraycopy(describing, 0, slots, 1, describing.length);
		return new CommandSlotHorizontalArrangement(slots);
	}

}

package net.earthcomputer.easyeditors.gui.command.slot;

import java.util.List;

import net.earthcomputer.easyeditors.gui.command.GuiCommandEditor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.fml.client.config.HoverChecker;

/**
 * A command slot which represents a button
 * 
 * @author Earthcomputer
 *
 */
public abstract class CommandSlotButton extends GuiCommandSlotImpl {

	private int x;
	private int y;
	private GuiClickListenerButton wrappedButton;
	private String hoverText;

	private HoverChecker hoverChecker;

	public CommandSlotButton(int width, int height, String text) {
		this(width, height, text, null);
	}

	public CommandSlotButton(int width, int height, String text, String hoverText) {
		super(width, height);
		wrappedButton = new GuiClickListenerButton(0, 0, width, height, text);
		this.hoverText = hoverText;
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
	 * @return The text displayed in the button
	 */
	public String getText() {
		return wrappedButton.displayString;
	}

	/**
	 * 
	 * @return The text displayed when hovered
	 */
	public String getHoverText() {
		return hoverText;
	}

	/**
	 * 
	 * @return Whether the button is enabled
	 */
	public boolean isEnabled() {
		return wrappedButton.enabled;
	}

	/**
	 * Sets whether the button is enabled
	 * 
	 * @param enabled
	 */
	public void setEnabled(boolean enabled) {
		wrappedButton.enabled = enabled;
	}

	/**
	 * 
	 * @return The text color in the button
	 */
	public int getTextColor() {
		return wrappedButton.packedFGColour;
	}

	/**
	 * Sets the text color in the button
	 * 
	 * @param textColor
	 */
	public void setTextColor(int textColor) {
		wrappedButton.packedFGColour = textColor;
	}

	@Override
	public void draw(int x, int y, int mouseX, int mouseY, float partialTicks) {
		GlStateManager.disableLighting();
		GlStateManager.disableFog();
		if (x != this.x || y != this.y) {
			this.x = x;
			this.y = y;
			GuiClickListenerButton oldButton = wrappedButton;
			GuiClickListenerButton newButton = wrappedButton = new GuiClickListenerButton(x, y, getWidth(), getHeight(),
					oldButton.displayString);
			newButton.enabled = oldButton.enabled;
			newButton.packedFGColour = oldButton.packedFGColour;
		}
		wrappedButton.drawButton(Minecraft.getMinecraft(), mouseX, mouseY);

		if (hoverText != null) {
			if (hoverChecker == null)
				hoverChecker = new HoverChecker(y, y + getHeight(), x, x + getWidth(), 1000);
			else
				hoverChecker.updateBounds(y, y + getHeight(), x, x + getWidth());

			if (hoverChecker.checkHover(mouseX, mouseY)) {
				drawTooltip(mouseX, mouseY, hoverText, 300);
			}
		}
	}

	@Override
	public boolean onMouseClicked(int mouseX, int mouseY, int mouseButton) {
		if (GuiCommandEditor.isInBounds(mouseX, mouseY)
				&& wrappedButton.mousePressed(Minecraft.getMinecraft(), mouseX, mouseY)) {
			wrappedButton.playPressSound(Minecraft.getMinecraft().getSoundHandler());
			onPress();
		}

		return false;
	}

	/**
	 * Invoked when the button is pressed
	 */
	public abstract void onPress();

	private static class GuiClickListenerButton extends GuiButton {

		public GuiClickListenerButton(int x, int y, int width, int height, String text) {
			super(0, x, y, width, height, text);
		}

	}

}

package net.earthcomputer.easyeditors.gui.command.slot;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;

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

	public CommandSlotButton(int width, int height, String text) {
		super(width, height);
		wrappedButton = new GuiClickListenerButton(0, 0, width, height, text);
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
		
		super.draw(x, y, mouseX, mouseY, partialTicks);
	}

	@Override
	public void onMouseClicked(int mouseX, int mouseY, int mouseButton) {
		if (wrappedButton.mousePressed(Minecraft.getMinecraft(), mouseX, mouseY)) {
			wrappedButton.playPressSound(Minecraft.getMinecraft().getSoundHandler());
			onPress();
		}
	}

	/**
	 * Invoked when the button is pressed
	 */
	public abstract void onPress();

	private static class GuiClickListenerButton extends GuiButton {

		public GuiClickListenerButton(int x, int y, int width, int height, String text) {
			super(0, x, y, width, height, text);
		}

		public void setMouseOver(boolean mouseOver) {
			hovered = mouseOver;
		}

	}

}

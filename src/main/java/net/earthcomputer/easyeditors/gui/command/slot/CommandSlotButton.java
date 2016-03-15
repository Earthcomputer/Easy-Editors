package net.earthcomputer.easyeditors.gui.command.slot;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;

import org.lwjgl.opengl.GL11;

import net.earthcomputer.easyeditors.api.util.GeneralUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.config.HoverChecker;

/**
 * A command slot which represents a button
 * 
 * @author Earthcomputer
 *
 */
public abstract class CommandSlotButton extends GuiCommandSlotImpl {

	private static final ResourceLocation buttonTextures = new ResourceLocation("textures/gui/widgets.png");

	private int x;
	private int y;
	private String text;
	private String hoverText;
	private boolean enabled = true;
	private int textColor;

	private boolean drawBackground = true;
	private ResourceLocation buttonImage;
	private int imageWidth;
	private int imageHeight;

	private HoverChecker hoverChecker;

	public CommandSlotButton(int width, int height, String text) {
		this(width, height, text, null);
	}

	public CommandSlotButton(int width, int height, String text, String hoverText) {
		super(width, height);
		this.text = text;
		this.hoverText = hoverText;
	}

	public CommandSlotButton(int width, int height, ResourceLocation image) {
		this(width, height, image, null);
	}

	public CommandSlotButton(int width, int height, ResourceLocation image, String hoverText) {
		super(width, height);
		setImage(image);
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
		return text;
	}

	/**
	 * Sets the text displayed in the button
	 * 
	 * @param text
	 */
	public void setText(String text) {
		this.text = text;
	}

	/**
	 * 
	 * @return The image displayed in the button
	 */
	public ResourceLocation getImage() {
		return buttonImage;
	}

	/**
	 * Sets the image displayed in the button
	 * 
	 * @param image
	 */
	public void setImage(ResourceLocation image) {
		this.buttonImage = image;
		try {
			BufferedImage buffImage = ImageIO
					.read(Minecraft.getMinecraft().getResourceManager().getResource(image).getInputStream());
			this.imageWidth = buffImage.getWidth();
			this.imageHeight = buffImage.getHeight();
		} catch (IOException e) {
			this.buttonImage = null;
		}
	}

	/**
	 * 
	 * @return The text displayed when hovered
	 */
	public String getHoverText() {
		return hoverText;
	}

	/**
	 * Sets the text displayed when hovered
	 * 
	 * @param hoverText
	 */
	public void setHoverText(String hoverText) {
		this.hoverText = hoverText;
	}

	/**
	 * 
	 * @return Whether the button is enabled
	 */
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * Sets whether the button is enabled
	 * 
	 * @param enabled
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	/**
	 * 
	 * @return The text color in the button
	 */
	public int getTextColor() {
		return textColor;
	}

	/**
	 * Sets the text color in the button
	 * 
	 * @param textColor
	 */
	public void setTextColor(int textColor) {
		this.textColor = textColor;
	}

	/**
	 * 
	 * @return Whether the background is drawn
	 */
	public boolean isBackgroundDrawn() {
		return drawBackground;
	}

	/**
	 * Sets whether the background is drawn
	 * 
	 * @param drawBackground
	 */
	public void setBackgroundDrawn(boolean drawBackground) {
		this.drawBackground = drawBackground;
	}

	@Override
	public void draw(int x, int y, int mouseX, int mouseY, float partialTicks) {
		GlStateManager.disableLighting();
		GlStateManager.disableFog();
		this.x = x;
		this.y = y;

		FontRenderer fontRenderer = Minecraft.getMinecraft().fontRendererObj;
		boolean hovered = mouseX >= x && mouseY >= y && mouseX < x + getWidth() && mouseY < y + getHeight()
				&& getContext().isMouseInBounds(mouseX, mouseY);

		if (drawBackground) {
			Minecraft.getMinecraft().getTextureManager().bindTexture(buttonTextures);
			GlStateManager.color(1, 1, 1, 1);
			int hoverState = !enabled ? 0 : (hovered ? 2 : 1);
			GlStateManager.enableBlend();
			GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
			GlStateManager.blendFunc(770, 771);
			drawTexturedModalRect(x, y, 0, 46 + hoverState * 20, getWidth() / 2, getHeight());
			drawTexturedModalRect(x + getWidth() / 2, y, 200 - getWidth() / 2, 46 + hoverState * 20, getWidth() / 2,
					getHeight());
		}

		if (buttonImage != null) {
			Minecraft.getMinecraft().getTextureManager().bindTexture(buttonImage);
			GlStateManager.color(1, 1, 1, 1);
			drawModalRectWithCustomSizedTexture(x + getWidth() / 2 - imageWidth / 2,
					y + getHeight() / 2 - imageHeight / 2, 0, 0, imageWidth, imageHeight, imageWidth, imageHeight);
		}

		int textColor = 0xe0e0e0;

		if (this.textColor != 0) {
			textColor = this.textColor;
		} else if (!enabled) {
			textColor = 0xa0a0a0;
		} else if (hovered) {
			textColor = 0xffffa0;
		}

		drawCenteredString(fontRenderer, text, x + getWidth() / 2, y + (getHeight() - 8) / 2, textColor);

		if (hoverText != null) {
			if (hoverChecker == null)
				hoverChecker = new HoverChecker(y, y + getHeight(), x, x + getWidth(), 1000);
			else
				hoverChecker.updateBounds(y, y + getHeight(), x, x + getWidth());

			if (!getContext().isMouseInBounds(mouseX, mouseY))
				hoverChecker.resetHoverTimer();
			else if (hoverChecker.checkHover(mouseX, mouseY)) {
				drawTooltip(mouseX, mouseY, hoverText, 300);
			}
		}
	}

	@Override
	public boolean onMouseClicked(int mouseX, int mouseY, int mouseButton) {
		boolean hovered = mouseX >= x && mouseY >= y && mouseX < x + getWidth() && mouseY < y + getHeight()
				&& getContext().isMouseInBounds(mouseX, mouseY);
		if (hovered) {
			GeneralUtils.playButtonSound();
			onPress();
		}

		return false;
	}

	/**
	 * Invoked when the button is pressed
	 */
	public abstract void onPress();

}

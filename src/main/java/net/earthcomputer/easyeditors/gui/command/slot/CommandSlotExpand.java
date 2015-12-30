package net.earthcomputer.easyeditors.gui.command.slot;

import java.util.List;

import org.lwjgl.opengl.GL11;

import net.earthcomputer.easyeditors.api.GeneralUtils;
import net.earthcomputer.easyeditors.gui.ISizeChangeListener;
import net.earthcomputer.easyeditors.gui.command.CommandSyntaxException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.config.GuiUtils;

/**
 * A command slot containing a button, which, when clicked, shows some
 * additional options
 * 
 * @author Earthcomputer
 *
 */
public class CommandSlotExpand extends GuiCommandSlotImpl implements ISizeChangeListener {

	private static final ResourceLocation arrowLocation = new ResourceLocation(
			"easyeditors:textures/gui/up_down_arrows.png");
	private static final ResourceLocation buttonLocation = new ResourceLocation("textures/gui/widgets.png");

	private IGuiCommandSlot child;
	private boolean isExpanded = false;
	private boolean hovered;

	public CommandSlotExpand(IGuiCommandSlot child) {
		super(32, 16);
		this.child = child;
		child.addSizeChangeListener(this);
	}

	/**
	 * 
	 * @return Whether expanded
	 */
	public boolean isExpanded() {
		return isExpanded;
	}

	/**
	 * Sets whether expanded
	 * 
	 * @param expanded
	 */
	public void setExpanded(boolean expanded) {
		isExpanded = expanded;
		recalcSize();
	}

	@Override
	public int readFromArgs(String[] args, int index) throws CommandSyntaxException {
		if (index >= args.length) {
			isExpanded = false;
			return 0;
		} else {
			isExpanded = true;
			return child.readFromArgs(args, index);
		}
	}

	@Override
	public void addArgs(List<String> args) {
		if (isExpanded)
			child.addArgs(args);
	}

	@Override
	public void draw(int x, int y, int mouseX, int mouseY, float partialTicks) {
		GlStateManager.color(1, 1, 1, 1);
		hovered = mouseX >= x && mouseY >= y && mouseX < x + 32 && mouseY < y + 16;
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GuiUtils.drawContinuousTexturedBox(buttonLocation, x, y, 0, hovered ? 86 : 66, 32, 16, 200, 20, 3,
				partialTicks);
		GlStateManager.disableDepth();
		Minecraft.getMinecraft().getTextureManager().bindTexture(arrowLocation);
		Gui.drawModalRectWithCustomSizedTexture(x + 8, y, isExpanded ? 16 : 0, hovered ? 16 : 0, 16, 16, 32, 32);

		if (isExpanded)
			child.draw(x, y + 18, mouseX, mouseY, partialTicks);
	}

	@Override
	public void onKeyTyped(char typedChar, int keyCode) {
		if (isExpanded)
			child.onKeyTyped(typedChar, keyCode);
	}

	@Override
	public void onMouseClicked(int mouseX, int mouseY, int mouseButton) {
		if (hovered) {
			setExpanded(!isExpanded);
			GeneralUtils.playButtonSound();
		} else if (isExpanded) {
			child.onMouseClicked(mouseX, mouseY, mouseButton);
		}
	}

	@Override
	public void onMouseReleased(int mouseX, int mouseY, int mouseButton) {
		if (isExpanded) {
			child.onMouseReleased(mouseX, mouseY, mouseButton);
		}
	}

	@Override
	public void onMouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
		if (isExpanded) {
			child.onMouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
		}
	}

	private void recalcSize() {
		if (isExpanded) {
			setWidth(child.getWidth() > 32 ? child.getWidth() : 32);
			setHeight(child.getHeight() + 18);
		} else {
			setWidth(32);
			setHeight(16);
		}
	}

	@Override
	public void onWidthChange(int oldWidth, int newWidth) {
		if (isExpanded) {
			setWidth(newWidth > 16 ? newWidth : 16);
		}
	}

	@Override
	public void onHeightChange(int oldHeight, int newHeight) {
		if (isExpanded) {
			setHeight(newHeight + 18);
		}
	}

}

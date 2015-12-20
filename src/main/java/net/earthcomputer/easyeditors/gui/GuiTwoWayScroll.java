package net.earthcomputer.easyeditors.gui;

import java.io.IOException;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.util.MathHelper;

public abstract class GuiTwoWayScroll extends GuiScreen {

	private int headerHeight;
	private int footerHeight;
	private int virtualWidth;
	private int virtualHeight;
	private int scrollX;
	private int scrollY;

	private int shownWidth;
	private int shownHeight;
	private int maxScrollX;
	private int maxScrollY;
	private int xScrollBarWidth;
	private int yScrollBarHeight;
	private int xScrollBarLeft;
	private int yScrollBarTop;
	private int xScrollBarRight;
	private int yScrollBarBottom;
	private boolean xScrollBarVisible;
	private boolean yScrollBarVisible;

	private int firstXScrollBarPos = -1;
	private int firstYScrollBarPos = -1;
	private int firstMouseX = -1;
	private int firstMouseY = -1;
	private boolean leftButtonDown;

	private int leftKey;
	private int rightKey;
	private int upKey;
	private int downKey;
	private boolean useMouseWheel = true;
	private int xScrollBarPolicy;
	private int yScrollBarPolicy;

	public static final int SHOWN_NEVER = 0;
	public static final int SHOWN_WHEN_NEEDED = 1;
	public static final int SHOWN_ALWAYS = 2;

	public GuiTwoWayScroll(int headerHeight, int footerHeight, int virtualWidth, int virtualHeight) {
		this.headerHeight = headerHeight;
		this.footerHeight = footerHeight;
		this.virtualWidth = virtualWidth;
		this.virtualHeight = virtualHeight;
		scrollX = scrollY = 0;
		xScrollBarPolicy = yScrollBarPolicy = SHOWN_WHEN_NEEDED;
	}

	private boolean hasInitialized = false;

	@Override
	public void initGui() {
		hasInitialized = true;
		refreshScrollBars();
	}

	private void refreshScrollBars() {
		if (!hasInitialized)
			return;
		switch (xScrollBarPolicy) {
		case SHOWN_NEVER:
			xScrollBarVisible = false;
			break;
		case SHOWN_WHEN_NEEDED:
			xScrollBarVisible = virtualWidth > width;
			break;
		case SHOWN_ALWAYS:
			xScrollBarVisible = true;
			break;
		default:
			throw new IllegalStateException("Illegal value for xScrollBarPolicy: " + xScrollBarPolicy);
		}
		shownHeight = height - headerHeight - footerHeight - (xScrollBarVisible ? 6 : 0);
		switch (yScrollBarPolicy) {
		case SHOWN_NEVER:
			yScrollBarVisible = false;
			break;
		case SHOWN_WHEN_NEEDED:
			yScrollBarVisible = virtualHeight > shownHeight;
			break;
		case SHOWN_ALWAYS:
			yScrollBarVisible = true;
			break;
		default:
			throw new IllegalStateException("Illegal value for yScrollBarPolicy: " + yScrollBarPolicy);
		}
		shownWidth = width - (yScrollBarVisible ? 6 : 0);
		if (yScrollBarVisible && !xScrollBarVisible && xScrollBarPolicy == SHOWN_WHEN_NEEDED
				&& virtualWidth > shownWidth) {
			xScrollBarVisible = true;
			shownHeight -= 6;
		}
		maxScrollX = virtualWidth - shownWidth;
		if (maxScrollX < 0)
			maxScrollX = 0;
		maxScrollY = virtualHeight - shownHeight;
		if (maxScrollY < 0)
			maxScrollY = 0;
		xScrollBarWidth = shownWidth * shownWidth / virtualWidth;
		if (xScrollBarWidth > shownWidth)
			xScrollBarWidth = shownWidth;
		yScrollBarHeight = shownHeight * shownHeight / virtualHeight;
		if (yScrollBarHeight > shownHeight)
			yScrollBarHeight = shownHeight;
		xScrollBarLeft = (maxScrollX == 0 ? 0 : (shownWidth - xScrollBarWidth) * scrollX / maxScrollX);
		yScrollBarTop = (maxScrollY == 0 ? 0 : (shownHeight - yScrollBarHeight) * scrollY / maxScrollY) + headerHeight;
		xScrollBarRight = xScrollBarLeft + xScrollBarWidth;
		yScrollBarBottom = yScrollBarTop + yScrollBarHeight;
		return;
	}

	public int getHeaderHeight() {
		return headerHeight;
	}

	public void setHeaderHeight(int headerHeight) {
		this.headerHeight = headerHeight;
		refreshScrollBars();
	}

	public int getFooterHeight() {
		return footerHeight;
	}

	public void setFooterHeight(int footerHeight) {
		this.footerHeight = footerHeight;
		refreshScrollBars();
	}

	public int getVirtualWidth() {
		return virtualWidth;
	}

	public void setVirtualWidth(int virtualWidth) {
		if (virtualWidth <= 0)
			virtualWidth = 1;
		this.virtualWidth = virtualWidth;
		refreshScrollBars();
	}

	public int getVirtualHeight() {
		return virtualHeight;
	}

	public void setVirtualHeight(int virtualHeight) {
		if (virtualHeight <= 0)
			virtualHeight = 1;
		this.virtualHeight = virtualHeight;
		refreshScrollBars();
	}

	public int getScrollX() {
		return scrollX;
	}

	public void setScrollX(int scrollX) {
		this.scrollX = MathHelper.clamp_int(scrollX, 0, maxScrollX);
		refreshScrollBars();
	}

	public void addScrollX(int scrollX) {
		setScrollX(this.scrollX + scrollX);
	}

	public int getScrollY() {
		return scrollY;
	}

	public void setScrollY(int scrollY) {
		this.scrollY = MathHelper.clamp_int(scrollY, 0, maxScrollY);
		refreshScrollBars();
	}

	public void addScrollY(int scrollY) {
		setScrollY(this.scrollY + scrollY);
	}

	public int getMaxScrollX() {
		return maxScrollX;
	}

	public int getMaxScrollY() {
		return maxScrollY;
	}

	public void scrollTo(int x, int y) {
		scrollX = MathHelper.clamp_int(x, 0, maxScrollX);
		scrollY = MathHelper.clamp_int(y, 0, maxScrollY);
		refreshScrollBars();
	}

	public int getShownWidth() {
		return shownWidth;
	}

	public int getShownHeight() {
		return shownHeight;
	}

	protected int getXScrollBarWidth() {
		return xScrollBarWidth;
	}

	protected int getYScrollBarHeight() {
		return yScrollBarHeight;
	}

	protected int getXScrollBarLeft() {
		return xScrollBarLeft;
	}

	protected int getYScrollBarTop() {
		return yScrollBarTop;
	}

	protected int getXScrollBarRight() {
		return xScrollBarRight;
	}

	protected int getYScrollBarBottom() {
		return yScrollBarBottom;
	}

	public int getXScrollBarPolicy() {
		return xScrollBarPolicy;
	}

	public void setXScrollBarPolicy(int xScrollBarPolicy) {
		this.xScrollBarPolicy = xScrollBarPolicy;
		refreshScrollBars();
	}

	public int getYScrollBarPolicy() {
		return yScrollBarPolicy;
	}

	public void setYScrollBarPolicy(int yScrollBarPolicy) {
		this.yScrollBarPolicy = yScrollBarPolicy;
		refreshScrollBars();
	}

	public int getLeftKey() {
		return leftKey;
	}

	public void setLeftKey(int leftKey) {
		this.leftKey = leftKey;
	}

	public int getRightKey() {
		return rightKey;
	}

	public void setRightKey(int rightKey) {
		this.rightKey = rightKey;
	}

	public int getUpKey() {
		return upKey;
	}

	public void setUpKey(int upKey) {
		this.upKey = upKey;
	}

	public int getDownKey() {
		return downKey;
	}

	public void setDownKey(int downKey) {
		this.downKey = downKey;
	}

	public boolean usesMouseWheel() {
		return useMouseWheel;
	}

	public void setUsesMouseWheel(boolean usesMouseWheel) {
		useMouseWheel = usesMouseWheel;
	}

	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		drawContainerBackground();

		drawVirtualScreen(mouseX + scrollX, mouseY + scrollY - headerHeight, partialTicks, scrollX, scrollY,
				headerHeight);
		GlStateManager.disableLighting();
		GlStateManager.disableFog();
		Tessellator tessellator = Tessellator.getInstance();
		WorldRenderer worldRenderer = tessellator.getWorldRenderer();
		GlStateManager.disableDepth();

		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 0, 1);
		GlStateManager.disableAlpha();
		GlStateManager.shadeModel(GL11.GL_SMOOTH);
		GlStateManager.disableTexture2D();
		worldRenderer.startDrawingQuads();
		worldRenderer.setColorRGBA_I(0, 255);
		worldRenderer.setColorRGBA_I(0, 0);
		worldRenderer.addVertexWithUV(0, headerHeight + 4, 0, 0, 1);
		worldRenderer.addVertexWithUV(width, headerHeight + 4, 0, 1, 1);
		worldRenderer.setColorRGBA_I(0, 255);
		worldRenderer.addVertexWithUV(width, headerHeight, 0, 1, 0);
		worldRenderer.addVertexWithUV(0, headerHeight, 0, 0, 0);
		tessellator.draw();
		worldRenderer.startDrawingQuads();
		worldRenderer.setColorRGBA_I(0, 255);
		worldRenderer.addVertexWithUV(0, height - footerHeight, 0, 0, 1);
		worldRenderer.addVertexWithUV(width, height - footerHeight, 0, 1, 1);
		worldRenderer.setColorRGBA_I(0, 0);
		worldRenderer.addVertexWithUV(width, height - footerHeight - 4, 0, 1, 0);
		worldRenderer.addVertexWithUV(0, height - footerHeight - 4, 0, 0, 0);
		tessellator.draw();
		GlStateManager.enableTexture2D();

		overlayBackground(0, headerHeight);
		overlayBackground(height - footerHeight, height);
		drawForeground(mouseX, mouseY, partialTicks);

		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 0, 1);
		GlStateManager.disableAlpha();
		GlStateManager.shadeModel(GL11.GL_SMOOTH);
		GlStateManager.disableTexture2D();

		if (yScrollBarVisible) {
			worldRenderer.startDrawingQuads();
			worldRenderer.setColorRGBA_I(0, 255);
			worldRenderer.addVertexWithUV(width - 6, height - footerHeight, 0, 0, 1);
			worldRenderer.addVertexWithUV(width, height - footerHeight, 0, 1, 1);
			worldRenderer.addVertexWithUV(width, headerHeight, 0, 1, 0);
			worldRenderer.addVertexWithUV(width - 6, headerHeight, 0, 0, 0);
			tessellator.draw();
		}
		if (xScrollBarVisible) {
			worldRenderer.startDrawingQuads();
			worldRenderer.setColorRGBA_I(0, 255);
			worldRenderer.addVertexWithUV(0, height - footerHeight, 0, 0, 1);
			worldRenderer.addVertexWithUV(width, height - footerHeight, 0, 1, 1);
			worldRenderer.addVertexWithUV(width, height - footerHeight - 6, 0, 1, 0);
			worldRenderer.addVertexWithUV(0, height - footerHeight - 6, 0, 0, 0);
			tessellator.draw();
		}
		if (xScrollBarVisible && yScrollBarVisible) {
			worldRenderer.startDrawingQuads();
			worldRenderer.setColorRGBA_I(0x404040, 255);
			worldRenderer.addVertexWithUV(width - 6, height - footerHeight, 0, 0, 1);
			worldRenderer.addVertexWithUV(width, height - footerHeight, 0, 1, 1);
			worldRenderer.addVertexWithUV(width, height - footerHeight - 6, 0, 1, 0);
			worldRenderer.addVertexWithUV(width - 6, height - footerHeight - 6, 0, 0, 0);
			tessellator.draw();
		}
		if (yScrollBarVisible) {
			worldRenderer.startDrawingQuads();
			worldRenderer.setColorRGBA_I(0x808080, 255);
			worldRenderer.addVertexWithUV(width - 6, yScrollBarBottom, 0, 0, 1);
			worldRenderer.addVertexWithUV(width, yScrollBarBottom, 0, 1, 1);
			worldRenderer.addVertexWithUV(width, yScrollBarTop, 0, 1, 0);
			worldRenderer.addVertexWithUV(width - 6, yScrollBarTop, 0, 0, 0);
			tessellator.draw();
		}
		if (xScrollBarVisible) {
			worldRenderer.startDrawingQuads();
			worldRenderer.setColorRGBA_I(0x808080, 255);
			worldRenderer.addVertexWithUV(xScrollBarLeft, height - footerHeight, 0, 0, 1);
			worldRenderer.addVertexWithUV(xScrollBarRight, height - footerHeight, 0, 1, 1);
			worldRenderer.addVertexWithUV(xScrollBarRight, height - footerHeight - 6, 0, 1, 0);
			worldRenderer.addVertexWithUV(xScrollBarLeft, height - footerHeight - 6, 0, 0, 0);
			tessellator.draw();
		}
		if (yScrollBarVisible) {
			worldRenderer.startDrawingQuads();
			worldRenderer.setColorRGBA_I(0xc0c0c0, 255);
			worldRenderer.addVertexWithUV(width - 6, yScrollBarBottom - 1, 0, 0, 1);
			worldRenderer.addVertexWithUV(width - 1, yScrollBarBottom - 1, 0, 1, 1);
			worldRenderer.addVertexWithUV(width - 1, yScrollBarTop, 0, 1, 0);
			worldRenderer.addVertexWithUV(width - 6, yScrollBarTop, 0, 0, 0);
			tessellator.draw();
		}
		if (xScrollBarVisible) {
			worldRenderer.startDrawingQuads();
			worldRenderer.setColorRGBA_I(0xc0c0c0, 255);
			worldRenderer.addVertexWithUV(xScrollBarLeft, height - footerHeight - 1, 0, 0, 1);
			worldRenderer.addVertexWithUV(xScrollBarRight - 1, height - footerHeight - 1, 0, 1, 1);
			worldRenderer.addVertexWithUV(xScrollBarRight - 1, height - footerHeight - 6, 0, 1, 0);
			worldRenderer.addVertexWithUV(xScrollBarLeft, height - footerHeight - 6, 0, 0, 0);
			tessellator.draw();
		}

		GlStateManager.enableTexture2D();
		GlStateManager.shadeModel(GL11.GL_FLAT);
		GlStateManager.enableAlpha();
		GlStateManager.disableBlend();

		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	protected void drawContainerBackground() {
		Tessellator tessellator = Tessellator.getInstance();
		WorldRenderer worldRenderer = tessellator.getWorldRenderer();
		mc.getTextureManager().bindTexture(Gui.optionsBackground);
		GlStateManager.color(1, 1, 1, 1);
		worldRenderer.startDrawingQuads();
		worldRenderer.setColorOpaque_I(0x202020);
		worldRenderer.addVertexWithUV(0, height - footerHeight, 0, (float) scrollX / 32,
				(float) (height - footerHeight + scrollY) / 32);
		worldRenderer.addVertexWithUV(width, height - footerHeight, 0, (float) (width + scrollX) / 32,
				(float) (height - footerHeight + scrollY) / 32);
		worldRenderer.addVertexWithUV(width, headerHeight, 0, (float) (width + scrollX) / 32,
				(float) (headerHeight + scrollY) / 32);
		worldRenderer.addVertexWithUV(0, headerHeight, 0, (float) scrollX / 32, (float) (headerHeight + scrollY) / 32);
		tessellator.draw();
	}

	protected void overlayBackground(int top, int bottom) {
		Tessellator tessellator = Tessellator.getInstance();
		WorldRenderer worldRenderer = tessellator.getWorldRenderer();
		this.mc.getTextureManager().bindTexture(Gui.optionsBackground);
		GlStateManager.color(1, 1, 1, 1);
		worldRenderer.startDrawingQuads();
		worldRenderer.setColorRGBA_I(0x404040, 255);
		worldRenderer.addVertexWithUV(0, bottom, 0, 0, (float) bottom / 32);
		worldRenderer.addVertexWithUV(width, bottom, 0, (float) width / 32, (float) bottom / 32);
		worldRenderer.addVertexWithUV(width, top, 0, (float) this.width / 32, (float) top / 32);
		worldRenderer.addVertexWithUV(0, top, 0, 0, (float) top / 32);
		tessellator.draw();
	}

	protected abstract void drawVirtualScreen(int virtualMouseX, int virtualMouseY, float partialTicks, int scrollX,
			int scrollY, int headerHeight);

	protected abstract void drawForeground(int mouseX, int mouseY, float partialTicks);

	@Override
	public void keyTyped(char typedChar, int keyCode) throws IOException {
		if (keyCode == leftKey) {
			addScrollX(-20);
		} else if (keyCode == rightKey) {
			addScrollX(20);
		} else if (keyCode == upKey) {
			addScrollY(-20);
		} else if (keyCode == downKey) {
			addScrollY(20);
		} else {
			super.keyTyped(typedChar, keyCode);
		}
	}

	@Override
	public void handleMouseInput() throws IOException {
		super.handleMouseInput();

		if (useMouseWheel) {
			int amtToScroll = Mouse.getEventDWheel();
			if (amtToScroll < 0)
				amtToScroll = 20;
			else if (amtToScroll > 0)
				amtToScroll = -20;
			if (yScrollBarVisible) {
				addScrollY(amtToScroll);
			} else if (xScrollBarVisible) {
				addScrollX(amtToScroll);
			}
		}
	}

	@Override
	public void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		if (mouseButton == 0) {
			if (yScrollBarVisible && mouseX >= width - 6 && mouseY >= headerHeight && mouseX < width
					&& mouseY >= headerHeight && mouseY < headerHeight + shownHeight) {
				if (mouseY < getYScrollBarTop() || mouseY >= getYScrollBarBottom()) {
					setScrollY(
							(mouseY - headerHeight - getYScrollBarHeight() / 2) * shownHeight / getYScrollBarHeight());
				}
				firstYScrollBarPos = scrollY;
				firstMouseY = mouseY;
			} else if (xScrollBarVisible && mouseX >= 0 && mouseY >= height - footerHeight - 6 && mouseX < shownWidth
					&& mouseY < height - footerHeight) {
				if (mouseX < getXScrollBarLeft() || mouseX >= getXScrollBarRight()) {
					setScrollX((mouseX - getXScrollBarWidth() / 2) * shownWidth / getXScrollBarWidth());
				}
				firstXScrollBarPos = scrollX;
				firstMouseX = mouseX;
			} else if (mouseY > headerHeight && mouseY < height - footerHeight) {
				mouseClickedVirtual(mouseX + scrollX, mouseY + scrollY - headerHeight, mouseButton);
			}
		} else if (mouseY > headerHeight && mouseY < height - footerHeight) {
			mouseClickedVirtual(mouseX + scrollX, mouseY + scrollY - headerHeight, mouseButton);
		}
		super.mouseClicked(mouseX, mouseY, mouseButton);
	}

	protected void mouseClickedVirtual(int virtualMouseX, int virtualMouseY, int mouseButton) {
	}

	@Override
	public void mouseReleased(int mouseX, int mouseY, int state) {
		if (state == 0) { // This test does not work
			firstXScrollBarPos = firstYScrollBarPos = firstMouseX = firstMouseY = -1;
		}
		if (mouseY > headerHeight && mouseY < height - footerHeight)
			mouseReleasedVirtual(mouseX + scrollX, mouseY + scrollY - headerHeight, state);
		super.mouseReleased(mouseX, mouseY, state);
	}

	protected void mouseReleasedVirtual(int virtualMouseX, int virtualMouseY, int releasedButton) {
	}

	@Override
	public void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
		if (firstXScrollBarPos != -1) {
			int dx = mouseX - firstMouseX;
			float multiplier = (float) getShownWidth() / getXScrollBarWidth();
			setScrollX(firstXScrollBarPos + MathHelper.ceiling_float_int(dx * multiplier));
		} else if (firstYScrollBarPos != -1) {
			int dy = mouseY - firstMouseY;
			float multiplier = (float) getShownHeight() / getYScrollBarHeight();
			setScrollY(firstYScrollBarPos + MathHelper.ceiling_float_int(dy * multiplier));
		} else if (mouseY > headerHeight && mouseY < height - footerHeight) {
			mouseClickMoveVirtual(mouseX + scrollX, mouseY + scrollY - headerHeight, clickedMouseButton,
					timeSinceLastClick);
		}
	}

	protected void mouseClickMoveVirtual(int virtualMouseX, int virtualMouseY, int clickedMouseButton,
			long timeSinceLastClick) {
	}

}

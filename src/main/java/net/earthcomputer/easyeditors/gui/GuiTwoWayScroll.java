package net.earthcomputer.easyeditors.gui;

import java.io.IOException;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.MathHelper;

/**
 * Used to represent a GuiScreen whose main contents, or "virtual size", may
 * exceed the physical size of the screen in the horizontal or vertical
 * direction, or both
 * 
 * @author Earthcomputer
 *
 */
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

	private int leftKey;
	private int rightKey;
	private int upKey;
	private int downKey;
	private boolean useMouseWheel = true;
	private int xScrollBarPolicy;
	private int yScrollBarPolicy;

	/**
	 * Never show the scroll bar
	 */
	public static final int SHOWN_NEVER = 0;
	/**
	 * Only show the scroll bar when the virtual width exceeds the physical
	 * width
	 */
	public static final int SHOWN_WHEN_NEEDED = 1;
	/**
	 * Always show the scroll bar
	 */
	public static final int SHOWN_ALWAYS = 2;

	/**
	 * Creates a GuiTwoWayScroll with the given headerHeight, footerHeight,
	 * initial virtualWidth and initial virtualHeight
	 * 
	 * @param headerHeight
	 *            - The height of the header, where the title generally goes
	 * @param footerHeight
	 *            - The height of the footer, where the done and cancel buttons
	 *            normally go
	 * @param virtualWidth
	 * @param virtualHeight
	 */
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

	/**
	 * 
	 * @return The height of the header
	 */
	public int getHeaderHeight() {
		return headerHeight;
	}

	/**
	 * Sets the height of the header
	 * 
	 * @param headerHeight
	 */
	public void setHeaderHeight(int headerHeight) {
		this.headerHeight = headerHeight;
		refreshScrollBars();
	}

	/**
	 * 
	 * @return The height of the footer
	 */
	public int getFooterHeight() {
		return footerHeight;
	}

	/**
	 * Sets the height of the footer
	 * 
	 * @param footerHeight
	 */
	public void setFooterHeight(int footerHeight) {
		this.footerHeight = footerHeight;
		refreshScrollBars();
	}

	/**
	 * 
	 * @return The width of the main contents, which may exceed the physical
	 *         width
	 */
	public int getVirtualWidth() {
		return virtualWidth;
	}

	/**
	 * Sets the width of the main contents, which may exceed the physical width
	 * 
	 * @param virtualWidth
	 */
	public void setVirtualWidth(int virtualWidth) {
		if (virtualWidth <= 0)
			virtualWidth = 1;
		this.virtualWidth = virtualWidth;
		refreshScrollBars();
	}

	/**
	 * 
	 * @return The height of the main contents, which may exceed the physical
	 *         height
	 */
	public int getVirtualHeight() {
		return virtualHeight;
	}

	/**
	 * Sets the height of the main contents, which may exceed the physical
	 * height
	 * 
	 * @param virtualHeight
	 */
	public void setVirtualHeight(int virtualHeight) {
		if (virtualHeight <= 0)
			virtualHeight = 1;
		this.virtualHeight = virtualHeight;
		refreshScrollBars();
	}

	/**
	 * 
	 * @return The amount scrolled horizontally
	 */
	public int getScrollX() {
		return scrollX;
	}

	/**
	 * Sets the amount scrolled horizontally
	 * 
	 * @param scrollX
	 */
	public void setScrollX(int scrollX) {
		this.scrollX = MathHelper.clamp_int(scrollX, 0, maxScrollX);
		refreshScrollBars();
	}

	/**
	 * Scrolls to the right by the given amount
	 * 
	 * @param scrollX
	 */
	public void addScrollX(int scrollX) {
		setScrollX(this.scrollX + scrollX);
	}

	/**
	 * 
	 * @return The amount scrolled vertically
	 */
	public int getScrollY() {
		return scrollY;
	}

	/**
	 * Sets the amount scrolled vertically
	 * 
	 * @param scrollY
	 */
	public void setScrollY(int scrollY) {
		this.scrollY = MathHelper.clamp_int(scrollY, 0, maxScrollY);
		refreshScrollBars();
	}

	/**
	 * Scrolls down by the given amount
	 * 
	 * @param scrollY
	 */
	public void addScrollY(int scrollY) {
		setScrollY(this.scrollY + scrollY);
	}

	/**
	 * @return The maximum amount that can be scrolled horizontally
	 */
	public int getMaxScrollX() {
		return maxScrollX;
	}

	/**
	 * 
	 * @return The maximum amount that can be scrolled vertically
	 */
	public int getMaxScrollY() {
		return maxScrollY;
	}

	/**
	 * Scrolls both horizontally and vertically at the same time, with some
	 * performance gains
	 * 
	 * @param x
	 * @param y
	 */
	public void scrollTo(int x, int y) {
		scrollX = MathHelper.clamp_int(x, 0, maxScrollX);
		scrollY = MathHelper.clamp_int(y, 0, maxScrollY);
		refreshScrollBars();
	}

	/**
	 * 
	 * @return The width of the area of the main contents the user can see
	 */
	public int getShownWidth() {
		return shownWidth;
	}

	/**
	 * 
	 * @return The height of the area of the main contents the user can see
	 */
	public int getShownHeight() {
		return shownHeight;
	}

	/**
	 * 
	 * @return The width of the horizontal scroll bar, as it appears on the
	 *         screen
	 */
	protected int getXScrollBarWidth() {
		return xScrollBarWidth;
	}

	/**
	 * 
	 * @return The height of the vertical scroll bar, as it appears on the
	 *         screen
	 */
	protected int getYScrollBarHeight() {
		return yScrollBarHeight;
	}

	/**
	 * 
	 * @return The x-position of the left of the horizontal scroll bar, as it
	 *         appears on the screen
	 */
	protected int getXScrollBarLeft() {
		return xScrollBarLeft;
	}

	/**
	 * 
	 * @return The y-position of the top of the vertical scroll bar, as it
	 *         appears on the screen
	 */
	protected int getYScrollBarTop() {
		return yScrollBarTop;
	}

	/**
	 * 
	 * @return The x-position of the right of the horizontal scroll bar, as it
	 *         appears on the screen
	 */
	protected int getXScrollBarRight() {
		return xScrollBarRight;
	}

	/**
	 * 
	 * @return The y-position of the bottom of the vertical scroll bar, as it
	 *         appears on the screen
	 */
	protected int getYScrollBarBottom() {
		return yScrollBarBottom;
	}

	/**
	 * 
	 * @return The horizontal scroll bar policy, either {@link #SHOWN_NEVER},
	 *         {@link #SHOWN_WHEN_NEEDED} or {@link #SHOWN_ALWAYS}
	 */
	public int getXScrollBarPolicy() {
		return xScrollBarPolicy;
	}

	/**
	 * Sets the horizontal scroll bar policy, either {@link #SHOWN_NEVER},
	 * {@link #SHOWN_WHEN_NEEDED} or {@link #SHOWN_ALWAYS}
	 * 
	 * @param xScrollBarPolicy
	 */
	public void setXScrollBarPolicy(int xScrollBarPolicy) {
		this.xScrollBarPolicy = xScrollBarPolicy;
		refreshScrollBars();
	}

	/**
	 * 
	 * @return The vertical scroll bar policy, either {@link #SHOWN_NEVER},
	 *         {@link #SHOWN_WHEN_NEEDED} or {@link #SHOWN_ALWAYS}
	 */
	public int getYScrollBarPolicy() {
		return yScrollBarPolicy;
	}

	/**
	 * Sets the vertical scroll bar policy, either {@link #SHOWN_NEVER},
	 * {@link #SHOWN_WHEN_NEEDED} or {@link #SHOWN_ALWAYS}
	 * 
	 * @param yScrollBarPolicy
	 */
	public void setYScrollBarPolicy(int yScrollBarPolicy) {
		this.yScrollBarPolicy = yScrollBarPolicy;
		refreshScrollBars();
	}

	/**
	 * 
	 * @return The keycode of the key which causes the horizontal scroll bar to
	 *         scroll left
	 */
	public int getLeftKey() {
		return leftKey;
	}

	/**
	 * Sets the key which causes the horizontal scroll bar to scroll left
	 * 
	 * @param leftKey
	 */
	public void setLeftKey(int leftKey) {
		this.leftKey = leftKey;
	}

	/**
	 * 
	 * @return The keycode of the key which causes the horizontal scroll bar to
	 *         scroll right
	 */
	public int getRightKey() {
		return rightKey;
	}

	/**
	 * Sets the key which causes the horizontal scroll bar to scroll right
	 * 
	 * @param rightKey
	 */
	public void setRightKey(int rightKey) {
		this.rightKey = rightKey;
	}

	/**
	 * 
	 * @return The keycode of the key which causes the vertical scroll bar to
	 *         scroll up
	 */
	public int getUpKey() {
		return upKey;
	}

	/**
	 * Sets the key which causes the vertical scroll bar to scroll up
	 * 
	 * @param upKey
	 */
	public void setUpKey(int upKey) {
		this.upKey = upKey;
	}

	/**
	 * 
	 * @return The keycode of the key which causes the vertical scroll bar to
	 *         scroll down
	 */
	public int getDownKey() {
		return downKey;
	}

	/**
	 * Sets the key which causes the vertical scroll bar to scroll down
	 * 
	 * @param downKey
	 */
	public void setDownKey(int downKey) {
		this.downKey = downKey;
	}

	/**
	 * 
	 * @return Whether the mouse wheel can be used to scroll
	 */
	public boolean usesMouseWheel() {
		return useMouseWheel;
	}

	/**
	 * Sets whether the mouse wheel can be used to scroll
	 * 
	 * @param usesMouseWheel
	 */
	public void setUsesMouseWheel(boolean usesMouseWheel) {
		useMouseWheel = usesMouseWheel;
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		drawContainerBackground();

		drawVirtualScreen(mouseX, mouseY, partialTicks, scrollX, scrollY, headerHeight);
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
		// worldRenderer.startDrawingQuads();
		// worldRenderer.setColorRGBA_I(0, 0);
		// worldRenderer.addVertexWithUV(0, headerHeight + 4, 0, 0, 1);
		// worldRenderer.addVertexWithUV(width, headerHeight + 4, 0, 1, 1);
		// worldRenderer.setColorRGBA_I(0, 255);
		// worldRenderer.addVertexWithUV(width, headerHeight, 0, 1, 0);
		// worldRenderer.addVertexWithUV(0, headerHeight, 0, 0, 0);
		worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
		worldRenderer.pos(0, headerHeight + 4, 0).tex(0, 1).color(0, 0, 0, 0).endVertex();
		worldRenderer.pos(width, headerHeight + 4, 0).tex(1, 1).color(0, 0, 0, 0).endVertex();
		worldRenderer.pos(width, headerHeight, 0).tex(1, 0).color(0, 0, 0, 255).endVertex();
		worldRenderer.pos(0, headerHeight, 0).tex(0, 0).color(0, 0, 0, 255).endVertex();
		tessellator.draw();
		// worldRenderer.startDrawingQuads();
		// worldRenderer.setColorRGBA_I(0, 255);
		// worldRenderer.addVertexWithUV(0, height - footerHeight, 0, 0, 1);
		// worldRenderer.addVertexWithUV(width, height - footerHeight, 0, 1, 1);
		// worldRenderer.setColorRGBA_I(0, 0);
		// worldRenderer.addVertexWithUV(width, height - footerHeight - 4, 0, 1,
		// 0);
		// worldRenderer.addVertexWithUV(0, height - footerHeight - 4, 0, 0, 0);
		worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
		worldRenderer.pos(0, height - footerHeight, 0).tex(0, 1).color(0, 0, 0, 255).endVertex();
		worldRenderer.pos(width, height - footerHeight, 0).tex(1, 1).color(0, 0, 0, 255).endVertex();
		worldRenderer.pos(width, height - footerHeight - 4, 0).tex(1, 0).color(0, 0, 0, 0).endVertex();
		worldRenderer.pos(0, height - footerHeight - 4, 0).tex(0, 0).color(0, 0, 0, 0).endVertex();
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
			// worldRenderer.startDrawingQuads();
			// worldRenderer.setColorRGBA_I(0, 255);
			// worldRenderer.addVertexWithUV(width - 6, height - footerHeight,
			// 0, 0, 1);
			// worldRenderer.addVertexWithUV(width, height - footerHeight, 0, 1,
			// 1);
			// worldRenderer.addVertexWithUV(width, headerHeight, 0, 1, 0);
			// worldRenderer.addVertexWithUV(width - 6, headerHeight, 0, 0, 0);
			worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
			worldRenderer.pos(width - 6, height - footerHeight, 0).tex(0, 1).color(0, 0, 0, 255).endVertex();
			worldRenderer.pos(width, height - footerHeight, 0).tex(1, 1).color(0, 0, 0, 255).endVertex();
			worldRenderer.pos(width, headerHeight, 0).tex(1, 0).color(0, 0, 0, 255).endVertex();
			worldRenderer.pos(width - 6, headerHeight, 0).tex(0, 0).color(0, 0, 0, 255).endVertex();
			tessellator.draw();
		}
		if (xScrollBarVisible) {
			// worldRenderer.startDrawingQuads();
			// worldRenderer.setColorRGBA_I(0, 255);
			// worldRenderer.addVertexWithUV(0, height - footerHeight, 0, 0, 1);
			// worldRenderer.addVertexWithUV(width, height - footerHeight, 0, 1,
			// 1);
			// worldRenderer.addVertexWithUV(width, height - footerHeight - 6,
			// 0, 1, 0);
			// worldRenderer.addVertexWithUV(0, height - footerHeight - 6, 0, 0,
			// 0);
			worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
			worldRenderer.pos(0, height - footerHeight, 0).tex(0, 1).color(0, 0, 0, 255).endVertex();
			worldRenderer.pos(width, height - footerHeight, 0).tex(1, 1).color(0, 0, 0, 255).endVertex();
			worldRenderer.pos(width, height - footerHeight - 6, 0).tex(1, 0).color(0, 0, 0, 255).endVertex();
			worldRenderer.pos(0, height - footerHeight - 6, 0).tex(0, 0).color(0, 0, 0, 255).endVertex();
			tessellator.draw();
		}
		if (xScrollBarVisible && yScrollBarVisible) {
			// worldRenderer.startDrawingQuads();
			// worldRenderer.setColorRGBA_I(0x404040, 255);
			// worldRenderer.addVertexWithUV(width - 6, height - footerHeight,
			// 0, 0, 1);
			// worldRenderer.addVertexWithUV(width, height - footerHeight, 0, 1,
			// 1);
			// worldRenderer.addVertexWithUV(width, height - footerHeight - 6,
			// 0, 1, 0);
			// worldRenderer.addVertexWithUV(width - 6, height - footerHeight -
			// 6, 0, 0, 0);
			worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
			worldRenderer.pos(width - 6, height - footerHeight, 0).tex(0, 1).color(0x40, 0x40, 0x40, 255).endVertex();
			worldRenderer.pos(width, height - footerHeight, 0).tex(1, 1).color(0x40, 0x40, 0x40, 255).endVertex();
			worldRenderer.pos(width, height - footerHeight - 6, 0).tex(1, 0).color(0x40, 0x40, 0x40, 255).endVertex();
			worldRenderer.pos(height - 6, height - footerHeight - 6, 0).tex(0, 0).color(0x40, 0x40, 0x40, 255)
					.endVertex();
			tessellator.draw();
		}
		if (yScrollBarVisible) {
			// worldRenderer.startDrawingQuads();
			// worldRenderer.setColorRGBA_I(0x808080, 255);
			// worldRenderer.addVertexWithUV(width - 6, yScrollBarBottom, 0, 0,
			// 1);
			// worldRenderer.addVertexWithUV(width, yScrollBarBottom, 0, 1, 1);
			// worldRenderer.addVertexWithUV(width, yScrollBarTop, 0, 1, 0);
			// worldRenderer.addVertexWithUV(width - 6, yScrollBarTop, 0, 0, 0);
			worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
			worldRenderer.pos(width - 6, yScrollBarBottom, 0).tex(0, 1).color(0x80, 0x80, 0x80, 255).endVertex();
			worldRenderer.pos(width, yScrollBarBottom, 0).tex(1, 1).color(0x80, 0x80, 0x80, 255).endVertex();
			worldRenderer.pos(width, yScrollBarTop, 0).tex(1, 0).color(0x80, 0x80, 0x80, 255).endVertex();
			worldRenderer.pos(width - 6, yScrollBarTop, 0).tex(0, 0).color(0x80, 0x80, 0x80, 255).endVertex();
			tessellator.draw();
		}
		if (xScrollBarVisible) {
			// worldRenderer.startDrawingQuads();
			// worldRenderer.setColorRGBA_I(0x808080, 255);
			// worldRenderer.addVertexWithUV(xScrollBarLeft, height -
			// footerHeight, 0, 0, 1);
			// worldRenderer.addVertexWithUV(xScrollBarRight, height -
			// footerHeight, 0, 1, 1);
			// worldRenderer.addVertexWithUV(xScrollBarRight, height -
			// footerHeight - 6, 0, 1, 0);
			// worldRenderer.addVertexWithUV(xScrollBarLeft, height -
			// footerHeight - 6, 0, 0, 0);
			worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
			worldRenderer.pos(xScrollBarLeft, height - footerHeight, 0).tex(0, 1).color(0x80, 0x80, 0x80, 255)
					.endVertex();
			worldRenderer.pos(xScrollBarRight, height - footerHeight, 0).tex(1, 1).color(0x80, 0x80, 0x80, 255)
					.endVertex();
			worldRenderer.pos(xScrollBarRight, height - footerHeight - 6, 0).tex(1, 0).color(0x80, 0x80, 0x80, 255)
					.endVertex();
			worldRenderer.pos(xScrollBarLeft, height - footerHeight - 6, 0).tex(0, 0).color(0x80, 0x80, 0x80, 255)
					.endVertex();
			tessellator.draw();
		}
		if (yScrollBarVisible) {
			// worldRenderer.startDrawingQuads();
			// worldRenderer.setColorRGBA_I(0xc0c0c0, 255);
			// worldRenderer.addVertexWithUV(width - 6, yScrollBarBottom - 1, 0,
			// 0, 1);
			// worldRenderer.addVertexWithUV(width - 1, yScrollBarBottom - 1, 0,
			// 1, 1);
			// worldRenderer.addVertexWithUV(width - 1, yScrollBarTop, 0, 1, 0);
			// worldRenderer.addVertexWithUV(width - 6, yScrollBarTop, 0, 0, 0);
			worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
			worldRenderer.pos(width - 6, yScrollBarBottom - 1, 0).tex(0, 1).color(0xc0, 0xc0, 0xc0, 255).endVertex();
			worldRenderer.pos(width - 1, yScrollBarBottom - 1, 0).tex(1, 1).color(0xc0, 0xc0, 0xc0, 255).endVertex();
			worldRenderer.pos(width - 1, yScrollBarTop, 0).tex(1, 0).color(0xc0, 0xc0, 0xc0, 255).endVertex();
			worldRenderer.pos(width - 6, yScrollBarTop, 0).tex(0, 0).color(0xc0, 0xc0, 0xc0, 255).endVertex();
			tessellator.draw();
		}
		if (xScrollBarVisible) {
			// worldRenderer.startDrawingQuads();
			// worldRenderer.setColorRGBA_I(0xc0c0c0, 255);
			// worldRenderer.addVertexWithUV(xScrollBarLeft, height -
			// footerHeight - 1, 0, 0, 1);
			// worldRenderer.addVertexWithUV(xScrollBarRight - 1, height -
			// footerHeight - 1, 0, 1, 1);
			// worldRenderer.addVertexWithUV(xScrollBarRight - 1, height -
			// footerHeight - 6, 0, 1, 0);
			// worldRenderer.addVertexWithUV(xScrollBarLeft, height -
			// footerHeight - 6, 0, 0, 0);
			worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
			worldRenderer.pos(xScrollBarLeft, height - footerHeight - 1, 0).tex(0, 1).color(0xc0, 0xc0, 0xc0, 255)
					.endVertex();
			worldRenderer.pos(xScrollBarRight, height - footerHeight - 1, 0).tex(1, 1).color(0xc0, 0xc0, 0xc0, 255)
					.endVertex();
			worldRenderer.pos(xScrollBarRight - 1, height - footerHeight - 6, 0).tex(1, 0).color(0xc0, 0xc0, 0xc0, 255)
					.endVertex();
			worldRenderer.pos(xScrollBarLeft, height - footerHeight - 6, 0).tex(0, 0).color(0xc0, 0xc0, 0xc0, 255)
					.endVertex();
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
		// worldRenderer.startDrawingQuads();
		// worldRenderer.setColorOpaque_I(0x202020);
		// worldRenderer.addVertexWithUV(0, height - footerHeight, 0, (float)
		// scrollX / 32,
		// (float) (height - footerHeight + scrollY) / 32);
		// worldRenderer.addVertexWithUV(width, height - footerHeight, 0,
		// (float) (width + scrollX) / 32,
		// (float) (height - footerHeight + scrollY) / 32);
		// worldRenderer.addVertexWithUV(width, headerHeight, 0, (float) (width
		// + scrollX) / 32,
		// (float) (headerHeight + scrollY) / 32);
		// worldRenderer.addVertexWithUV(0, headerHeight, 0, (float) scrollX /
		// 32, (float) (headerHeight + scrollY) / 32);
		worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
		worldRenderer.pos(0, height - footerHeight, 0)
				.tex((float) scrollX / 32, (float) (height - footerHeight + scrollY) / 32).color(0x20, 0x20, 0x20, 255)
				.endVertex();
		worldRenderer.pos(width, height - footerHeight, 0)
				.tex((float) (width + scrollX) / 32, (float) (height - footerHeight + scrollY) / 32)
				.color(0x20, 0x20, 0x20, 255).endVertex();
		worldRenderer.pos(width, headerHeight, 0)
				.tex((float) (width + scrollX) / 32, (float) (headerHeight + scrollY) / 32).color(0x20, 0x20, 0x20, 255)
				.endVertex();
		worldRenderer.pos(0, headerHeight, 0).tex((float) scrollX / 32, (float) (headerHeight + scrollY) / 32)
				.color(0x20, 0x20, 0x20, 255).endVertex();
		tessellator.draw();
	}

	protected void overlayBackground(int top, int bottom) {
		Tessellator tessellator = Tessellator.getInstance();
		WorldRenderer worldRenderer = tessellator.getWorldRenderer();
		this.mc.getTextureManager().bindTexture(Gui.optionsBackground);
		GlStateManager.color(1, 1, 1, 1);
		// worldRenderer.startDrawingQuads();
		// worldRenderer.setColorRGBA_I(0x404040, 255);
		// worldRenderer.addVertexWithUV(0, bottom, 0, 0, (float) bottom / 32);
		// worldRenderer.addVertexWithUV(width, bottom, 0, (float) width / 32,
		// (float) bottom / 32);
		// worldRenderer.addVertexWithUV(width, top, 0, (float) width / 32,
		// (float) top / 32);
		// worldRenderer.addVertexWithUV(0, top, 0, 0, (float) top / 32);
		worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
		worldRenderer.pos(0, bottom, 0).tex(0, (float) bottom / 32).color(0x40, 0x40, 0x40, 255).endVertex();
		worldRenderer.pos(width, bottom, 0).tex((float) width / 32, (float) bottom / 32).color(0x40, 0x40, 0x40, 255)
				.endVertex();
		worldRenderer.pos(width, top, 0).tex((float) width / 32, (float) top / 32).color(0x40, 0x40, 0x40, 255)
				.endVertex();
		worldRenderer.pos(0, top, 0).tex(0, (float) top / 32).color(0x40, 0x40, 0x40, 255).endVertex();
		tessellator.draw();
	}

	/**
	 * Draws the main contents
	 * 
	 * @param mouseX
	 * @param mouseY
	 * @param partialTicks
	 * @param scrollX
	 * @param scrollY
	 * @param headerHeight
	 */
	protected abstract void drawVirtualScreen(int mouseX, int mouseY, float partialTicks, int scrollX, int scrollY,
			int headerHeight);

	/**
	 * Draws the foreground. This would include the title, and anything else not
	 * part of the main contents, excluding GuiButtons and GuiLabels
	 * 
	 * @param mouseX
	 * @param mouseY
	 * @param partialTicks
	 */
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
				mouseClickedVirtual(mouseX, mouseY, mouseButton);
			}
		} else if (mouseY > headerHeight && mouseY < height - footerHeight) {
			mouseClickedVirtual(mouseX, mouseY, mouseButton);
		}
		super.mouseClicked(mouseX, mouseY, mouseButton);
	}

	/**
	 * Called when the mouse is clicked inside the area where the main contents
	 * is shown. mouseX and mouseY are physical, not relative
	 * 
	 * @param mouseX
	 * @param mouseY
	 * @param mouseButton
	 */
	protected void mouseClickedVirtual(int mouseX, int mouseY, int mouseButton) {
	}

	@Override
	public void mouseReleased(int mouseX, int mouseY, int state) {
		if (state == 0) { // This test does not work
			firstXScrollBarPos = firstYScrollBarPos = firstMouseX = firstMouseY = -1;
		}
		if (mouseY > headerHeight && mouseY < height - footerHeight)
			mouseReleasedVirtual(mouseX, mouseY, state);
		super.mouseReleased(mouseX, mouseY, state);
	}

	/**
	 * Called when the mouse is released inside the area where the main contents
	 * is shown. mouseX and mouseY are physical, not relative
	 * 
	 * @param mouseX
	 * @param mouseY
	 * @param releasedButton
	 */
	protected void mouseReleasedVirtual(int mouseX, int mouseY, int releasedButton) {
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
			mouseClickMoveVirtual(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
		}
	}

	/**
	 * Called when the mouse is clicked and dragged inside the area where the
	 * main contents is shown. mouseX and mouseY are physical, not relative
	 * 
	 * @param mouseX
	 * @param mouseY
	 * @param clickedMouseButton
	 * @param timeSinceLastClick
	 */
	protected void mouseClickMoveVirtual(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
	}

}

package net.earthcomputer.easyeditors.api.util;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import net.earthcomputer.easyeditors.api.EntityRendererRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiConfirmOpenLink;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.stats.Achievement;
import net.minecraft.stats.StatBase;
import net.minecraft.stats.StatList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;

/**
 * General utilities used by Easy Editors. These will work even if Easy Editors
 * is not loaded.
 * 
 * <b>This class is a member of the Easy Editors API</b>
 * 
 * @author Earthcomputer
 *
 */
public class GeneralUtils {

	private static final Splitter NEWLINE_SPLITTER = Splitter.on('\n');
	private static final Set<String> PROTOCOLS = Sets.newHashSet("http", "https");
	private static final Logger MC_LOGGER = LogManager.getLogger();

	/**
	 * Plays the button sound, as if a button had been pressed in a GUI
	 */
	public static void playButtonSound() {
		Minecraft.getMinecraft().getSoundHandler()
				.playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1));
	}

	/**
	 * Converts an unsigned hexadecimal number to a Java signed integer
	 * 
	 * @param hex
	 * @return
	 */
	public static int hexToInt(String hex) {
		long l = Long.parseLong(hex, 16);
		if (l > Integer.MAX_VALUE) {
			l -= Integer.MAX_VALUE + -((long) Integer.MIN_VALUE) + 1;
		}
		return (int) l;
	}

	/**
	 * Converts the hue, saturation and value color model to the red, green and
	 * blue color model
	 * 
	 * @param hue
	 *            - A positive integer, which, modulo 360, will represent the
	 *            hue of the color
	 * @param saturation
	 *            - An integer between 0 and 100
	 * @param value
	 *            - An integer between 0 and 100
	 * @return
	 */
	public static int hsvToRgb(int hue, int saturation, int value) {
		// Source: en.wikipedia.org/wiki/HSL_and_HSV#Converting_to_RGB#From_HSV
		hue %= 360;
		float s = (float) saturation / 100;
		float v = (float) value / 100;
		float c = v * s;
		float h = (float) hue / 60;
		float x = c * (1 - Math.abs(h % 2 - 1));
		float r, g, b;
		switch (hue / 60) {
		case 0:
			r = c;
			g = x;
			b = 0;
			break;
		case 1:
			r = x;
			g = c;
			b = 0;
			break;
		case 2:
			r = 0;
			g = c;
			b = x;
			break;
		case 3:
			r = 0;
			g = x;
			b = c;
			break;
		case 4:
			r = x;
			g = 0;
			b = c;
			break;
		case 5:
			r = c;
			g = 0;
			b = x;
			break;
		default:
			return 0;
		}
		float m = v - c;
		return ((int) ((r + m) * 255) << 16) | ((int) ((g + m) * 255) << 8) | ((int) ((b + m) * 255));
	}

	/**
	 * Converts the red, green and blue color model to the hue, saturation and
	 * value color model
	 * 
	 * @param rgb
	 * @return A 3-length array containing hue, saturation and value, in that
	 *         order. Hue is an integer between 0 and 359, or -1 if it is
	 *         undefined. Saturation and value are both integers between 0 and
	 *         100
	 */
	public static int[] rgbToHsv(int rgb) {
		// Source: en.wikipedia.org/wiki/HSV_and_HSL#Formal_derivation
		float r = (float) ((rgb & 0xff0000) >> 16) / 255;
		float g = (float) ((rgb & 0x00ff00) >> 8) / 255;
		float b = (float) (rgb & 0x0000ff) / 255;
		float M = r > g ? (r > b ? r : b) : (g > b ? g : b);
		float m = r < g ? (r < b ? r : b) : (g < b ? g : b);
		float c = M - m;
		float h;
		if (M == r) {
			h = ((g - b) / c);
			while (h < 0)
				h = 6 - h;
			h %= 6;
		} else if (M == g) {
			h = ((b - r) / c) + 2;
		} else {
			h = ((r - g) / c) + 4;
		}
		h *= 60;
		float s = c / M;
		return new int[] { c == 0 ? -1 : (int) h, (int) (s * 100), (int) (M * 100) };
	}

	/**
	 * Converts a double to a String without any trailing <code>.0</code>
	 * 
	 * @param d
	 * @return
	 */
	public static String doubleToString(double d) {
		String strValue = String.valueOf(d);
		if (strValue.endsWith(".0")) {
			strValue = strValue.substring(0, strValue.length() - 2);
		}
		return strValue;
	}

	/**
	 * Draws a rectangle with possibly different colors in different corners
	 * 
	 * @param left
	 * @param top
	 * @param right
	 * @param bottom
	 * @param coltl
	 *            - the color of the top left corner
	 * @param coltr
	 *            - the color of the top right corner
	 * @param colbl
	 *            - the color of the bottom left corner
	 * @param colbr
	 *            - the color of the bottom right corner
	 */
	public static void drawGradientRect(int left, int top, int right, int bottom, int coltl, int coltr, int colbl,
			int colbr) {
		drawGradientRect(left, top, right, bottom, coltl, coltr, colbl, colbr, 0);
	}

	/**
	 * Draws a rectangle with possibly different colors in different corners
	 * 
	 * @param left
	 * @param top
	 * @param right
	 * @param bottom
	 * @param coltl
	 *            - the color of the top left corner
	 * @param coltr
	 *            - the color of the top right corner
	 * @param colbl
	 *            - the color of the bottom left corner
	 * @param colbr
	 *            - the color of the bottom right corner
	 * @param zLevel
	 */
	public static void drawGradientRect(int left, int top, int right, int bottom, int coltl, int coltr, int colbl,
			int colbr, int zLevel) {
		GlStateManager.disableTexture2D();
		GlStateManager.enableBlend();
		GlStateManager.disableAlpha();
		GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
		GlStateManager.shadeModel(GL11.GL_SMOOTH);

		Tessellator tessellator = Tessellator.getInstance();
		VertexBuffer buffer = tessellator.getBuffer();
		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
		buffer.pos(right, top, zLevel).color((coltr & 0x00ff0000) >> 16, (coltr & 0x0000ff00) >> 8,
				(coltr & 0x000000ff), (coltr & 0xff000000) >>> 24).endVertex();
		buffer.pos(left, top, zLevel).color((coltl & 0x00ff0000) >> 16, (coltl & 0x0000ff00) >> 8, (coltl & 0x000000ff),
				(coltl & 0xff000000) >>> 24).endVertex();
		buffer.pos(left, bottom, zLevel).color((colbl & 0x00ff0000) >> 16, (colbl & 0x0000ff00) >> 8,
				(colbl & 0x000000ff), (colbl & 0xff000000) >>> 24).endVertex();
		buffer.pos(right, bottom, zLevel).color((colbr & 0x00ff0000) >> 16, (colbr & 0x0000ff00) >> 8,
				(colbr & 0x000000ff), (colbr & 0xff000000) >>> 24).endVertex();
		tessellator.draw();

		GlStateManager.shadeModel(GL11.GL_FLAT);
		GlStateManager.disableBlend();
		GlStateManager.enableAlpha();
		GlStateManager.enableTexture2D();
	}

	/**
	 * Draws a tooltip, automatically splitting the string so it fits in the
	 * specified width
	 * 
	 * @param x
	 * @param y
	 * @param text
	 * @param maxWidth
	 */
	public static void drawTooltip(int x, int y, String text, int maxWidth) {
		drawTooltip(x, y, Minecraft.getMinecraft().fontRendererObj.listFormattedStringToWidth(text, maxWidth));
	}

	/**
	 * A static alternative to
	 * {@link GuiScreen#drawHoveringText(List, int, int)}
	 * 
	 * @param x
	 * @param y
	 * @param lines
	 */
	public static void drawTooltip(int x, int y, String... lines) {
		drawTooltip(x, y, Arrays.asList(lines));
	}

	/**
	 * A static alternative to
	 * {@link GuiScreen#drawHoveringText(List, int, int)}
	 * 
	 * @param x
	 * @param y
	 * @param lines
	 */
	public static void drawTooltip(int x, int y, List<String> lines) {
		drawTooltip(x, y, lines, Minecraft.getMinecraft().fontRendererObj);
	}

	/**
	 * Draws hovering text in a specified font
	 * 
	 * @param x
	 * @param y
	 * @param lines
	 * @param font
	 */
	public static void drawTooltip(int x, int y, List<String> lines, FontRenderer font) {
		Minecraft mc = Minecraft.getMinecraft();
		ScaledResolution res = new ScaledResolution(mc);
		int width = res.getScaledWidth();
		int height = res.getScaledHeight();

		if (!lines.isEmpty()) {
			GlStateManager.disableRescaleNormal();
			RenderHelper.disableStandardItemLighting();
			GlStateManager.disableLighting();
			GlStateManager.disableDepth();
			int maxLineWidth = 0;
			for (String line : lines) {
				int lineWidth = font.getStringWidth(line);

				if (lineWidth > maxLineWidth) {
					maxLineWidth = lineWidth;
				}
			}

			int textX = x + 12;
			int textY = y - 12;
			int h = 8;

			if (lines.size() > 1) {
				h += 2 + (lines.size() - 1) * 10;
			}

			if (textX + maxLineWidth > width) {
				textX -= 28 + maxLineWidth;
			}

			if (textY + h + 6 > height) {
				textY = height - h - 6;
			}

			mc.getRenderItem().zLevel = 300;
			int col1 = 0xf0100010;
			drawGradientRect(textX - 3, textY - 4, textX + maxLineWidth + 3, textY - 3, col1, col1, col1, col1, 300);
			drawGradientRect(textX - 3, textY + h + 3, textX + maxLineWidth + 3, textY + h + 4, col1, col1, col1, col1,
					300);
			drawGradientRect(textX - 3, textY - 3, textX + maxLineWidth + 3, textY + h + 3, col1, col1, col1, col1,
					300);
			drawGradientRect(textX - 4, textY - 3, textX - 3, textY + h + 3, col1, col1, col1, col1, 300);
			drawGradientRect(textX + maxLineWidth + 3, textY - 3, textX + maxLineWidth + 4, textY + h + 3, col1, col1,
					col1, col1, 300);
			int col2 = 0x505000ff;
			int col3 = (col2 & 0xfefefe) >> 1 | col2 & 0xff000000;
			drawGradientRect(textX - 3, textY - 3 + 1, textX - 3 + 1, textY + h + 3 - 1, col2, col2, col3, col3, 300);
			drawGradientRect(textX + maxLineWidth + 2, textY - 3 + 1, textX + maxLineWidth + 3, textY + h + 3 - 1, col2,
					col2, col3, col3, 300);
			drawGradientRect(textX - 3, textY - 3, textX + maxLineWidth + 3, textY - 3 + 1, col2, col2, col2, col2,
					300);
			drawGradientRect(textX - 3, textY + h + 2, textX + maxLineWidth + 3, textY + h + 3, col3, col3, col3, col3,
					300);

			for (int i = 0; i < lines.size(); i++) {
				String line = lines.get(i);
				font.drawStringWithShadow(line, textX, textY, -1);

				if (i == 0) {
					textY += 2;
				}

				textY += 10;
			}

			mc.getRenderItem().zLevel = 0;
			GlStateManager.enableLighting();
			GlStateManager.enableDepth();
			RenderHelper.enableStandardItemLighting();
			GlStateManager.enableRescaleNormal();
		}
	}

	/**
	 * Draws a tooltip for an ItemStack
	 * 
	 * @param stack
	 * @param x
	 * @param y
	 */
	public static void drawItemStackTooltip(ItemStack stack, int x, int y) {
		List<String> list = stack.getTooltip(Minecraft.getMinecraft().player,
				Minecraft.getMinecraft().gameSettings.advancedItemTooltips);

		for (int i = 0; i < list.size(); ++i) {
			if (i == 0) {
				list.set(i, stack.getRarity().rarityColor + (String) list.get(i));
			} else {
				list.set(i, TextFormatting.GRAY + (String) list.get(i));
			}
		}

		FontRenderer font = stack.getItem().getFontRenderer(stack);
		drawTooltip(x, y, list, (font == null ? Minecraft.getMinecraft().fontRendererObj : font));
	}

	/**
	 * Equivalent of {@link Throwable#printStackTrace()}, but uses a logger
	 * 
	 * @param logger
	 * @param throwable
	 */
	public static void logStackTrace(Logger logger, Throwable throwable) {
		StringWriter sw = new StringWriter();
		throwable.printStackTrace(new PrintWriter(sw));
		Scanner scanner = new Scanner(new StringReader(sw.toString()));
		while (scanner.hasNextLine()) {
			logger.error(scanner.nextLine());
		}
		scanner.close();
	}

	/**
	 * Renders an entity at a given position (best-effort)
	 * 
	 * @param entity
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @param mouseX
	 * @param mouseY
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Entity> void renderEntityAt(Entity entity, int x, int y, int width, int height, int mouseX,
			int mouseY) {
		ICustomEntityRenderer<T> customRenderer = (ICustomEntityRenderer<T>) EntityRendererRegistry
				.findCustomRenderer(entity);
		if (customRenderer != null) {
			customRenderer.renderEntity((T) entity, x, y, width, height, mouseX, mouseY);
		} else if (entity instanceof EntityLivingBase) {
			EntityLivingBase eLiving = (EntityLivingBase) entity;
			boolean isWidthRestricting = (float) eLiving.width / width > (float) eLiving.height / height;
			float scale;
			if (isWidthRestricting) {
				scale = eLiving.width > 1 ? width / eLiving.width : width;
			} else {
				scale = eLiving.height > 1 ? height / eLiving.height : height;
			}
			mouseX -= x;
			mouseY -= y - 50;
			GlStateManager.disableBlend();
			GlStateManager.depthMask(true);
			GlStateManager.enableDepth();
			GlStateManager.enableAlpha();
			GlStateManager.pushMatrix();
			GlStateManager.enableColorMaterial();
			GlStateManager.color(1, 1, 1, 1);
			if (isWidthRestricting)
				height = (int) (width * eLiving.height / eLiving.width);
			else
				width = (int) (height * eLiving.width / eLiving.height);
			GlStateManager.translate(x, y + height / 2, 150);
			GlStateManager.scale(-scale, scale, scale);
			GlStateManager.rotate(180, 0, 0, 1);
			float prevRotationYawOffset = eLiving.renderYawOffset;
			float prevRotationYaw = eLiving.rotationYaw;
			float prevRotationPitch = eLiving.rotationPitch;
			float prevPrevRotationYawHead = eLiving.prevRotationYawHead;
			float prevRotationYawHead = eLiving.rotationYawHead;
			GlStateManager.rotate(135, 0, 1, 0);
			RenderHelper.enableStandardItemLighting();
			GlStateManager.rotate(-135, 0, 1, 0);
			GlStateManager.rotate(((float) Math.atan((double) ((float) mouseY / 40))) * 20, 1, 0, 0);
			eLiving.renderYawOffset = -(float) Math.atan((double) ((float) mouseX / 40)) * 20;
			eLiving.rotationYaw = -(float) Math.atan((double) ((float) mouseX / 40)) * 40;
			eLiving.rotationPitch = ((float) Math.atan((double) ((float) mouseY / 40))) * 20;
			eLiving.rotationYawHead = eLiving.rotationYaw;
			eLiving.prevRotationYawHead = eLiving.rotationYaw;
			GlStateManager.translate(0, 0, 0);
			RenderManager rendermanager = Minecraft.getMinecraft().getRenderManager();
			rendermanager.setPlayerViewY(180);
			rendermanager.setRenderShadow(false);
			boolean errored = false;
			try {
				rendermanager.doRenderEntity(eLiving, 0, 0, 0, 0, 1, false);
			} catch (Exception e) {
				errored = true;
			}
			rendermanager.setRenderShadow(true);
			eLiving.renderYawOffset = prevRotationYawOffset;
			eLiving.rotationYaw = prevRotationYaw;
			eLiving.rotationPitch = prevRotationPitch;
			eLiving.prevRotationYawHead = prevPrevRotationYawHead;
			eLiving.rotationYawHead = prevRotationYawHead;
			GlStateManager.popMatrix();
			RenderHelper.disableStandardItemLighting();
			GlStateManager.disableRescaleNormal();
			GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
			GlStateManager.disableTexture2D();
			GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
			if (errored) {
				FontRenderer fontRenderer = Minecraft.getMinecraft().fontRendererObj;
				String str = "Error drawing entity";
				fontRenderer.drawStringWithShadow(str, x - fontRenderer.getStringWidth(str) / 2, y - 4, 0xff0000);
			}
		}
	}

	/**
	 * A static alternative to
	 * {@link GuiScreen#handleComponentHover(IChatComponent, int, int)}, with
	 * the additional functionality that hover events from elsewhere can be
	 * handled
	 * 
	 * @param event
	 * @param mouseX
	 * @param mouseY
	 */
	public static void handleHoverEvent(HoverEvent event, int mouseX, int mouseY) {
		if (event != null) {
			if (event.getAction() == HoverEvent.Action.SHOW_ITEM) {
				ItemStack stackToShow = ItemStack.EMPTY;

				try {
					NBTBase nbtbase = JsonToNBT.getTagFromJson(event.getValue().getUnformattedText());

					if (nbtbase instanceof NBTTagCompound) {
						stackToShow = new ItemStack((NBTTagCompound) nbtbase);
					}
				} catch (NBTException e) {
				}

				if (!stackToShow.isEmpty()) {
					drawItemStackTooltip(stackToShow, mouseX, mouseY);
				} else {
					drawTooltip(mouseX, mouseY, TextFormatting.RED + "Invalid Item!");
				}
			} else if (event.getAction() == HoverEvent.Action.SHOW_ENTITY) {
				if (Minecraft.getMinecraft().gameSettings.advancedItemTooltips) {
					try {
						NBTBase entityNBT = JsonToNBT.getTagFromJson(event.getValue().getUnformattedText());

						if (entityNBT instanceof NBTTagCompound) {
							List<String> tooltipLines = Lists.<String>newArrayList();
							NBTTagCompound entityCompound = (NBTTagCompound) entityNBT;
							tooltipLines.add(entityCompound.getString("name"));

							if (entityCompound.hasKey("type", 8)) {
								String type = entityCompound.getString("type");
								tooltipLines.add("Type: " + type + " ("
										+ EntityList.getID(EntityList.getClass(new ResourceLocation(type))) + ")");
							}

							tooltipLines.add(entityCompound.getString("id"));
							drawTooltip(mouseX, mouseY, tooltipLines);
						} else {
							drawTooltip(mouseX, mouseY, TextFormatting.RED + "Invalid Entity!");
						}
					} catch (NBTException e) {
						drawTooltip(mouseX, mouseY, TextFormatting.RED + "Invalid Entity!");
					}
				}
			} else if (event.getAction() == HoverEvent.Action.SHOW_TEXT) {
				drawTooltip(mouseX, mouseY, NEWLINE_SPLITTER.splitToList(event.getValue().getFormattedText()));
			} else if (event.getAction() == HoverEvent.Action.SHOW_ACHIEVEMENT) {
				StatBase stat = StatList.getOneShotStat(event.getValue().getUnformattedText());

				if (stat != null) {
					ITextComponent statName = stat.getStatName();
					ITextComponent statType = new TextComponentTranslation(
							"stats.tooltip.type." + (stat.isAchievement() ? "achievement" : "statistic"));
					statType.getStyle().setItalic(true);
					String achievementDescription = stat instanceof Achievement ? ((Achievement) stat).getDescription()
							: null;
					List<String> lines = Lists.newArrayList(statName.getFormattedText(), statType.getFormattedText());

					if (achievementDescription != null) {
						lines.addAll(Minecraft.getMinecraft().fontRendererObj
								.listFormattedStringToWidth(achievementDescription, 150));
					}

					drawTooltip(mouseX, mouseY, lines);
				} else {
					drawTooltip(mouseX, mouseY, TextFormatting.RED + "Invalid statistic/achievement!");
				}
			}

			GlStateManager.disableLighting();
		}
	}

	/**
	 * A static alternative to
	 * {@link GuiScreen#handleComponentClick(IChatComponent)}, with the
	 * additional functionality that click events from elsewhere can be handled
	 * 
	 * @param event
	 * @return
	 */
	public static boolean handleClickEvent(ClickEvent event) {
		if (event != null) {
			if (event.getAction() == ClickEvent.Action.OPEN_URL) {
				if (!Minecraft.getMinecraft().gameSettings.chatLinks) {
					return false;
				}

				try {
					final URI webUri = new URI(event.getValue());
					String protocol = webUri.getScheme();

					if (protocol == null) {
						throw new URISyntaxException(event.getValue(), "Missing protocol");
					}

					if (!PROTOCOLS.contains(protocol.toLowerCase())) {
						throw new URISyntaxException(event.getValue(),
								"Unsupported protocol: " + protocol.toLowerCase());
					}

					if (Minecraft.getMinecraft().gameSettings.chatLinksPrompt) {
						final GuiScreen prevScreen = Minecraft.getMinecraft().currentScreen;
						GuiYesNoCallback callback = new GuiYesNoCallback() {

							@Override
							public void confirmClicked(boolean result, int id) {
								if (id == 31102009) {
									if (result) {
										openWebLink(webUri);
									}

									Minecraft.getMinecraft().displayGuiScreen(prevScreen);
								}
							}

						};
						Minecraft.getMinecraft()
								.displayGuiScreen(new GuiConfirmOpenLink(callback, event.getValue(), 31102009, false));
					} else {
						openWebLink(webUri);
					}
				} catch (URISyntaxException e) {
					MC_LOGGER.error("Can't open url for " + event, e);
				}
			} else if (event.getAction() == ClickEvent.Action.OPEN_FILE) {
				URI fileUri = new File(event.getValue()).toURI();
				openWebLink(fileUri);
			} else if (event.getAction() == ClickEvent.Action.SUGGEST_COMMAND) {
				// Can't do insertion
			} else if (event.getAction() == ClickEvent.Action.RUN_COMMAND) {
				if (Minecraft.getMinecraft().player != null)
					Minecraft.getMinecraft().player.sendChatMessage(event.getValue());
			} else {
				MC_LOGGER.error("Don't know how to handle " + event);
			}

			return true;
		}

		return false;
	}

	/**
	 * A static alternative to {@link GuiScreen#openWebLink(URI)}
	 * 
	 * @param uri
	 */
	public static void openWebLink(URI uri) {
		try {
			Class<?> desktopClass = Class.forName("java.awt.Desktop");
			Object desktopInstance = desktopClass.getMethod("getDesktop").invoke(null);
			desktopClass.getMethod("browse", URI.class).invoke(desktopInstance, uri);
		} catch (Throwable throwable) {
			MC_LOGGER.error("Couldn't open link", throwable);
		}
	}

}

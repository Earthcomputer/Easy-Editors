package net.earthcomputer.easyeditors.gui.command;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.List;

import javax.imageio.ImageIO;

import org.lwjgl.input.Keyboard;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import net.earthcomputer.easyeditors.EasyEditors;
import net.earthcomputer.easyeditors.api.util.GeneralUtils;
import net.earthcomputer.easyeditors.gui.GuiTwoWayScroll;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiUtilRenderComponents;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.EnumTypeAdapterFactory;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.ResourceLocation;

public class CommandHelpManager {

	private CommandHelpManager() {
	}

	private static final CommandHelpManager INSTANCE = new CommandHelpManager();

	public static CommandHelpManager getInstance() {
		return INSTANCE;
	}

	public void displayHelpScreen(String name, String id) {
		String modid = EasyEditors.ID;

		if (id.contains(":")) {
			int colonIndex = id.indexOf(':');
			modid = id.substring(0, colonIndex);
			id = id.substring(colonIndex + 1);
		}

		displayHelpScreen(name, modid, id);
	}

	public void displayHelpScreen(String name, String modid, String id) {
		try {
			Minecraft.getMinecraft()
					.displayGuiScreen(new GuiHelpScreen(name,
							deserializeComponents(Minecraft.getMinecraft().getResourceManager()
									.getResource(new ResourceLocation(modid, "command_help/" + id + ".json"))
									.getInputStream())));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	private List<ICommandHelpComponent> deserializeComponents(InputStream in) {
		return CommandHelpDeserializer.GSON.fromJson(new InputStreamReader(in), List.class);
	}

	private static class CommandHelpDeserializer implements JsonDeserializer<List<ICommandHelpComponent>> {

		public static final Gson GSON = new GsonBuilder().registerTypeAdapter(List.class, new CommandHelpDeserializer())
				.registerTypeHierarchyAdapter(IChatComponent.class, new IChatComponent.Serializer())
				.registerTypeHierarchyAdapter(ChatStyle.class, new ChatStyle.Serializer())
				.registerTypeAdapterFactory(new EnumTypeAdapterFactory()).create();

		@Override
		public List<ICommandHelpComponent> deserialize(JsonElement json, Type typeOfT,
				JsonDeserializationContext context) throws JsonParseException {
			List<ICommandHelpComponent> r = Lists.newArrayList();

			if (json.isJsonArray()) {
				for (JsonElement element : json.getAsJsonArray())
					r.add(deserializeSingleComponent(element, context));
			} else {
				r.add(deserializeSingleComponent(json, context));
			}

			return r;
		}

		private ICommandHelpComponent deserializeSingleComponent(JsonElement json, JsonDeserializationContext context) {
			if (json.isJsonPrimitive()) {
				String value = json.getAsString();
				return new ComponentChat(new ChatComponentTranslation(value));
			} else if (json.isJsonArray()) {
				return new ComponentChat((IChatComponent) context.deserialize(json, IChatComponent.class));
			} else if (json.isJsonNull()) {
				return new ComponentNewline();
			} else if (json.isJsonObject()) {
				JsonObject object = json.getAsJsonObject();
				JsonElement type = object.remove("type");
				String typeString = "text";
				if (type != null && type.isJsonPrimitive())
					typeString = type.getAsString();
				if ("text".equals(typeString)) {
					return new ComponentChat((IChatComponent) context.deserialize(object, IChatComponent.class));
				} else if ("image".equals(typeString)) {
					int u, v, w, h;
					JsonElement subimage = object.get("subimage");
					if (subimage == null) {
						u = 0;
						v = 0;
						w = 0;
						h = 0;
					} else {
						JsonArray subimageArray = subimage.getAsJsonArray();
						if (subimageArray.size() != 4) {
							throw new JsonParseException("The subimage array must be of length 4");
						}
						u = subimageArray.get(0).getAsInt();
						if (u < 0)
							u = 0;
						v = subimageArray.get(1).getAsInt();
						if (v < 0)
							v = 0;
						w = subimageArray.get(2).getAsInt();
						h = subimageArray.get(3).getAsInt();
					}

					HoverEvent hoverEvent = null;
					if (object.has("hoverEvent")) {
						JsonObject hoverEventObj = object.getAsJsonObject("hoverEvent");
						if (!hoverEventObj.has("action") || !hoverEventObj.has("value"))
							throw new JsonParseException("hoverEvents must always have an action and a value");
						HoverEvent.Action action = HoverEvent.Action
								.getValueByCanonicalName(hoverEventObj.get("action").getAsString());
						if (action == null)
							throw new JsonParseException(
									"No such hover event action " + hoverEventObj.get("action").getAsString());
						IChatComponent value = (IChatComponent) context.deserialize(hoverEventObj.get("value"),
								IChatComponent.class);
						hoverEvent = new HoverEvent(action, value);
					}

					ClickEvent clickEvent = null;
					if (object.has("clickEvent")) {
						JsonObject clickEventObj = object.getAsJsonObject("clickEvent");
						if (!clickEventObj.has("action") || !clickEventObj.has("value"))
							throw new JsonParseException("clickEvents must always have an action and a value");
						ClickEvent.Action action = ClickEvent.Action
								.getValueByCanonicalName(clickEventObj.get("action").getAsString());
						if (action == null)
							throw new JsonParseException(
									"No such click event action " + clickEventObj.get("action").getAsString());
						String value = clickEventObj.get("value").getAsString();
						clickEvent = new ClickEvent(action, value);
					}

					JsonElement imageLocation = object.get("location");
					if (imageLocation != null && imageLocation.isJsonPrimitive())
						return new ComponentImage(imageLocation.getAsString(), u, v, w, h, hoverEvent, clickEvent);
					else
						throw new JsonParseException(
								"Don't know how to turn anything but a json string into an image location");
				} else {
					throw new JsonParseException("Invalid type of command help component: " + typeString);
				}

			} else {
				throw new JsonParseException("What type of json type is this?! I've never seen that before");
			}
		}

	}

	private static interface ICommandHelpComponent {
		void draw(int mouseX, int mouseY, int y, int virtualWidth);

		int getHeight(int virtualWidth);

		boolean mouseClicked(int mouseX, int mouseY, int y, int virtualWidth);
	}

	private static class ComponentChat implements ICommandHelpComponent {
		private FontRenderer fontRenderer;
		private IChatComponent text;

		public ComponentChat(IChatComponent text) {
			this.fontRenderer = Minecraft.getMinecraft().fontRendererObj;
			this.text = text;
		}

		@Override
		public void draw(int mouseX, int mouseY, int y, int virtualWidth) {
			List<IChatComponent> lines = GuiUtilRenderComponents.func_178908_a(text, virtualWidth - 20, fontRenderer,
					false, false);

			int runningY = y;

			for (IChatComponent line : lines) {
				String strLine = line.getFormattedText();
				int halfLineWidth = fontRenderer.getStringWidth(strLine) / 2;
				int lineLeft = virtualWidth / 2 - halfLineWidth;
				fontRenderer.drawString(strLine, lineLeft, runningY, 0xffffff);

				runningY += fontRenderer.FONT_HEIGHT;
			}

			IChatComponent hoveredComponent = getHoveredComponent(virtualWidth, y, mouseX, mouseY);
			if (hoveredComponent != null)
				GeneralUtils.handleHoverEvent(hoveredComponent.getChatStyle().getChatHoverEvent(), mouseX, mouseY);

		}

		@Override
		public int getHeight(int virtualWidth) {
			return GuiUtilRenderComponents.func_178908_a(text, virtualWidth - 20, fontRenderer, false, false).size()
					* fontRenderer.FONT_HEIGHT;
		}

		@Override
		public boolean mouseClicked(int mouseX, int mouseY, int y, int virtualWidth) {
			IChatComponent hoveredComponent = getHoveredComponent(virtualWidth, y, mouseX, mouseY);
			if (hoveredComponent != null) {
				ClickEvent clickEvent = hoveredComponent.getChatStyle().getChatClickEvent();
				if (clickEvent.getAction() == ClickEvent.Action.CHANGE_PAGE) {
					String value = clickEvent.getValue();
					String name = null;
					if (value.contains("=")) {
						int equalsIndex = value.indexOf('=');
						name = value.substring(0, equalsIndex);
						value = value.substring(equalsIndex + 1);
					}
					CommandHelpManager.getInstance().displayHelpScreen(name, value);
					return true;
				} else {
					return GeneralUtils.handleClickEvent(clickEvent);
				}
			} else
				return false;
		}

		private IChatComponent getHoveredComponent(int virtualWidth, int y, int mouseX, int mouseY) {
			List<IChatComponent> lines = GuiUtilRenderComponents.func_178908_a(text, virtualWidth - 20, fontRenderer,
					false, false);

			for (IChatComponent line : lines) {
				String strLine = line.getFormattedText();
				int halfLineWidth = fontRenderer.getStringWidth(strLine) / 2;
				int lineLeft = virtualWidth / 2 - halfLineWidth;
				int lineRight = virtualWidth / 2 + halfLineWidth;

				if (mouseY >= y && mouseY < y + fontRenderer.FONT_HEIGHT && mouseX >= lineLeft && mouseX < lineRight) {
					IChatComponent hoveredComponent = null;
					for (IChatComponent sibling : line) {
						hoveredComponent = sibling;
						lineLeft += fontRenderer.getStringWidth(
								GuiUtilRenderComponents.func_178909_a(sibling.getUnformattedTextForChat(), false));
						if (mouseX < lineLeft)
							break;
					}
					return hoveredComponent;
				}

				y += fontRenderer.FONT_HEIGHT;
			}
			return null;
		}
	}

	private static class ComponentImage implements ICommandHelpComponent {
		private final FontRenderer fontRenderer;

		private final TextureManager textureManager;
		private final ResourceLocation location;
		private final BufferedImage theImage;

		private final int u;
		private final int v;
		private final int width;
		private final int height;
		private final HoverEvent hoverEvent;
		private final ClickEvent clickEvent;

		public ComponentImage(String location, int u, int v, int width, int height, HoverEvent hoverEvent,
				ClickEvent clickEvent) {
			this.fontRenderer = Minecraft.getMinecraft().fontRendererObj;
			this.textureManager = Minecraft.getMinecraft().getTextureManager();
			this.location = new ResourceLocation(location);
			BufferedImage theImage;
			try {
				theImage = ImageIO.read(
						Minecraft.getMinecraft().getResourceManager().getResource(this.location).getInputStream());
				if (width <= 0)
					width = theImage.getWidth();
				if (height <= 0)
					height = theImage.getHeight();
			} catch (IOException e) {
				theImage = null;
			}
			this.theImage = theImage;

			if (u + width >= theImage.getWidth()) {
				u = 0;
				width = theImage.getWidth();
			}
			if (v + height >= theImage.getHeight()) {
				v = 0;
				height = theImage.getHeight();
			}

			this.u = u;
			this.v = v;
			this.width = width;
			this.height = height;
			this.hoverEvent = hoverEvent;
			this.clickEvent = clickEvent;
		}

		@Override
		public void draw(int mouseX, int mouseY, int y, int virtualWidth) {
			int imageLeft;
			int imageRight;
			int imageBottom;
			if (theImage == null) {
				String str = EnumChatFormatting.RED + "No image found at " + location;
				imageLeft = virtualWidth / 2 - fontRenderer.getStringWidth(str) / 2;
				imageRight = imageLeft + fontRenderer.getStringWidth(str);
				imageBottom = y + fontRenderer.FONT_HEIGHT;
				fontRenderer.drawString(str, imageLeft, y, 0xffffff);
			} else {
				textureManager.bindTexture(location);
				imageLeft = virtualWidth / 2 - width / 2;
				imageRight = imageLeft + width;
				imageBottom = y + height;
				Gui.drawModalRectWithCustomSizedTexture(imageLeft, y, u, v, width, height, theImage.getWidth(),
						theImage.getHeight());
			}

			if (hoverEvent != null) {
				if (mouseX >= imageLeft && mouseX < imageRight && mouseY >= y && mouseY < imageBottom) {
					GeneralUtils.handleHoverEvent(hoverEvent, mouseX, mouseY);
				}
			}
		}

		@Override
		public int getHeight(int virtualWidth) {
			return theImage == null ? fontRenderer.FONT_HEIGHT : height;
		}

		@Override
		public boolean mouseClicked(int mouseX, int mouseY, int y, int virtualWidth) {
			if (clickEvent != null) {
				String str = EnumChatFormatting.RED + "No image found at " + location;
				int imageLeft, imageRight, imageBottom;
				if (theImage == null) {
					imageLeft = virtualWidth / 2 - fontRenderer.getStringWidth(str) / 2;
					imageRight = imageLeft + fontRenderer.getStringWidth(str);
					imageBottom = y + fontRenderer.FONT_HEIGHT;
				} else {
					imageLeft = virtualWidth / 2 - width / 2;
					imageRight = imageLeft + width;
					imageBottom = y + height;
				}
				if (mouseX >= imageLeft && mouseX < imageRight && mouseY >= y && mouseY < imageBottom) {
					if (clickEvent.getAction() == ClickEvent.Action.CHANGE_PAGE) {
						String value = clickEvent.getValue();
						String name = null;
						if (value.contains("=")) {
							int equalsIndex = value.indexOf('=');
							name = value.substring(0, equalsIndex);
							value = value.substring(equalsIndex + 1);
						}
						CommandHelpManager.getInstance().displayHelpScreen(name, value);
						return true;
					} else {
						return GeneralUtils.handleClickEvent(clickEvent);
					}
				}
			}
			return false;
		}

	}

	private static class ComponentNewline implements ICommandHelpComponent {

		@Override
		public void draw(int mouseX, int mouseY, int y, int virtualWidth) {
		}

		@Override
		public int getHeight(int virtualWidth) {
			return 9;
		}

		@Override
		public boolean mouseClicked(int mouseX, int mouseY, int y, int virtualWidth) {
			return false;
		}

	}

	private static class GuiHelpScreen extends GuiTwoWayScroll {

		private String name;
		private List<ICommandHelpComponent> components;
		private GuiScreen prevScreen;

		public GuiHelpScreen(String name, List<ICommandHelpComponent> components) {
			super(name == null ? 30 : 35, 30, 1, 1);
			if (name != null)
				this.name = I18n.format(name);
			this.components = components;
			this.prevScreen = Minecraft.getMinecraft().currentScreen;

			setXScrollBarPolicy(SHOWN_NEVER);
		}

		@Override
		public void initGui() {
			int totalHeight = components.size() * 2 - 2;
			for (ICommandHelpComponent component : components) {
				totalHeight += component.getHeight(width - 6);
			}

			setVirtualHeight(totalHeight);

			buttonList.add(new GuiButton(0, width / 2 - 100, height - 25, 200, 20, I18n.format("gui.done")));

			super.initGui();
		}

		@Override
		public void actionPerformed(GuiButton button) {
			if (button.id == 0)
				mc.displayGuiScreen(prevScreen);
		}

		@Override
		protected void drawVirtualScreen(int mouseX, int mouseY, float partialTicks, int scrollX, int scrollY,
				int headerHeight) {
			int virtualWidth = width - 6;
			int y = headerHeight + 2 - scrollY;

			for (ICommandHelpComponent component : components) {
				component.draw(mouseX, mouseY, y, virtualWidth);
				y += component.getHeight(virtualWidth) + 2;
			}
		}

		@Override
		protected void drawForeground(int mouseX, int mouseY, float partialTicks) {
			String str = I18n.format("gui.commandEditor.help.title");
			drawString(fontRendererObj, str, width / 2 - fontRendererObj.getStringWidth(str) / 2, 10, 0xffffff);
			if (name != null)
				drawString(fontRendererObj, name, width / 2 - fontRendererObj.getStringWidth(name) / 2, 22, 0xa0a0a0);
		}

		@Override
		protected void keyTyped(char typedChar, int keyCode) throws IOException {
			if (keyCode == Keyboard.KEY_ESCAPE) {
				mc.displayGuiScreen(prevScreen);
			} else {
				super.keyTyped(typedChar, keyCode);
			}
		}

		@Override
		protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
			if (mouseButton == 0) {
				if (mouseX < getShownWidth() && mouseY >= getHeaderHeight()
						&& mouseY < getHeaderHeight() + getShownHeight()) {
					int virtualWidth = width - 6;
					int y = getHeaderHeight() + 2 - getScrollY();

					for (ICommandHelpComponent component : components) {
						component.mouseClicked(mouseX, mouseY, y, virtualWidth);
						y += component.getHeight(virtualWidth) + 2;
					}
				}
			}

			super.mouseClicked(mouseX, mouseY, mouseButton);
		}

	}

}

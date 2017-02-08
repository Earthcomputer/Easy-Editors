package net.earthcomputer.easyeditors;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import com.google.common.collect.Lists;

import net.earthcomputer.easyeditors.api.util.GeneralUtils;
import net.earthcomputer.easyeditors.gui.GuiColorPicker;
import net.earthcomputer.easyeditors.gui.IColorPickerCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.fml.client.IModGuiFactory;
import net.minecraftforge.fml.client.config.DummyConfigElement.DummyCategoryElement;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.GuiConfigEntries;
import net.minecraftforge.fml.client.config.GuiConfigEntries.ButtonEntry;
import net.minecraftforge.fml.client.config.GuiConfigEntries.CategoryEntry;
import net.minecraftforge.fml.client.config.GuiConfigEntries.ListEntryBase;
import net.minecraftforge.fml.client.config.HoverChecker;
import net.minecraftforge.fml.client.config.IConfigElement;

/**
 * The Easy Editors GUI factory, used for config
 * 
 * @author Earthcomputer
 *
 */
public class GuiFactory implements IModGuiFactory {

	@Override
	public void initialize(Minecraft minecraftInstance) {

	}

	@Override
	public Class<? extends GuiScreen> mainConfigGuiClass() {
		return EasyEditorsConfigGui.class;
	}

	@Override
	public Set<RuntimeOptionCategoryElement> runtimeGuiCategories() {
		return null;
	}

	@Deprecated
	@Override
	public RuntimeOptionGuiHandler getHandlerFor(RuntimeOptionCategoryElement element) {
		return null;
	}

	/**
	 * The Easy Editors config GUI
	 * 
	 * @author Earthcomputer
	 *
	 */
	public static class EasyEditorsConfigGui extends GuiConfig {
		public EasyEditorsConfigGui(GuiScreen previousScreen) {
			super(previousScreen, getConfigElements(), EasyEditors.ID, false, false,
					I18n.format("gui.easyeditorsconfig.title"));
		}

		private static List<IConfigElement> getConfigElements() {
			List<IConfigElement> list = Lists.newArrayList();
			list.add(new DummyCategoryElement("general", "gui.easyeditorsconfig.ctgy.general", GeneralEntry.class));
			list.add(new DummyCategoryElement("colors", "gui.easyeditorsconfig.ctgy.colors", ColorsEntry.class));
			return list;
		}

		/**
		 * The category entry for general settings
		 * 
		 * @author Earthcomputer
		 *
		 */
		public static class GeneralEntry extends CategoryEntry {

			public GeneralEntry(GuiConfig owningScreen, GuiConfigEntries owningEntryList,
					IConfigElement configElement) {
				super(owningScreen, owningEntryList, configElement);
			}

			@Override
			protected GuiScreen buildChildScreen() {
				return new GuiConfig(owningScreen,
						new ConfigElement(EasyEditors.instance.config.getCategory("general")).getChildElements(),
						owningScreen.modID, false, false,
						GuiConfig.getAbridgedConfigPath(EasyEditors.instance.config.toString()));
			}

		}

		/**
		 * The category entry for color customization
		 * 
		 * @author Earthcomputer
		 *
		 */
		public static class ColorsEntry extends CategoryEntry {

			public ColorsEntry(GuiConfig owningScreen, GuiConfigEntries owningEntryList, IConfigElement configElement) {
				super(owningScreen, owningEntryList, configElement);
			}

			@Override
			protected GuiScreen buildChildScreen() {
				return new GuiConfig(owningScreen,
						new ConfigElement(EasyEditors.instance.config.getCategory("colors")).getChildElements(),
						owningScreen.modID, false, false,
						GuiConfig.getAbridgedConfigPath(EasyEditors.instance.config.toString()),
						I18n.format("gui.easyeditorsconfig.colortitleline2"));
			}

		}

		/**
		 * This entry class is basically the same as
		 * {@link GuiConfigEntries.CycleValueEntry CycleValueEntry}, except the
		 * cycled values can have more unique translation keys (specific to Easy
		 * Editors)
		 * 
		 * @author Earthcomputer
		 *
		 */
		public static class TranslatedCycleValueEntry extends ButtonEntry {
			protected final int beforeIndex;
			protected final int defaultIndex;
			protected int currentIndex;

			public TranslatedCycleValueEntry(GuiConfig owningScreen, GuiConfigEntries owningEntryList,
					IConfigElement configElement) {
				super(owningScreen, owningEntryList, configElement);
				beforeIndex = getIndex(configElement.get().toString());
				defaultIndex = getIndex(configElement.getDefault().toString());
				currentIndex = beforeIndex;
				this.btnValue.enabled = enabled();
				updateValueButtonText();
			}

			private int getIndex(String s) {
				for (int i = 0; i < configElement.getValidValues().length; i++)
					if (configElement.getValidValues()[i].equalsIgnoreCase(s)) {
						return i;
					}

				return 0;
			}

			@Override
			public void updateValueButtonText() {
				this.btnValue.displayString = I18n
						.format("gui.easyeditorsconfig.cycleValue." + configElement.getValidValues()[currentIndex]);
			}

			@Override
			public void valueButtonPressed(int slotIndex) {
				if (enabled()) {
					if (++this.currentIndex >= configElement.getValidValues().length)
						this.currentIndex = 0;

					updateValueButtonText();
				}
			}

			@Override
			public boolean isDefault() {
				return currentIndex == defaultIndex;
			}

			@Override
			public void setToDefault() {
				if (enabled()) {
					currentIndex = defaultIndex;
					updateValueButtonText();
				}
			}

			@Override
			public boolean isChanged() {
				return currentIndex != beforeIndex;
			}

			@Override
			public void undoChanges() {
				if (enabled()) {
					currentIndex = beforeIndex;
					updateValueButtonText();
				}
			}

			@Override
			public boolean saveConfigElement() {
				if (enabled() && isChanged()) {
					configElement.set(configElement.getValidValues()[currentIndex]);
					return configElement.requiresMcRestart();
				}
				return false;
			}

			@Override
			public String getCurrentValue() {
				return configElement.getValidValues()[currentIndex];
			}

			@Override
			public String[] getCurrentValues() {
				return new String[] { getCurrentValue() };
			}
		}

		/**
		 * The entry class used for customizing colors. Most of the code is
		 * adapted from
		 * {@link net.minecraftforge.fml.client.config.GuiConfigEntries.StringEntry
		 * StringEntry}
		 * 
		 * @author Earthcomputer
		 *
		 */
		public static class ColorEntry extends ListEntryBase implements IColorPickerCallback {

			protected final GuiTextField textFieldValue;
			protected final String beforeValue;
			protected final boolean allowAlpha;
			protected HoverChecker colorHoverChecker;
			protected final List<String> colorHoverTooltip = Arrays
					.asList(I18n.format("gui.easyeditorsconfig.colortooltip"));

			public ColorEntry(GuiConfig owningScreen, GuiConfigEntries owningEntryList, IConfigElement configElement) {
				super(owningScreen, owningEntryList, configElement);
				beforeValue = configElement.get().toString();
				this.textFieldValue = new GuiTextField(10, this.mc.fontRendererObj, this.owningEntryList.controlX + 1,
						0, this.owningEntryList.controlWidth - 38, 16);
				this.textFieldValue.setText(configElement.get().toString());
				this.allowAlpha = configElement.getDefault().toString().length() == 8;
			}

			@Override
			public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY,
					boolean isSelected) {
				super.drawEntry(slotIndex, x, y, listWidth, slotHeight, mouseX, mouseY, isSelected);

				this.textFieldValue.xPosition = this.owningEntryList.controlX + 2;
				this.textFieldValue.yPosition = y + 1;
				this.textFieldValue.width = this.owningEntryList.controlWidth - 39;
				this.textFieldValue.setEnabled(enabled());
				this.textFieldValue.drawTextBox();

				GlStateManager.color(1, 1, 1, 1);
				GlStateManager.enableBlend();
				int color = getColor();
				int rx = owningEntryList.controlX + textFieldValue.width + 6;
				Tessellator tessellator = Tessellator.getInstance();
				VertexBuffer buffer = tessellator.getBuffer();
				if (allowAlpha && ((color & 0xff000000) >>> 24) != 255) {
					mc.getTextureManager().bindTexture(GuiColorPicker.transparentBackground);
					buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
					buffer.pos(rx, y + 17, 0).tex(0, 16f / 16).endVertex();
					buffer.pos(rx + 32, y + 17, 0).tex(32f / 16, 16f / 16).endVertex();
					buffer.pos(rx + 32, y + 1, 0).tex(32f / 16, 0).endVertex();
					buffer.pos(rx, y + 1, 0).tex(0, 0).endVertex();
					tessellator.draw();
				}
				GlStateManager.disableTexture2D();
				int red = (color & 0x00ff0000) >> 16;
				int green = (color & 0x0000ff00) >> 8;
				int blue = color & 0x000000ff;
				int alpha = (color & 0xff000000) >>> 24;
				buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
				buffer.pos(rx, y + 17, 0).tex(0, 1).color(red, green, blue, alpha).endVertex();
				buffer.pos(rx + 32, y + 17, 0).tex(1, 1).color(red, green, blue, alpha).endVertex();
				buffer.pos(rx + 32, y + 1, 0).tex(1, 0).color(red, green, blue, alpha).endVertex();
				buffer.pos(rx, y + 1, 0).tex(0, 0).color(red, green, blue, alpha).endVertex();
				tessellator.draw();
				GlStateManager.enableTexture2D();

				if (colorHoverChecker == null)
					colorHoverChecker = new HoverChecker(y + 1, y + 17, rx, rx + 32, 800);
				else
					colorHoverChecker.updateBounds(y + 1, y + 17, rx, rx + 32);
			}

			@Override
			public void drawToolTip(int mouseX, int mouseY) {
				super.drawToolTip(mouseX, mouseY);

				boolean canHover = mouseY < owningScreen.entryList.bottom && mouseY > owningScreen.entryList.top;
				if (colorHoverChecker != null && colorHoverChecker.checkHover(mouseX, mouseY, canHover)) {
					owningScreen.drawToolTip(colorHoverTooltip, mouseX, mouseY);
				}
			}

			@Override
			public void keyTyped(char eventChar, int eventKey) {
				if (enabled() || eventKey == Keyboard.KEY_LEFT || eventKey == Keyboard.KEY_RIGHT
						|| eventKey == Keyboard.KEY_HOME || eventKey == Keyboard.KEY_END) {
					this.textFieldValue.textboxKeyTyped((enabled() ? eventChar : Keyboard.CHAR_NONE), eventKey);

					if (configElement.getValidationPattern() != null) {
						if (configElement.getValidationPattern().matcher(this.textFieldValue.getText().trim())
								.matches())
							isValidValue = true;
						else
							isValidValue = false;
					}
				}
			}

			@Override
			public void updateCursorCounter() {
				this.textFieldValue.updateCursorCounter();
			}

			@Override
			public void mouseClicked(int x, int y, int mouseEvent) {
				this.textFieldValue.mouseClicked(x, y, mouseEvent);

				int rx = owningEntryList.controlX + textFieldValue.width + 6;
				int ry = textFieldValue.yPosition;
				if (x >= rx && y >= ry && x < rx + 32 && y < ry + 16) {
					mc.displayGuiScreen(new GuiColorPicker(owningScreen, this, allowAlpha));
				}
			}

			@Override
			public boolean isDefault() {
				return configElement.getDefault() != null
						? configElement.getDefault().toString().equals(this.textFieldValue.getText())
						: this.textFieldValue.getText().trim().isEmpty();
			}

			@Override
			public void setToDefault() {
				if (enabled()) {
					this.textFieldValue.setText(this.configElement.getDefault().toString());
					keyTyped((char) Keyboard.CHAR_NONE, Keyboard.KEY_HOME);
				}
			}

			@Override
			public boolean isChanged() {
				return beforeValue != null ? !this.beforeValue.equals(textFieldValue.getText())
						: this.textFieldValue.getText().trim().isEmpty();
			}

			@Override
			public void undoChanges() {
				if (enabled())
					this.textFieldValue.setText(beforeValue);
			}

			@Override
			public boolean saveConfigElement() {
				if (enabled()) {
					if (isChanged() && this.isValidValue) {
						this.configElement.set(this.textFieldValue.getText());
						return configElement.requiresMcRestart();
					} else if (isChanged() && !this.isValidValue) {
						this.configElement.setToDefault();
						return configElement.requiresMcRestart() && beforeValue != null
								? beforeValue.equals(configElement.getDefault()) : configElement.getDefault() == null;
					}
				}
				return false;
			}

			@Override
			public Object getCurrentValue() {
				return this.textFieldValue.getText();
			}

			@Override
			public Object[] getCurrentValues() {
				return new Object[] { getCurrentValue() };
			}

			@Override
			public int getColor() {
				int color;
				if (isValidValue) {
					color = GeneralUtils.hexToInt(textFieldValue.getText());
					return allowAlpha ? color : color | 0xff000000;
				} else {
					color = GeneralUtils.hexToInt(configElement.getDefault().toString());
					return allowAlpha ? color : color | 0xff000000;
				}
			}

			@Override
			public void setColor(int color) {
				textFieldValue
						.setText(String.format(allowAlpha ? "%08X" : "%06X", allowAlpha ? color : color & 0x00ffffff));
			}

		}
	}

}

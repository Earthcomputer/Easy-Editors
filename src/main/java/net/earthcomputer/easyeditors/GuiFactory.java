package net.earthcomputer.easyeditors;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.lwjgl.input.Keyboard;

import com.google.common.collect.Lists;

import net.earthcomputer.easyeditors.api.GeneralUtils;
import net.earthcomputer.easyeditors.gui.GuiColorPicker;
import net.earthcomputer.easyeditors.gui.IColorPickerCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.fml.client.IModGuiFactory;
import net.minecraftforge.fml.client.config.DummyConfigElement.DummyCategoryElement;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.GuiConfigEntries;
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
			protected final List colorHoverTooltip = Arrays.asList(I18n.format("gui.easyeditorsconfig.colortooltip"));

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
				WorldRenderer worldRenderer = tessellator.getWorldRenderer();
				if (allowAlpha && ((color & 0xff000000) >>> 24) != 255) {
					mc.getTextureManager().bindTexture(GuiColorPicker.transparentBackground);
					worldRenderer.startDrawingQuads();
					worldRenderer.addVertexWithUV(rx, y + 17, 0, 0, 16f / 16);
					worldRenderer.addVertexWithUV(rx + 32, y + 17, 0, 32f / 16, 16f / 16);
					worldRenderer.addVertexWithUV(rx + 32, y + 1, 0, 32f / 16, 0);
					worldRenderer.addVertexWithUV(rx, y + 1, 0, 0, 0);
					tessellator.draw();
				}
				GlStateManager.disableTexture2D();
				worldRenderer.startDrawingQuads();
				worldRenderer.setColorRGBA_I(color & 0x00ffffff, allowAlpha ? ((color & 0xff000000) >>> 24) : 255);
				worldRenderer.addVertexWithUV(rx, y + 17, 0, 0, 1);
				worldRenderer.addVertexWithUV(rx + 32, y + 17, 0, 1, 1);
				worldRenderer.addVertexWithUV(rx + 32, y + 1, 0, 1, 0);
				worldRenderer.addVertexWithUV(rx, y + 1, 0, 0, 0);
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

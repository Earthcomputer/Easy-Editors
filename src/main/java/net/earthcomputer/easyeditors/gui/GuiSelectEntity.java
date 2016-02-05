package net.earthcomputer.easyeditors.gui;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.lwjgl.input.Keyboard;

import com.google.common.collect.Lists;

import net.earthcomputer.easyeditors.api.util.FakeWorld;
import net.earthcomputer.easyeditors.api.util.GeneralUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSlot;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

public class GuiSelectEntity extends GuiScreen {

	private GuiScreen previousScreen;
	private IEntitySelectorCallback callback;

	private List<String> entities;
	private int selectedIndex;

	private World theWorld;
	private Entity theEntity;

	private GuiButton cancelButton;
	private EList list;

	public GuiSelectEntity(GuiScreen previousScreen, IEntitySelectorCallback callback) {
		this(previousScreen, callback, false, false);
	}

	public GuiSelectEntity(GuiScreen previousScreen, IEntitySelectorCallback callback, boolean includePlayer,
			boolean includeLightning, String... additionalOptions) {
		this.previousScreen = previousScreen;
		this.callback = callback;

		entities = Lists.newArrayList(EntityList.getEntityNameList());
		if (includePlayer)
			entities.add("Player");
		if (!includeLightning)
			entities.remove("LightningBolt");
		for (String additionalOption : additionalOptions)
			entities.add(additionalOption);
		Collections.sort(entities, new Comparator<String>() {
			@Override
			public int compare(String s1, String s2) {
				return String.CASE_INSENSITIVE_ORDER.compare(getEntityName(s1), getEntityName(s2));
			}
		});

		if (entities.contains(callback.getEntity()))
			selectedIndex = entities.indexOf(callback.getEntity());

		theWorld = new FakeWorld();
		theEntity = createEntity(selectedIndex);
	}

	@Override
	public void initGui() {
		Keyboard.enableRepeatEvents(true);
		buttonList.add(new GuiButton(0, width / 2 - 160, height - 15 - 10, 150, 20, I18n.format("gui.done")));
		buttonList.add(
				cancelButton = new GuiButton(1, width / 2 + 5, height - 15 - 10, 150, 20, I18n.format("gui.cancel")));
		list = new EList();
	}

	@Override
	public void onGuiClosed() {
		Keyboard.enableRepeatEvents(false);
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		list.drawScreen(mouseX, mouseY, partialTicks);

		String str = I18n.format("gui.commandEditor.selectEntity.title");
		fontRendererObj.drawString(str, width / 2 - fontRendererObj.getStringWidth(str) / 2,
				15 - fontRendererObj.FONT_HEIGHT / 2, 0xffffff);

		super.drawScreen(mouseX, mouseY, partialTicks);

		GeneralUtils.renderEntityAt(theEntity, width * 3 / 4, height / 2, width / 2 - 120, 150, mouseX, mouseY);
	}

	@Override
	public void actionPerformed(GuiButton button) {
		switch (button.id) {
		case 0:
			callback.setEntity(entities.get(selectedIndex));
			mc.displayGuiScreen(previousScreen);
			break;
		case 1:
			mc.displayGuiScreen(previousScreen);
			break;
		default:
			list.actionPerformed(button);
		}
	}

	@Override
	public void keyTyped(char typedChar, int keyCode) throws IOException {
		if (keyCode == Keyboard.KEY_ESCAPE) {
			actionPerformed(cancelButton);
		} else {
			super.keyTyped(typedChar, keyCode);
		}
	}

	@Override
	public void handleMouseInput() throws IOException {
		super.handleMouseInput();
		list.handleMouseInput();
	}

	public static String getEntityName(String entity) {
		String unlocalized = "entity." + entity + ".name";
		if (StatCollector.canTranslate(unlocalized))
			return I18n.format(unlocalized);
		return entity;
	}

	private Entity createEntity(int slotIndex) {
		String entityName = entities.get(slotIndex);
		if ("Player".equals(entityName)) {
			return Minecraft.getMinecraft().thePlayer;
		} else if ("LightningBolt".equals(entityName)) {
			return new EntityLightningBolt(theWorld, 0, 0, 0);
		} else {
			return EntityList.createEntityByName(entityName, theWorld);
		}
	}

	private class EList extends GuiSlot {
		public EList() {
			super(GuiSelectEntity.this.mc, GuiSelectEntity.this.width / 2, GuiSelectEntity.this.height, 30,
					GuiSelectEntity.this.height - 30, GuiSelectEntity.this.fontRendererObj.FONT_HEIGHT * 2 + 8);
		}

		@Override
		protected int getSize() {
			return entities.size();
		}

		@Override
		protected void elementClicked(int slotIndex, boolean isDoubleClick, int mouseX, int mouseY) {
			selectedIndex = slotIndex;
			theEntity = createEntity(slotIndex);
		}

		@Override
		protected boolean isSelected(int slotIndex) {
			return slotIndex == selectedIndex;
		}

		@Override
		protected void drawBackground() {
			GuiSelectEntity.this.drawBackground(0);
		}

		@Override
		protected void drawSlot(int entryID, int x, int y, int height, int mouseX, int mouseY) {
			FontRenderer fontRenderer = GuiSelectEntity.this.fontRendererObj;
			String entityName = entities.get(entryID);

			String str = getEntityName(entityName);
			fontRenderer.drawString(str, x + getListWidth() / 2 - fontRenderer.getStringWidth(str) / 2, y + 2,
					0xffffff);
			fontRenderer.drawString(entityName, x + getListWidth() / 2 - fontRenderer.getStringWidth(entityName) / 2,
					y + 4 + fontRenderer.FONT_HEIGHT, 0xc0c0c0);
		}

		@Override
		public int getListWidth() {
			return GuiSelectEntity.this.width / 2 - 12;
		}

		@Override
		public int getScrollBarX() {
			return GuiSelectEntity.this.width / 2 - 6;
		}
	}

}

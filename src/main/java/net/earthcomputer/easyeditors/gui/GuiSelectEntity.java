package net.earthcomputer.easyeditors.gui;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import com.google.common.collect.Lists;

import net.earthcomputer.easyeditors.EasyEditors;
import net.earthcomputer.easyeditors.api.SmartTranslationRegistry;
import net.earthcomputer.easyeditors.api.util.FakeWorld;
import net.earthcomputer.easyeditors.api.util.GeneralUtils;
import net.earthcomputer.easyeditors.util.Translate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSlot;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

/**
 * The selection GUI for entities
 * 
 * @author Earthcomputer
 *
 */
public class GuiSelectEntity extends GuiScreen {

	private static final ResourceLocation LOCATION_ENTITY_BACKGROUND = new ResourceLocation(EasyEditors.ID,
			"textures/gui/select_entity_entity_background.png");
	private static final ResourceLocation LOCATION_ENTITY_FRAME = new ResourceLocation(EasyEditors.ID,
			"textures/gui/select_entity_entity_frame.png");

	public static final ResourceLocation PLAYER = new ResourceLocation("player");

	private GuiScreen previousScreen;
	private IEntitySelectorCallback callback;

	private List<ResourceLocation> allEntities;
	private List<ResourceLocation> displayedEntities;
	private ResourceLocation selectedEntity;

	private World theWorld;
	private Entity theEntity;

	private GuiTextField searchTextField;
	private GuiButton cancelButton;
	private EList list;

	public GuiSelectEntity(GuiScreen previousScreen, IEntitySelectorCallback callback) {
		this(previousScreen, callback, false, false);
	}

	public GuiSelectEntity(GuiScreen previousScreen, IEntitySelectorCallback callback, boolean includePlayer,
			boolean includeLightning, ResourceLocation... additionalOptions) {
		this.previousScreen = previousScreen;
		this.callback = callback;

		allEntities = Lists.newArrayList(EntityList.getEntityNameList());
		if (includePlayer)
			allEntities.add(PLAYER);
		if (includeLightning)
			allEntities.remove(EntityList.LIGHTNING_BOLT);
		for (ResourceLocation additionalOption : additionalOptions)
			allEntities.add(additionalOption);
		Collections.sort(allEntities, new Comparator<ResourceLocation>() {
			@Override
			public int compare(ResourceLocation s1, ResourceLocation s2) {
				return String.CASE_INSENSITIVE_ORDER.compare(getEntityName(s1), getEntityName(s2));
			}
		});
		displayedEntities = Lists.newArrayList(allEntities);

		if (displayedEntities.contains(callback.getEntity()))
			selectedEntity = callback.getEntity();

		theWorld = new FakeWorld();
		theEntity = createEntity(selectedEntity);
	}

	@Override
	public void initGui() {
		Keyboard.enableRepeatEvents(true);
		String searchLabel = Translate.GUI_COMMANDEDITOR_SELECTBLOCK_SEARCH;
		int labelWidth = fontRendererObj.getStringWidth(searchLabel);
		searchTextField = new GuiTextField(0, fontRendererObj, width / 2 - (205 + labelWidth) / 2 + labelWidth + 5, 25,
				200, 20);
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

		String str = Translate.GUI_COMMANDEDITOR_SELECTENTITY_TITLE;
		fontRendererObj.drawString(str, width / 2 - fontRendererObj.getStringWidth(str) / 2,
				15 - fontRendererObj.FONT_HEIGHT / 2, 0xffffff);

		super.drawScreen(mouseX, mouseY, partialTicks);

		str = Translate.GUI_COMMANDEDITOR_SELECTBLOCK_SEARCH;
		drawString(fontRendererObj, str, width / 2 - (fontRendererObj.getStringWidth(str) + 205) / 2, 30, 0xffffff);
		searchTextField.drawTextBox();

		final int x = this.width * 3 / 4;
		final int y = this.height / 2 + 10;
		final int width = this.width / 2 - 120;
		final int height = 150;
		final int left = x - width / 2;
		final int top = y - height / 2;
		final int right = x + width / 2;
		final int bottom = y + height / 2;
		final int frameWidth = width * 8 / 200;
		final int frameHeight = height * 8 / 275;

		Tessellator tessellator = Tessellator.getInstance();
		VertexBuffer buffer = tessellator.getBuffer();

		GlStateManager.color(1, 1, 1, 1);
		mc.getTextureManager().bindTexture(LOCATION_ENTITY_BACKGROUND);
		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		buffer.pos(left, top, 0).tex(0, 0).endVertex();
		buffer.pos(left, bottom, 0).tex(0, 1).endVertex();
		buffer.pos(right, bottom, 0).tex(1, 1).endVertex();
		buffer.pos(right, top, 0).tex(1, 0).endVertex();
		tessellator.draw();

		GL11.glEnable(GL11.GL_SCISSOR_TEST);
		GL11.glScissor(left * mc.displayWidth / this.width, (this.height - bottom) * mc.displayHeight / this.height,
				width * mc.displayWidth / this.width, height * mc.displayHeight / this.height);
		GeneralUtils.renderEntityAt(theEntity, x, y, width * 3 / 4, height * 3 / 4, mouseX, mouseY);
		GL11.glDisable(GL11.GL_SCISSOR_TEST);

		GlStateManager.disableDepth();
		mc.getTextureManager().bindTexture(LOCATION_ENTITY_FRAME);
		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		buffer.pos(left - frameWidth, top - frameHeight, 0).tex(0, 0).endVertex();
		buffer.pos(left - frameWidth, bottom + frameHeight, 0).tex(0, 1).endVertex();
		buffer.pos(right + frameWidth, bottom + frameHeight, 0).tex(1, 1).endVertex();
		buffer.pos(right + frameWidth, top - frameHeight, 0).tex(1, 0).endVertex();
		tessellator.draw();
	}

	@Override
	public void actionPerformed(GuiButton button) {
		switch (button.id) {
		case 0:
			callback.setEntity(selectedEntity);
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
			if (searchTextField.textboxKeyTyped(typedChar, keyCode)) {
				filterSearch();
			}
		}
	}

	@Override
	public void handleMouseInput() throws IOException {
		super.handleMouseInput();
		list.handleMouseInput();
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		super.mouseClicked(mouseX, mouseY, mouseButton);
		searchTextField.mouseClicked(mouseX, mouseY, mouseButton);
	}

	/**
	 * Returns a localized entity name for the given internal entity name
	 * 
	 * @param entity
	 * @return
	 */
	public static String getEntityName(ResourceLocation entity) {
		if (PLAYER.equals(entity)) {
			return Translate.ENTITY_PLAYER_NAME;
		} else if (EntityList.LIGHTNING_BOLT.equals(entity)) {
			return Translate.ENTITY_LIGHTNINGBOLT_NAME;
		}
		String unlocalized = "entity." + EntityList.getTranslationName(entity) + ".name";
		if (SmartTranslationRegistry.getLanguageMapInstance().isKeyTranslated(unlocalized))
			return I18n.format(unlocalized);
		return entity.getResourcePath();
	}

	private Entity createEntity(ResourceLocation entityName) {
		if (PLAYER.equals(entityName)) {
			return Minecraft.getMinecraft().player;
		} else if (EntityList.LIGHTNING_BOLT.equals(entityName)) {
			return new EntityLightningBolt(theWorld, 0, 0, 0, true);
		} else {
			return EntityList.createEntityByIDFromName(entityName, theWorld);
		}
	}

	private void filterSearch() {
		displayedEntities.clear();
		displayedEntities.addAll(allEntities);

		String searchText = searchTextField.getText();
		searchText = searchText.trim().toLowerCase();

		Iterator<ResourceLocation> entityItr = displayedEntities.iterator();
		while (entityItr.hasNext()) {
			ResourceLocation entity = entityItr.next();
			if (getEntityName(entity).toLowerCase().contains(searchText)) {
				continue;
			}
			if (String.valueOf(entity).toLowerCase().contains(searchText)) {
				continue;
			}
			entityItr.remove();
		}
	}

	private class EList extends GuiSlot {
		public EList() {
			super(GuiSelectEntity.this.mc, GuiSelectEntity.this.width / 2, GuiSelectEntity.this.height, 55,
					GuiSelectEntity.this.height - 30, GuiSelectEntity.this.fontRendererObj.FONT_HEIGHT * 2 + 8);
		}

		@Override
		protected int getSize() {
			return displayedEntities.size();
		}

		@Override
		protected void elementClicked(int slotIndex, boolean isDoubleClick, int mouseX, int mouseY) {
			selectedEntity = displayedEntities.get(slotIndex);
			theEntity = createEntity(selectedEntity);
		}

		@Override
		protected boolean isSelected(int slotIndex) {
			return displayedEntities.get(slotIndex).equals(selectedEntity);
		}

		@Override
		protected void drawBackground() {
			GuiSelectEntity.this.drawBackground(0);
		}

		@Override
		protected void drawSlot(int entryID, int x, int y, int height, int mouseX, int mouseY) {
			FontRenderer fontRenderer = GuiSelectEntity.this.fontRendererObj;
			ResourceLocation entityName = displayedEntities.get(entryID);

			String str = getEntityName(entityName);
			fontRenderer.drawString(str, x + getListWidth() / 2 - fontRenderer.getStringWidth(str) / 2, y + 2,
					0xffffff);
			fontRenderer.drawString(entityName.toString(),
					x + getListWidth() / 2 - fontRenderer.getStringWidth(entityName.toString()) / 2,
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

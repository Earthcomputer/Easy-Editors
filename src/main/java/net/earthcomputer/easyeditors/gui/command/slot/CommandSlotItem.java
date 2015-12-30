package net.earthcomputer.easyeditors.gui.command.slot;

import java.util.List;

import net.earthcomputer.easyeditors.api.Colors;
import net.earthcomputer.easyeditors.gui.command.CommandSyntaxException;
import net.earthcomputer.easyeditors.gui.command.GuiItemSelector;
import net.earthcomputer.easyeditors.gui.command.IItemSelectorCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;

/**
 * A command slot representing an item
 * 
 * @author Earthcomputer
 *
 */
public class CommandSlotItem extends GuiCommandSlotImpl implements IItemSelectorCallback {

	private ItemStack item;

	public CommandSlotItem() {
		super(18 + Minecraft.getMinecraft().fontRendererObj.getStringWidth(I18n.format("gui.commandEditor.noItem")),
				Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT > 16
						? Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT : 16);
		setItem(null);
	}

	@Override
	public int readFromArgs(String[] args, int index) throws CommandSyntaxException {
		if (args.length <= index)
			throw new CommandSyntaxException();
		Item item = Item.getByNameOrId(args[index]);
		setItem(item == null ? null : new ItemStack(item));
		return 1;
	}

	@Override
	public void addArgs(List<String> args) {
		String r = String.valueOf(Item.itemRegistry.getNameForObject(getItem().getItem()));
		args.add(r.startsWith("minecraft:") ? r.substring(10) : r);
	}

	private String getDisplayText() {
		return item == null ? I18n.format("gui.commandEditor.noItem") : item.getDisplayName();
	}

	@Override
	public void draw(int x, int y, int mouseX, int mouseY, float partialTicks) {
		FontRenderer fontRenderer = Minecraft.getMinecraft().fontRendererObj;
		RenderItem itemRender = Minecraft.getMinecraft().getRenderItem();

		RenderHelper.disableStandardItemLighting();
		RenderHelper.enableGUIStandardItemLighting();
		int top = fontRenderer.FONT_HEIGHT > 16 ? y + fontRenderer.FONT_HEIGHT / 2 - 8 : y;
		GlStateManager.translate(0, 0, 32);
		zLevel = 200;
		itemRender.zLevel = 200;
		itemRender.renderItemAndEffectIntoGUI(item, x, top);
		zLevel = 0;
		itemRender.zLevel = 0;
		RenderHelper.enableStandardItemLighting();
		GlStateManager.color(0, 0, 0, 0);

		GlStateManager.disableLighting();
		GlStateManager.disableFog();
		top = fontRenderer.FONT_HEIGHT > 16 ? y : y + 8 - fontRenderer.FONT_HEIGHT / 2;
		String str = getDisplayText();
		fontRenderer.drawString(str, x + 18, top, str.equals(I18n.format("gui.commandEditor.noItem"))
				? Colors.invalidItemName.color : Colors.itemName.color);
	}

	@Override
	public ItemStack getItem() {
		return item;
	}

	@Override
	public void setItem(ItemStack item) {
		if (!ItemStack.areItemStacksEqual(item, this.item)) {
			this.item = item;
			FontRenderer fontRenderer = Minecraft.getMinecraft().fontRendererObj;
			setWidth(18 + fontRenderer.getStringWidth(getDisplayText()));
		}
	}

	/**
	 * 
	 * @return An IGuiCommandSlot, consisting of this command slot, and a button
	 *         to change the item
	 */
	public IGuiCommandSlot withButton() {
		return new CommandSlotHorizontalArrangement(this, new CommandSlotButton(20, 20, "...") {
			@Override
			public void onPress() {
				Minecraft.getMinecraft().displayGuiScreen(
						new GuiItemSelector(Minecraft.getMinecraft().currentScreen, CommandSlotItem.this));
			}
		});
	}

}

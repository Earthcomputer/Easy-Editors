package net.earthcomputer.easyeditors.gui.command.slot;

import java.util.List;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import net.earthcomputer.easyeditors.api.Colors;
import net.earthcomputer.easyeditors.api.NBTToJson;
import net.earthcomputer.easyeditors.gui.command.CommandSyntaxException;
import net.earthcomputer.easyeditors.gui.command.GuiCommandEditor;
import net.earthcomputer.easyeditors.gui.command.GuiItemSelector;
import net.earthcomputer.easyeditors.gui.command.IItemSelectorCallback;
import net.earthcomputer.easyeditors.gui.command.ItemDamageHandler;
import net.earthcomputer.easyeditors.gui.command.NBTTagHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.client.config.HoverChecker;

/**
 * A command slot representing an item stack
 * 
 * @author Earthcomputer
 *
 */
public class CommandSlotItemStack extends CommandSlotVerticalArrangement implements IItemSelectorCallback {

	/**
	 * The item name component
	 */
	public static final int COMPONENT_ITEM = 1;
	/**
	 * The stack size component
	 */
	public static final int COMPONENT_STACK_SIZE = 2;
	/**
	 * The item damage component
	 */
	public static final int COMPONENT_DAMAGE = 4;
	/**
	 * The NBT tag component
	 */
	public static final int COMPONENT_NBT = 8;

	private int optionalStart;
	private int[] argOrder;
	private Item item;
	private int damage;

	private CommandSlotIntTextField stackSizeField;
	private List<ItemDamageHandler> damageHandlers = Lists.newArrayList();
	private CommandSlotModifiable damageSlot;
	private List<NBTTagHandler> nbtHandlers = Lists.newArrayList();
	private CommandSlotModifiable nbtSlot;

	/**
	 * Constructs a new command slot which represents an item stack
	 * 
	 * @param optionalStart
	 *            - the index at which the arguments start becoming optional,
	 *            relative to where this command slot starts reading. E.g. 0
	 *            would mean all the arguments are optional
	 * @param argOrder
	 *            - The order at which the arguments are to be inputed and
	 *            outputed. The sub-arguments are called components, and the
	 *            constants needed for this argument are defined in this class.
	 *            This array must contain {@link #COMPONENT_ITEM} and if it
	 *            contains {@link #COMPONENT_NBT}, it must be the last element
	 */
	public CommandSlotItemStack(int optionalStart, int... argOrder) {
		this.optionalStart = optionalStart;
		this.argOrder = argOrder;
		int displayComponents = 0;
		for (int component : argOrder) {
			displayComponents |= component;
		}
		final int finalDisplayComponents = displayComponents;

		addChild(new CommandSlotHorizontalArrangement(new CompItem(), new CommandSlotButton(20, 20, "...") {
			@Override
			public void onPress() {
				Minecraft.getMinecraft().displayGuiScreen(new GuiItemSelector(Minecraft.getMinecraft().currentScreen,
						CommandSlotItemStack.this, (finalDisplayComponents & (COMPONENT_DAMAGE | COMPONENT_NBT)) != 0));
			}
		}));

		if ((displayComponents & COMPONENT_STACK_SIZE) != 0)
			addChild(CommandSlotLabel.createLabel(I18n.format("gui.commandEditor.item.stackSize"),
					Colors.itemLabel.color, I18n.format("gui.commandEditor.item.stackSize.tooltip"),
					stackSizeField = new CommandSlotIntTextField(32, 32, 1, 64)));

		if ((displayComponents & COMPONENT_DAMAGE) != 0)
			addChild(damageSlot = new CommandSlotModifiable(null));

		if ((displayComponents & COMPONENT_NBT) != 0)
			addChild(nbtSlot = new CommandSlotModifiable(null));
	}

	@Override
	public int readFromArgs(String[] args, int index) throws CommandSyntaxException {
		if (index + optionalStart > args.length)
			throw new CommandSyntaxException();

		Item item = null;
		int stackSize = 1;
		int damage = 0;
		NBTTagCompound nbt = null;

		for (int i = 0; i < argOrder.length && i < args.length - index; i++) {
			switch (argOrder[i]) {
			case COMPONENT_ITEM:
				item = Item.getByNameOrId(args[index + i]);
				break;
			case COMPONENT_STACK_SIZE:
				try {
					stackSize = Integer.parseInt(args[index + i]);
				} catch (NumberFormatException e) {
					throw new CommandSyntaxException();
				}
				break;
			case COMPONENT_DAMAGE:
				try {
					damage = Integer.parseInt(args[index + i]);
				} catch (NumberFormatException e) {
					throw new CommandSyntaxException();
				}
				break;
			case COMPONENT_NBT:
				if (i != argOrder.length - 1)
					throw new IllegalStateException(
							"Invalid position of COMPONENT_NBT in argOrder. Must be last element");
				Joiner joiner = Joiner.on(' ');
				String[] nbtArgs = new String[args.length - index - i];
				System.arraycopy(args, index + i, nbtArgs, 0, nbtArgs.length);
				try {
					nbt = JsonToNBT.getTagFromJson(joiner.join(nbtArgs));
				} catch (NBTException e) {
					throw new CommandSyntaxException();
				}
				break;
			default:
				throw new IllegalStateException("Invalid value in argOrder: " + argOrder[i]);
			}
		}

		ItemStack stack = new ItemStack(item, stackSize, damage);
		stack.setTagCompound(nbt);
		setItem(stack);

		if (nbt != null)
			return args.length - index;
		return argOrder.length;
	}

	@Override
	public void addArgs(List<String> args) {
		List<String> potentialArgs = Lists.newArrayList();
		int maxElementToCopy = 0;
		for (int i = 0; i < argOrder.length; i++) {
			switch (argOrder[i]) {
			case COMPONENT_ITEM:
				String itemName = String.valueOf(Item.itemRegistry.getNameForObject(item));
				if (itemName.startsWith("minecraft:"))
					itemName = itemName.substring(10);
				potentialArgs.add(itemName);
				maxElementToCopy = i;
				break;
			case COMPONENT_STACK_SIZE:
				int stackSize = stackSizeField.getIntValue();
				if (i < optionalStart || stackSize != 1)
					maxElementToCopy = i;
				potentialArgs.add(String.valueOf(stackSize));
				break;
			case COMPONENT_DAMAGE:
				int damage = getDamage();
				if (i < optionalStart || damage != 0)
					maxElementToCopy = i;
				potentialArgs.add(String.valueOf(getDamage()));
				break;
			case COMPONENT_NBT:
				NBTTagCompound nbt = getNbt();
				if (i < optionalStart || nbt != null)
					maxElementToCopy = i;
				potentialArgs.add(nbt == null ? "{}" : NBTToJson.getJsonFromTag(nbt));
				break;
			}
		}
		potentialArgs = potentialArgs.subList(0, maxElementToCopy + 1);
		args.addAll(potentialArgs);
	}

	@Override
	public void setItem(ItemStack item) {
		this.item = item.getItem();
		if (stackSizeField != null)
			stackSizeField.setText(String.valueOf(item.stackSize));
		if (this.damageSlot != null) {
			damageHandlers = ItemDamageHandler.getHandlers(item.getItem());
			this.damageSlot.setChild(ItemDamageHandler.setupCommandSlot(damageHandlers, item.getItem()));
			this.damage = ItemDamageHandler.setDamage(damageHandlers, item.getItemDamage());
		} else {
			this.damage = 0;
		}
		if (this.nbtSlot != null) {
			nbtHandlers = NBTTagHandler.constructItemStackHandlers(item);
			this.nbtSlot.setChild(NBTTagHandler.setupCommandSlot(nbtHandlers));
			NBTTagHandler.readFromNBT(item.getTagCompound(), nbtHandlers);
		}
	}

	@Override
	public ItemStack getItem() {
		ItemStack stack = new ItemStack(item, stackSizeField == null ? 1 : stackSizeField.getIntValue(), getDamage());
		stack.setTagCompound(getNbt());
		return stack;
	}

	private int getDamage() {
		if (damageSlot == null)
			return damage;
		else
			return ItemDamageHandler.getDamage(damageHandlers, damage);
	}

	private NBTTagCompound getNbt() {
		if (nbtSlot == null)
			return null;
		else {
			NBTTagCompound nbt = new NBTTagCompound();
			NBTTagHandler.writeToNBT(nbt, nbtHandlers);
			return nbt.hasNoTags() ? null : nbt;
		}
	}

	/**
	 * 
	 * @return Whether this command slot would output a valid value
	 */
	public boolean isValid() {
		return item != null && !stackSizeField.getText().isEmpty();
	}

	private class CompItem extends GuiCommandSlotImpl {

		private HoverChecker hoverChecker;

		public CompItem() {
			super(18 + Minecraft.getMinecraft().fontRendererObj.getStringWidth(I18n.format("gui.commandEditor.noItem")),
					Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT > 16
							? Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT : 16);
		}

		@Override
		public void draw(int x, int y, int mouseX, int mouseY, float partialTicks) {
			FontRenderer fontRenderer = Minecraft.getMinecraft().fontRendererObj;
			RenderItem itemRender = Minecraft.getMinecraft().getRenderItem();

			ItemStack stack = null;

			if (item != null) {
				RenderHelper.disableStandardItemLighting();
				RenderHelper.enableGUIStandardItemLighting();
				int top = fontRenderer.FONT_HEIGHT > 16 ? y + fontRenderer.FONT_HEIGHT / 2 - 8 : y;
				GlStateManager.translate(0, 0, 32);
				zLevel = 200;
				itemRender.zLevel = 200;
				stack = new ItemStack(item, 1, getDamage());
				stack.setTagCompound(getNbt());
				itemRender.renderItemAndEffectIntoGUI(stack, x, top);
				zLevel = 0;
				itemRender.zLevel = 0;
				RenderHelper.enableStandardItemLighting();
				GlStateManager.color(0, 0, 0, 0);
			}

			GlStateManager.disableLighting();
			GlStateManager.disableFog();
			int top = fontRenderer.FONT_HEIGHT > 16 ? y : y + 8 - fontRenderer.FONT_HEIGHT / 2;
			String str = getDisplayText();
			fontRenderer.drawString(str, x + 18, top,
					item == null ? Colors.invalidItemName.color : Colors.itemName.color);
			setWidth(18 + fontRenderer.getStringWidth(str));

			if (item != null) {
				if (hoverChecker == null)
					hoverChecker = new HoverChecker(y, y + getHeight(), x, x + getWidth(), 1000);
				else
					hoverChecker.updateBounds(y, y + getHeight(), x, x + getWidth());

				if (!GuiCommandEditor.isInBounds(mouseX, mouseY))
					hoverChecker.resetHoverTimer();
				else if (hoverChecker.checkHover(mouseX, mouseY)) {
					drawTooltip(mouseX, mouseY, stack.getTooltip(Minecraft.getMinecraft().thePlayer,
							Minecraft.getMinecraft().gameSettings.advancedItemTooltips));
				}
			}
		}

		public String getDisplayText() {
			if (item == null)
				return I18n.format("gui.commandEditor.noItem");
			else {
				ItemStack stack = new ItemStack(item, stackSizeField == null ? 1 : stackSizeField.getIntValue(),
						getDamage());
				stack.setTagCompound(getNbt());
				return stack.getDisplayName();
			}
		}

		@Override
		public int readFromArgs(String[] args, int index) throws CommandSyntaxException {
			return 0;
		}

		@Override
		public void addArgs(List<String> args) {
		}
	}

}

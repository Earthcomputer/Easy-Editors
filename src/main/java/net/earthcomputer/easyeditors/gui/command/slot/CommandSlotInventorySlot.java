package net.earthcomputer.easyeditors.gui.command.slot;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import com.google.common.base.Objects;

import net.earthcomputer.easyeditors.gui.command.CommandSyntaxException;
import net.earthcomputer.easyeditors.gui.command.SlotHandler;
import net.earthcomputer.easyeditors.gui.command.UIInvalidException;
import net.earthcomputer.easyeditors.util.TranslateKeys;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;

/**
 * Represents an inventory slot, used mainly in <code>/replaceitem</code>
 * 
 * @author Earthcomputer
 *
 */
public class CommandSlotInventorySlot extends CommandSlotVerticalArrangement {

	private ResourceLocation currentEntityType;
	private CommandSlotMenu slotHandlersMenu;
	private CommandSlotModifiable<CommandSlotVerticalArrangement> parametersWrapper = new CommandSlotModifiable<CommandSlotVerticalArrangement>(
			null);
	private List<SlotHandler> slotHandlers;
	private IGuiCommandSlot[] logicalParameters;

	public CommandSlotInventorySlot() {
		setEntityType(CommandSlotPlayerSelector.ENTITY_ANYTHING);
	}

	/**
	 * Sets the entity type to allow this command slot to filter out slot
	 * handlers. <code>null</code> for container inventories,
	 * {@link CommandSlotPlayerSelector#ENTITY_ANYTHING} for unknown entity.
	 * Otherwise, use the registry name of the entity type
	 * 
	 * @param entityType
	 */
	public void setEntityType(@Nullable ResourceLocation entityType) {
		if (Objects.equal(currentEntityType, entityType)) {
			return;
		}
		currentEntityType = entityType;

		clearChildren();

		if (entityType == null) {
			slotHandlers = Collections.singletonList(SlotHandler.CONTAINER_HANDLER);
		} else if (CommandSlotPlayerSelector.ENTITY_ANYTHING.equals(entityType)) {
			slotHandlers = SlotHandler.getAllHandlers();
		} else {
			slotHandlers = SlotHandler.getHandlersForEntity(entityType);
		}

		String[] values = new String[slotHandlers.size()];
		for (int i = 0; i < values.length; i++) {
			values[i] = I18n.format(slotHandlers.get(i).getUnlocalizedName());
		}
		slotHandlersMenu = new CommandSlotMenu(values) {
			@Override
			protected void onChanged(String to) {
				refreshParameters();
			}
		};

		addChild(slotHandlersMenu);
		addChild(parametersWrapper);
		refreshParameters();
	}

	private void refreshParameters() {
		if (slotHandlers.isEmpty()) {
			parametersWrapper.setChild(new CommandSlotVerticalArrangement());
			return;
		}
		SlotHandler handler = slotHandlers.get(slotHandlersMenu.getCurrentIndex());
		logicalParameters = handler.setupCommandSlots();
		parametersWrapper.setChild(new CommandSlotVerticalArrangement(handler.layoutCommandSlots(logicalParameters)));
	}

	@Override
	public int readFromArgs(String[] args, int index) throws CommandSyntaxException {
		if (args.length == index) {
			throw new CommandSyntaxException();
		}

		boolean found = false;
		for (int i = 0; i < slotHandlers.size(); i++) {
			if (slotHandlers.get(i).handlesSlot(args[index])) {
				slotHandlersMenu.setCurrentIndex(i);
				found = true;
				break;
			}
		}
		if (!found) {
			throw new CommandSyntaxException();
		}

		slotHandlers.get(slotHandlersMenu.getCurrentIndex()).initializeCommandSlots(args[index], logicalParameters);
		return 1;
	}

	@Override
	public void addArgs(List<String> args) throws UIInvalidException {
		if (slotHandlers.isEmpty()) {
			throw new UIInvalidException(TranslateKeys.GUI_COMMANDEDITOR_REPLACEITEM_NOSLOTHANDLER);
		}
		args.add(slotHandlers.get(slotHandlersMenu.getCurrentIndex()).getSlotName(logicalParameters));
	}

}

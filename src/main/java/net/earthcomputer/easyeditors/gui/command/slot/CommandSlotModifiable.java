package net.earthcomputer.easyeditors.gui.command.slot;

/**
 * A command slot which wraps a child which can be changed
 * 
 * @author Earthcomputer
 *
 */
public class CommandSlotModifiable extends CommandSlotBox {

	public CommandSlotModifiable() {
		this(null);
	}
	
	public CommandSlotModifiable(IGuiCommandSlot child) {
		super(child);
	}

	@Override
	public void setChild(IGuiCommandSlot child) {
		super.setChild(child);
	}

}

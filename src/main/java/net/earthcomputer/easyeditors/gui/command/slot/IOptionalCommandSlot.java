package net.earthcomputer.easyeditors.gui.command.slot;

import net.earthcomputer.easyeditors.gui.command.UIInvalidException;

public interface IOptionalCommandSlot {

	boolean isDefault() throws UIInvalidException;

	void setToDefault();

}

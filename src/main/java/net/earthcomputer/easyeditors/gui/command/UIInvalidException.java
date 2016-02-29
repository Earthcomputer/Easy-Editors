package net.earthcomputer.easyeditors.gui.command;

import net.minecraft.client.resources.I18n;

/**
 * Thrown when a UI cannot be compiled into a lower-level data structure
 * 
 * @author Earthcomputer
 *
 */
public class UIInvalidException extends Exception {

	private static final long serialVersionUID = 3029663909229444081L;

	public UIInvalidException(String reason, Object... formatArgs) {
		super(I18n.format(reason, formatArgs));
	}

}

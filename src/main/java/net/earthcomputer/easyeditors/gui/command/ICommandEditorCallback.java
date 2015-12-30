package net.earthcomputer.easyeditors.gui.command;

/**
 * An interface for use with the command editor and the command selector. Once
 * the user is done with either of these GUIs, setCommand will be invoked. The
 * command being set will be something like "give Earthcomputer cobblestone" for
 * GuiCommandEditor and just "give" for GuiItemSelector
 * 
 * @author Earthcomputer
 *
 */
public interface ICommandEditorCallback {

	/**
	 * Sets the command
	 * @param command
	 */
	void setCommand(String command);

	/**
	 * 
	 * @return The command
	 */
	String getCommand();

}

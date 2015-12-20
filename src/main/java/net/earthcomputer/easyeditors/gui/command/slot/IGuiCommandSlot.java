package net.earthcomputer.easyeditors.gui.command.slot;

import java.util.List;

import net.earthcomputer.easyeditors.gui.ISizeChangeListener;
import net.earthcomputer.easyeditors.gui.command.CommandSyntaxException;

public interface IGuiCommandSlot {

	void addSizeChangeListener(ISizeChangeListener listener);

	void removeSizeChangeListener(ISizeChangeListener listener);
	
	int readFromArgs(String[] args, int index) throws CommandSyntaxException;

	void addArgs(List<String> args);

	void draw(int x, int y, int mouseX, int mouseY, float partialTicks);

	int getWidth();

	void setWidth(int width);

	int getHeight();

	void setHeight(int height);

	void onKeyTyped(char typedChar, int keyCode);

	void onMouseClicked(int mouseX, int mouseY, int mouseButton);

	void onMouseReleased(int mouseX, int mouseY, int mouseButton);

	void onMouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick);

}

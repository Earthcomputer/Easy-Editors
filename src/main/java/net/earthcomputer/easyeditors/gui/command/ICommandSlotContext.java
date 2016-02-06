package net.earthcomputer.easyeditors.gui.command;

import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public interface ICommandSlotContext {

	World getWorld();
	
	BlockPos getPos();
	
	ICommandSender getSender();
	
	boolean isMouseInBounds(int mouseX, int mouseY);
	
}

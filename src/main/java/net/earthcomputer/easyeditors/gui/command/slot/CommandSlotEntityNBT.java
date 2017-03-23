package net.earthcomputer.easyeditors.gui.command.slot;

import java.util.List;

import org.apache.commons.lang3.ArrayUtils;

import com.google.common.base.Joiner;

import net.earthcomputer.easyeditors.api.util.NBTToJson;
import net.earthcomputer.easyeditors.gui.command.CommandSlotContext;
import net.earthcomputer.easyeditors.gui.command.CommandSyntaxException;
import net.earthcomputer.easyeditors.gui.command.NBTTagHandler;
import net.earthcomputer.easyeditors.gui.command.UIInvalidException;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;

public class CommandSlotEntityNBT extends CommandSlotBox implements IOptionalCommandSlot {

	
	private NBTTagCompound nbt = new NBTTagCompound();
	private List<NBTTagHandler> entityHandlers;
	private Class<? extends Entity> entityType;

	public CommandSlotEntityNBT(CommandSlotContext context) {
		super(null);

		entityHandlers = NBTTagHandler.constructEntityHandlers(null, context);
		setChild(NBTTagHandler.setupCommandSlot(entityHandlers));
		NBTTagHandler.readFromNBT(nbt, entityHandlers);
	}

	public void setEntityType(Class<? extends Entity> entityType) {
		if (entityType == this.entityType) {
			return;
		}
		this.entityType = entityType;

		entityHandlers = NBTTagHandler.constructEntityHandlers(entityType, getContext());
		setChild(NBTTagHandler.setupCommandSlot(entityHandlers));
		NBTTagHandler.readFromNBT(nbt, entityHandlers);
	}

	@Override
	public int readFromArgs(String[] args, int index) throws CommandSyntaxException {
		if (args.length == index) {
			throw new CommandSyntaxException();
		}

		try {
			nbt = JsonToNBT.getTagFromJson(Joiner.on(' ').join(ArrayUtils.subarray(args, index, args.length)));
		} catch (NBTException e) {
			throw new CommandSyntaxException();
		}

		NBTTagHandler.readFromNBT(nbt, entityHandlers);
		return args.length - index;
	}

	@Override
	public void addArgs(List<String> args) throws UIInvalidException {
		NBTTagCompound nbt = new NBTTagCompound();
		NBTTagHandler.writeToNBT(nbt, entityHandlers);
		for (String part : NBTToJson.getJsonFromTag(nbt).split(" ")) {
			args.add(part);
		}
	}

	@Override
	public boolean isDefault() {
		NBTTagCompound nbt = new NBTTagCompound();
		NBTTagHandler.writeToNBT(nbt, entityHandlers);
		return nbt.hasNoTags();
	}

	@Override
	public void setToDefault() {
		nbt = new NBTTagCompound();
		NBTTagHandler.readFromNBT(nbt, entityHandlers);
	}

}

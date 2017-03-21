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

public class CommandSlotEntityNBT extends CommandSlotModifiable<IGuiCommandSlot> {

	private boolean optional;

	private NBTTagCompound nbt = new NBTTagCompound();
	private List<NBTTagHandler> entityHandlers;
	private Class<? extends Entity> entityType;

	public CommandSlotEntityNBT(CommandSlotContext context) {
		this(true, context);
	}

	public CommandSlotEntityNBT(boolean optional, CommandSlotContext context) {
		super(null);
		this.optional = optional;

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
			if (!optional) {
				throw new CommandSyntaxException();
			}
			nbt = new NBTTagCompound();
		} else {
			try {
				nbt = JsonToNBT.getTagFromJson(Joiner.on(' ').join(ArrayUtils.subarray(args, index, args.length)));
			} catch (NBTException e) {
				throw new CommandSyntaxException();
			}
		}
		NBTTagHandler.readFromNBT(nbt, entityHandlers);
		return args.length - index;
	}

	@Override
	public void addArgs(List<String> args) throws UIInvalidException {
		NBTTagCompound nbt = new NBTTagCompound();
		NBTTagHandler.writeToNBT(nbt, entityHandlers);
		if (optional && nbt.hasNoTags()) {
			return;
		}
		for (String part : NBTToJson.getJsonFromTag(nbt).split(" ")) {
			args.add(part);
		}
	}

}

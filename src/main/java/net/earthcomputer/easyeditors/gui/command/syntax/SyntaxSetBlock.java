package net.earthcomputer.easyeditors.gui.command.syntax;

import java.util.List;

import org.apache.commons.lang3.ArrayUtils;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import net.earthcomputer.easyeditors.api.util.Colors;
import net.earthcomputer.easyeditors.api.util.NBTToJson;
import net.earthcomputer.easyeditors.gui.command.CommandSyntaxException;
import net.earthcomputer.easyeditors.gui.command.NBTTagHandler;
import net.earthcomputer.easyeditors.gui.command.UIInvalidException;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotBlock;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotBox;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotLabel;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotMenu;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotOptional;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotRectangle;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotRelativeCoordinate;
import net.earthcomputer.easyeditors.gui.command.slot.IGuiCommandSlot;
import net.earthcomputer.easyeditors.gui.command.slot.IOptionalCommandSlot;
import net.earthcomputer.easyeditors.util.Translate;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

public class SyntaxSetBlock extends CommandSyntax {

	private CommandSlotRelativeCoordinate pos;
	private CommandSlotBlock block;
	private CommandSlotMenu.Optional mode;
	private BlockNBT nbt;

	@Override
	public IGuiCommandSlot[] setupCommand() {
		pos = new CommandSlotRelativeCoordinate();

		List<CommandSlotOptional> optionalGroup = Lists.newArrayList();

		mode = new CommandSlotMenu.WithDefault("replace",
				new String[] { Translate.GUI_COMMANDEDITOR_SETBLOCK_MODE_REPLACE,
						Translate.GUI_COMMANDEDITOR_SETBLOCK_MODE_DESTROY,
						Translate.GUI_COMMANDEDITOR_SETBLOCK_MODE_KEEP },
				"replace", "destroy", "keep");

		nbt = new BlockNBT();

		final CommandSlotOptional firstOptionalCmdSlot = new CommandSlotOptional.Impl(mode, optionalGroup);

		block = new CommandSlotBlock(false, 1, CommandSlotBlock.COMPONENT_BLOCK,
				CommandSlotBlock.COMPONENT_PROPERTIES) {
			@Override
			public boolean canSkipOptionals() throws UIInvalidException {
				return !firstOptionalCmdSlot.shouldAddToArgs();
			}

			@Override
			public void setBlock(IBlockState block) {
				super.setBlock(block);
				nbt.setBlockType(block);
			}
		};

		return new IGuiCommandSlot[] { CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_SETBLOCK_POS, pos),
				CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_SETBLOCK_BLOCK,
						new CommandSlotRectangle(block, Colors.itemBox.color)),
				CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_SETBLOCK_MODE, firstOptionalCmdSlot),
				new CommandSlotOptional.Impl(nbt, optionalGroup) };
	}

	public static class BlockNBT extends CommandSlotBox implements IOptionalCommandSlot {

		private Class<? extends TileEntity> currentTEClass = null;
		private List<NBTTagHandler> handlers = Lists.newArrayList();
		private NBTTagCompound nbt = new NBTTagCompound();

		public BlockNBT() {
			super(null);
		}

		public void setBlockType(IBlockState state) {
			boolean hasTE = false;
			if (state.getBlock().hasTileEntity(state)) {
				TileEntity te = state.getBlock().createTileEntity(Minecraft.getMinecraft().world, state);
				if (te != null) {
					hasTE = true;
					if (te.getClass() != currentTEClass) {
						currentTEClass = te.getClass();
						handlers = NBTTagHandler.constructTileEntityHandlers(currentTEClass, getContext());
						setChild(CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_SETBLOCK_NBT,
								new CommandSlotRectangle(NBTTagHandler.setupCommandSlot(handlers),
										Colors.nbtBox.color)));
						NBTTagHandler.readFromNBT(nbt, handlers);
					}
				}
			}
			if (!hasTE) {
				handlers.clear();
				setChild(null);
			}
		}

		@Override
		public boolean isDefault() throws UIInvalidException {
			NBTTagCompound nbt = new NBTTagCompound();
			NBTTagHandler.writeToNBT(nbt, handlers);
			return nbt.hasNoTags();
		}

		@Override
		public void setToDefault() {
			nbt = new NBTTagCompound();
			NBTTagHandler.readFromNBT(nbt, handlers);
		}

		@Override
		public int readFromArgs(String[] args, int index) throws CommandSyntaxException {
			try {
				nbt = JsonToNBT.getTagFromJson(Joiner.on(' ').join(ArrayUtils.subarray(args, index, args.length)));
			} catch (NBTException e) {
				throw new CommandSyntaxException();
			}
			NBTTagHandler.readFromNBT(nbt, handlers);
			return args.length - index;
		}

		@Override
		public void addArgs(List<String> args) throws UIInvalidException {
			NBTTagCompound nbt = new NBTTagCompound();
			NBTTagHandler.writeToNBT(nbt, handlers);
			args.add(NBTToJson.getJsonFromTag(nbt));
		}

	}

}

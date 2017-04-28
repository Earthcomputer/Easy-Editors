package net.earthcomputer.easyeditors.gui.command.syntax;

import java.util.List;

import org.apache.commons.lang3.ArrayUtils;

import com.google.common.base.Joiner;
import com.google.common.base.Predicate;

import net.earthcomputer.easyeditors.api.util.Colors;
import net.earthcomputer.easyeditors.api.util.GeneralUtils;
import net.earthcomputer.easyeditors.api.util.NBTToJson;
import net.earthcomputer.easyeditors.gui.command.CommandSyntaxException;
import net.earthcomputer.easyeditors.gui.command.UIInvalidException;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotBlock;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotBox;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotLabel;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotModifiable;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotRectangle;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotRelativeCoordinate;
import net.earthcomputer.easyeditors.gui.command.slot.IGuiCommandSlot;
import net.earthcomputer.easyeditors.util.Translate;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.Constants;

public class SyntaxBlockData extends CommandSyntax {

	private CommandSlotRelativeCoordinate position;
	private CommandSlotModifiable modifiableUnknownBlockMessage;
	private CommandSlotLabel unknownBlockMessage;
	private CommandSlotBlock block;
	private IGuiCommandSlot data;

	private NBTTagCompound pendingNbt;

	@Override
	public IGuiCommandSlot[] setupCommand() {
		position = new CommandSlotRelativeCoordinate();

		unknownBlockMessage = new CommandSlotLabel(Minecraft.getMinecraft().fontRendererObj,
				Translate.GUI_COMMANDEDITOR_BLOCKDATA_UNKNWONBLOCK);

		modifiableUnknownBlockMessage = new CommandSlotModifiable(unknownBlockMessage);

		block = new CommandSlotBlock(false, 2, CommandSlotBlock.COMPONENT_BLOCK, CommandSlotBlock.COMPONENT_NBT) {
			@Override
			public void setBlock(IBlockState block) {
				if (block == null) {
					modifiableUnknownBlockMessage.setChild(unknownBlockMessage);
				} else {
					modifiableUnknownBlockMessage.setChild(null);
				}
				super.setBlock(block);
				if (block != null && pendingNbt != null) {
					setNbt(pendingNbt);
					pendingNbt = null;
				}
			}
		};
		final Predicate<IBlockState> hasTileEntityPredicate = new Predicate<IBlockState>() {
			@Override
			public boolean apply(IBlockState input) {
				return input.getBlock().hasTileEntity(input);
			}
		};
		block.setAllowedBlockPredicate(hasTileEntityPredicate);

		data = new CommandSlotBox(block) {
			@Override
			public int readFromArgs(String[] args, int index) throws CommandSyntaxException {
				if (args.length == index) {
					throw new CommandSyntaxException();
				}
				String[] nbtArgs = ArrayUtils.subarray(args, index, args.length);
				NBTTagCompound nbt;
				try {
					nbt = JsonToNBT.getTagFromJson(Joiner.on(' ').join(nbtArgs));
				} catch (NBTException e) {
					throw new CommandSyntaxException();
				}
				Block block = Blocks.AIR;
				if (nbt.hasKey("ee_te", Constants.NBT.TAG_STRING)) {
					try {
						block = CommandBase.getBlockByText(null, nbt.getString("ee_te"));
					} catch (CommandException e) {
						throw new CommandSyntaxException();
					}
				}
				if (!hasTileEntityPredicate.apply(block.getDefaultState())) {
					pendingNbt = nbt;
					SyntaxBlockData.this.block.setBlock(null);
				} else {
					pendingNbt = null;
					super.readFromArgs(ArrayUtils.add(nbtArgs, 0, block.delegate.name().toString()), 0);
				}
				return args.length - index;
			}

			@Override
			public void addArgs(List<String> args) throws UIInvalidException {
				block.checkValid();
				NBTTagCompound nbt = block.getNbt();
				if (nbt == null) {
					nbt = new NBTTagCompound();
				}
				nbt.setString("ee_te",
						GeneralUtils.resourceLocationToString(block.getBlock().getBlock().delegate.name()));
				for (String part : NBTToJson.getJsonFromTag(nbt).split(" ")) {
					args.add(part);
				}
			}
		};

		return new IGuiCommandSlot[] {
				CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_BLOCKDATA_POS, position),
				modifiableUnknownBlockMessage, CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_BLOCKDATA_DATA,
						new CommandSlotRectangle(data, Colors.itemBox.color)) };
	}

}

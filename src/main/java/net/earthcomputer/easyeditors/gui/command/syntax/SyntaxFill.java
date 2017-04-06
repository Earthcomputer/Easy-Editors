package net.earthcomputer.easyeditors.gui.command.syntax;

import java.util.List;

import com.google.common.collect.Lists;

import net.earthcomputer.easyeditors.api.util.Colors;
import net.earthcomputer.easyeditors.gui.command.CommandSyntaxException;
import net.earthcomputer.easyeditors.gui.command.UIInvalidException;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotBlock;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotLabel;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotMenu;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotModifiable;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotOptional;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotRectangle;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotRelativeCoordinate;
import net.earthcomputer.easyeditors.gui.command.slot.IGuiCommandSlot;
import net.earthcomputer.easyeditors.util.Translate;
import net.earthcomputer.easyeditors.util.TranslateKeys;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;

public class SyntaxFill extends CommandSyntax {

	private CommandSlotRelativeCoordinate from;
	private CommandSlotRelativeCoordinate to;
	private CommandSlotBlock block;
	private CommandSlotMenu mode;
	private CommandSlotModifiable modifiableModeArg;
	private IGuiCommandSlot replaceArg;
	private IGuiCommandSlot nbtArg;
	private CommandSlotBlock replacing;
	private SyntaxSetBlock.BlockNBT nbt;

	@Override
	public IGuiCommandSlot[] setupCommand() {
		from = new CommandSlotRelativeCoordinate();
		to = new CommandSlotRelativeCoordinate();

		mode = new CommandSlotMenu(
				new String[] { Translate.GUI_COMMANDEDITOR_FILL_MODE_REPLACEALL,
						Translate.GUI_COMMANDEDITOR_FILL_MODE_DESTROY, Translate.GUI_COMMANDEDITOR_FILL_MODE_KEEP,
						Translate.GUI_COMMANDEDITOR_FILL_MODE_REPLACE, Translate.GUI_COMMANDEDITOR_FILL_MODE_HOLLOW,
						Translate.GUI_COMMANDEDITOR_FILL_MODE_OUTLINE },
				"", "destroy", "keep", "replace", "hollow", "outline") {
			@Override
			public int readFromArgs(String[] args, int index) throws CommandSyntaxException {
				if (!"destroy".equals(args[index]) && !"keep".equals(args[index]) && !"hollow".equals(args[index])
						&& !"outline".equals(args[index])) {
					boolean hasTileEntity = false;
					try {
						// No need for sender?! Just use null.
						Block block = CommandBase.getBlockByText(null, args[index - 2]);
						IBlockState state = CommandBase.convertArgToBlockState(block, args[index - 1]);
						hasTileEntity = block.hasTileEntity(state);
					} catch (CommandException e) {
						throw new RuntimeException(
								"This should never be thrown because it should have been handled by previous command slots",
								e);
					}
					if (!"replace".equals(args[index]) || args.length == index + 1 || hasTileEntity) {
						setCurrentIndex(0);
						return 1;
					}
				}
				return super.readFromArgs(args, index);
			}

			@Override
			public void addArgs(List<String> args) throws UIInvalidException {
				if (getCurrentValue().equals("replace")
						&& block.getBlock().getBlock().hasTileEntity(block.getBlock())) {
					throw new UIInvalidException(TranslateKeys.GUI_COMMANDEDITOR_FILL_REPLACEWITHTILEENTITY);
				}
				super.addArgs(args);
			}

			@Override
			protected void onChanged(String to) {
				if ("replace".equals(to)) {
					modifiableModeArg.setChild(replaceArg);
				} else {
					modifiableModeArg.setChild(nbtArg);
				}
			}
		};

		List<CommandSlotOptional> optionalGroup = Lists.newArrayList();

		final CommandSlotOptional firstOptionalArg = new CommandSlotOptional(mode, optionalGroup) {
			@Override
			public boolean isDefault() throws UIInvalidException {
				return mode.getCurrentIndex() == 0;
			}

			@Override
			public void setToDefault() {
				mode.setCurrentIndex(0);
			}
		};

		replacing = new CommandSlotBlock(true, 1, CommandSlotBlock.COMPONENT_BLOCK,
				CommandSlotBlock.COMPONENT_PROPERTIES);

		nbt = new SyntaxSetBlock.BlockNBT();

		replaceArg = CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_FILL_REPLACING,
				new CommandSlotRectangle(replacing, Colors.itemBox.color));

		nbtArg = new CommandSlotOptional.Impl(nbt, optionalGroup);

		modifiableModeArg = new CommandSlotModifiable(nbtArg);

		block = new CommandSlotBlock(false, 1, CommandSlotBlock.COMPONENT_BLOCK,
				CommandSlotBlock.COMPONENT_PROPERTIES) {
			@Override
			protected boolean canSkipOptionals() throws UIInvalidException {
				return !firstOptionalArg.shouldAddToArgs();
			}

			@Override
			public void setBlock(IBlockState block) {
				super.setBlock(block);
				nbt.setBlockType(block);
			}
		};

		return new IGuiCommandSlot[] { CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_FILL_FROM, from),
				CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_FILL_TO, to),
				CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_FILL_BLOCK,
						new CommandSlotRectangle(block, Colors.itemBox.color)),
				CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_FILL_MODE, mode), modifiableModeArg };
	}

}

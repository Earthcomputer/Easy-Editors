package net.earthcomputer.easyeditors.gui.command.slot;

import java.util.List;

import net.earthcomputer.easyeditors.gui.ICallback;
import net.earthcomputer.easyeditors.gui.command.CommandSyntaxException;
import net.earthcomputer.easyeditors.gui.command.GuiSelectScoreCriteria;
import net.earthcomputer.easyeditors.gui.command.UIInvalidException;
import net.earthcomputer.easyeditors.util.Translate;
import net.earthcomputer.easyeditors.util.TranslateKeys;
import net.minecraft.client.Minecraft;
import net.minecraft.scoreboard.IScoreCriteria;

public class CommandSlotScoreCriteria extends CommandSlotHorizontalArrangement implements ICallback<IScoreCriteria> {

	private IScoreCriteria selectedCriteria = null;
	private CommandSlotLabel criteriaLabel;

	public CommandSlotScoreCriteria() {
		addChild(criteriaLabel = new CommandSlotLabel(Minecraft.getMinecraft().fontRendererObj,
				Translate.GUI_COMMANDEDITOR_NOSCORECRITERIA, 0xff0000));

		addChild(new CommandSlotButton(20, 20, "...") {
			@Override
			public void onPress() {
				Minecraft.getMinecraft().displayGuiScreen(new GuiSelectScoreCriteria(
						Minecraft.getMinecraft().currentScreen, CommandSlotScoreCriteria.this));
			}
		});
	}

	public IScoreCriteria getCriteria() {
		return selectedCriteria;
	}

	public void setCriteria(IScoreCriteria criteria) {
		this.selectedCriteria = criteria;
		criteriaLabel.setText(GuiSelectScoreCriteria.getDisplayName(criteria.getName()));
		criteriaLabel.setColor(0x000000);
	}

	@Override
	public IScoreCriteria getCallbackValue() {
		return getCriteria();
	}

	@Override
	public void setCallbackValue(IScoreCriteria value) {
		setCriteria(value);
	}

	@Override
	public int readFromArgs(String[] args, int index) throws CommandSyntaxException {
		if (args.length == index) {
			throw new CommandSyntaxException();
		}
		IScoreCriteria criteria = IScoreCriteria.INSTANCES.get(args[index]);
		if (criteria == null) {
			throw new CommandSyntaxException();
		}
		setCriteria(criteria);
		return 1;
	}

	@Override
	public void addArgs(List<String> args) throws UIInvalidException {
		checkValid();
		args.add(selectedCriteria.getName());
	}

	public void checkValid() throws UIInvalidException {
		if (selectedCriteria == null) {
			throw new UIInvalidException(TranslateKeys.GUI_COMMANDEDITOR_NOSCORECRITERIASELECTED);
		}
	}

}

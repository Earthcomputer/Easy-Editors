package net.earthcomputer.easyeditors.gui.command;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;

import net.earthcomputer.easyeditors.gui.GuiSelectFromList;
import net.earthcomputer.easyeditors.gui.ICallback;
import net.earthcomputer.easyeditors.util.Translate;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.EnumParticleTypes;

public class GuiSelectParticle extends GuiSelectFromList<EnumParticleTypes> {

	public GuiSelectParticle(GuiScreen prevScreen, ICallback<EnumParticleTypes> callback) {
		super(prevScreen, callback, Lists.newArrayList(EnumParticleTypes.values()),
				Translate.GUI_COMMANDEDITOR_SELECTPARTICLE_TITLE);
	}

	@Override
	protected List<String> getTooltip(EnumParticleTypes value) {
		return Collections.emptyList();
	}

	@Override
	protected void drawSlot(int y, EnumParticleTypes value) {
		String str = I18n.format("gui.commandEditor.particle." + value.getParticleName() + ".name");
		fontRendererObj.drawString(str, width / 2 - fontRendererObj.getStringWidth(str) / 2, y + 2, 0xffffff);
		str = value.getParticleName();
		fontRendererObj.drawString(str, width / 2 - fontRendererObj.getStringWidth(str) / 2,
				y + 4 + fontRendererObj.FONT_HEIGHT, 0xc0c0c0);
	}

	@Override
	protected boolean doesSearchTextMatch(String searchText, EnumParticleTypes value) {
		String localizedName = I18n.format("gui.commandEditor.particle." + value.getParticleName() + ".name");
		if (localizedName.toLowerCase().contains(searchText)) {
			return true;
		}
		if (value.getParticleName().toLowerCase().contains(searchText)) {
			return true;
		}
		return false;
	}

}

package net.earthcomputer.easyeditors.gui.command.slot;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import com.google.common.base.Predicate;
import com.google.common.collect.Maps;

import net.earthcomputer.easyeditors.api.util.Colors;
import net.earthcomputer.easyeditors.api.util.Patterns;
import net.earthcomputer.easyeditors.gui.GuiSelectEntity;
import net.earthcomputer.easyeditors.gui.command.CommandSyntaxException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;

/**
 * A command slot representing a player selector
 * 
 * @author Earthcomputer
 *
 */
public class CommandSlotPlayerSelector extends CommandSlotVerticalArrangement {

	private CommandSlotRadioList radioList;
	private CommandSlotTextField playerNameField;
	private CommandSlotTextField UUIDField;
	private CmdPlayerSelector playerSelector;

	private int flags;
	public static final int DISALLOW_USERNAME = 1;
	public static final int DISALLOW_UUID = 2;
	public static final int DISALLOW_SELECTOR = 4;
	public static final int PLAYERS_ONLY = 8;
	public static final int NON_PLAYERS_ONLY = 16;
	public static final int ONE_ONLY = 32;

	public CommandSlotPlayerSelector() {
		this(0);
	}

	public CommandSlotPlayerSelector(int flags) {
		this.flags = flags;

		addChild(new CommandSlotLabel(Minecraft.getMinecraft().fontRendererObj,
				I18n.format("gui.commandEditor.playerSelector.selectBy"), Colors.playerSelectorSelectBy.color));

		radioList = new CommandSlotRadioList() {
			@Override
			protected int getSelectedIndexForString(String[] args, int index) throws CommandSyntaxException {
				int i = 0;
				if ((CommandSlotPlayerSelector.this.flags & DISALLOW_USERNAME) == 0) {
					if (Patterns.playerName.matcher(args[index]).matches())
						return i;
					i++;
				}
				if ((CommandSlotPlayerSelector.this.flags & DISALLOW_UUID) == 0) {
					if (Patterns.UUID.matcher(args[index]).matches())
						return i;
					i++;
				}
				if ((CommandSlotPlayerSelector.this.flags & DISALLOW_SELECTOR) == 0) {
					if (args[index].startsWith("@"))
						return i;
				}
				throw new CommandSyntaxException();
			}
		};

		if ((flags & DISALLOW_USERNAME) == 0) {
			playerNameField = new CommandSlotTextField(200, 200);
			playerNameField.setContentFilter(new Predicate<String>() {
				@Override
				public boolean apply(String input) {
					return Patterns.partialPlayerName.matcher(input).matches();
				}
			});
			radioList.addChild(CommandSlotLabel.createLabel(I18n.format("gui.commandEditor.playerSelector.username"),
					Colors.playerSelectorLabel.color, playerNameField));
		}

		if ((flags & DISALLOW_UUID) == 0) {
			UUIDField = new CommandSlotTextField(225, 225);
			UUIDField.setText("----");
			UUIDField.setMaxStringLength(48);
			UUIDField.setContentFilter(new Predicate<String>() {
				@Override
				public boolean apply(String input) {
					return Patterns.partialUUID.matcher(input).matches();
				}
			});
			radioList.addChild(CommandSlotLabel.createLabel(I18n.format("gui.commandEditor.playerSelector.uuid"),
					Colors.playerSelectorLabel.color, UUIDField));
		}

		if ((flags & DISALLOW_SELECTOR) == 0) {
			playerSelector = new CmdPlayerSelector();
			radioList.addChild(CommandSlotLabel.createLabel(I18n.format("gui.commandEditor.playerSelector.selector"),
					Colors.playerSelectorLabel.color, playerSelector));
		}

		addChild(radioList);
	}

	/**
	 * 
	 * @return Whether this player selector is valid
	 */
	public boolean isValid() {
		int i = 0;
		int selectedIndex = radioList.getSelectedIndex();
		if ((flags & DISALLOW_USERNAME) == 0) {
			if (selectedIndex == i)
				return !playerNameField.getText().isEmpty();
			i++;
		}
		if ((flags & DISALLOW_UUID) == 0) {
			if (selectedIndex == i)
				return Patterns.UUID.matcher(UUIDField.getText()).matches();
			i++;
		}
		if ((flags & DISALLOW_SELECTOR) == 0) {
			if (selectedIndex == i)
				return playerSelector.isValid();
		}
		return true;
	}

	private class CmdPlayerSelector extends CommandSlotVerticalArrangement {

		private CommandSlotMenu selectorType;
		private CommandSlotCheckbox targetInverted;
		private CommandSlotEntity targetEntity;
		private CommandSlotExpand expand;
		private CommandSlotIntTextField countField;
		private CommandSlotModifiable<IGuiCommandSlot> modifiableCountField;
		private CommandSlotCheckbox nameInverted;
		private CommandSlotTextField entityName;

		public CmdPlayerSelector() {
			CommandSlotHorizontalArrangement row = new CommandSlotHorizontalArrangement();
			row.addChild(selectorType = new CommandSlotMenu(I18n.format("gui.commandEditor.playerSelector.nearest"),
					I18n.format("gui.commandEditor.playerSelector.farthest"),
					I18n.format("gui.commandEditor.playerSelector.all"),
					I18n.format("gui.commandEditor.playerSelector.random")) {
				@Override
				protected void onChanged(String to) {
					if ((flags & ONE_ONLY) == 0) {
						if (getCurrentIndex() == 2)
							modifiableCountField.setChild(null);
						else
							modifiableCountField.setChild(countField);
					}
				}
			});
			if ((flags & PLAYERS_ONLY) == 0) {
				row.addChild(targetInverted = new CommandSlotCheckbox(
						I18n.format("gui.commandEditor.playerSelector.targetInverted")));
				row.addChild(targetEntity = new CommandSlotEntity(true, false, "Anything"));
				if ((flags & NON_PLAYERS_ONLY) == 0)
					targetEntity.setEntity("Player");
				else
					targetEntity.setEntity("Anything");
			} else {
				row.addChild(new CommandSlotLabel(Minecraft.getMinecraft().fontRendererObj,
						GuiSelectEntity.getEntityName("Player"), 0));
			}
			addChild(row);

			CommandSlotVerticalArrangement specifics = new CommandSlotVerticalArrangement();

			if ((flags & ONE_ONLY) == 0) {
				specifics.addChild(
						modifiableCountField = new CommandSlotModifiable<IGuiCommandSlot>(CommandSlotLabel.createLabel(
								I18n.format("gui.commandEditor.playerSelector.count"), Colors.playerSelectorLabel.color,
								countField = new CommandSlotIntTextField(50, 50, 1))));
				countField.setText("1");
			}

			specifics.addChild(new CommandSlotHorizontalArrangement(
					new CommandSlotLabel(Minecraft.getMinecraft().fontRendererObj,
							I18n.format("gui.commandEditor.playerSelector.entityName"),
							Colors.playerSelectorLabel.color),
					nameInverted = new CommandSlotCheckbox(
							I18n.format("gui.commandEditor.playerSelector.entityNameInverted")),
					entityName = new CommandSlotTextField(200, 200)));

			addChild(expand = new CommandSlotExpand(specifics));
		}

		@Override
		public int readFromArgs(String[] args, int index) throws CommandSyntaxException {
			if (index >= args.length)
				throw new CommandSyntaxException();
			Matcher matcher = Patterns.playerSelector.matcher(args[index]);
			if (!matcher.matches())
				throw new CommandSyntaxException();
			String selectorType = matcher.group(1);
			String specifiersString = matcher.group(2);
			Map<String, String> specifiers = Maps.newHashMap();
			// Parse specifiers
			if (specifiersString != null) {
				String[] specifierStrings = specifiersString.split(",");
				int i;
				for (i = 0; i < specifierStrings.length; i++) {
					if (specifierStrings[i].contains("="))
						break;
					String key = null;
					switch (i) {
					case 0:
						key = "x";
						break;
					case 1:
						key = "y";
						break;
					case 2:
						key = "z";
						break;
					case 3:
						key = "r";
						break;
					}
					if (key != null)
						specifiers.put(key, specifierStrings[i]);
				}
				for (; i < specifierStrings.length; i++) {
					int equalsIndex = specifierStrings[i].indexOf('=');
					if (equalsIndex == -1)
						throw new CommandSyntaxException();
					specifiers.put(specifierStrings[i].substring(0, equalsIndex),
							specifierStrings[i].substring(equalsIndex + 1));
				}
			}

			int c = selectorType.equals("p") || selectorType.equals("r") ? 1 : 0;
			if (specifiers.containsKey("c"))
				c = parseInt(specifiers.get("c"));

			if (c == 0) {
				this.selectorType.setCurrentIndex(2);
			} else if (selectorType.equals("r")) {
				this.selectorType.setCurrentIndex(3);
			} else if (c < 0) {
				this.selectorType.setCurrentIndex(1);
			} else {
				this.selectorType.setCurrentIndex(0);
			}

			if (this.targetInverted != null)
				this.targetInverted.setChecked(false);
			String targetType;
			if (selectorType.equals("p") || selectorType.equals("a"))
				targetType = "Player";
			else if (specifiers.containsKey("type")) {
				targetType = specifiers.get("type");
				if (targetType.startsWith("!")) {
					if (this.targetInverted != null)
						this.targetInverted.setChecked(true);
					targetType = targetType.substring(1);
				}
				if (targetType.isEmpty())
					targetType = "Anything";
			} else if (selectorType.equals("r"))
				targetType = "Player";
			else
				targetType = "Anything";
			if (this.targetEntity != null) {
				if ((flags & NON_PLAYERS_ONLY) != 0) {
					if (targetType.equals("Player")) {
						this.targetInverted.setChecked(false);
						targetType = "Anything";
					}
				}
				this.targetEntity.setEntity(targetType);
			}

			this.expand.setExpanded(false);
			if (c < 0)
				c = -c;
			if (c != 0 && (flags & ONE_ONLY) == 0) {
				this.countField.setText(String.valueOf(c));
				if (c != 1)
					this.expand.setExpanded(true);
			}

			this.entityName.setText("");
			this.nameInverted.setChecked(false);
			if (specifiers.containsKey("name")) {
				String name = specifiers.get("name");
				if (name.startsWith("!")) {
					name = name.substring(1);
					this.nameInverted.setChecked(true);
				}
				this.entityName.setText(name);
				this.expand.setExpanded(true);
			}

			return 1;
		}

		private int parseInt(String string) throws CommandSyntaxException {
			try {
				return Integer.parseInt(string);
			} catch (NumberFormatException e) {
				throw new CommandSyntaxException();
			}
		}

		@Override
		public void addArgs(List<String> args) {
			String selectorType;
			Map<String, String> specifiers = Maps.newHashMap();

			// Decide p/a/r/e
			if (this.selectorType.getCurrentIndex() == 3) {
				selectorType = "r";
			} else if (this.targetEntity != null
					&& (!this.targetEntity.getEntity().equals("Player") || this.targetInverted.isChecked())) {
				selectorType = "e";
			} else if (this.selectorType.getCurrentIndex() == 2 && (flags & ONE_ONLY) == 0) {
				selectorType = "a";
			} else {
				selectorType = "p";
			}

			// c
			int arg;
			if ((flags & ONE_ONLY) != 0)
				arg = 1;
			else if (this.selectorType.getCurrentIndex() == 2)
				arg = 0;
			else
				arg = this.countField.getIntValue();
			if (this.selectorType.getCurrentIndex() == 1)
				arg = -arg;
			if (((selectorType.equals("p") || selectorType.equals("r") || (flags & ONE_ONLY) != 0) && arg != 1)
					|| ((selectorType.equals("a") || selectorType.equals("e")) && arg != 0))
				specifiers.put("c", String.valueOf(arg));

			// type
			if (this.targetEntity != null
					&& (!this.targetEntity.getEntity().equals("Player") || this.targetInverted.isChecked())) {
				String targetType = targetEntity.getEntity();
				if (targetType.equals("Anything"))
					targetType = "";
				if (this.targetInverted.isChecked())
					targetType = "!" + targetType;
				if (!selectorType.equals("e") || !targetType.isEmpty()) {
					specifiers.put("type", targetType);
				}
			}

			// name
			if (!this.entityName.getText().isEmpty()) {
				String name = this.entityName.getText();
				if (this.nameInverted.isChecked())
					name = "!" + name;
				specifiers.put("name", name);
			}

			// Build final string
			StringBuilder builder = new StringBuilder("@").append(selectorType);
			if (!specifiers.isEmpty()) {
				builder.append("[");
				boolean appendCommaBefore = false;
				String[] strs = new String[] { "x", "y", "z", "r" };
				for (String str : strs) {
					if (!specifiers.containsKey(str))
						break;
					if (appendCommaBefore)
						builder.append(",");
					builder.append(specifiers.remove(str));
					appendCommaBefore = true;
				}
				for (Map.Entry<String, String> specifier : specifiers.entrySet()) {
					if (appendCommaBefore)
						builder.append(",");
					builder.append(specifier.getKey()).append("=").append(specifier.getValue());
					appendCommaBefore = true;
				}
				builder.append("]");
			}
			args.add(builder.toString());
		}

		public boolean isValid() {
			if (targetEntity != null) {
				if (!targetEntity.isValid())
					return false;
				else if (targetEntity.getEntity().equals("Anything")) {
					if (selectorType.getCurrentIndex() == 3)
						return false;
					else if (targetInverted.isChecked())
						return false;
				}
			}
			if (selectorType.getCurrentIndex() != 2 && (flags & ONE_ONLY) == 0 && !countField.isValid())
				return false;
			return true;
		}

	}

}

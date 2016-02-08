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
		private CommandSlotRadioList positionalConstraints;
		private CommandSlotIntTextField rOriginX;
		private CommandSlotIntTextField rOriginY;
		private CommandSlotIntTextField rOriginZ;
		private CommandSlotIntTextField minRadius;
		private CommandSlotIntTextField maxRadius;
		private CommandSlotIntTextField boundsX1;
		private CommandSlotIntTextField boundsY1;
		private CommandSlotIntTextField boundsZ1;
		private CommandSlotIntTextField boundsX2;
		private CommandSlotIntTextField boundsY2;
		private CommandSlotIntTextField boundsZ2;
		private CommandSlotIntTextField dOriginX;
		private CommandSlotIntTextField dOriginY;
		private CommandSlotIntTextField dOriginZ;
		private CommandSlotIntTextField distX;
		private CommandSlotIntTextField distY;
		private CommandSlotIntTextField distZ;

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
			entityName.setContentFilter(new Predicate<String>() {
				@Override
				public boolean apply(String input) {
					return Patterns.partialPlayerName.matcher(input).matches();
				}
			});

			specifics.addChild(new CommandSlotLabel(Minecraft.getMinecraft().fontRendererObj,
					I18n.format("gui.commandEditor.playerSelector.positionalConstraints"),
					Colors.playerSelectorLabel.color));
			positionalConstraints = new CommandSlotRadioList() {
				@Override
				protected int getSelectedIndexForString(String[] args, int index) {
					return 0;
				}
			};
			CommandSlotVerticalArrangement posConstraint = new CommandSlotVerticalArrangement();
			row = new CommandSlotHorizontalArrangement();
			row.addChild(new CommandSlotLabel(Minecraft.getMinecraft().fontRendererObj,
					I18n.format("gui.commandEditor.playerSelector.radius.origin"), Colors.playerSelectorLabel.color));
			row.addChild(new CommandSlotLabel(Minecraft.getMinecraft().fontRendererObj, "X",
					Colors.playerSelectorLabel.color));
			row.addChild(rOriginX = new CommandSlotIntTextField(30, 100));
			row.addChild(new CommandSlotLabel(Minecraft.getMinecraft().fontRendererObj, "Y",
					Colors.playerSelectorLabel.color));
			row.addChild(rOriginY = new CommandSlotIntTextField(30, 100));
			row.addChild(new CommandSlotLabel(Minecraft.getMinecraft().fontRendererObj, "Z",
					Colors.playerSelectorLabel.color));
			row.addChild(rOriginZ = new CommandSlotIntTextField(30, 100));
			posConstraint.addChild(row);
			posConstraint
					.addChild(CommandSlotLabel.createLabel(I18n.format("gui.commandEditor.playerSelector.radius.min"),
							Colors.playerSelectorLabel.color, minRadius = new CommandSlotIntTextField(30, 100, 0)));
			posConstraint
					.addChild(CommandSlotLabel.createLabel(I18n.format("gui.commandEditor.playerSelector.radius.max"),
							Colors.playerSelectorLabel.color, maxRadius = new CommandSlotIntTextField(30, 100, 0)));
			positionalConstraints
					.addChild(CommandSlotLabel.createLabel(I18n.format("gui.commandEditor.playerSelector.radius"),
							Colors.playerSelectorLabel.color, posConstraint));
			posConstraint = new CommandSlotVerticalArrangement();
			row = new CommandSlotHorizontalArrangement();
			row.addChild(new CommandSlotLabel(Minecraft.getMinecraft().fontRendererObj,
					I18n.format("gui.commandEditor.playerSelector.boundsFromTo.from"),
					Colors.playerSelectorLabel.color));
			row.addChild(new CommandSlotLabel(Minecraft.getMinecraft().fontRendererObj, "X",
					Colors.playerSelectorLabel.color));
			row.addChild(boundsX1 = new CommandSlotIntTextField(30, 100));
			row.addChild(new CommandSlotLabel(Minecraft.getMinecraft().fontRendererObj, "Y",
					Colors.playerSelectorLabel.color));
			row.addChild(boundsY1 = new CommandSlotIntTextField(30, 100));
			row.addChild(new CommandSlotLabel(Minecraft.getMinecraft().fontRendererObj, "Z",
					Colors.playerSelectorLabel.color));
			row.addChild(boundsZ1 = new CommandSlotIntTextField(30, 100));
			posConstraint.addChild(row);
			row = new CommandSlotHorizontalArrangement();
			row.addChild(new CommandSlotLabel(Minecraft.getMinecraft().fontRendererObj,
					I18n.format("gui.commandEditor.playerSelector.boundsFromTo.to"), Colors.playerSelectorLabel.color));
			row.addChild(new CommandSlotLabel(Minecraft.getMinecraft().fontRendererObj, "X",
					Colors.playerSelectorLabel.color));
			row.addChild(boundsX2 = new CommandSlotIntTextField(30, 100));
			row.addChild(new CommandSlotLabel(Minecraft.getMinecraft().fontRendererObj, "Y",
					Colors.playerSelectorLabel.color));
			row.addChild(boundsY2 = new CommandSlotIntTextField(30, 100));
			row.addChild(new CommandSlotLabel(Minecraft.getMinecraft().fontRendererObj, "Z",
					Colors.playerSelectorLabel.color));
			row.addChild(boundsZ2 = new CommandSlotIntTextField(30, 100));
			posConstraint.addChild(row);
			positionalConstraints
					.addChild(CommandSlotLabel.createLabel(I18n.format("gui.commandEditor.playerSelector.boundsFromTo"),
							Colors.playerSelectorLabel.color, posConstraint));
			posConstraint = new CommandSlotVerticalArrangement();
			row = new CommandSlotHorizontalArrangement();
			row.addChild(new CommandSlotLabel(Minecraft.getMinecraft().fontRendererObj,
					I18n.format("gui.commandEditor.playerSelector.boundsDist.origin"),
					Colors.playerSelectorLabel.color));
			row.addChild(new CommandSlotLabel(Minecraft.getMinecraft().fontRendererObj, "X",
					Colors.playerSelectorLabel.color));
			row.addChild(dOriginX = new CommandSlotIntTextField(30, 100));
			row.addChild(new CommandSlotLabel(Minecraft.getMinecraft().fontRendererObj, "Y",
					Colors.playerSelectorLabel.color));
			row.addChild(dOriginY = new CommandSlotIntTextField(30, 100));
			row.addChild(new CommandSlotLabel(Minecraft.getMinecraft().fontRendererObj, "Z",
					Colors.playerSelectorLabel.color));
			row.addChild(dOriginZ = new CommandSlotIntTextField(30, 100));
			posConstraint.addChild(row);
			row = new CommandSlotHorizontalArrangement();
			row.addChild(new CommandSlotLabel(Minecraft.getMinecraft().fontRendererObj,
					I18n.format("gui.commandEditor.playerSelector.boundsDist.distance"),
					Colors.playerSelectorLabel.color));
			row.addChild(new CommandSlotLabel(Minecraft.getMinecraft().fontRendererObj, "X",
					Colors.playerSelectorLabel.color));
			row.addChild(distX = new CommandSlotIntTextField(30, 100));
			row.addChild(new CommandSlotLabel(Minecraft.getMinecraft().fontRendererObj, "Y",
					Colors.playerSelectorLabel.color));
			row.addChild(distY = new CommandSlotIntTextField(30, 100));
			row.addChild(new CommandSlotLabel(Minecraft.getMinecraft().fontRendererObj, "Z",
					Colors.playerSelectorLabel.color));
			row.addChild(distZ = new CommandSlotIntTextField(30, 100));
			posConstraint.addChild(row);
			positionalConstraints
					.addChild(CommandSlotLabel.createLabel(I18n.format("gui.commandEditor.playerSelector.boundsDist"),
							Colors.playerSelectorLabel.color, posConstraint));
			specifics.addChild(new CommandSlotRectangle(positionalConstraints, Colors.playerSelectorBox.color));

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

			this.rOriginX.setText("");
			this.rOriginY.setText("");
			this.rOriginZ.setText("");
			this.minRadius.setText("");
			this.maxRadius.setText("");
			this.boundsX1.setText("");
			this.boundsY1.setText("");
			this.boundsZ1.setText("");
			this.boundsX2.setText("");
			this.boundsY2.setText("");
			this.boundsZ2.setText("");
			this.dOriginX.setText("");
			this.dOriginY.setText("");
			this.dOriginZ.setText("");
			this.distX.setText("");
			this.distY.setText("");
			this.distZ.setText("");
			if (specifiers.containsKey("x") && specifiers.containsKey("y") && specifiers.containsKey("z")
					&& specifiers.containsKey("dx") && specifiers.containsKey("dy") && specifiers.containsKey("dz")) {
				this.positionalConstraints.setSelectedIndex(1);
				int x = parseInt(specifiers.get("x"));
				int y = parseInt(specifiers.get("y"));
				int z = parseInt(specifiers.get("z"));
				int dx = parseInt(specifiers.get("dx"));
				int dy = parseInt(specifiers.get("dy"));
				int dz = parseInt(specifiers.get("dz"));
				int x1 = dx < 0 ? x + dx : x + dx + 1;
				int y1 = dy < 0 ? y + dy : y + dy + 1;
				int z1 = dz < 0 ? z + dz : z + dz + 1;
				this.boundsX1.setText(String.valueOf(x));
				this.boundsY1.setText(String.valueOf(y));
				this.boundsZ1.setText(String.valueOf(z));
				this.boundsX2.setText(String.valueOf(x1));
				this.boundsY2.setText(String.valueOf(y1));
				this.boundsZ2.setText(String.valueOf(z1));
			} else if (specifiers.containsKey("dx") || specifiers.containsKey("dy") || specifiers.containsKey("dz")) {
				this.positionalConstraints.setSelectedIndex(2);
				if (specifiers.containsKey("x"))
					this.dOriginX.setText(String.valueOf(parseInt(specifiers.get("x"))));
				if (specifiers.containsKey("y"))
					this.dOriginY.setText(String.valueOf(parseInt(specifiers.get("y"))));
				if (specifiers.containsKey("z"))
					this.dOriginZ.setText(String.valueOf(parseInt(specifiers.get("z"))));
				if (specifiers.containsKey("dx"))
					this.distX.setText(String.valueOf(parseInt(specifiers.get("dx"))));
				if (specifiers.containsKey("dy"))
					this.distY.setText(String.valueOf(parseInt(specifiers.get("dy"))));
				if (specifiers.containsKey("dz"))
					this.distZ.setText(String.valueOf(parseInt(specifiers.get("dz"))));
			} else {
				this.positionalConstraints.setSelectedIndex(0);
				if (specifiers.containsKey("x"))
					this.rOriginX.setText(String.valueOf(parseInt(specifiers.get("x"))));
				if (specifiers.containsKey("y"))
					this.rOriginY.setText(String.valueOf(parseInt(specifiers.get("y"))));
				if (specifiers.containsKey("z"))
					this.rOriginZ.setText(String.valueOf(parseInt(specifiers.get("z"))));
				if (specifiers.containsKey("rm"))
					this.minRadius.setText(String.valueOf(parseInt(specifiers.get("rm"))));
				if (specifiers.containsKey("r"))
					this.maxRadius.setText(String.valueOf(parseInt(specifiers.get("r"))));
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

			// x/y/z/dx/dy/dz/r/rm
			switch (this.positionalConstraints.getSelectedIndex()) {
			case 0:
				if (!this.rOriginX.getText().isEmpty())
					specifiers.put("x", this.rOriginX.getText());
				if (!this.rOriginY.getText().isEmpty())
					specifiers.put("y", this.rOriginY.getText());
				if (!this.rOriginZ.getText().isEmpty())
					specifiers.put("z", this.rOriginZ.getText());
				if (!this.minRadius.getText().isEmpty() && this.minRadius.getIntValue() != 0)
					specifiers.put("rm", this.minRadius.getText());
				if (!this.maxRadius.getText().isEmpty())
					specifiers.put("r", this.maxRadius.getText());
				break;
			case 1:
				int x1 = this.boundsX1.getIntValue();
				int y1 = this.boundsY1.getIntValue();
				int z1 = this.boundsZ1.getIntValue();
				int x2 = this.boundsX2.getIntValue();
				int y2 = this.boundsY2.getIntValue();
				int z2 = this.boundsZ2.getIntValue();
				int x = x1 < x2 ? x1 : x2;
				int y = y1 < y2 ? y1 : y2;
				int z = z1 < z2 ? z1 : z2;
				int dx = x1 < x2 ? x2 - x1 - 1 : x1 - x2 - 1;
				int dy = y1 < y2 ? y2 - y1 - 1 : y1 - y2 - 1;
				int dz = z1 < z2 ? z2 - z1 - 1 : z1 - z2 - 1;
				specifiers.put("x", String.valueOf(x));
				specifiers.put("y", String.valueOf(y));
				specifiers.put("z", String.valueOf(z));
				specifiers.put("dx", String.valueOf(dx));
				specifiers.put("dy", String.valueOf(dy));
				specifiers.put("dz", String.valueOf(dz));
				break;
			case 2:
				if (!this.dOriginX.getText().isEmpty())
					specifiers.put("x", this.dOriginX.getText());
				if (!this.dOriginY.getText().isEmpty())
					specifiers.put("y", this.dOriginY.getText());
				if (!this.dOriginZ.getText().isEmpty())
					specifiers.put("z", this.dOriginZ.getText());
				if (!this.distX.getText().isEmpty())
					specifiers.put("dx", this.distX.getText());
				if (!this.distY.getText().isEmpty())
					specifiers.put("dy", this.distY.getText());
				if (!this.distZ.getText().isEmpty())
					specifiers.put("dz", this.distZ.getText());
				break;
			default:
				throw new IllegalStateException();
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

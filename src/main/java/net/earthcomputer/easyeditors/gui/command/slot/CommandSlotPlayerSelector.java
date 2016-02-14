package net.earthcomputer.easyeditors.gui.command.slot;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.earthcomputer.easyeditors.api.util.ChatBlocker;
import net.earthcomputer.easyeditors.api.util.Colors;
import net.earthcomputer.easyeditors.api.util.Instantiator;
import net.earthcomputer.easyeditors.api.util.Patterns;
import net.earthcomputer.easyeditors.api.util.ReturnedValueListener;
import net.earthcomputer.easyeditors.gui.GuiSelectEntity;
import net.earthcomputer.easyeditors.gui.command.CommandSyntaxException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.world.WorldSettings.GameType;

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
		private CommandSlotIntTextField minExp;
		private CommandSlotIntTextField maxExp;
		private CommandSlotMenu gamemode;
		private IGuiCommandSlot playersOnlySlots;
		private CommandSlotModifiable<IGuiCommandSlot> modifiablePlayersOnlySlots;
		private String waitingTeamName;
		private IGuiCommandSlot team;
		private CommandSlotModifiable<IGuiCommandSlot> modifiableTeam;
		private CommandSlotCheckbox teamInverted;
		private IGuiCommandSlot teamName;
		private CommandSlotModifiable<IGuiCommandSlot> modifiableTeamName;
		private List<ScoreObjective> objectivesList;
		private String objectiveErrorMessage;
		private CommandSlotList<CmdScoreTest> scoreTests;

		public CmdPlayerSelector() {
			modifiableTeam = new CommandSlotModifiable<IGuiCommandSlot>(team = CommandSlotLabel.createLabel(
					I18n.format("gui.commandEditor.playerSelector.team"), Colors.playerSelectorLabel.color,
					teamInverted = new CommandSlotCheckbox(
							I18n.format("gui.commandEditor.playerSelector.team.inverted")),
					modifiableTeamName = new CommandSlotModifiable<IGuiCommandSlot>(
							teamName = new CommandSlotLabel(Minecraft.getMinecraft().fontRendererObj,
									I18n.format("gui.commandEditor.playerSelector.team.waiting"), 0x404040))));
			ChatBlocker.obtainTeamsList(new ReturnedValueListener<List<ScorePlayerTeam>>() {
				@Override
				public void returnValue(List<ScorePlayerTeam> value) {
					List<String> registeredNames = Lists.newArrayList();
					registeredNames.add("any");
					registeredNames.add("none");
					List<String> displayNames = Lists.newArrayList();
					displayNames.add(I18n.format("gui.commandEditor.playerSelector.team.any"));
					displayNames.add(I18n.format("gui.commandEditor.playerSelector.team.none"));
					for (ScorePlayerTeam team : value) {
						registeredNames.add(team.getRegisteredName());
						displayNames.add(team.getTeamName());
					}
					CommandSlotMenu teamMenu;
					modifiableTeamName.setChild(teamName = teamMenu = new CommandSlotMenu(
							displayNames.toArray(new String[displayNames.size()]),
							registeredNames.toArray(new String[registeredNames.size()])));
					if ("".equals(waitingTeamName))
						teamMenu.setCurrentIndex(1);
					else if (waitingTeamName != null) {
						for (int i = 2; i < value.size(); i++)
							if (value.get(i).equals(waitingTeamName))
								teamMenu.setCurrentIndex(i);
					}
				}

				@Override
				public void abortFindingValue(int reason) {
					modifiableTeamName
							.setChild(teamName = new CommandSlotLabel(Minecraft.getMinecraft().fontRendererObj,
									I18n.format(reason == 0 ? "gui.commandEditor.playerSelector.team.timedOut"
											: "gui.commandEditor.playerSelector.team.noPermission"),
									0xff0000));
				}
			});

			scoreTests = new CommandSlotList<CmdScoreTest>(new Instantiator<CmdScoreTest>() {
				@Override
				public CmdScoreTest newInstance() {
					CmdScoreTest r = new CmdScoreTest();
					if (objectivesList != null) {
						String[] names = new String[objectivesList.size()];
						String[] displayNames = new String[names.length];
						for (int i = 0; i < names.length; i++) {
							names[i] = objectivesList.get(i).getName();
							displayNames[i] = objectivesList.get(i).getDisplayName();
						}
						r.modifiableObjective.setChild(r.objective = new CommandSlotMenu(displayNames, names));
					} else if (objectiveErrorMessage != null) {
						r.modifiableObjective.setChild(new CommandSlotLabel(Minecraft.getMinecraft().fontRendererObj,
								objectiveErrorMessage, 0xff0000));
					}
					return r;
				}
			}).setAppendHoverText(I18n.format("gui.commandEditor.playerSelector.score.append"))
					.setInsertHoverText(I18n.format("gui.commandEditor.playerSelector.score.insert"))
					.setRemoveHoverText(I18n.format("gui.commandEditor.playerSelector.score.remove"));
			ChatBlocker.obtainObjectiveList(new ReturnedValueListener<List<ScoreObjective>>() {

				@Override
				public void returnValue(List<ScoreObjective> value) {
					objectivesList = value;
					String[] names = new String[value.size()];
					String[] displayNames = new String[names.length];
					for (int i = 0; i < names.length; i++) {
						names[i] = value.get(i).getName();
						displayNames[i] = value.get(i).getDisplayName();
					}
					for (int i = 0; i < scoreTests.entryCount(); i++) {
						CmdScoreTest entry = scoreTests.getEntry(i);
						entry.modifiableObjective.setChild(entry.objective = new CommandSlotMenu(displayNames, names));
					}
				}

				@Override
				public void abortFindingValue(int reason) {
					objectiveErrorMessage = reason == 0 ? "gui.commandEditor.playerSelector.score.timedOut"
							: "gui.commandEditor.playerSelector.score.noPermission";
					for (int i = 0; i < scoreTests.entryCount(); i++) {
						scoreTests.getEntry(i).modifiableObjective.setChild(new CommandSlotLabel(
								Minecraft.getMinecraft().fontRendererObj, objectiveErrorMessage, 0xff0000));
					}
				}

			});

			modifiablePlayersOnlySlots = new CommandSlotModifiable<IGuiCommandSlot>(null);

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
				row.addChild(targetEntity = new CommandSlotEntity((flags & NON_PLAYERS_ONLY) == 0, false, "Anything") {
					@Override
					public void setEntity(String entityName) {
						super.setEntity(entityName);
						if ("Player".equals(entityName)) {
							modifiablePlayersOnlySlots.setChild(playersOnlySlots);
						} else {
							modifiablePlayersOnlySlots.setChild(null);
						}
						Class<? extends Entity> entityClass = EntityList.stringToClassMapping.get(entityName);
						if (entityClass == null || EntityLivingBase.class.isAssignableFrom(entityClass)) {
							modifiableTeam.setChild(team);
						} else {
							modifiableTeam.setChild(null);
						}
					}
				});
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

			CommandSlotVerticalArrangement playersOnlySlots = new CommandSlotVerticalArrangement();
			this.playersOnlySlots = playersOnlySlots;
			playersOnlySlots.addChild(new CommandSlotLabel(Minecraft.getMinecraft().fontRendererObj,
					I18n.format("gui.commandEditor.playerSelector.exp"), Colors.playerSelectorLabel.color));
			CommandSlotVerticalArrangement expThings = new CommandSlotVerticalArrangement();
			expThings.addChild(CommandSlotLabel.createLabel(I18n.format("gui.commandEditor.playerSelector.exp.min"),
					Colors.playerSelectorLabel.color, minExp = new CommandSlotIntTextField(30, 100, 0)));
			expThings.addChild(CommandSlotLabel.createLabel(I18n.format("gui.commandEditor.playerSelector.exp.max"),
					Colors.playerSelectorLabel.color, maxExp = new CommandSlotIntTextField(30, 100, 0)));
			playersOnlySlots.addChild(new CommandSlotRectangle(expThings, Colors.playerSelectorLabel.color));
			GameType[] gamemodes = GameType.values();
			String[] gamemodeNames = new String[gamemodes.length];
			String[] gamemodeIds = new String[gamemodeNames.length];
			for (int i = 0; i < gamemodes.length; i++) {
				if (gamemodes[i] == GameType.NOT_SET) {
					gamemodeNames[i] = I18n.format("gui.commandEditor.playerSelector.gamemode.any");
					gamemodeIds[i] = "-1";
				} else {
					gamemodeNames[i] = I18n.format("gameMode." + gamemodes[i].getName());
					gamemodeIds[i] = String.valueOf(gamemodes[i].getID());
				}
			}
			playersOnlySlots.addChild(CommandSlotLabel.createLabel(
					I18n.format("gui.commandEditor.playerSelector.gamemode"), Colors.playerSelectorLabel.color,
					gamemode = new CommandSlotMenu(gamemodeNames, gamemodeIds)));
			if ((flags & NON_PLAYERS_ONLY) == 0)
				modifiablePlayersOnlySlots.setChild(playersOnlySlots);
			specifics.addChild(modifiablePlayersOnlySlots);

			specifics.addChild(modifiableTeam);

			specifics.addChild(CommandSlotLabel.createLabel(I18n.format("gui.commandEditor.playerSelector.score"),
					Colors.playerSelectorLabel.color, scoreTests));

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

			this.minExp.setText("");
			this.maxExp.setText("");
			if (specifiers.containsKey("lm"))
				this.minExp.setText(String.valueOf(Integer.parseInt(specifiers.get("lm"))));
			if (specifiers.containsKey("l"))
				this.maxExp.setText(String.valueOf(Integer.parseInt(specifiers.get("l"))));

			this.gamemode.setCurrentIndex(0);
			if (specifiers.containsKey("m")) {
				String gamemode = specifiers.get("m");
				for (int i = 0; i < this.gamemode.wordCount(); i++) {
					if (this.gamemode.getValueAt(i).equals(gamemode)) {
						this.gamemode.setCurrentIndex(i);
						break;
					}
				}
			}

			this.waitingTeamName = null;
			this.teamInverted.setChecked(false);
			if (specifiers.containsKey("team")) {
				String teamName = specifiers.get("team");
				if (teamName.startsWith("!")) {
					teamName = teamName.substring(1);
					this.teamInverted.setChecked(true);
				}
				this.waitingTeamName = teamName;
				if (this.teamName instanceof CommandSlotMenu && this.modifiableTeam.getChild() == this.team) {
					CommandSlotMenu teamMenu = (CommandSlotMenu) this.teamName;
					teamMenu.setCurrentIndex(0);
					if ("".equals(this.waitingTeamName))
						teamMenu.setCurrentIndex(1);
					else {
						for (int i = 2; i < teamMenu.wordCount(); i++) {
							if (teamMenu.getValueAt(i).equals(waitingTeamName)) {
								teamMenu.setCurrentIndex(i);
								break;
							}
						}
					}
				}
			}

			this.scoreTests.clearEntries();
			for (Map.Entry<String, String> specifier : specifiers.entrySet()) {
				if (specifier.getKey().startsWith("score_") && specifier.getKey().length() > 6) {
					String objective = specifier.getKey().substring(6);
					boolean min = false;
					if (objective.endsWith("_min")) {
						objective = objective.substring(objective.length() - 4, objective.length());
						min = true;
					}

					CmdScoreTest scoreTest = this.scoreTests.newEntry();
					scoreTest.waitingObjective = objective;
					scoreTest.minOrMax.setCurrentIndex(min ? 0 : 1);
					scoreTest.value.setText(String.valueOf(parseInt(specifier.getValue())));
					this.scoreTests.addEntry(scoreTest);
				}
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
					specifiers.put("x", String.valueOf(this.rOriginX.getIntValue()));
				if (!this.rOriginY.getText().isEmpty())
					specifiers.put("y", String.valueOf(this.rOriginY.getIntValue()));
				if (!this.rOriginZ.getText().isEmpty())
					specifiers.put("z", String.valueOf(this.rOriginZ.getIntValue()));
				if (!this.minRadius.getText().isEmpty() && this.minRadius.getIntValue() != 0)
					specifiers.put("rm", String.valueOf(this.minRadius.getIntValue()));
				if (!this.maxRadius.getText().isEmpty())
					specifiers.put("r", String.valueOf(this.maxRadius.getIntValue()));
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
					specifiers.put("x", String.valueOf(this.dOriginX.getIntValue()));
				if (!this.dOriginY.getText().isEmpty())
					specifiers.put("y", String.valueOf(this.dOriginY.getIntValue()));
				if (!this.dOriginZ.getText().isEmpty())
					specifiers.put("z", String.valueOf(this.dOriginZ.getIntValue()));
				if (!this.distX.getText().isEmpty())
					specifiers.put("dx", String.valueOf(this.distX.getIntValue()));
				if (!this.distY.getText().isEmpty())
					specifiers.put("dy", String.valueOf(this.distY.getIntValue()));
				if (!this.distZ.getText().isEmpty())
					specifiers.put("dz", String.valueOf(this.distZ.getIntValue()));
				break;
			default:
				throw new IllegalStateException();
			}

			if (this.modifiablePlayersOnlySlots.getChild() == this.playersOnlySlots) {
				// lm/l
				if (!this.minExp.getText().isEmpty() && this.minExp.getIntValue() != 0)
					specifiers.put("lm", String.valueOf(this.minExp.getIntValue()));
				if (!this.maxExp.getText().isEmpty())
					specifiers.put("l", String.valueOf(this.maxExp.getIntValue()));

				// m
				if (this.gamemode.getCurrentIndex() != 0)
					specifiers.put("m", this.gamemode.getCurrentValue());
			}

			// team
			if (this.teamName instanceof CommandSlotMenu && this.modifiableTeam.getChild() == this.team) {
				CommandSlotMenu teamMenu = (CommandSlotMenu) this.teamName;
				if (teamMenu.getCurrentIndex() != 0) {
					String team;
					if (teamMenu.getCurrentIndex() == 1)
						team = "";
					else
						team = teamMenu.getCurrentValue();
					if (this.teamInverted.isChecked())
						team = "!" + team;
					specifiers.put("team", team);
				}
			} else if (this.waitingTeamName != null) {
				String team = this.waitingTeamName;
				if (this.teamInverted.isChecked())
					team = "!" + team;
				specifiers.put("team", team);
			}

			// scores
			for (int i = 0; i < this.scoreTests.entryCount(); i++) {
				CmdScoreTest scoreTest = this.scoreTests.getEntry(i);
				String key = scoreTest.objective == null ? scoreTest.waitingObjective
						: scoreTest.objective.getCurrentValue();
				key = "score_" + key;
				if (scoreTest.minOrMax.getCurrentIndex() == 0)
					key += "_min";
				specifiers.put(key, String.valueOf(scoreTest.value.getIntValue()));
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
			switch (positionalConstraints.getSelectedIndex()) {
			case 0:
				if (!rOriginX.getText().isEmpty() && !rOriginX.isValid())
					return false;
				if (!rOriginY.getText().isEmpty() && !rOriginY.isValid())
					return false;
				if (!rOriginZ.getText().isEmpty() && !rOriginZ.isValid())
					return false;
				if (!minRadius.getText().isEmpty() && !minRadius.isValid())
					return false;
				if (!maxRadius.getText().isEmpty() && !maxRadius.isValid())
					return false;
				break;
			case 1:
				if (!boundsX1.isValid() || !boundsY1.isValid() || !boundsZ1.isValid() || !boundsX2.isValid()
						|| !boundsY2.isValid() || !boundsZ2.isValid())
					return false;
				break;
			case 2:
				if (!dOriginX.getText().isEmpty() && !dOriginX.isValid())
					return false;
				if (!dOriginY.getText().isEmpty() && !dOriginY.isValid())
					return false;
				if (!dOriginZ.getText().isEmpty() && !dOriginZ.isValid())
					return false;
				if (!distX.getText().isEmpty() && !distX.isValid())
					return false;
				if (!distY.getText().isEmpty() && !distY.isValid())
					return false;
				if (!distZ.getText().isEmpty() && !distZ.isValid())
					return false;
				break;
			}

			if (modifiablePlayersOnlySlots.getChild() == playersOnlySlots) {
				if (!minExp.getText().isEmpty() && !minExp.isValid())
					return false;
				if (!maxExp.getText().isEmpty() && !maxExp.isValid())
					return false;
			}

			if (modifiableTeam.getChild() != null && modifiableTeamName.getChild() == teamName
					&& teamName instanceof CommandSlotMenu) {
				if (((CommandSlotMenu) teamName).getCurrentIndex() == 0 && teamInverted.isChecked())
					return false;
			}
			for (int i = 0; i < scoreTests.entryCount(); i++) {
				if (!scoreTests.getEntry(i).isValid())
					return false;
			}
			return true;
		}

		private class CmdScoreTest extends CommandSlotHorizontalArrangement {

			String waitingObjective;
			CommandSlotModifiable<IGuiCommandSlot> modifiableObjective;
			CommandSlotMenu objective;
			CommandSlotMenu minOrMax;
			CommandSlotIntTextField value;

			public CmdScoreTest() {
				addChild(modifiableObjective = new CommandSlotModifiable<IGuiCommandSlot>(
						new CommandSlotLabel(Minecraft.getMinecraft().fontRendererObj,
								I18n.format("gui.commandEditor.playerSelector.score.waiting"), 0x404040)));
				addChild(minOrMax = new CommandSlotMenu(I18n.format("gui.commandEditor.playerSelector.score.min"),
						I18n.format("gui.commandEditor.playerSelector.score.max")));
				addChild(value = new CommandSlotIntTextField(30, 100));
				value.setText("0");
			}

			public boolean isValid() {
				if (objective == null) {
					if (waitingObjective == null)
						return false;
				} else if (objective.wordCount() == 0) {
					return false;
				}
				if (!value.isValid())
					return false;
				return true;
			}

		}

	}

}

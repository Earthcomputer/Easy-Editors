package net.earthcomputer.easyeditors.gui.command.syntax;

import java.util.List;

import com.google.common.base.Supplier;
import com.google.common.collect.Lists;

import net.earthcomputer.easyeditors.api.util.Colors;
import net.earthcomputer.easyeditors.api.util.GeneralUtils;
import net.earthcomputer.easyeditors.gui.command.CommandSyntaxException;
import net.earthcomputer.easyeditors.gui.command.UIInvalidException;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotBox;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotCheckbox;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotCycleButton;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotEntityNBT;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotFormattedTextField;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotIntTextField;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotLabel;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotList;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotMenu;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotModifiable;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotOptional;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotPlayerSelector;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotRadioList;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotRectangle;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotScore;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotScoreCriteria;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotTeam;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotTextField;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotVerticalArrangement;
import net.earthcomputer.easyeditors.gui.command.slot.IGuiCommandSlot;
import net.earthcomputer.easyeditors.gui.command.slot.ITextField;
import net.earthcomputer.easyeditors.util.Translate;
import net.minecraft.client.resources.I18n;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;

public class SyntaxScoreboard extends CommandSyntax {

	private CommandSlotMenu subCommand;
	private CommandSlotModifiable subCommandArgs;

	private IGuiCommandSlot subCommandObjectives;
	private CommandSlotMenu objectivesSubCommand;
	private CommandSlotModifiable objectivesSubCommandArgs;
	private IGuiCommandSlot argsObjectivesList;
	private IGuiCommandSlot argsObjectivesAdd;
	private IGuiCommandSlot argsObjectivesRemove;
	private IGuiCommandSlot argsObjectivesSetdisplay;

	private IGuiCommandSlot subCommandPlayers;
	private CommandSlotMenu playersSubCommand;
	private CommandSlotModifiable playersSubCommandArgs;
	private IGuiCommandSlot argsPlayersList;
	private IGuiCommandSlot argsPlayersAdd;
	private IGuiCommandSlot argsPlayersRemove;
	private IGuiCommandSlot argsPlayersSet;
	private IGuiCommandSlot argsPlayersReset;
	private IGuiCommandSlot argsPlayersEnable;
	private IGuiCommandSlot argsPlayersTest;
	private IGuiCommandSlot argsPlayersOperation;
	private IGuiCommandSlot argsPlayersTag;

	private IGuiCommandSlot subCommandTeams;
	private CommandSlotMenu teamsSubCommand;
	private CommandSlotModifiable teamsSubCommandArgs;
	private IGuiCommandSlot argsTeamsList;
	private IGuiCommandSlot argsTeamsAdd;
	private IGuiCommandSlot argsTeamsRemove;
	private IGuiCommandSlot argsTeamsEmpty;
	private IGuiCommandSlot argsTeamsJoin;
	private IGuiCommandSlot argsTeamsLeave;
	private IGuiCommandSlot argsTeamsOption;

	private CommandSlotScore objective1;
	private CommandSlotScore objective2;
	private CommandSlotPlayerSelector player1;
	private CommandSlotPlayerSelector player2;
	private CommandSlotList<IGuiCommandSlot> playerList;
	private CommandSlotEntityNBT playerNBT;
	private CommandSlotIntTextField value1;
	private CommandSlotIntTextField value2;
	private ITextField<?> displayName;
	private CommandSlotScoreCriteria criteria;
	private CommandSlotMenu objectiveDisplaySlot;
	private CommandSlotModifiable modifiableSidebarTeam;
	private IGuiCommandSlot sidebarTeam;
	private CommandSlotMenu operation;
	private CommandSlotMenu tagOperation;
	private CommandSlotModifiable tagOperationArg;
	private IGuiCommandSlot tagOperationList;
	private IGuiCommandSlot tagOperationAddRemove;
	private CommandSlotTextField tag;
	private CommandSlotTeam team;
	private CommandSlotMenu teamOption;
	private CommandSlotModifiable teamOptionArg;
	private CommandSlotCycleButton teamOptionColor;
	private CommandSlotCheckbox teamOptionBoolean;
	private CommandSlotMenu teamOptionVisibility;
	private CommandSlotMenu teamOptionCollisionRule;

	// @formatter:off
	/*
	 * Command slot structure:
	 * /scoreboard
	 * |         | objectives
	 * |         |          | list
	 * |         |          | add <objective1> <criteria> [displayName]
	 * |         |          | remove <objective1>
	 * |         |          | setdisplay <objectiveDisplaySlot> <objective1>
	 * |         | players
	 * |         |       | list
	 * |         |       | add <player1> <objective1> [value1] [playerNBT]
	 * |         |       | remove <player1> <objective1> [value1] [playerNBT]
	 * |         |       | set <player1> <objective1> <value1> [playerNBT]
	 * |         |       | reset <player1> [objective1]
	 * |         |       | enable <player1> <objective1>
	 * |         |       | test <player1> <objective1> <value1> [value2]
	 * |         |       | operation <player1> <objective1> <operation> <player2> <objective2>
	 * |         |       | tag
	 * |         |       |   | list
	 * |         |       |   | add <player1> <tag> [playerNBT]
	 * |         |       |   | remove <player1> <tag> [playerNBT]
	 * |         | teams
	 * |         |     | list
	 * |         |     | add <team> [displayName]
	 * |         |     | remove <team>
	 * |         |     | empty <team>
	 * |         |     | join <team> [playerList]
	 * |         |     | leave <team> [playerList]
	 * |         |     | option <team>
	 * |         |     |             | color <teamOptionColor>
	 * |         |     |             | friendlyfire <teamOptionBoolean>
	 * |         |     |             | seeFriendlyInvisibles <teamOptionBoolean>
	 * |         |     |             | nametagVisibility <teamOptionVisibility>
	 * |         |     |             | deathMessageVisibility <teamOptionVisibility>
	 * |         |     |             | collisionRule <teamOptionCollisionRule>
	 */
	// @formatter:on

	@Override
	public IGuiCommandSlot[] setupCommand() {
		objective1 = new CommandSlotScore();
		objective2 = new CommandSlotScore();
		playerNBT = new CommandSlotEntityNBT(getContext());
		player1 = new CommandSlotPlayerSelector() {
			@Override
			protected void onSetEntityTo(ResourceLocation newEntityType) {
				playerNBT.setEntityType(CommandSlotPlayerSelector.ENTITY_ANYTHING.equals(newEntityType) ? null
						: GeneralUtils.getEntityClassFromLocation(newEntityType));
			}
		};
		player2 = new CommandSlotPlayerSelector();
		playerList = new CommandSlotList<IGuiCommandSlot>(new Supplier<IGuiCommandSlot>() {
			@Override
			public IGuiCommandSlot get() {
				return new CommandSlotRectangle(new CommandSlotPlayerSelector(CommandSlotPlayerSelector.DISALLOW_UUID),
						Colors.playerSelectorBox.color);
			}
		}) {
			@Override
			public int readFromArgs(String[] args, int index) throws CommandSyntaxException {
				clearEntries();
				int amtRead = args.length - index;
				for (; index < args.length; index++) {
					IGuiCommandSlot playerSelector = newEntry();
					addEntry(playerSelector);
					playerSelector.readFromArgs(args, index);
				}
				return amtRead;
			}
		};
		value1 = new CommandSlotIntTextField(50, 50);
		value2 = new CommandSlotIntTextField(50, 50);
		if (getContext().canHoldFormatting()) {
			displayName = new CommandSlotFormattedTextField(100);
		} else {
			displayName = new CommandSlotTextField(100, 100);
		}
		criteria = new CommandSlotScoreCriteria();
		objectiveDisplaySlot = new CommandSlotMenu(
				new String[] { Translate.GUI_COMMANDEDITOR_SCOREBOARD_OBJECTIVES_SETDISPLAY_LIST,
						Translate.GUI_COMMANDEDITOR_SCOREBOARD_OBJECTIVES_SETDISPLAY_SIDEBAR,
						Translate.GUI_COMMANDEDITOR_SCOREBOARD_OBJECTIVES_SETDISPLAY_BELOWNAME },
				"list", "sidebar", "belowName") {
			@Override
			public int readFromArgs(String[] args, int index) throws CommandSyntaxException {
				if (args.length == index) {
					throw new CommandSyntaxException();
				}
				String slot = args[index];
				if (slot.startsWith("sidebar.team.")) {
					setCurrentIndex(1);
					slot = slot.substring(13);
					TextFormatting color = TextFormatting.getValueByName(slot);
					if (color == null || !color.isColor()) {
						throw new CommandSyntaxException();
					}
					teamOptionColor.setCurrentIndex(color.getColorIndex() + 1);
					return 1;
				} else if (slot.equalsIgnoreCase("sidebar")) {
					setCurrentIndex(1);
					teamOptionColor.setCurrentIndex(0);
					return 1;
				} else if (slot.equalsIgnoreCase("list")) {
					setCurrentIndex(0);
					return 1;
				} else if (slot.equalsIgnoreCase("belowName")) {
					setCurrentIndex(2);
					return 1;
				} else {
					throw new CommandSyntaxException();
				}
			}

			@Override
			public void addArgs(List<String> args) {
				if (getCurrentIndex() == 1) {
					if (teamOptionColor.getCurrentIndex() == 0) {
						args.add("sidebar");
					} else {
						args.add("sidebar.team." + teamOptionColor.getCurrentValue());
					}
				} else {
					super.addArgs(args);
				}
			}

			@Override
			protected void onChanged(String to) {
				if ("sidebar".equals(to)) {
					modifiableSidebarTeam.setChild(sidebarTeam);
				} else {
					modifiableSidebarTeam.setChild(null);
				}
			}
		};
		modifiableSidebarTeam = new CommandSlotModifiable();
		String[] displayNames = new String[17];
		String[] values = new String[17];
		displayNames[0] = Translate.GUI_COMMANDEDITOR_SELECTTEAM_OPTIONS_COLOR_NONE;
		values[0] = "none";
		for (int i = 1; i < 17; i++) {
			values[i] = TextFormatting.fromColorIndex(i - 1).getFriendlyName();
			displayNames[i] = I18n.format("color." + values[i]);
		}
		teamOptionColor = new CommandSlotCycleButton(100, 20, displayNames, values);
		sidebarTeam = CommandSlotLabel.createLabel(
				Translate.GUI_COMMANDEDITOR_SCOREBOARD_OBJECTIVES_SETDISPLAY_SIDEBAR_TEAM,
				new CommandSlotBox(teamOptionColor) {
					@Override
					public int readFromArgs(String[] args, int index) throws CommandSyntaxException {
						return 0;
					}

					@Override
					public void addArgs(List<String> args) throws UIInvalidException {
					}
				});
		operation = new CommandSlotMenu(
				new String[] { Translate.GUI_COMMANDEDITOR_SCOREBOARD_PLAYERS_OPERATION_ADD,
						Translate.GUI_COMMANDEDITOR_SCOREBOARD_PLAYERS_OPERATION_SUBTRACT,
						Translate.GUI_COMMANDEDITOR_SCOREBOARD_PLAYERS_OPERATION_MULTIPLY,
						Translate.GUI_COMMANDEDITOR_SCOREBOARD_PLAYERS_OPERATION_DIVIDE,
						Translate.GUI_COMMANDEDITOR_SCOREBOARD_PLAYERS_OPERATION_MOD,
						Translate.GUI_COMMANDEDITOR_SCOREBOARD_PLAYERS_OPERATION_ASSIGN,
						Translate.GUI_COMMANDEDITOR_SCOREBOARD_PLAYERS_OPERATION_MIN,
						Translate.GUI_COMMANDEDITOR_SCOREBOARD_PLAYERS_OPERATION_MAX,
						Translate.GUI_COMMANDEDITOR_SCOREBOARD_PLAYERS_OPERATION_SWAP },
				"+=", "-=", "*=", "/=", "%=", "=", "<", ">", "><");
		tagOperation = new CommandSlotMenu(new String[] { Translate.GUI_COMMANDEDITOR_SCOREBOARD_PLAYERS_TAG_LIST,
				Translate.GUI_COMMANDEDITOR_SCOREBOARD_PLAYERS_TAG_ADD,
				Translate.GUI_COMMANDEDITOR_SCOREBOARD_PLAYERS_TAG_REMOVE }, "list", "add", "remove") {
			@Override
			protected void onChanged(String to) {
				if ("list".equals(to)) {
					tagOperationArg.setChild(tagOperationList);
				} else if ("add".equals(to) || "remove".equals(to)) {
					tagOperationArg.setChild(tagOperationAddRemove);
				} else {
					throw new IllegalStateException();
				}
			}
		};
		tag = new CommandSlotTextField(50, 50);
		tagOperationList = new CommandSlotVerticalArrangement();
		tagOperationAddRemove = new CommandSlotVerticalArrangement(
				CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_SCOREBOARD_PLAYERS_TAG_NAME, tag),
				CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_SCOREBOARD_NBT,
						new CommandSlotRectangle(
								new CommandSlotOptional.Impl(playerNBT, Lists.<CommandSlotOptional>newArrayList()),
								Colors.nbtBox.color)));
		tagOperationArg = new CommandSlotModifiable(tagOperationList);
		team = new CommandSlotTeam();
		teamOption = new CommandSlotMenu(
				new String[] { Translate.GUI_COMMANDEDITOR_SELECTTEAM_OPTIONS_COLOR,
						Translate.GUI_COMMANDEDITOR_SELECTTEAM_OPTIONS_FRIENDLYFIRE,
						Translate.GUI_COMMANDEDITOR_SELECTTEAM_OPTIONS_SEEFRIENDLYINVISIBLES,
						Translate.GUI_COMMANDEDITOR_SELECTTEAM_OPTIONS_NAMETAGVISIBILITY,
						Translate.GUI_COMMANDEDITOR_SELECTTEAM_OPTIONS_DEATHMESSAGEVISIBILITY,
						Translate.GUI_COMMANDEDITOR_SELECTTEAM_OPTIONS_COLLISIONRULE },
				"color", "friendlyfire", "seeFriendlyInvisibles", "nametagVisibility", "deathMessageVisibility",
				"collisionRule") {
			@Override
			protected void onChanged(String to) {
				if ("color".equals(to)) {
					teamOptionArg.setChild(teamOptionColor);
				} else if ("friendlyfire".equals(to) || "seeFriendlyInvisibles".equals(to)) {
					teamOptionArg.setChild(teamOptionBoolean);
				} else if ("nametagVisibility".equals(to) || "deathMessageVisibility".equals(to)) {
					teamOptionArg.setChild(teamOptionVisibility);
				} else if ("collisionRule".equals(to)) {
					teamOptionArg.setChild(teamOptionCollisionRule);
				} else {
					throw new IllegalStateException();
				}
			}
		};
		teamOptionBoolean = new CommandSlotCheckbox(Translate.GUI_ON);
		values = new String[Team.EnumVisible.values().length];
		displayNames = new String[values.length];
		for (int i = 0; i < values.length; i++) {
			values[i] = Team.EnumVisible.values()[i].internalName;
			displayNames[i] = I18n.format("gui.commandEditor.selectTeam.options.visibility." + values[i]);
		}
		teamOptionVisibility = new CommandSlotMenu(displayNames, values);
		values = new String[Team.CollisionRule.values().length];
		displayNames = new String[values.length];
		for (int i = 0; i < values.length; i++) {
			values[i] = Team.CollisionRule.values()[i].name;
			displayNames[i] = I18n.format("gui.commandEditor.selectTeam.options.collisionRule." + values[i]);
		}
		teamOptionCollisionRule = new CommandSlotMenu(displayNames, values);
		teamOptionArg = new CommandSlotModifiable(teamOptionColor);

		objectivesSubCommand = new CommandSlotMenu(
				new String[] { Translate.GUI_COMMANDEDITOR_SCOREBOARD_OBJECTIVES_LIST,
						Translate.GUI_COMMANDEDITOR_SCOREBOARD_OBJECTIVES_ADD,
						Translate.GUI_COMMANDEDITOR_SCOREBOARD_OBJECTIVES_REMOVE,
						Translate.GUI_COMMANDEDITOR_SCOREBOARD_OBJECTIVES_SETDISPLAY },
				"list", "add", "remove", "setdisplay") {
			@Override
			protected void onChanged(String to) {
				if ("list".equals(to)) {
					objectivesSubCommandArgs.setChild(argsObjectivesList);
				} else if ("add".equals(to)) {
					objectivesSubCommandArgs.setChild(argsObjectivesAdd);
				} else if ("remove".equals(to)) {
					objectivesSubCommandArgs.setChild(argsObjectivesRemove);
				} else if ("setdisplay".equals(to)) {
					objectivesSubCommandArgs.setChild(argsObjectivesSetdisplay);
				} else {
					throw new IllegalStateException();
				}
			}
		};

		argsObjectivesList = new CommandSlotVerticalArrangement();

		argsObjectivesAdd = new CommandSlotVerticalArrangement(
				CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_SCOREBOARD_OBJECTIVE, objective1),
				CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_SCOREBOARD_OBJECTIVES_ADD_CRITERIA, criteria),
				CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_SCOREBOARD_DISPLAYNAME,
						new CommandSlotOptional((IGuiCommandSlot) displayName,
								Lists.<CommandSlotOptional>newArrayList()) {
							@Override
							protected boolean isDefault() throws UIInvalidException {
								return displayName.getTextAsString().equals(objective1.getScore())
										|| displayName.getTextAsString().isEmpty();
							}

							@Override
							protected void setToDefault() {
								displayName.setTextAsString("");
							}
						}));

		argsObjectivesRemove = new CommandSlotVerticalArrangement(
				CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_SCOREBOARD_OBJECTIVE, objective1));

		argsObjectivesSetdisplay = new CommandSlotVerticalArrangement(
				CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_SCOREBOARD_OBJECTIVES_SETDISPLAY_SLOT,
						objectiveDisplaySlot),
				modifiableSidebarTeam,
				CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_SCOREBOARD_OBJECTIVE, objective1));

		objectivesSubCommandArgs = new CommandSlotModifiable(argsObjectivesList);

		subCommandObjectives = new CommandSlotVerticalArrangement(objectivesSubCommand, objectivesSubCommandArgs);

		playersSubCommand = new CommandSlotMenu(
				new String[] { Translate.GUI_COMMANDEDITOR_SCOREBOARD_PLAYERS_LIST,
						Translate.GUI_COMMANDEDITOR_SCOREBOARD_PLAYERS_ADD,
						Translate.GUI_COMMANDEDITOR_SCOREBOARD_PLAYERS_REMOVE,
						Translate.GUI_COMMANDEDITOR_SCOREBOARD_PLAYERS_SET,
						Translate.GUI_COMMANDEDITOR_SCOREBOARD_PLAYERS_RESET,
						Translate.GUI_COMMANDEDITOR_SCOREBOARD_PLAYERS_ENABLE,
						Translate.GUI_COMMANDEDITOR_SCOREBOARD_PLAYERS_TEST,
						Translate.GUI_COMMANDEDITOR_SCOREBOARD_PLAYERS_OPERATION,
						Translate.GUI_COMMANDEDITOR_SCOREBOARD_PLAYERS_TAG },
				"list", "add", "remove", "set", "reset", "enable", "test", "operation", "tag") {
			@Override
			protected void onChanged(String to) {
				if ("list".equals(to)) {
					playersSubCommandArgs.setChild(argsPlayersList);
				} else if ("add".equals(to)) {
					playersSubCommandArgs.setChild(argsPlayersAdd);
				} else if ("remove".equals(to)) {
					playersSubCommandArgs.setChild(argsPlayersRemove);
				} else if ("set".equals(to)) {
					playersSubCommandArgs.setChild(argsPlayersSet);
				} else if ("reset".equals(to)) {
					playersSubCommandArgs.setChild(argsPlayersReset);
				} else if ("enable".equals(to)) {
					playersSubCommandArgs.setChild(argsPlayersEnable);
				} else if ("test".equals(to)) {
					playersSubCommandArgs.setChild(argsPlayersTest);
				} else if ("operation".equals(to)) {
					playersSubCommandArgs.setChild(argsPlayersOperation);
				} else if ("tag".equals(to)) {
					playersSubCommandArgs.setChild(argsPlayersTag);
				} else {
					throw new IllegalStateException();
				}
			}
		};

		argsPlayersList = new CommandSlotVerticalArrangement();

		List<CommandSlotOptional> optionalGroup = Lists.newArrayList();
		argsPlayersAdd = new CommandSlotVerticalArrangement(
				CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_SCOREBOARD_PLAYER,
						new CommandSlotRectangle(player1, Colors.playerSelectorBox.color)),
				CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_SCOREBOARD_OBJECTIVE, objective1),
				CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_SCOREBOARD_VALUE,
						new CommandSlotOptional(value1, optionalGroup) {
							@Override
							protected boolean isDefault() throws UIInvalidException {
								value1.checkValid();
								return value1.getIntValue() == 0;
							}

							@Override
							protected void setToDefault() {
								value1.setText("0");
							}
						}),
				CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_SCOREBOARD_NBT, new CommandSlotRectangle(
						new CommandSlotOptional.Impl(playerNBT, optionalGroup), Colors.nbtBox.color)));

		argsPlayersRemove = argsPlayersAdd;

		argsPlayersSet = new CommandSlotVerticalArrangement(
				CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_SCOREBOARD_PLAYER,
						new CommandSlotRectangle(player1, Colors.playerSelectorBox.color)),
				CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_SCOREBOARD_OBJECTIVE, objective1),
				CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_SCOREBOARD_VALUE, value1),
				CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_SCOREBOARD_NBT, new CommandSlotRectangle(
						new CommandSlotOptional.Impl(playerNBT, optionalGroup), Colors.nbtBox.color)));

		argsPlayersReset = new CommandSlotVerticalArrangement(
				CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_SCOREBOARD_PLAYER,
						new CommandSlotRectangle(player1, Colors.playerSelectorBox.color)),
				new CommandSlotRadioList(
						CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_SCOREBOARD_PLAYERS_RESET_ALL),
						CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_SCOREBOARD_OBJECTIVE, objective1)) {
					@Override
					protected int getSelectedIndexForString(String[] args, int index) throws CommandSyntaxException {
						return args.length == index ? 0 : 1;
					}
				});

		argsPlayersEnable = new CommandSlotVerticalArrangement(
				CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_SCOREBOARD_PLAYER,
						new CommandSlotRectangle(player1, Colors.playerSelectorBox.color)),
				CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_SCOREBOARD_OBJECTIVE, objective1));

		argsPlayersTest = new CommandSlotVerticalArrangement(
				CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_SCOREBOARD_PLAYER,
						new CommandSlotRectangle(player1, Colors.playerSelectorBox.color)),
				CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_SCOREBOARD_OBJECTIVE, objective1),
				CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_SCOREBOARD_PLAYERS_TEST_MIN,
						new CommandSlotBox(value1) {
							@Override
							public int readFromArgs(String[] args, int index) throws CommandSyntaxException {
								if (args.length == index) {
									throw new CommandSyntaxException();
								} else if ("*".equals(args[index])) {
									value1.setText(String.valueOf(Integer.MIN_VALUE));
									return 1;
								} else {
									return value1.readFromArgs(args, index);
								}
							}

							@Override
							public void addArgs(List<String> args) throws UIInvalidException {
								value1.checkValid();
								if (value1.getIntValue() == Integer.MIN_VALUE) {
									args.add("*");
								} else {
									value1.addArgs(args);
								}
							}
						}),
				CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_SCOREBOARD_PLAYERS_TEST_MAX,
						new CommandSlotOptional(new CommandSlotBox(value2) {
							@Override
							public int readFromArgs(String[] args, int index) throws CommandSyntaxException {
								if ("*".equals(args[index])) {
									value2.setText(String.valueOf(Integer.MAX_VALUE));
									return 1;
								} else {
									return value2.readFromArgs(args, index);
								}
							}
						}, Lists.<CommandSlotOptional>newArrayList()) {
							@Override
							protected boolean isDefault() throws UIInvalidException {
								value2.checkValid();
								return value2.getIntValue() == Integer.MAX_VALUE;
							}

							@Override
							protected void setToDefault() {
								value2.setText(String.valueOf(Integer.MAX_VALUE));
							}
						}));

		argsPlayersOperation = new CommandSlotVerticalArrangement(
				CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_SCOREBOARD_PLAYERS_OPERATION_TARGETPLAYER,
						Translate.GUI_COMMANDEDITOR_SCOREBOARD_PLAYERS_OPERATION_TARGETPLAYER_TOOLTIP,
						new CommandSlotRectangle(player1, Colors.playerSelectorBox.color)),
				CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_SCOREBOARD_PLAYERS_OPERATION_TARGETOBJECTIVE,
						Translate.GUI_COMMANDEDITOR_SCOREBOARD_PLAYERS_OPERATION_TARGETOBJECTIVE_TOOLTIP, objective1),
				CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_SCOREBOARD_PLAYERS_OPERATION_TYPE, operation),
				CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_SCOREBOARD_PLAYERS_OPERATION_SOURCEPLAYER,
						Translate.GUI_COMMANDEDITOR_SCOREBOARD_PLAYERS_OPERATION_SOURCEPLAYER_TOOLTIP,
						new CommandSlotRectangle(player2, Colors.playerSelectorBox.color)),
				CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_SCOREBOARD_PLAYERS_OPERATION_SOURCEOBJECTIVE,
						Translate.GUI_COMMANDEDITOR_SCOREBOARD_PLAYERS_OPERATION_SOURCEOBJECTIVE_TOOLTIP, objective2));

		argsPlayersTag = new CommandSlotVerticalArrangement(tagOperation, tagOperationArg);

		playersSubCommandArgs = new CommandSlotModifiable(argsPlayersList);

		subCommandPlayers = new CommandSlotVerticalArrangement(playersSubCommand, playersSubCommandArgs);

		teamsSubCommand = new CommandSlotMenu(new String[] { Translate.GUI_COMMANDEDITOR_SCOREBOARD_TEAMS_LIST,
				Translate.GUI_COMMANDEDITOR_SCOREBOARD_TEAMS_ADD, Translate.GUI_COMMANDEDITOR_SCOREBOARD_TEAMS_REMOVE,
				Translate.GUI_COMMANDEDITOR_SCOREBOARD_TEAMS_EMPTY, Translate.GUI_COMMANDEDITOR_SCOREBOARD_TEAMS_JOIN,
				Translate.GUI_COMMANDEDITOR_SCOREBOARD_TEAMS_LEAVE,
				Translate.GUI_COMMANDEDITOR_SCOREBOARD_TEAMS_OPTION }, "list", "add", "remove", "empty", "join",
				"leave", "option") {
			@Override
			protected void onChanged(String to) {
				if ("list".equals(to)) {
					teamsSubCommandArgs.setChild(argsTeamsList);
				} else if ("add".equals(to)) {
					teamsSubCommandArgs.setChild(argsTeamsAdd);
				} else if ("remove".equals(to)) {
					teamsSubCommandArgs.setChild(argsTeamsRemove);
				} else if ("empty".equals(to)) {
					teamsSubCommandArgs.setChild(argsTeamsEmpty);
				} else if ("join".equals(to)) {
					teamsSubCommandArgs.setChild(argsTeamsJoin);
				} else if ("leave".equals(to)) {
					teamsSubCommandArgs.setChild(argsTeamsLeave);
				} else if ("option".equals(to)) {
					teamsSubCommandArgs.setChild(argsTeamsOption);
				} else {
					throw new IllegalStateException();
				}
			}
		};

		argsTeamsList = new CommandSlotVerticalArrangement();

		argsTeamsAdd = new CommandSlotVerticalArrangement(
				CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_SCOREBOARD_TEAM, team),
				CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_SCOREBOARD_DISPLAYNAME,
						new CommandSlotOptional((IGuiCommandSlot) displayName,
								Lists.<CommandSlotOptional>newArrayList()) {
							@Override
							protected boolean isDefault() throws UIInvalidException {
								return displayName.getTextAsString().isEmpty()
										|| displayName.getTextAsString().equals(team.getTeam());
							}

							@Override
							protected void setToDefault() {
								displayName.setTextAsString("");
							}
						}));

		argsTeamsRemove = new CommandSlotVerticalArrangement(
				CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_SCOREBOARD_TEAM, team));

		argsTeamsEmpty = argsTeamsRemove;

		argsTeamsJoin = new CommandSlotVerticalArrangement(
				CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_SCOREBOARD_TEAM, team),
				CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_SCOREBOARD_PLAYERLIST, playerList));

		argsTeamsLeave = argsTeamsJoin;

		argsTeamsOption = new CommandSlotVerticalArrangement(
				CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_SCOREBOARD_TEAM, team),
				CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_SCOREBOARD_TEAMS_OPTION_TYPE, teamOption),
				teamOptionArg);

		teamsSubCommandArgs = new CommandSlotModifiable(argsTeamsList);

		subCommandTeams = new CommandSlotVerticalArrangement(teamsSubCommand, teamsSubCommandArgs);

		subCommandArgs = new CommandSlotModifiable(subCommandObjectives);

		subCommand = new CommandSlotMenu(
				new String[] { Translate.GUI_COMMANDEDITOR_SCOREBOARD_OBJECTIVES,
						Translate.GUI_COMMANDEDITOR_SCOREBOARD_PLAYERS, Translate.GUI_COMMANDEDITOR_SCOREBOARD_TEAMS },
				"objectives", "players", "teams") {
			@Override
			protected void onChanged(String to) {
				if ("objectives".equals(to)) {
					subCommandArgs.setChild(subCommandObjectives);
				} else if ("players".equals(to)) {
					subCommandArgs.setChild(subCommandPlayers);
				} else if ("teams".equals(to)) {
					subCommandArgs.setChild(subCommandTeams);
				} else {
					throw new IllegalStateException();
				}
			}
		};

		return new IGuiCommandSlot[] { subCommand, subCommandArgs };
	}

}

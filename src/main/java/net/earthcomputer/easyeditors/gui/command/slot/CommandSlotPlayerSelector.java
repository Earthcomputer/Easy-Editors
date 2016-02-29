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
import net.earthcomputer.easyeditors.gui.command.UIInvalidException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.util.MathHelper;
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

	@Override
	public void addArgs(List<String> args) throws UIInvalidException {
		checkValid();
		super.addArgs(args);
	}

	/**
	 * 
	 * @throws UIInvalidException
	 *             - when this player selector is invalid
	 */
	public void checkValid() throws UIInvalidException {
		int i = 0;
		int selectedIndex = radioList.getSelectedIndex();
		if ((flags & DISALLOW_USERNAME) == 0) {
			if (selectedIndex == i) {
				if (playerNameField.getText().isEmpty()) {
					throw new UIInvalidException("gui.commandEditor.playerSelector.noUsernameTyped");
				}
			}
			i++;
		}
		if ((flags & DISALLOW_UUID) == 0) {
			if (selectedIndex == i) {
				if (!Patterns.UUID.matcher(UUIDField.getText()).matches()) {
					throw new UIInvalidException("gui.commandEditor.playerSelector.invalidUUID");
				}
			}
			i++;
		}
		if ((flags & DISALLOW_SELECTOR) == 0) {
			if (selectedIndex == i)
				playerSelector.checkValid();
		}
	}

	private class CmdPlayerSelector extends CommandSlotVerticalArrangement {

		private CommandSlotMenu selectorType;
		private CommandSlotCheckbox targetInverted;
		private CommandSlotEntity targetEntity;

		private static final int SELTYPE_NEAREST = 0;
		private static final int SELTYPE_FARTHEST = 1;
		private static final int SELTYPE_ALL = 2;
		private static final int SELTYPE_RANDOM = 3;

		private CommandSlotExpand expand;

		private CommandSlotIntTextField countField;
		private CommandSlotModifiable<IGuiCommandSlot> modifiableCountField;

		private CommandSlotCheckbox nameInverted;
		private CommandSlotTextField entityName;

		private static final String ENTITY_ANYTHING = "Anything";

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

		private static final int POSTYPE_RADIUS = 0;
		private static final int POSTYPE_BB_KNOWN_COORDS = 1;
		private static final int POSTYPE_BB_UNKNOWN_COORDS = 2;

		private CommandSlotIntTextField minHRotation;
		private CommandSlotIntTextField maxHRotation;
		private CommandSlotIntTextField minVRotation;
		private CommandSlotIntTextField maxVRotation;
		private IGuiCommandSlot rotations;
		private CommandSlotModifiable<IGuiCommandSlot> modifiableRotations;

		private CommandSlotIntTextField minExp;
		private CommandSlotIntTextField maxExp;
		private CommandSlotMenu gamemode;
		private IGuiCommandSlot playersOnlySlots;
		private CommandSlotModifiable<IGuiCommandSlot> modifiablePlayersOnlySlots;

		private static final int GAMEMODE_ANY = 0;

		private String waitingTeamName;
		private IGuiCommandSlot team;
		private CommandSlotModifiable<IGuiCommandSlot> modifiableTeam;
		private CommandSlotCheckbox teamInverted;
		private IGuiCommandSlot teamName;
		private CommandSlotModifiable<IGuiCommandSlot> modifiableTeamName;

		private static final int TEAM_ANY = 0;
		private static final int TEAM_NONE = 1;
		private static final String TEAM_ANY_NAME = "any";
		private static final String TEAM_NONE_NAME = "none";

		private List<ScoreObjective> objectivesList;
		private String objectiveErrorMessage;
		private CommandSlotList<CmdScoreTest> scoreTests;

		public CmdPlayerSelector() {
			preSetup();
			setupHeader();
			setupSpecifics();
		}

		private void preSetup() {
			setupTeamSlot();
			obtainTeamsList();

			setupScoresSlot();
			obtainObjectiveList();

			preSetupRotationsSlot();
			preSetupPlayersOnlySlot();
		}

		private void setupTeamSlot() {
			teamInverted = new CommandSlotCheckbox(I18n.format("gui.commandEditor.playerSelector.team.inverted"));

			teamName = new CommandSlotLabel(Minecraft.getMinecraft().fontRendererObj,
					I18n.format("gui.commandEditor.playerSelector.team.waiting"), 0x404040);
			modifiableTeamName = new CommandSlotModifiable<IGuiCommandSlot>(teamName);

			team = CommandSlotLabel.createLabel(I18n.format("gui.commandEditor.playerSelector.team"),
					Colors.playerSelectorLabel.color, teamInverted, modifiableTeamName);
			modifiableTeam = new CommandSlotModifiable<IGuiCommandSlot>(team);
		}

		private void obtainTeamsList() {
			ChatBlocker.obtainTeamsList(new ReturnedValueListener<List<ScorePlayerTeam>>() {

				@Override
				public void returnValue(List<ScorePlayerTeam> value) {
					List<String> registeredNames = Lists.newArrayList();
					registeredNames.add(TEAM_ANY_NAME);
					registeredNames.add(TEAM_NONE_NAME);

					List<String> displayNames = Lists.newArrayList();
					displayNames.add(I18n.format("gui.commandEditor.playerSelector.team.any"));
					displayNames.add(I18n.format("gui.commandEditor.playerSelector.team.none"));

					for (ScorePlayerTeam team : value) {
						registeredNames.add(team.getRegisteredName());
						displayNames.add(team.getTeamName());
					}

					CommandSlotMenu teamMenu;
					String[] displayNamesArray = displayNames.toArray(new String[displayNames.size()]);
					String[] registeredNamesArray = registeredNames.toArray(new String[registeredNames.size()]);
					teamName = teamMenu = new CommandSlotMenu(displayNamesArray, registeredNamesArray);
					modifiableTeamName.setChild(teamName);

					if ("".equals(waitingTeamName))
						teamMenu.setCurrentIndex(1);
					else if (waitingTeamName != null) {
						for (int i = 2; i < value.size(); i++) {
							if (value.get(i).equals(waitingTeamName))
								teamMenu.setCurrentIndex(i);
						}
					}
				}

				@Override
				public void abortFindingValue(int reason) {
					String errorMessage = reason == 0 ? "gui.commandEditor.playerSelector.team.timedOut"
							: "gui.commandEditor.playerSelector.team.noPermission";
					errorMessage = I18n.format(errorMessage);
					teamName = new CommandSlotLabel(Minecraft.getMinecraft().fontRendererObj, errorMessage, 0xff0000);
					modifiableTeamName.setChild(teamName);
				}
			});

		}

		private void setupScoresSlot() {
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

						r.objective = new CommandSlotMenu(displayNames, names);
						r.modifiableObjective.setChild(r.objective);
					} else if (objectiveErrorMessage != null) {
						r.modifiableObjective.setChild(new CommandSlotLabel(Minecraft.getMinecraft().fontRendererObj,
								objectiveErrorMessage, 0xff0000));
					}
					return r;
				}

			}).setAppendHoverText(I18n.format("gui.commandEditor.playerSelector.score.append"))
					.setInsertHoverText(I18n.format("gui.commandEditor.playerSelector.score.insert"))
					.setRemoveHoverText(I18n.format("gui.commandEditor.playerSelector.score.remove"));
		}

		private void obtainObjectiveList() {
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
					objectiveErrorMessage = I18n.format(objectiveErrorMessage);
					for (int i = 0; i < scoreTests.entryCount(); i++) {
						scoreTests.getEntry(i).modifiableObjective.setChild(new CommandSlotLabel(
								Minecraft.getMinecraft().fontRendererObj, objectiveErrorMessage, 0xff0000));
					}
				}

			});
		}

		private void preSetupRotationsSlot() {
			modifiableRotations = new CommandSlotModifiable<IGuiCommandSlot>(null);
		}

		private void preSetupPlayersOnlySlot() {
			modifiablePlayersOnlySlots = new CommandSlotModifiable<IGuiCommandSlot>(null);
		}

		private void setupHeader() {
			CommandSlotHorizontalArrangement header = new CommandSlotHorizontalArrangement();

			header.addChild(setupSelectorTypeSlot());
			if ((flags & PLAYERS_ONLY) == 0) {
				header.addChild(setupTargetEntityInvertedSlot());
				header.addChild(setupTargetEntitySlot());
			} else {
				header.addChild(setupPlayersOnlyTargetEntitySlot());
			}

			addChild(header);
		}

		private IGuiCommandSlot setupSelectorTypeSlot() {
			return selectorType = new CommandSlotMenu(I18n.format("gui.commandEditor.playerSelector.nearest"),
					I18n.format("gui.commandEditor.playerSelector.farthest"),
					I18n.format("gui.commandEditor.playerSelector.all"),
					I18n.format("gui.commandEditor.playerSelector.random")) {

				@Override
				protected void onChanged(String to) {
					if ((flags & ONE_ONLY) == 0) {
						if (getCurrentIndex() == SELTYPE_ALL)
							modifiableCountField.setChild(null);
						else
							modifiableCountField.setChild(countField);
					}
				}

			};
		}

		private IGuiCommandSlot setupTargetEntityInvertedSlot() {
			return targetInverted = new CommandSlotCheckbox(
					I18n.format("gui.commandEditor.playerSelector.targetInverted"));
		}

		private IGuiCommandSlot setupTargetEntitySlot() {
			targetEntity = new CommandSlotEntity((flags & NON_PLAYERS_ONLY) == 0, false, ENTITY_ANYTHING) {

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
						modifiableRotations.setChild(CmdPlayerSelector.this.rotations);
						modifiableTeam.setChild(team);
					} else {
						modifiableRotations.setChild(null);
						modifiableTeam.setChild(null);
					}
				}

			};

			if ((flags & NON_PLAYERS_ONLY) == 0)
				targetEntity.setEntity("Player");
			else
				targetEntity.setEntity(ENTITY_ANYTHING);

			return targetEntity;
		}

		private IGuiCommandSlot setupPlayersOnlyTargetEntitySlot() {
			return new CommandSlotLabel(Minecraft.getMinecraft().fontRendererObj,
					GuiSelectEntity.getEntityName("Player"), 0);
		}

		private void setupSpecifics() {
			CommandSlotVerticalArrangement specifics = new CommandSlotVerticalArrangement();

			if ((flags & ONE_ONLY) == 0) {
				specifics.addChild(setupCountSlot());
			}

			specifics.addChild(setupNameSlot());
			specifics.addChild(setupPositionalConstraintsSlot());
			specifics.addChild(setupRotationsSlot());
			specifics.addChild(setupPlayersOnlySlot());
			specifics.addChild(modifiableTeam);
			specifics.addChild(setupScoresSlotWithLabel());

			expand = new CommandSlotExpand(specifics);
			addChild(expand);
		}

		private IGuiCommandSlot setupCountSlot() {
			countField = new CommandSlotIntTextField(50, 50, 1);
			countField.setNumberInvalidMessage("gui.commandEditor.playerSelector.count.invalid")
					.setOutOfBoundsMessage("gui.commandEditor.playerSelector.count.outOfBounds");
			modifiableCountField = new CommandSlotModifiable<IGuiCommandSlot>(
					CommandSlotLabel.createLabel(I18n.format("gui.commandEditor.playerSelector.count"),
							Colors.playerSelectorLabel.color, countField));

			countField.setText("1");

			return modifiableCountField;
		}

		private IGuiCommandSlot setupNameSlot() {
			nameInverted = new CommandSlotCheckbox(I18n.format("gui.commandEditor.playerSelector.entityNameInverted"));
			entityName = new CommandSlotTextField(200, 200);
			IGuiCommandSlot r = new CommandSlotHorizontalArrangement(
					new CommandSlotLabel(Minecraft.getMinecraft().fontRendererObj,
							I18n.format("gui.commandEditor.playerSelector.entityName"),
							Colors.playerSelectorLabel.color),
					nameInverted, entityName);

			entityName.setContentFilter(new Predicate<String>() {

				@Override
				public boolean apply(String input) {
					return Patterns.partialPlayerName.matcher(input).matches();
				}

			});

			return r;
		}

		private IGuiCommandSlot setupPositionalConstraintsSlot() {
			CommandSlotVerticalArrangement wholeColumn = new CommandSlotVerticalArrangement();

			wholeColumn.addChild(new CommandSlotLabel(Minecraft.getMinecraft().fontRendererObj,
					I18n.format("gui.commandEditor.playerSelector.positionalConstraints"),
					Colors.playerSelectorLabel.color));

			positionalConstraints = new CommandSlotRadioList() {
				@Override
				protected int getSelectedIndexForString(String[] args, int index) {
					return POSTYPE_RADIUS;
				}
			};

			positionalConstraints.addChild(setupRadiusPosConstraint());
			positionalConstraints.addChild(setupBoundsFromToConstraint());
			positionalConstraints.addChild(setupBoundsDistConstraint());

			wholeColumn.addChild(new CommandSlotRectangle(positionalConstraints, Colors.playerSelectorBox.color));

			return wholeColumn;
		}

		private IGuiCommandSlot setupRadiusPosConstraint() {
			CommandSlotVerticalArrangement radiusConstraint = new CommandSlotVerticalArrangement();

			IGuiCommandSlot[] xyz = setupXYZConstraint();
			rOriginX = (CommandSlotIntTextField) xyz[1];
			rOriginX.setNumberInvalidMessage("gui.commandEditor.playerSelector.radius.origin.xInvalid")
					.setOutOfBoundsMessage("gui.commandEditor.playerSelector.radius.origin.xOutOfBounds");
			rOriginY = (CommandSlotIntTextField) xyz[2];
			rOriginY.setNumberInvalidMessage("gui.commandEditor.playerSelector.radius.origin.yInvalid")
					.setOutOfBoundsMessage("gui.commandEditor.playerSelector.radius.origin.yOutOfBounds");
			rOriginZ = (CommandSlotIntTextField) xyz[3];
			rOriginZ.setNumberInvalidMessage("gui.commandEditor.playerSelector.radius.origin.zInvalid")
					.setOutOfBoundsMessage("gui.commandEditor.playerSelector.radius.origin.zOutOfBounds");
			radiusConstraint.addChild(
					CommandSlotLabel.createLabel(I18n.format("gui.commandEditor.playerSelector.radius.origin"),
							Colors.playerSelectorLabel.color, xyz[0]));

			radiusConstraint.addChild(setupMinRadiusConstraint());
			radiusConstraint.addChild(setupMaxRadiusConstraint());

			return CommandSlotLabel.createLabel(I18n.format("gui.commandEditor.playerSelector.radius"),
					Colors.playerSelectorLabel.color, radiusConstraint);
		}

		private IGuiCommandSlot setupMinRadiusConstraint() {
			minRadius = new CommandSlotIntTextField(30, 100, 0);
			minRadius.setNumberInvalidMessage("gui.commandEditor.playerSelector.radius.min.invalid")
					.setOutOfBoundsMessage("gui.commandEditor.playerSelector.radius.min.outOfBounds");
			return CommandSlotLabel.createLabel(I18n.format("gui.commandEditor.playerSelector.radius.min"),
					Colors.playerSelectorLabel.color, minRadius);
		}

		private IGuiCommandSlot setupMaxRadiusConstraint() {
			maxRadius = new CommandSlotIntTextField(30, 100, 0);
			maxRadius.setNumberInvalidMessage("gui.commandEditor.playerSelector.radius.max.invalid")
					.setOutOfBoundsMessage("gui.commandEditor.playerSelector.radius.max.outOfBounds");
			return CommandSlotLabel.createLabel(I18n.format("gui.commandEditor.playerSelector.radius.max"),
					Colors.playerSelectorLabel.color, maxRadius);
		}

		private IGuiCommandSlot setupBoundsFromToConstraint() {
			CommandSlotVerticalArrangement fromToConstraint = new CommandSlotVerticalArrangement();

			IGuiCommandSlot[] xyz = setupXYZConstraint();
			boundsX1 = (CommandSlotIntTextField) xyz[1];
			boundsX1.setNumberInvalidMessage("gui.commandEditor.playerSelector.boundsFromTo.from.xInvalid")
					.setOutOfBoundsMessage("gui.commandEditor.playerSelector.boundsFromTo.from.xOutOfBounds");
			boundsY1 = (CommandSlotIntTextField) xyz[2];
			boundsY1.setNumberInvalidMessage("gui.commandEditor.playerSelector.boundsFromTo.from.yInvalid")
					.setOutOfBoundsMessage("gui.commandEditor.playerSelector.boundsFromTo.from.yOutOfBounds");
			boundsZ1 = (CommandSlotIntTextField) xyz[3];
			boundsZ1.setNumberInvalidMessage("gui.commandEditor.playerSelector.boundsFromTo.from.zInvalid")
					.setOutOfBoundsMessage("gui.commandEditor.playerSelector.boundsFromTo.from.zOutOfBounds");
			fromToConstraint.addChild(
					CommandSlotLabel.createLabel(I18n.format("gui.commandEditor.playerSelector.boundsFromTo.from"),
							Colors.playerSelectorLabel.color, xyz[0]));

			xyz = setupXYZConstraint();
			boundsX2 = (CommandSlotIntTextField) xyz[1];
			boundsX2.setNumberInvalidMessage("gui.commandEditor.playerSelector.boundsFromTo.to.xInvalid")
					.setOutOfBoundsMessage("gui.commandEditor.playerSelector.boundsFromTo.to.xOutOfBounds");
			boundsY2 = (CommandSlotIntTextField) xyz[2];
			boundsY2.setNumberInvalidMessage("gui.commandEditor.playerSelector.boundsFromTo.to.yInvalid")
					.setOutOfBoundsMessage("gui.commandEditor.playerSelector.boundsFromTo.to.yOutOfBounds");
			boundsZ2 = (CommandSlotIntTextField) xyz[3];
			boundsZ2.setNumberInvalidMessage("gui.commandEditor.playerSelector.boundsFromTo.to.zInvalid")
					.setOutOfBoundsMessage("gui.commandEditor.playerSelector.boundsFromTo.to.zOutOfBounds");
			fromToConstraint.addChild(
					CommandSlotLabel.createLabel(I18n.format("gui.commandEditor.playerSelector.boundsFromTo.to"),
							Colors.playerSelectorLabel.color, xyz[0]));

			return CommandSlotLabel.createLabel(I18n.format("gui.commandEditor.playerSelector.boundsFromTo"),
					Colors.playerSelectorLabel.color, fromToConstraint);
		}

		private IGuiCommandSlot setupBoundsDistConstraint() {
			CommandSlotVerticalArrangement boundsDistConstraint = new CommandSlotVerticalArrangement();

			IGuiCommandSlot[] xyz = setupXYZConstraint();
			dOriginX = (CommandSlotIntTextField) xyz[1];
			dOriginX.setNumberInvalidMessage("gui.commandEditor.playerSelector.boundsDist.origin.xInvalid")
					.setOutOfBoundsMessage("gui.commandEditor.playerSelector.boundsDist.origin.xOutOfBounds");
			dOriginY = (CommandSlotIntTextField) xyz[2];
			dOriginY.setNumberInvalidMessage("gui.commandEditor.playerSelector.boundsDist.origin.yInvalid")
					.setOutOfBoundsMessage("gui.commandEditor.playerSelector.boundsDist.origin.yOutOfBounds");
			dOriginZ = (CommandSlotIntTextField) xyz[3];
			dOriginZ.setNumberInvalidMessage("gui.commandEditor.playerSelector.boundsDist.origin.zInvalid")
					.setOutOfBoundsMessage("gui.commandEditor.playerSelector.boundsDist.origin.zOutOfBounds");
			boundsDistConstraint.addChild(
					CommandSlotLabel.createLabel(I18n.format("gui.commandEditor.playerSelector.boundsDist.origin"),
							Colors.playerSelectorLabel.color, xyz[0]));

			xyz = setupXYZConstraint();
			distX = (CommandSlotIntTextField) xyz[1];
			distX.setNumberInvalidMessage("gui.commandEditor.playerSelector.boundsDist.distance.xInvalid")
					.setOutOfBoundsMessage("gui.commandEditor.playerSelector.boundsDist.distance.xOutOfBounds");
			distY = (CommandSlotIntTextField) xyz[2];
			distY.setNumberInvalidMessage("gui.commandEditor.playerSelector.boundsDist.distance.yInvalid")
					.setOutOfBoundsMessage("gui.commandEditor.playerSelector.boundsDist.distance.yOutOfBounds");
			distZ = (CommandSlotIntTextField) xyz[3];
			distZ.setNumberInvalidMessage("gui.commandEditor.playerSelector.boundsDist.distance.zInvalid")
					.setOutOfBoundsMessage("gui.commandEditor.playerSelector.boundsDist.distance.zOutOfBounds");
			boundsDistConstraint.addChild(
					CommandSlotLabel.createLabel(I18n.format("gui.commandEditor.playerSelector.boundsDist.distance"),
							Colors.playerSelectorLabel.color, xyz[0]));

			return CommandSlotLabel.createLabel(I18n.format("gui.commandEditor.playerSelector.boundsDist"),
					Colors.playerSelectorLabel.color, boundsDistConstraint);
		}

		/**
		 * 
		 * @return An array with length 4:<br/>
		 *         0: The whole CommandSlotHorizontalArrangement which should be
		 *         displayed to the user<br/>
		 *         1: The CommandSlotIntTextField for the X-coordinate<br/>
		 *         2: The CommandSlotIntTextField for the Y-coordinate<br/>
		 *         3: The CommandSlotIntTextField for the Z-coordinate
		 */
		private IGuiCommandSlot[] setupXYZConstraint() {
			IGuiCommandSlot[] r = new IGuiCommandSlot[4];

			CommandSlotHorizontalArrangement wholeRow = new CommandSlotHorizontalArrangement();
			r[0] = wholeRow;

			wholeRow.addChild(new CommandSlotLabel(Minecraft.getMinecraft().fontRendererObj, "X",
					Colors.playerSelectorLabel.color));
			wholeRow.addChild(r[1] = new CommandSlotIntTextField(30, 100));
			wholeRow.addChild(new CommandSlotLabel(Minecraft.getMinecraft().fontRendererObj, "Y",
					Colors.playerSelectorLabel.color));
			wholeRow.addChild(r[2] = new CommandSlotIntTextField(30, 100));
			wholeRow.addChild(new CommandSlotLabel(Minecraft.getMinecraft().fontRendererObj, "Z",
					Colors.playerSelectorLabel.color));
			wholeRow.addChild(r[3] = new CommandSlotIntTextField(30, 100));

			return r;
		}

		private IGuiCommandSlot setupRotationsSlot() {
			CommandSlotVerticalArrangement wholeColumn = new CommandSlotVerticalArrangement();
			rotations = wholeColumn;

			wholeColumn.addChild(new CommandSlotLabel(Minecraft.getMinecraft().fontRendererObj,
					I18n.format("gui.commandEditor.playerSelector.rotations"), Colors.playerSelectorLabel.color));

			CommandSlotVerticalArrangement insideBox = new CommandSlotVerticalArrangement();

			insideBox.addChild(setupHorizontalRotationSlot());
			insideBox.addChild(setupVerticalRotationSlot());

			wholeColumn.addChild(new CommandSlotRectangle(insideBox, Colors.playerSelectorBox.color));

			modifiableRotations.setChild(wholeColumn);
			return modifiableRotations;
		}

		private IGuiCommandSlot setupHorizontalRotationSlot() {
			CommandSlotVerticalArrangement insideBox = new CommandSlotVerticalArrangement();

			insideBox.addChild(setupMinHRotationSlot());
			insideBox.addChild(setupMaxHRotationSlot());

			IGuiCommandSlot component = new CommandSlotRectangle(insideBox, Colors.playerSelectorLabel.color);
			return CommandSlotLabel.createLabel(I18n.format("gui.commandEditor.playerSelector.rotations.horizontal"),
					Colors.playerSelectorLabel.color, component);
		}

		private IGuiCommandSlot setupMinHRotationSlot() {
			minHRotation = new CommandSlotIntTextField(30, 100, -180, 179);
			minHRotation.setNumberInvalidMessage("gui.commandEditor.playerSelector.rotations.horizontal.minInvalid")
					.setOutOfBoundsMessage("gui.commandEditor.playerSelector.rotations.horizontal.minOutOfBounds");
			return CommandSlotLabel.createLabel(I18n.format("gui.commandEditor.playerSelector.rotations.min"),
					Colors.playerSelectorLabel.color, minHRotation);
		}

		private IGuiCommandSlot setupMaxHRotationSlot() {
			maxHRotation = new CommandSlotIntTextField(30, 100, -180, 179);
			maxHRotation.setNumberInvalidMessage("gui.commandEditor.playerSelector.rotations.horizontal.maxInvalid")
					.setOutOfBoundsMessage("gui.commandEditor.playerSelector.rotations.horizontal.maxOutOfBounds");
			return CommandSlotLabel.createLabel(I18n.format("gui.commandEditor.playerSelector.rotations.max"),
					Colors.playerSelectorLabel.color, maxHRotation);
		}

		private IGuiCommandSlot setupVerticalRotationSlot() {
			CommandSlotVerticalArrangement insideBox = new CommandSlotVerticalArrangement();

			insideBox.addChild(setupMinVRotationSlot());
			insideBox.addChild(setupMaxVRotationSlot());

			IGuiCommandSlot component = new CommandSlotRectangle(insideBox, Colors.playerSelectorLabel.color);
			return CommandSlotLabel.createLabel(I18n.format("gui.commandEditor.playerSelector.rotations.vertical"),
					Colors.playerSelectorLabel.color, component);
		}

		private IGuiCommandSlot setupMinVRotationSlot() {
			minVRotation = new CommandSlotIntTextField(30, 100, -180, 179);
			minVRotation.setNumberInvalidMessage("gui.commandEditor.playerSelector.rotations.vertical.minInvalid")
					.setOutOfBoundsMessage("gui.commandEditor.playerSelector.rotations.vertical.minOutOfBounds");
			return CommandSlotLabel.createLabel(I18n.format("gui.commandEditor.playerSelector.rotations.min"),
					Colors.playerSelectorLabel.color, minVRotation);
		}

		private IGuiCommandSlot setupMaxVRotationSlot() {
			maxVRotation = new CommandSlotIntTextField(30, 100, -180, 179);
			maxVRotation.setNumberInvalidMessage("gui.commandEditor.playerSelector.rotations.vertical.maxInvalid")
					.setOutOfBoundsMessage("gui.commandEditor.playerSelector.rotations.vertical.maxOutOfBounds");
			return CommandSlotLabel.createLabel(I18n.format("gui.commandEditor.playerSelector.rotations.max"),
					Colors.playerSelectorLabel.color, maxVRotation);
		}

		private IGuiCommandSlot setupPlayersOnlySlot() {
			CommandSlotVerticalArrangement playersOnlySlots = new CommandSlotVerticalArrangement();
			this.playersOnlySlots = playersOnlySlots;

			playersOnlySlots.addChild(setupExperienceSlot());
			playersOnlySlots.addChild(setupGamemodeSlot());

			if ((flags & NON_PLAYERS_ONLY) == 0)
				modifiablePlayersOnlySlots.setChild(playersOnlySlots);

			return modifiablePlayersOnlySlots;
		}

		private IGuiCommandSlot setupExperienceSlot() {
			CommandSlotVerticalArrangement expSlots = new CommandSlotVerticalArrangement();

			expSlots.addChild(new CommandSlotLabel(Minecraft.getMinecraft().fontRendererObj,
					I18n.format("gui.commandEditor.playerSelector.exp"), Colors.playerSelectorLabel.color));

			CommandSlotVerticalArrangement insideBox = new CommandSlotVerticalArrangement();
			minExp = new CommandSlotIntTextField(30, 100, 0);
			minExp.setNumberInvalidMessage("gui.commandEditor.playerSelector.exp.min.invalid")
					.setOutOfBoundsMessage("gui.commandEditor.playerSelector.exp.min.outOfBounds");
			insideBox.addChild(CommandSlotLabel.createLabel(I18n.format("gui.commandEditor.playerSelector.exp.min"),
					Colors.playerSelectorLabel.color, minExp));
			maxExp = new CommandSlotIntTextField(30, 100, 0);
			maxExp.setNumberInvalidMessage("gui.commandEditor.playerSelector.exp.max.invalid")
			.setOutOfBoundsMessage("gui.commandEditor.playerSelector.exp.max.outOfBounds");
			insideBox.addChild(CommandSlotLabel.createLabel(I18n.format("gui.commandEditor.playerSelector.exp.max"),
					Colors.playerSelectorLabel.color, maxExp));

			expSlots.addChild(new CommandSlotRectangle(insideBox, Colors.playerSelectorLabel.color));

			return expSlots;
		}

		private IGuiCommandSlot setupGamemodeSlot() {
			GameType[] gamemodes = GameType.values();
			String[] gamemodeNames = new String[gamemodes.length];
			String[] gamemodeIds = new String[gamemodeNames.length];
			for (int i = 0; i < gamemodes.length; i++) {
				if (gamemodes[i] == GameType.NOT_SET) {
					gamemodeNames[i] = I18n.format("gui.commandEditor.playerSelector.gamemode.any");
				} else {
					gamemodeNames[i] = I18n.format("gameMode." + gamemodes[i].getName());
				}
				gamemodeIds[i] = String.valueOf(gamemodes[i].getID());
			}

			gamemode = new CommandSlotMenu(gamemodeNames, gamemodeIds);

			return CommandSlotLabel.createLabel(I18n.format("gui.commandEditor.playerSelector.gamemode"),
					Colors.playerSelectorLabel.color, gamemode);
		}

		private IGuiCommandSlot setupScoresSlotWithLabel() {
			return CommandSlotLabel.createLabel(I18n.format("gui.commandEditor.playerSelector.score"),
					Colors.playerSelectorLabel.color, scoreTests);
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
			Map<String, String> specifiers = parseSpecifiersToMap(specifiersString);

			// Find the default count
			int c = selectorType.equals("p") || selectorType.equals("r") ? 1 : 0;
			// Overwrite with specified count
			if (specifiers.containsKey("c"))
				c = parseInt(specifiers.get("c"));

			if (c == 0) {
				// if c is 0, looking for ALL entities
				this.selectorType.setCurrentIndex(SELTYPE_ALL);
			} else if (selectorType.equals("r")) {
				// otherwise if @r, looking for random entity/ies
				this.selectorType.setCurrentIndex(SELTYPE_RANDOM);
			} else if (c < 0) {
				// otherwise if c < 0, looking for furthest entity/ies
				this.selectorType.setCurrentIndex(SELTYPE_FARTHEST);
			} else {
				// otherwise, looking for closest entity/ies
				this.selectorType.setCurrentIndex(SELTYPE_NEAREST);
			}

			// The target should default to not inverted
			if (this.targetInverted != null)
				this.targetInverted.setChecked(false);

			String targetType;
			if (selectorType.equals("p") || selectorType.equals("a")) {
				// @p or @a both imply we're searching for Player
				targetType = "Player";
			} else if (specifiers.containsKey("type")) {
				// There may be a specified target type
				targetType = specifiers.get("type");

				if (targetType.startsWith("!")) {
					// If the target type starts with !, it is inverted
					if (this.targetInverted != null)
						this.targetInverted.setChecked(true);
					targetType = targetType.substring(1);
				}

				if (targetType.isEmpty()) {
					// If nothing else is specified, it could be Anything
					targetType = ENTITY_ANYTHING;
				}
			} else if (selectorType.equals("r")) {
				// @r implies Player if type isn't specified
				targetType = "Player";
			} else {
				// If nothing is specified, it could be Anything
				targetType = ENTITY_ANYTHING;
			}
			if (this.targetEntity != null) {
				if ((flags & NON_PLAYERS_ONLY) != 0) {
					if (targetType.equals("Player")) {
						this.targetInverted.setChecked(false);
						targetType = ENTITY_ANYTHING;
					}
				}
				this.targetEntity.setEntity(targetType);
			}

			// Not expanded by default
			this.expand.setExpanded(false);

			// Eliminate possible negative value of c
			if (c < 0)
				c = -c;

			// Map c onto the count field if there is logically a count
			if (c != 0 && (flags & ONE_ONLY) == 0) {
				this.countField.setText(String.valueOf(c));
			}

			// Defaults for entity name and name inverted
			this.entityName.setText("");
			this.nameInverted.setChecked(false);

			if (specifiers.containsKey("name")) {
				// The name is set only if it is specified
				String name = specifiers.get("name");

				if (name.startsWith("!")) {
					// If name starts with !, it is inverted
					name = name.substring(1);
					this.nameInverted.setChecked(true);
				}

				this.entityName.setText(name);
			}

			// All positional fields default to 0
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
				// If all non-radius positional arguments are specified, we're
				// dealing with a known-coordinate bounding box
				this.positionalConstraints.setSelectedIndex(POSTYPE_BB_KNOWN_COORDS);
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
				// If any xyz distance argument is specified, we're dealing with
				// an unknown coordinate bounding box
				this.positionalConstraints.setSelectedIndex(POSTYPE_BB_UNKNOWN_COORDS);
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
				// Otherwise we're dealing with a radius
				this.positionalConstraints.setSelectedIndex(POSTYPE_RADIUS);
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

			// All rotational fields default to empty
			this.minHRotation.setText("");
			this.maxHRotation.setText("");
			this.minVRotation.setText("");
			this.maxVRotation.setText("");

			if (this.modifiableRotations.getChild() == this.rotations) {
				// If rotations is expanded, map the rotations onto the fields
				if (specifiers.containsKey("rym"))
					this.minHRotation.setText(
							String.valueOf((int) MathHelper.wrapAngleTo180_float(parseInt(specifiers.get("rym")))));
				if (specifiers.containsKey("ry"))
					this.maxHRotation.setText(
							String.valueOf((int) MathHelper.wrapAngleTo180_float(parseInt(specifiers.get("ry")))));
				if (specifiers.containsKey("rxm"))
					this.minVRotation.setText(
							String.valueOf((int) MathHelper.wrapAngleTo180_float(parseInt(specifiers.get("rxm")))));
				if (specifiers.containsKey("rx"))
					this.maxVRotation.setText(
							String.valueOf((int) MathHelper.wrapAngleTo180_float(parseInt(specifiers.get("rx")))));
			}

			// All experience fields default to empty
			this.minExp.setText("");
			this.maxExp.setText("");

			// Map the experience specifics to the fields
			if (specifiers.containsKey("lm"))
				this.minExp.setText(String.valueOf(Integer.parseInt(specifiers.get("lm"))));
			if (specifiers.containsKey("l"))
				this.maxExp.setText(String.valueOf(Integer.parseInt(specifiers.get("l"))));

			// The gamemode defaults to Any Mode
			this.gamemode.setCurrentIndex(GAMEMODE_ANY);
			if (specifiers.containsKey("m")) {
				// Map the gamemode to the correct value
				String gamemode = specifiers.get("m");
				for (int i = 0; i < this.gamemode.wordCount(); i++) {
					if (this.gamemode.getValueAt(i).equals(gamemode)) {
						this.gamemode.setCurrentIndex(i);
						break;
					}
				}
			}

			// By default, there is no waiting team name
			this.waitingTeamName = null;
			// By default, the team is not inverted
			this.teamInverted.setChecked(false);

			if (specifiers.containsKey("team")) {
				String teamName = specifiers.get("team");

				if (teamName.startsWith("!")) {
					// If the specified team name starts with !, it is inverted
					teamName = teamName.substring(1);
					this.teamInverted.setChecked(true);
				}

				// We now know the team name, so set the waiting team name
				this.waitingTeamName = teamName;

				boolean teamsHaveBeenObtained = this.teamName instanceof CommandSlotMenu;
				if (teamsHaveBeenObtained && this.modifiableTeam.getChild() == this.team) {
					CommandSlotMenu teamMenu = (CommandSlotMenu) this.teamName;

					// Default to any team
					teamMenu.setCurrentIndex(TEAM_ANY);
					if ("".equals(this.waitingTeamName)) {
						// If team name is specified but empty, it means no team
						teamMenu.setCurrentIndex(TEAM_NONE);
					} else {
						// Otherwise, find appropriate team
						for (int i = 2; i < teamMenu.wordCount(); i++) {
							if (teamMenu.getValueAt(i).equals(waitingTeamName)) {
								teamMenu.setCurrentIndex(i);
								break;
							}
						}
					}
				}
			}

			// Remove any previously existing score tests
			this.scoreTests.clearEntries();

			for (Map.Entry<String, String> specifier : specifiers.entrySet()) {
				boolean isScoreTest = specifier.getKey().startsWith("score_") && specifier.getKey().length() > 6;
				if (isScoreTest) {
					// Objective name is specifier key with "score_" removed
					String objective = specifier.getKey().substring(6);
					boolean min = false;
					if (objective.endsWith("_min")) {
						// If score test ends with "_min", it is a minimum
						// score, and "_min" is not included in the objective
						// name
						objective = objective.substring(objective.length() - 4, objective.length());
						min = true;
					}

					// Create a new entry for this score test
					CmdScoreTest scoreTest = this.scoreTests.newEntry();
					scoreTest.waitingObjective = objective;
					scoreTest.minOrMax.setCurrentIndex(min ? CmdScoreTest.TEST_MIN : CmdScoreTest.TEST_MAX);
					scoreTest.value.setText(String.valueOf(parseInt(specifier.getValue())));
					// Add the entry
					this.scoreTests.addEntry(scoreTest);
				}
			}

			// After all this, only one argument has been read. Eek!!
			return 1;
		}

		private Map<String, String> parseSpecifiersToMap(String specifiersString) throws CommandSyntaxException {
			Map<String, String> specifiers = Maps.newHashMap();
			// If there are no specifiers, that's it
			if (specifiersString == null)
				return specifiers;

			// Specifiers are separated with commas
			String[] specifierStrings = specifiersString.split(",");

			int argIndex;

			// First, deal with implied keys
			for (argIndex = 0; argIndex < specifierStrings.length; argIndex++) {
				// If there is an equals sign, there is a key, so this is not an
				// implied key
				if (specifierStrings[argIndex].contains("="))
					break;

				// Obtain the key from the index
				String key;
				switch (argIndex) {
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
				default:
					key = null;
				}

				// If there is a key and the value isn't empty, we have a
				// specifier
				if (key != null && !specifierStrings[argIndex].isEmpty())
					specifiers.put(key, specifierStrings[argIndex]);
			}

			// Next, deal with specifiers with explicit keys
			for (; argIndex < specifierStrings.length; argIndex++) {
				int equalsIndex = specifierStrings[argIndex].indexOf('=');

				// If there is no equals sign, we have a syntax error
				if (equalsIndex == -1)
					throw new CommandSyntaxException();

				// Split key and value at the equals sign
				specifiers.put(specifierStrings[argIndex].substring(0, equalsIndex),
						specifierStrings[argIndex].substring(equalsIndex + 1));
			}

			return specifiers;
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
			Map<String, String> specifiers = Maps.newLinkedHashMap();

			// Decide @p/@a/@r/@e
			if (this.selectorType.getCurrentIndex() == SELTYPE_RANDOM) {
				selectorType = "r";
			} else if (this.targetEntity != null
					&& (!this.targetEntity.getEntity().equals("Player") || this.targetInverted.isChecked())) {
				selectorType = "e";
			} else if (this.selectorType.getCurrentIndex() == SELTYPE_ALL && (flags & ONE_ONLY) == 0) {
				selectorType = "a";
			} else {
				selectorType = "p";
			}

			// x/y/z/dx/dy/dz/r/rm
			switch (this.positionalConstraints.getSelectedIndex()) {
			case POSTYPE_RADIUS:
				// Only specify them if the user specifies them
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
			case POSTYPE_BB_KNOWN_COORDS:
				// All of these must be specified by the user anyway
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
			case POSTYPE_BB_UNKNOWN_COORDS:
				// Only specify them if the user specifies them
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

			// Decide count
			int c;
			if ((flags & ONE_ONLY) != 0) {
				// This means there can only ever be one
				c = 1;
			} else if (this.selectorType.getCurrentIndex() == SELTYPE_ALL) {
				// All is coded as 0
				c = 0;
			} else {
				c = this.countField.getIntValue();
			}
			if (this.selectorType.getCurrentIndex() == SELTYPE_FARTHEST) {
				// Farthest is coded as a negative number
				c = -c;
			}
			boolean oneImplied = selectorType.equals("p") || selectorType.equals("r") || (flags & ONE_ONLY) != 0;
			boolean zeroImplied = selectorType.equals("a") || selectorType.equals("e");
			if ((oneImplied && c != 1) || (zeroImplied && c != 0)) {
				// If the value is not implied, specify it
				specifiers.put("c", String.valueOf(c));
			}

			// type
			if (this.targetEntity != null) {
				boolean targetImplied = this.targetEntity.getEntity().equals("Player")
						&& !this.targetInverted.isChecked();
				if (!targetImplied) {
					String targetType = targetEntity.getEntity();
					if (targetType.equals(ENTITY_ANYTHING)) {
						// Anything is still sometimes specified as an empty
						// string
						targetType = "";
					}

					if (this.targetInverted.isChecked()) {
						// Add ! if inverted
						targetType = "!" + targetType;
					}

					targetImplied = selectorType.equals("e") && targetType.isEmpty();
					if (!targetImplied) {
						specifiers.put("type", targetType);
					}
				}
			}

			// name
			if (!this.entityName.getText().isEmpty()) {
				String name = this.entityName.getText();
				if (this.nameInverted.isChecked()) {
					// Names are inverted by adding !
					name = "!" + name;
				}
				specifiers.put("name", name);
			}

			// rx/rxm/ry/rym
			if (this.modifiableRotations.getChild() == this.rotations) {
				if (!this.minHRotation.getText().isEmpty() && this.minHRotation.getIntValue() != -180)
					specifiers.put("rym", String.valueOf(this.minHRotation.getIntValue()));
				if (!this.maxHRotation.getText().isEmpty() && this.maxHRotation.getIntValue() != 179)
					specifiers.put("ry", String.valueOf(this.maxHRotation.getIntValue()));
				if (!this.minVRotation.getText().isEmpty() && this.minVRotation.getIntValue() != -180)
					specifiers.put("rxm", String.valueOf(this.minVRotation.getIntValue()));
				if (!this.maxVRotation.getText().isEmpty() && this.maxVRotation.getIntValue() != 179)
					specifiers.put("rx", String.valueOf(this.maxVRotation.getIntValue()));
			}

			if (this.modifiablePlayersOnlySlots.getChild() == this.playersOnlySlots) {
				// lm/l
				if (!this.minExp.getText().isEmpty() && this.minExp.getIntValue() != 0)
					specifiers.put("lm", String.valueOf(this.minExp.getIntValue()));
				if (!this.maxExp.getText().isEmpty())
					specifiers.put("l", String.valueOf(this.maxExp.getIntValue()));

				// m
				if (this.gamemode.getCurrentIndex() != GAMEMODE_ANY) {
					// Any gamemode doesn't have to be specified
					specifiers.put("m", this.gamemode.getCurrentValue());
				}
			}

			// team
			boolean teamsHaveBeenObtained = this.teamName instanceof CommandSlotMenu;
			if (teamsHaveBeenObtained && this.modifiableTeam.getChild() == this.team) {
				CommandSlotMenu teamMenu = (CommandSlotMenu) this.teamName;
				// Any team is not coded
				if (teamMenu.getCurrentIndex() != TEAM_ANY) {
					String team;

					if (teamMenu.getCurrentIndex() == TEAM_NONE) {
						// No team is coded as an empty string
						team = "";
					} else {
						team = teamMenu.getCurrentValue();
					}

					if (this.teamInverted.isChecked()) {
						// Inverted team is prefixed by !
						team = "!" + team;
					}

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
				if (scoreTest.minOrMax.getCurrentIndex() == CmdScoreTest.TEST_MIN)
					key += "_min";
				specifiers.put(key, String.valueOf(scoreTest.value.getIntValue()));
			}

			// Build final string
			args.add(buildSelector(selectorType, specifiers));
		}

		private String buildSelector(String selectorType, Map<String, String> specifiers) {
			StringBuilder builder = new StringBuilder("@").append(selectorType);

			if (!specifiers.isEmpty()) {
				builder.append("[");

				boolean appendCommaBefore = false;

				// This is used to ensure that shorthand args are strictly
				// shorter and clearer than their longhand equivalent
				int maxShorthandArg;
				if (specifiers.containsKey("r")) {
					if (specifiers.containsKey("y") || specifiers.containsKey("z")) {
						// X,Y,,R is shorter than X,Y,r=R use shorthand
						maxShorthandArg = 3;
					} else if (specifiers.containsKey("x")) {
						// X,r=R is the same length as X,,,R longhand is clearer
						maxShorthandArg = 0;
					} else {
						// ,,,R is longer than r=R use longhand
						maxShorthandArg = -1;
					}
				} else if (specifiers.containsKey("z")) {
					if (specifiers.containsKey("x") || specifiers.containsKey("y")) {
						// X,,Z is shorter than X,z=Z
						maxShorthandArg = 2;
					} else {
						// z=Z is the same length as ,,Z longhand is clearer
						maxShorthandArg = -1;
					}
				} else if (specifiers.containsKey("y")) {
					// ,Y is shorter than y=Y use shorthand
					maxShorthandArg = 1;
				} else if (specifiers.containsKey("x")) {
					// X is shorter than x=X use shorthand
					maxShorthandArg = 0;
				} else {
					// There are no shorthand args - shorthand is not possible
					maxShorthandArg = -1;
				}

				if (maxShorthandArg != -1) {
					// If we're using shorthand, parse shorthand up to
					// maxShorthandArg
					String[] keys = new String[] { "x", "y", "z", "r" };
					for (int i = 0; i <= maxShorthandArg; i++) {
						String key = keys[i];

						if (appendCommaBefore)
							builder.append(",");

						if (specifiers.containsKey(key))
							builder.append(specifiers.remove(key));

						appendCommaBefore = true;
					}
				}

				for (Map.Entry<String, String> specifier : specifiers.entrySet()) {
					if (appendCommaBefore)
						builder.append(",");

					builder.append(specifier.getKey()).append("=").append(specifier.getValue());

					appendCommaBefore = true;
				}

				builder.append("]");
			}
			return builder.toString();
		}

		public void checkValid() throws UIInvalidException {
			if (targetEntity != null) {
				targetEntity.checkValid();

				if (targetEntity.getEntity().equals(ENTITY_ANYTHING)) {
					if (selectorType.getCurrentIndex() == SELTYPE_RANDOM) {
						throw new UIInvalidException("gui.commandEditor.playerSelector.randomAnything");
					} else if (targetInverted.isChecked()) {
						throw new UIInvalidException("gui.commandEditor.notAny");
					}
				}
			}

			if (selectorType.getCurrentIndex() != SELTYPE_ALL && (flags & ONE_ONLY) == 0) {
				countField.checkValid();
			}

			switch (positionalConstraints.getSelectedIndex()) {
			case POSTYPE_RADIUS:
				if (!rOriginX.getText().isEmpty())
					rOriginX.checkValid();
				if (!rOriginY.getText().isEmpty())
					rOriginY.checkValid();
				if (!rOriginZ.getText().isEmpty())
					rOriginZ.checkValid();
				if (!minRadius.getText().isEmpty())
					minRadius.checkValid();
				if (!maxRadius.getText().isEmpty())
					maxRadius.checkValid();
				break;
			case POSTYPE_BB_KNOWN_COORDS:
				boundsX1.checkValid();
				boundsY1.checkValid();
				boundsZ1.checkValid();
				boundsX2.checkValid();
				boundsY2.checkValid();
				boundsZ2.checkValid();
				break;
			case POSTYPE_BB_UNKNOWN_COORDS:
				if (!dOriginX.getText().isEmpty())
					dOriginX.checkValid();
				if (!dOriginY.getText().isEmpty())
					dOriginY.checkValid();
				if (!dOriginZ.getText().isEmpty())
					dOriginZ.checkValid();
				if (!distX.getText().isEmpty())
					distX.checkValid();
				if (!distY.getText().isEmpty())
					distY.checkValid();
				if (!distZ.getText().isEmpty())
					distZ.checkValid();
				break;
			}

			if (modifiableRotations.getChild() == rotations) {
				if (!minHRotation.getText().isEmpty())
					minHRotation.checkValid();
				if (!maxHRotation.getText().isEmpty())
					maxHRotation.checkValid();
				if (!minVRotation.getText().isEmpty())
					minVRotation.checkValid();
				if (!maxVRotation.getText().isEmpty())
					maxVRotation.checkValid();
			}

			if (modifiablePlayersOnlySlots.getChild() == playersOnlySlots) {
				if (!minExp.getText().isEmpty())
					minExp.checkValid();
				if (!maxExp.getText().isEmpty())
					maxExp.checkValid();
			}

			boolean teamsHaveBeenObtained = teamName instanceof CommandSlotMenu;
			if (teamsHaveBeenObtained && modifiableTeamName.getChild() == teamName) {
				if (((CommandSlotMenu) teamName).getCurrentIndex() == TEAM_ANY && teamInverted.isChecked())
					throw new UIInvalidException("gui.commandEditor.notAny");
			}

			for (int i = 0; i < scoreTests.entryCount(); i++) {
				scoreTests.getEntry(i).checkValid();
			}
		}

		private class CmdScoreTest extends CommandSlotHorizontalArrangement {

			String waitingObjective;
			CommandSlotModifiable<IGuiCommandSlot> modifiableObjective;
			CommandSlotMenu objective;
			CommandSlotMenu minOrMax;
			CommandSlotIntTextField value;

			public static final int TEST_MIN = 0;
			public static final int TEST_MAX = 1;

			public CmdScoreTest() {
				modifiableObjective = new CommandSlotModifiable<IGuiCommandSlot>(
						new CommandSlotLabel(Minecraft.getMinecraft().fontRendererObj,
								I18n.format("gui.commandEditor.playerSelector.score.waiting"), 0x404040));
				addChild(modifiableObjective);

				minOrMax = new CommandSlotMenu(I18n.format("gui.commandEditor.playerSelector.score.min"),
						I18n.format("gui.commandEditor.playerSelector.score.max"));
				addChild(minOrMax);

				value = new CommandSlotIntTextField(30, 100);
				addChild(value);
				value.setText("0");
			}

			public void checkValid() throws UIInvalidException {
				if (objective == null) {
					if (waitingObjective == null)
						throw new UIInvalidException("gui.commandEditor.playerSelector.scoreTestInvalid.noObjective");
				} else if (objective.wordCount() == 0) {
					throw new UIInvalidException(
							"gui.commandEditor.playerSelector.scoreTestInvalid.noObjectivesInList");
				}

				value.checkValid();
			}

		}

	}

}

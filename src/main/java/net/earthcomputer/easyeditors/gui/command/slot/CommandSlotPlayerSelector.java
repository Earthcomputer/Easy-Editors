package net.earthcomputer.easyeditors.gui.command.slot;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;

import org.apache.commons.lang3.ArrayUtils;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.earthcomputer.easyeditors.api.util.ChatBlocker;
import net.earthcomputer.easyeditors.api.util.Colors;
import net.earthcomputer.easyeditors.api.util.Instantiator;
import net.earthcomputer.easyeditors.api.util.Patterns;
import net.earthcomputer.easyeditors.api.util.Predicates2;
import net.earthcomputer.easyeditors.api.util.ReturnedValueListener;
import net.earthcomputer.easyeditors.gui.GuiSelectEntity;
import net.earthcomputer.easyeditors.gui.command.CommandSyntaxException;
import net.earthcomputer.easyeditors.gui.command.UIInvalidException;
import net.earthcomputer.easyeditors.util.Translate;
import net.earthcomputer.easyeditors.util.TranslateKeys;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.GameType;

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

	private static final ResourceLocation ENTITY_ANYTHING = new ResourceLocation("anything");

	public CommandSlotPlayerSelector() {
		this(0);
	}

	public CommandSlotPlayerSelector(int flags) {
		this.flags = flags;

		addChild(CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_PLAYERSELECTOR_SELECTBY,
				Colors.playerSelectorSelectBy.color,
				new CommandSlotHelp(TranslateKeys.HELP_PLAYERSELECTORS, "player_selectors")));

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
			radioList.addChild(CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_PLAYERSELECTOR_USERNAME,
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
			radioList.addChild(CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_PLAYERSELECTOR_UUID,
					Colors.playerSelectorLabel.color, UUIDField));
		}

		if ((flags & DISALLOW_SELECTOR) == 0) {
			playerSelector = new CmdPlayerSelector();
			radioList.addChild(CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_PLAYERSELECTOR_SELECTOR,
					Colors.playerSelectorLabel.color, playerSelector));
		}

		addChild(radioList);
	}

	/**
	 * Gets the flags this player selector was constructed with
	 * 
	 * @return
	 */
	public int getFlags() {
		return flags;
	}

	/**
	 * Gets the internal radio list
	 * 
	 * @return
	 */
	public CommandSlotRadioList getRadioList() {
		return radioList;
	}

	// Hacky method to get player selector because Java is stupid
	private static CmdPlayerSelector getPlayerSelector(CommandSlotPlayerSelector selector) {
		return selector.playerSelector;
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
					throw new UIInvalidException(TranslateKeys.GUI_COMMANDEDITOR_PLAYERSELECTOR_NOUSERNAMETYPED);
				}
			}
			i++;
		}
		if ((flags & DISALLOW_UUID) == 0) {
			if (selectedIndex == i) {
				if (!Patterns.UUID.matcher(UUIDField.getText()).matches()) {
					throw new UIInvalidException(TranslateKeys.GUI_COMMANDEDITOR_PLAYERSELECTOR_INVALIDUUID);
				}
			}
			i++;
		}
		if ((flags & DISALLOW_SELECTOR) == 0) {
			if (selectedIndex == i)
				playerSelector.checkValid();
		}
	}

	public static class WithDefault extends CommandSlotPlayerSelector {
		private boolean optionalPlayersOnly = true;

		public WithDefault() {
		}

		public WithDefault(int flags) {
			super(flags);
		}

		public WithDefault(int flags, boolean optionalPlayersOnly) {
			super(flags);
			this.optionalPlayersOnly = optionalPlayersOnly;
			if ((flags & NON_PLAYERS_ONLY) == 0) {
				getRadioList().setSelectedIndex(2);
			} else {
				getRadioList().setSelectedIndex(2);
				getPlayerSelector(this).selectorType.setCurrentIndex(0);
				if ((flags & ONE_ONLY) != 0) {
					getPlayerSelector(this).countField.setText("1");
				}
			}
		}

		@Override
		public void addArgs(List<String> args) throws UIInvalidException {
			super.addArgs(args);
			if (getContext().getSender() instanceof EntityPlayer) {
				if ((getFlags() & NON_PLAYERS_ONLY) == 0) {
					String lastArg = args.get(args.size() - 1);
					boolean redundant = false;
					if (lastArg.equals("@p")) {
						redundant = true;
					} else {
						EntityPlayer player = (EntityPlayer) getContext().getSender();
						if (lastArg.equals(player.getName())) {
							redundant = true;
						} else if (Patterns.UUID.matcher(lastArg).matches()
								&& UUID.fromString(lastArg).equals(player.getUniqueID())) {
							redundant = true;
						}
					}
					if (redundant) {
						args.remove(args.size() - 1);
					}
				}
			} else if (getContext().getSender() instanceof Entity) {
				if ((getFlags() & PLAYERS_ONLY) == 0 && !optionalPlayersOnly) {
					String lastArg = args.get(args.size() - 1);
					boolean redundant = false;
					if (lastArg.equals("@e[c=1]")) {
						redundant = true;
					} else {
						Entity entity = (Entity) getContext().getSender();
						if (Patterns.UUID.matcher(lastArg).matches()
								&& UUID.fromString(lastArg).equals(entity.getUniqueID())) {
							redundant = true;
						}
					}
					if (redundant) {
						args.remove(args.size() - 1);
					}
				}
			}
		}

		@Override
		public int readFromArgs(String[] args, int index) throws CommandSyntaxException {
			if (isArgAbsent(args, index)) {
				if (getContext().getSender() instanceof EntityPlayer) {
					if ((getFlags() & NON_PLAYERS_ONLY) != 0) {
						throw new CommandSyntaxException();
					}
					super.readFromArgs(ArrayUtils.add(args, "@p"), index);
					return 0;
				} else if (getContext().getSender() instanceof Entity) {
					if ((getFlags() & PLAYERS_ONLY) != 0 || optionalPlayersOnly) {
						throw new CommandSyntaxException();
					}
					super.readFromArgs(ArrayUtils.add(args, "@e[c=1]"), index);
					return 0;
				} else {
					throw new CommandSyntaxException();
				}
			} else {
				return super.readFromArgs(args, index);
			}
		}

		protected boolean isArgAbsent(String[] args, int index) {
			return args.length == index;
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

		private IGuiCommandSlot count;
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

		private CommandSlotRadioList tag;
		private CommandSlotCheckbox tagNameInverted;
		private CommandSlotTextField tagName;

		private static final int TAG_ANY = 0;
		private static final int TAG_NONE = 1;
		private static final int TAG_SOME = 2;
		private static final int TAG_WITH_NAME = 3;

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
			teamInverted = new CommandSlotCheckbox(Translate.GUI_COMMANDEDITOR_PLAYERSELECTOR_TEAM_INVERTED);

			teamName = new CommandSlotLabel(Minecraft.getMinecraft().fontRendererObj,
					Translate.GUI_COMMANDEDITOR_PLAYERSELECTOR_TEAM_WAITING, 0x404040);
			modifiableTeamName = new CommandSlotModifiable<IGuiCommandSlot>(teamName);

			team = CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_PLAYERSELECTOR_TEAM,
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
					displayNames.add(Translate.GUI_COMMANDEDITOR_PLAYERSELECTOR_TEAM_ANY);
					displayNames.add(Translate.GUI_COMMANDEDITOR_PLAYERSELECTOR_TEAM_NONE);

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
					String errorMessage = reason == 0 ? TranslateKeys.GUI_COMMANDEDITOR_PLAYERSELECTOR_TEAM_TIMEDOUT
							: TranslateKeys.GUI_COMMANDEDITOR_PLAYERSELECTOR_TEAM_NOPERMISSION;
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

			}).setAppendHoverText(Translate.GUI_COMMANDEDITOR_PLAYERSELECTOR_SCORE_APPEND)
					.setInsertHoverText(Translate.GUI_COMMANDEDITOR_PLAYERSELECTOR_SCORE_INSERT)
					.setRemoveHoverText(Translate.GUI_COMMANDEDITOR_PLAYERSELECTOR_SCORE_REMOVE);
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
					objectiveErrorMessage = reason == 0 ? TranslateKeys.GUI_COMMANDEDITOR_PLAYERSELECTOR_SCORE_TIMEDOUT
							: TranslateKeys.GUI_COMMANDEDITOR_PLAYERSELECTOR_SCORE_NOPERMISSION;
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
			return selectorType = new CommandSlotMenu(Translate.GUI_COMMANDEDITOR_PLAYERSELECTOR_NEAREST,
					Translate.GUI_COMMANDEDITOR_PLAYERSELECTOR_FARTHEST, Translate.GUI_COMMANDEDITOR_PLAYERSELECTOR_ALL,
					Translate.GUI_COMMANDEDITOR_PLAYERSELECTOR_RANDOM) {

				@Override
				protected void onChanged(String to) {
					if ((flags & ONE_ONLY) == 0) {
						if (getCurrentIndex() == SELTYPE_ALL)
							modifiableCountField.setChild(null);
						else
							modifiableCountField.setChild(count);
					}
				}

			};
		}

		private IGuiCommandSlot setupTargetEntityInvertedSlot() {
			return targetInverted = new CommandSlotCheckbox(Translate.GUI_COMMANDEDITOR_PLAYERSELECTOR_TARGETINVERTED);
		}

		private IGuiCommandSlot setupTargetEntitySlot() {
			targetEntity = new CommandSlotEntity((flags & NON_PLAYERS_ONLY) == 0, false, ENTITY_ANYTHING) {

				@Override
				public void setEntity(ResourceLocation entityName) {
					super.setEntity(entityName);

					if (GuiSelectEntity.PLAYER.equals(entityName)) {
						modifiablePlayersOnlySlots.setChild(playersOnlySlots);
					} else {
						modifiablePlayersOnlySlots.setChild(null);
					}

					Class<? extends Entity> entityClass = EntityList.getClass(entityName);
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
				targetEntity.setEntity(GuiSelectEntity.PLAYER);
			else
				targetEntity.setEntity(ENTITY_ANYTHING);

			return targetEntity;
		}

		private IGuiCommandSlot setupPlayersOnlyTargetEntitySlot() {
			return new CommandSlotLabel(Minecraft.getMinecraft().fontRendererObj,
					GuiSelectEntity.getEntityName(GuiSelectEntity.PLAYER), 0);
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
			specifics.addChild(setupTagSlot());

			expand = new CommandSlotExpand(specifics);
			addChild(expand);
		}

		private IGuiCommandSlot setupCountSlot() {
			countField = new CommandSlotIntTextField(50, 50, 1);
			countField.setNumberInvalidMessage(TranslateKeys.GUI_COMMANDEDITOR_PLAYERSELECTOR_COUNT_INVALID)
					.setOutOfBoundsMessage(TranslateKeys.GUI_COMMANDEDITOR_PLAYERSELECTOR_COUNT_OUTOFBOUNDS);
			count = CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_PLAYERSELECTOR_COUNT,
					Colors.playerSelectorLabel.color, countField);
			modifiableCountField = new CommandSlotModifiable<IGuiCommandSlot>(count);

			countField.setText("1");

			return modifiableCountField;
		}

		private IGuiCommandSlot setupNameSlot() {
			nameInverted = new CommandSlotCheckbox(Translate.GUI_COMMANDEDITOR_PLAYERSELECTOR_ENTITYNAMEINVERTED);
			entityName = new CommandSlotTextField(200, 200);
			IGuiCommandSlot r = new CommandSlotHorizontalArrangement(
					new CommandSlotLabel(Minecraft.getMinecraft().fontRendererObj,
							Translate.GUI_COMMANDEDITOR_PLAYERSELECTOR_ENTITYNAME, Colors.playerSelectorLabel.color),
					nameInverted, entityName);

			entityName.setContentFilter(Predicates2.<String>matchingPattern(Patterns.partialPlayerName));

			return r;
		}

		private IGuiCommandSlot setupPositionalConstraintsSlot() {
			CommandSlotVerticalArrangement wholeColumn = new CommandSlotVerticalArrangement();

			wholeColumn.addChild(new CommandSlotLabel(Minecraft.getMinecraft().fontRendererObj,
					Translate.GUI_COMMANDEDITOR_PLAYERSELECTOR_POSITIONALCONSTRAINTS,
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
			rOriginX.setNumberInvalidMessage(TranslateKeys.GUI_COMMANDEDITOR_PLAYERSELECTOR_RADIUS_ORIGIN_XINVALID)
					.setOutOfBoundsMessage(TranslateKeys.GUI_COMMANDEDITOR_PLAYERSELECTOR_RADIUS_ORIGIN_XOUTOFBOUNDS);
			rOriginY = (CommandSlotIntTextField) xyz[2];
			rOriginY.setNumberInvalidMessage(TranslateKeys.GUI_COMMANDEDITOR_PLAYERSELECTOR_RADIUS_ORIGIN_YINVALID)
					.setOutOfBoundsMessage(TranslateKeys.GUI_COMMANDEDITOR_PLAYERSELECTOR_RADIUS_ORIGIN_YOUTOFBOUNDS);
			rOriginZ = (CommandSlotIntTextField) xyz[3];
			rOriginZ.setNumberInvalidMessage(TranslateKeys.GUI_COMMANDEDITOR_PLAYERSELECTOR_RADIUS_ORIGIN_ZINVALID)
					.setOutOfBoundsMessage(TranslateKeys.GUI_COMMANDEDITOR_PLAYERSELECTOR_RADIUS_ORIGIN_ZOUTOFBOUNDS);
			radiusConstraint
					.addChild(CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_PLAYERSELECTOR_RADIUS_ORIGIN,
							Colors.playerSelectorLabel.color, xyz[0]));

			radiusConstraint.addChild(setupMinRadiusConstraint());
			radiusConstraint.addChild(setupMaxRadiusConstraint());

			return CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_PLAYERSELECTOR_RADIUS,
					Colors.playerSelectorLabel.color, radiusConstraint);
		}

		private IGuiCommandSlot setupMinRadiusConstraint() {
			minRadius = new CommandSlotIntTextField(30, 100, 0);
			minRadius.setNumberInvalidMessage(TranslateKeys.GUI_COMMANDEDITOR_PLAYERSELECTOR_RADIUS_MIN_INVALID)
					.setOutOfBoundsMessage(TranslateKeys.GUI_COMMANDEDITOR_PLAYERSELECTOR_RADIUS_MIN_OUTOFBOUNDS);
			return CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_PLAYERSELECTOR_RADIUS_MIN,
					Colors.playerSelectorLabel.color, minRadius);
		}

		private IGuiCommandSlot setupMaxRadiusConstraint() {
			maxRadius = new CommandSlotIntTextField(30, 100, 0);
			maxRadius.setNumberInvalidMessage(TranslateKeys.GUI_COMMANDEDITOR_PLAYERSELECTOR_RADIUS_MAX_INVALID)
					.setOutOfBoundsMessage(TranslateKeys.GUI_COMMANDEDITOR_PLAYERSELECTOR_RADIUS_MAX_OUTOFBOUNDS);
			return CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_PLAYERSELECTOR_RADIUS_MAX,
					Colors.playerSelectorLabel.color, maxRadius);
		}

		private IGuiCommandSlot setupBoundsFromToConstraint() {
			CommandSlotVerticalArrangement fromToConstraint = new CommandSlotVerticalArrangement();

			IGuiCommandSlot[] xyz = setupXYZConstraint();
			boundsX1 = (CommandSlotIntTextField) xyz[1];
			boundsX1.setNumberInvalidMessage(TranslateKeys.GUI_COMMANDEDITOR_PLAYERSELECTOR_BOUNDSFROMTO_FROM_XINVALID)
					.setOutOfBoundsMessage(
							TranslateKeys.GUI_COMMANDEDITOR_PLAYERSELECTOR_BOUNDSFROMTO_FROM_XOUTOFBOUNDS);
			boundsY1 = (CommandSlotIntTextField) xyz[2];
			boundsY1.setNumberInvalidMessage(TranslateKeys.GUI_COMMANDEDITOR_PLAYERSELECTOR_BOUNDSFROMTO_FROM_YINVALID)
					.setOutOfBoundsMessage(
							TranslateKeys.GUI_COMMANDEDITOR_PLAYERSELECTOR_BOUNDSFROMTO_FROM_YOUTOFBOUNDS);
			boundsZ1 = (CommandSlotIntTextField) xyz[3];
			boundsZ1.setNumberInvalidMessage(TranslateKeys.GUI_COMMANDEDITOR_PLAYERSELECTOR_BOUNDSFROMTO_FROM_ZINVALID)
					.setOutOfBoundsMessage(
							TranslateKeys.GUI_COMMANDEDITOR_PLAYERSELECTOR_BOUNDSFROMTO_FROM_ZOUTOFBOUNDS);
			fromToConstraint
					.addChild(CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_PLAYERSELECTOR_BOUNDSFROMTO_FROM,
							Colors.playerSelectorLabel.color, xyz[0]));

			xyz = setupXYZConstraint();
			boundsX2 = (CommandSlotIntTextField) xyz[1];
			boundsX2.setNumberInvalidMessage(TranslateKeys.GUI_COMMANDEDITOR_PLAYERSELECTOR_BOUNDSFROMTO_TO_XINVALID)
					.setOutOfBoundsMessage(TranslateKeys.GUI_COMMANDEDITOR_PLAYERSELECTOR_BOUNDSFROMTO_TO_XOUTOFBOUNDS);
			boundsY2 = (CommandSlotIntTextField) xyz[2];
			boundsY2.setNumberInvalidMessage(TranslateKeys.GUI_COMMANDEDITOR_PLAYERSELECTOR_BOUNDSFROMTO_TO_YINVALID)
					.setOutOfBoundsMessage(TranslateKeys.GUI_COMMANDEDITOR_PLAYERSELECTOR_BOUNDSFROMTO_TO_YOUTOFBOUNDS);
			boundsZ2 = (CommandSlotIntTextField) xyz[3];
			boundsZ2.setNumberInvalidMessage(TranslateKeys.GUI_COMMANDEDITOR_PLAYERSELECTOR_BOUNDSFROMTO_TO_ZINVALID)
					.setOutOfBoundsMessage(TranslateKeys.GUI_COMMANDEDITOR_PLAYERSELECTOR_BOUNDSFROMTO_TO_ZOUTOFBOUNDS);
			fromToConstraint
					.addChild(CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_PLAYERSELECTOR_BOUNDSFROMTO_TO,
							Colors.playerSelectorLabel.color, xyz[0]));

			return CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_PLAYERSELECTOR_BOUNDSFROMTO,
					Colors.playerSelectorLabel.color, fromToConstraint);
		}

		private IGuiCommandSlot setupBoundsDistConstraint() {
			CommandSlotVerticalArrangement boundsDistConstraint = new CommandSlotVerticalArrangement();

			IGuiCommandSlot[] xyz = setupXYZConstraint();
			dOriginX = (CommandSlotIntTextField) xyz[1];
			dOriginX.setNumberInvalidMessage(TranslateKeys.GUI_COMMANDEDITOR_PLAYERSELECTOR_BOUNDSDIST_ORIGIN_XINVALID)
					.setOutOfBoundsMessage(
							TranslateKeys.GUI_COMMANDEDITOR_PLAYERSELECTOR_BOUNDSDIST_ORIGIN_XOUTOFBOUNDS);
			dOriginY = (CommandSlotIntTextField) xyz[2];
			dOriginY.setNumberInvalidMessage(TranslateKeys.GUI_COMMANDEDITOR_PLAYERSELECTOR_BOUNDSDIST_ORIGIN_YINVALID)
					.setOutOfBoundsMessage(
							TranslateKeys.GUI_COMMANDEDITOR_PLAYERSELECTOR_BOUNDSDIST_ORIGIN_YOUTOFBOUNDS);
			dOriginZ = (CommandSlotIntTextField) xyz[3];
			dOriginZ.setNumberInvalidMessage(TranslateKeys.GUI_COMMANDEDITOR_PLAYERSELECTOR_BOUNDSDIST_ORIGIN_ZINVALID)
					.setOutOfBoundsMessage(
							TranslateKeys.GUI_COMMANDEDITOR_PLAYERSELECTOR_BOUNDSDIST_ORIGIN_ZOUTOFBOUNDS);
			boundsDistConstraint
					.addChild(CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_PLAYERSELECTOR_BOUNDSDIST_ORIGIN,
							Colors.playerSelectorLabel.color, xyz[0]));

			xyz = setupXYZConstraint();
			distX = (CommandSlotIntTextField) xyz[1];
			distX.setNumberInvalidMessage(TranslateKeys.GUI_COMMANDEDITOR_PLAYERSELECTOR_BOUNDSDIST_DISTANCE_XINVALID)
					.setOutOfBoundsMessage(
							TranslateKeys.GUI_COMMANDEDITOR_PLAYERSELECTOR_BOUNDSDIST_DISTANCE_XOUTOFBOUNDS);
			distY = (CommandSlotIntTextField) xyz[2];
			distY.setNumberInvalidMessage(TranslateKeys.GUI_COMMANDEDITOR_PLAYERSELECTOR_BOUNDSDIST_DISTANCE_YINVALID)
					.setOutOfBoundsMessage(
							TranslateKeys.GUI_COMMANDEDITOR_PLAYERSELECTOR_BOUNDSDIST_DISTANCE_YOUTOFBOUNDS);
			distZ = (CommandSlotIntTextField) xyz[3];
			distZ.setNumberInvalidMessage(TranslateKeys.GUI_COMMANDEDITOR_PLAYERSELECTOR_BOUNDSDIST_DISTANCE_ZINVALID)
					.setOutOfBoundsMessage(
							TranslateKeys.GUI_COMMANDEDITOR_PLAYERSELECTOR_BOUNDSDIST_DISTANCE_ZOUTOFBOUNDS);
			boundsDistConstraint.addChild(
					CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_PLAYERSELECTOR_BOUNDSDIST_DISTANCE,
							Colors.playerSelectorLabel.color, xyz[0]));

			return CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_PLAYERSELECTOR_BOUNDSDIST,
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
					Translate.GUI_COMMANDEDITOR_PLAYERSELECTOR_ROTATIONS, Colors.playerSelectorLabel.color));

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
			return CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_PLAYERSELECTOR_ROTATIONS_HORIZONTAL,
					Colors.playerSelectorLabel.color, component);
		}

		private IGuiCommandSlot setupMinHRotationSlot() {
			minHRotation = new CommandSlotIntTextField(30, 100, -180, 179);
			minHRotation
					.setNumberInvalidMessage(
							TranslateKeys.GUI_COMMANDEDITOR_PLAYERSELECTOR_ROTATIONS_HORIZONTAL_MININVALID)
					.setOutOfBoundsMessage(
							TranslateKeys.GUI_COMMANDEDITOR_PLAYERSELECTOR_ROTATIONS_HORIZONTAL_MINOUTOFBOUNDS);
			return CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_PLAYERSELECTOR_ROTATIONS_MIN,
					Colors.playerSelectorLabel.color, minHRotation);
		}

		private IGuiCommandSlot setupMaxHRotationSlot() {
			maxHRotation = new CommandSlotIntTextField(30, 100, -180, 179);
			maxHRotation
					.setNumberInvalidMessage(
							TranslateKeys.GUI_COMMANDEDITOR_PLAYERSELECTOR_ROTATIONS_HORIZONTAL_MAXINVALID)
					.setOutOfBoundsMessage(
							TranslateKeys.GUI_COMMANDEDITOR_PLAYERSELECTOR_ROTATIONS_HORIZONTAL_MAXOUTOFBOUNDS);
			return CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_PLAYERSELECTOR_ROTATIONS_MAX,
					Colors.playerSelectorLabel.color, maxHRotation);
		}

		private IGuiCommandSlot setupVerticalRotationSlot() {
			CommandSlotVerticalArrangement insideBox = new CommandSlotVerticalArrangement();

			insideBox.addChild(setupMinVRotationSlot());
			insideBox.addChild(setupMaxVRotationSlot());

			IGuiCommandSlot component = new CommandSlotRectangle(insideBox, Colors.playerSelectorLabel.color);
			return CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_PLAYERSELECTOR_ROTATIONS_VERTICAL,
					Colors.playerSelectorLabel.color, component);
		}

		private IGuiCommandSlot setupMinVRotationSlot() {
			minVRotation = new CommandSlotIntTextField(30, 100, -180, 179);
			minVRotation
					.setNumberInvalidMessage(
							TranslateKeys.GUI_COMMANDEDITOR_PLAYERSELECTOR_ROTATIONS_VERTICAL_MININVALID)
					.setOutOfBoundsMessage(
							TranslateKeys.GUI_COMMANDEDITOR_PLAYERSELECTOR_ROTATIONS_VERTICAL_MINOUTOFBOUNDS);
			return CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_PLAYERSELECTOR_ROTATIONS_MIN,
					Colors.playerSelectorLabel.color, minVRotation);
		}

		private IGuiCommandSlot setupMaxVRotationSlot() {
			maxVRotation = new CommandSlotIntTextField(30, 100, -180, 179);
			maxVRotation
					.setNumberInvalidMessage(
							TranslateKeys.GUI_COMMANDEDITOR_PLAYERSELECTOR_ROTATIONS_VERTICAL_MAXINVALID)
					.setOutOfBoundsMessage(
							TranslateKeys.GUI_COMMANDEDITOR_PLAYERSELECTOR_ROTATIONS_VERTICAL_MAXOUTOFBOUNDS);
			return CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_PLAYERSELECTOR_ROTATIONS_MAX,
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
					Translate.GUI_COMMANDEDITOR_PLAYERSELECTOR_EXP, Colors.playerSelectorLabel.color));

			CommandSlotVerticalArrangement insideBox = new CommandSlotVerticalArrangement();
			minExp = new CommandSlotIntTextField(30, 100, 0);
			minExp.setNumberInvalidMessage(TranslateKeys.GUI_COMMANDEDITOR_PLAYERSELECTOR_EXP_MIN_INVALID)
					.setOutOfBoundsMessage(TranslateKeys.GUI_COMMANDEDITOR_PLAYERSELECTOR_EXP_MIN_OUTOFBOUNDS);
			insideBox.addChild(CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_PLAYERSELECTOR_EXP_MIN,
					Colors.playerSelectorLabel.color, minExp));
			maxExp = new CommandSlotIntTextField(30, 100, 0);
			maxExp.setNumberInvalidMessage(TranslateKeys.GUI_COMMANDEDITOR_PLAYERSELECTOR_EXP_MAX_INVALID)
					.setOutOfBoundsMessage(TranslateKeys.GUI_COMMANDEDITOR_PLAYERSELECTOR_EXP_MAX_OUTOFBOUNDS);
			insideBox.addChild(CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_PLAYERSELECTOR_EXP_MAX,
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
					gamemodeNames[i] = Translate.GUI_COMMANDEDITOR_PLAYERSELECTOR_GAMEMODE_ANY;
				} else {
					gamemodeNames[i] = I18n.format("gameMode." + gamemodes[i].getName());
				}
				gamemodeIds[i] = String.valueOf(gamemodes[i].getID());
			}

			gamemode = new CommandSlotMenu(gamemodeNames, gamemodeIds);

			return CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_PLAYERSELECTOR_GAMEMODE,
					Colors.playerSelectorLabel.color, gamemode);
		}

		private IGuiCommandSlot setupScoresSlotWithLabel() {
			return CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_PLAYERSELECTOR_SCORE,
					Colors.playerSelectorLabel.color, scoreTests);
		}

		private IGuiCommandSlot setupTagSlot() {
			CommandSlotVerticalArrangement tagSlots = new CommandSlotVerticalArrangement();
			tagSlots.addChild(CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_PLAYERSELECTOR_TAG,
					Colors.playerSelectorLabel.color));

			tagName = new CommandSlotTextField(200, 200);
			tagName.setContentFilter(Predicates2.<String>matchingPattern(Patterns.partialPlayerName));
			tagNameInverted = new CommandSlotCheckbox(Translate.GUI_COMMANDEDITOR_PLAYERSELECTOR_TAG_INVERTED);
			IGuiCommandSlot tagNameWithLabel = CommandSlotLabel.createLabel(
					Translate.GUI_COMMANDEDITOR_PLAYERSELECTOR_TAG_NAME, Colors.playerSelectorLabel.color,
					tagNameInverted, tagName);
			tag = new CommandSlotRadioList(
					CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_PLAYERSELECTOR_TAG_ANY,
							Colors.playerSelectorLabel.color),
					CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_PLAYERSELECTOR_TAG_NONE,
							Colors.playerSelectorLabel.color),
					CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_PLAYERSELECTOR_TAG_SOME,
							Colors.playerSelectorLabel.color),
					tagNameWithLabel) {
				@Override
				protected int getSelectedIndexForString(String[] args, int index) throws CommandSyntaxException {
					assert false;
					return 0;
				}
			};
			tagSlots.addChild(new CommandSlotRectangle(tag, Colors.playerSelectorLabel.color));
			return tagSlots;
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

			ResourceLocation targetType;
			if (selectorType.equals("p") || selectorType.equals("a")) {
				// @p or @a both imply we're searching for Player
				targetType = GuiSelectEntity.PLAYER;
			} else if (specifiers.containsKey("type")) {
				// There may be a specified target type
				String strType = specifiers.get("type");

				if (strType.startsWith("!")) {
					// If the target type starts with !, it is inverted
					if (this.targetInverted != null)
						this.targetInverted.setChecked(true);
					strType = strType.substring(1);
				}

				if (strType.isEmpty()) {
					// If nothing else is specified, it could be Anything
					targetType = ENTITY_ANYTHING;
				} else {
					targetType = new ResourceLocation(strType);
				}
			} else if (selectorType.equals("r")) {
				// @r implies Player if type isn't specified
				targetType = GuiSelectEntity.PLAYER;
			} else {
				// If nothing is specified, it could be Anything
				targetType = ENTITY_ANYTHING;
			}
			if (this.targetEntity != null) {
				if ((flags & NON_PLAYERS_ONLY) != 0) {
					if (targetType.equals("player")) {
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
					this.minHRotation
							.setText(String.valueOf((int) MathHelper.wrapDegrees(parseInt(specifiers.get("rym")))));
				if (specifiers.containsKey("ry"))
					this.maxHRotation
							.setText(String.valueOf((int) MathHelper.wrapDegrees(parseInt(specifiers.get("ry")))));
				if (specifiers.containsKey("rxm"))
					this.minVRotation
							.setText(String.valueOf((int) MathHelper.wrapDegrees(parseInt(specifiers.get("rxm")))));
				if (specifiers.containsKey("rx"))
					this.maxVRotation
							.setText(String.valueOf((int) MathHelper.wrapDegrees(parseInt(specifiers.get("rx")))));
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
				GameType gameType;
				try {
					gameType = GameType.parseGameTypeWithDefault(Integer.parseInt(gamemode), GameType.NOT_SET);
				} catch (NumberFormatException e) {
					gameType = GameType.parseGameTypeWithDefault(gamemode, GameType.NOT_SET);
				}
				gamemode = String.valueOf(gameType.getID());
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

			// By default, tag is set to TAG_ANY
			this.tag.setSelectedIndex(TAG_ANY);
			this.tagName.setText("");
			this.tagNameInverted.setChecked(false);

			if (specifiers.containsKey("tag")) {
				String tagSpecifier = specifiers.get("tag");

				boolean inverted = tagSpecifier.startsWith("!");
				if (inverted) {
					tagSpecifier = tagSpecifier.substring(1);
				}

				if (tagSpecifier.isEmpty()) {
					if (inverted) {
						this.tag.setSelectedIndex(TAG_SOME);
					} else {
						this.tag.setSelectedIndex(TAG_NONE);
					}
				} else {
					this.tag.setSelectedIndex(TAG_WITH_NAME);
					this.tagName.setText(tagSpecifier);
					this.tagNameInverted.setChecked(inverted);
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
			} else if (this.targetEntity != null && (!this.targetEntity.getEntity().equals(GuiSelectEntity.PLAYER)
					|| this.targetInverted.isChecked())) {
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
				boolean targetImplied = this.targetEntity.getEntity().equals(GuiSelectEntity.PLAYER)
						&& !this.targetInverted.isChecked();
				if (!targetImplied) {
					ResourceLocation targetTypeLocation = targetEntity.getEntity();
					String targetType = targetTypeLocation.getResourceDomain().equals("minecraft")
							? targetTypeLocation.getResourcePath() : targetTypeLocation.toString();
					if (targetTypeLocation.equals(ENTITY_ANYTHING)) {
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

			// tag
			switch (this.tag.getSelectedIndex()) {
			case TAG_WITH_NAME: {
				String tag = this.tagName.getText();
				if (this.tagNameInverted.isChecked()) {
					tag = "!" + tag;
				}
				specifiers.put("tag", tag);
				break;
			}
			case TAG_NONE:
				specifiers.put("tag", "");
				break;
			case TAG_SOME:
				specifiers.put("tag", "!");
				break;
			case TAG_ANY:
				break;
			default:
				throw new IllegalStateException("tag had illegal selected index");
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
						throw new UIInvalidException(TranslateKeys.GUI_COMMANDEDITOR_PLAYERSELECTOR_RANDOMANYTHING);
					} else if (targetInverted.isChecked()) {
						throw new UIInvalidException(TranslateKeys.GUI_COMMANDEDITOR_NOTANY);
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
					throw new UIInvalidException(TranslateKeys.GUI_COMMANDEDITOR_NOTANY);
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
								Translate.GUI_COMMANDEDITOR_PLAYERSELECTOR_SCORE_WAITING, 0x404040));
				addChild(modifiableObjective);

				minOrMax = new CommandSlotMenu(Translate.GUI_COMMANDEDITOR_PLAYERSELECTOR_SCORE_MIN,
						Translate.GUI_COMMANDEDITOR_PLAYERSELECTOR_SCORE_MAX);
				addChild(minOrMax);

				value = new CommandSlotIntTextField(30, 100);
				addChild(value);
				value.setText("0");
			}

			public void checkValid() throws UIInvalidException {
				if (objective == null) {
					if (waitingObjective == null)
						throw new UIInvalidException(
								TranslateKeys.GUI_COMMANDEDITOR_PLAYERSELECTOR_SCORETESTINVALID_NOOBJECTIVE);
				} else if (objective.wordCount() == 0) {
					throw new UIInvalidException(
							TranslateKeys.GUI_COMMANDEDITOR_PLAYERSELECTOR_SCORETESTINVALID_NOOBJECTIVESINLIST);
				}

				value.checkValid();
			}

		}

	}

}

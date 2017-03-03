package net.earthcomputer.easyeditors.api.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer.EnumChatVisibility;
import net.minecraft.network.play.client.CPacketChatMessage;
import net.minecraft.scoreboard.IScoreCriteria;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.GameRules;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * A class that has the capability of listening to and blocking messages in chat
 * client-side.
 * 
 * <b>This class is a member of the Easy Editors API</b>
 * 
 * @author Earthcomputer
 *
 */
public class ChatBlocker {

	private static final ChatBlocker instance = new ChatBlocker();

	/**
	 * Represents obtainDataFrom in the config
	 */
	public static boolean obtainDataFromServer = false;

	private List<Blocker> blocks = Lists.newArrayList();

	private ChatBlocker() {
		MinecraftForge.EVENT_BUS.register(this);
	}

	/**
	 * @return The singleton instance of ChatBlocker
	 */
	public static ChatBlocker getInstance() {
		return instance;
	}

	/**
	 * Executes the given command server side. The client will recieve results
	 * 
	 * @param command
	 */
	public static void executeCommand(String command) {
		executeCommand(command, true);
	}

	/**
	 * Executes the given command on the server side.
	 * 
	 * @param command
	 * @param recieveResults
	 *            - whether the client should recieve the results of the command
	 *            (in the form of a chat message). May temporarily modify the
	 *            client's game settings. Relies on the game rule
	 *            sendCommandFeedback = true
	 */
	public static void executeCommand(String command, boolean recieveResults) {
		if (!command.startsWith("/"))
			command = "/" + command;
		Minecraft mc = Minecraft.getMinecraft();
		boolean hidden = false;
		if (recieveResults && mc.gameSettings.chatVisibility == EnumChatVisibility.HIDDEN) {
			hidden = true;
			mc.gameSettings.chatVisibility = EnumChatVisibility.SYSTEM;
			mc.gameSettings.sendSettingsToServer();
		}
		mc.getConnection().sendPacket(new CPacketChatMessage(command));
		if (hidden) {
			mc.gameSettings.chatVisibility = EnumChatVisibility.HIDDEN;
			mc.gameSettings.sendSettingsToServer();
		}
	}

	/**
	 * Blocks the next {@link ChatComponentTranslation} with the given key
	 * 
	 * @param key
	 */
	public static void blockNextTranslation(final String key) {
		addBlock(new Blocker() {
			private boolean done = false;

			@Override
			public boolean isDone() {
				return done;
			}

			@Override
			public boolean accept(ITextComponent chat) {
				if (!(chat instanceof TextComponentTranslation))
					return true;
				TextComponentTranslation translation = (TextComponentTranslation) chat;
				if (translation.getKey().equals(key)) {
					done = true;
					return false;
				} else {
					return true;
				}
			}
		});
	}

	/**
	 * Adds a new {@link Blocker} to the ChatBlocker
	 * 
	 * @param block
	 */
	public static void addBlock(Blocker block) {
		instance.blocks.add(block);
	}

	/**
	 * Obtains a list of registered teams from somewhere
	 * 
	 * @param teams
	 *            - the listener for obtaining the list of teams. The meaning of
	 *            the abort codes are as follows:<br/>
	 *            0: Timed out<br/>
	 *            1: No permission
	 */
	public static void obtainTeamsList(final ReturnedValueListener<List<ScorePlayerTeam>> teams) {
		if (obtainDataFromServer && !Minecraft.getMinecraft().isIntegratedServerRunning()) {
			addBlock(new Blocker() {
				private boolean aborted = false;
				private long lastActivity = System.nanoTime();

				private int amtToRecieve = -1;
				private List<ScorePlayerTeam> valuesFound = Lists.newArrayList();

				@Override
				public boolean isDone() {
					if (System.nanoTime() > lastActivity + 10000000000L)
						teams.abortFindingValue(0);
					else if (valuesFound.size() < amtToRecieve)
						return false;
					if (!aborted)
						teams.returnValue(valuesFound);
					return true;
				}

				@Override
				public boolean accept(ITextComponent chat) {
					if (chat instanceof TextComponentTranslation) {
						TextComponentTranslation translation = (TextComponentTranslation) chat;
						if (translation.getKey().equals("commands.generic.permission")) {
							teams.abortFindingValue(1);
							aborted = true;
							amtToRecieve = valuesFound.size();
							return false;
						} else if (translation.getKey().equals("commands.scoreboard.teams.list.empty")) {
							valuesFound.clear();
							amtToRecieve = 0;
							lastActivity = System.nanoTime();
							return false;
						} else if (translation.getKey().equals("commands.scoreboard.teams.list.count")) {
							if (translation.getFormatArgs().length != 0) {
								Object formatArgObj = translation.getFormatArgs()[0];
								String formatArgStr;
								if (formatArgObj instanceof ITextComponent)
									formatArgStr = ((ITextComponent) formatArgObj).getUnformattedText();
								else
									formatArgStr = formatArgObj.toString();
								valuesFound.clear();
								try {
									amtToRecieve = Integer.parseInt(formatArgStr);
								} catch (NumberFormatException e) {
									amtToRecieve = 0;
								}
								lastActivity = System.nanoTime();
							}
							return false;
						} else if (translation.getKey().equals("commands.scoreboard.teams.list.entry")) {
							Object[] formatArgs = translation.getFormatArgs();
							if (formatArgs.length >= 2) {
								Object formatArgObj = formatArgs[0];
								String internalTeamName, displayTeamName;
								if (formatArgObj instanceof ITextComponent)
									internalTeamName = ((ITextComponent) formatArgObj).getUnformattedText();
								else
									internalTeamName = formatArgObj.toString();
								formatArgObj = formatArgs[1];
								if (formatArgObj instanceof ITextComponent)
									displayTeamName = ((ITextComponent) formatArgObj).getUnformattedText();
								else
									displayTeamName = formatArgObj.toString();
								ScorePlayerTeam team = new ScorePlayerTeam(null, internalTeamName);
								team.setTeamName(displayTeamName);
								valuesFound.add(team);
								lastActivity = System.nanoTime();
							}
							return false;
						} else {
							return true;
						}
					} else {
						return true;
					}
				}
			});
			executeCommand("/scoreboard teams list");
		} else {
			Collection<ScorePlayerTeam> teamsCollection = Minecraft.getMinecraft().world.getScoreboard().getTeams();
			teams.returnValue(Lists.newArrayList(teamsCollection));
		}
	}

	/**
	 * Obtains a list of registered objectives from somewhere
	 * 
	 * @param objectives
	 *            - the listener for obtaining the list of objectives. The
	 *            meaning of the abort codes are as follows:<br/>
	 *            0: Timed out<br/>
	 *            1: No permission
	 */
	public static void obtainObjectiveList(final ReturnedValueListener<List<ScoreObjective>> objectives) {
		if (!Minecraft.getMinecraft().isIntegratedServerRunning()) {
			addBlock(new Blocker() {
				private boolean aborted = false;
				private long lastActivity = System.nanoTime();

				private int amtToRecieve = -1;
				private List<ScoreObjective> valuesFound = Lists.newArrayList();

				@Override
				public boolean isDone() {
					if (System.nanoTime() > lastActivity + 10000000000L)
						objectives.abortFindingValue(0);
					else if (valuesFound.size() < amtToRecieve)
						return false;
					if (!aborted)
						objectives.returnValue(valuesFound);
					return true;
				}

				@Override
				public boolean accept(ITextComponent chat) {
					if (chat instanceof TextComponentTranslation) {
						TextComponentTranslation translation = (TextComponentTranslation) chat;
						if (translation.getKey().equals("commands.generic.permission")) {
							objectives.abortFindingValue(1);
							aborted = true;
							amtToRecieve = valuesFound.size();
							return false;
						} else if (translation.getKey().equals("commands.scoreboard.objectives.list.empty")) {
							valuesFound.clear();
							amtToRecieve = 0;
							lastActivity = System.nanoTime();
							return false;
						} else if (translation.getKey().equals("commands.scoreboard.objectives.list.count")) {
							if (translation.getFormatArgs().length != 0) {
								Object formatArgObj = translation.getFormatArgs()[0];
								String formatArgStr;
								if (formatArgObj instanceof ITextComponent)
									formatArgStr = ((ITextComponent) formatArgObj).getUnformattedText();
								else
									formatArgStr = formatArgObj.toString();
								valuesFound.clear();
								try {
									amtToRecieve = Integer.parseInt(formatArgStr);
								} catch (NumberFormatException e) {
									amtToRecieve = 0;
								}
								lastActivity = System.nanoTime();
							}
							return false;
						} else if (translation.getKey().equals("commands.scoreboard.objectives.list.entry")) {
							Object[] formatArgs = translation.getFormatArgs();
							if (formatArgs.length >= 3) {
								Object formatArgObj = formatArgs[0];
								String objectiveName, objectiveDisplayName, objectiveCriterion;
								if (formatArgObj instanceof ITextComponent)
									objectiveName = ((ITextComponent) formatArgObj).getUnformattedText();
								else
									objectiveName = formatArgObj.toString();
								formatArgObj = formatArgs[1];
								if (formatArgObj instanceof ITextComponent)
									objectiveDisplayName = ((ITextComponent) formatArgObj).getUnformattedText();
								else
									objectiveDisplayName = formatArgObj.toString();
								formatArgObj = formatArgs[2];
								if (formatArgObj instanceof ITextComponent)
									objectiveCriterion = ((ITextComponent) formatArgObj).getUnformattedText();
								else
									objectiveCriterion = formatArgObj.toString();
								ScoreObjective objective = new ScoreObjective(null, objectiveName,
										IScoreCriteria.INSTANCES.get(objectiveCriterion));
								objective.setDisplayName(objectiveDisplayName);
								valuesFound.add(objective);
								lastActivity = System.nanoTime();
							}
							return false;
						} else {
							return true;
						}
					} else {
						return true;
					}
				}
			});
			executeCommand("/scoreboard objectives list");
		} else {
			Collection<ScoreObjective> objectivesCollection = DimensionManager.getWorld(0).getScoreboard()
					.getScoreObjectives();
			objectives.returnValue(Lists.newArrayList(objectivesCollection));
		}
	}

	private static final Splitter GAME_RULE_LIST_SPLITTER = Splitter.on(", ");

	/**
	 * Obtains a list of game rules from somewhere.
	 * 
	 * @param gameRules
	 *            - the listener for obtaining the list of game rules. The
	 *            meaning of the abort codes are as follows:<br/>
	 *            0: Timed out<br/>
	 *            1: No permission
	 */
	public static void getGameRuleNames(final ReturnedValueListener<List<String>> gameRules) {
		if (!Minecraft.getMinecraft().isIntegratedServerRunning()) {
			addBlock(new Blocker() {

				private boolean done = false;
				private boolean aborted = false;
				private long lastActivity = System.nanoTime();

				@Override
				public boolean isDone() {
					if (System.nanoTime() > lastActivity + 10000000000L) {
						gameRules.abortFindingValue(0);
						aborted = true;
					}
					return done || aborted;
				}

				@Override
				public boolean accept(ITextComponent chat) {
					if (!(chat instanceof TextComponentString)) {
						if (chat instanceof TextComponentTranslation) {
							String translation = ((TextComponentTranslation) chat).getKey();
							if ("commands.generic.permission".equals(translation)) {
								gameRules.abortFindingValue(1);
								aborted = true;
								return false;
							}
						}
						return true;
					}
					TextComponentString text = (TextComponentString) chat;
					if (!text.getSiblings().isEmpty() || !text.getStyle().isEmpty()) {
						return true;
					}
					List<String> gameRuleList = GAME_RULE_LIST_SPLITTER.splitToList(text.getText());
					if (gameRuleList.isEmpty()) {
						return true;
					}
					String lastGameRule = gameRuleList.get(gameRuleList.size() - 1);
					if (!lastGameRule.contains(" and ")) {
						return true;
					}
					int andIndex = lastGameRule.indexOf(" and ");
					gameRuleList.remove(gameRuleList.size() - 1);
					gameRuleList.add(lastGameRule.substring(0, andIndex));
					gameRuleList.add(lastGameRule.substring(andIndex + 5));

					// check for certain known game rules to check if this
					// actually is the game rule message
					if (!gameRuleList.contains("doFireTick") || !gameRuleList.contains("mobGriefing")
							|| !gameRuleList.contains("keepInventory")) {
						return true;
					}
					gameRules.returnValue(gameRuleList);
					done = true;
					lastActivity = System.nanoTime();
					return false;
				}
			});
			executeCommand("/gamerule");
		} else {
			gameRules.returnValue(Lists.newArrayList(Minecraft.getMinecraft().getIntegratedServer()
					.worldServerForDimension(0).getGameRules().getRules()));
		}
	}

	/**
	 * Obtains a map of game rules and their values from somewhere.
	 * 
	 * @param gameRules
	 *            - the listener for obtaining the map of game rules. The
	 *            meaning of the abort codes are as follows:<br/>
	 *            0: Timed out<br/>
	 *            1: No permission
	 */
	public static void getGameRules(final ReturnedValueListener<Map<String, String>> gameRules) {
		getGameRuleNames(new ReturnedValueListener<List<String>>() {
			@Override
			public void returnValue(final List<String> gameRuleNames) {
				if (!Minecraft.getMinecraft().isIntegratedServerRunning()) {
					final Map<String, String> gameRuleMap = Maps.newHashMap();
					for (final String gameRuleName : gameRuleNames) {
						addBlock(new Blocker() {
							private boolean done = false;
							private boolean aborted = false;
							private long lastActivity = System.nanoTime();

							@Override
							public boolean isDone() {
								if (System.nanoTime() > lastActivity + 10000000000L) {
									gameRules.abortFindingValue(0);
									aborted = true;
								}
								return done || aborted;
							}

							@Override
							public boolean accept(ITextComponent chat) {
								if (!(chat instanceof TextComponentString)) {
									if (chat instanceof TextComponentTranslation) {
										String translation = ((TextComponentTranslation) chat).getKey();
										if ("commands.generic.permission".equals(translation)) {
											gameRules.abortFindingValue(1);
											aborted = true;
											return false;
										}
									}
								}
								String text = chat.getUnformattedText();
								String[] nameAndValue = text.split(" = ", 2);
								if (nameAndValue.length != 2) {
									return true;
								}
								if (!gameRuleName.equals(nameAndValue[0])) {
									return true;
								}
								gameRuleMap.put(gameRuleName, nameAndValue[1]);
								if (gameRuleMap.size() == gameRuleNames.size()) {
									gameRules.returnValue(gameRuleMap);
								}
								done = true;
								lastActivity = System.nanoTime();
								return false;
							}
						});
						executeCommand(String.format("/gamerule %s", gameRuleName));
					}
				} else {
					GameRules gameRulesObj = Minecraft.getMinecraft().getIntegratedServer().worldServerForDimension(0)
							.getGameRules();
					Map<String, String> gameRuleMap = Maps.newHashMap();
					for (String gameRuleName : gameRuleNames) {
						gameRuleMap.put(gameRuleName, gameRulesObj.getString(gameRuleName));
					}
					gameRules.returnValue(gameRuleMap);
				}
			}

			@Override
			public void abortFindingValue(int reason) {
				gameRules.abortFindingValue(reason);
			}
		});
	}

	@SubscribeEvent
	public void clientChatReceived(ClientChatReceivedEvent e) {
		Iterator<Blocker> blocksIterator = blocks.iterator();
		while (blocksIterator.hasNext()) {
			Blocker block = blocksIterator.next();
			if (!block.accept(e.getMessage()))
				e.setCanceled(true);
			if (block.isDone())
				blocksIterator.remove();
		}
	}

	/**
	 * A class that controls listening to a blocking an {@link IChatComponent}.
	 * 
	 * <b>This class is a member of the Easy Editors API</b>
	 * 
	 * @author Earthcomputer
	 *
	 */
	public static abstract class Blocker {
		/**
		 * When this method returns true, the {@link #accept(IChatComponent)}
		 * method will stop being invoked
		 * 
		 * @return Whether this Blocker has finished listening and blocking and
		 *         is no longer of any use
		 */
		public abstract boolean isDone();

		/**
		 * Called for every {@link IChatComponent} received on the client side
		 * while this Blocker is registered to the {@link ChatBlocker}
		 * 
		 * @param chat
		 * @return false if the chat message should be blocked, true otherwise
		 */
		public abstract boolean accept(ITextComponent chat);
	}

}

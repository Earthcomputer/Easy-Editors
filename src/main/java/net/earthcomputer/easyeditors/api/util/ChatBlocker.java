package net.earthcomputer.easyeditors.api.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer.EnumChatVisibility;
import net.minecraft.network.play.client.C01PacketChatMessage;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
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
		mc.getNetHandler().addToSendQueue(new C01PacketChatMessage(command));
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
			public boolean accept(IChatComponent chat) {
				if (!(chat instanceof ChatComponentTranslation))
					return true;
				ChatComponentTranslation translation = (ChatComponentTranslation) chat;
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
	 * Obtains a list of registered team names from the server
	 * 
	 * @param teams
	 *            - the listener for obtaining the list of teams. The meaning of
	 *            the abort codes are as follows:<br/>
	 *            0: Timed out<br/>
	 *            1: No permission
	 */
	public static void obtainTeamsList(final ReturnedValueListener<List<String>> teams) {
		if (obtainDataFromServer && !Minecraft.getMinecraft().isIntegratedServerRunning()) {
			addBlock(new Blocker() {
				private boolean aborted = false;
				private long lastActivity = System.nanoTime();

				private int amtToRecieve = -1;
				List<String> valuesFound = Lists.newArrayList();

				@Override
				public boolean isDone() {
					if (System.nanoTime() > lastActivity + 10000000000L)
						teams.abortFindingValue(0);
					if (valuesFound.size() < amtToRecieve)
						return false;
					if (!aborted)
						teams.returnValue(valuesFound);
					return true;
				}

				@Override
				public boolean accept(IChatComponent chat) {
					if (chat instanceof ChatComponentTranslation) {
						ChatComponentTranslation translation = (ChatComponentTranslation) chat;
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
								if (formatArgObj instanceof IChatComponent)
									formatArgStr = ((IChatComponent) formatArgObj).getUnformattedText();
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
							if (translation.getFormatArgs().length != 0) {
								Object formatArgObj = translation.getFormatArgs()[0];
								String formatArgStr;
								if (formatArgObj instanceof IChatComponent)
									formatArgStr = ((IChatComponent) formatArgObj).getUnformattedText();
								else
									formatArgStr = formatArgObj.toString();
								valuesFound.add(formatArgStr);
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
			Collection<ScorePlayerTeam> teamsCollection = Minecraft.getMinecraft().theWorld.getScoreboard().getTeams();
			List<String> teamNames = Lists.newArrayList();
			for (ScorePlayerTeam team : teamsCollection) {
				teamNames.add(team.getRegisteredName());
			}
			teams.returnValue(teamNames);
		}
	}

	@SubscribeEvent
	public void clientChatReceived(ClientChatReceivedEvent e) {
		Iterator<Blocker> blocksIterator = blocks.iterator();
		while (blocksIterator.hasNext()) {
			Blocker block = blocksIterator.next();
			if (!block.accept(e.message))
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
		public abstract boolean accept(IChatComponent chat);
	}

}

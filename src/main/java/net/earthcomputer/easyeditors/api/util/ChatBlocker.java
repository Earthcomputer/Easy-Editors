package net.earthcomputer.easyeditors.api.util;

import java.util.Iterator;
import java.util.List;

import com.google.common.collect.Lists;

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

package com.clannotifications;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.vars.AccountType;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.api.events.StatChanged;
import net.runelite.api.clan.ClanChannelMember;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@PluginDescriptor(
	name = "Clan Notifications"
)
public class ClanNotificationsPlugin extends Plugin
{
	private final Map<String, Integer> currentLevels = new HashMap<>();

	@Inject
	private Client client;

	@Inject
	private ClanNotificationsConfig config;

	@Inject
	private ChatMessageManager chatMessageManager;

	@Override
	protected void startUp() throws Exception
	{
		log.info("Initializing Clan Notifications plugin!");
	}

	@Override
	protected void shutDown() throws Exception
	{
		log.info("Clan Notifications plugin stopped!");
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		if (gameStateChanged.getGameState() == GameState.LOGGED_IN)
		{
			currentLevels.clear();
		}
	}

	@Subscribe
	private void onStatChanged(StatChanged statChanged) {
		Skill skill = statChanged.getSkill();
		String skillName = skill.getName();
		int currentLevel = statChanged.getLevel();

		if (currentLevels.containsKey(skillName)) {
			int prevLevel = currentLevels.get(skillName);
			if (currentLevel > prevLevel) {
				onLevelUp(skillName, currentLevel);
			}
		}

		currentLevels.put(skillName, currentLevel);
	}

	@Subscribe
	private void onChatMessage(ChatMessage chatMessage) {
		if (chatMessage.getType() == ChatMessageType.CLAN_MESSAGE) {
			log.info(String.format("[Clan Message] %s", chatMessage.getMessage()));
		}
	}

	@Provides
	ClanNotificationsConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ClanNotificationsConfig.class);
	}

	private void onLevelUp(String skillName, int newLevel) {
		if (!config.isLevelUpEnabled()) {
			return;
		}
		Player player = client.getLocalPlayer();
		if (player == null) {
			return;
		}
		if (player.isClanMember()) {
			// Notify client
			String name = player.getName();
			String message =  String.format("%s's %s level has advanced to: %d", name, skillName, newLevel);

			chatMessageManager.queue(QueuedMessage.builder().type(ChatMessageType.CLAN_MESSAGE).runeLiteFormattedMessage(message).build());
		}
	}
}

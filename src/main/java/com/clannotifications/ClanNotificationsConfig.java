package com.clannotifications;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;


@ConfigGroup("example")
public interface ClanNotificationsConfig extends Config
{
	@ConfigItem(
			keyName = "levelUpNotifications",
			name = "Level-up notifications",
			description = "Configures whether to notify your clan of level-up messages."
	)
	default boolean isLevelUpEnabled()
	{
		return true;
	}
}

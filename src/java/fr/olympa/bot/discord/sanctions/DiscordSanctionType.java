package fr.olympa.bot.discord.sanctions;

import java.util.Arrays;

import fr.olympa.api.utils.Utils;

public enum DiscordSanctionType {

	MUTE,
	BAN;

	public String getName() {
		return Utils.capitalize(name());
	}

	public long getId() {
		return ordinal();
	}

	public static DiscordSanctionType get(int id) {
		return Arrays.stream(DiscordSanctionType.values()).filter(dst -> dst.getId() == id).findFirst().orElse(null);
	}
}

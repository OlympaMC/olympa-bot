package fr.olympa.bot.discord.member;

import fr.olympa.bot.discord.api.commands.DiscordCommand;

public enum MemberSettings {

	ALLOW_NOTIF_INVITE_USED("Notification lors de l'utilisation d'une invation");

	String name;
	boolean def;

	MemberSettings(String name, boolean b) {
		this.name = name;
		def = b;
	}

	MemberSettings(String name) {
		this.name = name;
		def = true;
	}

	public boolean getDefault() {
		return def;
	}

	public String getName() {
		return name;
	}

	public String getCmd() {
		return DiscordCommand.prefix + "toggle" + ordinal() + 1;
	}
}

package fr.olympa.bot.discord.commands.api;

import net.dv8tion.jda.api.entities.Message;

public interface CommandEvent {
	void onCommandSend(DiscordCommand command, String[] args, Message message);
}

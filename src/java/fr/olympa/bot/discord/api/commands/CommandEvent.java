package fr.olympa.bot.discord.api.commands;

import net.dv8tion.jda.api.entities.Message;

public interface CommandEvent {

	void onCommandSend(DiscordCommand command, String[] args, Message message, String label);
}

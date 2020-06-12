package fr.olympa.bot.discord.commands;

import fr.olympa.bot.bungee.ServerHandler;
import fr.olympa.bot.discord.api.DiscordPermission;
import fr.olympa.bot.discord.api.commands.DiscordCommand;
import net.dv8tion.jda.api.entities.Message;

public class StartCommand extends DiscordCommand {

	public StartCommand() {
		super("start", DiscordPermission.DEV);
		description = "[serveurs]";
		minArg = 1;
	}
	
	@Override
	public void onCommandSend(DiscordCommand command, String[] args, Message message) {
		ServerHandler.action("start", args[0], message);
	}
}

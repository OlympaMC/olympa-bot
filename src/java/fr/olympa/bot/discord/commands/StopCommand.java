package fr.olympa.bot.discord.commands;

import java.util.stream.Collectors;

import fr.olympa.bot.bungee.ServerHandler;
import fr.olympa.bot.discord.api.DiscordPermission;
import fr.olympa.bot.discord.api.commands.DiscordCommand;
import fr.olympa.core.bungee.servers.MonitorInfo;
import fr.olympa.core.bungee.servers.MonitorServers;
import net.dv8tion.jda.api.entities.Message;

public class StopCommand extends DiscordCommand {

	public StopCommand() {
		super("stop", DiscordPermission.DEV);
		description = "[" + MonitorServers.getLastServerInfo().stream().map(MonitorInfo::getName).collect(Collectors.joining("|")) + "]";
		minArg = 1;
	}
	
	@Override
	public void onCommandSend(DiscordCommand command, String[] args, Message message) {
		ServerHandler.action("stop", args[0], message);
	}
}

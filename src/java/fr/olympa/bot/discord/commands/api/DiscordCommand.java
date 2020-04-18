package fr.olympa.bot.discord.commands.api;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.dv8tion.jda.api.Permission;

public abstract class DiscordCommand implements CommandEvent {

	protected static String prefix = ":";
	private static Map<String, DiscordCommand> commands = new HashMap<>();

	public static DiscordCommand getCommand(String nameOrAliase) {
		String commandName = nameOrAliase.toLowerCase();
		DiscordCommand command = commands.get(commandName);
		if (command == null) {
			command = commands.values().stream().filter(commands -> commands.aliases != null && commands.aliases.contains(commandName)).findFirst().orElse(null);
		}
		return command;
	}

	String name;
	protected List<String> aliases;
	protected boolean privateChannel = false;
	protected Permission permission;
	protected Integer minArg;

	public DiscordCommand(String name) {
		this.name = name;
	}

	public DiscordCommand(String name, Permission permission) {
		this.name = name;
		this.permission = permission;
	}

	public DiscordCommand(String name, String... aliases) {
		this.name = name;
		this.aliases = Arrays.asList(aliases);
	}

	public void register() {
		DiscordCommand.commands.put(name, this);
	}
}

package fr.olympa.bot.discord.api.commands;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.olympa.bot.discord.api.DiscordPermission;

public abstract class DiscordCommand implements CommandEvent {

	protected static String prefix = ".";
	private static Map<String, DiscordCommand> commands = new HashMap<>();

	public static DiscordCommand getCommand(String nameOrAliase) {
		String commandName = nameOrAliase.toLowerCase();
		DiscordCommand command = commands.get(commandName);
		if (command == null) {
			command = commands.values().stream().filter(commands -> commands.aliases != null && commands.aliases.contains(commandName)).findFirst().orElse(null);
		}
		return command;
	}

	public static Map<String, DiscordCommand> getCommands() {
		return commands;
	}

	String name;
	protected List<String> aliases;
	protected boolean privateChannel = false;
	protected DiscordPermission permission;
	protected Integer minArg;
	protected String description;
	protected String usage;

	public DiscordCommand(String name) {
		this.name = name;
	}

	public DiscordCommand(String name, DiscordPermission permission) {
		this.name = name;
		this.permission = permission;
	}

	public DiscordCommand(String name, String... aliases) {
		this.name = name;
		this.aliases = Arrays.asList(aliases);
	}

	public String getDescription() {
		return description;
	}

	public DiscordPermission getPermission() {
		return permission;
	}

	public void register() {
		DiscordCommand.commands.put(name, this);
	}
}

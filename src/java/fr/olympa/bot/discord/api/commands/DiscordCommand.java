package fr.olympa.bot.discord.api.commands;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import fr.olympa.bot.OlympaBots;
import fr.olympa.bot.discord.api.DiscordPermission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.requests.ErrorResponse;

public abstract class DiscordCommand implements CommandEvent {

	protected static String prefix = ".";
	private static Map<String, DiscordCommand> commands = new HashMap<>();

	public static DiscordCommand getCommand(String nameOrAliase) {
		String commandName = nameOrAliase.toLowerCase();
		DiscordCommand command = commands.get(commandName);
		if (command == null)
			command = commands.values().stream().filter(commands -> commands.aliases != null && commands.aliases.contains(commandName)).findFirst().orElse(null);
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
	
	public String buildText(int min, String[] args) {
		return String.join(" ", Arrays.copyOfRange(args, min, args.length));
	}
	
	public DiscordCommand(String name, String... aliases) {
		this.name = name;
		this.aliases = Arrays.asList(aliases);
	}

	public String getDescription() {
		return description;
	}
	
	public void deleteMessage(Message message) {
		message.delete().queue(null, ErrorResponseException.ignore(ErrorResponse.UNKNOWN_MESSAGE));
	}

	public ScheduledFuture<?> deleteMessageAfter(Message message) {
		return message.delete().queueAfter(OlympaBots.getInstance().getDiscord().timeToDelete, TimeUnit.SECONDS, null, ErrorResponseException.ignore(ErrorResponse.UNKNOWN_MESSAGE));
	}

	public DiscordPermission getPermission() {
		return permission;
	}

	public void register() {
		DiscordCommand.commands.put(name, this);
	}
}

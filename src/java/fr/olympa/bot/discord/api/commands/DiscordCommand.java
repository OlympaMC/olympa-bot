package fr.olympa.bot.discord.api.commands;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import fr.olympa.api.match.RegexMatcher;
import fr.olympa.bot.discord.OlympaDiscord;
import fr.olympa.bot.discord.api.DiscordPermission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.requests.ErrorResponse;

public abstract class DiscordCommand implements CommandEvent {

	protected static String prefix = ".";
	private static Map<String, DiscordCommand> commands = new HashMap<>();

	public static DiscordCommand getCommand(String nameOrAliase) {
		String commandName = nameOrAliase.toLowerCase();
		DiscordCommand command = commands.get(commandName);
		if (command == null)
			command = commands.values().stream().filter(commands -> commands.getAliases() != null && commands.getAliases().contains(commandName)).findFirst().orElse(null);
		return command;
	}

	public static Map<String, DiscordCommand> getCommands() {
		return commands;
	}

	String name;
	protected List<String> aliases;
	protected boolean privateChannel = false;
	protected boolean checkEditedMsg = true;
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

	public DiscordCommand(String name, DiscordPermission permission, String... aliases) {
		this.name = name;
		this.aliases = Arrays.asList(aliases);
		this.permission = permission;
	}

	public String buildText(int min, String[] args) {
		return String.join(" ", Arrays.copyOfRange(args, min, args.length));
	}

	public Member getMember(Guild guild, String arg) {
		Member member = null;
		List<Member> members = guild.getMembersByEffectiveName(arg, true);
		if (members.isEmpty())
			members = guild.getMembersByName(arg, true);
		if (!members.isEmpty())
			member = members.get(0);
		else if (RegexMatcher.DISCORD_TAG.is(arg))
			member = guild.getMemberByTag(arg);
		if (member == null)
			member = guild.getMemberById(arg);
		return member;
	}

	public DiscordCommand(String name, String... aliases) {
		this.name = name;
		this.aliases = Arrays.asList(aliases);
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public List<String> getAliases() {
		return aliases;
	}

	public boolean checkPrivateChannel(Message message, User user) {
		if (!privateChannel && !message.isFromGuild()) {
			message.getChannel().sendMessage("Désolé " + user.getAsMention() + " mais cette commande est impossible en privé.").queue();
			return false;
		}
		return true;
	}

	public void deleteMessage(Message message) {
		message.delete().queue(null, ErrorResponseException.ignore(ErrorResponse.UNKNOWN_MESSAGE));
	}

	public ScheduledFuture<?> deleteMessageAfter(Message message) {
		return message.delete().queueAfter(OlympaDiscord.getTimeToDelete(), TimeUnit.SECONDS, null, ErrorResponseException.ignore(ErrorResponse.UNKNOWN_MESSAGE));
	}

	public DiscordPermission getPermission() {
		return permission;
	}

	public void register() {
		DiscordCommand.commands.put(name, this);
	}
}

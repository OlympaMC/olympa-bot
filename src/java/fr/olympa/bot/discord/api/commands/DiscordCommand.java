package fr.olympa.bot.discord.api.commands;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import fr.olympa.api.common.match.RegexMatcher;
import fr.olympa.api.common.player.OlympaPlayer;
import fr.olympa.api.common.provider.AccountProvider;
import fr.olympa.bot.discord.OlympaDiscord;
import fr.olympa.bot.discord.api.DiscordPermission;
import fr.olympa.bot.discord.member.DiscordMember;
import fr.olympa.bot.discord.sql.CacheDiscordSQL;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.requests.ErrorResponse;

public abstract class DiscordCommand implements CommandEvent {

	public class DiscordCommandContext {

	}

	public static String prefix = ".";
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
	protected boolean otherDiscord = false;
	protected DiscordPermission permission;
	protected Integer minArg;
	protected String description;
	protected String usage;

	protected User user;
	protected Member member;
	protected boolean isFromGuild;
	protected DiscordMember dm;

	public User getUser() {
		return user;
	}

	public Member getMember() {
		return member;
	}

	public boolean isFromGuild() {
		return isFromGuild;
	}

	public DiscordMember getDm() {
		return dm;
	}

	public void setDm(DiscordMember dm) {
		this.dm = dm;
	}

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
		if (member == null && RegexMatcher.LONG.is(arg))
			member = guild.getMemberById(arg);
		if (member == null && RegexMatcher.USERNAME.is(arg))
			try {
				member = getMemberByMinecraftName(guild, arg);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		return member;
	}

	public Member getMemberByMinecraftName(Guild guild, String arg) throws SQLException {
		Member member = null;
		DiscordMember discordMember;
		OlympaPlayer olympaPlayer = AccountProvider.get(arg);
		if (olympaPlayer != null) {
			discordMember = CacheDiscordSQL.getDiscordMemberByOlympaId(olympaPlayer.getId());
			if (discordMember != null)
				member = guild.getMemberById(discordMember.getDiscordId());
		}
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

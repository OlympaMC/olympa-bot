package fr.olympa.bot.discord.api.commands;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import fr.olympa.bot.OlympaBots;
import fr.olympa.bot.discord.api.DiscordPermission;
import fr.olympa.bot.discord.api.DiscordUtils;
import fr.olympa.bot.discord.guild.GuildHandler;
import fr.olympa.bot.discord.guild.OlympaGuild.DiscordGuildType;
import fr.olympa.bot.discord.sql.CacheDiscordSQL;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageType;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.GenericMessageEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.restaction.MessageAction;

public class CommandListener extends ListenerAdapter {

	private void checkMsg(GenericMessageEvent event, Message message) {
		User user = message.getAuthor();
		MessageChannel channel = event.getChannel();
		if (user.isFake() || message.getJDA().getSelfUser().getIdLong() == message.getAuthor().getIdLong())
			return;

		MessageType type = message.getType();
		if (type != MessageType.DEFAULT)
			return;
		String[] args = message.getContentDisplay().split(" ");
		if (args.length == 0)
			return;
		String label = args[0];
		List<User> mentions = message.getMentionedUsers();
		if (!Pattern.compile("^\\" + DiscordCommand.prefix + "[a-zA-Z]+$").matcher(label).find()) {
			if (mentions.contains(event.getJDA().getSelfUser())) {
				EmbedBuilder eb = new EmbedBuilder();
				eb.setColor(OlympaBots.getInstance().getDiscord().getColor());
				eb.setDescription(OlympaBots.getInstance().getDiscord().getJda().getSelfUser().getAsMention() + " mon prefix est `" + DiscordCommand.prefix + "`" + ".");
				channel.sendMessage(eb.build()).queue();
			}
			return;
		}
		Member member = null;
		if (message.isFromGuild())
			member = message.getMember();
		else
			member = GuildHandler.getOlympaGuild(DiscordGuildType.STAFF).getGuild().getMember(message.getAuthor());

		args = Arrays.copyOfRange(args, 1, args.length);
		if (label.length() > 1)
			label = label.substring(1);
		if (label.isEmpty())
			return;
		DiscordCommand discordCommand = DiscordCommand.getCommand(label);
		if (discordCommand == null) {
			channel.sendMessage("Désolé " + user.getAsMention() + " mais cette commande n'existe pas. Fait .help pour voir la liste des commandes.").queue();
			return;
		}
		if (!discordCommand.checkEditedMsg && event instanceof MessageUpdateEvent || !discordCommand.checkPrivateChannel(message, user))
			return;
		DiscordPermission permision = discordCommand.permission;
		try {
			discordCommand.dm = CacheDiscordSQL.getDiscordMember(member.getUser());
		} catch (SQLException e) {
			e.printStackTrace();
			return;
		}
		discordCommand.user = message.getAuthor();
		discordCommand.member = message.getMember();
		discordCommand.isFromGuild = message.isFromGuild();
		Guild guild;
		if (message.isFromGuild())
			guild = member.getGuild();
		else
			guild = null;
		if (permision != null && (discordCommand.dm == null || !discordCommand.dm.hasPermission(permision, guild))) {
			MessageAction out = channel.sendMessage(user.getAsMention() + " ➤ Tu n'a pas la permission :open_mouth:.");
			if (!message.isFromGuild())
				out.queue();
			else {
				DiscordUtils.deleteTempMessage(message);
				DiscordUtils.sendTempMessage(out);
			}
			return;
		}
		Integer minArg = discordCommand.minArg;
		if (minArg != null && minArg > args.length) {
			DiscordUtils.deleteTempMessage(message);
			DiscordUtils.sendTempMessage(channel, member.getAsMention() + " ➤ Syntaxe : " + DiscordCommand.prefix + label + " " + discordCommand.usage);
			return;
		}
		discordCommand.onCommandSend(discordCommand, args, message, label);
	}

	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		checkMsg(event, event.getMessage());
	}

	@Override
	public void onMessageUpdate(MessageUpdateEvent event) {
		checkMsg(event, event.getMessage());
	}
}

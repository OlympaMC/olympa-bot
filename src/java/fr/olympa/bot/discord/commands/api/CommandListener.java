package fr.olympa.bot.discord.commands.api;

import java.util.Arrays;
import java.util.List;

import fr.olympa.bot.OlympaBots;
import fr.olympa.bot.discord.api.DiscordIds;
import fr.olympa.bot.discord.api.DiscordUtils;
import fr.olympa.bot.discord.groups.DiscordGroup;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageType;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.restaction.MessageAction;

public class CommandListener extends ListenerAdapter {

	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		Message message = event.getMessage();
		User user = event.getAuthor();

		MessageChannel channel = event.getChannel();

		MessageType type = message.getType();
		if (type != MessageType.DEFAULT) {
			return;
		}
		String[] args = message.getContentDisplay().split(" ");
		if (args.length == 0) {
			return;
		}
		String commandName = args[0];

		if (!commandName.startsWith(DiscordCommand.prefix)) {
			List<User> mentions = message.getMentionedUsers();
			if (mentions.contains(event.getJDA().getSelfUser())) {
				EmbedBuilder eb = new EmbedBuilder();
				eb.setColor(OlympaBots.getInstance().getDiscord().getColor());
				eb.setDescription(OlympaBots.getInstance().getDiscord().getJda().getSelfUser().getAsMention() + " pour te servir. Le prefix est '" + DiscordCommand.prefix + "'" + ".");
				channel.sendMessage(eb.build()).queue();
			}
			return;
		}
		Member member = null;
		if (event.isFromGuild()) {
			member = message.getMember();
		} else {
			member = DiscordIds.getStaffGuild().getMember(message.getAuthor());
		}
		if (member == null || !DiscordGroup.isStaff(member)) {
			channel.sendMessage("Le bot est encore en développement, t'es pas prêt.").queue();
			return;
		}

		args = Arrays.copyOfRange(args, 1, args.length);
		commandName = commandName.substring(1);
		DiscordCommand discordCommand = DiscordCommand.getCommand(commandName);
		if (discordCommand == null) {
			channel.sendMessage("Désoler " + user.getAsMention() + " mais cette commande n'existe pas.").queue();
			return;
		}
		/*
		 * if (message.isFromGuild()) { DiscordUtils.deleteTempMessage(message); }
		 */
		boolean privateChannel = discordCommand.privateChannel;
		if (!privateChannel && !message.isFromGuild()) {
			channel.sendMessage("Désoler " + user.getAsMention() + " mais cette commande est impossible en priver.").queue();
			return;
		}
		if (message.isFromGuild()) {
			member = event.getMember();
		} else {
			member = DiscordUtils.getMember(user);
		}
		Permission permision = discordCommand.permission;
		if (permision != null && (member == null || !member.hasPermission(permision))) {
			MessageAction out = channel.sendMessage(user.getAsMention() + " ➤ Tu n'a pas la permission :open_mouth:.");
			if (!message.isFromGuild()) {
				out.queue();
			} else {
				DiscordUtils.deleteTempMessage(message);
				DiscordUtils.sendTempMessage(out);
			}
			return;
		}
		Integer minArg = discordCommand.minArg;
		if (minArg != null && minArg > args.length) {
			DiscordUtils.deleteTempMessage(message);
			DiscordUtils.sendTempMessage(channel, member.getAsMention() + " ➤ Usage: !" + commandName + " <message>");
			return;
		}
		discordCommand.onCommandSend(discordCommand, args, message);
	}
}

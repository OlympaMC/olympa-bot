package fr.olympa.bot.discord.commands.api;

import java.util.Arrays;

import fr.olympa.bot.discord.api.DiscordUtils;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
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

		Guild guild = null;
		if (event.isFromGuild()) {
			guild = event.getGuild();
		}
		if (guild == null || guild.getIdLong() != 541605430397370398L) {
			return;
		}
		MessageChannel channel = event.getChannel();
		if (channel == null || channel.getIdLong() != 679464560784179252L) {
			return;
		}

		MessageType type = message.getType();
		if (type != MessageType.DEFAULT) {
			return;
		}
		String[] args = message.getContentDisplay().split(" ");
		String commandName = args[0];
		
		args = Arrays.copyOfRange(args, 1, args.length);
		if (!commandName.startsWith(DiscordCommand.prefix)) {
			return;
		}
		commandName = commandName.substring(1);
		DiscordCommand discordCommand = DiscordCommand.getCommand(commandName);
		if (discordCommand == null) {
			return;
		}
		/*if (message.isFromGuild()) {
			DiscordUtils.deleteTempMessage(message);
		}*/
		boolean privateChannel = discordCommand.privateChannel;
		if (!privateChannel && !message.isFromGuild()) {
			channel.sendMessage("Désoler " + user + " mais cette commande est impossible en priver.").queue();
			return;
		}
		Member member;
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

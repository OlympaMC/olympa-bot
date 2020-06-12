package fr.olympa.bot.discord.api.commands;

import java.util.Arrays;
import java.util.List;

import fr.olympa.bot.OlympaBots;
import fr.olympa.bot.discord.api.DiscordPermission;
import fr.olympa.bot.discord.api.DiscordUtils;
import fr.olympa.bot.discord.groups.DiscordGroup;
import fr.olympa.bot.discord.guild.GuildHandler;
import fr.olympa.bot.discord.guild.OlympaGuild.DiscordGuildType;
import net.dv8tion.jda.api.EmbedBuilder;
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
		
		MessageType type = message.getType();
		if (type != MessageType.DEFAULT)
			return;
		String[] args = message.getContentDisplay().split(" ");
		if (args.length == 0)
			return;
		String commandName = args[0];
		if (commandName.startsWith(DiscordCommand.prefix + DiscordCommand.prefix))
			return;
		if (!commandName.startsWith(DiscordCommand.prefix)) {
			List<User> mentions = message.getMentionedUsers();
			if (mentions.contains(event.getJDA().getSelfUser())) {
				EmbedBuilder eb = new EmbedBuilder();
				eb.setColor(OlympaBots.getInstance().getDiscord().getColor());
				eb.setDescription(OlympaBots.getInstance().getDiscord().getJda().getSelfUser().getAsMention() + " pour te servir. Le prefix est `" + DiscordCommand.prefix + "`" + ".");
				channel.sendMessage(eb.build()).queue();
			}
			return;
		}
		Member member = null;
		if (message.isFromGuild())
			member = message.getMember();
		else
			member = GuildHandler.getOlympaGuild(DiscordGuildType.STAFF).getGuild().getMember(message.getAuthor());
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
		boolean privateChannel = discordCommand.privateChannel;
		if (!privateChannel && !message.isFromGuild()) {
			channel.sendMessage("Désoler " + user.getAsMention() + " mais cette commande est impossible en priver.").queue();
			return;
		}
		if (message.isFromGuild())
			member = message.getMember();
		else
			member = event.getGuild().getMember(user);
		DiscordPermission permision = discordCommand.permission;
		if (permision != null && (member == null || !permision.hasPermission(member))) {
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
			DiscordUtils.sendTempMessage(channel, member.getAsMention() + " ➤ Usage: !" + commandName + " " + discordCommand.usage);
			return;
		}
		discordCommand.onCommandSend(discordCommand, args, message);
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

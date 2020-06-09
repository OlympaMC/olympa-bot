package fr.olympa.bot.discord.old;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import fr.olympa.bot.discord.OlympaDiscord;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.SelfUser;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.ErrorResponse;

public class DiscordListener extends ListenerAdapter {

	private static TextChannel cachedChannel;

	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		Guild guild = event.getGuild();
		Message message = event.getMessage();
		MessageChannel channel = event.getChannel();
		Member member = event.getMember();
		channel.sendTyping().queue();
		String msg = message.getContentDisplay();

		String[] args = message.getContentDisplay().split(" ");

		if (args[0].equalsIgnoreCase("!delete")) {

			Set<Category> categories = guild.getCategories().stream().filter(cat -> cat.getName().equalsIgnoreCase("🏳 Support")).collect(Collectors.toSet());

			if (categories.isEmpty()) {
				channel.sendMessage("Rien à delete !").queue();
				return;
			}

			for (Category cat : categories) {
				for (GuildChannel chan : cat.getChannels())
					chan.delete().queue();
				cat.delete().queue();
			}

		} else if (args[0].equalsIgnoreCase("!msg")) {
			message.delete().queue(null, ErrorResponseException.ignore(ErrorResponse.UNKNOWN_MESSAGE));
			if (!member.hasPermission(Permission.MESSAGE_MANAGE)) {
				OlympaDiscord.sendTempMessageToChannel(channel, member.getAsMention() + " ➤ Vous n'avez pas la permission.");
				return;
			}

			if (args.length < 1) {
				OlympaDiscord.sendTempMessageToChannel(channel, member.getAsMention() + " ➤ Usage: !msg <message>");
				return;
			}

			TextChannel findChannel = guild.getTextChannels().stream().filter(ch -> ch.getId().equalsIgnoreCase(args[1])).findFirst().orElse(null);
			String[] messageToSend;
			if (cachedChannel != null && findChannel == null) {
				findChannel = cachedChannel;
				messageToSend = Arrays.copyOfRange(args, 1, args.length);
			} else if (findChannel == null)
				return;
			else
				messageToSend = Arrays.copyOfRange(args, 2, args.length);
			findChannel.sendMessage(String.join(" ", messageToSend)).queue();

		} else if (msg.startsWith("!instance")) {
			message.delete().queue(null, ErrorResponseException.ignore(ErrorResponse.UNKNOWN_MESSAGE));
			if (!member.hasPermission(Permission.MANAGE_ROLES)) {
				OlympaDiscord.sendTempMessageToChannel(channel, member.getAsMention() + " ➤ Vous n'avez pas la permission d'utiliser !instance.");
				return;
			}
			JDA jda = event.getJDA();
			SelfUser user = jda.getSelfUser();

			OlympaDiscord.sendTempMessageToChannel(channel, "Bot '" + user.getName() + "': Ping: " + jda.getGatewayPing() + " ms" + " Status: " + jda.getStatus().name());

		} else if (msg.startsWith("!clear")) {
			message.delete().queue(null, ErrorResponseException.ignore(ErrorResponse.UNKNOWN_MESSAGE));
			if (!member.hasPermission(Permission.MANAGE_ROLES)) {
				OlympaDiscord.sendTempMessageToChannel(channel, member.getAsMention() + " ➤ Vous n'avez pas la permission d'utiliser !clear.");
				return;
			}

			if (args.length < 2) {
				OlympaDiscord.sendTempMessageToChannel(channel, member.getAsMention() + " ➤ Usage: !clear <all|number>");
				return;
			}

			List<Message> hist = channel.getHistoryBefore(message.getIdLong(), 100).complete().getRetrievedHistory();
			hist.stream().forEach(history -> history.delete().queue());
			OlympaDiscord.sendTempMessageToChannel(channel, member.getAsMention() + " ➤ " + hist.size() + " messages ont été supprimés.");

		}
	}

}

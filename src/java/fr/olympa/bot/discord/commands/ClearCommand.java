package fr.olympa.bot.discord.commands;

import java.util.List;
import java.util.stream.Collectors;

import fr.olympa.api.common.match.MatcherPattern;
import fr.olympa.api.common.match.RegexMatcher;
import fr.olympa.bot.discord.api.DiscordPermission;
import fr.olympa.bot.discord.api.DiscordUtils;
import fr.olympa.bot.discord.api.commands.DiscordCommand;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;

public class ClearCommand extends DiscordCommand {

	private static boolean taskAll = false;

	public ClearCommand() {
		// TODO Change to ASSISTANT & + and DEV
		super("clear", DiscordPermission.STAFF);
		minArg = 1;
		description = "Supprime le nombre de lignes en argument (ou ALL).";
		usage = "<nombre>";
	}

	@Override
	public void onCommandSend(DiscordCommand command, String[] args, Message message, String label) {
		Member member = message.getMember();
		MatcherPattern<Integer> nb = RegexMatcher.NUMBER;
		if (nb.is(args[0])) {
			Integer i = nb.parse(args[0]);
			deleteMessage(message);

			new Thread(() -> clearMessage(member, message, i)).start();
		} else if (!args[0].equals("ALL"))
			message.getChannel().sendMessage(member.getAsMention() + "➤ " + args[0] + " doit être un nombre valide.").queue();
		else {
			deleteMessage(message);
			if (!clearAllMessage(member.getUser(), message))
				message.getChannel().sendMessage(member.getAsMention() + "➤ Je suis déjà en train de supprimer tous les messages d'un channel.").queue();
		}
	}

	public static boolean clearAllMessage(User user, Message message) {
		if (taskAll)
			return false;
		taskAll = true;
		new Thread(() -> {
			int count = 1;
			TextChannel channel = (TextChannel) message.getChannel();
			DiscordUtils.sendTempMessage(channel, user.getAsMention() + " ➤ Suppression de tous les messages du channel " + channel.getAsMention() + " en cours...");
			while (true) {
				List<Message> messagesPurges = channel.getHistory().getRetrievedHistory().stream().filter(msg -> !msg.isPinned()).collect(Collectors.toList());
				if (messagesPurges.isEmpty())
					break;
				channel.purgeMessages(messagesPurges);
				try {
					Thread.sleep(20000);
				} catch (InterruptedException e) {
					taskAll = false;
					DiscordUtils.sendTempMessage(channel, user.getAsMention() + " ➤ Une erreur est survenue. Réésaye. " + e.getMessage());
					return;
				}
				count++;
			}
			taskAll = false;
			DiscordUtils.sendTempMessage(channel, user.getAsMention() + " ➤ Tous les messages ont été supprimés en " + count + " coups, soit " + (count - 1) * 20 + " secondes.");
		}).start();
		return true;
	}

	public void clearMessage(Member member, Message message, int i) {
		if (i > 100)
			i = 100;
		MessageChannel channel = message.getChannel();
		channel.getHistoryBefore(message.getIdLong(), i).queue(hists -> {
			channel.purgeMessages(hists.getRetrievedHistory());
			//			int deleted = channel.purgeMessages(hists.getRetrievedHistory()).size();
			//				int deleted = 0;
			//				for (Message hist : hists.getRetrievedHistory())
			//					try {
			//						hist.delete().queue();
			//						hist.delete().queue();
			//						channel.deleteMessageById(hist.getIdLong()).reason("Supprimer par " + message.getAuthor().getAsTag());
			//						deleted++;
			//					} catch (Exception e) {
			//						DiscordUtils.sendTempMessage(channel, member.getAsMention() + " ➤ Impossible de supprimer " + hist.getJumpUrl() + " : " + e.getMessage());
			//					}
			DiscordUtils.sendTempMessage(channel, member.getAsMention() + " ➤ " + hists.size() + " messages ont été supprimés.");
			//			DiscordUtils.sendTempMessage(channel, member.getAsMention() + " ➤ " + deleted + "/" + hists.size() + " messages ont été supprimés.");
		});
	}
}

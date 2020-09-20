package fr.olympa.bot.discord.commands;

import fr.olympa.api.match.RegexMatcher;
import fr.olympa.bot.discord.api.DiscordPermission;
import fr.olympa.bot.discord.api.DiscordUtils;
import fr.olympa.bot.discord.api.commands.DiscordCommand;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;

public class ClearCommand extends DiscordCommand {

	public ClearCommand() {
		super("clear", DiscordPermission.ASSISTANT);
		minArg = 1;
		description = "Supprime le nombre lignes en argument.";
		usage = "<nombre>";
	}

	@Override
	public void onCommandSend(DiscordCommand command, String[] args, Message message, String label) {
		MessageChannel channel = message.getChannel();
		Member member = message.getMember();

		Integer i = (Integer) RegexMatcher.NUMBER.parse(args[0]);
		if (i == null || i == 0) {
			message.getChannel().sendMessage(member.getAsMention() + "➤ " + args[0] + " doit être un nombre valide.").queue();
			return;
		}

		deleteMessage(message);
		new Thread(() -> {
			channel.getHistoryBefore(message.getIdLong(), i).queue(hists -> {
				int deleted = channel.purgeMessages(hists.getRetrievedHistory()).size();
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
				DiscordUtils.sendTempMessage(channel, member.getAsMention() + " ➤ " + deleted + "/" + hists.size() + " messages ont été supprimés.");
			});
		}).run();
	}

}

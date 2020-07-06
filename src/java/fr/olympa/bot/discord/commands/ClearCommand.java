package fr.olympa.bot.discord.commands;

import java.util.List;

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

		channel = message.getChannel();

		// TODO CHECK INT
		int i = Integer.parseInt(args[0]);
		int j = 0;

		deleteMessage(message);
		List<Message> hists = channel.getHistoryBefore(message.getIdLong(), i).complete().getRetrievedHistory();
		for (Message hist : hists)
			try {
				deleteMessage(hist);
				j++;
			} catch (Exception e) {
				e.printStackTrace();
			}
		DiscordUtils.sendTempMessage(channel, member.getAsMention() + " ➤ " + j + "/" + hists.size() + " messages ont été supprimés.");

	}

}

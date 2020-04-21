package fr.olympa.bot.discord.commands;

import java.util.List;

import fr.olympa.bot.discord.api.DiscordUtils;
import fr.olympa.bot.discord.api.commands.DiscordCommand;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;

public class ClearCommand extends DiscordCommand {
	
	public ClearCommand() {
		super("clear", Permission.MESSAGE_MANAGE);
		this.minArg = 1;
	}
	
	@Override
	public void onCommandSend(DiscordCommand command, String[] args, Message message) {
		MessageChannel channel = message.getChannel();
		Member member = message.getMember();

		channel = message.getChannel();

		// TODO CHECK INT
		int i = Integer.parseInt(args[0]);
		int j = 0;

		message.delete().queue();
		List<Message> hists = channel.getHistoryBefore(message.getIdLong(), i).complete().getRetrievedHistory();
		for (Message hist : hists) {
			try {
				hist.delete().queue();
				j++;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		DiscordUtils.sendTempMessage(channel, member.getAsMention() + " ➤ " + j + "/" + hists.size() + " messages ont été supprimés.");

	}
	
}

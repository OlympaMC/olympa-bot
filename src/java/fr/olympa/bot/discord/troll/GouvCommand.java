package fr.olympa.bot.discord.troll;

import java.util.Arrays;

import fr.olympa.bot.discord.commands.api.DiscordCommand;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.TextChannel;

public class GouvCommand extends DiscordCommand {
	
	public GouvCommand() {
		super("gouvfr", Permission.ADMINISTRATOR);
	}
	
	@Override
	public void onCommandSend(DiscordCommand command, String[] args, Message message) {
		MessageChannel channel = message.getChannel();
		message.delete().queue();
		channel = message.getChannel();

		if (args.length > 2) {
			String id = args[0];
			TextChannel ch = Gouvfr.jda.getTextChannelById(id);
			String msg = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
			ch.sendMessage(msg).queue();
			
		} else
		Gouvfr.sendMsg();
	}
}
	

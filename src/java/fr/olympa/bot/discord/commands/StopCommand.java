package fr.olympa.bot.discord.commands;

import fr.olympa.api.utils.machine.OlympaRuntime;
import fr.olympa.bot.discord.api.DiscordPermission;
import fr.olympa.bot.discord.api.commands.DiscordCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;

public class StopCommand extends DiscordCommand {

	public StopCommand() {
		super("stop", DiscordPermission.BUILDER);
		description = "[serveurs]";
		minArg = 1;
	}

	@Override
	public void onCommandSend(DiscordCommand command, String[] args, Message message) {
		message.delete();
		OlympaRuntime.action("stop", args[0], out -> {
			message.getChannel().sendMessage(new EmbedBuilder().setDescription(message.getAuthor().getAsMention() + " " + out).build()).queue(m -> deleteMessageAfter(m));
		});
	}
}

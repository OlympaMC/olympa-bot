package fr.olympa.bot.discord.commands;

import fr.olympa.api.utils.machine.OlympaRuntime;
import fr.olympa.bot.OlympaBots;
import fr.olympa.bot.discord.api.DiscordPermission;
import fr.olympa.bot.discord.api.commands.DiscordCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;

public class StartStopCommand extends DiscordCommand {

	public StartStopCommand() {
		super("stop", DiscordPermission.BUILDER, "restart", "start");
		description = "[serveurs]";
		minArg = 1;
		description = "Effectue une action (stop/start/restart) sur un serveur.";
	}

	@Override
	public void onCommandSend(DiscordCommand command, String[] args, Message message, String label) {
		OlympaRuntime.action(label, args[0], out -> {
			message.getChannel().sendMessage(
					new EmbedBuilder().setDescription(message.getAuthor().getAsMention() + " " + out.replaceAll("§.", "")).setColor(OlympaBots.getInstance().getDiscord().getColor()).build())
					.queue(m -> deleteMessageAfter(m));
		}).start();
	}
}

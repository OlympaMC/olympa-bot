package fr.olympa.bot.discord.commands;

import java.util.Arrays;

import fr.olympa.api.utils.machine.OlympaRuntime;
import fr.olympa.bot.OlympaBots;
import fr.olympa.bot.discord.api.DiscordPermission;
import fr.olympa.bot.discord.api.commands.DiscordCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;

public class DeployCommand extends DiscordCommand {

	public DeployCommand() {
		super("deploy", DiscordPermission.DEV);
		description = "<plugin> [serveurs]";
		minArg = 1;
		description = "Deploie un plugin (sans le prefix Olympa) sur un serveur.";
	}

	@Override
	public void onCommandSend(DiscordCommand command, String[] args, Message message, String label) {
		String argument = String.join(" ", Arrays.copyOfRange(args, 0, args.length));
		OlympaRuntime.action(label, argument, out -> {
			message.getChannel().sendMessage(
					new EmbedBuilder().setDescription(message.getAuthor().getAsMention() + " " + out.replaceAll("§.", "")).setColor(OlympaBots.getInstance().getDiscord().getColor()).build())
					.queue(m -> deleteMessageAfter(m));
		}).start();
	}
}

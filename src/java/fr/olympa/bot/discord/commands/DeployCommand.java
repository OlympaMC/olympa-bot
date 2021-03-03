package fr.olympa.bot.discord.commands;

import java.util.Arrays;

import fr.olympa.api.utils.machine.OlympaRuntime;
import fr.olympa.bot.discord.api.DiscordPermission;
import fr.olympa.bot.discord.api.commands.DiscordCommand;
import net.dv8tion.jda.api.entities.Message;

public class DeployCommand extends DiscordCommand {

	public DeployCommand() {
		super("deploy", DiscordPermission.DEV, "autodeploy", "deployto");
		description = "<plugin> [serveurs]";
		minArg = 1;
		description = "Deploie un plugin (sans le prefix Olympa) sur un serveur.";
	}

	@Override
	public void onCommandSend(DiscordCommand command, String[] args, Message message, String label) {
		if (Arrays.stream(args).anyMatch(s -> s.contains("&&") || s.contains(";") || s.contains("|")) || args.length > 5) {
			message.getChannel().sendMessage(message.getAuthor().getAsMention() + " Sécuriter > commande trop complexe, impossible de l'exécuter.").queue();
			return;
		}
		String argument = String.join(" ", Arrays.copyOfRange(args, 0, args.length));
		OlympaRuntime.action(label, argument, out -> message.getChannel().sendMessage(message.getAuthor().getAsMention() + "```" + out.replaceAll("§.", "") + "```").queue(m -> deleteMessageAfter(m))).start();
	}
}

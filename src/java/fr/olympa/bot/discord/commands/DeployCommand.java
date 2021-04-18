package fr.olympa.bot.discord.commands;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringJoiner;
import java.util.concurrent.TimeUnit;

import fr.olympa.api.LinkSpigotBungee;
import fr.olympa.api.match.MatcherPattern;
import fr.olympa.api.utils.machine.OlympaRuntime;
import fr.olympa.bot.discord.api.DiscordPermission;
import fr.olympa.bot.discord.api.commands.DiscordCommand;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;

public class DeployCommand extends DiscordCommand {

	private static int taskId = -1;
	public final static Map<MessageChannel, StringJoiner> OUT = new HashMap<>();

	public DeployCommand() {
		super("deploy", DiscordPermission.DEV, "autodeploy", "deployto");
		description = "<plugin> [serveurs]";
		minArg = 1;
		description = "Deploie un plugin (sans le prefix Olympa) sur un serveur.";
	}

	private static StringJoiner getSJ() {
		return new StringJoiner("\n", "```\n", "```");
	}

	private void sendMsg(MessageChannel channel, StringJoiner out) {
		channel.sendMessage(out.toString()).queue();
	}

	private void addOut(MessageChannel channel, String s) {
		StringJoiner sj = OUT.get(channel);
		if (sj == null) {
			sj = getSJ();
			OUT.put(channel, sj);
		}
		StringJoiner sj2 = getSJ().merge(sj);
		sj2.add(s);
		if (sj2.toString().length() > Message.MAX_CONTENT_LENGTH) {
			sendMsg(channel, sj);
			sj = getSJ();
			OUT.replace(channel, sj);
			sj.add(s);
		} else
			OUT.replace(channel, sj2);
		enableTask();
	}

	private void disableTask() {
		if (taskId == -1)
			return;
		LinkSpigotBungee.Provider.link.getTask().removeTaskById(taskId);
		taskId = -1;
	}

	private void enableTask() {
		if (taskId != -1)
			return;
		taskId = LinkSpigotBungee.Provider.link.getTask().scheduleSyncRepeatingTask(() -> {
			if (OUT.isEmpty()) {
				disableTask();
				return;
			}
			for (Entry<MessageChannel, StringJoiner> e : OUT.entrySet())
				sendMsg(e.getKey(), e.getValue());
			OUT.clear();
		}, 3, 3, TimeUnit.SECONDS);
	}

	@Override
	public void onCommandSend(DiscordCommand command, String[] args, Message message, String label) {
		String argument = String.join(" ", Arrays.copyOfRange(args, 0, args.length));
		if (MatcherPattern.of("[^\\w àáâãäåçèéêëìíîïðòóôõöùúûüýÿÀÁÂÃÄÅÇÈÉÊËÌÍÎÏÐÒÓÔÕÖÙÚÛÜÝŸ]").contains(argument) || args.length > 5) {
			message.getChannel().sendMessage(message.getAuthor().getAsMention() + " Sécuriter > commande trop complexe, impossible de l'exécuter.").queue();
			return;
		}
		OlympaRuntime.action(label, argument, out -> addOut(message.getChannel(), out.replaceAll("§.", ""))).start();
	}
}

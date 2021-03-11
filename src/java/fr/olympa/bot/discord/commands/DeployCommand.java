package fr.olympa.bot.discord.commands;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringJoiner;
import java.util.concurrent.TimeUnit;

import fr.olympa.api.LinkSpigotBungee;
import fr.olympa.api.utils.machine.OlympaRuntime;
import fr.olympa.bot.discord.api.DiscordPermission;
import fr.olympa.bot.discord.api.commands.DiscordCommand;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;

public class DeployCommand extends DiscordCommand {

	private static int taskId = -1;
	private final static Map<MessageChannel, StringJoiner> OUT = new HashMap<>();

	public DeployCommand() {
		super("deploy", DiscordPermission.DEV, "autodeploy", "deployto");
		description = "<plugin> [serveurs]";
		minArg = 1;
		description = "Deploie un plugin (sans le prefix Olympa) sur un serveur.";
	}

	private void sendMsg(MessageChannel channel, StringJoiner out) {
		channel.sendMessage(out.toString()).queue();
	}

	private void addOut(MessageChannel channel, String s) {
		StringJoiner sj = OUT.get(channel);
		if (sj == null) {
			sj = new StringJoiner("\n", "```\n", "```");
			OUT.put(channel, sj);
		}
		StringJoiner sj2 = sj;
		sj2.add(s);
		if (sj2.toString().length() > Message.MAX_CONTENT_LENGTH) {
			sendMsg(channel, sj);
			sj = new StringJoiner("\n", "```\n", "```");
			sj.add(s);
		} else
			sj = sj2;
		enableTask();
	}

	private void disableTask() {
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
		}, 5, 5, TimeUnit.SECONDS);
	}

	@Override
	public void onCommandSend(DiscordCommand command, String[] args, Message message, String label) {
		if (Arrays.stream(args).anyMatch(s -> s.contains("&&") || s.contains(";") || s.contains("|")) || args.length > 5) {
			message.getChannel().sendMessage(message.getAuthor().getAsMention() + " Sécuriter > commande trop complexe, impossible de l'exécuter.").queue();
			return;
		}
		String argument = String.join(" ", Arrays.copyOfRange(args, 0, args.length));
		OlympaRuntime.action(label, argument, out -> addOut(message.getChannel(), out.replaceAll("§.", ""))).start();
	}
}

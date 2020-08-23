package fr.olympa.bot.discord.servers;

import java.awt.Color;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import fr.olympa.api.server.ServerStatus;
import fr.olympa.bot.discord.api.commands.DiscordCommand;
import fr.olympa.core.bungee.servers.MonitorInfo;
import fr.olympa.core.bungee.servers.MonitorServers;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;

public class ServersCommand extends DiscordCommand {

	public ServersCommand() {
		super("server", "servers");
		description = "Affiche la liste des serveurs et leur état.";
	}

	@Override
	public void onCommandSend(DiscordCommand command, String[] args, Message message, String label) {
		Member member = message.getMember();
		TextChannel channel = message.getTextChannel();

		Map<String, String> map = new HashMap<>();
		map.put("🔄", "refresh");
		channel.sendMessage(getEmbed()).queue(msg -> {
			RefreshServersReaction reaction = new RefreshServersReaction(map, msg, member.getIdLong());
			reaction.addToMessage(msg);
			reaction.saveToDB();
		});
	}

	public static MessageEmbed getEmbed() {
		EmbedBuilder embedBuilder = new EmbedBuilder();
		embedBuilder.setTitle("Liste des serveurs Minecraft:");
		Collection<MonitorInfo> info = MonitorServers.getServers();
		for (MonitorInfo serverInfo : info) {
			ServerStatus status = serverInfo.getStatus();
			StringJoiner sb = new StringJoiner(" ");
			sb.add("__" + status.getName() + "__");
			sb.add("**" + serverInfo.getName() + ":**\u200B");
			if (serverInfo.getOnlinePlayers() != null)
				sb.add("**Joueurs :** " + serverInfo.getOnlinePlayers() + "/" + serverInfo.getMaxPlayers() + "\u200B");
			if (serverInfo.getTps() != null)
				sb.add("**TPS :** " + serverInfo.getTps() + "\u200B");
			if (serverInfo.getPing() != null)
				sb.add("**Ping :** " + serverInfo.getPing() + "ms\u200B");
			if (serverInfo.getError() != null)
				sb.add("Erreur : *" + serverInfo.getError() + "*");
			embedBuilder.addField(serverInfo.getOlympaServer().getNameCaps(), sb.toString(), true);
		}
		List<ServerStatus> statuss = info.stream().map(si -> si.getStatus()).collect(Collectors.toList());
		if (statuss.stream().allMatch(s -> s == ServerStatus.OPEN))
			embedBuilder.setColor(Color.GREEN);
		else if (statuss.stream().allMatch(s -> s == ServerStatus.CLOSE))
			embedBuilder.setColor(Color.RED);
		else
			embedBuilder.setColor(Color.ORANGE);
		return embedBuilder.build();
	}
}
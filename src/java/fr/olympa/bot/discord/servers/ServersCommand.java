package fr.olympa.bot.discord.servers;

import java.awt.Color;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import org.apache.commons.collections4.map.LinkedMap;

import fr.olympa.api.server.ServerStatus;
import fr.olympa.bot.discord.api.commands.DiscordCommand;
import fr.olympa.bot.discord.guild.GuildHandler;
import fr.olympa.core.bungee.servers.MonitorInfoBungee;
import fr.olympa.core.bungee.servers.MonitorServers;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.md_5.bungee.api.config.ServerInfo;

public class ServersCommand extends DiscordCommand {

	public ServersCommand() {
		super("server", "servers");
		description = "Affiche la liste des serveurs et leur état.";
	}

	@Override
	public void onCommandSend(DiscordCommand command, String[] args, Message message, String label) {
		TextChannel channel = message.getTextChannel();
		LinkedMap<String, String> map = new LinkedMap<>();
		map.put("🔄", "refresh");
		channel.sendMessage(getEmbed()).queue(msg -> {
			RefreshServersReaction reaction = new RefreshServersReaction(map, msg, GuildHandler.getOlympaGuild(message.getGuild()), message.getAuthor());
			reaction.addToMessage(msg);
			reaction.saveToDB();
		});
	}

	public static MessageEmbed getEmbed() {
		EmbedBuilder embedBuilder = new EmbedBuilder();
		embedBuilder.setTitle("Liste des serveurs Minecraft:");
		Map<ServerInfo, MonitorInfoBungee> infos = MonitorServers.getServersMap();
		MonitorServers.getServersSorted().forEach(info -> {
			if (!info.getName().contains("bungee")) {
				ServerStatus status = info.getStatus();
				StringJoiner sb = new StringJoiner("\n");
				sb.add("**" + info.getName() + " : ** __" + status.getName() + "__");
				if (info.getOnlinePlayers() != null)
					sb.add("**Joueurs :** " + info.getOnlinePlayers() + "/" + info.getMaxPlayers() + "");
				if (info.getTps() != null)
					sb.add("**TPS :** " + info.getTps() + "");
				if (!info.getRangeVersion().equals("unknown")) {
					String ver = info.getRangeVersion();
					sb.add("**Version" + (ver.contains("à") ? "s" : "") + " : **" + ver + "");
				}
				if (info.getPing() != null)
					sb.add("**Ping :** " + info.getPing() + "ms");
				if (info.getError() != null && !info.getError().isEmpty())
					sb.add("Erreur : `" + info.getError() + "`");
				if (info.getLastModifiedCore() != null && !info.getLastModifiedCore().isBlank())
					sb.add("Dernière MAJ Core : `" + info.getLastModifiedCore() + "`");
				embedBuilder.addField(info.getOlympaServer().getNameCaps(), sb.toString(), true);
			}
		});
		List<ServerStatus> statuss = infos.entrySet().stream().map(entry -> entry.getValue().getStatus()).collect(Collectors.toList());
		if (statuss.stream().allMatch(s -> s == ServerStatus.OPEN))
			embedBuilder.setColor(Color.GREEN);
		else if (statuss.stream().allMatch(s -> s == ServerStatus.CLOSE))
			embedBuilder.setColor(Color.RED);
		else
			embedBuilder.setColor(Color.PINK);
		return embedBuilder.build();
	}
}
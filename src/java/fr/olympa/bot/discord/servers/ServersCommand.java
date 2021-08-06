package fr.olympa.bot.discord.servers;

import java.awt.Color;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import org.apache.commons.collections4.map.LinkedMap;

import fr.olympa.api.common.server.ServerInfoAdvancedBungee;
import fr.olympa.api.common.server.ServerStatus;
import fr.olympa.bot.discord.api.commands.DiscordCommand;
import fr.olympa.core.bungee.servers.MonitorServers;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.md_5.bungee.api.config.ServerInfo;

public class ServersCommand extends DiscordCommand {

	LinkedMap<String, String> base = new LinkedMap<>();
	LinkedMap<String, String> numbers = new LinkedMap<>();

	public ServersCommand() {
		super("server", "servers");
		description = "Affiche la liste des serveurs et leur état.";
		base.put("🔄", "refresh");
		numbers.put("1️⃣", "1");
		numbers.put("2️⃣", "2");
		numbers.put("3️⃣", "3");
		numbers.put("4️⃣", "4");
		numbers.put("5️⃣", "5");
		numbers.put("6️⃣", "6");
		numbers.put("7️⃣", "7");
		numbers.put("8️⃣", "8");
		numbers.put("9️⃣", "9");
		numbers.put("🔟", "10");
	}

	@Override
	public void onCommandSend(DiscordCommand command, String[] args, Message message, String label) {
		TextChannel channel = message.getTextChannel();
		RefreshServersReaction reaction = new RefreshServersReaction(base, message.getAuthor());
		channel.sendMessage(getEmbed()).queue(msg -> {
			reaction.addToMessage(msg);
			reaction.saveToDB();
		});
	}

	public static MessageEmbed getEmbed() {
		EmbedBuilder embedBuilder = new EmbedBuilder();
		embedBuilder.setTitle("Liste des serveurs Minecraft:");
		Map<ServerInfo, ServerInfoAdvancedBungee> infos = MonitorServers.getServersMap();
		MonitorServers.getServersByTypeWithBungee().forEach((olympaServer, serverEntrys) -> {
			StringJoiner sb = new StringJoiner("\n");
			serverEntrys.forEach((nb, info) -> {
				if (sb.length() != 0)
					sb.add("\n");
				ServerStatus status = info.getStatus();
				sb.add("**" + info.getName() + "** " + (!info.isOpen() ? "__" + status.getName() + "__" : ""));
				if (info.getOnlinePlayers() != null)
					sb.add("**Joueurs** " + info.getOnlinePlayers() + "/" + info.getMaxPlayers());
				StringJoiner sj = new StringJoiner(" | ");
				if (info.getTps() != null)
					sj.add("**TPS** " + info.getTps());
				if (info.getTps() != null)
					sj.add("**CPU** " + info.getCPUUsage());
				//				if (info.getRawMemUsage() != null)
				//					sj.add("**RAM** " + info.getMemUsage());
				String infoExtra = sj.toString();
				if (!infoExtra.isBlank())
					sb.add(infoExtra);
				String ver = info.getRangeVersionMinecraft();
				if (!ver.equals("unknown"))
					sb.add("**Version" + (ver.contains("à") ? "s" : "") + " : **" + ver + "");
				if (info.getPing() != null && status != ServerStatus.CLOSE)
					sb.add("**Ping :** " + info.getPing() + "ms");
				if (info.getError() != null && !info.getError().isEmpty())
					sb.add("Erreur : `" + info.getError() + "`");
			});
			embedBuilder.addField(olympaServer.getNameCaps() != null ? olympaServer.getNameCaps() : "Autres", sb.toString(), true);
		});
		//
		//		MonitorServers.getServersWithBungee();
		//		for (ServerInfoAdvancedBungee info : MonitorServers.getServersSorted().toList())
		//			if (!info.getName().contains("bungee")) {
		//				ServerStatus status = info.getStatus();
		//				StringJoiner sb = new StringJoiner("\n");
		//				sb.add("**" + info.getName() + "** " + (!info.isOpen() ? "__" + status.getName() + "__" : ""));
		//				if (info.getOnlinePlayers() != null)
		//					sb.add("**Joueurs** " + info.getOnlinePlayers() + "/" + info.getMaxPlayers() + "");
		//				StringJoiner sj = new StringJoiner(" | ");
		//				if (info.getTps() != null)
		//					sj.add("**TPS** " + info.getTps());
		//				if (info.getRawMemUsage() != null)
		//					sj.add("**RAM** " + info.getMemUsage());
		//				String infoExtra = sj.toString();
		//				if (!infoExtra.isBlank())
		//					sj.add(infoExtra);
		//				String ver = info.getRangeVersionMinecraft();
		//				if (!ver.equals("unknown"))
		//					sb.add("**Version" + (ver.contains("à") ? "s" : "") + " : **" + ver + "");
		//				if (info.getPing() != null)
		//					sb.add("**Ping :** " + info.getPing() + "ms");
		//				if (info.getError() != null && !info.getError().isEmpty())
		//					sb.add("Erreur : `" + info.getError() + "`");
		//				embedBuilder.addField(info.getOlympaServer().getNameCaps(), sb.toString(), true);
		//			}
		List<ServerStatus> statuss = infos.entrySet().stream().map(entry -> entry.getValue().getStatus()).toList();
		if (statuss.stream().allMatch(s -> s == ServerStatus.OPEN))
			embedBuilder.setColor(Color.GREEN);
		else if (statuss.stream().allMatch(s -> s == ServerStatus.CLOSE))
			embedBuilder.setColor(Color.RED);
		else
			embedBuilder.setColor(Color.PINK);
		return embedBuilder.build();
	}
}
package fr.olympa.bot.discord.commands;

import java.awt.Color;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import fr.olympa.api.maintenance.MaintenanceStatus;
import fr.olympa.api.utils.spigot.TPSUtils;
import fr.olympa.bot.discord.api.commands.DiscordCommand;
import fr.olympa.bot.discord.api.reaction.AwaitReaction;
import fr.olympa.bot.discord.api.reaction.ReactionDiscord;
import fr.olympa.core.bungee.servers.MonitorInfo;
import fr.olympa.core.bungee.servers.MonitorServers;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;

public class ServersCommand extends DiscordCommand {

	public ServersCommand() {
		super("server", "servers");
	}
	
	@Override
	public void onCommandSend(DiscordCommand command, String[] args, Message message) {
		Member member = message.getMember();
		TextChannel channel = message.getTextChannel();
		EmbedBuilder embedBuilder = new EmbedBuilder();
		embedBuilder.setTitle("Liste des serveurs Minecraft:");
		Set<MonitorInfo> info = MonitorServers.getLastServerInfo();
		for (MonitorInfo serverInfo : info) {
			MaintenanceStatus status = serverInfo.getStatus();
			StringJoiner sb = new StringJoiner(" ");
			sb.add("[" + status.getName() + "]");
			sb.add("**" + serverInfo.getName() + ":**");
			if (serverInfo.getOnlinePlayer() != null)
				sb.add(serverInfo.getOnlinePlayer() + "/" + serverInfo.getMaxPlayers());
			if (serverInfo.getTps() != null)
				sb.add(serverInfo.getTps() + "tps");
			if (serverInfo.getPing() != null)
				sb.add(serverInfo.getPing() + "ms");
			if (serverInfo.getError() != null)
				sb.add("Erreur: " + serverInfo.getError());
			embedBuilder.addField(serverInfo.getName(), sb.toString(), true);
		}
		List<MaintenanceStatus> statuss = info.stream().map(si -> si.getStatus()).collect(Collectors.toList());
		if (statuss.stream().allMatch(s -> s == MaintenanceStatus.OPEN))
			embedBuilder.setColor(Color.GREEN);
		else if (statuss.stream().allMatch(s -> s == MaintenanceStatus.CLOSE))
			embedBuilder.setColor(Color.RED);
		else
			embedBuilder.setColor(Color.ORANGE);
		Map<String, String> map = new HashMap<>();
		map.put("🔄", "refresh");
		channel.sendMessage(embedBuilder.build()).queue(msg -> AwaitReaction.addReaction(msg, new ReactionDiscord(map, member.getIdLong()) {
			@Override
			public void onReactRemove(long messageId, MessageChannel messageChannel, MessageReaction messageReaction, User user) {
			}

			@Override
			public void onReactModDeleteOne(long messageId, MessageChannel messageChannel) {
			}

			@Override
			public void onReactModClearAll(long messageId, MessageChannel messageChannel) {
			}

			@Override
			public boolean onReactAdd(long messageId, MessageChannel messageChannel, MessageReaction messageReaction, User user) {
				if ("refresh".equalsIgnoreCase(getData(messageReaction)))
					msg.editMessage(getEmbed()).queue();
				return false;
			}

			@Override
			public void onBotStop(long messageId) {
			}
		}));
	}
	
	public MessageEmbed getEmbed() {
		EmbedBuilder embedBuilder = new EmbedBuilder();
		embedBuilder.setTitle("Liste des serveurs Minecraft:");
		Set<MonitorInfo> info = MonitorServers.getLastServerInfo();
		for (MonitorInfo serverInfo : info) {
			MaintenanceStatus status = serverInfo.getStatus();
			StringJoiner sb = new StringJoiner(" ");
			sb.add("[" + status.getName() + "]");
			sb.add(status.getColor() + serverInfo.getName() + ":");
			if (serverInfo.getOnlinePlayer() != null)
				sb.add(serverInfo.getOnlinePlayer() + "/" + serverInfo.getMaxPlayers());
			if (serverInfo.getTps() != null)
				sb.add(TPSUtils.getTpsColor(serverInfo.getTps()) + "tps");
			if (serverInfo.getPing() != null)
				sb.add(serverInfo.getPing() + "ms");
			if (serverInfo.getError() != null)
				sb.add(status.getColor() + "Erreur: " + serverInfo.getError());
			embedBuilder.addField(serverInfo.getName(), sb.toString(), true);
		}
		return embedBuilder.build();
	}
}
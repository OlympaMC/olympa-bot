package fr.olympa.bot.discord.commands;

import java.awt.Color;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import fr.olympa.api.server.ServerStatus;
import fr.olympa.bot.discord.reaction.ReactionDiscord;
import fr.olympa.core.bungee.servers.MonitorInfo;
import fr.olympa.core.bungee.servers.MonitorServers;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.User;

public class RefreshServersMessage extends ReactionDiscord {
	
	public RefreshServersMessage(ResultSet resultSet) throws SQLException {
		super(resultSet.getString("data"), resultSet.getString("allowed_users_ids"), resultSet.getInt("can_multiple") == 1, resultSet.getInt("guild_id"), resultSet.getLong("message_id"));
	}
	
	public RefreshServersMessage(Map<String, String> map, Message msg, long guildOlympaId, long... canReactUserIds) {
		super(map, msg.getIdLong(), guildOlympaId, canReactUserIds);
	}

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
			messageChannel.retrieveMessageById(messageId).queue(x -> x.editMessage(getEmbed()).queue());
		return false;
	}
	
	@Override
	public void onBotStop(long messageId) {
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

package fr.olympa.bot.discord.commands;

import java.awt.Color;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import fr.olympa.api.maintenance.MaintenanceStatus;
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
			messageChannel.getHistory().getMessageById(messageId).editMessage(getEmbed()).queue();
		return false;
	}

	@Override
	public void onBotStop(long messageId) {
	}

	public static MessageEmbed getEmbed() {
		EmbedBuilder embedBuilder = new EmbedBuilder();
		embedBuilder.setTitle("Liste des serveurs Minecraft:");
		Set<MonitorInfo> info = MonitorServers.getLastServerInfo();
		for (MonitorInfo serverInfo : info) {
			MaintenanceStatus status = serverInfo.getStatus();
			StringJoiner sb = new StringJoiner(" ");
			sb.add("__" + status.getName() + "__");
			sb.add("**" + serverInfo.getName() + ":**");
			if (serverInfo.getOnlinePlayer() != null)
				sb.add(serverInfo.getOnlinePlayer() + "/" + serverInfo.getMaxPlayers());
			if (serverInfo.getTps() != null)
				sb.add(serverInfo.getTps() + "tps");
			if (serverInfo.getPing() != null)
				sb.add(serverInfo.getPing() + "ms");
			if (serverInfo.getError() != null)
				sb.add("Erreur: *" + serverInfo.getError() + "*");
			embedBuilder.addField(serverInfo.getName(), sb.toString(), true);
		}
		List<MaintenanceStatus> statuss = info.stream().map(si -> si.getStatus()).collect(Collectors.toList());
		if (statuss.stream().allMatch(s -> s == MaintenanceStatus.OPEN))
			embedBuilder.setColor(Color.GREEN);
		else if (statuss.stream().allMatch(s -> s == MaintenanceStatus.CLOSE))
			embedBuilder.setColor(Color.RED);
		else
			embedBuilder.setColor(Color.ORANGE);
		return embedBuilder.build();
	}
}
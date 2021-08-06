package fr.olympa.bot.teamspeak;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.github.theholywaffle.teamspeak3.TS3Api;
import com.github.theholywaffle.teamspeak3.TS3ApiAsync;
import com.github.theholywaffle.teamspeak3.api.ClientProperty;
import com.github.theholywaffle.teamspeak3.api.wrapper.Client;
import com.github.theholywaffle.teamspeak3.api.wrapper.ClientInfo;
import com.github.theholywaffle.teamspeak3.api.wrapper.ServerGroup;

import fr.olympa.api.common.player.OlympaPlayer;
import fr.olympa.api.utils.Prefix;
import fr.olympa.bot.OlympaBots;
import fr.olympa.core.bungee.redis.RedisBungeeSend;
import fr.olympa.core.common.provider.AccountProvider;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class TeamspeakHandler {

	@SuppressWarnings("deprecation")
	public static void check(ProxiedPlayer player) throws InterruptedException {
		TS3Api query = OlympaBots.getInstance().getTeamspeak().getQuery();
		if (query == null) {
			player.sendMessage(Prefix.DEFAULT_BAD.formatMessageB("La laison TeamSpeak/Serveur est cassée."));
			return;
		}
		List<Client> sameip = query.getClients().stream().filter(client -> client.getIp().equals(player.getAddress().getAddress().getHostAddress())).toList();
		if (sameip.isEmpty()) {
			player.sendMessage(Prefix.DEFAULT_BAD.formatMessageB("Tu dois être connecté sur le TeamSpeak."));
			return;
		}
		Client cl = sameip.stream().filter(client -> client.getNickname().equalsIgnoreCase(player.getName())).findFirst().orElse(null);
		if (cl == null) {
			player.sendMessage(Prefix.DEFAULT_BAD.formatMessageB("Tu dois utiliser le même pseudo sur Minecraft & TeamSpeak"));
			return;
		}
		setSynchronized(player, cl);
	}

	private static void setSynchronized(ProxiedPlayer player, Client client) throws InterruptedException {
		AccountProvider accountProvider = new AccountProvider(player.getUniqueId());
		OlympaPlayer olympaPlayer;
		try {
			olympaPlayer = accountProvider.get();
		} catch (SQLException e) {
			e.printStackTrace();
			return;
		}
		olympaPlayer.setTeamspeakId(client.getDatabaseId());
		updateRank(olympaPlayer, client);
		RedisBungeeSend.sendOlympaPlayerTeamspeakIDChanged(olympaPlayer);
		OlympaBots.getInstance().getTeamspeak().getQueryAsync().sendPrivateMessage(client.getId(), String.format("Ton identitée Teamspeak est désormais liée au compte Minecraft %s.", player.getName())).await();
		player.sendMessage(Prefix.DEFAULT_GOOD.formatMessageB("Ton compte Minecraft est désormais lié à ton identitée Teamspeak &2%s&a.", client.getNickname()));
	}

	public static void updateRank(OlympaPlayer olympaPlayer) throws InterruptedException {
		int id = olympaPlayer.getTeamspeakId();
		if (id == 0)
			return;
		TS3Api query = OlympaBots.getInstance().getTeamspeak().getQuery();
		ClientInfo client = query.getClientInfo(id);
		updateRank(olympaPlayer, client);
	}

	public static void updateRank(OlympaPlayer olympaPlayer, Client client) throws InterruptedException {
		TeamspeakUtils.removeAllServerGroup(client);
		TS3Api query = OlympaBots.getInstance().getTeamspeak().getQuery();
		TS3ApiAsync queryAsync = OlympaBots.getInstance().getTeamspeak().getQueryAsync();
		queryAsync.editClient(client.getId(), Collections.singletonMap(ClientProperty.CLIENT_DESCRIPTION, "Pseudo Minecraft : " + olympaPlayer.getName())).await();
		List<TeamspeakGroups> permissions = TeamspeakGroups.get(olympaPlayer.getGroups().keySet());
		permissions.add(TeamspeakGroups.CUSTOM.name(olympaPlayer.getGroupName()));
		permissions.addAll(TeamspeakGroups.getSeperators());
		List<ServerGroup> servergroups = query.getServerGroups().stream().filter(sg -> permissions.stream().anyMatch(p -> sg.getName().equalsIgnoreCase(p.getName()))).toList();
		for (ServerGroup tsGroup : servergroups)
			queryAsync.addClientToServerGroup(tsGroup.getId(), client.getDatabaseId()).await();
	}
}

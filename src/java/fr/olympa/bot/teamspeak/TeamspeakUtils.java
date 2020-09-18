package fr.olympa.bot.teamspeak;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.github.theholywaffle.teamspeak3.TS3Api;
import com.github.theholywaffle.teamspeak3.TS3ApiAsync;
import com.github.theholywaffle.teamspeak3.api.ClientProperty;
import com.github.theholywaffle.teamspeak3.api.wrapper.ChannelInfo;
import com.github.theholywaffle.teamspeak3.api.wrapper.Client;
import com.github.theholywaffle.teamspeak3.api.wrapper.ServerGroup;

import fr.olympa.api.player.OlympaPlayer;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.utils.Prefix;
import fr.olympa.bot.OlympaBots;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class TeamspeakUtils {

	static ServerGroup defaultServerGroup = null;
	static Map<Integer, ServerGroup> minecraftGroup = new HashMap<>();
	static List<Integer> minecraftStaffGroup = new ArrayList<>();

	@SuppressWarnings("deprecation")
	public static void check(ProxiedPlayer player) throws InterruptedException {
		TS3Api query = OlympaBots.getInstance().getTeamspeak().getQuery();
		if (query == null) {
			player.sendMessage(Prefix.DEFAULT_BAD.formatMessageB("La laison TeamSpeak/Serveur est cassée."));
			return;
		}
		List<Client> sameip = query.getClients().stream().filter(client -> client.getIp().equals(player.getAddress().getAddress().getHostAddress())).collect(Collectors.toList());
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

	public static String getChannelURI(ChannelInfo channel) {
		return "[URL=channelid://" + channel.getId() + "]" + channel.getName() + "[/URL]";
	}

	public static String getClientURI(Client client) {
		return "[URL=" + client.getClientURI() + "]" + client.getNickname() + "[/URL]";
	}

	public static boolean isInGroup(Client client, int groupID) {
		return Arrays.stream(client.getServerGroups()).filter(s -> groupID == s).findFirst().isPresent();
	}

	public static boolean isStaff(Client client) {
		return Arrays.stream(client.getServerGroups()).filter(s -> minecraftStaffGroup.contains(s)).findFirst().isPresent();
	}

	public static void removeAllServerGroup(Client client) {
		for (int clientservergroupid : client.getServerGroups())
			OlympaBots.getInstance().getTeamspeak().getQuery().removeClientFromServerGroup(clientservergroupid, client.getDatabaseId());
	}

	public static void setSynchronized(ProxiedPlayer player, Client client) throws InterruptedException {
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
		OlympaBots.getInstance().getTeamspeak().getQueryAsync()
				.sendPrivateMessage(client.getId(), String.format("Ton identitée Teamspeak est désormais liée au compte Minecraft %s.", player.getName())).await();
		player.sendMessage(Prefix.DEFAULT_GOOD.formatMessageB("Ton compte Minecraft est désormais lié à ton identitée Teamspeak &2%s&a.", client.getNickname()));
	}

	public static void updateRank(OlympaPlayer olympaPlayer, Client client) throws InterruptedException {
		removeAllServerGroup(client);
		TS3Api query = OlympaBots.getInstance().getTeamspeak().getQuery();
		TS3ApiAsync queryAsync = OlympaBots.getInstance().getTeamspeak().getQueryAsync();
		queryAsync.editClient(client.getId(), Collections.singletonMap(ClientProperty.CLIENT_DESCRIPTION, "Pseudo Minecraft : " + olympaPlayer.getName())).await();
		List<TeamspeakGroups> permissions = TeamspeakGroups.get(olympaPlayer.getGroups().keySet());
		permissions.add(TeamspeakGroups.CUSTOM.name(olympaPlayer.getGroupName()));
		List<ServerGroup> servergroups = query.getServerGroups().stream().filter(sg -> permissions.stream().anyMatch(p -> sg.getName().equalsIgnoreCase(p.getName()))).collect(Collectors.toList());
		for (ServerGroup tsGroup : servergroups)
			queryAsync.addClientToServerGroup(tsGroup.getId(), client.getDatabaseId()).await();
	}
}

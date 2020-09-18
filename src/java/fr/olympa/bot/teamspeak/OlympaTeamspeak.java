package fr.olympa.bot.teamspeak;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import com.github.theholywaffle.teamspeak3.TS3Api;
import com.github.theholywaffle.teamspeak3.TS3ApiAsync;
import com.github.theholywaffle.teamspeak3.TS3Config;
import com.github.theholywaffle.teamspeak3.TS3Query;
import com.github.theholywaffle.teamspeak3.api.wrapper.Channel;
import com.github.theholywaffle.teamspeak3.api.wrapper.Client;
import com.google.gson.Gson;

import fr.olympa.bot.OlympaBots;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.config.Configuration;

public class OlympaTeamspeak {

	TS3Query queryConfig;

	//	static List<Integer> helpchannelsId;
	Channel helpChannelsAdmin;
	OlympaBots plugin;

	public OlympaTeamspeak(OlympaBots plugin) {
		this.plugin = plugin;
	}

	public void connect() {
		try {
			TS3Config tsConfig = new TS3Config();
			Configuration config = plugin.getConfig().getConfig().getSection("teamspeak");
			String nickName = config.getString("clientName");
			tsConfig.setHost(config.getString("host"));

			queryConfig = new TS3Query(tsConfig);
			try {
				queryConfig.connect();
			} catch (Exception e) {
				ProxyServer.getInstance().getScheduler().schedule(OlympaBots.getInstance(), () -> connect(), 60, TimeUnit.SECONDS);
				return;
			}

			TS3Api query = queryConfig.getApi();
			query.login(config.getString("user"), config.getString("password"));
			query.selectVirtualServerById(config.getInt("serverId"));
			//		query.setCustomClientProperty(0, "client_lastconnected", String.valueOf(Utils.getCurrentTimeInSeconds()));
			for (Client client : query.getClients())
				if (client.getNickname().equalsIgnoreCase(nickName))
					if (client.isServerQueryClient()) {
						plugin.getLogger().log(Level.SEVERE, "Un ServerQuery nommé " + nickName + " est déjà connecter.");
						disconnect();
						return;
					} else
						query.kickClientFromServer("Merci de ne pas utiliser le nom '" + nickName + "'.", client);
			query.setNickname(nickName);
			//		helpchannelsId = query.getChannelsByName("Besoin d'aide #").stream().filter(c -> c.isPermanent()).map(c -> c.getId()).collect(Collectors.toList());
			helpChannelsAdmin = query.getChannelByNameExact("Demande d'aide > Administrateurs", true);
			System.out.println("map : " + new Gson().toJson(helpChannelsAdmin.getMap()));
			/*
			 * List<Integer> helpchannelsID = new ArrayList<>(); // Besoin d'aide channels
			 * helpchannelsID.add(18); helpchannelsID.add(16); helpchannelsID.add(15);
			 */

			query.registerAllEvents();
			query.addTS3Listeners(new TsListener());
			OlympaBots.getInstance().sendMessage("§aConnexion à §2Teamspeak§a établie.");
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void disconnect() {
		if (queryConfig != null) {
			queryConfig.exit();
			OlympaBots.getInstance().sendMessage("§cConnexion à §4Teamspeak§c fermée.");
		}
	}

	public TS3Api getQuery() {
		return queryConfig.getApi();
	}

	public TS3ApiAsync getQueryAsync() {
		return queryConfig.getAsyncApi();
	}

}

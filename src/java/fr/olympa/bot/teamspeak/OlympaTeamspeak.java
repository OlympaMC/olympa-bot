package fr.olympa.bot.teamspeak;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import com.github.theholywaffle.teamspeak3.TS3Api;
import com.github.theholywaffle.teamspeak3.TS3Config;
import com.github.theholywaffle.teamspeak3.TS3Query;
import com.github.theholywaffle.teamspeak3.api.wrapper.Client;

import fr.olympa.bot.OlympaBots;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.config.Configuration;

public class OlympaTeamspeak {

	TS3Query queryConfig;
	TS3Api query;

	//	static List<Integer> helpchannelsId;
	//	static List<Integer> helpchannelsIdAdmin;

	public void connect(OlympaBots plugin) {
		final TS3Config config = new TS3Config();
		Configuration c = plugin.getConfig().getConfig().getSection("teamspeak");
		String nickName = c.getString("clientName");
		config.setHost(c.getString("host"));

		queryConfig = new TS3Query(config);
		try {
			queryConfig.connect();
		} catch (Exception e) {
			ProxyServer.getInstance().getScheduler().schedule(OlympaBots.getInstance(), () -> connect(plugin), 60, TimeUnit.SECONDS);
			return;
		}

		query = queryConfig.getApi();
		query.login(c.getString("user"), c.getString("password"));
		query.selectVirtualServerById(c.getInt("serverId"));
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
		//		helpchannelsIdAdmin = query.getChannelsByName("Besoin d'un Admin").stream().filter(c -> c.isPermanent()).map(c -> c.getId()).collect(Collectors.toList());
		/*
		 * List<Integer> helpchannelsID = new ArrayList<>(); // Besoin d'aide channels
		 * helpchannelsID.add(18); helpchannelsID.add(16); helpchannelsID.add(15);
		 */

		query.registerAllEvents();
		query.addTS3Listeners(new TsListener());

		OlympaBots.getInstance().sendMessage("§aConnexion à §2Teamspeak§a établie.");
	}

	public void disconnect() {
		if (query != null) {
			query.unregisterAllEvents();
			queryConfig.exit();
			query.logout();
			OlympaBots.getInstance().sendMessage("§cConnexion à §4Teamspeak§c fermée.");
		}
	}

	public TS3Api getQuery() {
		return query;
	}

}

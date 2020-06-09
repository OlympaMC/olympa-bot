package fr.olympa.bot.twitter;

import java.util.concurrent.TimeUnit;

import fr.olympa.api.utils.Utils;
import fr.olympa.bot.OlympaBots;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

public class TwitterAPI {
	
	private Twitter twitter;
	private String errorMsg;
	
	OlympaBots plugin;
	
	public TwitterAPI(OlympaBots plugin) {
		this.plugin = plugin;
	}

	public void connect() {
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setDebugEnabled(true)
				.setOAuthConsumerKey("xvz1evFS4wEEPTGEFPHBog")
				.setOAuthConsumerSecret("kAcSOqF21Fu85e7zjz7ZN2U4ZRhfV3WpwPAoE3Z7kBw")
				.setOAuthAccessToken("370773112-GmHxMAgYyLbNEtIKZeRNFsMKPR9EyMZeS9weJAEb")
				.setOAuthAccessTokenSecret("LswwdoUaIvS8ltyTt5jkRh4J50vUPVVHtR2YPi5kE");
		TwitterFactory tf = new TwitterFactory(cb.build());
		twitter = tf.getInstance();
		plugin.getProxy().getScheduler().schedule(plugin, () -> {
			String errorMsg2 = updateConnected();
			if (errorMsg == null) {
				if (errorMsg2.isEmpty())
					plugin.sendMessage("&aConnexion à l'api &2Twitter&a établie.");
			} else if (!errorMsg.isEmpty())
				if (errorMsg2.isEmpty())
					plugin.sendMessage("&aConnexion à l'api &2Twitter&a établie.");
			if (!errorMsg2.isEmpty())
				plugin.sendMessage("&cConnexion à l'api &4Twitter&c impossible: " + errorMsg2);
			errorMsg = errorMsg2;
		}, 0, 5, TimeUnit.MINUTES);
	}
	
	public boolean isWork() {
		return errorMsg != null && errorMsg.isEmpty();
	}
	
	public String updateConnected() {
		try {
			int co = plugin.getProxy().getOnlineCount();
			twitter.updateProfile("Olympa", "http://olympa.fr", "⚠️ En développement",
					"☁️ Olympa, serveur Minecraft multi-jeux ! (1.9 - 1.15)\n👥 " + co + " connecté" + Utils.withOrWithoutS(co) + "\n💬 Discord: https://discord.gg/guF78Zb\n📞 Teamspeak: ts.olympa.fr");
			return new String();
		} catch (TwitterException e) {
			return e.getErrorMessage();
		}
	}
}

package fr.olympa.bot.discord;

import java.awt.Color;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import javax.security.auth.login.LoginException;

import fr.olympa.bot.discord.commands.AnnonceCommand;
import fr.olympa.bot.discord.commands.ClearCommand;
import fr.olympa.bot.discord.commands.EmoteCommand;
import fr.olympa.bot.discord.commands.InstanceCommand;
import fr.olympa.bot.discord.commands.api.CommandListener;
import fr.olympa.bot.discord.invites.InviteCommand;
import fr.olympa.bot.discord.listener.JoinListener;
import fr.olympa.bot.discord.listener.ReadyListener;
import fr.olympa.bot.discord.support.SupportCommand;
import fr.olympa.bot.discord.support.SupportListener;
import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;

public class OlympaDiscord {

	private static JDA jda;
	public static int timeToDelete = 20;
	public static Color color = Color.YELLOW;

	public static void connect() {

		JDABuilder builder = new JDABuilder(AccountType.BOT);

		// builder.setToken("NjYwMjIzOTc0MDAwNjg5MTgy.Xg0CEg.klNZz78zFNarlFSNCmfwbL6roKI");

		builder.setToken("NjYwMjIzOTc0MDAwNjg5MTgy.XkxtvQ.YaIarU6NAh0RxgEnogxpc8exlEg");
		builder.setAutoReconnect(true);
		builder.setGuildSubscriptionsEnabled(true);
		builder.setStatus(OnlineStatus.DO_NOT_DISTURB);

		builder.addEventListeners(new CommandListener());
		builder.addEventListeners(new ReadyListener());
		builder.addEventListeners(new JoinListener());
		builder.addEventListeners(new SupportListener());
		new AnnonceCommand().register();
		new EmoteCommand().register();
		new SupportCommand().register();
		new InstanceCommand().register();
		new ClearCommand().register();
		new InviteCommand().register();

		try {
			jda = builder.build();
		} catch (LoginException e) {
			e.printStackTrace();
			return;
		}

		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				jda.getPresence().setActivity(Activity.playing("🚧 En développement"));
			}
		}, 0, 20000);
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				int usersConnected = 0;
				int usersTotal = 0;
				for (User user2 : jda.getUserCache()) {
					if (!user2.isBot()) {
						Guild firstGuild = user2.getMutualGuilds().get(0);
						Member member2 = firstGuild.getMember(user2);
						if (member2.getOnlineStatus() != OnlineStatus.OFFLINE) {
							usersConnected++;
						}
						usersTotal++;
					}
				}
				jda.getPresence().setActivity(Activity.watching(usersConnected + "/" + usersTotal + " membres"));
			}
		}, 10000, 20000);
	}

	public static void disconnect() {
		if (jda != null) {
			jda.shutdown();
		}
	}

	public static Color getColor() {
		return color;
	}
	
	public static JDA getJda() {
		return jda;
	}

	@Deprecated
	public static void sendTempMessageToChannel(MessageChannel channel, String msg) {
		channel.sendMessage(msg).queue(message -> message.delete().queueAfter(1, TimeUnit.MINUTES));
	}
	
	public static void setColor(Color color) {
		OlympaDiscord.color = color;
	}
}

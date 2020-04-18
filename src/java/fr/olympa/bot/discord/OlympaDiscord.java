package fr.olympa.bot.discord;

import java.awt.Color;
import java.util.concurrent.TimeUnit;

import javax.security.auth.login.LoginException;

import fr.olympa.bot.discord.commands.AnnonceCommand;
import fr.olympa.bot.discord.commands.ClearCommand;
import fr.olympa.bot.discord.commands.EmoteCommand;
import fr.olympa.bot.discord.commands.InstanceCommand;
import fr.olympa.bot.discord.commands.api.CommandListener;
import fr.olympa.bot.discord.groups.GroupCommand;
import fr.olympa.bot.discord.groups.GroupListener;
import fr.olympa.bot.discord.invites.InviteCommand;
import fr.olympa.bot.discord.link.LinkListener;
import fr.olympa.bot.discord.listener.JoinListener;
import fr.olympa.bot.discord.listener.ReadyListener;
import fr.olympa.bot.discord.observer.ObserverListener;
import fr.olympa.bot.discord.support.SupportCommand;
import fr.olympa.bot.discord.support.SupportListener;
import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.MessageChannel;

public class OlympaDiscord {

	@Deprecated
	public static void sendTempMessageToChannel(MessageChannel channel, String msg) {
		channel.sendMessage(msg).queue(message -> message.delete().queueAfter(1, TimeUnit.MINUTES));
	}

	private JDA jda;
	public int timeToDelete = 20;
	private Color color = Color.YELLOW;

	public void connect() {

		JDABuilder builder = new JDABuilder(AccountType.BOT);

		// builder.setToken("NjYwMjIzOTc0MDAwNjg5MTgy.Xg0CEg.klNZz78zFNarlFSNCmfwbL6roKI");

		builder.setToken("NjYwMjIzOTc0MDAwNjg5MTgy.XkxtvQ.YaIarU6NAh0RxgEnogxpc8exlEg");
		builder.setAutoReconnect(true);
		builder.setGuildSubscriptionsEnabled(true);
		builder.setStatus(OnlineStatus.IDLE);

		builder.addEventListeners(new CommandListener());
		builder.addEventListeners(new ReadyListener());
		builder.addEventListeners(new JoinListener());
		builder.addEventListeners(new SupportListener());
		builder.addEventListeners(new ObserverListener());
		builder.addEventListeners(new LinkListener());
		builder.addEventListeners(new GroupListener());
		new AnnonceCommand().register();
		new EmoteCommand().register();
		new SupportCommand().register();
		new InstanceCommand().register();
		new ClearCommand().register();
		new InviteCommand().register();
		new GroupCommand().register();

		try {
			jda = builder.build();
		} catch (LoginException e) {
			e.printStackTrace();
			return;
		}

	}

	public void disconnect() {
		if (jda != null) {
			jda.shutdown();
			jda = null;
		}
	}

	public Color getColor() {
		return color;
	}

	public JDA getJda() {
		return jda;
	}

	public void setColor(Color color) {
		this.color = color;
	}
}

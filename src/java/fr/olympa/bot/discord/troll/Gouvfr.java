package fr.olympa.bot.discord.troll;

import java.util.Timer;
import java.util.TimerTask;

import javax.security.auth.login.LoginException;

import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.managers.Presence;

public class Gouvfr {

	public static JDA jda;

	public static void connect() {

		JDABuilder builder = new JDABuilder(AccountType.BOT);
		builder.setToken("Njg5MjY2MzE2MTU4MzA0Mjk4.XnAY-g.b1WRtrsPpm7bQ3_CLtO4jSpHVEA");
		builder.setAutoReconnect(true);

		new GouvCommand().register();
		try {
			jda = builder.build();
		} catch (LoginException e) {
			e.printStackTrace();
		}

		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				Presence presence = jda.getPresence();
				presence.setStatus(OnlineStatus.DO_NOT_DISTURB);
				presence.setActivity(Activity.watching("❌ Restez chez vous"));

			}
		}, 0 + 10000, 20000);
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				Presence presence = jda.getPresence();
				presence.setStatus(OnlineStatus.IDLE);
				presence.setActivity(Activity.listening("❌ Protégez vos proches"));

			}
		}, 10000 + 10000, 20000);
	}

	protected static void sendMsg() {

		String msg = "Alerte COVID-19\nLe Président de la République a décidé de mettre en place un dispositif de confinement sur l’ensemble du territoire à compter de maintenant ce mardi 17 mars à 12:00, pour quinze jours minimum. Les déplacements sont interdits sauf dans certains cas et uniquement à condition d'être munis d'une attestation. Plus d'infos sur https://www.gouvernement.fr/info-coronavirus";

		Guild guild = jda.getGuildById(544593846831415307L);
		TextChannel channel = guild.getTextChannelById(558148903837368320L);
		channel.sendMessage(msg).queue();
	}
}

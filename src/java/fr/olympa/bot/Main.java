package fr.olympa.bot;

import javax.security.auth.login.LoginException;

import fr.olympa.bot.discord.support.chat.SupportChatListener;
import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;

public class Main {

	private static JDA jda;

	public static void main(String[] args) {

		JDABuilder builder = new JDABuilder(AccountType.BOT);

		builder.setToken("NjYwMjIzOTc0MDAwNjg5MTgy.XkxtvQ.YaIarU6NAh0RxgEnogxpc8exlEg");
		builder.setAutoReconnect(true);
		builder.setGuildSubscriptionsEnabled(true);
		builder.setStatus(OnlineStatus.IDLE);

		builder.addEventListeners(new SupportChatListener());
		try {
			jda = builder.build();
		} catch (LoginException e) {
			e.printStackTrace();
			return;
		}

	}

}

package fr.olympa.bot.discord;

import java.awt.Color;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

import javax.security.auth.login.LoginException;

import fr.olympa.api.utils.Utils;
import fr.olympa.bot.discord.api.commands.CommandListener;
import fr.olympa.bot.discord.commands.AnnonceCommand;
import fr.olympa.bot.discord.commands.ClearCommand;
import fr.olympa.bot.discord.commands.EmoteCommand;
import fr.olympa.bot.discord.commands.HelpCommand;
import fr.olympa.bot.discord.commands.InfoCommand;
import fr.olympa.bot.discord.commands.PlayersCommand;
import fr.olympa.bot.discord.commands.ServersCommand;
import fr.olympa.bot.discord.commands.StartStopCommand;
import fr.olympa.bot.discord.commands.UsurpCommand;
import fr.olympa.bot.discord.groups.GroupCommand;
import fr.olympa.bot.discord.groups.GroupListener;
import fr.olympa.bot.discord.guild.GuildHandler;
import fr.olympa.bot.discord.guild.GuildsListener;
import fr.olympa.bot.discord.guild.SettingsCommand;
import fr.olympa.bot.discord.invites.InviteCommand;
import fr.olympa.bot.discord.link.LinkListener;
import fr.olympa.bot.discord.listeners.ReadyListener;
import fr.olympa.bot.discord.member.MemberListener;
import fr.olympa.bot.discord.observer.LogListener;
import fr.olympa.bot.discord.reaction.ReactionListener;
import fr.olympa.bot.discord.sanctions.MuteCommand;
import fr.olympa.bot.discord.spam.SpamListener;
import fr.olympa.bot.discord.sql.DiscordSQL;
import fr.olympa.bot.discord.staff.StaffListener;
import fr.olympa.bot.discord.support.SupportCommand;
import fr.olympa.bot.discord.support.SupportListener;
import fr.olympa.bot.discord.support.chat.SupportChatListener;
import fr.olympa.bot.discord.textmessage.TextChannelListener;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.requests.ErrorResponse;
import net.md_5.bungee.api.plugin.Plugin;

public class OlympaDiscord {

	public static long uptime = Utils.getCurrentTimeInSeconds();
	public static long lastConnection;

	@Deprecated
	public static void sendTempMessageToChannel(MessageChannel channel, String msg) {
		channel.sendMessage(msg).queue(message -> message.delete().queueAfter(1, TimeUnit.MINUTES, null, ErrorResponseException.ignore(ErrorResponse.UNKNOWN_MESSAGE)));
	}

	private JDA jda;
	public int timeToDelete = 60;
	private Color color = Color.YELLOW;

	@SuppressWarnings("deprecation")
	public void connect(Plugin plugin) {

		JDABuilder builder = new JDABuilder("NjYwMjIzOTc0MDAwNjg5MTgy.XkxtvQ.YaIarU6NAh0RxgEnogxpc8exlEg");
		builder.setStatus(OnlineStatus.IDLE);
		builder.setBulkDeleteSplittingEnabled(true);

		builder.addEventListeners(new CommandListener());
		builder.addEventListeners(new ReadyListener());
		builder.addEventListeners(new SupportListener());
		//builder.addEventListeners(new ObserverListener());
		builder.addEventListeners(new LinkListener());
		builder.addEventListeners(new GroupListener());
		builder.addEventListeners(new SpamListener());
		builder.addEventListeners(new ReactionListener());
		builder.addEventListeners(new SupportChatListener());
		builder.addEventListeners(new TextChannelListener());
		builder.addEventListeners(new GuildsListener());
		builder.addEventListeners(new LogListener());
		builder.addEventListeners(new MemberListener());
		builder.addEventListeners(new StaffListener());
		new AnnonceCommand().register();
		new EmoteCommand().register();
		new SupportCommand().register();
		new InfoCommand().register();
		new ClearCommand().register();
		new InviteCommand().register();
		new GroupCommand().register();
		new MuteCommand().register();
		new SettingsCommand().register();
		new UsurpCommand().register();
		new StartStopCommand().register();
		new ServersCommand().register();
		new HelpCommand().register();
		new PlayersCommand().register();

		plugin.getProxy().getScheduler().runAsync(plugin, () -> {
			try {
				GuildHandler.guilds = DiscordSQL.selectGuilds();
				jda = builder.build();
			} catch (LoginException | SQLException e) {
				e.printStackTrace();
				return;
			}
		});

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

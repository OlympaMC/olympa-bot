package fr.olympa.bot.discord;

import java.awt.Color;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

import javax.security.auth.login.LoginException;

import fr.olympa.api.utils.Utils;
import fr.olympa.bot.OlympaBots;
import fr.olympa.bot.discord.api.commands.CommandListener;
import fr.olympa.bot.discord.api.reaction.ReactionListener;
import fr.olympa.bot.discord.commands.AnnonceCommand;
import fr.olympa.bot.discord.commands.ClearCommand;
import fr.olympa.bot.discord.commands.DeployCommand;
import fr.olympa.bot.discord.commands.EmoteCommand;
import fr.olympa.bot.discord.commands.HelpCommand;
import fr.olympa.bot.discord.commands.InfoCommand;
import fr.olympa.bot.discord.commands.PermissionCommand;
import fr.olympa.bot.discord.commands.PlayersCommand;
import fr.olympa.bot.discord.commands.PurgeCommand;
import fr.olympa.bot.discord.commands.StartStopCommand;
import fr.olympa.bot.discord.commands.UsurpCommand;
import fr.olympa.bot.discord.groups.GroupCommand;
import fr.olympa.bot.discord.groups.GroupListener;
import fr.olympa.bot.discord.guild.GuildHandler;
import fr.olympa.bot.discord.guild.GuildSQL;
import fr.olympa.bot.discord.guild.GuildsListener;
import fr.olympa.bot.discord.guild.SettingsCommand;
import fr.olympa.bot.discord.invites.InviteCommand;
import fr.olympa.bot.discord.invites.InvitesListener;
import fr.olympa.bot.discord.invites.VanillaInviteCommand;
import fr.olympa.bot.discord.link.LinkListener;
import fr.olympa.bot.discord.member.MemberListener;
import fr.olympa.bot.discord.message.TextChannelListener;
import fr.olympa.bot.discord.observer.LogListener;
import fr.olympa.bot.discord.ready.ReadyListener;
import fr.olympa.bot.discord.sanctions.MuteCommand;
import fr.olympa.bot.discord.servers.ServersCommand;
import fr.olympa.bot.discord.spam.SpamListener;
import fr.olympa.bot.discord.staff.StaffListener;
import fr.olympa.bot.discord.support.SupportCommand;
import fr.olympa.bot.discord.support.SupportListener;
import fr.olympa.bot.discord.support.chat.SupportChatListener;
import fr.olympa.bot.discord.suvey.SurveyCommand;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;

public class OlympaDiscord {

	private static final long UPTIME = Utils.getCurrentTimeInSeconds();
	private static long lastConnection;
	private static final int TIMETODELETE = 60;

	private JDA jda;
	private Color color = Color.YELLOW;
	OlympaBots plugin;

	public OlympaDiscord(OlympaBots plugin) {
		this.plugin = plugin;
	}

	@SuppressWarnings("deprecation")
	public void connect() {

		JDABuilder builder = new JDABuilder(plugin.getConfig().getConfig().getString("discord.token"));
		builder.setStatus(OnlineStatus.IDLE);
		builder.setBulkDeleteSplittingEnabled(true);

		builder.addEventListeners(new CommandListener());
		builder.addEventListeners(new ReadyListener());
		builder.addEventListeners(new SupportListener());
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
		builder.addEventListeners(new InvitesListener());
		new AnnonceCommand().register();
		new EmoteCommand().register();
		new SupportCommand().register();
		new InfoCommand().register();
		new ClearCommand().register();
		new VanillaInviteCommand().register();
		new GroupCommand().register();
		new MuteCommand().register();
		new SettingsCommand().register();
		new UsurpCommand().register();
		new StartStopCommand().register();
		new ServersCommand().register();
		new HelpCommand().register();
		new PlayersCommand().register();
		new DeployCommand().register();
		new SurveyCommand().register();
		new PurgeCommand().register();
		new PermissionCommand().register();
		new InviteCommand().register();

		plugin.getProxy().getScheduler().runAsync(plugin, () -> {
			try {
				GuildHandler.guilds = GuildSQL.selectGuilds();
				jda = builder.build();
				plugin.getProxy().getScheduler().schedule(plugin, OlympaBots.getInstance().bungeeListener::sendErrorFlushInfo, 2, TimeUnit.SECONDS);
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

	public static long getUptime() {
		return UPTIME;
	}

	public static String connectedFrom() {
		return Utils.timestampToDuration(OlympaDiscord.lastConnection);
	}

	public static void setLastConnection(long lastConnection) {
		OlympaDiscord.lastConnection = lastConnection;
	}

	public static int getTimeToDelete() {
		return TIMETODELETE;
	}
}

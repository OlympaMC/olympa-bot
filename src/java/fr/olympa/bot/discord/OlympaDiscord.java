package fr.olympa.bot.discord;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import javax.security.auth.login.LoginException;

import fr.olympa.bot.discord.commands.AnnonceCommand;
import fr.olympa.bot.discord.commands.EmoteCommand;
import fr.olympa.bot.discord.commands.InstanceCommand;
import fr.olympa.bot.discord.commands.SupportCommand;
import fr.olympa.bot.discord.commands.api.CommandListener;
import fr.olympa.bot.discord.listener.JoinListener;
import fr.olympa.bot.discord.listener.ReadyListener;
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

	public static void connect() {

		JDABuilder builder = new JDABuilder(AccountType.BOT);

		// builder.setToken("NjYwMjIzOTc0MDAwNjg5MTgy.Xg0CEg.klNZz78zFNarlFSNCmfwbL6roKI");

		builder.setToken("NjYwMjIzOTc0MDAwNjg5MTgy.XkxtvQ.YaIarU6NAh0RxgEnogxpc8exlEg");
		builder.setAutoReconnect(true);
		builder.setStatus(OnlineStatus.DO_NOT_DISTURB);

		builder.addEventListeners(new CommandListener());
		builder.addEventListeners(new ReadyListener());
		builder.addEventListeners(new JoinListener());
		new AnnonceCommand().register();
		new EmoteCommand().register();
		new SupportCommand().register();
		new InstanceCommand().register();

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
				jda.getPresence().setActivity(Activity.playing("⚠️ En développement"));
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

		/*try {
			jda = builder.buildAsync();
			ProxyServer.getInstance().getScheduler().schedule(Main.getInstance(), () -> updateConnected(), 0, 1, TimeUnit.MINUTES);
		} catch(LoginException e) {
			e.printStackTrace();
		}*/
	}

	/*public static void updateConnected() {
		int co = ProxyServer.getInstance().getOnlineCount();
		JDA jda = OlympaDiscord.jda;
		jda.getPresence().setGame(Game.playing(co + " connecté" + Utils.withOrWithoutS(co) + " | play.olympamc.fr"));
	}

	public static void checkName(Guild guild, Member member) {
		List<TextChannel> channels = guild.getTextChannelsByName("bot", false);
		TextChannel channel;
		if(!channels.isEmpty()) {
			channel = channels.get(0);
		} else {
			channel = guild.getDefaultChannel();
		}

		GuildController guildController = guild.getController();

		OlympaPlayer olympaPlayer = MySQL.getPlayer(member.getEffectiveName());
		if(olympaPlayer == null) {
			guildController.removeRolesFromMember(member, member.getRoles()).queue();
			channel.sendMessage(member.getAsMention() + " ➤ " + member.getEffectiveName() + " n'est pas connu par OlympaMC, merci d'utiliser le même pseudo que en jeux").queue();
			return;
		}
	}

	public static void setRole(Member author, Guild guild, MessageChannel channel, String memberName) {
		List<Member> members = guild.getMembersByEffectiveName(memberName, false);

		if(members.isEmpty()) {
			OlympaDiscord.sendTempMessageToChannel(channel, author.getAsMention() + " ➤ Le pseudo " + memberName + " est introuvable sur ce Discord.");
			return;
		}

		if(members.size() > 1) {
			OlympaDiscord.sendTempMessageToChannel(channel,
					author.getAsMention() + " ➤ Le pseudo " + memberName + " est associé à plusieurs comptes: " + members.stream().map(member -> member.getEffectiveName()).collect(
							Collectors.joining(", ")) + ".");
			return;
		}

		Member member = members.get(0);

		OlympaPlayer olympaPlayer = MySQL.getPlayer(member.getEffectiveName());
		if(olympaPlayer == null) {
			OlympaDiscord.sendTempMessageToChannel(channel, author.getAsMention() + " ➤ Le pseudo " + member.getEffectiveName() + " est introuvable dans la base de donnés minecraft OlympaMC.");
			return;
		}
		GuildController guildController = guild.getController();

		List<Role> roles = jda.getRolesByName(olympaPlayer.getGroup().getName(), true);

		if(roles.isEmpty()) {
			OlympaDiscord.sendTempMessageToChannel(channel, author.getAsMention() + " ➤ Le rôle " + olympaPlayer.getGroup().getName() + " n'existe pas sur ce Discord.");
			return;
		}

		Role role = roles.get(0);


		guildController.addSingleRoleToMember(member, role).queue();
		OlympaDiscord.sendTempMessageToChannel(channel, author.getAsMention() + " ➤ " + member.getEffectiveName() + " a désormais le rôle " + role.getName());
	}*/

	public static void disconnect() {
		if (jda != null) {
			jda.shutdown();
		}
	}

	public static JDA getJda() {
		return jda;
	}
	
	public static void sendTempMessageToChannel(MessageChannel channel, String msg) {
		channel.sendMessage(msg).queue(message -> message.delete().queueAfter(1, TimeUnit.MINUTES));
	}
}

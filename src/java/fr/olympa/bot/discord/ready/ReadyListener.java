package fr.olympa.bot.discord.ready;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import fr.olympa.api.common.server.ServerStatus;
import fr.olympa.api.utils.Utils;
import fr.olympa.bot.OlympaBots;
import fr.olympa.bot.discord.OlympaDiscord;
import fr.olympa.bot.discord.api.reaction.ReactionHandler;
import fr.olympa.bot.discord.groups.DiscordGroup;
import fr.olympa.bot.discord.guild.GuildHandler;
import fr.olympa.bot.discord.guild.OlympaGuild;
import fr.olympa.bot.discord.guild.OlympaGuild.DiscordGuildType;
import fr.olympa.core.bungee.OlympaBungee;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDA.Status;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.StatusChangeEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.Presence;
import net.md_5.bungee.api.ProxyServer;

public class ReadyListener extends ListenerAdapter {

	@Override
	public void onReady(ReadyEvent event) {
		JDA jda = event.getJDA();
		List<Guild> guilds = jda.getGuilds();
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				Presence presence = jda.getPresence();
				ProxyServer.getInstance();
				int count = ProxyServer.getInstance().getOnlineCount();
				String s = count > 1 ? "s" : new String();
				if (count > 0) {
					presence.setActivity(Activity.playing("🎮" + count + " joueur%s connecté%s".replace("%s", s)));
					presence.setStatus(OnlineStatus.ONLINE);
				} else {
					ServerStatus status = OlympaBungee.getInstance().getStatus();
					switch (status) {
					case BETA:
						presence.setStatus(OnlineStatus.DO_NOT_DISTURB);
						presence.setActivity(Activity.playing("🐛 En " + status.getName().toLowerCase()));
						break;
					case MAINTENANCE:
					case DEV:
						presence.setStatus(OnlineStatus.DO_NOT_DISTURB);
						presence.setActivity(Activity.playing("🚧 En " + status.getName().toLowerCase()));
						break;
					case OPEN:
						presence.setStatus(OnlineStatus.IDLE);
						presence.setActivity(Activity.playing("✅ Ouvert : play.olympa.fr"));
						break;
					case SOON:
						presence.setStatus(OnlineStatus.DO_NOT_DISTURB);
						presence.setActivity(Activity.playing("🏁 Ouverture bientôt"));
						break;
					case CLOSE:
					case UNKNOWN:
						presence.setStatus(OnlineStatus.DO_NOT_DISTURB);
						presence.setActivity(Activity.playing("❌ Serveur Minecraft Fermé"));
						break;
					default:
						break;

					}
				}
			}
		}, 0, 40000);
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				if (OlympaBots.getInstance().getDiscord().getJda() == null)
					return;
				int usersConnected = 0;
				int usersTotal = 0;
				for (Member member : GuildHandler.getOlympaGuild(DiscordGuildType.PUBLIC).getGuild().getMembers())
					if (!member.getUser().isBot()) {
						if (member.getOnlineStatus() != OnlineStatus.OFFLINE)
							usersConnected++;
						usersTotal++;
					}
				Presence presence = jda.getPresence();
				presence.setActivity(Activity.watching(usersConnected + "/" + usersTotal + " membres"));
			}
		}, 20000, 40000);
		OlympaBots.getInstance().sendMessage("&aConnecté aux discords suivants:");
		for (Guild guild : guilds) {
			StringBuilder sb = new StringBuilder();
			@Nullable
			Consumer<? super Member> consumer = o -> {
				sb.append("&2" + guild.getName() + " avec " + guild.getMemberCount() + " membres. Owner: " + o.getEffectiveName() + "(" + o.getUser().getAsTag() + ", " + o.getId() + ") ");
				guild.retrieveInvites().queue(invites -> {
					if (!invites.isEmpty())
						sb.append(invites.stream().filter(i -> !i.isTemporary()).findFirst().orElse(invites.get(0)));
				});
				sb.append("&7[");
				if (guild.getSelfMember().hasPermission(Permission.ADMINISTRATOR))
					sb.append("&a&lADMIN");
				else
					sb.append("&4&mADMIN");
				sb.append("&7]");
				OlympaBots.getInstance().sendMessage(sb.toString());
			};
			Member owner = guild.getOwner();
			if (owner != null)
				consumer.accept(owner);
			else
				guild.retrieveOwner().queue(consumer);
		}

		OlympaGuild olympaGuild = GuildHandler.getOlympaGuild(DiscordGuildType.PUBLIC);

		Guild defaultGuild = olympaGuild.getGuild();
		Role defaultRole = DiscordGroup.PLAYER.getRole(defaultGuild);
		List<Member> members = defaultGuild.getMembers();
		members.stream().filter(m -> m.getRoles().isEmpty()).forEach(member -> {
			defaultGuild.addRoleToMember(member, defaultRole).queue();
			OlympaBots.getInstance().sendMessage("&c" + member.getUser().getAsTag() + " n'avait pas de roles.");
		});

		EmbedBuilder embed = new EmbedBuilder().setTitle("Bot connecté").setDescription("Je suis de retour.");
		embed.setTimestamp(OffsetDateTime.now());
		embed.setColor(OlympaBots.getInstance().getDiscord().getColor());
		for (OlympaGuild olympaGuilds : GuildHandler.guilds) {
			TextChannel logChannel = olympaGuilds.getLogChannel();
			if (logChannel == null || !olympaGuilds.isStatusMessageEnabled())
				continue;
			logChannel.sendMessage(embed.build()).queue();
		}
		OlympaDiscord.setLastConnection(Utils.getCurrentTimeInSeconds());
		ReactionHandler.initReactions();
		OlympaBots.getInstance().spigotReceiveError.sendErrorsInQueue();

		// DEBUG if some DiscordMember has tag enmpty
		//		LinkSpigotBungee.Provider.link.getTask().runTaskAsynchronously(() -> {
		//			try {
		//				List<DiscordMember> list = DiscordSQL.debug();
		//				System.out.println("list " + list.size());
		//				for (DiscordMember dm : list)
		//					OlympaBots.getInstance().getDiscord().getJda().retrieveUserById(dm.getDiscordId()).queue(u -> {
		//						dm.updateName(u);
		//						try {
		//							DiscordSQL.updateMember(dm);
		//						} catch (SQLException e) {
		//							e.printStackTrace();
		//						}
		//					});
		//			} catch (SQLException e) {
		//				e.printStackTrace();
		//			}
		//		});
	}

	@Override
	public void onStatusChange(StatusChangeEvent event) {
		if (!event.getNewStatus().equals(Status.SHUTTING_DOWN))
			return;
		EmbedBuilder embed = new EmbedBuilder().setTitle("Déconnexion du bot").setDescription("Il est désormais impossible d'utiliser toutes les commandes liés au bot.");
		embed.setColor(OlympaBots.getInstance().getDiscord().getColor());
		embed.setTimestamp(OffsetDateTime.now());
		for (OlympaGuild olympaGuilds : GuildHandler.guilds) {
			TextChannel logChannel = olympaGuilds.getLogChannel();
			if (logChannel == null || !olympaGuilds.isStatusMessageEnabled())
				continue;
			logChannel.sendMessage(embed.build()).queue();
		}
	}
}

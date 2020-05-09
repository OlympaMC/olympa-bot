package fr.olympa.bot.discord.listeners;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import fr.olympa.bot.OlympaBots;
import fr.olympa.bot.discord.api.DiscordIds;
import fr.olympa.bot.discord.groups.DiscordGroup;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDA.Status;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
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
					presence.setActivity(Activity.playing("🚧 " + count + " joueur%s connecté%s".replaceAll("%s", s)));
				} else {
					presence.setActivity(Activity.playing("🚧 En développement"));
				}
				presence.setStatus(OnlineStatus.DO_NOT_DISTURB);
			}
		}, 0, 40000);
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
				Presence presence = jda.getPresence();
				presence.setStatus(OnlineStatus.IDLE);
				presence.setActivity(Activity.watching(usersConnected + "/" + usersTotal + " membres"));
			}
		}, 20000, 40000);
		StringBuilder db = new StringBuilder("&aConnecté aux discords suivants:");
		for (Guild guild : guilds) {
			Member owner = guild.getOwner();
			db.append("\n&2" + guild.getName() + " avec " + guild.getMemberCount() + " membres. Owner: " + owner.getEffectiveName() + "(" + owner.getUser().getAsTag() + ", " + owner.getId() + ") ");
			guild.retrieveInvites().queue(invites -> {
				if (!invites.isEmpty()) {
					db.append(invites.stream().filter(i -> !i.isTemporary()).findFirst().orElse(invites.get(0)));
				}
			});
			db.append("&7[");
			if (guild.getSelfMember().hasPermission(Permission.ADMINISTRATOR)) {
				db.append("&a&lADMIN");
			} else {
				db.append("&4&mADMIN");
			}
			db.append("&7]");
		}
		OlympaBots.getInstance().sendMessage(db.toString());

		Guild defaultGuild = DiscordIds.getDefaultGuild(event.getJDA());
		Role defaultRole = DiscordGroup.PLAYER.getRole(defaultGuild);
		List<Member> members = defaultGuild.getMembers();
		System.out.println("Membres : " + members.size());
		members.stream().filter(m -> m.getRoles().isEmpty()).forEach(member -> {
			defaultGuild.addRoleToMember(member, defaultRole).queue();
			OlympaBots.getInstance().sendMessage("&c" + member.getUser().getAsTag() + " n'avait pas de roles.");
		});

		EmbedBuilder embed = new EmbedBuilder().setTitle("Bot connecté").setDescription("Je suis de retour.");
		embed.setTimestamp(OffsetDateTime.now());
		embed.setColor(OlympaBots.getInstance().getDiscord().getColor());
		MessageChannel channel = DiscordIds.getChannelInfo();
		channel.sendMessage(embed.build()).queue();
//		MessageHistory.getHistoryAfter(channel, channel.getLatestMessageId()).limit(2).queue(historyMsg -> {
//			List<Message> list = historyMsg.getRetrievedHistory();
//			if (!list.isEmpty()) {
//				for (Message histMessage : list) {
//					if (DiscordUtils.isMe(histMessage.getAuthor())) {
//						histMessage.editMessage(embed.build()).queue();
//						return;
//					}
//				}
//			}
//		});
	}

	@Override
	public void onStatusChange(StatusChangeEvent event) {
		if (!event.getNewStatus().equals(Status.SHUTTING_DOWN)) {
			return;
		}
		EmbedBuilder embed = new EmbedBuilder().setTitle("Déconnexion du bot").setDescription("Il est désormais impossible d'utiliser toutes les commandes liés au bot.");
		embed.setColor(OlympaBots.getInstance().getDiscord().getColor());
		embed.setTimestamp(OffsetDateTime.now());
		MessageChannel channel = DiscordIds.getChannelInfo();
		channel.sendMessage(embed.build()).queue();
//		MessageHistory.getHistoryAfter(channel, channel.getLatestMessageId()).limit(2).queue(historyMsg -> {
//			List<Message> list = historyMsg.getRetrievedHistory();
//			if (!list.isEmpty()) {
//				for (Message histMessage : list) {
//					if (DiscordUtils.isMe(histMessage.getAuthor())) {
//						histMessage.editMessage(embed.build()).queue();
//						return;
//					}
//				}
//			}
//		});
	}
}

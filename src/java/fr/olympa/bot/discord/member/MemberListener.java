package fr.olympa.bot.discord.member;

import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import fr.olympa.api.LinkSpigotBungee;
import fr.olympa.api.common.server.ServerStatus;
import fr.olympa.api.utils.Utils;
import fr.olympa.bot.OlympaBots;
import fr.olympa.bot.discord.guild.GuildHandler;
import fr.olympa.bot.discord.guild.OlympaGuild.DiscordGuildType;
import fr.olympa.bot.discord.link.LinkHandler;
import fr.olympa.bot.discord.sql.CacheDiscordSQL;
import fr.olympa.bot.discord.webhook.WebHookHandler;
import fr.olympa.core.bungee.OlympaBungee;
import fr.olympa.core.common.provider.AccountProvider;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceSelfDeafenEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceSelfMuteEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageUpdateEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.user.update.UserUpdateActivityOrderEvent;
import net.dv8tion.jda.api.events.user.update.UserUpdateNameEvent;
import net.dv8tion.jda.api.events.user.update.UserUpdateOnlineStatusEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.ErrorResponse;

public class MemberListener extends ListenerAdapter {

	private long channelUpdateExpiration1 = 0;
	private long channelUpdateExpiration2 = 0;
	private boolean task = false;

	/*
		@Override
		public void onGuildMemberUpdateBoostTime(GuildMemberUpdateBoostTimeEvent event) {
			Guild guild = event.getGuild();
			Member member = event.getMember();
			TextChannel defaultChannel = guild.getDefaultChannel();
			OlympaGuild opGuild = GuildHandler.getOlympaGuild(guild);
			if (opGuild == null || !opGuild.isSendingWelcomeMessage() || defaultChannel == null)
				return;
			EmbedBuilder em = new EmbedBuilder();
			em.setDescription(member.getAsMention() + " vient de boost le serveur !");
			defaultChannel.sendMessageEmbeds(em.build()).queue();
		}
	*/
	@Override
	public void onGuildMemberJoin(GuildMemberJoinEvent event) {
		Guild guild = event.getGuild();
		Member member = event.getMember();
		User user = member.getUser();
		DiscordGuildType type = GuildHandler.getOlympaGuild(guild).getType();
		if (type != DiscordGuildType.PUBLIC && type != DiscordGuildType.STAFF || member.getUser().isBot())
			return;
		try {
			DiscordMember discordMember = CacheDiscordSQL.getDiscordMember(user);
			discordMember.updateLastSeen();
			if (type == DiscordGuildType.STAFF) {
				EmbedBuilder em = new EmbedBuilder();
				em.setTitle("Bienvenue sur le Discord du Staff de Olympa " + member.getEffectiveName() + " !");
				em.setDescription("Tu fais parti de la confidence maintenant.\n\nLe channel général & sérieux :\n<#642802548629176358>\nLe channel de détente :\n<#643038179070312448>.");
				if (discordMember.getOlympaId() == 0)
					em.appendDescription("\n⚠️ Tu dois relier ton compte Minecraft & Discord avec la commande **/discord link** sur le serveur pour accéder aux channels du staff.");
				em.setColor(OlympaBots.getInstance().getDiscord().getColor());
				member.getUser().openPrivateChannel().queue(ch -> ch.sendMessageEmbeds(em.build()).queue(null, ErrorResponseException.ignore(ErrorResponse.CANNOT_SEND_TO_USER)));
				WebhookMessageBuilder messageBuilder = new WebhookMessageBuilder();
				EmbedBuilder messageEmbed = new EmbedBuilder();
				messageEmbed.setDescription("Bienvenue à " + member.getAsMention() + " !");

				messageBuilder.addEmbeds(WebHookHandler.convertEmbed(messageEmbed.build()));
				//								messageBuilder.setContent(DiscordPermission.HIGH_STAFF.getAllow().stream().map(g -> g.getRole(guild).getAsMention()).collect(Collectors.joining(", ")));
				WebHookHandler.send(messageBuilder, guild.getDefaultChannel(),
						"Console", "https://c7.uihere.com/files/250/925/132/computer-terminal-linux-console-computer-icons-command-line-interface-linux.jpg", null);
			} else {
				long usersTotal = updateChannelMember(guild);
				EmbedBuilder em = new EmbedBuilder();
				em.setTitle("Bienvenue sur notre discord " + member.getEffectiveName() + " !");
				em.setDescription("Tu es le " + usersTotal + " ème membre à rejoindre le discord.\n");
				ServerStatus status = OlympaBungee.getInstance().getStatus();
				if (status != null) {
					switch (status) {
					case BETA:
						em.appendDescription("Notre serveur minecraft est actuellement en bêta.\n Suis les dernières informations dans <#558148715286888448>.");
						break;
					case CLOSE:
						em.appendDescription("❌ Notre serveur minecraft est actuellement indisponible. Suis les dernières informations dans <#558148715286888448>.");
						break;
					case CLOSE_BETA:
						em.appendDescription("❌ Notre serveur minecraft est actuellement en bêta fermée. Suis les dernières informations dans <#558148715286888448>.");
						break;
					case DEV:
						em.appendDescription("❌ Notre serveur minecraft est actuellement en développement, suis les dernières informations dans <#558148715286888448>.");
						break;
					case MAINTENANCE, UNKNOWN:
						em.appendDescription("Notre serveur minecraft est actuellement en maintenance.\n Suis les dernières informations dans <#558148715286888448>.");
						break;
					case SOON:
						em.appendDescription("Notre serveur minecraft va bientôt ouvrir.\n Suis les dernières informations dans <#558148715286888448>.");
						break;
					default:
						break;
					}
					em.appendDescription("\nIP du serveur Minecraft `play.olympa.fr`");
				}
				em.setColor(OlympaBots.getInstance().getDiscord().getColor());
				member.getUser().openPrivateChannel().queue(ch -> ch.sendMessageEmbeds(em.build()).queue(null, ErrorResponseException.ignore(ErrorResponse.CANNOT_SEND_TO_USER)));
				discordMember.updateJoinTime(member.getTimeJoined().toEpochSecond());
				//				DiscordSQL.updateMember(discordMember);
			}
			if (discordMember.getOlympaId() != 0)
				LinkSpigotBungee.Provider.link.getTask().runTaskLater(() -> {
					try {
						LinkHandler.updateGroups(member, AccountProvider.getter().get(discordMember.getOlympaId()));
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}, 5, TimeUnit.SECONDS);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onGuildMemberRemove(GuildMemberRemoveEvent event) {
		Guild guild = event.getGuild();
		User user = event.getUser();
		if (GuildHandler.getOlympaGuild(guild).getType() == DiscordGuildType.PUBLIC)
			updateChannelMember(guild);
		try {
			DiscordMember discordMember = CacheDiscordSQL.getDiscordMember(user);
			discordMember.updateLeaveTime(Utils.getCurrentTimeInSeconds());
			discordMember.updateLastSeen();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public long updateChannelMember(Guild defaultGuild) {
		long usersTotal = defaultGuild.getMembers().stream().filter(member -> !member.getUser().isBot()).count();
		long time1 = channelUpdateExpiration1 - System.currentTimeMillis();
		long time2 = channelUpdateExpiration2 - System.currentTimeMillis();
		if (time1 > 0 && time2 > 0) {
			if (!task) {
				task = true;
				OlympaBungee.getInstance().getTask().runTaskLater(() -> updateChannelMember(defaultGuild), time1);
			}
		} else {
			GuildChannel membersChannel = defaultGuild.getChannels().stream().filter(c -> c.getIdLong() == 589164145664851972L).findFirst().orElse(null);
			if (membersChannel != null)
				membersChannel.getManager().setName("Membres : " + usersTotal).queue();
			long expiration = System.currentTimeMillis() + 10 * 60 * 1000;
			if (time1 > 0)
				channelUpdateExpiration1 = expiration;
			else
				channelUpdateExpiration2 = expiration;
		}
		return usersTotal;
	}

	@Override
	public void onUserUpdateName(UserUpdateNameEvent event) {
		User user = event.getUser();
		if (user.isSystem())
			return;
		try {
			DiscordMember discordMember = CacheDiscordSQL.getDiscordMember(user);
			discordMember.updateName(user);
			discordMember.updateLastSeen();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	// Only custom
	@Override
	public void onUserUpdateActivityOrder(UserUpdateActivityOrderEvent event) {
		User user = event.getEntity();
		if (user.isSystem() || user.isBot())
			return;
		System.out.println("UserUpdateActivityOrderEvent " + user.getAsTag());
		try {
			DiscordMember discordMember = CacheDiscordSQL.getDiscordMember(user);
			discordMember.updateLastSeen();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		System.out.println("DEBUG Activity de " + user.getAsTag() + " : ");
		for (Activity value : event.getNewValue())
			System.out.println("Name " + value.getName() + " Type " + value.getType().name() + " URL " + value.getUrl() + " Emoji " + value.getEmoji()
					+ (value.getTimestamps() != null ? " Depuis " + Utils.timestampToDuration(value.getTimestamps().getStart())
							+ (value.getTimestamps().getEnd() != 0 ? " Termine dans  " + Utils.timestampToDuration(value.getTimestamps().getEnd()) : "") : ""));
	}

	@Override
	public void onUserUpdateOnlineStatus(UserUpdateOnlineStatusEvent event) {
		User user = event.getEntity();
		if (user.isSystem() || user.isBot())
			return;
		try {
			DiscordMember discordMember = CacheDiscordSQL.getDiscordMember(user);
			discordMember.updateLastSeen();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
		User user = event.getAuthor();
		if (user.isSystem() || user.isBot())
			return;
		try {
			DiscordMember discordMember = CacheDiscordSQL.getDiscordMember(user);
			discordMember.updateLastSeen();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onGuildMessageUpdate(GuildMessageUpdateEvent event) {
		User user = event.getAuthor();
		if (user.isSystem() || user.isBot())
			return;
		try {
			DiscordMember discordMember = CacheDiscordSQL.getDiscordMember(user);
			discordMember.updateLastSeen();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onGuildVoiceJoin(GuildVoiceJoinEvent event) {
		User user = event.getEntity().getUser();
		if (user.isSystem() || user.isBot())
			return;
		try {
			DiscordMember discordMember = CacheDiscordSQL.getDiscordMember(user);
			discordMember.updateLastSeen();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onGuildVoiceLeave(GuildVoiceLeaveEvent event) {
		User user = event.getEntity().getUser();
		if (user.isSystem() || user.isBot())
			return;
		try {
			DiscordMember discordMember = CacheDiscordSQL.getDiscordMember(user);
			discordMember.updateLastSeen();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onMessageReactionAdd(MessageReactionAddEvent event) {
		User user = event.getMember().getUser();
		if (user.isSystem() || user.isBot())
			return;
		try {
			DiscordMember discordMember = CacheDiscordSQL.getDiscordMember(user);
			discordMember.updateLastSeen();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onGuildVoiceSelfMute(GuildVoiceSelfMuteEvent event) {
		User user = event.getMember().getUser();
		if (user.isSystem() || user.isBot())
			return;
		try {
			DiscordMember discordMember = CacheDiscordSQL.getDiscordMember(user);
			discordMember.updateLastSeen();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onGuildVoiceSelfDeafen(GuildVoiceSelfDeafenEvent event) {
		User user = event.getMember().getUser();
		if (user.isSystem() || user.isBot())
			return;
		try {
			DiscordMember discordMember = CacheDiscordSQL.getDiscordMember(user);
			discordMember.updateLastSeen();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}

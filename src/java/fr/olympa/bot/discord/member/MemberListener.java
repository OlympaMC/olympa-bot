package fr.olympa.bot.discord.member;

import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import fr.olympa.api.LinkSpigotBungee;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.utils.Utils;
import fr.olympa.bot.OlympaBots;
import fr.olympa.bot.discord.guild.GuildHandler;
import fr.olympa.bot.discord.guild.OlympaGuild.DiscordGuildType;
import fr.olympa.bot.discord.link.LinkHandler;
import fr.olympa.bot.discord.sql.CacheDiscordSQL;
import fr.olympa.bot.discord.sql.DiscordSQL;
import fr.olympa.bot.discord.webhook.WebHookHandler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageUpdateEvent;
import net.dv8tion.jda.api.events.user.update.UserUpdateActivityOrderEvent;
import net.dv8tion.jda.api.events.user.update.UserUpdateNameEvent;
import net.dv8tion.jda.api.events.user.update.UserUpdateOnlineStatusEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.ErrorResponse;

public class MemberListener extends ListenerAdapter {

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
			if (type == DiscordGuildType.STAFF) {
				EmbedBuilder em = new EmbedBuilder();
				em.setTitle("Bienvenue sur le Discord du Staff de Olympa " + member.getEffectiveName() + " !");
				em.setDescription("Tu fais parti de la confidence maintenant.\n\nLe channel général & sérieux :\n<#642802548629176358>\nLe channel de détente :\n<#643038179070312448>.");
				if (discordMember.getOlympaId() == 0)
					em.appendDescription("\n⚠️ Tu dois relier ton compte Minecraft & Discord avec la commande **/discord link** sur le serveur pour accéder aux channels du staff.");
				em.setColor(OlympaBots.getInstance().getDiscord().getColor());
				member.getUser().openPrivateChannel().queue(ch -> ch.sendMessage(em.build()).queue(null, ErrorResponseException.ignore(ErrorResponse.CANNOT_SEND_TO_USER)));
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
				em.setDescription("Tu es le " + usersTotal + " ème membre à rejoindre le discord.\n❌ Le serveur est actuellement en développement, suit les dernières informations dans <#558148715286888448>.");
				em.setColor(OlympaBots.getInstance().getDiscord().getColor());
				member.getUser().openPrivateChannel().queue(ch -> ch.sendMessage(em.build()).queue(null, ErrorResponseException.ignore(ErrorResponse.CANNOT_SEND_TO_USER)));
				discordMember.updateJoinTime(member.getTimeJoined().toEpochSecond());
				DiscordSQL.updateMember(discordMember);
			}
			if (discordMember.getOlympaId() != 0)
				LinkSpigotBungee.Provider.link.getTask().runTaskLater(() -> {
					try {
						LinkHandler.updateGroups(member, AccountProvider.get(discordMember.getOlympaId()));
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
		Member member = event.getMember();
		if (GuildHandler.getOlympaGuild(guild).getType() != DiscordGuildType.PUBLIC)
			return;
		updateChannelMember(guild);
		User user = member.getUser();
		try {
			DiscordMember discordMember = CacheDiscordSQL.getDiscordMember(user);
			discordMember.updateLeaveTime(Utils.getCurrentTimeInSeconds());
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public long updateChannelMember(Guild defaultGuild) {
		int usersTotal = 0;
		for (Member user2 : defaultGuild.getMembers())
			if (!user2.getUser().isBot())
				usersTotal++;
		GuildChannel membersChannel = defaultGuild.getChannels().stream().filter(c -> c.getIdLong() == 589164145664851972L).findFirst().orElse(null);
		if (membersChannel != null)
			membersChannel.getManager().setName("Membres : " + usersTotal).queue();
		return usersTotal;
	}

	@Override
	public void onUserUpdateName(UserUpdateNameEvent event) {
		User user = event.getUser();
		if (user.isFake())
			return;
		try {
			DiscordMember discordMember = CacheDiscordSQL.getDiscordMember(user);
			discordMember.updateName(user);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	// NOT LOAD
	@Override
	public void onUserUpdateActivityOrder(UserUpdateActivityOrderEvent event) {
		User user = event.getEntity();
		if (user.isFake())
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
							+ (value.getTimestamps().getEnd() != 0 ? " Termine " + Utils.timestampToDuration(value.getTimestamps().getEnd()) : "") : ""));
	}

	@Override
	public void onUserUpdateOnlineStatus(UserUpdateOnlineStatusEvent event) {
		User user = event.getEntity();
		if (user.isFake())
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
		if (user.isFake())
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
		try {
			DiscordMember discordMember = CacheDiscordSQL.getDiscordMember(user);
			discordMember.updateLastSeen();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}

package fr.olympa.bot.discord.member;

import java.sql.SQLException;

import fr.olympa.api.utils.Utils;
import fr.olympa.bot.OlympaBots;
import fr.olympa.bot.discord.guild.GuildHandler;
import fr.olympa.bot.discord.guild.OlympaGuild.DiscordGuildType;
import fr.olympa.bot.discord.sql.CacheDiscordSQL;
import fr.olympa.bot.discord.sql.DiscordSQL;
import net.dv8tion.jda.api.EmbedBuilder;
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
		if (GuildHandler.getOlympaGuild(guild).getType() != DiscordGuildType.PUBLIC)
			return;
		long usersTotal = updateChannelMember(guild);
		Member member = event.getMember();
		User user = member.getUser();
		EmbedBuilder em = new EmbedBuilder();
		em.setTitle("Bienvenue sur notre discord " + member.getEffectiveName() + " !");
		em.setDescription("Tu es le " + usersTotal + " ème membre à rejoindre le discord.\n❌ Le serveur est actuellement en développement, suit les dernières informations dans <#558148715286888448>.");
		em.setColor(OlympaBots.getInstance().getDiscord().getColor());
		member.getUser().openPrivateChannel().queue(ch -> ch.sendMessage(em.build()).queue(null, ErrorResponseException.ignore(ErrorResponse.CANNOT_SEND_TO_USER)));
		try {
			DiscordMember discordMember = CacheDiscordSQL.getDiscordMember(user);
			discordMember.updateJoinTime(member.getTimeJoined().toEpochSecond());
			DiscordSQL.updateMember(discordMember);
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
			DiscordSQL.updateMember(discordMember);
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
			DiscordSQL.updateMember(discordMember);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onUserUpdateActivityOrder(UserUpdateActivityOrderEvent event) {
		User user = event.getEntity();
		if (user.isFake())
			return;
		try {
			DiscordMember discordMember = CacheDiscordSQL.getDiscordMember(user);
			long lastSeenTime = discordMember.getLastSeenTime();
			discordMember.updateLastSeen();
			if (lastSeenTime == -1 || lastSeenTime > 60 * 60)
				DiscordSQL.updateMember(discordMember);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onUserUpdateOnlineStatus(UserUpdateOnlineStatusEvent event) {
		User user = event.getEntity();
		if (user.isFake())
			return;
		try {
			DiscordMember discordMember = CacheDiscordSQL.getDiscordMember(user);
			long lastSeenTime = discordMember.getLastSeenTime();
			discordMember.updateLastSeen();
			if (lastSeenTime == -1 || lastSeenTime > 60 * 60)
				DiscordSQL.updateMember(discordMember);
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
			long lastSeenTime = discordMember.getLastSeenTime();
			discordMember.updateLastSeen();
			if (lastSeenTime == -1 || lastSeenTime > 60 * 60)
				DiscordSQL.updateMember(discordMember);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onGuildMessageUpdate(GuildMessageUpdateEvent event) {
		User user = event.getAuthor();
		try {
			DiscordMember discordMember = CacheDiscordSQL.getDiscordMember(user);
			long lastSeenTime = discordMember.getLastSeenTime();
			discordMember.updateLastSeen();
			if (lastSeenTime == -1 || lastSeenTime > 60 * 60)
				DiscordSQL.updateMember(discordMember);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}

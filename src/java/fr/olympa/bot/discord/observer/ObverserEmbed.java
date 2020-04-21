package fr.olympa.bot.discord.observer;

import java.time.OffsetDateTime;

import fr.olympa.bot.OlympaBots;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;

public class ObverserEmbed {

	public static EmbedBuilder get(String title, String titleUrl, String description, Member member) {
		User user = member.getUser();
		EmbedBuilder embed = new EmbedBuilder().setTitle(title, titleUrl).setDescription(description);
		embed.setAuthor(member.getEffectiveName(), user.getAvatarUrl(), user.getEffectiveAvatarUrl());
		embed.setFooter(user.getAsTag() + " | " + member.getId());
		embed.setColor(OlympaBots.getInstance().getDiscord().getColor());
		embed.setThumbnail(user.getAvatarUrl());
		embed.setTimestamp(OffsetDateTime.now());
		return embed;
	}
}

package fr.olympa.bot.discord.listener;

import fr.olympa.bot.OlympaBots;
import fr.olympa.bot.discord.api.DiscordUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberLeaveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class JoinListener extends ListenerAdapter {

	@Override
	public void onGuildMemberJoin(GuildMemberJoinEvent event) {
		Guild guild = event.getGuild();
		if (!DiscordUtils.isDefaultGuild(guild)) {
			return;
		}
		int usersTotal = 0;
		for (User user2 : event.getJDA().getUsers()) {
			if (!user2.isBot()) {
				usersTotal++;
			}
		}
		GuildChannel membersChannel = guild.getChannels().stream().filter(c -> c.getIdLong() == 589164145664851972L).findFirst().orElse(null);
		membersChannel.getManager().setName("Membres : " + usersTotal).queue();

		Member member = event.getMember();
		EmbedBuilder em = new EmbedBuilder();
		em.setTitle("Bienvenue sur notre discord " + member.getEffectiveName() + " !");
		em.setDescription("Tu es le " + usersTotal + " ème membre à rejoindre le discord.\n❌ Le serveur est actuellement en développement, suit les dernières informations dans <#558148715286888448>.");
		em.setColor(OlympaBots.getInstance().getDiscord().getColor());
		member.getUser().openPrivateChannel().queue(ch -> ch.sendMessage(em.build()).queue());
	}

	@Override
	public void onGuildMemberLeave(GuildMemberLeaveEvent event) {
		Guild guild = event.getGuild();
		if (!DiscordUtils.isDefaultGuild(guild)) {
			return;
		}
		int usersTotal = 0;
		for (User user2 : event.getJDA().getUserCache()) {
			if (!user2.isBot()) {
				usersTotal++;
			}
		}
		GuildChannel membersChannel = guild.getChannels().stream().filter(c -> c.getIdLong() == 589164145664851972L).findFirst().orElse(null);
		membersChannel.getManager().setName("Membres : " + usersTotal).queue();
	}
}

package fr.olympa.bot.discord.link;

import fr.olympa.api.objects.OlympaPlayer;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.bot.discord.api.DiscordIds;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class LinkListener extends ListenerAdapter {

	@Override
	public void onPrivateMessageReceived(PrivateMessageReceivedEvent event) {
		User user = event.getAuthor();
		if (user.isBot()) {
			return;
		}
		Message message = event.getMessage();
		MessageChannel channel = message.getChannel();
		String msg = message.getContentRaw();
		String code = msg.split(" ")[0];

		ProxiedPlayer player = LinkHandler.getPlayer(code);
		if (player == null || !player.isConnected()) {
			EmbedBuilder embed = new EmbedBuilder();
			embed.setTitle("Bonjour " + user.getName());
			embed.setDescription("Pour relier ton compte Olympa et ton compte Discord, fait **/discord link** sur Minecraft et donne moi ici le code obtenu.");
			channel.sendMessage(embed.build()).queue();
			return;
		}

		Member member = DiscordIds.getStaffGuild().getMemberById(user.getIdLong());
		AccountProvider account = new AccountProvider(player.getUniqueId());
		OlympaPlayer olympaPlayer = account.getFromRedis();
		olympaPlayer.setDiscordId(user.getIdLong());
		account.saveToRedis(olympaPlayer);

		if (member == null) {
			EmbedBuilder embed = new EmbedBuilder();
			embed.setTitle("Bonjour " + user.getName());
			embed.setDescription("Tu dois rejoindre le discord avant de vouloir liée ton compte. http:///discord.olympa.fr");
			channel.sendMessage(embed.build()).queue();
			return;
		}
		member.modifyNickname(olympaPlayer.getName()).queue();
		LinkHandler.updateGroups(member, olympaPlayer);
		EmbedBuilder embed = new EmbedBuilder();
		embed.setTitle("Bonjour " + user.getName());
		embed.setDescription("Tu a relié ton compte Olympa " + player.getName() + " avec ton compte discord " + user.getAsMention() + ". Tu as reçu les bons rôle sur discord.");
		channel.sendMessage(embed.build()).queue();

	}

}

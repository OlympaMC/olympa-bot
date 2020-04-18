package fr.olympa.bot.discord.webhook;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import fr.olympa.bot.discord.api.DiscordIds;
import net.dv8tion.jda.api.entities.Icon;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.Webhook;
import net.dv8tion.jda.api.entities.WebhookType;
import net.dv8tion.jda.api.managers.WebhookManager;
import net.dv8tion.jda.internal.entities.WebhookImpl;

public class WebHookHandler {

	public void test() {

		String name = "test";
		TextChannel channel = DiscordIds.getChannelInfo();
		User user = channel.getJDA().getUserById(217682399234883584L);

		channel.getGuild().retrieveWebhooks().queue(wbs -> {
			Webhook webhook = wbs.stream().filter(wb -> wb.getName().equals(name)).findFirst().orElse(null);
			if (webhook == null) {
				channel.createWebhook(name).queue(wb -> test());
				return;
			}

			WebhookManager man = webhook.getManager();
			man.setName(user.getName()).queue();
			File file = Paths.get(user.getEffectiveAvatarUrl()).toFile();
			try {
				man.setAvatar(Icon.from(file)).queue();
			} catch (IOException e) {
				e.printStackTrace();
			}
			man.setChannel(channel).queue();

			WebhookImpl wb = new WebhookImpl(channel, webhook.getIdLong(), WebhookType.INCOMING).setToken(webhook.getToken());
		});
	}

}

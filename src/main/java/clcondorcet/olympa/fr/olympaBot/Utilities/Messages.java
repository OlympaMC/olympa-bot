package clcondorcet.olympa.fr.olympaBot.Utilities;

import java.awt.Color;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import clcondorcet.olympa.fr.olympaBot.Main;
import clcondorcet.olympa.fr.olympaBot.Functions.WelcomeMessages;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.entities.MessageEmbed.Field;

/**
	 * A temporary class that stores messages (text and embed) waiting to be send.
	 * A new form need to be found.
	 * 
	 * @author clcondorcet
	 * @since 0.0.3
	 */
public class Messages {

	/**
	 * Send the WelcomeMessage.
	 * @see WelcomeMessages
	 * 
	 * @param channel Channel to send the message.
	 * @param joined The name as mention of the member who join.
	 * @param inviter The name as mention of the inviter of this member.
	 * @param invites The number of invites the inviter does.
	 * @param allmembers The number of members in the 
	 */
	public static void sendWelcomeMessage(TextChannel channel, String joined, String inviter, int invites, int allmembers){
    	channel.sendMessage(new EmbedBuilder()
    			.setAuthor("OlympaBot", "https://olympa.net---BROKEN", "https://cdn.discordapp.com/app-icons/614936649096233160/d02b7707f95487df0db947deb62d33a6.png")
    			.setColor(new Color(255, 153, 51))
    			.setDescription("Bienvenue à toi " + joined + " sur Olympa !")
    			.addField(new Field("", "• Tu as été invité par: " + inviter + ", qui a lui: " + invites + " invitations !", false))
    			.addField(new Field("", "» Grâce à toi, notre Discord à atteint les " + allmembers + " joueurs !", false))
    			.setFooter("OlympaBot", null)
    			.setTimestamp(new Date(System.currentTimeMillis()).toInstant())
    			.build()).queue();
	}
	
	public static void sendTestMessage(TextChannel channel, String sender, int members){
		channel.sendMessage(new EmbedBuilder()
				.setAuthor("OlympaBot", "https://olympa.net---BROKEN", "https://cdn.discordapp.com/app-icons/614936649096233160/d02b7707f95487df0db947deb62d33a6.png")
				.setColor(new Color(255, 153, 51))
				.setDescription("Bienvenue à toi " + sender + " sur Olympa !")
				.addField(new Field("", "• Tu as été invité par: " + sender + ", qui a lui: " + "x" + " invitations !", false))
				.addField(new Field("", "» Grâce à toi, notre Discord à atteint les " + members + " joueurs !", false))
				.setFooter("OlympaBot", null)
				.setTimestamp(new Date(System.currentTimeMillis()).toInstant())
				.build()).queue();
	}
	
	public static void sendServeursMessage(TextChannel channel, String sender){
		String ss = "";
		for(Guild guild : Main.jda.getGuilds()){
			ss += "- " + guild.getName() + " (" + guild.getId() + ")\n";
		}
		Message msg = new MessageBuilder(sender + "\n"
				+ "Listes des serveurs:\n"
				+ "il y a " + Main.jda.getGuilds().size() + " serveurs\n" 
				+ ss
				).build();
		channel.sendMessage(msg).queue();
	}
	
	public static void sendInvitesMessage(TextChannel channel, User who, int invites, int iBonus, int iLeaves, int iFakes){
		MessageEmbed msg = new EmbedBuilder()
				.setColor(new Color(255, 153, 51))
				.setAuthor(who.getName(), null, who.getAvatarUrl())
				.addField(new Field("Invitation(s) comptabilisé(s):", (invites + iBonus - iLeaves) + "", true))
				.addField(new Field("Toutes:", (invites + iFakes) + "", true))
				.addBlankField(true)
				.addField(new Field("Bonus:", (iBonus) + "", true))
				.addField(new Field("Fake(s)/Bot(s):", (iFakes) + "", true))
				.addField(new Field("Parti(s):", (iLeaves) + "", true))
				.setFooter("OlympaBot", "https://cdn.discordapp.com/app-icons/614936649096233160/d02b7707f95487df0db947deb62d33a6.png")
				.setTimestamp(new Date(System.currentTimeMillis()).toInstant())
				.build();
		channel.sendMessage(msg).queue();
	}
	
	public static void sendAddBonusMessage(TextChannel channel, int bonusBefore, int bonusAdd){
		MessageEmbed msg = new EmbedBuilder()
				.setColor(new Color(0, 255, 0))
				.addField(new Field("Invitations bonus:", bonusBefore + "", true))
				.addField(new Field("Bonus ajouté", bonusAdd + "", true))
				.addField(new Field("Résultat", (bonusAdd + bonusBefore) + "", true))
				.addField(new Field("", "Ce message va s'autodétruire dans 20 secondes KABOUUUM", false))
				.setFooter("OlympaBot", "https://cdn.discordapp.com/app-icons/614936649096233160/d02b7707f95487df0db947deb62d33a6.png")
				.setTimestamp(new Date(System.currentTimeMillis()).toInstant())
				.build();
		channel.sendMessage(msg).queue(msgg -> msgg.delete().queueAfter(20, TimeUnit.SECONDS) );
	}
	
	public static void sendRemoveBonusMessage(TextChannel channel, int bonusBefore, int bonusRemove){
		MessageEmbed msg = new EmbedBuilder()
				.setColor(new Color(0, 255, 0))
				.addField(new Field("Invitations bonus:", bonusBefore + "", true))
				.addField(new Field("Bonus enlevé", bonusRemove + "", true))
				.addField(new Field("Résultat", (bonusBefore - bonusRemove) + "", true))
				.addField(new Field("", "Ce message va s'autodétruire dans 20 secondes KABOUUUM", false))
				.setFooter("OlympaBot", "https://cdn.discordapp.com/app-icons/614936649096233160/d02b7707f95487df0db947deb62d33a6.png")
				.setTimestamp(new Date(System.currentTimeMillis()).toInstant())
				.build();
		channel.sendMessage(msg).queue(msgg -> msgg.delete().queueAfter(20, TimeUnit.SECONDS) );
	}
	
	public static void sendMessage(TextChannel channel, String message){
		Message msg = new MessageBuilder(message).build();
		channel.sendMessage(msg).queue();
	}
	
	public static void sendMessage(TextChannel channel, String message, int kaboum){
		Message msg = new MessageBuilder(message).build();
		channel.sendMessage(msg).queue(msgg -> msgg.delete().queueAfter(kaboum, TimeUnit.SECONDS) );
	}
	
	public static void sendErrorMessage(TextChannel channel, String message){
		MessageEmbed msg = new EmbedBuilder()
				.setColor(new Color(255, 0, 0))
				.addField(new Field("Erreur", message, false))
				.setFooter("OlympaBot", "https://cdn.discordapp.com/app-icons/614936649096233160/d02b7707f95487df0db947deb62d33a6.png")
				.setTimestamp(new Date(System.currentTimeMillis()).toInstant())
				.build();
		channel.sendMessage(msg).queue();
	}
	
	public static void sendErrorMessage(TextChannel channel, String message, int kaboum){
		MessageEmbed msg = new EmbedBuilder()
				.setColor(new Color(255, 0, 0))
				.addField(new Field("Erreur", message, false))
				.addField(new Field("", "Ce message va s'autodétruire dans " + kaboum + " secondes KABOUUUM", false))
				.setFooter("OlympaBot", "https://cdn.discordapp.com/app-icons/614936649096233160/d02b7707f95487df0db947deb62d33a6.png")
				.setTimestamp(new Date(System.currentTimeMillis()).toInstant())
				.build();
		channel.sendMessage(msg).queue(msgg -> msgg.delete().queueAfter(kaboum, TimeUnit.SECONDS) );
	}
	
	public static void sendDoneMessage(TextChannel channel, String message){
		MessageEmbed msg = new EmbedBuilder()
				.setColor(new Color(255, 0, 0))
				.addField(new Field("Erreur", message, false))
				.setFooter("OlympaBot", "https://cdn.discordapp.com/app-icons/614936649096233160/d02b7707f95487df0db947deb62d33a6.png")
				.setTimestamp(new Date(System.currentTimeMillis()).toInstant())
				.build();
		channel.sendMessage(msg).queue();
	}
	
	public static void sendDoneMessage(TextChannel channel, String message, int kaboum){
		MessageEmbed msg = new EmbedBuilder()
				.setColor(new Color(0, 255, 0))
				.addField(new Field("Fait !", message, false))
				.addField(new Field("", "Ce message va s'autodétruire dans " + kaboum + " secondes KABOUUUM", false))
				.setFooter("OlympaBot", "https://cdn.discordapp.com/app-icons/614936649096233160/d02b7707f95487df0db947deb62d33a6.png")
				.setTimestamp(new Date(System.currentTimeMillis()).toInstant())
				.build();
		channel.sendMessage(msg).queue(msgg -> msgg.delete().queueAfter(kaboum, TimeUnit.SECONDS) );
	}
	
}

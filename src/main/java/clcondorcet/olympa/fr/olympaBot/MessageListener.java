package clcondorcet.olympa.fr.olympaBot;

import java.awt.Color;
import java.sql.SQLException;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.MessageEmbed.Field;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

/**
 * @author clcondorcet
 *
 * 
 */
public class MessageListener extends ListenerAdapter {
	
	@Override
	public void onMessageReceived(MessageReceivedEvent e) {
		if(e.getChannelType().equals(ChannelType.TEXT) && e.getMessage().getContentDisplay().startsWith("!")) {
    		String message = e.getMessage().getContentDisplay().replaceFirst("!", "");
    		String args[] = message.split(" ");
    		if(e.getMember().hasPermission(Permission.ADMINISTRATOR) && args[0].equalsIgnoreCase("test")) {
    			e.getChannel().sendMessage(new EmbedBuilder()
    					.setAuthor("OlympaBot", "https://olympa.net---BROKEN", "https://cdn.discordapp.com/app-icons/614936649096233160/d02b7707f95487df0db947deb62d33a6.png")
    					.setColor(new Color(255, 153, 51))
    					.setDescription("Bienvenue à toi " + e.getAuthor().getAsMention() + " sur Olympa !")
    					.addField(new Field("", "• Tu as été invité par: " + e.getAuthor().getAsMention() + ", qui a lui: " + "x" + " invitations !", false))
    					.addField(new Field("", "» Grâce à toi, notre Discord à atteint les " + e.getGuild().getMembers().size() + " joueurs !", false))
    					.setFooter("OlympaBot", null)
    					.setTimestamp(new Date(System.currentTimeMillis()).toInstant())
    					.build()).queue();
    		}else if(e.getMember().hasPermission(Permission.ADMINISTRATOR) && args[0].equalsIgnoreCase("serveurs")) {
    			e.getMessage().delete().queue();
    			String ss = "";
    			for(Guild guild : Main.jda.getGuilds()){
					ss += "- " + guild.getName() + " (" + guild.getId() + ")\n";
				}
    			Message msg = new MessageBuilder(e.getAuthor().getAsMention() + "\n"
    					+ "Listes des serveurs:\n"
    					+ "il y a " + Main.jda.getGuilds().size() + " serveurs\n" 
    					+ ss
    					).build();
    			e.getChannel().sendMessage(msg).queue();
    		}else if(e.getMember().hasPermission(Permission.ADMINISTRATOR) && args[0].equalsIgnoreCase("setChannel")) {
    			try {
					Main.database.set("UPDATE `connectionsChannels` SET channel='" + e.getChannel().getId() + "' WHERE id='" + e.getGuild().getId() + "';");
					Main.idsChannels.put(e.getGuild().getId(), e.getChannel().getId());
					e.getMessage().delete().queue();
					Message msg = new MessageBuilder(e.getAuthor().getAsMention() + "\n"
	    					+ "Le channel ou les messages seront émis a bien été mis à jour ! ;)\n"
	    					+ "\n"
	    					+ "Ce message va s'autodétruire dans 10 secondes KABOUUUM !").build();
					e.getChannel().sendMessage(msg).queue(msgg -> msgg.delete().queueAfter(10, TimeUnit.SECONDS) );
    			} catch (SQLException e1) {
    				e1.printStackTrace();
    				e.getMessage().delete().queue();
    				Message msg = new MessageBuilder(e.getAuthor().getAsMention() + "\n"
	    					+ "Erreur ! Veuillez ressayer plus tard.\n"
	    					+ "\n"
	    					+ "Ce message va s'autodétruire dans 10 secondes KABOUUUM !").build();
    				e.getChannel().sendMessage(msg).queue(msgg -> msgg.delete().queueAfter(10, TimeUnit.SECONDS) );
				}
    		}else if(args[0].equalsIgnoreCase("invite") || args[0].equalsIgnoreCase("invites")){
    			User who = e.getAuthor();
    			if(args.length > 1){
    				if(args[1].startsWith("@")){
    					args[1] = args[1].replaceFirst("@", "");
    				}
    				try{
    					who = getUserByName(e.getGuild(), args[1], false);
    				}catch(NullPointerException ex){
    					MessageEmbed msg = new EmbedBuilder()
        						.setColor(new Color(255, 0, 0))
        						.addField(new Field("Erreur", "\"" + args[1] + "\" est introuvable.", false))
        						.addField(new Field("", "Ce message va s'autodétruire dans 10 secondes KABOUUUM", false))
        						.setFooter("OlympaBot", "https://cdn.discordapp.com/app-icons/614936649096233160/d02b7707f95487df0db947deb62d33a6.png")
        						.setTimestamp(new Date(System.currentTimeMillis()).toInstant())
        						.build();
        				e.getChannel().sendMessage(msg).queue(msgg -> msgg.delete().queueAfter(10, TimeUnit.SECONDS) );
        				return;
    				}
    			}
    			int invites = InvitationListener.getNumberOfInvites(e.getGuild(), who.getId());
    			int iFakes = InvitationListener.getNumberOfInvitesFakes(e.getGuild(), who.getId());
    			int iBonus = InvitationListener.getNumberOfInvitesBonus(e.getGuild(), who.getId());
    			int iLeaves = InvitationListener.getNumberOfInvitesLeaves(e.getGuild(), who.getId());
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
    			e.getChannel().sendMessage(msg).queue();
    		}else if(args[0].equalsIgnoreCase("addBonus") || args[0].equalsIgnoreCase("addB")){
    			if(args.length <= 2){
    				MessageEmbed msg = new EmbedBuilder()
    						.setColor(new Color(255, 0, 0))
    						.addField(new Field("Erreur", "il vous manque des paramètres.\nUsage: !" + args[0] + " [@Joueur] [nombre]", false))
    						.addField(new Field("", "Ce message va s'autodétruire dans 10 secondes KABOUUUM", false))
    						.setFooter("OlympaBot", "https://cdn.discordapp.com/app-icons/614936649096233160/d02b7707f95487df0db947deb62d33a6.png")
    						.setTimestamp(new Date(System.currentTimeMillis()).toInstant())
    						.build();
    				e.getChannel().sendMessage(msg).queue(msgg -> msgg.delete().queueAfter(10, TimeUnit.SECONDS) );
    				return;
    			}else{
    				User who = null;
    				if(args[1].startsWith("@")){
    					args[1] = args[1].replaceFirst("@", "");
    				}
    				try{
    					who = getUserByName(e.getGuild(), args[1], false);
    				}catch(NullPointerException ex){
    					MessageEmbed msg = new EmbedBuilder()
        						.setColor(new Color(255, 0, 0))
        						.addField(new Field("Erreur", "\"" + args[1] + "\" est introuvable.", false))
        						.addField(new Field("", "Ce message va s'autodétruire dans 10 secondes KABOUUUM", false))
        						.setFooter("OlympaBot", "https://cdn.discordapp.com/app-icons/614936649096233160/d02b7707f95487df0db947deb62d33a6.png")
        						.setTimestamp(new Date(System.currentTimeMillis()).toInstant())
        						.build();
        				e.getChannel().sendMessage(msg).queue(msgg -> msgg.delete().queueAfter(10, TimeUnit.SECONDS) );
        				return;
    				}
    				int i = 0;
    				try{
    					i = Integer.parseInt(args[2]);
    				}catch(Exception ex){
    					MessageEmbed msg = new EmbedBuilder()
        						.setColor(new Color(255, 0, 0))
        						.addField(new Field("Erreur", "\"" + args[2] + "\" n'est pas un nombre !", false))
        						.addField(new Field("", "Ce message va s'autodétruire dans 10 secondes KABOUUUM", false))
        						.setFooter("OlympaBot", "https://cdn.discordapp.com/app-icons/614936649096233160/d02b7707f95487df0db947deb62d33a6.png")
        						.setTimestamp(new Date(System.currentTimeMillis()).toInstant())
        						.build();
        				e.getChannel().sendMessage(msg).queue(msgg -> msgg.delete().queueAfter(10, TimeUnit.SECONDS) );
        				return;
    				}
    				int o = InvitationListener.getNumberOfInvitesBonus(e.getGuild(), who.getId());
    				InvitationListener.setNumberOfInvitesBonus(e.getGuild(), who.getId(), o + i);
    				e.getMessage().delete().queue();
    				MessageEmbed msg = new EmbedBuilder()
    						.setColor(new Color(0, 255, 0))
    						.addField(new Field("Invitations bonus:", o + "", true))
    						.addField(new Field("Bonus ajouté", i + "", true))
    						.addField(new Field("Résultat", (i + o) + "", true))
    						.addField(new Field("", "Ce message va s'autodétruire dans 20 secondes KABOUUUM", false))
    						.setFooter("OlympaBot", "https://cdn.discordapp.com/app-icons/614936649096233160/d02b7707f95487df0db947deb62d33a6.png")
    						.setTimestamp(new Date(System.currentTimeMillis()).toInstant())
    						.build();
    				e.getChannel().sendMessage(msg).queue(msgg -> msgg.delete().queueAfter(20, TimeUnit.SECONDS) );
    				return;
    			}
    		}else if(args[0].equalsIgnoreCase("removeBonus") || args[0].equalsIgnoreCase("removeB")){
    			if(args.length <= 2){
    				MessageEmbed msg = new EmbedBuilder()
    						.setColor(new Color(255, 0, 0))
    						.addField(new Field("Erreur", "il vous manque des paramètres.\nUsage: !" + args[0] + " [@Joueur] [nombre]", false))
    						.addField(new Field("", "Ce message va s'autodétruire dans 10 secondes KABOUUUM", false))
    						.setFooter("OlympaBot", "https://cdn.discordapp.com/app-icons/614936649096233160/d02b7707f95487df0db947deb62d33a6.png")
    						.setTimestamp(new Date(System.currentTimeMillis()).toInstant())
    						.build();
    				e.getChannel().sendMessage(msg).queue(msgg -> msgg.delete().queueAfter(10, TimeUnit.SECONDS) );
    				return;
    			}else{
    				User who = null;
    				if(args[1].startsWith("@")){
    					args[1] = args[1].replaceFirst("@", "");
    				}
    				try{
    					who = getUserByName(e.getGuild(), args[1], false);
    				}catch(NullPointerException ex){
    					MessageEmbed msg = new EmbedBuilder()
        						.setColor(new Color(255, 0, 0))
        						.addField(new Field("Erreur", "\"" + args[1] + "\" est introuvable.", false))
        						.addField(new Field("", "Ce message va s'autodétruire dans 10 secondes KABOUUUM", false))
        						.setFooter("OlympaBot", "https://cdn.discordapp.com/app-icons/614936649096233160/d02b7707f95487df0db947deb62d33a6.png")
        						.setTimestamp(new Date(System.currentTimeMillis()).toInstant())
        						.build();
        				e.getChannel().sendMessage(msg).queue(msgg -> msgg.delete().queueAfter(10, TimeUnit.SECONDS) );
        				return;
    				}
    				int i = 0;
    				try{
    					i = Integer.parseInt(args[2]);
    				}catch(Exception ex){
    					MessageEmbed msg = new EmbedBuilder()
        						.setColor(new Color(255, 0, 0))
        						.addField(new Field("Erreur", "\"" + args[2] + "\" n'est pas un nombre !", false))
        						.addField(new Field("", "Ce message va s'autodétruire dans 10 secondes KABOUUUM", false))
        						.setFooter("OlympaBot", "https://cdn.discordapp.com/app-icons/614936649096233160/d02b7707f95487df0db947deb62d33a6.png")
        						.setTimestamp(new Date(System.currentTimeMillis()).toInstant())
        						.build();
        				e.getChannel().sendMessage(msg).queue(msgg -> msgg.delete().queueAfter(10, TimeUnit.SECONDS) );
        				return;
    				}
    				int o = InvitationListener.getNumberOfInvitesBonus(e.getGuild(), who.getId());
    				InvitationListener.setNumberOfInvitesBonus(e.getGuild(), who.getId(), o - i);
    				e.getMessage().delete().queue();
    				MessageEmbed msg = new EmbedBuilder()
    						.setColor(new Color(0, 255, 0))
    						.addField(new Field("Invitations bonus:", o + "", true))
    						.addField(new Field("Bonus enlevé", i + "", true))
    						.addField(new Field("Résultat", (o - i) + "", true))
    						.addField(new Field("", "Ce message va s'autodétruire dans 20 secondes KABOUUUM", false))
    						.setFooter("OlympaBot", "https://cdn.discordapp.com/app-icons/614936649096233160/d02b7707f95487df0db947deb62d33a6.png")
    						.setTimestamp(new Date(System.currentTimeMillis()).toInstant())
    						.build();
    				e.getChannel().sendMessage(msg).queue(msgg -> msgg.delete().queueAfter(20, TimeUnit.SECONDS) );
    				return;
    			}
    		}else if(e.getMember().hasPermission(Permission.ADMINISTRATOR)){
    			e.getMessage().delete().queue();
    			Message msg = new MessageBuilder(e.getAuthor().getAsMention() + "\n"
    					+ "Listes des commandes:\n"
    					+ "!invite | !invites [@Pseudo] - Montre le nombre d'invitations.\n"
    					+ "!aide - Vous le regardez actuellement.\n"
    					+ "\n"
    					+ "---- Commandes d'admins ----\n"
    					+ "!test - Test les messages du dev\n"
    					+ "!setChannel - Permet de désigner le channel ou le bot va écrire le message de connexion (le channel ou vous écrivez la commande)\n"
    					+ "!addBonus | !addB [@Pseudo] [nombre] - Ajoute des invitations bonus a un joueur.\n"
    					+ "!removeBonus | !removeB [@Pseudo] [nombre] - Enlève des invitations bonus a un joueur.\n"
    					+ "\n"
    					+ "Ce message va s'autodétruire dans 30 secondes KABOUUUM").build();
    			e.getChannel().sendMessage(msg).queue(msgg -> msgg.delete().queueAfter(30, TimeUnit.SECONDS) );
    		}else{
    			e.getMessage().delete().queue();
    			Message msg = new MessageBuilder(e.getAuthor().getAsMention() + "\n"
    					+ "Listes des commandes:\n"
    					+ "!invite | !invites [@Pseudo] - Montre le nombre d'invitations.\n"
    					+ "!aide - Vous le regardez actuellement.\n"
    					+ "\n"
    					+ "Ce message va s'autodétruire dans 30 secondes KABOUUUM").build();
    			e.getChannel().sendMessage(msg).queue(msgg -> msgg.delete().queueAfter(30, TimeUnit.SECONDS) );
    		}
		}
	}
	
	public User getUserByName(Guild guild, String name, boolean isCaseSensive) throws NullPointerException{
		for(Member mem : guild.getMembers()){
			String nickname = mem.getUser().getName();
			try{
				nickname = mem.getNickname();
				if(nickname == null){
					nickname = mem.getUser().getName();
				}
			}catch(Exception ex){
				nickname = mem.getUser().getName();
			}
			if(isCaseSensive && (mem.getUser().getName().equals(name) || nickname.equals(name))){
				return mem.getUser();
			}else if(mem.getUser().getName().equalsIgnoreCase(name) || nickname.equalsIgnoreCase(name)){
				return mem.getUser();
			}
		}
		throw new NullPointerException();
	}
}
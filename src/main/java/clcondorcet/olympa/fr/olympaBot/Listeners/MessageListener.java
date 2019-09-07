package clcondorcet.olympa.fr.olympaBot.Listeners;

import clcondorcet.olympa.fr.olympaBot.Functions.Invitations;
import clcondorcet.olympa.fr.olympaBot.Functions.WelcomeMessages;
import clcondorcet.olympa.fr.olympaBot.Utilities.Messages;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

/**
 * Listener for messages receive event by JDA.
 *
 * @author clcondorcet
 * @since 0.0.1
 */
public class MessageListener extends ListenerAdapter {
	
	/**
	 * Generic method that is being call by JDA.
	 * 
	 * @param e The MessageReceivedEvent event.
	 */
	@Override
	public void onMessageReceived(MessageReceivedEvent e) {
		if(e.getChannelType().equals(ChannelType.TEXT) && e.getMessage().getContentDisplay().startsWith("!")) {
    		String message = e.getMessage().getContentDisplay().replaceFirst("!", "");
    		String args[] = message.split(" ");
    		if(e.getMember().hasPermission(Permission.ADMINISTRATOR) && args[0].equalsIgnoreCase("test")) {
    			//              | test
    			Messages.sendTestMessage(e.getTextChannel(), e.getAuthor().getAsMention(), e.getGuild().getMembers().size());
    			
    		}else if(e.getMember().hasPermission(Permission.ADMINISTRATOR) && args[0].equalsIgnoreCase("serveurs")) {
    			//              | serveurs
    			e.getMessage().delete().queue();
    			Messages.sendServeursMessage(e.getTextChannel(), e.getAuthor().getAsMention());
    			
    		}else if(e.getMember().hasPermission(Permission.ADMINISTRATOR) && args[0].equalsIgnoreCase("setChannel")) {
    			//              | setChannel
    			e.getMessage().delete().queue();
    			WelcomeMessages.setChannelCommand(e.getGuild(), e.getTextChannel(), e.getAuthor());
    			
    		}else if(args[0].equalsIgnoreCase("invite") || args[0].equalsIgnoreCase("invites")){
    			//              | invite , invites
    			Invitations.InvitesCommand(e.getTextChannel(), e.getAuthor(), args);
    			
    		}else if(args[0].equalsIgnoreCase("addBonus") || args[0].equalsIgnoreCase("addB")){
    			//              | addBonus , addB
    			boolean deleteMessage = Invitations.addBonusCommand(e.getTextChannel(), e.getAuthor(), args);
    			if(deleteMessage){
    				e.getMessage().delete().queue();
    			}
    			
    		}else if(args[0].equalsIgnoreCase("removeBonus") || args[0].equalsIgnoreCase("removeB")){
    			//              | removeNonus , removeB
    			boolean deleteMessage = Invitations.removeBonusCommand(e.getTextChannel(), e.getAuthor(), args);
    			if(deleteMessage){
    				e.getMessage().delete().queue();
    			}
    			
    		}else if(e.getMember().hasPermission(Permission.ADMINISTRATOR)){
    			e.getMessage().delete().queue();
    			Messages.sendMessage(e.getTextChannel(), 
    					e.getAuthor().getAsMention() + "\n"
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
    	    					+ "Ce message va s'autodétruire dans 30 secondes KABOUUUM"
    	    					, 30);
    		}else{
    			e.getMessage().delete().queue();
    			Messages.sendMessage(e.getTextChannel(), 
    					e.getAuthor().getAsMention() + "\n"
    	    					+ "Listes des commandes:\n"
    	    					+ "!invite | !invites [@Pseudo] - Montre le nombre d'invitations.\n"
    	    					+ "!aide - Vous le regardez actuellement.\n"
    	    					+ "\n"
    	    					+ "Ce message va s'autodétruire dans 30 secondes KABOUUUM"
    	    					, 30);
    		}
		}
	}
	
}
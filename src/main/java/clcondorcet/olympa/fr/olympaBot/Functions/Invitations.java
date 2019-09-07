package clcondorcet.olympa.fr.olympaBot.Functions;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import clcondorcet.olympa.fr.olympaBot.Main;
import clcondorcet.olympa.fr.olympaBot.Listeners.GuildListener;
import clcondorcet.olympa.fr.olympaBot.Utilities.Messages;
import clcondorcet.olympa.fr.olympaBot.Utilities.Utils;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Invite;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.guild.member.GuildMemberLeaveEvent;

/**
 * Invitations manager.
 * 
 * Events: 
 *  - GuildMemberJoinEvent (integrated in WelcomeMessages)
 *  - GuildMemberLeaveEvent
 * Commands:
 *  - 
 * 
 * @author clcondorcet
 * @since 0.0.3
 */
public class Invitations {
	
	/**
	 * Cache of all invites code and their value on all guilds.
	 */
	public static HashMap<String, HashMap<String, Integer>> invites = new HashMap<>();
	
	/**
	 * Event triggered when a member leave a guild.
	 * Removing them to the database.
	 * @see GuildListener
	 * 
	 * @param e The event
	 */
	public static void memberLeaveEvent(GuildMemberLeaveEvent e){
		String idInviter = getWhoInvite(e.getGuild(), e.getUser().getId());
		if(!idInviter.equals("NONE")){
			if(e.getUser().isFake() || e.getUser().isBot()){
				setNumberOfInvitesFakes(e.getGuild(), idInviter, getNumberOfInvitesFakes(e.getGuild(), idInviter) - 1);
			}else{
				setNumberOfInvitesLeaves(e.getGuild(), idInviter, getNumberOfInvitesLeaves(e.getGuild(), idInviter) + 1);
			}
			removeMemberFromDb(e.getGuild(), e.getUser().getId());
		}
	}

	/**
	 * Get number of normal invites.
	 * 
	 * @param guild The guild where you want to get the number to.
	 * @param userId The user you want get the number of invites from.
	 * @return The number of invites the user do in the specified guild.
	 */
	public static int getNumberOfInvites(Guild guild, String userId){
		try {
			ResultSet result = Main.database.get("SELECT * FROM Users WHERE GuildId='" + guild.getId() + "' AND UserId='" + userId + "';");
			if(result.next()){
				return result.getInt("invites");
			}
			Main.database.set("INSERT INTO `Users` (`GuildId`, `UserId`, `UserIdWhoInvite`, `invites`, `invitesFakes`, `invitesLeaves`, `invitesBonus`) VALUES ('" + guild.getId() + "','" + userId + "', 'NONE', 0, 0, 0)");
			return 0;
		} catch (SQLException e) {
			return 0;
		}
	}
	
	/**
	 * Get number of Fakes invites. (including fakes and bots)
	 * 
	 * @param guild The guild where you want to get the number to.
	 * @param userId The user you want get the number of invites from.
	 * @return The number of invites the user do in the specified guild.
	 */
	public static int getNumberOfInvitesFakes(Guild guild, String userId){
		try {
			ResultSet result = Main.database.get("SELECT * FROM Users WHERE GuildId='" + guild.getId() + "' AND UserId='" + userId + "';");
			if(result.next()){
				return result.getInt("invitesFakes");
			}
			Main.database.set("INSERT INTO `Users` (`GuildId`, `UserId`, `UserIdWhoInvite`, `invites`, `invitesFakes`, `invitesLeaves`, `invitesBonus`) VALUES ('" + guild.getId() + "','" + userId + "', 'NONE', 0, 0, 0)");
			return 0;
		} catch (SQLException e) {
			return 0;
		}
	}
	
	/**
	 * Get number member who leave after be inviting by the member.
	 * 
	 * @param guild The guild where you want to get the number to.
	 * @param userId The user you want get the number of invites from.
	 * @return The number of invites the user do in the specified guild.
	 */
	public static int getNumberOfInvitesLeaves(Guild guild, String userId){
		try {
			ResultSet result = Main.database.get("SELECT * FROM Users WHERE GuildId='" + guild.getId() + "' AND UserId='" + userId + "';");
			if(result.next()){
				return result.getInt("invitesLeaves");
			}
			Main.database.set("INSERT INTO `Users` (`GuildId`, `UserId`, `UserIdWhoInvite`, `invites`, `invitesFakes`, `invitesLeaves`, `invitesBonus`) VALUES ('" + guild.getId() + "','" + userId + "', 'NONE', 0, 0, 0)");
			return 0;
		} catch (SQLException e) {
			return 0;
		}
	}
	
	/**
	 * Get number of bonus invites.
	 * 
	 * @param guild The guild where you want to get the number to.
	 * @param userId The user you want get the number of invites from.
	 * @return The number of invites the user do in the specified guild.
	 */
	public static int getNumberOfInvitesBonus(Guild guild, String userId){
		try {
			ResultSet result = Main.database.get("SELECT * FROM Users WHERE GuildId='" + guild.getId() + "' AND UserId='" + userId + "';");
			if(result.next()){
				return result.getInt("invitesBonus");
			}
			Main.database.set("INSERT INTO `Users` (`GuildId`, `UserId`, `UserIdWhoInvite`, `invites`, `invitesFakes`, `invitesLeaves`, `invitesBonus`) VALUES ('" + guild.getId() + "','" + userId + "', 'NONE', 0, 0, 0)");
			return 0;
		} catch (SQLException e) {
			return 0;
		}
	}
	
	/**
	 * Set the number of normal invites for this member.
	 * 
	 * @param guild The guild you want to set the number of invites to.
	 * @param userId The user you want to set the number of invites.
	 * @param i The new value.
	 */
	public static void setNumberOfInvites(Guild guild, String userId, int i) {
		try {
			ResultSet result = Main.database.get("SELECT * FROM Users WHERE GuildId='" + guild.getId() + "' AND UserId='" + userId + "';");
			if(result.next()){
				Main.database.set("UPDATE `Users` SET invites='" + i + "' WHERE GuildId='" + guild.getId() + "' AND UserId='" + userId + "';");
				return;
			}
			Main.database.set("INSERT INTO `Users` (`GuildId`, `UserId`, `UserIdWhoInvite`, `invites`, `invitesFakes`, `invitesLeaves` , `invitesBonus`) VALUES ('" + guild.getId() + "','" + userId + "', 'NONE', " + i + ", 0, 0, 0)");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Set the number of fakes invites for this member.
	 * 
	 * @param guild The guild you want to set the number of invites to.
	 * @param userId The user you want to set the number of invites.
	 * @param i The new value.
	 */
	public static void setNumberOfInvitesFakes(Guild guild, String userId, int i) {
		try {
			ResultSet result = Main.database.get("SELECT * FROM Users WHERE GuildId='" + guild.getId() + "' AND UserId='" + userId + "';");
			if(result.next()){
				Main.database.set("UPDATE `Users` SET invitesFakes='" + i + "' WHERE GuildId='" + guild.getId() + "' AND UserId='" + userId + "';");
				return;
			}
			Main.database.set("INSERT INTO `Users` (`GuildId`, `UserId`, `UserIdWhoInvite`, `invites` , `invitesFakes`, `invitesLeaves` , `invitesBonus`) VALUES ('" + guild.getId() + "','" + userId + "', 'NONE', 0, " + i + ", 0, 0)");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Set the number of Bonus invites for this member.
	 * 
	 * @param guild The guild you want to set the number of invites to.
	 * @param userId The user you want to set the number of invites.
	 * @param i The new value.
	 */
	public static void setNumberOfInvitesBonus(Guild guild, String userId, int i) {
		try {
			ResultSet result = Main.database.get("SELECT * FROM Users WHERE GuildId='" + guild.getId() + "' AND UserId='" + userId + "';");
			if(result.next()){
				Main.database.set("UPDATE `Users` SET invitesBonus='" + i + "' WHERE GuildId='" + guild.getId() + "' AND UserId='" + userId + "';");
				return;
			}
			Main.database.set("INSERT INTO `Users` (`GuildId`, `UserId`, `UserIdWhoInvite`, `invites`, `invitesFakes`, `invitesLeaves` , `invitesBonus`) VALUES ('" + guild.getId() + "','" + userId + "', 'NONE', 0, 0, 0, " + i + ")");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Set the number of Leaves invites for this member.
	 * 
	 * @param guild The guild you want to set the number of invites to.
	 * @param userId The user you want to set the number of invites.
	 * @param i The new value.
	 */
	public static void setNumberOfInvitesLeaves(Guild guild, String userId, int i) {
		try {
			ResultSet result = Main.database.get("SELECT * FROM Users WHERE GuildId='" + guild.getId() + "' AND UserId='" + userId + "';");
			if(result.next()){
				Main.database.set("UPDATE `Users` SET invitesLeaves='" + i + "' WHERE GuildId='" + guild.getId() + "' AND UserId='" + userId + "';");
				return;
			}
			Main.database.set("INSERT INTO `Users` (`GuildId`, `UserId`, `UserIdWhoInvite`, `invites`, `invitesFakes` , `invitesLeaves`, `invitesBonus`) VALUES ('" + guild.getId() + "','" + userId + "', 'NONE', 0, 0," + i + ", 0)");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Get the userId of the inviter of this member.
	 * 
	 * @param guild The guild where the invite is from.
	 * @param userId The user you want to know who is his inviter.
	 * @return The inviter userId.
	 */
	public static String getWhoInvite(Guild guild, String userId){
		try {
			ResultSet result = Main.database.get("SELECT * FROM Users WHERE GuildId='" + guild.getId() + "' AND UserId='" + userId + "';");
			if(result.next()){
				return result.getString("UserIdWhoInvite");
			}
			Main.database.set("UPDATE INTO `Users` (`GuildId`, `UserId`, `UserIdWhoInvite`, `invites`) VALUES ('" + guild.getId() + "','" + userId + "', 'NONE', 0)");
			return "NONE";
		} catch (SQLException e) {
			return "NONE";
		}
	}
	
	/**
	 * Add a member to the database.
	 * 
	 * @param guild The guild from.
	 * @param userId The userId.
	 * @param userWhoInviteId The userId of the inviter.
	 */
	public static void addMemberToDb(Guild guild, String userId, String userWhoInviteId){
		try {
			ResultSet result = Main.database.get("SELECT * FROM Users WHERE GuildId='" + guild.getId() + "' AND UserId='" + userId + "';");
			if(result.next()){
				Main.database.set("DELETE FROM `Users` WHERE GuildId='" + guild.getId() + "' AND UserId='" + userId + "';");
			}
			Main.database.set("INSERT INTO `Users` (`GuildId`, `UserId`, `UserIdWhoInvite`, `invites`, `invitesFakes`, `invitesLeaves`, `invitesBonus`) VALUES ('" + guild.getId() + "','" + userId + "', '" + userWhoInviteId + "', 0, 0, 0, 0)");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Remove a member from the database.
	 * 
	 * @param guild The guild from.
	 * @param userId The userid of the user you want to delete.
	 */
	public static void removeMemberFromDb(Guild guild, String userId){
		try {
			Main.database.set("DELETE FROM `Users` WHERE GuildId='" + guild.getId() + "' AND UserId='" + userId + "';");
		} catch (SQLException e) {
		}
	}
	
	/**
	 * Get the invite that change after a JoinEvent.
	 * 
	 * @param guild The guild you want to look at.
	 * @return The invite that change.
	 */
	public static Invite getInviteThatChange(Guild guild) {
		HashMap<String, Integer> actualMap = getInvites(guild);
		for(String code : invites.get(guild.getId()).keySet()) {
			if(invites.get(guild.getId()).get(code) != actualMap.get(code)) {
				for(Invite inv : guild.getInvites().complete()) {
					if(inv.getCode().equals(code)) {
						return inv;
					}
				}
				return null;
			}
		}
		for(Invite inv : guild.getInvites().complete()){
			if(!invites.get(guild.getId()).containsKey(inv.getCode()) && inv.getUses() > 0){
				return inv;
			}
		}
		return null;
	}
	
	/**
	 * Get the invites HashMap of a guild from the JDA.
	 * 
	 * @param guild The guild you want to look at.
	 * @return Hashmap of all the invites and their number of uses.
	 */
	public static HashMap<String, Integer> getInvites(Guild guild) {
		HashMap<String, Integer> map = new HashMap<>();
		for(Invite inv : guild.getInvites().complete()) {
			map.put(inv.getCode(), inv.getUses());
		}
		return map;
	}
	
	/**
	 * Stock all the invites of a all joined guilds.
	 * That wont work for a guild if the bot doesn't have the permission "MANAGE_SERVER" (this will be log an error)
	 */
	public static void stockAllGuildsInvites() {
		invites.clear();
		for(Guild guild : Main.jda.getGuilds()) {
			HashMap<String, Integer> map = new HashMap<>();
			if(!guild.getMemberById(Main.jda.getSelfUser().getId()).hasPermission(Permission.MANAGE_SERVER)){
				System.out.print("[WARN] - Le bot n'a pas la permission MANAGE_SERVER sur le serveur " + guild.getName() + " (" + guild.getId() + ")\n");
				continue;
			}
			for(Invite inv : guild.getInvites().complete()) {
				map.put(inv.getCode(), inv.getUses());
			}
			invites.put(guild.getId(), map);
		}
	}
	
	/**
	 * Add guild invites to the cache
	 * 
	 * @param guild The guild you want to get the invites from.
	 */
	public static void addGuildInvites(Guild guild) {
		if(invites.containsKey(guild.getId())) {
			invites.remove(guild.getId());
		}
		HashMap<String, Integer> map = new HashMap<>();
		for(Invite inv : guild.getInvites().complete()) {
			map.put(inv.getCode(), inv.getUses());
		}
		invites.put(guild.getId(), map);
	}
	
	/**
	 * Remove guild invites from the cache.
	 * 
	 * @param guild The guild you want to remove the invites.
	 */
	public static void removeGuildInvites(Guild guild) {
		if(invites.containsKey(guild.getId())) {
			invites.remove(guild.getId());
		}
	}
	
	/**
	 * Send the message that show invites numbers of a member.
	 * 
	 * @param channel The channel where the command is send.
	 * @param author The author of the command.
	 * @param cmd The message of the command.
	 */
	public static void InvitesCommand(TextChannel channel, User author, String cmd[]){
		User who = author;
		if(cmd.length > 1){
			if(cmd[1].startsWith("@")){
				cmd[1] = cmd[1].replaceFirst("@", "");
			}
			try{
				who = Utils.getUserByName(channel.getGuild(), cmd[1], false);
			}catch(NullPointerException ex){
				Messages.sendErrorMessage(channel, "\"" + cmd[1] + "\" est introuvable.", 10);
				return;
			}
		}
		int invites = Invitations.getNumberOfInvites(channel.getGuild(), who.getId());
		int iFakes = Invitations.getNumberOfInvitesFakes(channel.getGuild(), who.getId());
		int iBonus = Invitations.getNumberOfInvitesBonus(channel.getGuild(), who.getId());
		int iLeaves = Invitations.getNumberOfInvitesLeaves(channel.getGuild(), who.getId());
		Messages.sendInvitesMessage(channel, who, invites, iBonus, iLeaves, iFakes);
	}
	
	/**
	 * Change the bonus invites of a member in the specified guild.
	 * And send a custom message.
	 * 
	 * @param channel The channel where the command is send.
	 * @param author The author of the command.
	 * @param cmd The message of the command.
	 * @return True when the change occure, False when there is an error
	 */
	public static boolean addBonusCommand(TextChannel channel, User author, String cmd[]){
		if(cmd.length <= 2){
			Messages.sendErrorMessage(channel, "il vous manque des paramètres.\nUsage: !" + cmd[0] + " [@Joueur] [nombre]", 10);
			return false;
		}else{
			User who = null;
			if(cmd[1].startsWith("@")){
				cmd[1] = cmd[1].replaceFirst("@", "");
			}
			try{
				who = Utils.getUserByName(channel.getGuild(), cmd[1], false);
			}catch(NullPointerException ex){
				Messages.sendErrorMessage(channel, "\"" + cmd[1] + "\" est introuvable.", 10);
				return false;
			}
			int i = 0;
			try{
				i = Integer.parseInt(cmd[2]);
			}catch(Exception ex){
				Messages.sendErrorMessage(channel, "\"" + cmd[2] + "\" n'est pas un nombre !", 10);
				return false;
			}
			int o = Invitations.getNumberOfInvitesBonus(channel.getGuild(), who.getId());
			Invitations.setNumberOfInvitesBonus(channel.getGuild(), who.getId(), o + i);
			Messages.sendAddBonusMessage(channel, o, i);
			return true;
		}
	}
	
	/**
	 * Change the bonus invites of a member in the specified guild.
	 * And send a custom message.
	 * 
	 * @param channel The channel where the command is send.
	 * @param author The author of the command.
	 * @param cmd The message of the command.
	 * @return True when the change occure, False when there is an error
	 */
	public static boolean removeBonusCommand(TextChannel channel, User author, String cmd[]){
		if(cmd.length <= 2){
			Messages.sendErrorMessage(channel, "il vous manque des paramètres.\nUsage: !" + cmd[0] + " [@Joueur] [nombre]", 10);
			return false;
		}else{
			User who = null;
			if(cmd[1].startsWith("@")){
				cmd[1] = cmd[1].replaceFirst("@", "");
			}
			try{
				who = Utils.getUserByName(channel.getGuild(), cmd[1], false);
			}catch(NullPointerException ex){
				Messages.sendErrorMessage(channel, "\"" + cmd[1] + "\" est introuvable.", 10);
				return false;
			}
			int i = 0;
			try{
				i = Integer.parseInt(cmd[2]);
			}catch(Exception ex){
				Messages.sendErrorMessage(channel, "\"" + cmd[2] + "\" n'est pas un nombre !", 10);
				return false;
			}
			int o = Invitations.getNumberOfInvitesBonus(channel.getGuild(), who.getId());
			Invitations.setNumberOfInvitesBonus(channel.getGuild(), who.getId(), o - i);
			Messages.sendRemoveBonusMessage(channel, o, i);
			return true;
		}
	}
}

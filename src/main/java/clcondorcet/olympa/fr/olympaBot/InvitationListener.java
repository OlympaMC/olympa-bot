package clcondorcet.olympa.fr.olympaBot;

import java.awt.Color;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Invite;
import net.dv8tion.jda.core.entities.MessageEmbed.Field;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberLeaveEvent;
import net.dv8tion.jda.core.hooks.EventListener;

/**
 * @author clcondorcet
 *
 * 
 */
public class InvitationListener implements EventListener {
	
	public static HashMap<String, HashMap<String, Integer>> invites = new HashMap<>();
	
	@Override
    public void onEvent(Event event) {
		if(event instanceof GuildMemberJoinEvent){
			if(!Main.canWork) {
				return;
			}
			GuildMemberJoinEvent e = (GuildMemberJoinEvent) event;
			
			// Take invites from the guild to compare with past data
			Invite inv = getInviteThatChange(e.getGuild());
			// Update the data for next event
			invites.get(e.getGuild().getId()).put(inv.getCode(), inv.getUses());
			// Get the type of account
			if(inv.getInviter().isFake() || inv.getInviter().isBot()){
				// Get the number of people invited by this user
				int i = getNumberOfInvitesFakes(e.getGuild(), inv.getInviter().getId()) + 1;
				// Update the number
				setNumberOfInvitesFakes(e.getGuild(), inv.getInviter().getId(), i);
				addMemberToDb(e.getGuild(), e.getUser().getId(), inv.getInviter().getId());
			}else{
				// Get the number of people invited by this user
				int i = getNumberOfInvites(e.getGuild(), inv.getInviter().getId()) + 1;
				// Update the number
				setNumberOfInvites(e.getGuild(), inv.getInviter().getId(), i);
				addMemberToDb(e.getGuild(), e.getUser().getId(), inv.getInviter().getId());
			}
			int i = getNumberOfInvites(e.getGuild(), inv.getInviter().getId()) + getNumberOfInvitesBonus(e.getGuild(), inv.getInviter().getId()) - getNumberOfInvitesLeaves(e.getGuild(), inv.getInviter().getId());
			
			// Message
			Main.jda.getTextChannelById(Main.idsChannels.get(e.getGuild().getId())).sendMessage(new EmbedBuilder()
					.setAuthor("OlympaBot", "https://olympa.net---BROKEN", "https://cdn.discordapp.com/app-icons/614936649096233160/d02b7707f95487df0db947deb62d33a6.png")
					.setColor(new Color(255, 153, 51))
					.setDescription("Bienvenue à toi " + e.getMember().getAsMention() + " sur Olympa !")
					.addField(new Field("", "• Tu as été invité par: " + inv.getInviter().getAsMention() + ", qui a lui: " + i + " invitations !", false))
					.addField(new Field("", "» Grâce à toi, notre Discord à atteint les " + e.getGuild().getMembers().size() + " joueurs !", false))
					.setFooter("OlympaBot", null)
					.setTimestamp(new Date(System.currentTimeMillis()).toInstant())
					.build()).queue();
		}else if(event instanceof GuildMemberLeaveEvent){
			if(!Main.canWork) {
				return;
			}
			GuildMemberLeaveEvent e = (GuildMemberLeaveEvent) event;
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
	}
	
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
	
	public static void removeMemberFromDb(Guild guild, String userId){
		try {
			Main.database.set("DELETE FROM `Users` WHERE GuildId='" + guild.getId() + "' AND UserId='" + userId + "';");
		} catch (SQLException e) {
		}
	}
	
	public Invite getInviteThatChange(Guild guild) {
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
	
	public HashMap<String, Integer> getInvites(Guild guild) {
		HashMap<String, Integer> map = new HashMap<>();
		for(Invite inv : guild.getInvites().complete()) {
			map.put(inv.getCode(), inv.getUses());
		}
		return map;
	}
	
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
	
	public static void removeGuildInvites(Guild guild) {
		if(invites.containsKey(guild.getId())) {
			invites.remove(guild.getId());
		}
	}

}

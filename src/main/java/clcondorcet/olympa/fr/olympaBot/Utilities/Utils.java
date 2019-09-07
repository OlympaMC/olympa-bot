package clcondorcet.olympa.fr.olympaBot.Utilities;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.User;

/**
 * Some useful methods that can be use in all the project.
 * 
 * @author clcondorcet
 * @since 0.0.3
 */
public class Utils {
	
	/**
	 * Get the user in a guild by it's name or nickname. 
	 * Throw a NullPointerException when the player does not exist.
	 * 
	 * @param guild The guild where to find him.
	 * @param name The name of the user to be found.
	 * @param isCaseSensive If you want the name to be CaseSensive
	 * @return The user.
	 * @throws NullPointerException Player does not exist.
	 */
	public static User getUserByName(Guild guild, String name, boolean isCaseSensive) throws NullPointerException{
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
		throw new NullPointerException("User \"" + name + "\" does not exist in " + guild.getName() + "(" + guild.getId() + ")");
	}
}

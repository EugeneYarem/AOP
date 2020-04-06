package laba1;

import java.util.ArrayList;

/**
 *  The chat service interface.
 */
public interface IBotService
{
	/**
	 *  Receives a chat message.
	 *  @param sender The sender's name.
	 *  @param text The message text.
	 */
	public ArrayList<Message> validateMessage(final String from, final String to, final String text) throws Exception;
	
	public boolean addFriend(final String nickname, final String password);
}

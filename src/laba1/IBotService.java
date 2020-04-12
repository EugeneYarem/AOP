package laba1;

import java.util.ArrayList;

/**
 *  The bot service interface.
 */
public interface IBotService
{
	public ArrayList<Message> validateMessage(final String from, final String to, final String text) throws Exception;
	
	public boolean addFriend(final String nickname, final String password);
	
	public String answerOnFact(final String from, final String to, final String text);
}

package laba1;

/**
 *  The chat service interface.
 */
public interface IChatService
{
	/**
	 *  Receives a chat message.
	 *  @param sender The sender's name.
	 *  @param text The message text.
	 */
	public void message(final String from, final String to, final String text);
	
	public void addChatter(String nickname);
}

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
	public void message(String sender, String text);
	
	public void addChatter(String nickname);
	
	public void sendPrivateMessage(String to, String text);
}

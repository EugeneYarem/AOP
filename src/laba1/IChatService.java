package laba1;

/**
 *  The chat service interface.
 */
public interface IChatService
{
	public void message(final String from, final String to, final String text, boolean isMessageFromBot);
	
	public void addChatter(String nickname);
}

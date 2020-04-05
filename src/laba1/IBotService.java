package laba1;

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
	public String censorMessage(final String from, final String to, final String text) throws ForbiddenException;
}

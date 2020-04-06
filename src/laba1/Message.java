package laba1;

public class Message {
	private String from;
	private String to;
	private String text;
	private boolean isMessageFromBot;
	
	public Message(String from, String to, String text, boolean isMessageFromBot) {
		this.from = from;
		this.to = to;
		this.text = text;
		this.isMessageFromBot = isMessageFromBot;
	}

	public String getFrom() {
		return from;
	}
	
	public String getTo() {
		return to;
	}
	
	public String getText() {
		return text;
	}
	
	public boolean isMessageFromBot() {
		return isMessageFromBot;
	}
}

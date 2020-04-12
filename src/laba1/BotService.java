package laba1;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import jadex.bridge.IInternalAccess;
import jadex.bridge.service.annotation.Service;
import jadex.bridge.service.annotation.ServiceComponent;

import org.python.util.PythonInterpreter;
import org.python.core.*;

/**
 *  The bot service.
 */
@Service
public class BotService implements IBotService
{
	/** The agent. */
	@ServiceComponent
	protected IInternalAccess agent;	
	protected String nickname;	
	protected String appeal;	
	private final Integer maxNegativeRate = -2;
	private HashMap<String, Integer> userRates = new HashMap<String, Integer>();
	private HashSet<String> friends = new HashSet<String>();
	private HashSet<String> beliefs = new HashSet<String>();
	private HashSet<String> violators = new HashSet<String>();
	
	public class Pair<U, V> {
		private U first;
		private V second;
		
		public Pair(U first, V second) {
			this.first = first;
			this.second = second;
		}

		public U getFirst() {
			return first;
		}
		
		public V getSecond() {
			return second;
		}
	}
	
	@Override
	public ArrayList<Message> validateMessage(final String from, final String to, final String text) throws Exception, PyException {
		System.out.println("@@@@bot hashCode: "+this.hashCode());
		ArrayList<Message> messages = new ArrayList<Message>();
		
		for(int i = 0; i < text.length(); i++) {
			System.out.println("char: "+text.charAt(i)+" int: " + (int)text.charAt(i));
			if(text.charAt(i) >= 1040 && text.charAt(i) <= 1103) {
				throw new Exception("NOT LATIN!!!"); 
			}
		}
		
		if(violators.contains(from) && violators.contains(to)) {
			messages.add(new Message(from, to, "<span style=\"color:red;\">You are not allowed to communicate because you are an undisciplined users!</span>", true));
			return messages;
		}
		
		PythonInterpreter pi = new PythonInterpreter();
    	pi.exec("from profanityfilter import ProfanityFilter");
    	pi.exec("pf = ProfanityFilter()");
        pi.set("string", new PyString(text));
        pi.exec("result = pf.censor(string)");
        pi.exec("badWordsNum = pf.is_profane(string)");
        pi.exec("print(result)");
        pi.exec("print(badWordsNum)");
        String censoredMessage = ((PyString)pi.get("result")).asString();
        Integer badWordsNumber = ((PyInteger)pi.get("badWordsNum")).asInt();
                		
		System.out.println("badWordsNumber: " + badWordsNumber);
		System.out.println("censoredMessage: " + censoredMessage);
		
		Integer userRate = userRates.get(from);
		System.out.println(from + " rate: " + userRate);
		if(userRate != null) {
			userRate -= badWordsNumber;
			this.userRates.put(from, userRate);
		}
		else {
			this.userRates.put(from, -badWordsNumber);
			userRate = -badWordsNumber;
		}
		
		if(userRate <= this.maxNegativeRate && badWordsNumber != 0) {
			violators.add(from);
			throw new Exception("FORBIDDEN!!!"); 
		}
		
		nickname = "@Bot";
		appeal = nickname + ",";
		
		messages.add(new Message(from, to, censoredMessage, false));
		
		Integer spacePos = text.indexOf(" ");
		if(spacePos > 0 && text.substring(0, spacePos).equals(appeal)) {
			if(badWordsNumber == 0) {
				messages.add(new Message(from, to, this.answerOnFact(from, to, censoredMessage), true));
			} else {
				messages.add(new Message(from, to, "    " + from + ", not even going to answer your insults!", true));
			}
		}
				
		return messages;
	}
	
	@Override
	public boolean addFriend(final String nickname, final String password) {
		if(agent.getArgument("password") != null && agent.getArgument("password").equals(password)) {
			friends.add(nickname);
			return true;
		} else return false;
	}
	
	@Override
	public String answerOnFact(final String from, final String to, final String text) {
		if(friends.contains(from)) {
			String fact = text.substring(text.indexOf(" ") + 1);
			System.out.println("fact: "+fact);
			
			if(fact.equals("-?")) {
				return "-f - facts list";
			}
			else if(fact.equals("-f")) {
				String answer = "";
				
				for(String i : beliefs) {
					answer += "<p>" + i + "</p>";
				}
				
				return answer;
			} else {
				beliefs.add(fact);
				return "    Ok, " + from + ", I accepted!";
			}
		} else return "    " + from + ", You are not on my friends list!";
	}
}

package laba1;

import java.awt.BorderLayout;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import javax.swing.SwingUtilities;

import jadex.bridge.IExternalAccess;
import jadex.bridge.IInternalAccess;
import jadex.bridge.service.annotation.Service;
import jadex.bridge.service.annotation.ServiceComponent;
import jadex.bridge.service.annotation.ServiceShutdown;
import jadex.bridge.service.annotation.ServiceStart;
import jadex.bridge.service.component.IRequiredServicesFeature;
import jadex.bridge.service.types.clock.IClockService;
import jadex.commons.future.Future;
import jadex.commons.future.IFuture;
import jadex.commons.gui.future.SwingExceptionDelegationResultListener;
import laba1.SpamInterceptor.Pair;

import org.python.util.PythonInterpreter;
import org.python.core.*;

/**
 *  The chat service.
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
		
		/*Pair<String,Integer> censoringResult = this.censorText(text);
		String censoredMessage = censoringResult.getFirst();
		Integer badWordsNumber = censoringResult.getSecond();	*/	
		
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
                		
		System.out.println("badWordsNumber: "+badWordsNumber);
		System.out.println("censoredMessage: "+censoredMessage);
		
		Integer userRate = userRates.get(from);
		System.out.println(from + " rate: "+userRate);
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
		
		nickname = "@Bot"; // + agent.getComponentIdentifier().getLocalName();
		System.out.println("@@@@nickname: "+nickname);
		appeal = nickname + ",";
		
		messages.add(new Message(from, to, censoredMessage, false));
		
		// this.agent.getComponentIdentifier().getName();
		Integer spacePos = text.indexOf(" ");
		if(spacePos > 0 && text.substring(0, spacePos).equals(appeal)) {
			if(badWordsNumber == 0) {
				messages.add(new Message(from, to, this.answerOnFact(from, to, censoredMessage), true));
			} else {
				//"	" + from + ", Вы не входите в список моих друзей!"
				messages.add(new Message(from, to, "    " + from + ", not even going to answer your insults!", true));
			}
		}
				
		return messages;
		
		// context.getArgumentArray()[2] = censoredMessage;
		
		/*List<Object> arguments = new ArrayList<Object>();		 
		arguments.add(sender);
		arguments.add(target);
		arguments.add(censoredMessage);
		
		context.setArguments(arguments);
		return context.invoke();*/
	}
	
	private Pair<String,Integer> censorText(String message) {
		Character[] symbols = {'.', ',', '!', '?', ';', ':'};
		List<Character> symbolsList = Arrays.asList(symbols);
		
		String[] censored = {"дура", "дурак", "балбес"};
        String[] splitstring = message.split(" ");
        
        Integer badWordsCounter = 0;
        
        for(int k = 0; k < censored.length; k++){
            for(int i = 0; i < splitstring.length; i++){
                if(splitstring[i].toLowerCase().contains(censored[k])){
                	if(symbolsList.contains(splitstring[i].charAt(splitstring[i].length() - 1))) {
                		splitstring[i] = "[вырезано цензурой]" + splitstring[i].charAt(splitstring[i].length() - 1);
                	} else splitstring[i] = "[вырезано цензурой]";
                    badWordsCounter++;
                }
            }
        }
 
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < splitstring.length; i++) {
        	if(i > 0) {
        		stringBuilder.append(" ");
        	}
            stringBuilder.append(splitstring[i]);
        }
        
        return new Pair<String, Integer>(stringBuilder.toString(), badWordsCounter);
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
				return "-f - list facts";
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

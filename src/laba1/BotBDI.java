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

import jadex.bdiv3.IBDIAgent;
import jadex.bdiv3.annotation.Belief;
import jadex.bdiv3.annotation.Plan;
import jadex.bdiv3.annotation.PlanBody;
import jadex.bdiv3.annotation.Trigger;
import jadex.bdiv3.annotation.ServiceTrigger;
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
import jadex.micro.annotation.Agent;
import jadex.micro.annotation.Description;
import jadex.micro.annotation.Implementation;
import jadex.micro.annotation.ProvidedService;
import jadex.micro.annotation.ProvidedServices;
import laba1.SpamInterceptor.Pair;

/**
 *  The chat agent.
 */
@Agent
@Description("This agent presents the chat bot.")
@Service
//@ProvidedServices(@ProvidedService(type=IBotService.class))
@ProvidedServices(@ProvidedService(name="transser", type=IBotService.class, 
implementation=@Implementation(IBDIAgent.class)))
public class BotBDI/* implements IBotService*/
{
	/** The agent. */
	/*@ServiceComponent
	protected IInternalAccess agent;*/
	
	@Belief
	protected String nickname;
	
	@Belief
	protected String appeal;
	
	@Belief
	protected final Integer maxNegativeRate = -2;
	
	@Belief
	protected HashMap<String, Integer> userRates = new HashMap<String, Integer>();
	
	@Belief
	protected HashSet<String> friends = new HashSet<String>();
	
	@Belief
	protected HashSet<String> facts = new HashSet<String>();
		
	@Plan(trigger=@Trigger(service=@ServiceTrigger(type=IBotService.class, method="validateMessage")))
	public class BotValidationPlan
	{
		protected class Pair<U, V> {
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
		
		@PlanBody
		public ArrayList<Message> body(Object[] params) throws Exception
		{
			final String from = (String)params[0];
			final String to = (String)params[1];
			final String text = (String)params[2];
			
			System.out.println("@@@@bot hashCode: "+this.hashCode());
			
			Pair<String,Integer> censoringResult = this.censorText(text);
			String censoredMessage = censoringResult.getFirst();
			Integer badWordsNumber = censoringResult.getSecond();		
			System.out.println("badWordsNumber: "+badWordsNumber);
			System.out.println("censoredMessage: "+censoredMessage);
			
			Integer userRate = userRates.get(from);
			System.out.println(from + " rate: "+userRate);
			if(userRate != null) {
				userRate -= badWordsNumber;
				userRates.put(from, userRate);
			}
			else {
				userRates.put(from, -badWordsNumber);
				userRate = -badWordsNumber;
			}
			
			if(userRate <= maxNegativeRate && badWordsNumber != 0) {
				throw new Exception("FORBIDDEN!!!"); 
			}
			
			nickname = "@Bot"; // + agent.getComponentIdentifier().getLocalName();
			System.out.println("@@@@nickname: "+nickname);
			appeal = nickname + ",";
			
			ArrayList<Message> messages = new ArrayList<Message>();
			messages.add(new Message(from, to, censoredMessage, false));
			
			// this.agent.getComponentIdentifier().getName();
			Integer spacePos = text.indexOf(" ");
			if(spacePos > 0 && text.substring(0, spacePos).equals(appeal)) {
				if(badWordsNumber == 0) {
					messages.add(new Message(from, to, this.answerOnFact(from, to, censoredMessage), true));
				} else {
					//"	" + from + ", Вы не входите в список моих друзей!"
					messages.add(new Message(from, to, "    " + from + ", даже не собираюсь отвечать на Ваши оскорбления!", true));
				}
			}
					
			return messages;
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
		
		public String answerOnFact(final String from, final String to, final String text) {
			if(friends.contains(from)) {
				String fact = text.substring(text.indexOf(" ") + 1);
				System.out.println("fact: "+fact);
				
				if(fact.equals("-?")) {
					return "-f - вывести список фактов";
				}
				else if(fact.equals("-f")) {
					String answer = "";
					
					for(String i : facts) {
						answer += "<p>" + i + "</p>";
					}
					
					return answer;
				} else {
					facts.add(fact);
					return "    Окей, " + from + ", я принял!";
				}
			} else return "    " + from + ", Вы не входите в список моих друзей!";
		}
	}
	
	/*@Override
	public ArrayList<Message> validateMessage(final String from, final String to, final String text) throws Exception {
		System.out.println("@@@@bot hashCode: "+this.hashCode());
		
		Pair<String,Integer> censoringResult = this.censorText(text);
		String censoredMessage = censoringResult.getFirst();
		Integer badWordsNumber = censoringResult.getSecond();		
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
			throw new Exception("FORBIDDEN!!!"); 
		}
		
		nickname = "@Bot"; // + agent.getComponentIdentifier().getLocalName();
		System.out.println("@@@@nickname: "+nickname);
		appeal = nickname + ",";
		
		ArrayList<Message> messages = new ArrayList<Message>();
		messages.add(new Message(from, to, censoredMessage, false));
		
		// this.agent.getComponentIdentifier().getName();
		Integer spacePos = text.indexOf(" ");
		if(spacePos > 0 && text.substring(0, spacePos).equals(appeal)) {
			if(badWordsNumber == 0) {
				messages.add(new Message(from, to, this.answerOnFact(from, to, censoredMessage), true));
			} else {
				//"	" + from + ", Вы не входите в список моих друзей!"
				messages.add(new Message(from, to, "    " + from + ", даже не собираюсь отвечать на Ваши оскорбления!", true));
			}
		}
				
		return messages;
	}*/
	
	@Plan(trigger=@Trigger(service=@ServiceTrigger(type=IBotService.class, method="addFriend")))
	public class BotFriendshipPlan
	{
		@PlanBody
		public boolean body(Object[] params)
		{
			final String nickname = (String)params[0];
			final String password = (String)params[1];
			
			/*if(agent.getArgument("password") != null && agent.getArgument("password").equals(password)) {
				friends.add(nickname);
				return true;
			} else return false;*/
			
			return true;
		}
	}
	
	/*@Override
	public boolean addFriend(final String nickname, final String password) {
		if(agent.getArgument("password") != null && agent.getArgument("password").equals(password)) {
			friends.add(nickname);
			return true;
		} else return false;
	}*/
	
	
}

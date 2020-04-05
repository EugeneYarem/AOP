package laba1;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jadex.bridge.service.component.IServiceInvocationInterceptor;
import jadex.bridge.service.component.ServiceInvocationContext;
import jadex.commons.future.Future;
import jadex.commons.future.IFuture;

/**
 *  Simple interceptor that refuses messages of spammers.
 */
public class SpamInterceptor implements IServiceInvocationInterceptor
{
	/**
	 *  Test if the interceptor is applicable.
	 *  @return True, if applicable.
	 */
	public boolean isApplicable(ServiceInvocationContext context)
	{
		try
		{
			return context.getMethod().getName().equals("message");
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
	/**
	 *  Execute the interceptor.
	 *  @param context The invocation context.
	 */
	public IFuture<Void> execute(ServiceInvocationContext context)
	{
		System.out.println("bot hashCode: "+this.hashCode());
		
		String sender = (String)context.getArgumentArray()[0];
		String target = (String)context.getArgumentArray()[1];
		String text = (String)context.getArgumentArray()[2];
		
		Pair<String,Integer> censoringResult = this.censorMessage(text);
		Integer badWordsNumber = censoringResult.getSecond();
		String censoredMessage = censoringResult.getFirst();
		System.out.println("badWordsNumber: "+badWordsNumber);
		System.out.println("censoredMessage: "+censoredMessage);
		// context.getArgumentArray()[2] = censoredMessage;
		
		List<Object> arguments = new ArrayList<Object>();		 
		arguments.add(sender);
		arguments.add(target);
		arguments.add(censoredMessage);
		
		context.setArguments(arguments);
		return context.invoke();
		
		/*if(sender.indexOf("Bot")!=-1)
		{
			System.out.println("Blocked spam message: "+sender+" "+text);
			
			return new Future<Void>((new RuntimeException("No spammers allowed.")));
		}
		else
		{
			return context.invoke();
		}*/
	}
	
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
		
	private Pair<String,Integer> censorMessage(String message) {
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
}

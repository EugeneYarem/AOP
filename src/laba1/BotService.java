package laba1;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
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

/**
 *  The chat service.
 */
@Service
public class BotService implements IBotService
{
	private final Integer maxNegativeRate = 2;
	private HashMap<String, Integer> userRates = new HashMap<String, Integer>();
	
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
	public String censorMessage(final String from, final String to, final String text) throws ForbiddenException {
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
		
		if(userRate <= this.maxNegativeRate) {
			throw new ForbiddenException("FORBIDDEN!!!"); 
		}
		
		return censoredMessage;
		
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
		
		String[] censored = {"����", "�����", "������"};
        String[] splitstring = message.split(" ");
        
        Integer badWordsCounter = 0;
        
        for(int k = 0; k < censored.length; k++){
            for(int i = 0; i < splitstring.length; i++){
                if(splitstring[i].toLowerCase().contains(censored[k])){
                	if(symbolsList.contains(splitstring[i].charAt(splitstring[i].length() - 1))) {
                		splitstring[i] = "[�������� ��������]" + splitstring[i].charAt(splitstring[i].length() - 1);
                	} else splitstring[i] = "[�������� ��������]";
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
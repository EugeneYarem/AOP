package laba1;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import jadex.bridge.IComponentIdentifier;
import jadex.bridge.service.annotation.Service;
import jadex.commons.future.Future;
import jadex.commons.future.IFuture;
import jadex.commons.future.ISubscriptionIntermediateFuture;
import jadex.commons.future.ITerminationCommand;
import jadex.commons.future.SubscriptionIntermediateFuture;
import jadex.commons.future.TerminationCommand;

/**
 *  Service for registering a nickname with component identifier.
 */
@Service
public class RegistryServiceE3 implements IRegistryServiceE3
{
	//-------- attributes --------
	
	 protected Set<SubscriptionIntermediateFuture<String>>   subscriptions
     = new LinkedHashSet<SubscriptionIntermediateFuture<String>>();
	
	/** The entries map. */
	protected Map<String, IComponentIdentifier> entries = new HashMap<String, IComponentIdentifier>();
	
	//-------- methods --------

	/** 
	 *  Register a chatter. 
	 */
	public ISubscriptionIntermediateFuture<String> register(IComponentIdentifier cid, String nickname)
	{
		// entries.forEach((k,v)->{System.out.println("Item : " + k); v.notifyAll();});
		entries.put(nickname, cid);
		System.out.println("!!!!!!!!!!!!!!!!!");
		
		
		final SubscriptionIntermediateFuture<String>    ret = new SubscriptionIntermediateFuture<String>();
        subscriptions.add(ret);
        System.out.println("Subscribers number: " + subscriptions.size());
        
        for(SubscriptionIntermediateFuture<String> subscriber: subscriptions)
        {
            // Add the current time as intermediate result.
            // The if-undone part is used to ignore errors,
            // when subscription was cancelled in the mean time.
			System.out.println("@@@@@@@@@@@@@@@@@@@");
			for (String nick : entries.keySet()) {
				subscriber.addIntermediateResultIfUndone(nick);
			}
			
			//subscriber.addResultListener(new IFunctionalResultListener<Set<String>>());
			//subscriber.addIntermediateResult(subscriptions);
            // subscriber.addIntermediateResultIfUndone(nickname);
        }
        
        ret.setTerminationCommand((ITerminationCommand) new TerminationCommand()
        {
            /**
             *  The termination command allows to be informed, when the subscription ends,
             *  e.g. due to a communication error or when the service user explicitly
             *  cancels the subscription.
             */
            public void terminated(Exception reason)
            {
                System.out.println("removed subscriber due to: "+reason);
                subscriptions.remove(ret);
            }
        });

        return ret;
	}
	
	/**
	 *  Get the registered chatters.
	 */
	public IFuture<Map<String, IComponentIdentifier>> getChatters()
	{
		return new Future<Map<String, IComponentIdentifier>>(entries);
	}
}

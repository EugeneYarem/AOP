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
public class RegistryService implements IRegistryService
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
		entries.put(nickname, cid);
		
		final SubscriptionIntermediateFuture<String> ret = new SubscriptionIntermediateFuture<String>();
        subscriptions.add(ret);
        // System.out.println("Subscribers number: " + subscriptions.size());
        
        for(SubscriptionIntermediateFuture<String> subscriber: subscriptions)
        {
			subscriber.addIntermediateResultIfUndone("#general");
			for (String nick : entries.keySet()) {
				subscriber.addIntermediateResultIfUndone(nick);
			}
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

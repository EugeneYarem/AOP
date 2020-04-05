package laba1;

import java.util.Map;

import jadex.bridge.IComponentIdentifier;
import jadex.commons.future.IFuture;
import jadex.commons.future.ISubscriptionIntermediateFuture;

/**
 *  Simple registry service.
 */
public interface IRegistryServiceE3
{
	/** 
	 *  Register a chatter. 
	 */
	public ISubscriptionIntermediateFuture<String> register(IComponentIdentifier cid, String nickname);
	
	/**
	 *  Get the registered chatters.
	 */
	public IFuture<Map<String, IComponentIdentifier>> getChatters();
}


package laba1;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import jadex.bridge.IComponentIdentifier;
import jadex.bridge.IComponentStep;
import jadex.bridge.IInternalAccess;
import jadex.bridge.component.IExecutionFeature;
import jadex.bridge.service.annotation.Value;
import jadex.bridge.service.component.IRequiredServicesFeature;
import jadex.bridge.service.types.clock.IClockService;
import jadex.commons.future.DefaultResultListener;
import jadex.commons.future.IFuture;
import jadex.commons.future.IIntermediateFuture;
import jadex.commons.future.IIntermediateResultListener;
import jadex.commons.future.ISubscriptionIntermediateFuture;
import jadex.commons.future.IntermediateDefaultResultListener;
import jadex.micro.annotation.Agent;
import jadex.micro.annotation.AgentArgument;
import jadex.micro.annotation.AgentBody;
import jadex.micro.annotation.AgentService;
import jadex.micro.annotation.Binding;
import jadex.micro.annotation.Description;
import jadex.micro.annotation.Implementation;
import jadex.micro.annotation.ProvidedService;
import jadex.micro.annotation.ProvidedServices;
import jadex.micro.annotation.RequiredService;
import jadex.micro.annotation.RequiredServices;

/**
 *  Chat micro agent with a . 
 */
@Description("This agent provides a basic chat service.")
@Agent
@ProvidedServices(@ProvidedService(type=IChatService.class, 
	implementation=@Implementation(value=ChatServiceD5.class, 
	interceptors=@Value(clazz=SpamInterceptorD4.class))))
@RequiredServices({
	@RequiredService(name="clockservice", type=IClockService.class, 
		binding=@Binding(scope=Binding.SCOPE_PLATFORM)),
	@RequiredService(name="chatservices", type=IChatService.class, multiple=true,
		binding=@Binding(dynamic=true, scope=Binding.SCOPE_GLOBAL)),
	@RequiredService(name="regservice", type=IRegistryServiceE3.class,
		binding=@Binding(dynamic=true, scope=Binding.SCOPE_GLOBAL))
})
public class ChatD5Agent
{
	/** The agent. */
	@Agent
	protected IInternalAccess agent;
	
	/** The nickname. */
	@AgentArgument
	protected String nickname;
	
	protected ISubscriptionIntermediateFuture<String> subscription;
	
	/**
	 *  Execute the functional body of the agent.
	 *  Is only called once.
	 */
	@AgentBody
	public void executeBody()
	{
		IFuture<IRegistryServiceE3>	regservice	= agent.getComponentFeature(IRequiredServicesFeature.class).getRequiredService("regservice");
		regservice.addResultListener(new DefaultResultListener<IRegistryServiceE3>()
		{
			public void resultAvailable(final IRegistryServiceE3 rs)
			{
				nickname = agent.getComponentIdentifier().getName();
				System.out.println(nickname);
				subscription = rs.register(agent.getComponentIdentifier(), nickname);
				
				rs.getChatters().addResultListener(new DefaultResultListener<Map<String, IComponentIdentifier>>()
				{
					public void resultAvailable(Map<String, IComponentIdentifier> chatters)
					{
						System.out.println(nickname + ": The current chatters number: "+chatters.size());
					}
				});
				
				subscription.addIntermediateResultListener(new IntermediateDefaultResultListener<String>() {
				    public void intermediateResultAvailable(String nick) {
				    	// System.out.println(nickname + ": New user joined to chat: " + result);
				    	
				    	/*IFuture<IChatService> chatservice = agent.getComponentFeature(IRequiredServicesFeature.class).getRequiredService("chatservices");
				    	chatservice.addResultListener(new DefaultResultListener<IChatService>()
					{
						public void resultAvailable(IChatService cs)
						{
							System.out.println(nickname + " - " + result + " : " + result.equals(nickname));
							if(!result.equals(nickname)) {
								cs.addChatter(result);
							}
						}
					});*/
				    	
				    	IIntermediateFuture<IChatService>	fut	= agent.getComponentFeature(IRequiredServicesFeature.class).getRequiredServices("chatservices");
						fut.addResultListener(new IIntermediateResultListener<IChatService>()
						{
							public void resultAvailable(Collection<IChatService> result)
							{
								for(Iterator<IChatService> it=result.iterator(); it.hasNext(); )
								{
									IChatService cs = it.next();
									try
									{
										//System.out.println(nickname + " - " + nick + " : " + nick.equals(nickname));
										/* if(!nick.equals(nickname)) {
											cs.addChatter(nick);
										}*/ 
										// cs.message(ChatGuiD5.this.agent.getComponentIdentifier().getName(), text);
									}
									catch(Exception e)
									{
										System.out.println("Error while adding new chatter: "+cs);
									}
								}
							}
							
							public void intermediateResultAvailable(IChatService cs)
							{
								
										
										if(!nick.equals(nickname)) {
											System.out.println(nickname + " - " + nick + " : " + nick.equals(nickname));
											cs.addChatter(nick);
										}
										
							}
							
							public void finished()
							{
							}
							
							public void exceptionOccurred(Exception exception)
							{
							}
							
						});
				    	
				    }
				});
				
				 /*while(subscription.hasNextIntermediateResult())
		        {
		            String  newChatter    = subscription.getNextIntermediateResult();
		            // String  platform    = ((IService)timeservice).getServiceIdentifier().getProviderId().getPlatformName();
		            System.out.println("New user joined to chat: " + newChatter);
		        }*/
				
				/* agent.getComponentFeature(IExecutionFeature.class).waitForDelay(1000, new IComponentStep<Void>()
				{
					public IFuture<Void> execute(IInternalAccess ia)
					{
						rs.getChatters().addResultListener(new DefaultResultListener<Map<String, IComponentIdentifier>>()
						{
							public void resultAvailable(Map<String, IComponentIdentifier> chatters)
							{
								System.out.println(nickname + ": The current chatters number: "+chatters.size());
								/*final IComponentIdentifier cid = chatters.get(partner);
								if(cid==null)
								{
									System.out.println("Could not find chat partner named: "+partner);
								}
								else
								{
									agent.getComponentFeature(IRequiredServicesFeature.class).searchService(IChatService.class, cid)
										.addResultListener(new DefaultResultListener<IChatService>()
									{
										public void resultAvailable(IChatService cs)
										{
											System.out.println("is on: "+IComponentIdentifier.LOCAL.get());
											cs.message(agent.getComponentIdentifier().toString(), "Private hello from: "+nickname);
										}
									});
								}*/
							/*}
						});
						return IFuture.DONE;
					}
				});*/
			}
		});
	}
	
	 /*@AgentService
	    public void addRegistryService()
	    {
		 // IFuture<IRegistryServiceE3>	regservice	= agent.getComponentFeature(IRequiredServicesFeature.class).getRequiredService("regservice");
			// regservice.addResultListener(new DefaultResultListener<IRegistryServiceE3>()
			// {
				// public void resultAvailable(final IRegistryServiceE3 rs)
				// {
					// nickname = agent.getComponentIdentifier().getName();
					// System.out.println(nickname);
					// subscription = rs.register(agent.getComponentIdentifier(), nickname);
					
					while(subscription.hasNextIntermediateResult())
			        {
			            String  newChatter    = subscription.getNextIntermediateResult();
			            // String  platform    = ((IService)timeservice).getServiceIdentifier().getProviderId().getPlatformName();
			            System.out.println("New user joined to chat: " + newChatter);
			        }
					
					/* rs.getChatters().addResultListener(new DefaultResultListener<Map<String, IComponentIdentifier>>()
					{
						public void resultAvailable(Map<String, IComponentIdentifier> chatters)
						{
							System.out.println(nickname + ": The current chatters number: "+chatters.size());
						}
					}); */
				//}
			// });
		 
	    //}
}
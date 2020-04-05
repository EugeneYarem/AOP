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
	implementation=@Implementation(value=ChatService.class, 
	interceptors=@Value(clazz=SpamInterceptor.class))))
@RequiredServices({
	@RequiredService(name="clockservice", type=IClockService.class, 
		binding=@Binding(scope=Binding.SCOPE_PLATFORM)),
	@RequiredService(name="chatservices", type=IChatService.class, multiple=true,
		binding=@Binding(dynamic=true, scope=Binding.SCOPE_GLOBAL)),
	@RequiredService(name="regservice", type=IRegistryService.class,
		binding=@Binding(dynamic=true, scope=Binding.SCOPE_GLOBAL))
})
public class ChatAgent
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
		IFuture<IRegistryService>	regservice	= agent.getComponentFeature(IRequiredServicesFeature.class).getRequiredService("regservice");
		regservice.addResultListener(new DefaultResultListener<IRegistryService>()
		{
			public void resultAvailable(final IRegistryService rs)
			{
				nickname = agent.getComponentIdentifier().getName();
				subscription = rs.register(agent.getComponentIdentifier(), nickname);
				
				subscription.addIntermediateResultListener(new IntermediateDefaultResultListener<String>() {
				    public void intermediateResultAvailable(String nick) {				    	
				    	IIntermediateFuture<IChatService>	fut	= agent.getComponentFeature(IRequiredServicesFeature.class).getRequiredServices("chatservices");
						fut.addResultListener(new IIntermediateResultListener<IChatService>()
						{
							public void resultAvailable(Collection<IChatService> result)
							{}
							
							public void intermediateResultAvailable(IChatService cs)
							{
								if(!nick.equals(nickname)) {
									//System.out.println(nickname + " - " + nick + " : " + nick.equals(nickname));
									cs.addChatter(nick);
								}										
							}
							
							public void finished()
							{}
							
							public void exceptionOccurred(Exception exception)
							{}
							
						});
				    	
				    }
				});	
			}
		});
	}
}
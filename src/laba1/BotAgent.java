package laba1;

import jadex.bridge.IInternalAccess;
import jadex.micro.annotation.Agent;
import jadex.micro.annotation.AgentArgument;
import jadex.micro.annotation.Description;
import jadex.micro.annotation.Implementation;
import jadex.micro.annotation.ProvidedService;
import jadex.micro.annotation.ProvidedServices;

/**
 *  Chat micro agent with a bot service. 
 */
@Description("This agent presents the chat bot.")
@Agent
@ProvidedServices(@ProvidedService(type=IBotService.class, 
	implementation=@Implementation(BotService.class)))
public class BotAgent
{
	/** The agent. */
	@Agent
	protected IInternalAccess agent;
	
	@AgentArgument("password")
	protected String password;
}
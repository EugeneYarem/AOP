package laba1;

import jadex.micro.annotation.Agent;
import jadex.micro.annotation.Description;
import jadex.micro.annotation.Implementation;
import jadex.micro.annotation.ProvidedService;
import jadex.micro.annotation.ProvidedServices;

/**
 *  Chat micro agent with a registry service. 
 */
@Description("This agent provides a registry service.")
@Agent
@ProvidedServices(@ProvidedService(type=IRegistryService.class, 
	implementation=@Implementation(RegistryService.class)))
public class RegistryAgent
{
}
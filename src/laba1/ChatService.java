package laba1;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
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

/**
 *  The chat service.
 */
@Service
public class ChatService implements IChatService
{
	//-------- attributes --------
	
	/** The agent. */
	@ServiceComponent
	protected IInternalAccess agent;
	
	/** The clock service. */
	protected IClockService clock;
	
	/** The time format. */
	protected DateFormat format;
	
	/** The user interface. */
	protected ChatGui gui;
	
	//-------- attributes --------
	
	/**
	 *  Init the service.
	 */
	@ServiceStart
	public IFuture<Void> startService()
	{
		final Future<Void> ret = new Future<Void>();
		
		this.format = new SimpleDateFormat("hh:mm:ss");
		IFuture<IClockService> fut = agent.getComponentFeature(IRequiredServicesFeature.class).getRequiredService("clockservice");
		fut.addResultListener(new SwingExceptionDelegationResultListener<IClockService, Void>(ret)
		{
			public void customResultAvailable(IClockService result)
			{
				clock = result;
				gui = createGui(agent.getExternalAccess());
				ret.setResult(null);
			}
		});
		return ret;
	}
	
	/**
	 *  Init the service.
	 */
	@ServiceShutdown
	public void shutdownService()
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				gui.dispose();				
			}
		});
//		return IFuture.DONE;
	}
	
	/**
	 *  Receives a chat message.
	 */
	public void message(final String from, final String to, final String text, boolean isMessageFromBot)
	{
		gui.addMessage(from, to, "<p><u>" + format.format(new Date(clock.getTime()))+"</u> : <b>"+ (isMessageFromBot ? "@Bot" : from) +"</b> : "+text+ "</p>");
	}
	
	/**
	 *  Create the gui.
	 */
	protected ChatGui createGui(IExternalAccess agent)
	{
		return new ChatGui(agent);
	}

	@Override
	public void addChatter(String nickname) {
		gui.addChatter(nickname);
	}
}

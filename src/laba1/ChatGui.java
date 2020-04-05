package laba1;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;

import jadex.bridge.IComponentStep;
import jadex.bridge.IExternalAccess;
import jadex.bridge.IInternalAccess;
import jadex.bridge.service.component.IRequiredServicesFeature;
import jadex.commons.future.IFuture;
import jadex.commons.future.IIntermediateFuture;
import jadex.commons.future.IIntermediateResultListener;

/**
 *  Basic chat user interface.
 */
public class ChatGui extends JFrame
{
	//-------- attributes --------
	
	/** The textfield with received messages. */
	protected JLabel chat;
	
	protected JTextArea received;
	
	protected JList<String> chattersList;
	
	/** The agent owning the gui. */
	protected IExternalAccess agent;
	
	protected String nickname;
	
	protected Map<String, String> messages;
	
	protected String currentChat;
	
	protected Set<String> activeChats;
	
	//-------- constructors --------
	
	/**
	 *  Create the user interface
	 */
	public ChatGui(IExternalAccess agent)
	{
		super(agent.getComponentIdentifier().getName());
		
		this.agent	= agent;
		this.setLayout(new BorderLayout());
		
		nickname = ChatGui.this.agent.getComponentIdentifier().getName();
		
		chat = new JLabel("#general");
		chat.setFont(new Font("Arial", Font.BOLD, 20 ));
		
		received = new JTextArea(10, 20);
		received.setEditable(false);
		received.setDisabledTextColor(Color.BLACK);
		
		messages = new HashMap<String, String>();
		currentChat = "#general";
		activeChats = new HashSet<String>();
		
		chattersList = new JList<String>(new DefaultListModel<String>());
		chattersList.setBackground(Color.PINK);
		chattersList.setSelectionBackground(Color.magenta);
		MouseListener mouseListener = new MouseAdapter() {
		    public void mouseClicked(MouseEvent e) {
		        if (e.getClickCount() == 2) {
		           String selectedItem = (String) chattersList.getSelectedValue();
		           currentChat = selectedItem;
		           received.setText(messages.get(selectedItem));
		           activeChats.remove(selectedItem);
		           chat.setText(currentChat);
		        }
		    }
		};
		chattersList.addMouseListener(mouseListener);		
		
		final JTextField message = new JTextField();
		JButton send = new JButton("send");
		
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(chattersList, BorderLayout.WEST);
		panel.add(chat, BorderLayout.NORTH);
		panel.add(received, BorderLayout.CENTER);
		
		JPanel panel2 = new JPanel(new BorderLayout());
		panel2.add(message, BorderLayout.CENTER);
		panel2.add(send, BorderLayout.EAST);
		
		getContentPane().add(panel, BorderLayout.CENTER);
		getContentPane().add(panel2, BorderLayout.SOUTH);
		
		send.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				final String text = message.getText(); 
				ChatGui.this.agent.scheduleStep(new IComponentStep<Void>()
				{
					public IFuture<Void> execute(IInternalAccess ia)
					{
						IIntermediateFuture<IChatService>	fut	= ia.getComponentFeature(IRequiredServicesFeature.class).getRequiredServices("chatservices");
						fut.addResultListener(new IIntermediateResultListener<IChatService>()
						{
							public void resultAvailable(Collection<IChatService> result)
							{
								/*for(Iterator<IChatService> it=result.iterator(); it.hasNext(); )
								{
									IChatService cs = it.next();
									try
									{
										cs.message(nickname, currentChat, text);
									}
									catch(Exception e)
									{
										System.out.println("Could not send message to: "+cs);
									}
								}*/
							}
							
							public void intermediateResultAvailable(IChatService cs)
							{
								System.out.println("found: "+cs);
								cs.message(nickname, currentChat, text);
							}
							
							public void finished()
							{
							}
							
							public void exceptionOccurred(Exception exception)
							{
							}
							
						});
						return IFuture.DONE;
					}
				});
			}
		});
		
		addWindowListener(new WindowAdapter()
		{
			public void windowClosed(WindowEvent e)
			{
				ChatGui.this.agent.killComponent();
				ChatGui.this.agent	= null;
			}
		});

		pack();
		setVisible(true);
	}
	
	/**
	 *  Method to add a new text message.
	 *  @param text The text.
	 */
	public void addMessage(final String from, final String to, final String text)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				if(to.equals("#general")) {
					String curText = messages.get("#general");
					if(curText != null)
						messages.put("#general", curText + text + "\n");
					else messages.put("#general", text + "\n");
					
					received.setText(messages.get(currentChat));
					
					if(!from.equals(nickname) && !to.equals(currentChat)) {
						activeChats.add(to);
					}
					
					chattersList.setCellRenderer(new ComplexCellRenderer(activeChats));
					
				} else if(from.equals(nickname) || (from.equals(currentChat) && to.equals(nickname))) {
					String curText = messages.get(currentChat);
					if(curText != null)
						messages.put(currentChat, curText + text + "\n");
					else messages.put(currentChat, text + "\n");
					
					received.setText(messages.get(currentChat));
				}
				else if(to.equals(nickname)) {
					String curText = messages.get(from);
					if(curText != null)
						messages.put(from, curText + text + "\n");
					else messages.put(from, text + "\n");
					
					if(from.equals(currentChat)) {
						received.setText(messages.get(currentChat));
					}
					
					activeChats.add(from);					
					chattersList.setCellRenderer(new ComplexCellRenderer(activeChats));
				}
			}	
		});
	}
	
	public void addChatter(final String nickname)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				if(!((DefaultListModel)chattersList.getModel()).contains(nickname) && !agent.getComponentIdentifier().getName().equals(nickname)) {
					((DefaultListModel)chattersList.getModel()).addElement(nickname);
					messages.put(nickname, "");
				}
 				chattersList.setSelectedValue(currentChat, true);
			}
		});
	}
	
	class ComplexCellRenderer implements ListCellRenderer {
		  protected DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();
		  
		  protected Set<String> activeChats;
		  
		  public ComplexCellRenderer(Set<String> chatsForHighlighting) {
			  activeChats = chatsForHighlighting;
		  }

		  public Component getListCellRendererComponent(JList list, Object value, int index,
		      boolean isSelected, boolean cellHasFocus) {
			  Component c = defaultRenderer.getListCellRendererComponent( list, value, index, isSelected, cellHasFocus );
	            if ( activeChats.contains(value) ) {
	                c.setBackground( Color.yellow );
	            }
	            else {
	                c.setBackground( Color.pink );
	            }
	            
	            if ( isSelected ) {
	            	c.setBackground(Color.magenta);
	            }
	            
	            return c;
		  }
		}
}

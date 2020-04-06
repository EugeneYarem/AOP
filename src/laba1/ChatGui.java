package laba1;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;

import jadex.bridge.IComponentStep;
import jadex.bridge.IExternalAccess;
import jadex.bridge.IInternalAccess;
import jadex.bridge.service.RequiredServiceInfo;
import jadex.bridge.service.component.IRequiredServicesFeature;
import jadex.bridge.service.search.SServiceProvider;
import jadex.commons.future.IFuture;
import jadex.commons.future.IIntermediateFuture;
import jadex.commons.future.IIntermediateResultListener;
import jadex.commons.future.IntermediateDefaultResultListener;

/**
 *  Basic chat user interface.
 */
public class ChatGui extends JFrame
{
	//-------- attributes --------
	
	/** The textfield with received messages. */
	protected JLabel chat;
	
	protected JTextPane received;
	
	protected JList<String> chattersList;
	
	/** The agent owning the gui. */
	protected IExternalAccess agent;
	
	protected String nickname;
	
	protected Map<String, String> messages;
	
	protected String currentChat;
	
	protected Set<String> activeChats;
	
	private ArrayList<Message> currentCensoredMessage; // contains censored message and possible bot answer on it
	
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
		
		JLabel madeFriends = new JLabel("You made friends with the bot!");
		madeFriends.setFont(new Font("Arial", Font.BOLD, 15 ));
		madeFriends.setForeground(Color.decode("#449c5c"));
		
		received = new JTextPane();
		received.setEditable(false);
		received.setDisabledTextColor(Color.BLACK);
		received.setContentType("text/html");
		JScrollPane scroller = new JScrollPane();
	    scroller.setViewportView(received);
		
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
		JButton makeFriends = new JButton("Make friends with the bot");
		
		JPanel topPanel = new JPanel(new BorderLayout());
		topPanel.add(chat, BorderLayout.WEST);
		topPanel.add(makeFriends, BorderLayout.EAST);
		
		JPanel centerPanel = new JPanel(new BorderLayout());
		centerPanel.add(chattersList, BorderLayout.WEST);
		centerPanel.add(topPanel, BorderLayout.NORTH);
		centerPanel.add(scroller, BorderLayout.CENTER);
		
		JPanel bottomPanel = new JPanel(new BorderLayout());
		bottomPanel.add(message, BorderLayout.CENTER);
		bottomPanel.add(send, BorderLayout.EAST);
		
		getContentPane().add(centerPanel, BorderLayout.CENTER);
		getContentPane().add(bottomPanel, BorderLayout.SOUTH);
		
		this.setMinimumSize(new Dimension(500, 400));
		
		makeFriends.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				final String text = message.getText(); 
				
				SServiceProvider.getServices(agent, IBotService.class, RequiredServiceInfo.SCOPE_GLOBAL)
		        .addResultListener(new IntermediateDefaultResultListener<IBotService>()
		    {
		        public void intermediateResultAvailable(IBotService s)
		        {
		        	String password = JOptionPane.showInputDialog(null,
		        			 "Enter a password to make friends with the bot",
		        			 "What is a password?",
		        			 JOptionPane.QUESTION_MESSAGE);
		        	
		        	if(s.addFriend(nickname, password)) {
		        		JOptionPane.showMessageDialog(ChatGui.this,
	        				    "You are allowed to send messages to the bot.",
	        				    "You made friends!",
	        				    JOptionPane.INFORMATION_MESSAGE);
		        		topPanel.remove(makeFriends);
		        		topPanel.add(madeFriends, BorderLayout.EAST);
		        		topPanel.updateUI();
		        	} else {
		        		JOptionPane.showMessageDialog(ChatGui.this,
	        				    "The password was wrong.",
	        				    "Sorry...",
	        				    JOptionPane.INFORMATION_MESSAGE);
		        	}
		        }
		    });
			
			}
		});
		
		send.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				final String text = message.getText(); 
				
				SServiceProvider.getServices(agent, IBotService.class, RequiredServiceInfo.SCOPE_GLOBAL)
		        .addResultListener(new IntermediateDefaultResultListener<IBotService>()
		    {
		        public void intermediateResultAvailable(IBotService s)
		        {
		        	try {
			        	currentCensoredMessage = s.validateMessage(nickname, currentChat, text);       	
						
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
										for(Message i : currentCensoredMessage) {
											cs.message(i.getFrom(), i.getTo(), i.getText(), i.isMessageFromBot());
										}
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
		        	} catch(Exception e) {
		        		System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
		        		System.out.println(e.getMessage());
		        		try {
		        			send.setEnabled(false);
		        			JOptionPane.showMessageDialog(ChatGui.this,
		        				    "You are forbidden to send messages for 1 minute.\nPlease, be polite!",
		        				    "You are forbidden",
		        				    JOptionPane.WARNING_MESSAGE);
							TimeUnit.MINUTES.sleep(1);
							send.setEnabled(true);
						} catch (InterruptedException e1) {
							System.out.println("======================================");
							e1.printStackTrace();
						}
		        	}
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
						messages.put("#general", curText + text);
					else messages.put("#general", text);
					
					received.setText(messages.get(currentChat));
					
					if(!from.equals(nickname) && !to.equals(currentChat)) {
						activeChats.add(to);
					}
					
					chattersList.setCellRenderer(new ComplexCellRenderer(activeChats));
					
				} else if(from.equals(nickname) || (from.equals(currentChat) && to.equals(nickname))) {
					String curText = messages.get(currentChat);
					if(curText != null)
						messages.put(currentChat, curText + text);
					else messages.put(currentChat, text);
					
					received.setText(messages.get(currentChat));
				}
				else if(to.equals(nickname)) {
					String curText = messages.get(from);
					if(curText != null)
						messages.put(from, curText + text);
					else messages.put(from, text);
					
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

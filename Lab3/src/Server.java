/*
 * @ Ranjan Dhar
 */
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.*;
import java.util.List;
import java.util.ArrayList;


		public class Server
		{
			private ServerSocket serverSocket;
			private static final ThreadPoolExecutor pool = (ThreadPoolExecutor)Executors.newFixedThreadPool(10);
			private int clientId=0;
			private int chatRoomNo=100;
		
			List<ChatRoom> chatRoomList = new ArrayList<ChatRoom>();
			List<ClientThread> clientLists = new ArrayList<ClientThread>();
			
			// Constructor
			
			public Server(ServerSocket serverSocket)
			{
				this.serverSocket = serverSocket;
			}
			
			// Accept Client Connection
			
			public void start()
			{
				try
				{
					while(true)
					{	
						if(pool.getActiveCount()<pool.getMaximumPoolSize())
						{										
							System.out.println(" || Chatroom up & running || ");													
							Socket socket = serverSocket.accept();
                                                        clientId++;
							ClientThread client = new ClientThread(socket,this,clientId);
							clientLists.add(client);
							Server.pool.execute(client);
						}
						
					}						                       
				}
				catch(Exception e)
				{
					System.out.println(e);
				}	
			}
			
			// Kill Service Method
			
			public void killService()
			{
				try
				{
					pool.shutdownNow();
					serverSocket.close();
					System.exit(0);
				}
				catch(Exception e)
				{
					System.out.println(e);
				}
			}
			
			// Initializing default Chat Rooms
			
			public void initializeChatRooms()
			{
				for(int i=0;i<5;i++)
				{
					String chatRoomName = "room"+(i+1);
					int chatRoomId = chatRoomNo+1;
					chatRoomNo++;
					ChatRoom newChatRoom = new ChatRoom(chatRoomName ,chatRoomId);
					chatRoomList.add(newChatRoom);
				}
			}
			
		    // Joining Method
			
			@SuppressWarnings("rawtypes")
			public void joinChatRoom(String chatRoomName,ClientThread clientThread)
			{
				ListIterator illookup = chatRoomList.listIterator();
				while(illookup.hasNext())
				{
					ChatRoom cr = (ChatRoom)illookup.next();
					if((cr.chatRoomName).equals(chatRoomName))
					{
						cr.addClient(clientThread);
						break;
					}					
				}
			}
			
			// Leaving Method
			
			@SuppressWarnings("rawtypes")
			public void leaveChatRoom(int chatRoomId,ClientThread clientThread)
			{
				ListIterator illeave = chatRoomList.listIterator();
				while(illeave.hasNext())
				{
					ChatRoom cr = (ChatRoom)illeave.next();
					if(cr.chatRoomId==chatRoomId)
					{
						cr.removeClient(clientThread);
						break;
					}					
				}
			}
			
			// Disconnect Method
			
			@SuppressWarnings("rawtypes")
			public void leaveAllChatRooms(ClientThread clientThread)
			{
				ListIterator illeave = chatRoomList.listIterator();
				while(illeave.hasNext())
				{
					ChatRoom cr = (ChatRoom)illeave.next();
					ListIterator ilclientsConnected = cr.clientsConnected.listIterator();
					while(ilclientsConnected.hasNext())
					{
						ClientThread ctCompare = (ClientThread)ilclientsConnected.next();
						if(ctCompare.clientName.equals(clientThread.clientName))
						{
							cr.disconnect(clientThread);
							break;
						}
					}					
				}
			} 
			
			// Messaging Method
			
			@SuppressWarnings("rawtypes")
			public void chat(int chatRoomId,ClientThread clientThread,String message)
			{
				ListIterator ilchat = chatRoomList.listIterator();
				while(ilchat.hasNext())
				{
					ChatRoom cr = (ChatRoom)ilchat.next();
					if(cr.chatRoomId==chatRoomId)
					{
						cr.chat(clientThread,message);
						break;
					}					
				}
			}
		
	
			// Client Worker Thread
			
			public class ClientThread implements Runnable
			{
				private Socket socket;
				private Server serverSoc;
				private boolean kill;
				private int clientId;
				private String clientName;
			
				ClientThread(Socket socket,Server serverSoc,int clientId)
				{
					this.socket=socket;
					this.serverSoc=serverSoc;
					this.kill = false;
					this.clientId=clientId;
				}

				public void joinChatRoom(String chatRoomName, ClientThread clientThread)
				{
					serverSoc.joinChatRoom(chatRoomName,clientThread);	
				}
				
				public void leaveChatRoom(int chatRoomId, ClientThread clientThread)
				{
					serverSoc.leaveChatRoom(chatRoomId,clientThread);	
				}
				
				public void leaveAllChatRooms(ClientThread clientThread)
				{
					serverSoc.leaveAllChatRooms(clientThread);
				}
				
				public void chat(int chatRoomId, ClientThread clientThread, String message)
				{
					serverSoc.chat(chatRoomId, clientThread,message);	
				}
				public void killService()
				{
					serverSoc.killService();	
				}
			
				
				@SuppressWarnings("rawtypes")
				@Override			
				public void run()
				{
					try
					{
						BufferedReader bd = new BufferedReader(new InputStreamReader(socket.getInputStream()));
						PrintWriter output = new PrintWriter(socket.getOutputStream(),true);
						while(!kill)
						{
							List<String> inputStrings= new ArrayList<String>();
							String temp = "";

							while(bd.ready())
							{
								inputStrings.add(bd.readLine());
							}
							ListIterator ilstring = inputStrings.listIterator();
							while(ilstring.hasNext())
							{
								temp=temp+(String)(ilstring.next());
							}

							// Verifying the input from Client
							
							if(temp.equals("HELO BASE_TEST"))
							{
								String messagetoclient = "HELO BASE_TEST";
								messagetoclient = messagetoclient + "\nIP:134.226.58.115\nPort:5949\nStudentID:15314217\n";
								output.println(messagetoclient);
							}
							else if(temp.startsWith("JOIN_CHATROOM")==true)
							{	
								String chatRoomName = ((String)(inputStrings.get(0))).split(":")[1];
								this.clientName =((String)(inputStrings.get(3))).split(":")[1];
								joinChatRoom(chatRoomName,this);
												                   
							}
							else if(temp.startsWith("CHAT")==true)
							{
								int chatRoomId =Integer.parseInt( (((String)(inputStrings.get(0))).split(":")[1]).trim());
								String message =(((String)(inputStrings.get(3))).split(":")[1].trim());
								chat(chatRoomId,this,message+"\n\n");
							}
							else if(temp.startsWith("LEAVE_CHATROOM")==true)
							{
								int chatRoomId =Integer.parseInt( (((String)(inputStrings.get(0))).split(":")[1]).trim());
								leaveChatRoom(chatRoomId,this);
							}
							else if(temp.startsWith("DISCONNECT")==true)
							{
								leaveAllChatRooms(this);
								socket.close();
							}
							else if(temp.equals("KILL_SERVICE"))
							{

								kill=true;
								socket.close();
								killService();
						
							}
							else
							{
								// do nothing		
							}
						}
					}
					catch(Exception e)
					{
						System.out.println(e);
					}
				}
			}
			
			public class ChatRoom
			{
				private String chatRoomName;
				private int chatRoomId;
				List<ClientThread> clientsConnected = new ArrayList<ClientThread>();

				ChatRoom(String chatRoomName,int chatRoomId)
				{
					this.chatRoomName = chatRoomName;
					this.chatRoomId = chatRoomId;
				}

				// Adding a Client to the present Chat Room
				public void addClient(ClientThread clientThread)
				{
					this.clientsConnected.add(clientThread);
					String messagetoclient = "JOINED_CHATROOM:"+this.chatRoomName+"\nSERVER_IP:134.226.58.115\nPORT:5949\nROOM_REF:"+this.chatRoomId+"\nJOIN_ID:"+clientThread.clientId+"\n";
					sendMessage(messagetoclient,clientThread);
					chat(clientThread,clientThread.clientName+" has joined this chatroom.\n\n");
				}
				
				// Removing a Client from the present Chat Room
				public void removeClient(ClientThread clientThread)
				{
					String messagetoclient = "LEFT_CHATROOM:"+this.chatRoomId+"\nJOIN_ID:"+clientThread.clientId+"\n";
					sendMessage(messagetoclient,clientThread);
					chat(clientThread,clientThread.clientName+"  has left this chatroom.\n\n");
					this.clientsConnected.remove(clientThread);
					
				}
				
				// Disconnecting from Chat Rooms and Servers
				public void disconnect(ClientThread clientThread)
				{
					chat(clientThread,clientThread.clientName+"  has left this chatroom.\n\n");
					this.clientsConnected.remove(clientThread);
					
				}
				
				// Messaging Method
				@SuppressWarnings("rawtypes")
				public void chat(ClientThread clientThread, String message)
				{
					ListIterator ilClientChat = clientsConnected.listIterator();
					String messageToClient2 = "CHAT:"+this.chatRoomId+"\nCLIENT_NAME:"+clientThread.clientName+"\nMESSAGE:"+message;
					if(!ilClientChat.hasNext())
					{
						sendMessage(messageToClient2,clientThread);
					}
					while(ilClientChat.hasNext())
					{
						ClientThread ct = (ClientThread)ilClientChat.next();
						sendMessage(messageToClient2,ct);
					}
								
				}
			
				// Method to send messages
				
				public void sendMessage(String messagetoclient,ClientThread clientThread)
				{
					try
					{
						PrintWriter output =  new PrintWriter(clientThread.socket.getOutputStream(),true);
						output.printf(messagetoclient);	
					}
					catch(Exception e)
					{
						System.out.println(e);
					}
					
				}
				
			}
			
			// Main Method
			
			public static void main(String[] args) throws Exception
			{		
				
				Server serSoc = new Server(new ServerSocket(5949));
				serSoc.initializeChatRooms();
				serSoc.start();
			}																														
		}


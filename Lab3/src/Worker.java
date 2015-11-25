import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

public class Worker implements Runnable {
	
	private Socket socket;
	private ChatServer server;
	private int joinId;
	private boolean killServer;
	
	// Constructor
	public Worker(Socket socket, int clientId, ChatServer server){
		this.socket = socket;
		this.server = server;
		this.joinId = clientId;
		this.killServer = false;
	}
	
	// Join
	public void joinChatroom(String chatroom, String clientName){
		Client client = new Client(joinId, clientName, socket);
		server.joinChatRoom(chatroom, client);
	}
	
	// Leave
	public void leaveChatroom(String chatroomId, String clientId){
		int chatroom = Integer.parseInt(chatroomId);
		int client = Integer.parseInt(clientId);
		server.leaveChatRoom(chatroom, client);
	}
	
	// Chat
	public void chat(String chatroomId, String clientId, String message){
		int chatroom = Integer.parseInt(chatroomId);
		int client = Integer.parseInt(clientId);
		if(!server.chat(chatroom, client, message)){
			error(1);
		}
	}
	
	public String heloResponse(String command){
		return command + "\nIP:" + socket.getLocalAddress().toString().substring(1) + "\nPort:" + socket.getLocalPort() + "\nStudentID:15314217\n";  
	}
	
	// Sending an error message back to the client
	
	public void error(int error){
		try {
			PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
			String errorMessage = "ERROR_CODE: " + error + "\nERROR_DESCRIPTION: ";
			String description = "";
			if(error == 0){
				description = "Incorrect Format, Not enough lines\n";
			}
			else if(error == 1){
				description = "Chatroom does not exist\n";
			}
			writer.println(errorMessage + description);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
			ArrayList<String> lines;
			while(!killServer){
				lines = new ArrayList<String>();
				while(reader.ready()){
					lines.add(reader.readLine());
					System.out.println("Received: " + lines.get(0));
				}
				if(lines.isEmpty()){
					//Do nothing
				}
				else if(lines.get(0).startsWith("JOIN_CHATROOM")){
					String[] line0 = lines.get(0).split(":");
					String chatroomName = line0[1];
					String[] line3 = lines.get(3).split(":");
					String clientName = line3[1];
					joinChatroom(chatroomName, clientName);
				}
				else if(lines.get(0).startsWith("LEAVE_CHATROOM")){
					String[] line0 = lines.get(0).split(":");
					String chatroomId = line0[1].substring(1);
					String[] line1 = lines.get(1).split(":");
					String joinId = line1[1].substring(1);
					leaveChatroom(chatroomId, joinId);
				}
				else if(lines.get(0).startsWith("CHAT")){
					String[] line0 = lines.get(0).split(":");
					String chatroomId = line0[1].substring(1);
					String[] line1 = lines.get(1).split(":");
					String joinId = line1[1].substring(1);
					String[] line3 = lines.get(3).split(":");
					String message = line3[1].substring(1);
					chat(chatroomId, joinId, message);
				}
				else if(lines.get(0).startsWith("HELO")){
					writer.println(heloResponse(lines.get(0)));			
				}
				else if(lines.get(0).startsWith("KILL_SERVICE")){
					killServer = true;
					socket.close();
					server.killService();
				}
				else{
					// Do Nothing
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
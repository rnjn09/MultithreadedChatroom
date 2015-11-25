import java.net.Socket;

public class Client {

	private int id;
	private String name;
	private Socket socket;
	
	public Client(int id, String name, Socket socket){
		this.id = id;
		this.name = name;
		this.socket = socket;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Socket getSocket() {
		return socket;
	}

	public void setSocket(Socket socket) {
		this.socket = socket;
	}
	
	
}

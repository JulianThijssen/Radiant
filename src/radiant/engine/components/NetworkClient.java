package radiant.engine.components;

import java.net.Socket;

public class NetworkClient extends Component {
	private String host = "0.0.0.0";
	private int port = 80;
	
	private Socket socket;
	
	public NetworkClient(String host, int port) {
		this.host = host;
		this.port = port;
	}
}

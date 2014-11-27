package radiant.engine.network;

import java.util.ArrayList;
import java.util.List;

import radiant.engine.ISystem;
import radiant.engine.components.NetworkClient;

public class NetworkSystem implements ISystem {
	List<NetworkClient> clients = new ArrayList<NetworkClient>();
	
	@Override
	public void create() {

	}

	@Override
	public void update() {
		
	}

	@Override
	public void destroy() {

	}
	
	public NetworkClient createClient(String host, int port) {
		NetworkClient client = new NetworkClient(host, port);
		clients.add(client);
		return client;
	}
}

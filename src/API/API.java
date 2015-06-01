package API;


public class API {
	public API() {
	}
	
	public APICall process(String command) {
		if(command == null) return null;
		if(command.equals("PING")) {
			return new Ping();
		}
		return null;
	}
}

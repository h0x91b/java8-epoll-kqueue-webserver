package API;

import java.util.concurrent.Future;

import org.aredis.cache.AsyncRedisClient;
import org.aredis.cache.RedisCommand;
import org.aredis.cache.RedisCommandInfo;

import net.minidev.json.JSONArray;


public class Ping implements APICall {
	String response = "PONG";
	Boolean isComplete = false;
	int calls = 0;
	Boolean isProcessing = false;
	Future<RedisCommandInfo> futureRedis;
	public String getResponse() {
		return response;
	}
	
	public Boolean isComplete() {
		if(calls++ > 3 && !isProcessing) {
			isProcessing = true;
			readRedisResults();
		}
		return isComplete;
	}
	
	public int getStatusCode() {
		return 200;
	}
	
	void readRedisResults() {
        try {
            int val = Integer.parseInt((String) futureRedis.get().getResult());
            response = "PONG "+val;
            isComplete = true;
        }
        catch(Exception e) {
            e.printStackTrace();
            isComplete = true;
            response = "redis error";
        }
	}
	
	public void process(AsyncRedisClient redis, JSONArray args) {
		//enqueue to pipe a command...
		futureRedis = redis.submitCommand(RedisCommand.INCR, "INCR:PINGCMD");
	}
}

package org.colman.liberty;


import javax.ws.rs.ApplicationPath;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.api.StatefulRedisConnection;
import com.lambdaworks.redis.api.sync.RedisCommands;



@ApplicationPath("/liber")
@Path("/asaf")
public class HelloResource extends Application {
	
	
	
	@Context
	private UriInfo context;
	
	@GET
	@Produces("application/json")
	public String get() {
		 RedisClient client = RedisClient.create("redis://eKCBFHIvMfnZZYc6@pub-redis-17379.dal-05.1.sl.garantiadata.com:17379");
		 StatefulRedisConnection<String, String> con = client.connect(); 
		 RedisCommands<String, String> coms =  con.sync();
		 String res = coms.get("notifications");
		 coms.close();
		 client.shutdown();
		 return res;
		 
	}
	
	
}

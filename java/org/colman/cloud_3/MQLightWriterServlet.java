package org.colman.liberty;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import org.json.simple.JSONObject;

import com.ibm.mqlight.api.CompletionListener;
import com.ibm.mqlight.api.Delivery;
import com.ibm.mqlight.api.DestinationAdapter;
import com.ibm.mqlight.api.NonBlockingClient;
import com.ibm.mqlight.api.NonBlockingClientAdapter;
import com.ibm.mqlight.api.QOS;
import com.ibm.mqlight.api.SendOptions;
import com.ibm.mqlight.api.SendOptions.SendOptionsBuilder;
import com.ibm.mqlight.api.StringDelivery;
import com.ibm.mqlight.api.SubscribeOptions;


@ApplicationPath("/liberty")
@Path("Writer")
public class MQLightWriterServlet extends Application {
	

	/** The topic we subscribe on to receive notifications from the back-end */
	private static final String SUBSCRIBE_TOPIC = "mqlight/sample/words";



	NonBlockingClient client = null;
	
	@Context
	private UriInfo context;
	
	@SuppressWarnings("unchecked")
	@GET
	@Produces("application/json")
	public String get() {
		JSONObject json = new JSONObject();
		json.put("hello", "world");
		SendOptions opts = SendOptions.builder().setQos(QOS.AT_LEAST_ONCE).build();
		client.send(SUBSCRIBE_TOPIC,json.toString(),null,opts,new CompletionListener<Void>() {
			public void onError(NonBlockingClient client, Void context, Exception exception) {};
			public void onSuccess(NonBlockingClient client, Void context) {};
		},null);
		
		return "done !";
	}
	
	public MQLightWriterServlet() {

		client =  NonBlockingClient.create(null, new NonBlockingClientAdapter<Void>() {
			@Override
			public void onStarted(NonBlockingClient client, Void context) {
				// TODO Auto-generated method stub
				
				
			}
		},null);
		
	}

}
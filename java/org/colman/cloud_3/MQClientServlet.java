package org.colman.liberty;

import java.io.IOException;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.annotate.JsonAutoDetect;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ibm.mqlight.api.*;
import com.ibm.mqlight.api.SubscribeOptions.SubscribeOptionsBuilder;



@WebServlet("/SimpleServlet")
public class MQClientServlet extends HttpServlet {
	/**
	 * 
	 */
	
	
	
	
	
	/** The topic we subscribe on to receive notifications from the back-end */
	private static final String SUBSCRIBE_TOPIC = "mqlight/sample/words";

	
	//share id is important, it allows multiple destinations recieve a single message
	private static final String SHARE_ID = "fishalive-workers";

	
	private static final long serialVersionUID = 7937413877891273135L;


	Vector<String> ProcessedMessages = new Vector<String>();
	
	private void ProcessMessage(String Message) {
		JsonParser parser = new JsonParser();
		JsonObject obj = (JsonObject)parser.parse(Message);
		
		//do something with the message !!
		ProcessedMessages.add(obj.toString());
		
	}
	
	
	
	@Override
	public void init() throws ServletException {
		// TODO Auto-generated method stub
		super.init();

		NonBlockingClient.create(null, new NonBlockingClientAdapter<Void>() {
			@Override
			public void onStarted(NonBlockingClient client, Void context) {
				// TODO Auto-generated method stub
				
				//set share id !
				SubscribeOptions opts = SubscribeOptions.builder().setShare(SHARE_ID).build();
				client.subscribe(SUBSCRIBE_TOPIC, opts, new DestinationAdapter<Void>() {
					public void onMessage(NonBlockingClient client, Void context, Delivery delivery) {
						System.out.println("messege recieved from queue");
						StringDelivery sd = (StringDelivery)delivery;
						ProcessMessage(sd.getData());
					};
				}, new CompletionListener<Void>() {
					@Override
					public void onSuccess(NonBlockingClient client, Void context) {
					
						System.out.println("read from queue");
					}
					
					@Override
					public void onError(NonBlockingClient client, Void context, Exception exception) {
						System.out.println("something went wrong with queue");
						
					}
				},null);
				
				
			}
		},null);
		
	}
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setContentType("text/html");
	
		resp.getWriter().write("list of messages processed by service\n");
		
		for (int i=0; i<ProcessedMessages.size(); i++)
			resp.getWriter().println(ProcessedMessages.elementAt(i));
		
	}
	
	
}

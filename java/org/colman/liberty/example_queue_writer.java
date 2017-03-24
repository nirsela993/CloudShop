package org.colman.liberty;

import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.InputStreamReader;


import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ibm.mqlight.api.CompletionListener;
import com.ibm.mqlight.api.NonBlockingClient;
import com.ibm.mqlight.api.NonBlockingClientAdapter;
import com.ibm.mqlight.api.QOS;
import com.ibm.mqlight.api.SendOptions;

@WebServlet(urlPatterns = "/QWriter", loadOnStartup = 1)
public class example_queue_writer extends HttpServlet {

	private static final long serialVersionUID = 4378655080948830937L;
	private NonBlockingClient client;
	private static final String SUBSCRIBE_TOPIC = "mqlight/samplePosts";
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.getWriter().write("hello world");
	}
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		InputStreamReader reader = new InputStreamReader(req.getInputStream());
		BufferedReader buffer = new BufferedReader(reader);
		String line = null;
		SendOptions opts = SendOptions.builder().setQos(QOS.AT_LEAST_ONCE).build();
		while ((line = buffer.readLine()) != null) {
			client.send(SUBSCRIBE_TOPIC,line,null,opts,new CompletionListener<Void>() {
				public void onError(NonBlockingClient client, Void context, Exception exception) {};
				public void onSuccess(NonBlockingClient client, Void context) {};
			},null);
		}
	}
	
	@Override
	public void init() throws ServletException {
		// TODO Auto-generated method stub
		
		client =  NonBlockingClient.create(null, new NonBlockingClientAdapter<Void>() {
			@Override
			public void onStarted(NonBlockingClient client, Void context) { /*do nothing;*/}
		},null);
		
		super.init();
		
	}
	
}

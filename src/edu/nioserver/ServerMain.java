package edu.nioserver;

import java.io.File;

public class ServerMain
{

	/**
	 * 
	 * @param args Port 
	 */
	public static void main(String[] args)
	{
		File configDir = new File(args[0]);
		
		Thread serverThread = new Thread(new Server(Integer.parseInt(args[0]), args[1]));
		serverThread.start();
	}

}

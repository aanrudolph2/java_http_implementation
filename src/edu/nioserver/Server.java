package edu.nioserver;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Server implements Runnable
{
	private final int PORT;
	private final long TIMEOUT = 10000;
	private final String DOCUMENT_ROOT;
	
	private ServerSocketChannel serverChannel;
	private Selector selector;
	
	private Map<SocketChannel, byte[]> dataTracking = new HashMap<SocketChannel, byte[]>();
	
	private Map<String, String> mimetypes = new HashMap<String, String>();
	
	public Server(int port, String documentRoot)
	{
		/* Set up Mimetypes */
		
		mimetypes.put("html", "text/html");
		mimetypes.put("js", "text/javascript");
		mimetypes.put("css", "text/css");
		mimetypes.put("bmp", "image/bmp");
		mimetypes.put("gif", "image/gif");
		mimetypes.put("png", "image/png");
		mimetypes.put("svg", "image/svg+xml");
		mimetypes.put("swf", "application/x-shockwave-flash");
		mimetypes.put("txt", "text/plai");
		
		PORT = port;
		DOCUMENT_ROOT = documentRoot;
		
		try
		{
			selector = Selector.open();
			serverChannel = ServerSocketChannel.open();
			serverChannel.configureBlocking(false);
			serverChannel.socket().bind(new InetSocketAddress(PORT));
			
			serverChannel.register(selector, SelectionKey.OP_ACCEPT);
		}
		catch (IOException e)
		{
			System.out.println("Failed to initialize. Exiting.");
			System.exit(1);
		}
	}
	@Override
	public void run()
	{
		System.out.println("Accepting connections...");
		
		while(!Thread.currentThread().isInterrupted())
		{
			try
			{
				selector.select(TIMEOUT);
				
				Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
				
				while(keys.hasNext())
				{
					SelectionKey key = keys.next();
					
					keys.remove();
					
					if(key.isValid())
					{
						if(key.isAcceptable())
						{
							accept(key);
						}
						else if(key.isWritable())
						{
							write(key);
						}
						else if(key.isReadable())
						{
							read(key);
						}
					}
				}
			}
			catch(IOException e)
			{
				e.printStackTrace();
			}
		}
		try
		{
			selector.close();
			serverChannel.socket().close();
			serverChannel.close();
		}
		catch(IOException e)
		{
			// Do nothing
		}
	}
	
	private void write(SelectionKey key) throws IOException
	{
		SocketChannel channel = (SocketChannel) key.channel();
		
		byte[] data = dataTracking.get(channel);
		if(data != null)
		{
			dataTracking.remove(channel);
			
			channel.write(ByteBuffer.wrap(data));
			
			channel.register(selector, SelectionKey.OP_READ);
		}
	}
	
	private void accept(SelectionKey key) throws IOException
	{
		ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
		SocketChannel socketChannel = serverSocketChannel.accept();
		socketChannel.configureBlocking(false);
		
		socketChannel.register(selector, SelectionKey.OP_READ);
	}
	
	private void read(SelectionKey key) throws IOException
	{
		SocketChannel channel = (SocketChannel) key.channel();
		ByteBuffer readBuffer = ByteBuffer.allocate(2048);
		
		readBuffer.clear();
		int read;
		
		try
		{
			read = channel.read(readBuffer);
		}
		catch (IOException e)
		{
			key.cancel();
			channel.close();
			return;
		}
		
		if(read >= 0)
		{
			readBuffer.flip();
			byte[] data = new byte[read];
			readBuffer.get(data, 0, read);
			
			try
			{
				RequestParser req = new RequestParser(new String(data, "UTF-8"));
				Date curr = new Date();
				System.out.println("[" + curr.toString() + "] Request for " + req.getURI());
				
				File reqFile = new File(DOCUMENT_ROOT + req.getURI());
				if(reqFile.isDirectory())
				{
					reqFile = new File(reqFile.getPath() + "/index.html");
				}
				
				byte[] responseData = null;
				try
				{
					
					String mimetype;
					String ext = reqFile.getName().substring(reqFile.getName().lastIndexOf('.') + 1, reqFile.getName().length());

					byte[] fileContents = new byte[(int) reqFile.length()];
					
					FileInputStream fIS = new FileInputStream(reqFile);
					fIS.read(fileContents);
					fIS.close();
					
					mimetype = mimetypes.get(ext);
					
					if(mimetype == null)
					{
						mimetype = "text/plain";
					}
					
					HTTPResponse res = new HTTPResponse(200, new String(fileContents, "UTF-8"), mimetype);
					responseData = res.toString().getBytes();
					/*
					else if(ext.equals("php"))
					{
						try
						{
							mimetype = "text/html";
							
							Process php_proc = Runtime.getRuntime().exec("php " + reqFile);
							
							int exitVal = php_proc.waitFor();
							BufferedInputStream php_output = new BufferedInputStream(php_proc.getInputStream());
							byte[] fileContents = new byte[php_output.available()];
							
							php_output.read(fileContents);
							
							HTTPResponse res = new HTTPResponse(200, new String(fileContents, "UTF-8"), mimetype);
							responseData = res.toString().getBytes();
						}
						catch (InterruptedException e)
						{
							e.printStackTrace();
						}
					}
					*/
				}
				catch(FileNotFoundException e)
				{
					System.err.println(reqFile.getPath() + " not found");
					HTTPResponse res = new HTTPResponse(404, "File not found", "text/plain");
					responseData = res.toString().getBytes();
				}
				putData(key, responseData);
			}
			catch(InvalidHTTPRequestException e)
			{
				
			}
		}
	}
	
	private void putData(SelectionKey key, byte[] data)
	{
		SocketChannel socketChannel = (SocketChannel) key.channel();
		dataTracking.put(socketChannel, data);
		key.interestOps(SelectionKey.OP_WRITE);
	}
}

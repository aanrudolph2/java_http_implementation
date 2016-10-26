package edu.nioserver;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class HTTPResponse
{
	private String response;
	private String contents;
	
	private Map<String, String> headers = new HashMap<String, String>();
	
	public HTTPResponse(int responseCode, String contents, String mimetype)
	{
		switch (responseCode)
		{
		case 200:
			response = "200 OK";
			break;
		case 401:
			response = "401 Unauthorized";
			break;
		case 403:
			response = "403 Forbidden";
			break;
		case 404:
			response = "404 Not Found";
			break;
		default:
			response = "500 Internal Server Error";
		}
		this.contents = contents;
		
		headers.put("Content-Type", mimetype);
		headers.put("Content-Length", String.valueOf(contents.length()));
	}
	
	public void putHeader(String key, String value)
	{
		headers.put(key, value);
	}
	
	public String toString()
	{
		String output = "HTTP/1.1 " + response + "\r\n";
		
		Iterator<String> it = headers.keySet().iterator();
		
		while(it.hasNext())
		{
			String headerKey = it.next();
			output += headerKey + ": " + headers.get(headerKey) + "\r\n";
		}
		
		return output + "\r\n" + contents + "\r\n";
	}
}

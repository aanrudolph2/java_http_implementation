package edu.nioserver;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RequestParser
{
	private String method;
	private String uri;
	private String http_version;
	
	private Map<String, String> headers = new HashMap<String, String>();
	
	public RequestParser(String input) throws InvalidHTTPRequestException
	{
		int requestEnd = input.indexOf("\r\n");
		
		if(requestEnd == -1)
		{
			throw new InvalidHTTPRequestException();
		}
		
		String request = input.substring(0, requestEnd);
		
		ArrayList<String> reqParts = new ArrayList<String>(3);
		for(String part : request.split(" "))
		{
			reqParts.add(part);
		}
		
		method = reqParts.get(0);
		uri = reqParts.get(1);
		http_version = reqParts.get(2);
		
		for(String keyValue : input.substring(requestEnd).split("\r\n"))
		{
			String[] headerPair = keyValue.split(" *: *", 2);
			
			if(headerPair.length == 2)
			{
				headers.put(headerPair[0], headerPair[1]);
			}
		}
	}
	
	public String getHeader(String headerKey)
	{
		return headers.get(headerKey);
	}
	
	public String getHTTPVersion()
	{
		return http_version;
	}
	
	public String getURI()
	{
		return uri;
	}

	public String getMethod()
	{
		return method;
	}
}

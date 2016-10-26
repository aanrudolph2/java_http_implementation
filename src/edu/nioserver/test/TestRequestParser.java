package edu.nioserver.test;
import static org.junit.Assert.*;

import org.junit.Test;

import edu.nioserver.RequestParser;

public class TestRequestParser {

	@Test
	public void test()
	{
		String request = 
			"GET /hello HTTP/1.1\r\n" +
			"Host: localhost\r\n" +
			"User-Agent: Mozilla/5.0 (Windows NT 10.0; WOW64; rv:41.0) Gecko/20100101 Firefox/41.0\r\n" +
			"Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8\r\n" +
			"Accept-Language: en-US,en;q=0.5\r\n" +
			"Accept-Encoding: gzip, deflate\r\n" +
			"DNT: 1\r\n" +
			"Connection: keep-alive\r\n" +
			"Cache-Control: max-age=0";
		
		RequestParser testObj = new RequestParser(request);
		
		assertEquals(testObj.getHTTPVersion(), "HTTP/1.1");
		assertEquals(testObj.getMethod(), "GET");
		assertEquals(testObj.getURI(), "/hello");
		
		assertEquals(testObj.getHeader("Host"), "localhost");
		assertEquals(testObj.getHeader("User-Agent"), "Mozilla/5.0 (Windows NT 10.0; WOW64; rv:41.0) Gecko/20100101 Firefox/41.0");
		assertEquals(testObj.getHeader("Accept"), "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		assertEquals(testObj.getHeader("Accept-Language"), "en-US,en;q=0.5");
		assertEquals(testObj.getHeader("Accept-Encoding"), "gzip, deflate");
		assertEquals(testObj.getHeader("DNT"), "1");
		assertEquals(testObj.getHeader("Connection"), "keep-alive");
		assertEquals(testObj.getHeader("Cache-Control"), "max-age=0");
	}

}

package esutdal.javanotes.cache.util;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class CacheConfig
{
	public static List<Map<String, String>> read(String xml) throws ParseException, SAXException, IOException
	{
		Document dom = null;
		//try as stream
		try {
			dom = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder().parse(CacheConfig.class.getResourceAsStream(xml));
		} catch (Exception e) {
			try {
				//try as file
				dom = DocumentBuilderFactory.newInstance()
						.newDocumentBuilder().parse(new File(xml));
				
			} catch (Exception e1) {
				//finally try as uri
				try {
					dom = DocumentBuilderFactory.newInstance()
							.newDocumentBuilder().parse(xml);
				} catch (SAXException e2) {
					throw e2;
				} catch (IOException e2) {
					throw e2;
				} catch (Exception e2) {
					throw new SAXException(e1);
				}
			}
		}
		try
		{
			List<Map<String, String>> list = new ArrayList<>();
			Element root = dom.getDocumentElement();
			NodeList groups = root.getElementsByTagName("cache");
			
			for(int i=0; i<groups.getLength(); i++)
			{
				Element cache = (Element) groups.item(i);
				NamedNodeMap map = cache.getAttributes();
				Map<String, String> mapped = new HashMap<>();
				for(int j=0; j<map.getLength(); j++)
				{
					Node n = map.item(j);
					mapped.put(n.getNodeName(), n.getNodeValue());
					
				}
				list.add(mapped);
			}
			
			return list;
			
		}
		catch (Exception e)
		{
			throw new ParseException(e.toString(),0);
		}
		
	}
	
}

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.swing.JFrame;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.xml.sax.SAXException;

import junit.framework.TestCase;


public class ParseTest extends TestCase{

private ConfigurationAnalyzer configTest;
private File f;
	
	public void setUp()
	{
	   try {
       configTest=new ConfigurationAnalyzer();
	   configTest.setTitle("Datapower Configuration Analyzer");
	   configTest.setSize(400,400);
	   configTest.setLocationRelativeTo(null);
	   configTest.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	   configTest.setVisible(true);
	   f=new File("c:\\3.xml");
	   
	   } 
	   catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void testParseFile() throws XPathExpressionException, SAXException, IOException, ParserConfigurationException
	{
		configTest.startParsing(f);
	}
	
	public void testNumPolicies() throws XPathExpressionException, SAXException, IOException, ParserConfigurationException
	{	
		testParseFile();
		assertEquals(configTest.getPolicyCount(),1);		
	}
	
	public void testNumRules() throws XPathExpressionException, SAXException, IOException, ParserConfigurationException
	{	
		testParseFile();
		assertEquals(configTest.getRuleCount(),3);		
	}
	
	public void testNumElements() throws XPathExpressionException, SAXException, IOException, ParserConfigurationException
	{	
		testParseFile();
		assertEquals(configTest.getElementCount(),9);		
	}
	
	public void tearDown()
	{
		configTest.dispose();
	}
	
}

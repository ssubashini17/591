
import org.w3c.dom.*;
import org.xml.sax.SAXException;
import javax.xml.parsers.*;
import javax.xml.xpath.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.TitledBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.filechooser.FileFilter;



public class ConfigurationAnalyzer extends JFrame implements ActionListener {
	
	private static final long serialVersionUID = 1L;
	private Document doc;
	private XPath xpath;
	private HashMap<String,HashMap<String,Object>> Rule;  
	private HashMap<String,Object> Element;
	private HashSet<String> Filter;
	private HashMap<String,Object> Policy; 
	private ArrayList<ImageIcon> elementsAL;
	private ArrayList<String> elementsNameAL;
	private ArrayList<String> elementsTypeAL;
	private JButton parseButton;
	private JButton goButton;
	private JFileChooser fc;
	private JComboBox cb;
    private String ruleDirection;
	private JPanel containerPanel;
	private static HashMap<String,String> mapElementToPng = new HashMap<String,String>();
      
	
	public ConfigurationAnalyzer() throws IOException 
	{
		fc = new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fc.addChoosableFileFilter(new xmlFilter());
		containerPanel = new JPanel();
		JScrollPane jsp = new JScrollPane(containerPanel,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		addParseButton();
		this.setUpMapForImages();
        getContentPane().add(jsp);
        getContentPane().validate();    
	}

	void addParseButton(){

		parseButton = new JButton("Parse a File");
		parseButton.addActionListener(this);
		JPanel buttonPanel = new JPanel(); 
		buttonPanel.add(parseButton);
		containerPanel.add(buttonPanel, BorderLayout.PAGE_START);

	}
	void startParsing() throws XPathExpressionException
	{
		evaluateExpression("/datapower-configuration/configuration//StylePolicyRule[@name]","Rule");
		evaluateExpression("/datapower-configuration/configuration//StylePolicyAction[@name]","Element");
		evaluateExpression("/datapower-configuration/configuration//Matching[@name]","Filter Matching Rule");
		evaluateExpression("/datapower-configuration/configuration//StylePolicy[@name]","Policy");
	}


	//Evaluates XPath expression and returns a NodeList
	void evaluateExpression(String exp,String type) throws XPathExpressionException
	{
		XPathExpression expr = xpath.compile(exp);
		Object result = expr.evaluate(doc, XPathConstants.NODESET);
		NodeList nodes = (NodeList) result;
		parseNodeNames(nodes,type);
	}

	// Parse Elements, their type and direction
	void parseElementDetails(NodeList nodes,String type)
	{
		for (int i = 0; i < nodes.getLength(); i++)
		{
			HashMap<String,String> details=new HashMap<String,String>();
			NodeList children=nodes.item(i).getChildNodes();
			details.put("Type",children.item(3).getTextContent());
			details.put("Direction",children.item(5).getNodeName());
			Element.put(nodes.item(i).getAttributes().item(0).getTextContent(), details);
		}
	}

	//Parse rules and elements applied in a rule
	void parseRuleDetails(NodeList nodes,String type)
	{
		for (int i = 0; i < nodes.getLength(); i++)
		{
			HashMap<String,Object> details=new HashMap<String,Object>();
			NodeList children=nodes.item(i).getChildNodes();
			details.put("Type",children.item(3).getTextContent());
			HashSet<String> details_element=new HashSet<String>();
			for(int j=13;j<children.getLength();j+=2)
				details_element.add(children.item(j).getTextContent());
			details.put("Elements", details_element);
			Rule.put(nodes.item(i).getAttributes().item(0).getTextContent(),details);
		}


	}

	//Parse filters
	void parseFilterDetails(NodeList nodes,String type)
	{
		for (int i = 0; i < nodes.getLength(); i++)
			Filter.add(nodes.item(i).getAttributes().item(0).getTextContent());
	}

	//Parse policy and rules within a policy 

	void parsePolicyDetails(NodeList nodes,String type)
	{
		for (int i = 0; i < nodes.getLength(); i++)
		{
			HashMap<String,Object> details=new HashMap<String,Object>();
			NodeList children=nodes.item(i).getChildNodes();
			HashSet<String> details_rules=new HashSet<String>();

			int j=0;
			while(j<children.getLength())
			{
				Node node=children.item(j);
				if(node.getNodeName().equals("PolicyMaps"))
				{

					details_rules.add(node.getLastChild().getTextContent());

				}
				j++;
			}

			details.put("Rules", details_rules);
			Policy.put(nodes.item(i).getAttributes().getNamedItem("name").getNodeValue(), details);

		}
	}


	void parseNodeNames(NodeList nodes,String type)
	{
		if(type.equals("Element"))
			parseElementDetails(nodes,type);
		else if(type.equals("Rule"))
			parseRuleDetails(nodes,type);
		else if(type.equals("Filter Matching Rule"))
			parseFilterDetails(nodes,type);
		else if(type.equals("Policy"))
			parsePolicyDetails(nodes,type);
	}

	void printRules()
	{
		System.out.println("Rules \n"+Rule.toString());

	}

	void printElements()
	{
		System.out.println("Elements \n"+Element.toString());
	}

	void printFilters()
	{
		System.out.println("Filters Matching Rules\n"+Filter.toString());
	}

	void printPolicies()
	{
		System.out.println("Policies\n"+Policy.toString());

	}
	
	int getPolicyCount()
	{
		return Policy.size();
	}
	
	int getRuleCount()
	{
		return Rule.size();
	}
	
	int getElementCount()
	{
		return Element.size();
	}

	/* Drop down containing all policies */
	public void showPolicyDropdown(int selectedIndex){
	
		if(Policy!=null){
		    
		    Object[] policyNames = Policy.keySet().toArray();
			cb = new JComboBox(policyNames);
			cb.setSelectedIndex(selectedIndex);
			cb.addActionListener(this);
			goButton = new JButton("Go");
			goButton.addActionListener(this);
			
			JPanel buttonPanel = new JPanel(); 
			buttonPanel.add(new JLabel("Policy Name"));
			buttonPanel.add(cb);
			buttonPanel.add(goButton);
			buttonPanel.validate();
			buttonPanel.repaint();
			containerPanel.add(buttonPanel, BorderLayout.PAGE_START);
			containerPanel.validate();
 
	 }
		
	}

	void addToFrame(String policyName)
	{

		HashMap<String, Object> policy = (HashMap<String, Object>) Policy.get(policyName);
		Iterator it= policy.values().iterator();

		while(it.hasNext()){
			HashSet details_rule = (HashSet) it.next();
			Iterator i = details_rule.iterator();

			/*iterates over each Rule */
			while(i.hasNext()){ 
				String RuleName = (String)i.next();
				System.out.println("Rule Name:"+ RuleName);
				HashMap<String,Object> rules = Rule.get(RuleName);
				HashSet<String> elementsSet = (HashSet<String>)rules.get("Elements");
				Object ruleType = Rule.get(RuleName).get("Type");
				ruleDirection = ruleType.toString();
				Iterator<String> i_elements = elementsSet.iterator();
				elementsAL.clear();
				elementsNameAL.clear();
				elementsTypeAL.clear();
				elementsAL.add(new ImageIcon(".//images//server.png"));
				elementsNameAL.add("Server");
				elementsTypeAL.add("Server");
				String direction = " ";
				while(i_elements.hasNext()){
					String keysForElementsMap = (String)i_elements.next();
					System.out.println("Element :"+keysForElementsMap);
					HashMap detailsOfElements = (HashMap)Element.get(keysForElementsMap);
					System.out.println("Type : "+detailsOfElements.get("Type"));
					direction= detailsOfElements.get("Direction").toString();
					System.out.println("Direction :" +detailsOfElements.get("Direction"));
					this.addElementsToList(keysForElementsMap,(String)detailsOfElements.get("Type"),(String)detailsOfElements.get("Direction"));
					System.out.println((String)detailsOfElements.get("Type"));
				}
				elementsAL.add(new ImageIcon(".//images//client.png"));
				elementsNameAL.add("Client");
				elementsTypeAL.add("Client");
				
				this.addRuleToFrame(RuleName,direction);
			}

		}

	}


	public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {

		ConfigurationAnalyzer config=new ConfigurationAnalyzer();
		config.setTitle("Datapower Configuration Analyzer");
		config.setSize(400,400);
		config.setResizable(true);
		config.setLocationRelativeTo(null);
		config.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		config.setVisible(true);
	}

	/* Add rules and the corresponding elements to the frame */
	private void addRuleToFrame(String ruleName,String direction){
		JPanel panelForRule = new JPanel();
		panelForRule.setSize(400, 200);
		TitledBorder titleForPanel = BorderFactory.createTitledBorder("Rule Name: "+ruleName +"      "+"(Type: "+ruleDirection+")");
		titleForPanel.setTitleJustification(TitledBorder.CENTER);
	
		panelForRule.setBorder(titleForPanel);
		
		for(int i=0;i<=elementsAL.size()-1;i++){
			JPanel outerPanel = new JPanel();
			outerPanel.setLayout(new BoxLayout(outerPanel, BoxLayout.Y_AXIS));
			outerPanel.setAlignmentX(CENTER_ALIGNMENT);
			outerPanel.setToolTipText(elementsNameAL.get(i));
			JLabel element = new JLabel(elementsAL.get(i));
			outerPanel.add(element);
			String imageName = ".//images//"+ruleDirection.toLowerCase()+".png";
			JLabel arrow = new JLabel(new ImageIcon(imageName));
			JLabel elementType = new JLabel(elementsTypeAL.get(i)) ;
			outerPanel.add(elementType);
			outerPanel.validate();
			panelForRule.add(outerPanel);
			if(i!=elementsAL.size()-1)
				panelForRule.add(arrow);
			panelForRule.validate();
		}		
		containerPanel.add(panelForRule);
		containerPanel.validate();
		
	}


    /*Map 'type' to one of the 11 standard elements */
	public void setUpMapForImages() throws IOException{
		
		mapElementToPng.put("encrypt", ".//images//encrypt.png");
		mapElementToPng.put("decrypt", ".//images//decrypt.png");
		
		mapElementToPng.put("aaa", ".//images//aaa.png");
		mapElementToPng.put("validate", ".//images//validate.png");
		
		
		mapElementToPng.put("filter", ".//images//filter.png");
		mapElementToPng.put("advanced", ".//images//advanced.png");
		mapElementToPng.put("antivirus", ".//images//antivirus.png");
		mapElementToPng.put("call", ".//images//call.png");
		mapElementToPng.put("conditional", ".//images//conditional.png");
		mapElementToPng.put("convert-http", ".//images//convert-http.png");
		mapElementToPng.put("cryptobin", ".//images//cryptobin.png");
		mapElementToPng.put("extract", ".//images//extract.png");	/*no icon*/
		mapElementToPng.put("eventsink", ".//images//eventsink.png"); /*  */
		mapElementToPng.put("fetch", ".//images//fetch.png");
		mapElementToPng.put("for-each", ".//images//foreach.png");
		mapElementToPng.put("log", ".//images//log.png");
		mapElementToPng.put("mq", ".//images//mq.png");
		mapElementToPng.put("on-error", ".//images//on-error.png");
		mapElementToPng.put("results-async", ".//images//results-async.png");
		mapElementToPng.put("results", ".//images//results.png");
		mapElementToPng.put("rewrite", ".//images//rewrite.png");
		mapElementToPng.put("route-action", ".//images//route.png");// both are mapped to route
		mapElementToPng.put("route-set", ".//images//route.png");//
		mapElementToPng.put("setvar", ".//images//setvar.png");
		mapElementToPng.put("slm", ".//images//slm.png");
		mapElementToPng.put("sql", ".//images//sql.png");
		mapElementToPng.put("strip-attachments", ".//images//strip-attachments.png");
		
	     mapElementToPng.put("xform", ".//images//advanced.png");	
		mapElementToPng.put("xformbin", ".//images//xformbin.png");
		mapElementToPng.put("xformpi", ".//images//xformpi.png");
		 
		
		   
	}

	/* Adds the elements as images/icons into an ArrayList  */	
	private void addElementsToList(String elementName, String type, String Direction){
		
		System.out.println("type= "+type);
		elementsAL.add(new ImageIcon(mapElementToPng.get(type).toString()));
		elementsNameAL.add(elementName);
		elementsTypeAL.add(type);
       // ruleDirection = Direction;

	}
	
	public void startParsing(File f) {
		DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
		domFactory.setNamespaceAware(true); 
		DocumentBuilder builder;
		try {
			builder = domFactory.newDocumentBuilder();
			doc = builder.parse(f.getAbsolutePath());
			XPathFactory factory = XPathFactory.newInstance();
			xpath = factory.newXPath();

			// Instantiating variables
			Rule=new HashMap<String,HashMap<String,Object>>();
			Policy= new HashMap<String,Object>();
			Element=new HashMap<String,Object>();
			Filter=new HashSet<String>();


			elementsAL =  new ArrayList<ImageIcon>();
			elementsNameAL = new ArrayList<String>();
	        elementsTypeAL = new ArrayList<String>();
	        startParsing();
		}catch (ParserConfigurationException e) {
			showErrorMessage("ParserConfigException");
		}catch (SAXException e) {
			 showErrorMessage("SAXException");
		} catch (IOException e) {
			showErrorMessage("IOException");
		}catch (XPathExpressionException e) {
			 showErrorMessage("XPathExpressionException");
		}
		
		containerPanel.setLayout(new BoxLayout(containerPanel, BoxLayout.Y_AXIS));
	}
    
	/* Display dialog for error messages */
	private void showErrorMessage(String errorString) {
		JOptionPane.showMessageDialog(null, errorString);
		 this.dispose();
		 try {
			ConfigurationAnalyzer.main(null);	
			
		} catch (XPathExpressionException e1) {
			
		} catch (ParserConfigurationException e1) {
			
		} catch (SAXException e1) {
			
		} catch (IOException e1) {
			
		}	
	}

	/*Handle onClick of parse and go buttons */
    public void actionPerformed(ActionEvent e) {

		if (e.getSource() == parseButton) {

			int returnVal = fc.showOpenDialog(ConfigurationAnalyzer.this);

			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = fc.getSelectedFile();
				
				containerPanel.removeAll();
				startParsing(file);
				addParseButton();
				showPolicyDropdown(0);
				
			} 
		}else if (e.getSource()== goButton){
			String selected = cb.getSelectedItem().toString();
			int selectedIndex = cb.getSelectedIndex();
			containerPanel.removeAll();
			addParseButton();
			showPolicyDropdown(selectedIndex);
			addToFrame(selected);
			getContentPane().validate();

		}
	}

}

/*Show only XML files in the file chooser  */
class xmlFilter extends FileFilter {
    public boolean accept(File file) {
        String filename = file.getName();
        return filename.endsWith(".xml");
    }
    public String getDescription() {
        return "*.xml";
    }
}
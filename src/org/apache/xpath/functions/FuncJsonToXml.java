/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the  "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * $Id$
 */
package org.apache.xpath.functions;

import java.util.Iterator;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.SourceLocator;
import javax.xml.transform.dom.DOMSource;

import org.apache.xalan.xslt.util.XslTransformEvaluationHelper;
import org.apache.xml.dtm.DTM;
import org.apache.xml.dtm.DTMManager;
import org.apache.xpath.Expression;
import org.apache.xpath.XPathContext;
import org.apache.xpath.compiler.FunctionTable;
import org.apache.xpath.objects.XNodeSet;
import org.apache.xpath.objects.XObject;
import org.apache.xpath.objects.XPathMap;
import org.apache.xpath.operations.Variable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

/**
 * Implementation of XPath 3.1 function, json-to-xml().
 * 
 * @author Mukul Gandhi <mukulg@apache.org>
 * 
 * @xsl.usage advanced
 */
public class FuncJsonToXml extends FunctionMultiArgs
{
	
	private static final long serialVersionUID = 945183900907386647L;

    /**
     * Implementation of the function. The function must return a valid object.
     * 
     * @param xctxt The current execution context.
     * @return A valid XObject.
     *
     * @throws javax.xml.transform.TransformerException
     */
    public XObject execute(XPathContext xctxt) throws javax.xml.transform.TransformerException
    {
    	XObject result = null;
        
        SourceLocator srcLocator = xctxt.getSAXLocator();
        
        Expression arg0 = m_arg0;        
        Expression arg1 = m_arg1;
        
        if ((arg0 == null) && (arg1 == null)) {
           throw new javax.xml.transform.TransformerException("FOAP0001 : An XPath function fn:json-to-xml needs to have "
           		                                                      + "at-least one argument.", srcLocator);
        }
        else if (m_arg2 != null) {
           throw new javax.xml.transform.TransformerException("FOAP0001 : An XPath function fn:json-to-xml can "
           		                                                      + "have either 1 or two arguments.", srcLocator);
        }
        
        String jsonStr = null;
        
        if (arg0 instanceof Variable) {
           XObject arg0Obj = ((Variable)arg0).execute(xctxt);
           jsonStr = XslTransformEvaluationHelper.getStrVal(arg0Obj); 
        }
        else {
           XObject arg0Obj = arg0.execute(xctxt);
           jsonStr = XslTransformEvaluationHelper.getStrVal(arg0Obj);
        }
        
        // REVISIT
        // To try implementing fn:json-to-xml function's 2nd argument 
        XPathMap optionsMap = null;        
        if (arg1 != null) {
           XObject arg1Obj = null;
           if (arg1 instanceof Variable) {
        	  arg1Obj = ((Variable)arg1).execute(xctxt);               
           }
           else {
        	  arg1Obj = arg1.execute(xctxt);                
           }
           
           if (!(arg1Obj instanceof XPathMap)) {
        	  throw new javax.xml.transform.TransformerException("FOAP0001 : An XPath function fn:json-to-xml's optional 2nd "
        	  		                                                     + "argument should be a map, that specifies options for "
        	  		                                                     + "the function call fn:json-to-xml.", srcLocator); 
           }
           else {
        	  optionsMap = (XPathMap)arg1Obj;    
           }
        }
        
        Object jsonObj = null;
        try {
           if (jsonStr.charAt(0) == '{') {
        	  jsonObj = new JSONObject(jsonStr);
           }
           else if (jsonStr.charAt(0) == '[') {
        	  jsonObj = new JSONArray(jsonStr); 
           }
           else {
        	  throw new javax.xml.transform.TransformerException("FOJS0001 : The string value provided in XPath "
                                                              + "function call fn:json-to-xml's 1st argument is, not a "
                                                              + "valid JSON string. A JSON string can only start with "
                                                              + "characters '{', or '['.", srcLocator); 
           }
        }
        catch (JSONException ex) {
           String jsonParseErrStr = ex.getMessage();
           throw new javax.xml.transform.TransformerException("FOJS0001 : The string value provided in XPath "
           		                                                      + "function call fn:json-to-xml's 1st argument is, not "
           		                                                      + "a valid JSON string. The JSON parser produced an "
           		                                                      + "error: " + jsonParseErrStr + ".", srcLocator);
        }
        
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        
        DocumentBuilder dBuilder = null;		
        try {
		   dBuilder = dbf.newDocumentBuilder();
		} catch (ParserConfigurationException ex) {
		   throw new javax.xml.transform.TransformerException("FOJS0001 : An error occured, within an XML parser "
		   		                                                      + "library.", srcLocator);
		}
		
        Document document = dBuilder.newDocument();
        
        constructXmlDom(jsonObj, document, document, null);
        
        Element docElem = document.getDocumentElement();
        docElem.setAttribute("xmlns", FunctionTable.XPATH_BUILT_IN_FUNCS_NS_URI);
        
        DTMManager dtmMgr = xctxt.getDTMManager();
         
        DTM dtm = dtmMgr.getDTM(new DOMSource(document), true, null, false, false);
        
        result = new XNodeSet(dtm.getDocument(), dtmMgr);
        
        return result;
    }
    
    /**
     * A method to construct an XML DOM object, from an input JSON object.
     * 
     * @param jsonObj        An object that is either of type JSONObject, or JSONArray 
     * @param document       An empty XML DOM object, that needs to be built 
     *                       to a fully populated DOM document node within this method 
     *                       implementation.
     * @parentNode           This method supports to construct an XML DOM object, via
     *                       recursive calls to this method.                     
     * @return               void
     */
    private void constructXmlDom(Object jsonObj, Document document, Node parentNode, String keyVal) {
    	
    	if (jsonObj instanceof JSONObject) {
    		Element mapElem = document.createElement("map");
    		parentNode.appendChild(mapElem);
    		
    		if (keyVal != null) {
    		   mapElem.setAttribute("key", keyVal);	
    		}
    		
	    	Iterator<String> jsonKeys = ((JSONObject)jsonObj).keys();	    	
	    	while (jsonKeys.hasNext()) {
	      	   String key = jsonKeys.next();
	      	   Object value = ((JSONObject)jsonObj).get(key);        	  
	      	   if (value instanceof String) {
	      		 Element strElem = document.createElement("string");
	      		 strElem.setAttribute("key", key);
	      		 Text text = document.createTextNode((String)value);
	      		 strElem.appendChild(text);
	      		 mapElem.appendChild(strElem);
	      	   }
	      	   else if (value instanceof Number) {
	      		 Element numberElem = document.createElement("number");
	      		 numberElem.setAttribute("key", key);
	      		 Text text = document.createTextNode(value.toString());
	      		 numberElem.appendChild(text);
	      		 mapElem.appendChild(numberElem);
	      	   }
	      	   else if (value instanceof Boolean) {
	      		 Element boolElem = document.createElement("boolean");
	      		 boolElem.setAttribute("key", key);
	      		 Text text = document.createTextNode(value.toString());
	      		 boolElem.appendChild(text);
	      		 mapElem.appendChild(boolElem);
	      	   }	      	   
	      	   else if (value instanceof JSONArray) {
	      		  Element arrayElem = document.createElement("array");
	      		  arrayElem.setAttribute("key", key);
	      		  mapElem.appendChild(arrayElem);
	      		  JSONArray jsonArr = (JSONArray)value;
	      		  int arrLen = jsonArr.length();
	    		  for (int idx = 0; idx < arrLen; idx++) {
	    			  Object arrItem = jsonArr.get(idx);
	    			  if (arrItem instanceof String) {
	    				  Element strElem = document.createElement("string");
	    				  Text text = document.createTextNode((String)arrItem);
	    				  strElem.appendChild(text);
	    				  arrayElem.appendChild(strElem);	 
	    			  }
	    			  else if (arrItem instanceof Number) {
	    				  Element numberElem = document.createElement("number");
	    				  Text text = document.createTextNode(arrItem.toString());
	    				  numberElem.appendChild(text);
	    				  arrayElem.appendChild(numberElem); 
	    			  }
	    			  else if (arrItem instanceof Boolean) {
	    				  Element boolElem = document.createElement("boolean");
	    				  Text text = document.createTextNode(arrItem.toString());
	    				  boolElem.appendChild(text);
	    				  arrayElem.appendChild(boolElem);  
	    			  }
	    			  else if (arrItem instanceof JSONObject) {
	    				  constructXmlDom(arrItem, document, arrayElem, null);
	    			  }
	    			  else if (arrItem instanceof JSONArray) {
	    				  constructXmlDom(arrItem, document, arrayElem, null);
	    			  }
	    		  }
	      	   }
	      	   else if (value instanceof JSONObject) {
	      		  constructXmlDom(value, document, mapElem, key); 
	      	   }
	      	   else if (JSONObject.NULL.equals(value)) {
	      		  Element nullElem = document.createElement("null");
	      		  nullElem.setAttribute("key", key);
	      		  mapElem.appendChild(nullElem);
	      	   }
	        }	    		    	
    	}
    	else if (jsonObj instanceof JSONArray) {
    		Element arrayElem = document.createElement("array");
     		parentNode.appendChild(arrayElem);
     		
     		if (keyVal != null) {
     		   arrayElem.setAttribute("key", keyVal);	
     		}
    		
    		JSONArray jsonArr = (JSONArray)jsonObj;	      		  
    		int arrLen = jsonArr.length();
    		for (int idx = 0; idx < arrLen; idx++) {
    		   Object arrItem = jsonArr.get(idx);
    		   if (arrItem instanceof String) {
    			  Element strElem = document.createElement("string");
  	      		  Text text = document.createTextNode((String)arrItem);
  	      		  strElem.appendChild(text);
  	      		  arrayElem.appendChild(strElem);	 
    		   }
    		   else if (arrItem instanceof Number) {
    			  Element numberElem = document.createElement("number");
   	      		  Text text = document.createTextNode(arrItem.toString());
   	      		  numberElem.appendChild(text);
   	      		  arrayElem.appendChild(numberElem); 
    		   }
    		   else if (arrItem instanceof Boolean) {
    			  Element boolElem = document.createElement("boolean");
    	          Text text = document.createTextNode(arrItem.toString());
    	          boolElem.appendChild(text);
    	          arrayElem.appendChild(boolElem);  
    		   }
    		   else if (arrItem instanceof JSONObject) {
      		      constructXmlDom(arrItem, document, arrayElem, null);
      		   }
    		   else if (arrItem instanceof JSONArray) {
    		      constructXmlDom(arrItem, document, arrayElem, null);
    		   }    		   
    		}
    	}    	
    }
    
}

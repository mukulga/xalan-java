/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.xpath.functions.map;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.xml.transform.SourceLocator;

import org.apache.xpath.Expression;
import org.apache.xpath.XPathContext;
import org.apache.xpath.functions.FunctionOneArg;
import org.apache.xpath.objects.ResultSequence;
import org.apache.xpath.objects.XObject;
import org.apache.xpath.objects.XPathMap;
import org.apache.xpath.operations.Variable;

/**
 * Implementation of an map:keys function.
 * 
 * @author Mukul Gandhi <mukulg@apache.org>
 * 
 * @xsl.usage advanced
 */
public class FuncMapKeys extends FunctionOneArg {
	
	private static final long serialVersionUID = -7109316358636297324L;

	public XObject execute(XPathContext xctxt) throws javax.xml.transform.TransformerException
	{
		ResultSequence resultSeq = new ResultSequence();
	       
	    SourceLocator srcLocator = xctxt.getSAXLocator();
	       
	    Expression arg0 = getArg0();
	    
	    if (arg0 instanceof Variable) {
	       XObject xObject = ((Variable)arg0).execute(xctxt);
	       XPathMap xpathMap = (XPathMap)xObject;
	       Map<XObject, XObject> nativeMap = xpathMap.getNativeMap();
	       Set<XObject> keySet = nativeMap.keySet();
	       Iterator<XObject> iter = keySet.iterator();
	       while (iter.hasNext()) {
	    	  XObject keyVal = iter.next();
	    	  resultSeq.add(keyVal);
	       }
	    }
	    else {
	    	XObject xObject = arg0.execute(xctxt);
		    XPathMap xpathMap = (XPathMap)xObject;
		    Map<XObject, XObject> nativeMap = xpathMap.getNativeMap();
		    Set<XObject> keySet = nativeMap.keySet();
		    Iterator<XObject> iter = keySet.iterator();
		    while (iter.hasNext()) {
		       XObject keyVal = iter.next();
		       resultSeq.add(keyVal);  
		    }
	    }
	    
	    return resultSeq;
	}

}

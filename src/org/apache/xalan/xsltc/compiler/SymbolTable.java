/*
 * @(#)$Id$
 *
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Xalan" and "Apache Software Foundation" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation and was
 * originally based on software copyright (c) 2001, Sun
 * Microsystems., http://www.sun.com.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * @author Jacek Ambroziak
 * @author Santiago Pericas-Geertsen
 *
 */

package org.apache.xalan.xsltc.compiler;

import java.util.Vector;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.StringTokenizer;

import org.apache.xalan.xsltc.compiler.util.*;

final class SymbolTable {
    private final Hashtable _templates   = new Hashtable();
    private final Hashtable _stylesheets = new Hashtable();
    private final Hashtable _primops     = new Hashtable();
    private final Hashtable _variables   = new Hashtable();
    private final Hashtable _atsets      = new Hashtable();
    private final Hashtable _namespaces  = new Hashtable();
    private final Hashtable _prefixes    = new Hashtable();
    private final Hashtable _aliases     = new Hashtable();
    private final Hashtable _excludedURI = new Hashtable();

    private int nsCounter = 0;

    public Stylesheet addStylesheet(QName name, Stylesheet node) {
	return (Stylesheet)_stylesheets.put(name, node);
    }
	
    public Stylesheet lookupStylesheet(QName name) {
	return (Stylesheet)_stylesheets.get(name);
    }

    public Template addTemplate(Template template) {
	final QName name = template.getName();
	name.clearDefaultNamespace();
	return (Template)_templates.put(name, template);
    }
	
    public Template lookupTemplate(QName name) {
	name.clearDefaultNamespace();
	return (Template)_templates.get(name);
    }

    public Variable addVariable(Variable variable) {
	final String name = variable.getName().getStringRep();
	return (Variable)_variables.put(name, variable);
    }
	
    public Param addParam(Param parameter) {
	final String name = parameter.getName().getStringRep();
	return (Param)_variables.put(name, parameter);
    }
	
    public Variable lookupVariable(QName qname) {
	final String name = qname.getStringRep();
	final Object obj = _variables.get(name);
	return obj instanceof Variable ? (Variable)obj : null;
    }

    public Param lookupParam(QName qname) {
	final String name = qname.getStringRep();
	final Object obj = _variables.get(name);
	return obj instanceof Param ? (Param)obj : null;
    }
	
    public SyntaxTreeNode lookupName(QName qname) {
	final String name = qname.getStringRep();
	return (SyntaxTreeNode)_variables.get(name);
    }

    public AttributeSet addAttributeSet(AttributeSet atts) {
	return (AttributeSet)_atsets.put(atts.getName(), atts);
    }

    public AttributeSet lookupAttributeSet(QName name) {
	return (AttributeSet)_atsets.get(name);
    }

    /**
     * Add a primitive operator or function to the symbol table. To avoid
     * name clashes with user-defined names, the prefix <tt>PrimopPrefix</tt>
     * is prepended.
     */
    public void addPrimop(String name, MethodType mtype) {
	Vector methods = (Vector)_primops.get(name);
	if (methods == null) {
	    _primops.put(name, methods = new Vector());
	}
	methods.addElement(mtype);
    }
	
    /**
     * Lookup a primitive operator or function in the symbol table by
     * prepending the prefix <tt>PrimopPrefix</tt>.
     */
    public Vector lookupPrimop(String name) {
	return (Vector)_primops.get(name);
    }

    /**
     * This is used for xsl:attribute elements that have a "namespace"
     * attribute that is currently not defined using xmlns:
     */
    public String generateNamespacePrefix() {
	final String prefix = new String("ns"+(nsCounter++));
	return(prefix);
    }

    /**
     * Use a namespace prefix to lookup a namespace URI
     */
    private SyntaxTreeNode _current = null;
    public void setCurrentNode(SyntaxTreeNode node) {
	_current = node;
    }

    public String lookupNamespace(String prefix) {
	if (_current != null)
	    return(_current.lookupNamespace(prefix));
	else
	    return(Constants.EMPTYSTRING);
    }

    /**
     * Adds an alias for a namespace prefix
     */ 
    public void addPrefixAlias(String prefix, String alias) {
	_aliases.put(prefix,alias);
    }

    /**
     * Retrieves any alias for a given namespace prefix
     */ 
    public String lookupPrefixAlias(String prefix) {
	return (String)_aliases.get(prefix);
    }

    /**
     *
     */
    public void excludeURI(String uri) {
	if (uri == null) return;
	Integer refcnt = (Integer)_excludedURI.get(uri);
	if (refcnt == null)
	    refcnt = new Integer(1);
	else
	    refcnt = new Integer(refcnt.intValue() + 1);
	_excludedURI.put(uri,refcnt);
    }

    /**
     *
     */
    public void excludeNamespaces(String prefixes) {
	if (prefixes != null) {
	    StringTokenizer tokens = new StringTokenizer(prefixes);
	    while (tokens.hasMoreTokens()) {
		final String prefix = tokens.nextToken();
		final String uri;
		if (prefix.equals("#default"))
		    uri = lookupNamespace(Constants.EMPTYSTRING);
		else
		    uri = lookupNamespace(prefix);
		if (uri != null) excludeURI(uri);
	    }
	}
    }

    /**
     *
     */
    public boolean isExcludedNamespace(String uri) {
	if (uri == null) return false;
	final Integer refcnt = (Integer)_excludedURI.get(uri);
	if (refcnt == null) return false;
	if (refcnt.intValue() > 0) return true;
	return false;
    }

    /**
     *
     */
    public void unExcludeNamespaces(String prefixes) {
	if (prefixes != null) {
	    StringTokenizer tokens = new StringTokenizer(prefixes);
	    while (tokens.hasMoreTokens()) {
		final String prefix = tokens.nextToken();
		final String uri;
		if (prefix.equals("#default"))
		    uri = lookupNamespace(Constants.EMPTYSTRING);
		else
		    uri = lookupNamespace(prefix);
		Integer refcnt = (Integer)_excludedURI.get(uri);
		if (refcnt != null)
		    _excludedURI.put(uri, new Integer(refcnt.intValue() - 1));
	    }
	}	
    }
}


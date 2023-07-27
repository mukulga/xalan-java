<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:math="http://www.w3.org/2005/xpath-functions/math"
                exclude-result-prefixes="math"
                version="3.0">
                
   <!-- Author: mukulg@apache.org -->               
   
   <!-- An XSLT stylesheet test, to test the evaluation of an 
        XPath 3.1 "for" expression. -->                 

   <xsl:output method="xml" indent="yes"/>
   
   <xsl:template match="/">
      <result>
         <xsl:for-each select="(2, 3, 4)">
           <xsl:variable name="num" select="."/>
           <xsl:variable name="result1" select="for $a in (1, 2, 3, 4, 5), $b in $num return 
                                                                     'math:pow(' || $a || ', ' || $b || ')' || ' = ' || math:pow($a, $b)"/>
           <detail inp="math:pow(a, {$num})"><xsl:value-of select="string-join($result1, ' , ')"/></detail>
         </xsl:for-each>
      </result>
   </xsl:template>
   
   <!--
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
   -->

</xsl:stylesheet>
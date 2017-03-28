/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.taglib.functions;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang.BooleanUtils;

/**
 * Functions used within taglibs in a webapp jsp page. <br>
 * <br>
 * Example:
 *
 * <pre>
 * &lt;c:forEach items="${openmrs:sort(someListObject)}" var="o" end="0"&gt;
 *   ....
 *   ....
 * &lt;/c:forEach&gt;
 * </pre>
 */
public class Util {
	
	/**
	 * This method will make untrusted strings safe for use as JavaScript strings
	 *
	 * @param s
	 * @return a JS-escaped version of s
	 */
	public static String getSafeJsString(String s) {
		return StringEscapeUtils.escapeJavaScript(s);
	}
	
	/**
	 * This method will make untrusted strings safe for use as JavaScript booleans
	 *
	 * @param s
	 * @return string representation of a boolean (guaranteed safe for use in JS)
	 */
	public static String getSafeJsBoolean(String s){
		return Boolean.toString(BooleanUtils.toBooleanObject(s));
	}
	
	/**
	 * This method will make untrusted strings safe for use as JavaScript integers
	 *
	 * @param s
	 * @return either the original value (if an int) or 'null'
	 */
	public static String getSafeJsInt(String s){
		if(NumberUtils.isNumber(s)){
			return s;
		}else{
			return "null";
		}
	}
	
}

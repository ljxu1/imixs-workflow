package org.imixs.workflow.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.List;
import java.util.logging.Logger;

import org.imixs.workflow.ItemCollection;

public class JSONParser {

	private static Logger logger = Logger.getLogger(JSONParser.class.getName());

	/**
	 * This method parses a json input stream
	 * 
	 * Example: <code>
	 *  {
		"item":[
				{"name":"$isauthor","value":{"@type":"xs:boolean","$":"true"}},
				{"name":"$readaccess","value":{"@type":"xs:string","$":"Anna"}},
				{"name":"txtmessage","value":{"@type":"xs:string","$":"worklist"}},
				{"name":"txtlog","value":[
					{"@type":"xs:string","$":"A"},
					{"@type":"xs:string","$":"B"},
					{"@type":"xs:string","$":"C"}]
				},
				{"name":"$activityid","value":{"@type":"xs:int","$":"0"}}
			]
		}
	 * </code>
	 * 
	 * @param requestBodyStream
	 * @param encoding
	 *            - default encoding use to parse the stream
	 * @return a workitem
	 * @throws ParseException
	 * @throws UnsupportedEncodingException
	 */
	public final static ItemCollection parseWorkitem(InputStream requestBodyStream, String encoding)
			throws ParseException, UnsupportedEncodingException {

		if (requestBodyStream == null) {
			logger.severe("parseWorkitem - inputStream is null!");
			throw new java.text.ParseException("inputStream is null", -1);
		}

		// default encoding?
		if (encoding==null || encoding.isEmpty()) {
			logger.fine("parseWorkitem - switch to default encoding 'UTF-8'");
			encoding="UTF-8";
		}
		
		// Vector<String> vMultiValueFieldNames = new Vector<String>();
		BufferedReader in = new BufferedReader(new InputStreamReader(requestBodyStream, encoding));

		String inputLine;
		ItemCollection workitem = new ItemCollection();

		String content = null;
		String token = null;
		String name = null;
		StringBuffer stringBuffer = new StringBuffer();
		int iPos = -1;
		int iStart = -1;
		int iEnd = -1;
		try {
			// first we concat all lines
			while ((inputLine = in.readLine()) != null) {
				stringBuffer.append(inputLine);
				logger.finest("parseWorkitem - read line:" + inputLine + "");
			}
			content = stringBuffer.toString();

			// find start ...."item":[...
			content = content.substring(content.indexOf('[') + 0);
			logger.finest("parseWorkitem - start parsing...");
			while (content != null) {

				// find name => "name" : "$isauthor" ,
				iPos = content.indexOf(':');
				content = content.substring(iPos);

				token = content.substring(0, content.indexOf(','));
				iStart = token.indexOf('"') + 1;
				iEnd = token.lastIndexOf('"');
				if (iEnd < iStart)
					throw new java.text.ParseException("Unexpected position of '}", iEnd);

				name = token.substring(iStart, iEnd);

				content = content.substring(token.length());
				if (!isValueArray(content)) {
					// now find the value token =>
					// "value":{"@type":"xs:boolean","$":"true"}},
					iStart = findNextChar(content, '{') + 1;
					iEnd = findNextChar(content, '}');
					if (iEnd < iStart)
						throw new java.text.ParseException("Unexpected position of '}", iEnd);
					token = content.substring(iStart, iEnd);
					content = content.substring(iEnd + 1);
					storeValue(name, token, workitem);
				} else {
					// get content of array
					iStart = findNextChar(content, '[') + 1;
					iEnd = findNextChar(content, ']');
					if (iEnd < iStart)
						throw new java.text.ParseException("Unexpected position of '}", iEnd);

					String arrayContent = content.substring(iStart, iEnd);
					content = content.substring(iEnd + 1);
					// parse array values....
					while (arrayContent != null) {
						// now find the value token =>
						// "value":{"@type":"xs:boolean","$":"true"}},
						iStart = findNextChar(arrayContent, '{') + 1;
						iEnd = findNextChar(arrayContent, '}');
						if (iEnd < iStart)
							throw new java.text.ParseException("Unexpected position of '}", iEnd);

						token = arrayContent.substring(iStart, iEnd);
						arrayContent = arrayContent.substring(iEnd + 1);
						storeValue(name, token, workitem);

						if (!arrayContent.contains("{"))
							break;
					}

				}

				if (!content.contains("{"))
					break;

			}
		} catch (IOException e1) {
			// logger.severe("Unable to parse workitem data!");
			e1.printStackTrace();
			return null;
		} finally {
			try {
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

		return workitem;
	}

	/**
	 * This helper method extracts the type and value of a token and stores the
	 * value into the workitem
	 * 
	 * e.g.
	 * 
	 * {"name":"$isauthor","value":{"@type":"xs:boolean","$":true}},
	 * {"name":"$readaccess","value":{"@type":"xs:string","$":"Anna"}},
	 * {"name":"txtmessage","value":{"@type":"xs:string","$":"worklist"}},
	 * {"name":"$activityid","value":{"@type":"xs:int","$":10}},
	 * {"name":"$processid","value":{"@type":"xs:int","$":100}}
	 * 
	 * @param token
	 * @throws ParseException
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	static void storeValue(String name, String token, ItemCollection workitem) throws ParseException {
		int iPos, iStart, iEnd;
		Object value;
		String type = null;

		// check if "@type" exists
		iPos = token.indexOf("\"@type\"");
		if (iPos > -1) {
			iStart = token.indexOf('"', iPos + "\"@type\"".length() + 1) + 1;
			iEnd = token.indexOf('"', iStart);
			if (iEnd < iStart)
				throw new java.text.ParseException("Unexpected position of '}", iEnd);

			type = token.substring(iStart, iEnd);
			token = token.substring(iEnd + 1);
		}

		// store value - the value can be surrounded by " or not
		iPos = token.indexOf(":") + 1;
		if (token.indexOf('"', iPos) > -1) {
			iStart = token.indexOf('"', iPos) + 1;
			iEnd = token.indexOf('"', iStart);
		} else {
			iStart = iPos;
			iEnd = token.length();
		}
		if (iEnd < iStart)
			throw new java.text.ParseException("Unexpected position of '}", iEnd);

		String stringValue = token.substring(iStart, iEnd);
		value = stringValue;

		// convert value to Object Type
		if ("xs:boolean".equalsIgnoreCase(type)) {
			value = Boolean.getBoolean(stringValue);
			logger.fine("[JSONParser] storeValue - datatype=xs:boolean");
		}
		if ("xs:integer".equalsIgnoreCase(type)) {
			value = Integer.getInteger(stringValue);
			logger.fine("[JSONParser] storeValue - datatype=xs:integer");
		}
		if ("xs:long".equalsIgnoreCase(type)) {
			value = Long.getLong(stringValue);
			logger.fine("[JSONParser] storeValue - datatype=xs:long");
		}
		if ("xs:float".equalsIgnoreCase(type)) {
			value = new Float(stringValue);
			logger.fine("[JSONParser] storeValue - datatype=xs:float");
		}
		if ("xs:double".equalsIgnoreCase(type)) {
			value = new Double(stringValue);
			logger.fine("[JSONParser] storeValue - datatype=xs:double");
		}

		// store value
		if (!workitem.hasItem(name)) {
			// frist value
			workitem.replaceItemValue(name, value);
			logger.fine("[JSONParser] storeValue: '" + name + "' = '" + value + "'");
		} else {
			// add value
			List valueList = workitem.getItemValue(name);
			valueList.add(value);
			workitem.replaceItemValue(name, valueList);
			logger.fine("[JSONParser] store multivalue: '" + name + "' = '" + value + "'");
		}

	}

	/**
	 * Checks if the value is an array of values
	 * 
	 * ,"value":[ {"@type":"xs:string","$":"A"},
	 * 
	 * @param token
	 * @return
	 */
	static boolean isValueArray(String token) {
		int b1 = findNextChar(token, '[');
		int b2 = findNextChar(token, '{');
		if (b1 > -1 && b1 < b2)
			return true;
		else
			return false;
	}

	/**
	 * This method finds the next position of a char. The method scips excapte
	 * characters like '\"' or '\['
	 * 
	 * @param token
	 * @param c
	 * @return
	 */
	static int findNextChar(String token, char c) {
		int iPos = token.indexOf(c);

		if (iPos <= 0)
			return iPos;

		// check if the char before is a \
		while ((token.charAt(iPos - 1)) == '\\') {
			iPos = token.indexOf(c, iPos + 2);
			if (iPos == -1)
				break;
		}

		return iPos;

	}

}

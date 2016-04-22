/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.ssdev.WettkampfManager;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Properties;

public class Settings {
	protected static boolean debug = false;	
	protected static HashMap<String,String> fileProperties = new HashMap<String, String>();
	
	public static void loadConfigFileToSystemProperties(String configFile) {
		Properties props = IO.readPropertiesConfiguration(configFile);
		
		/*
		 * We can't just use System.setProperties(props) if we want
		 * additional command line properties to live aside the properties
		 * from our configuration file. Instead we first store all properties
		 * from all files in our own HashMap and when all configuration files
		 * are loaded, we merge the HashMap into our system properties without
		 * overwriting existing values (which come from command line).
		 */
		Enumeration<?> propKeyEnum = props.propertyNames();
		while (propKeyEnum.hasMoreElements()) {
			String propKey = (String) propKeyEnum.nextElement();
			
			/* 
			 * Put key/value pair, possibly overwriting earlier settings
			 * from previous files, as expected.
			 */
			fileProperties.put(propKey, props.getProperty(propKey));
		}
	}
	
	public static void finishConfigFileToSystemProperties() {
		for(Entry<String, String> entry : fileProperties.entrySet()) {
			/* Allow command line properties to overwrite config file */
			if (System.getProperty(entry.getKey()) == null) {
				System.setProperty(entry.getKey(), entry.getValue());
			}
		}
		
		/* Avoid being called again, free up temporary fileProperties hash map */
		fileProperties = null;
	}
	
	public static void initNonLazyProperties() {
		// TODO: The majority of the following properties could be lazy instead
		
		if (System.getProperty("debug") != null) {
			debug = true;
		}
	}
	
	public static String getVersion() {
		return "1.0";
	}
	
	public static String getLineTerminator() {
		return System.getProperty("line.separator");
	}

	public static boolean isDebug() {
		return debug;
	}
	
	public static void setDebug(boolean val) {
		debug = val;
	}
}

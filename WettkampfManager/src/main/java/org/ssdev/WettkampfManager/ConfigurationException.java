/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.ssdev.WettkampfManager;

public class ConfigurationException extends RuntimeException {
	private static final long serialVersionUID = 4859239756074289688L;
	
	public ConfigurationException(String msg) {
		super(msg);
		outputError();
	}

	public ConfigurationException(String msg, Throwable t) {
		super(msg, t);
		outputError();
	}
	
	protected void outputError() {
		System.err.println("************************************************************");
		System.err.println("*             Configuration problem detected:              *");
		System.err.println("************************************************************");
		System.err.println("  Problem:");
		System.err.println("        " + this.getMessage());
		System.err.println("");
		System.err.println("  Backtrace:");
		this.printStackTrace();
		System.err.println("************************************************************");
		
		System.exit(1);
	}

}

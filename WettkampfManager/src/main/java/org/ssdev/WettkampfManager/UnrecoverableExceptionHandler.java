/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.ssdev.WettkampfManager;

import java.lang.Thread.UncaughtExceptionHandler;

public class UnrecoverableExceptionHandler implements UncaughtExceptionHandler {

	@Override
	public void uncaughtException(Thread t, Throwable exc) {
		StringBuilder errMsg = new StringBuilder();
		String lt = Settings.getLineTerminator();
		
		errMsg.append("************************************************************" + lt);
		errMsg.append("*            Fatal error: Unrecoverable Exception          *" + lt);
		errMsg.append("************************************************************" + lt);
		errMsg.append("  Failure in thread: " + t.getName() + lt);
		errMsg.append(RecoverableExceptionHandler.formatException(exc));
		errMsg.append("************************************************************" + lt);
		errMsg.append(lt);
		Long epoch = System.currentTimeMillis() / 1000;
		String filename = "langfuzz-critical-tombstone-" + epoch.toString() + ".log";
		
		try {
			/* Try writing the tombstone, but silently ignore any failures to prevent
			 * recursing into this handler again. */
			IO.writeFileUTF8(filename, errMsg.toString(), true);
		} catch (Throwable e) {}
		
		
		drawReset(); drawWarn();
		System.err.println("************************************************************");
		drawWarnBlink();
		System.err.println("*           Fatal error: Unrecoverable Exception           *");
		drawReset(); drawWarn();
		System.err.println("************************************************************");
		System.err.println("  Failure in thread: " + t.getName());
		System.err.println("  Backtrace: ");
		exc.printStackTrace();
		System.err.println("************************************************************");
		drawReset(); 
		System.err.println();
		
		System.exit(1);
	}
	
	protected void drawWarn() {
		System.err.print((char)27 + "[1;31;40m");
	}
	
	protected void drawWarnBlink() {
		System.err.print((char)27 + "[5;31;40m");
	}
	
	protected void drawReset() {
		System.err.print((char)27 + "[0;31;40m");
	}

}

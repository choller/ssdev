/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.ssdev.WettkampfManager;

import java.lang.Thread.UncaughtExceptionHandler;

public class RecoverableExceptionHandler implements UncaughtExceptionHandler {
	
	protected static RecoverableExceptionHandler myInstance = null;

	@Override
	public void uncaughtException(Thread t, Throwable exc) {
		StringBuilder errMsg = new StringBuilder();
		String lt = Settings.getLineTerminator();
		
		errMsg.append("************************************************************" + lt);
		errMsg.append("*          Non-Fatal error: Recoverable Exception          *" + lt);
		errMsg.append("************************************************************" + lt);
		errMsg.append("  Failure in thread: " + t.getName() + lt);
		errMsg.append(RecoverableExceptionHandler.formatException(exc));
		errMsg.append("************************************************************" + lt);
		errMsg.append(lt);
		
		Long epoch = System.currentTimeMillis() / 1000;
		
		String filename = "langfuzz-tombstone-" + epoch.toString() + ".log";
		
		/* Fail with an unrecoverable error if writing of tombstone does not succeed */
		IO.writeFileUTF8(filename, errMsg.toString(), true);
		
		String msg = "Caught Recoverable Exception: Wrote information to " + filename;
		System.err.println(msg);
	}
	
	public static String formatException(Throwable exc) {
		String lt = Settings.getLineTerminator();
		StringBuilder errMsg = new StringBuilder();

		/* Append message if available */
		String excMsg = exc.getMessage();
		if (excMsg != null) {
			errMsg.append("  Message: " + excMsg + lt);
		}
		
		errMsg.append("  Backtrace: " + lt);
		
		/* Append backtrace of outer exception */
		StackTraceElement[] trace = exc.getStackTrace();
		for (StackTraceElement e : trace) {
			errMsg.append("        " + e.toString() + lt);
		}
		
		/* Append backtrace of optional inner (wrapped) exception(s) */
		Throwable cause = exc.getCause();
		while (cause != null) {
			errMsg.append("  caused by: " + lt);
			
			StackTraceElement[] innerTrace = cause.getStackTrace();
			for (StackTraceElement e : innerTrace) {
				errMsg.append("        " + e.toString() + lt);
			}
			
			/* Append message if available */
			String causeMsg = cause.getMessage();
			if (causeMsg != null) {
				errMsg.append("  Cause message: " + causeMsg + lt);
			}
		
			/* Check if cause has again a cause to print */
			cause = cause.getCause();
		}
		
		return errMsg.toString();
	}
	
	public static RecoverableExceptionHandler getInstance() {
		if (myInstance == null) {
			myInstance = new RecoverableExceptionHandler();
		}
		
		return myInstance;
	}
}

package com.rapleafapi.sample;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Adapted from
 * http://java.sun.com/developer/technicalArticles/Security/pwordmask/
 * 
 */
public class PasswordField {

	/**
	 * @param prompt
	 *            The prompt to display to the user
	 * @return The password as entered by the user
	 */
	public static String readPassword(String prompt) {
		PasswordField p = new PasswordField();
		EraserThread et = p.new EraserThread(prompt);
		Thread mask = new Thread(et);
		mask.start();

		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		String password = "";

		try {
			password = in.readLine();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		// stop masking
		et.stopMasking();
		// return the password entered by the user
		return password;
	}

	class EraserThread implements Runnable {
		private boolean stop;

		/**
		 * @param The
		 *            prompt displayed to the user
		 */
		public EraserThread(String prompt) {
			System.out.print(prompt);
		}

		/**
		 * Begin masking...display asterisks (*)
		 */
		public void run() {
			stop = true;
			while (stop) {
				// hacky way of masking the password, but only way to do it in
				// Java without using swing
				System.out.print("\010*");
				try {
					Thread.currentThread().sleep(1);
				} catch (InterruptedException ie) {
					ie.printStackTrace();
				}
			}
		}

		/**
		 * Instruct the thread to stop masking
		 */
		public void stopMasking() {
			this.stop = false;
		}
	}
}

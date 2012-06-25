/**
 * Java Image Science Toolkit (JIST)
 *
 * Image Analysis and Communications Laboratory &
 * Laboratory for Medical Image Computing &
 * The Johns Hopkins University
 * 
 * http://www.nitrc.org/projects/jist/
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or (at
 * your option) any later version.  The license is available for reading at:
 * http://www.gnu.org/copyleft/lgpl.html
 *
 */
package edu.jhu.ece.iacl.jist.pipeline.view.input;

import java.util.Vector;

import edu.jhu.ece.iacl.jist.utility.JistLogger;

// TODO: Auto-generated Javadoc
/**
 * The Class Refresher.
 */
public class Refresher implements Runnable {

	/** The refresher. */
	protected static Refresher refresher = null;

	/** The refresh interval. */
	private static long refreshInterval = 1000;

	/** The Constant thLock. */
	private static final Object thLock = new Object(); // there can only be one
														// (refresher)

	/** The disabled. */
														protected boolean disabled = false;

	/** The disabled count. */
	protected int disabledCount = 0;

	/** The objs. */
	protected Vector<Refreshable> objs;
	/** The paused. */
	private boolean paused;

	/** The running. */
	private boolean running;

	/** The th. */
	private Thread th;
	/**
	 * Constructs new refresher for refreshable view. Starts refresher thread.
	 */
	public Refresher() {
		// thLock = new Object();
		th = null;
		paused = false;
		objs = new Vector<Refreshable>();
		start();
	}

	/**
	 * add item to monitor.
	 * 
	 * @param obj
	 *            refreshable object
	 */
	public void add(Refreshable obj) {
		{
			// System.err.println(getClass().getCanonicalName()+"Refresher: Add.");
			// System.err.flush();
			if (!disabled && !objs.contains(obj)) {
				if (!running) {
					start();
				}
				objs.add(obj);
			}
		}
	}

	/**
	 * Start refresher.
	 */
	public void start() {
		if (!running) {
			running = true;
		} else {
			// System.err.println(getClass().getCanonicalName()+"Refresher already started. Ignoring.");
			return;
		}
		// System.err.println(getClass().getCanonicalName()+"Waiting to start Refresher");System.err.flush();
		synchronized (thLock) {
			// System.err.println(getClass().getCanonicalName()+"Starting Refresher");System.err.flush();
			disabledCount = 0;
			disabled = false;
			if (th == null) {
				th = new Thread(this);
				th.setName("Refresher");
				th.setPriority(Thread.MIN_PRIORITY);
				th.start();
			} else {
				System.err.println(getClass().getCanonicalName()
						+ "Refresher thread not cleared. Warning.");
			}
		}
		// System.err.println(getClass().getCanonicalName()+"Refresher started.");System.err.flush();
	}

	/**
	 * Stop refresher.
	 */
	public void stopAll() {
		pauseAll();
		removeAll();
		stop();
	}

	/**
	 * Pause all refreshing.
	 */
	public void pauseAll() {
		paused = true;
	}

	/**
	 * Remove all refreshable objects.
	 */
	public void removeAll() {
		{
			System.err.println(getClass().getCanonicalName()
					+ "Refresher: removeAll.");
			System.err.flush();
			objs.clear();
		}
	}

	/**
	 * Force refresher to stop prematurely.
	 */
	public void stop() {
		// System.err.println(getClass().getCanonicalName()+"Waiting to stop Refresher");System.err.flush();
		synchronized (thLock) {
			// System.err.println(getClass().getCanonicalName()+"Stopping Refresher");
			// System.err.flush();
			System.err.flush();
			running = false;

			try {
				// th.join();
				// th.join(100);
				if (th != null && th.isAlive()) {
					th.interrupt();
					th.join(200);
					if (th.isAlive()) {
						System.err.println(getClass().getCanonicalName()
								+ "Refresher join failed.");
						System.err.flush();
					}
				}
			} catch (InterruptedException e) {
				System.err.println(getClass().getCanonicalName()
						+ "Refresher stop interrupted.");
				System.err.flush();
			} catch (NullPointerException e) {
				System.err.println(getClass().getCanonicalName()
						+ "Refresher not successfully stopped during join.");
				System.err.flush();
			}
			th = null;
		}
	}

	/**
	 * Periodically refresh view.
	 */
	@Override
	public void run() {
		while (running) {
			if (!paused && !disabled) {
				refreshAll();
			}
			try {
				Thread.sleep(refreshInterval);
			} catch (InterruptedException e) {
				running = false;
				System.err.println(getClass().getCanonicalName()
						+ "Refresher stopping via interrupt.");
				System.err.flush();
			}
		}
		System.err.println(getClass().getCanonicalName()
				+ "Refresher finished.");
		System.err.flush();
	}

	/**
	 * Refresh all.
	 */
	public void refreshAll() {

		try {
			for (int i = 0; i < objs.size(); i++) {
				// for (Refreshable obj : objs) {
				try {
					Refreshable obj = objs.get(i);
					System.out.flush();
					try {
						obj.refresh();
					} catch (NullPointerException e) {
						// The object changed while refreshing
						JistLogger.logError(
								JistLogger.FINE,
								"Refresher caught a null point exception:"
										+ e.toString());
					}
				} catch (java.lang.ArrayIndexOutOfBoundsException e) {
					// don't worry, refresher updated asynchronously
				}
			}
		} catch (OutOfMemoryError e) {
			System.err.println(getClass().getCanonicalName() + "OBJECTS "
					+ objs.size());
			e.printStackTrace();
		} catch (RuntimeException e) {
			System.err.println(getClass().getCanonicalName()
					+ "refreshAll Exception. ");
			e.printStackTrace();
		}
	}

	/**
	 * Get singleton reference to constructor.
	 * 
	 * @return the instance
	 */
	public static Refresher getInstance() {
		if (refresher == null) {
			refresher = new Refresher();
		}
		return refresher;
	}

	/**
	 * Disable.
	 */
	public void disable() {
		disabledCount++;
		disabled = true;
		// System.err.println(getClass().getCanonicalName()+"dc:"+disabledCount);System.err.flush();
	}

	/**
	 * Enable.
	 */
	public void enable() {
		disabledCount--;
		if (disabledCount <= 0) {
			disabled = false;
			disabledCount = 0;
		}
		// System.err.println(getClass().getCanonicalName()+"dc:"+disabledCount);System.err.flush();
	}

	/**
	 * Get refreshing interval.
	 * 
	 * @return time in milliseconds
	 */
	public long getRefreshInterval() {
		return refreshInterval;
	}

	/**
	 * Remove refreshable object.
	 * 
	 * @param obj
	 *            refreshable object
	 * @return true if removed successfully
	 */
	public boolean remove(Refreshable obj) {
		{
			System.err.println(getClass().getCanonicalName()
					+ "Refresher: Remove.");
			System.err.flush();
			return objs.remove(obj);
		}
	}

	/**
	 * Resume all refreshing.
	 */
	public void resumeAll() {
		paused = false;
	}

	/**
	 * Set refresh interval. The default is 1 second.
	 * 
	 * @param refreshInterval
	 *            Refresh interval in milliseconds
	 */
	public void setRefreshInterval(long refreshInterval) {
		Refresher.refreshInterval = refreshInterval;
	}
}

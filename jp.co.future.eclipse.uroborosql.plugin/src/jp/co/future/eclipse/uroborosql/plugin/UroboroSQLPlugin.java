package jp.co.future.eclipse.uroborosql.plugin;

import java.io.PrintStream;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import jp.co.future.eclipse.uroborosql.plugin.utils.Eclipses;

/**
 * The activator class controls the plug-in life cycle
 */
public class UroboroSQLPlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "jp.co.future.eclipse.uroborosql.plugin"; //$NON-NLS-1$

	public static final String ICON_KEY = "uroborosql";

	// The shared instance
	private static UroboroSQLPlugin plugin;

	/**
	 * The constructor
	 */
	public UroboroSQLPlugin() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static UroboroSQLPlugin getDefault() {
		return plugin;
	}

	@Override
	protected void initializeImageRegistry(ImageRegistry registry) {
		registry.put(ICON_KEY,
				ImageDescriptor.createFromURL(FileLocator.find(getBundle(), new Path("icons/uroborosql.ico"), null)));
	}

	public static void printConsole(String message) {
		MessageConsole console = Eclipses.getConsole(PLUGIN_ID + ".console");
		if (console == null) {
			System.out.println(message);
			return;
		}
		@SuppressWarnings("resource")
		MessageConsoleStream out = console.newMessageStream();
		out.println(message);
	}

	public static void printConsole(Throwable e) {
		MessageConsole console = Eclipses.getConsole(PLUGIN_ID + ".console");
		if (console == null) {
			e.printStackTrace();
			return;
		}
		MessageConsoleStream out = console.newMessageStream();
		@SuppressWarnings("resource")
		PrintStream ps = new PrintStream(out);
		e.printStackTrace(ps);
	}
}

package jp.co.future.eclipse.uroborosql.plugin.utils;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.texteditor.ITextEditor;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

public class Eclipses {
	public static IResource getResource(IPath path) {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		if (path.segmentCount() == 0) {
			return root;
		}
		IProject project = root.getProject(path.segment(0));
		if (path.segmentCount() == 1) {
			return project;
		}
		if (path.hasTrailingSeparator()) {
			return root.getFolder(path);
		} else {
			return root.getFile(path);
		}
	}

	public static IProject getProject(IEditorPart editor) {
		return getFile(editor).getProject();
	}

	public static IFile getFile(IEditorPart editor) {
		IFileEditorInput editorInput = (IFileEditorInput) editor.getEditorInput();
		return editorInput.getFile();
	}

	private static ITextEditor getTextEditor(IEditorPart editor) {

		if (editor instanceof ITextEditor) {
			return (ITextEditor) editor;
		}
		if (editor instanceof MultiPageEditorPart) {
			MultiPageEditorPart multiPageEditorPart = (MultiPageEditorPart) editor;
			Object sel = multiPageEditorPart.getSelectedPage();
			if (sel instanceof IEditorPart) {
				return getTextEditor((IEditorPart) sel);

			}
		}
		return null;
	}

	public static IWorkbenchWindow getActiveWorkbenchWindow() {
		return getActiveWorkbenchWindow(PlatformUI.getWorkbench());
	}

	public static IWorkbenchWindow getActiveWorkbenchWindow(AbstractUIPlugin plugin) {
		return getActiveWorkbenchWindow(plugin.getWorkbench());
	}

	public static IWorkbenchWindow getActiveWorkbenchWindow(IWorkbench workbench) {
		IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
		if (window != null) {
			return window;
		}
		IWorkbenchWindow[] windows = workbench.getWorkbenchWindows();
		if (windows.length > 0) {
			return windows[0];
		}
		return null;
	}

	public static ITextEditor getActiveEditor() {
		IWorkbenchWindow window = getActiveWorkbenchWindow();
		return getActiveEditor(window);
	}

	public static ITextEditor getActiveEditor(IWorkbenchWindow window) {
		IEditorPart editor = window.getActivePage().getActiveEditor();
		if (editor == null) {
			return null;
		}
		return getTextEditor(editor);
	}

	public static String getLineDelimiter(IDocument document) {
		try {
			ITextFileBufferManager bufferManager = FileBuffers.getTextFileBufferManager();
			ITextFileBuffer textFileBuffer = bufferManager.getTextFileBuffer(document);
			IResource resource;
			if (textFileBuffer != null) {
				IPath path = textFileBuffer.getLocation();
				resource = getResource(path);
			} else {
				resource = getFile(getActiveEditor());
			}
			return getLineDelimiter(resource);
		} catch (Exception e) {
			e.printStackTrace();
			return "\n";
		} catch (ExceptionInInitializerError | NoClassDefFoundError e) {
			e.printStackTrace();
			return "\n";
		}
	}

	public static String getLineDelimiter(IResource resource) {
		//https://stackoverflow.com/questions/36022415/find-the-configured-line-delimiter-for-a-file-in-eclipse
		String delimiter = null;
		IPreferencesService preferencesService = Platform.getPreferencesService();
		Preferences projectPreferences = preferencesService.getRootNode().node(ProjectScope.SCOPE)
				.node(resource.getProject().getName());
		try {
			if (projectPreferences.nodeExists(Platform.PI_RUNTIME)) {
				delimiter = projectPreferences.node(Platform.PI_RUNTIME).get(Platform.PREF_LINE_SEPARATOR, null);
			}
		} catch (BackingStoreException e) {
		}
		if (delimiter == null) {
			Preferences workspacePreferences = preferencesService.getRootNode().node(InstanceScope.SCOPE);
			delimiter = workspacePreferences.node(Platform.PI_RUNTIME).get(Platform.PREF_LINE_SEPARATOR, null);
		}
		if (delimiter == null) {
			delimiter = System.getProperty(Platform.PREF_LINE_SEPARATOR);
		}
		return delimiter;
	}
}

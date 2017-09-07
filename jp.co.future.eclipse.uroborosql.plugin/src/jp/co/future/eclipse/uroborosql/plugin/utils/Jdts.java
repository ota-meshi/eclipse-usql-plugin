package jp.co.future.eclipse.uroborosql.plugin.utils;

import java.io.IOException;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.ui.JavadocContentAccess;

public class Jdts {
	public static String getJavadoc(IMember member) {
		try {
			@SuppressWarnings("resource")
			Reader reader = JavadocContentAccess.getContentReader(member, false);
			if (reader == null) {
				return null;
			}
			try {
				StringBuilder buf = new StringBuilder();
				int ch;
				while ((ch = reader.read()) != -1) {
					buf.append((char) ch);
				}
				String s = buf.toString();
				return s.isEmpty() ? null : s;
			} finally {
				reader.close();
			}
		} catch (IOException | JavaModelException e) {
			return null;
		}
	}

	public static Class<?> getType(IField field) throws JavaModelException {
		Class<?> type = getSigType(field.getTypeSignature());
		if (type != null) {
			return type;
		}
		return findSigType(field.getTypeSignature(), field);
	}

	public static Object getValue(IField field) throws JavaModelException {
		Object value = field.getConstant();
		if (value instanceof String) {
			if (getSigType(field.getTypeSignature()).equals(String.class)) {
				String s = value.toString();
				return s.substring(1, s.length() - 1);
			}
		}
		if (value == null) {
			try {
				return getValueExpression(field);
			} catch (Exception e) {
				// ignore
			}
		}
		return value;
	}

	public static String getSimpleName(IType targetClass) {
		return targetClass.getElementName();
	}

	public static String getName(IType targetClass) {
		return targetClass.getFullyQualifiedName();
	}

	public static List<IField> getEnumConstants(IType targetClass) throws JavaModelException {
		List<IField> result = new ArrayList<>();
		for (IField f : targetClass.getFields()) {
			if (f.isEnumConstant()) {
				result.add(f);
			}
		}
		return result;
	}

	public static Optional<URL> getURLFromJavaProject(IProject project, String className) {
		if (className == null) {
			return Optional.empty();
		}

		IJavaProject javaProject = JavaCore.create(project);
		if (javaProject == null) {
			return Optional.empty();
		}
		try {

			IType type = javaProject.findType(className);

			if (type == null) {
				return Optional.empty();
			}
			IClassFile cf = type.getClassFile();
			if (cf == null) {
				return Optional.empty();
			}
			return Optional.ofNullable(cf.getPath().toFile().toURI().toURL());
		} catch (JavaModelException | MalformedURLException e) {
			return Optional.empty();
		}
	}

	//nashornで評価して値を取得する
	private static Object getValueExpression(IField field) throws JavaModelException, ScriptException {

		Set<Class<?>> imports = new HashSet<>();
		for (String importDeclaration : getImports(field)) {
			try {
				imports.add(Class.forName(importDeclaration));
			} catch (ClassNotFoundException e) {
				//
			}
		}
		//fieldのソースコードを取得
		IBuffer buffer = field.getOpenable().getBuffer();
		ISourceRange range = field.getSourceRange();
		StringBuilder sb = new StringBuilder();
		for (int i = range.getOffset(); i < range.getOffset() + range.getLength(); i++) {
			sb.append(buffer.getChar(i));
		}
		String[] ss = sb.toString().split("=");
		if (ss.length <= 1) {
			return null;
		}
		String s = ss[1].replaceAll(";\\s*$", "");

		//実行
		ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
		for (Class<?> importClass : imports) {
			engine.eval("var " + importClass.getSimpleName() + " = Java.type(\"" + importClass.getName() + "\");");
		}
		return engine.eval(s);
	}

	private static Set<String> getImports(IJavaElement element) throws JavaModelException {
		ICompilationUnit compilationUnit;
		while (!(element instanceof ICompilationUnit)) {
			element = element.getParent();
		}
		compilationUnit = (ICompilationUnit) element;
		return Arrays.stream(compilationUnit.getImports()).map(i -> i.getElementName()).collect(Collectors.toSet());
	}

	private static Class<?> getSigType(String signature) {
		if (signature.equals(Signature.SIG_INT)) {
			return int.class;
		} else if (signature.equals(Signature.SIG_SHORT)) {
			return short.class;
		} else if (signature.equals(Signature.SIG_BYTE)) {
			return byte.class;
		} else if (signature.equals(Signature.SIG_BOOLEAN)) {
			return boolean.class;
		} else if (signature.equals(Signature.SIG_CHAR)) {
			return char.class;
		} else if (signature.equals(Signature.SIG_DOUBLE)) {
			return boolean.class;
		} else if (signature.equals(Signature.SIG_FLOAT)) {
			return float.class;
		} else if (signature.equals(Signature.SIG_LONG)) {
			return long.class;
		} else if (signature.equals("QString;")) {//$NON-NLS-1$
			return String.class;
		} else if (signature.equals("Qjava.lang.String;")) {//$NON-NLS-1$
			return String.class;
		}
		return null;
	}

	private static Class<?> findSigType(String signature, IField field) throws JavaModelException {
		String sig = signature.replaceAll("^Q", "").replaceAll(";$", "");
		try {
			return Class.forName(sig);
		} catch (ClassNotFoundException e) {
			//
		}
		Set<String> imports = getImports(field);
		return imports.stream()
				.filter(s -> {
					int dotPos = s.lastIndexOf(".");
					String simpleName = s.substring(dotPos + 1);
					return sig.equals(simpleName);
				}).findFirst()
				.map(name -> {
					try {
						return Class.forName(name);
					} catch (ClassNotFoundException e) {
						//
					}
					return null;
				}).orElse(null);
	}

}

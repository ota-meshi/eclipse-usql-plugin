package jp.co.future.eclipse.uroborosql.plugin.contentassist;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

public class TestUtil {
	public static class StringList extends AbstractList<String> {
		private final List<String> list = new ArrayList<>();

		@Override
		public void add(int index, String element) {
			list.add(index, element);
		}

		@Override
		public String get(int index) {
			return list.get(index);
		}

		@Override
		public int size() {
			return list.size();
		}

		@Override
		public String toString() {
			return stream()
					.map(TestUtil::escape)
					.map(s -> "\"" + s + "\"")
					.collect(Collectors.joining(",\n", "Arrays.asList(\n", "\n)"));
		}

	}

	public static ITextViewer createTextViewer(String text) {
		IDocument doc = createDocument(text);
		return (ITextViewer) Proxy.newProxyInstance(TestUtil.class.getClassLoader(),
				new Class<?>[] { ITextViewer.class }, (proxy, method, args) -> {
					if (method.getName().equals("getDocument") && method.getParameterCount() == 0) {
						return doc;
					}
					throw new IllegalStateException("未対応:" + method.toString());
				});
	}

	public static IDocument createDocument(String text) {
		class InvocationHandlerImpl implements InvocationHandler {
			private String text;

			InvocationHandlerImpl(String text) {
				this.text = text;
			}

			@Override
			public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
				if (method.getName().equals("get") && method.getParameterCount() == 0) {
					return text;
				}
				if (method.getName().equals("get")
						&& Arrays.equals(method.getParameterTypes(), new Class<?>[] { int.class, int.class })) {
					return text.substring((Integer) args[0], (Integer) args[1]);
				}
				if (method.getName().equals("replace")
						&& Arrays.equals(method.getParameterTypes(),
								new Class<?>[] { int.class, int.class, String.class })) {
					int offset = (Integer) args[0];
					int length = (Integer) args[1];
					String reptext = (String) args[2];

					StringBuilder builder = new StringBuilder(text);
					builder.delete(offset, offset + length);
					builder.insert(offset, reptext);

					text = builder.toString();

					return null;
				}

				throw new IllegalStateException("未対応:" + method.toString());
			}

		}

		return (IDocument) Proxy.newProxyInstance(TestUtil.class.getClassLoader(), new Class<?>[] { IDocument.class },
				new InvocationHandlerImpl(text));
	}

	public static List<ICompletionProposal> toEqualable(ICompletionProposal... completionProposals) {
		return Arrays.stream(completionProposals)
				.map(TestUtil::toEqualable)
				.collect(Collectors.toList());
	}

	private static ICompletionProposal toEqualable(ICompletionProposal completionProposal) {
		if (completionProposal instanceof CompletionProposal) {
			return toEqualable((CompletionProposal) completionProposal);
		}
		if (completionProposal instanceof EqualableCompletionProposal) {
			return completionProposal;
		}

		System.out.println("cannot equalable");
		return completionProposal;
	}

	private static class EqualableCompletionProposal implements ICompletionProposal {

		private final CompletionProposal completionProposal;

		private final String fDisplayString;
		private final String fReplacementString;
		private final int fReplacementOffset;
		private final int fReplacementLength;
		private final int fCursorPosition;
		private final String fAdditionalProposalInfo;

		EqualableCompletionProposal(CompletionProposal completionProposal) {
			this.completionProposal = completionProposal;

			fDisplayString = get(completionProposal, CompletionProposal.class, "fDisplayString");
			fReplacementString = get(completionProposal, CompletionProposal.class, "fReplacementString");
			fReplacementOffset = get(completionProposal, CompletionProposal.class, "fReplacementOffset");
			fReplacementLength = get(completionProposal, CompletionProposal.class, "fReplacementLength");
			fCursorPosition = get(completionProposal, CompletionProposal.class, "fCursorPosition");
			fAdditionalProposalInfo = get(completionProposal, CompletionProposal.class, "fAdditionalProposalInfo");
		}

		@Override
		public Point getSelection(IDocument document) {
			return completionProposal.getSelection(document);
		}

		@Override
		public Image getImage() {
			return completionProposal.getImage();
		}

		@Override
		public String getDisplayString() {
			return completionProposal.getDisplayString();
		}

		@Override
		public IContextInformation getContextInformation() {
			return completionProposal.getContextInformation();
		}

		@Override
		public String getAdditionalProposalInfo() {
			return completionProposal.getAdditionalProposalInfo();
		}

		@Override
		public void apply(IDocument document) {
			completionProposal.apply(document);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + (fAdditionalProposalInfo == null ? 0 : fAdditionalProposalInfo.hashCode());
			result = prime * result + fCursorPosition;
			result = prime * result + (fDisplayString == null ? 0 : fDisplayString.hashCode());
			result = prime * result + fReplacementLength;
			result = prime * result + fReplacementOffset;
			result = prime * result + (fReplacementString == null ? 0 : fReplacementString.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (obj instanceof ICompletionProposal) {
				obj = toEqualable((ICompletionProposal) obj);
			}
			if (!(obj instanceof EqualableCompletionProposal)) {
				return false;
			}
			EqualableCompletionProposal other = (EqualableCompletionProposal) obj;
			if (fAdditionalProposalInfo == null) {
				if (other.fAdditionalProposalInfo != null) {
					return false;
				}
			} else if (!fAdditionalProposalInfo.equals(other.fAdditionalProposalInfo)) {
				return false;
			}
			if (fCursorPosition != other.fCursorPosition) {
				return false;
			}
			if (fDisplayString == null) {
				if (other.fDisplayString != null) {
					return false;
				}
			} else if (!fDisplayString.equals(other.fDisplayString)) {
				return false;
			}
			if (fReplacementLength != other.fReplacementLength) {
				return false;
			}
			if (fReplacementOffset != other.fReplacementOffset) {
				return false;
			}
			if (fReplacementString == null) {
				if (other.fReplacementString != null) {
					return false;
				}
			} else if (!fReplacementString.equals(other.fReplacementString)) {
				return false;
			}
			return true;
		}

		@Override
		public String toString() {
			return "new CompletionProposal(\"" + escape(fReplacementString) + "\", " + fReplacementOffset + ", "
					+ fReplacementLength + ", " + fCursorPosition + ", " + null + ", \"" + escape(fDisplayString)
					+ "\", "
					+ null
					+ ", \"" + escape(fAdditionalProposalInfo) + "\")";
		}

	}

	private static ICompletionProposal toEqualable(CompletionProposal completionProposal) {

		return new EqualableCompletionProposal(completionProposal);
	}

	private static String escape(String s) {
		return s.replace("\n", "\\n")
				.replace("\r", "\\r")
				.replace("\t", "\\t");
	}

	@SuppressWarnings("unchecked")
	private static <T, R> R get(T obj, Class<T> type, String name) {
		try {
			Field field = type.getDeclaredField(name);
			field.setAccessible(true);
			return (R) field.get(obj);
		} catch (NoSuchFieldException | SecurityException | IllegalAccessException e) {
			throw new IllegalStateException(e);
		}
	}

}

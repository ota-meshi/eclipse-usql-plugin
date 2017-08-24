package jp.co.future.eclipse.uroborosql.plugin.contentassist.uroborosql;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.PrimitiveIterator;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.swt.graphics.Image;

import jp.co.future.eclipse.uroborosql.plugin.UroboroSQLPlugin;
import jp.co.future.eclipse.uroborosql.plugin.config.PluginConfig;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.Document;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.DocumentPoint;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.DocumentScanner;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.SqlConstants;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.contentassist.FmtContentAssistProcessor;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.contentassist.ListContentAssistProcessor;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.contentassist.PartContentAssistProcessor;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.contentassist.TextContentAssistProcessor;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.contentassist.TreeContentAssistProcessor;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.parser.IdentifierNode;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.parser.Token;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.parser.TokenType;

public class UroboroSQLUtils {
	private static class Variable {
		private final String variableName;
		private final String sqlValue;

		Variable(String variableName, String sqlValue) {
			this.variableName = variableName;
			this.sqlValue = sqlValue;
		}

		String toCompletionProposal() {
			return "/*" + variableName + "*/" + Objects.toString(sqlValue, "''");
		}

		String getVariableName() {
			return variableName;
		}
	}

	private static class Variables implements Iterable<Variable> {
		private final Map<String, Variable> map = new HashMap<>();

		Variables put(Collection<String> variableNames, String sqlValue) {
			for (String variable : variableNames) {
				put(variable, sqlValue);
			}
			return this;
		}

		boolean containsVariable(String variableName) {
			return map.containsKey(variableName);
		}

		Variables put(Variable variable) {
			return put(variable.variableName, variable.sqlValue);
		}

		Variables put(String variable, String sqlValue) {
			if (sqlValue != null) {
				map.put(variable, new Variable(variable, sqlValue));
			} else {
				Variable old = map.get(variable);
				if (old == null) {
					map.put(variable, new Variable(variable, sqlValue));
				}
			}
			return this;
		}

		Variables putAll(Variables variables) {
			for (Variable variable : variables) {
				this.put(variable);
			}
			return this;
		}

		Collection<Variable> variables() {
			return map.values();
		}

		@Override
		public Iterator<Variable> iterator() {
			return variables().iterator();
		}
	}

	private enum ScriptType {
		IF, ELIF, VALIABLE, UNKNOWN
	}

	private static class ScriptVariables {
		private final Set<String> variableNames;
		private final ScriptType type;

		ScriptVariables(Set<String> variableNames, ScriptType type) {
			this.variableNames = variableNames;
			this.type = type;
		}
	}

	public static final List<String> SYNTAXES = Collections
			.unmodifiableList(Arrays.asList("IF", "END", "ELSE", "ELIF", "BEGIN"));
	public static final List<PartContentAssistProcessor> SYNTAX_PROCESSORS = Collections.unmodifiableList(Arrays.asList(
			new TextContentAssistProcessor("/*IF", new String[] { "/*IF */", "/*END*/" }, 5, "/*IF*/", "uroboroSQL IF"),
			new TextContentAssistProcessor("/*BEGIN*/", new String[] { "/*BEGIN*/", "/*END*/" }, 10, "/*BEGIN*/",
					"uroboroSQL BEGIN"),
			new TextContentAssistProcessor("/*END*/", "/*END*/", "uroboroSQL END"),
			new TextContentAssistProcessor("/*ELSE*/", "/*ELSE*/", "uroboroSQL ELSE"),
			new TextContentAssistProcessor("/*ELIF", "/*ELIF */", 7, "/*ELIF*/", "uroboroSQL ELIF")

	));

	public static final List<String> STR_FUNCTION_METHODS = Collections
			.unmodifiableList(Arrays.asList(
					"SF",
					"SF.isEmpty()",
					"SF.isNotEmpty()",
					"SF.isBlank()",
					"SF.isNotBlank()",
					"SF.trim()",
					"SF.trimToEmpty()",
					"SF.left()",
					"SF.right()",
					"SF.mid()",
					"SF.rightPad()",
					"SF.rightPad()",
					"SF.leftPad()",
					"SF.leftPad()",
					"SF.split()",
					"SF.capitalize()",
					"SF.uncapitalize()"));

	public static boolean withinScript(DocumentPoint commentStart) {
		String viewText = commentStart.getRangeText();

		if (viewText.startsWith("/*IF ") || viewText.startsWith("/*ELIF ")) {
			return true;
		}
		return false;
	}

	public static ListContentAssistProcessor getScriptAssistProcessors(Document document, PluginConfig config) {
		TreeContentAssistProcessor contentAssistProcessor = new TreeContentAssistProcessor();

		//string functions
		contentAssistProcessor.addAll(STR_FUNCTION_METHODS, "uroboroSQL String Function script");

		//定数
		contentAssistProcessor.addAll(config.getConsts().keySet(), "uroboroSQL const value\nsetting const value.");

		//すでに利用しているスクリプト
		contentAssistProcessor.addAll(document.getTokens().stream()
				.filter(t -> t.getType() == TokenType.M_COMMENT)
				.map(t -> getScripts(t, document, config))
				.flatMap(Set::stream)
				.collect(Collectors.toSet()), "uroboroSQL script\nused script.");

		//SQLの中から可能性のある変数名
		Set<String> sqlTokenVariables = new HashSet<>();
		setAllIdentifierVariables(sqlTokenVariables, document.getIdentifierNodes());

		contentAssistProcessor.addAll(sqlTokenVariables,
				"uroboroSQL variable candidate\ncandidates calculated from identifiers.");

		return contentAssistProcessor;
	}

	public static List<PartContentAssistProcessor> getAllVariableAssistProcessors(Document document,
			PluginConfig config) {
		List<PartContentAssistProcessor> variableAssistProcessors = new ArrayList<>();

		//定数
		config.getConsts().entrySet().stream()
				.sorted(Comparator.comparing(Map.Entry::getKey))
				.map(e -> new FmtContentAssistProcessor("/*#" + e.getKey() + "*/",
						"/*#" + e.getKey() + "*/" + toLiteral(e.getValue()),
						"/*#" + e.getKey() + "*/" + toLiteral(e.getValue()),
						"uroboroSQL const value\nsetting const value."))
				.forEach(variableAssistProcessors::add);

		//すでに利用している変数
		Variables variables = document.getTokens().stream()
				.filter(t -> t.getType() == TokenType.M_COMMENT)
				.map(t -> getVariables(t, document, config))
				.collect(Collectors.reducing(Variables::putAll)).orElseGet(Variables::new);

		variables.variables().stream()
				.filter(e -> !STR_FUNCTION_METHODS.contains(e.getVariableName()))
				.sorted(Comparator.comparing(Variable::getVariableName))
				.map(e -> new FmtContentAssistProcessor("/*" + e.getVariableName() + "*/",
						e.toCompletionProposal(),
						e.toCompletionProposal(),
						"uroboroSQL variable\nused variable name."))
				.forEach(variableAssistProcessors::add);

		//SQLの中から可能性のある変数名
		Set<String> sqlTokenVariables = new HashSet<>();
		setAllIdentifierVariables(sqlTokenVariables, document.getIdentifierNodes());
		List<PartContentAssistProcessor> sqlTokenVariableAssistProcessors = sqlTokenVariables.stream()
				.filter(s -> !variables.containsVariable(s))
				.sorted(Comparator.naturalOrder())
				.map(token -> new FmtContentAssistProcessor("/*" + token + "*/",
						"/*" + token + "*/''",
						"/*" + token + "*/''",
						"uroboroSQL variable candidate\ncandidates calculated from identifiers."))
				.collect(Collectors.toList());

		variableAssistProcessors.addAll(sqlTokenVariableAssistProcessors);
		return variableAssistProcessors;
	}

	private static String toLiteral(Object value) {
		if (value == null) {
			return "''";
		}
		if (value instanceof Number) {
			return value.toString();
		}
		return "'" + value + "'";
	}

	public static String toCamel(final String original) {
		if (original == null || original.isEmpty()) {
			return "";
		}
		StringBuilder builder = new StringBuilder();
		PrimitiveIterator.OfInt ci = original.trim().toLowerCase().codePoints().iterator();
		while (ci.hasNext()) {
			int codePoint = ci.nextInt();
			if (codePoint == '_') {
				if (ci.hasNext()) {
					codePoint = ci.nextInt();
					builder.append(Character.toChars(Character.toUpperCase(codePoint)));
				}
			} else {
				builder.append(Character.toChars(codePoint));
			}
		}
		return builder.toString();
	}

	public static DocumentPoint getScriptStartPoint(Document document) {
		DocumentScanner scanner = document.createDocumentScanner();
		while (scanner.hasPrevious()) {
			char c = scanner.previous();
			if (!Character.isJavaIdentifierPart(c) && c != '.' && c != '#') {
				scanner.next();
				return scanner.toDocumentPoint();
			}
		}
		return null;
	}

	private static void setAllIdentifierVariables(Set<String> sqlTokenVariables,
			Collection<IdentifierNode> identifierNodes) {
		for (IdentifierNode identifierNode : identifierNodes) {
			if (identifierNode.children().isEmpty()) {
				if (!SqlConstants.SQL_RESERVED_WORDS.contains(identifierNode.getNode().toUpperCase())) {
					sqlTokenVariables.add(toCamel(identifierNode.getNode()));
				}
			} else {
				setAllIdentifierVariables(sqlTokenVariables, identifierNode.children());
			}
		}
	}

	private static Variables getVariables(Token commentToken, Document document, PluginConfig config) {
		ScriptVariables scriptVariables = getScriptVariables(commentToken, document, config);

		if (scriptVariables.type == ScriptType.VALIABLE) {
			String sqlValue = getVariableSqlValue(commentToken);
			return new Variables().put(scriptVariables.variableNames, sqlValue);
		} else {
			return new Variables().put(scriptVariables.variableNames, null);
		}
	}

	private static Set<String> getScripts(Token commentToken, Document document, PluginConfig config) {
		return getScriptVariables(commentToken, document, config).variableNames;
	}

	private static ScriptVariables getScriptVariables(Token commentToken, Document document, PluginConfig config) {
		if (commentToken.equals(document.getUserOffsetToken())) {
			return new ScriptVariables(Collections.emptySet(), ScriptType.UNKNOWN);
		}

		String s = commentToken.getString();
		if (s.startsWith("/*IF ")) {
			String expression = s.substring(5).replaceAll("\\*/$", "");
			return new ScriptVariables(OGNLUtils.getVariableNames(expression), ScriptType.IF);
		} else if (s.startsWith("/*ELIF ")) {
			String expression = s.substring(7).replaceAll("\\*/$", "");
			return new ScriptVariables(OGNLUtils.getVariableNames(expression), ScriptType.ELIF);
		} else {
			String comment = s.replaceAll("\\*/$", "").replaceAll("^/\\*", "").trim();
			if (!isTargetComment(comment)) {
				return new ScriptVariables(Collections.emptySet(), ScriptType.VALIABLE);
			}
			if (SYNTAXES.contains(comment) || config.getSqlId().equals(comment)) {
				return new ScriptVariables(Collections.emptySet(), ScriptType.VALIABLE);
			}

			if ('#' == comment.charAt(0) || '$' == comment.charAt(0)) {
				return new ScriptVariables(new HashSet<>(Arrays.asList(comment)), ScriptType.VALIABLE);
			}

			return new ScriptVariables(OGNLUtils.getVariableNames(comment), ScriptType.VALIABLE);
		}
	}

	private static String getVariableSqlValue(Token commentToken) {
		StringBuilder sb = new StringBuilder();
		Token next = commentToken.getNextToken().orElse(null);
		while (next != null && next.getType().isVariableNext()) {
			sb.append(next.getString());
			next = next.getNextToken().orElse(null);
		}
		return sb.length() > 0 ? sb.toString() : null;
	}

	private static boolean isTargetComment(final String comment) {
		if (comment != null
				&& !comment.isEmpty()) {
			char c = comment.charAt(0);
			if (Character.isJavaIdentifierStart(c) || '#' == c || '(' == c) {
				return !comment.contains("\n") && !comment.contains("\r");
			}
		}
		return false;
	}

	public static Image getImage() {
		try {
			return UroboroSQLPlugin.getDefault().getImageRegistry().get(UroboroSQLPlugin.ICON_KEY);
		} catch (Exception e) {

		}
		return null;
	}

}

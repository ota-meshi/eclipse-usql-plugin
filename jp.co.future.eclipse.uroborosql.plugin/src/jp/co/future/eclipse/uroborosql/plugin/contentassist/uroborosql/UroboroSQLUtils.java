package jp.co.future.eclipse.uroborosql.plugin.contentassist.uroborosql;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.swt.graphics.Image;

import jp.co.future.eclipse.uroborosql.plugin.UroboroSQLPlugin;
import jp.co.future.eclipse.uroborosql.plugin.config.PluginConfig;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.uroborosql.data.variables.Const;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.uroborosql.data.variables.IVariable;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.uroborosql.data.variables.Variable;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.uroborosql.data.variables.VariableValue;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.uroborosql.data.variables.Variables;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.Document;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.DocumentPoint;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.DocumentScanner;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.SqlConstants;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.contentassist.IListContentAssistProcessor;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.contentassist.IPartContentAssistProcessor;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.contentassist.Replacement;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.contentassist.TextContentAssistProcessor;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.contentassist.TokenContentAssistProcessor;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.contentassist.TreeContentAssistProcessor;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.data.IBranch;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.data.IMethod;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.data.INamedNode;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.data.impl.Branch;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.parser.IdentifierNode;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.parser.Token;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.parser.TokenType;
import jp.co.future.eclipse.uroborosql.plugin.utils.Strings;

public class UroboroSQLUtils {

	private enum ScriptType {
		IF, ELIF, VALIABLE, CONST, UNKNOWN
	}

	private static class ScriptVariables {
		private final Set<String> variableNames;
		private final ScriptType type;

		ScriptVariables(ScriptType type) {
			variableNames = Collections.emptySet();
			this.type = type;
		}

		ScriptVariables(Set<String> variableNames, ScriptType type) {
			this.variableNames = variableNames;
			this.type = type;
		}

		ScriptVariables(String variableName, ScriptType type) {
			variableNames = new HashSet<>();
			variableNames.add(variableName);
			this.type = type;
		}
	}

	public static final List<String> SYNTAXES = Collections
			.unmodifiableList(Arrays.asList("IF", "END", "ELSE", "ELIF", "BEGIN"));
	public static final List<IPartContentAssistProcessor> SYNTAX_PROCESSORS = Collections
			.unmodifiableList(Arrays.asList(
					new TextContentAssistProcessor("/*IF",
							new Replacement(new String[] { "/*IF */", "/*END*/" }, 5, false), "/*IF*/",
							() -> "syntax IF"),
					new TextContentAssistProcessor("/*BEGIN*/",
							new Replacement(new String[] { "/*BEGIN*/", "/*END*/" }, 10, false),
							"/*BEGIN*/",
							() -> "syntax BEGIN"),
					new TextContentAssistProcessor("/*END*/", false, "/*END*/", () -> "syntax END"),
					new TextContentAssistProcessor("/*ELSE*/", false, "/*ELSE*/", () -> "syntax ELSE"),
					new TextContentAssistProcessor("/*ELIF", new Replacement("/*ELIF */", 7, false), "/*ELIF*/",
							() -> "syntax ELIF")

	));

	public static final IBranch STR_FUNCTION = IBranch.ofDef("SF",

			IMethod.ofDef("isEmpty", boolean.class, "str")
					.setClassName("jp.co.future.uroborosql.utils.StringFunction"),
			IMethod.ofDef("isNotEmpty", boolean.class, "str")
					.setClassName("jp.co.future.uroborosql.utils.StringFunction"),
			IMethod.ofDef("isBlank", boolean.class, "str")
					.setClassName("jp.co.future.uroborosql.utils.StringFunction"),
			IMethod.ofDef("isNotBlank", boolean.class, "str")
					.setClassName("jp.co.future.uroborosql.utils.StringFunction"),
			IMethod.ofDef("trim", String.class, "str")
					.setClassName("jp.co.future.uroborosql.utils.StringFunction"),
			IMethod.ofDef("trimToEmpty", String.class, "str")
					.setClassName("jp.co.future.uroborosql.utils.StringFunction"),
			IMethod.ofDef("left", String.class, "str", "len")
					.setClassName("jp.co.future.uroborosql.utils.StringFunction"),
			IMethod.ofDef("right", String.class, "str", "len")
					.setClassName("jp.co.future.uroborosql.utils.StringFunction"),
			IMethod.ofDef("mid", String.class, "str", "pos", "len")
					.setClassName("jp.co.future.uroborosql.utils.StringFunction"),
			IMethod.ofDef("rightPad", String.class, "str", "size")
					.setClassName("jp.co.future.uroborosql.utils.StringFunction"),
			IMethod.ofDef("rightPad", String.class, "str", "size", "padChar")
					.setClassName("jp.co.future.uroborosql.utils.StringFunction"),
			IMethod.ofDef("leftPad", String.class, "str", "size")
					.setClassName("jp.co.future.uroborosql.utils.StringFunction"),
			IMethod.ofDef("leftPad", String.class, "str", "size", "padChar")
					.setClassName("jp.co.future.uroborosql.utils.StringFunction"),
			IMethod.ofDef("split", String[].class, "str")
					.setClassName("jp.co.future.uroborosql.utils.StringFunction"),
			IMethod.ofDef("split", String[].class, "str", "separatorChar")
					.setClassName("jp.co.future.uroborosql.utils.StringFunction"),
			IMethod.ofDef("split", String[].class, "str", "separatorChars", "max")
					.setClassName("jp.co.future.uroborosql.utils.StringFunction"),
			IMethod.ofDef("capitalize", String.class, "str")
					.setClassName("jp.co.future.uroborosql.utils.StringFunction"),
			IMethod.ofDef("uncapitalize", String.class, "str")
					.setClassName("jp.co.future.uroborosql.utils.StringFunction")

	).setClassName("jp.co.future.uroborosql.utils.StringFunction");

	public static boolean withinScript(DocumentPoint commentStart) {
		String viewText = commentStart.getRangeText();

		if (viewText.startsWith("/*IF ") || viewText.startsWith("/*ELIF ")) {
			return true;
		}
		return false;
	}

	public static IListContentAssistProcessor getScriptAssistProcessors(Document document, boolean lazy,
			PluginConfig config) {
		TreeContentAssistProcessor contentAssistProcessor = new TreeContentAssistProcessor();

		//string functions
		contentAssistProcessor.add(STR_FUNCTION);

		//定数
		config.getConsts().forEach(
				v -> contentAssistProcessor.add(IBranch.ofValue(v.getVariableName(), v.getValue().getOriginal())
						.setGetAdditionalProposalInfo(() -> v.getActDescription())));
		if (lazy) {
			//TODO
		}

		//すでに利用しているスクリプト
		Set<String> names = document.getTokens().stream()
				.filter(t -> t.getType() == TokenType.M_COMMENT)
				.map(t -> getScripts(t, document, config))
				.flatMap(Set::stream)
				.collect(Collectors.toSet());
		Collection<INamedNode> nodes = splitNamedNodes(names, "used script.");
		contentAssistProcessor.addAll(nodes);

		//SQLの中から可能性のある変数名
		Set<String> sqlTokenVariables = new HashSet<>();
		setAllIdentifierVariables(sqlTokenVariables, document.getIdentifierNodes());

		nodes = splitNamedNodes(sqlTokenVariables, "candidates calculated from identifiers.");
		contentAssistProcessor.addAll(nodes);

		return contentAssistProcessor;
	}

	private static Collection<INamedNode> splitNamedNodes(Collection<String> collection,
			String additionalProposalInfo) {
		Branch root = IBranch.ofUnknown("root");
		for (String s : collection) {
			List<String> parents = new ArrayList<>();
			for (String name : s.split("\\.")) {
				INamedNode node = INamedNode.ofUnknownToken(name).setAdditionalProposalInfo(additionalProposalInfo);
				root.putChild(parents, node);
				parents.add(name);
			}
		}
		return root.children().stream().collect(Collectors.toList());

	}

	public static List<IPartContentAssistProcessor> getAllVariableAssistProcessors(Document document,
			boolean lazy, PluginConfig config) {

		Variables variables = new Variables();
		//定数
		variables.putAll(config.getConsts());

		//すでに利用している変数
		variables.putAll(document.getTokens().stream()
				.filter(t -> t.getType() == TokenType.M_COMMENT)
				.map(t -> getVariables(t, document, config))
				.collect(Variables.reducing()).orElseGet(Variables::new));

		List<IPartContentAssistProcessor> variableAssistProcessors = new ArrayList<>();

		for (IVariable variable : variables.variables()) {
			if (STR_FUNCTION.name().equals(variable.getVariableName())) {
				continue;
			}
			if (!lazy) {
				variableAssistProcessors.add(variable.createContentAssistProcessor());
			} else {
				variableAssistProcessors.add(variable.createLazyContentAssistProcessor());
			}
		}

		//SQLの中から可能性のある変数名
		Set<String> sqlTokenVariables = new HashSet<>();
		setAllIdentifierVariables(sqlTokenVariables, document.getIdentifierNodes());
		List<IPartContentAssistProcessor> sqlTokenVariableAssistProcessors = sqlTokenVariables.stream()
				.filter(s -> !variables.containsVariable(s))
				.map(token -> new TokenContentAssistProcessor("/*" + token + "*/",
						"/*" + token + "*/''", false,
						() -> "candidates calculated from identifiers."))
				.collect(Collectors.toList());

		variableAssistProcessors.addAll(sqlTokenVariableAssistProcessors);
		return variableAssistProcessors;
	}

	public static DocumentPoint getScriptStartPoint(Document document) {
		DocumentScanner scanner = document.createDocumentScanner();
		while (scanner.hasPrevious()) {
			char c = scanner.previous();
			if (c == ')') {
				skipPrevPair(scanner, c);
				continue;
			}
			if (!Character.isJavaIdentifierPart(c) && c != '.' && c != '#') {
				scanner.next();
				return scanner.toDocumentPoint();
			}
		}
		return null;
	}

	public static void skipPrevPair(DocumentScanner scanner, char ch) {
		if (ch == ')') {
			while (scanner.hasPrevious()) {
				char c = scanner.previous();
				if (c == ')') {
					skipPrevPair(scanner, c);
					continue;
				} else if (c == '(') {
					return;
				}
			}
		}
	}

	private static void setAllIdentifierVariables(Set<String> sqlTokenVariables,
			Collection<IdentifierNode> identifierNodes) {
		for (IdentifierNode identifierNode : identifierNodes) {
			if (identifierNode.children().isEmpty()) {
				if (!SqlConstants.SQL_RESERVED_WORDS.contains(identifierNode.getNode().toUpperCase())) {
					sqlTokenVariables.add(Strings.toCamel(identifierNode.getNode()));
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
			return scriptVariables.variableNames.stream()
					.map(s -> new Variable(s, VariableValue.ofLiteral(sqlValue)))
					.collect(Variables.toVariables());
		} else if (scriptVariables.type == ScriptType.CONST) {
			String sqlValue = getVariableSqlValue(commentToken);
			return scriptVariables.variableNames.stream()
					.map(s -> new Const(s, VariableValue.ofLiteral(sqlValue)))
					.collect(Variables.toVariables());
		} else {
			return scriptVariables.variableNames.stream()
					.map(s -> new Variable(s))
					.collect(Variables.toVariables());
		}
	}

	private static Set<String> getScripts(Token commentToken, Document document, PluginConfig config) {
		return getScriptVariables(commentToken, document, config).variableNames;
	}

	private static ScriptVariables getScriptVariables(Token commentToken, Document document, PluginConfig config) {
		if (commentToken.equals(document.getUserOffsetToken())) {
			return new ScriptVariables(ScriptType.UNKNOWN);
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
			if (!isVariableTargetComment(comment)) {
				return new ScriptVariables(ScriptType.VALIABLE);
			}
			if (SYNTAXES.contains(comment) || config.getSqlId().equals(comment)) {
				return new ScriptVariables(ScriptType.VALIABLE);
			}

			if ('#' == comment.charAt(0) || '$' == comment.charAt(0)) {
				return new ScriptVariables(comment.substring(1), ScriptType.CONST);
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

	public static boolean isVariableTargetComment(final String comment) {
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

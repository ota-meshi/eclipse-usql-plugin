package jp.co.future.eclipse.uroborosql.plugin.contentassist.uroborosql.type;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.function.Function;

import jp.co.future.eclipse.uroborosql.plugin.config.PluginConfig;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.uroborosql.ContentAssistProcessors;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.uroborosql.data.identifiers.Column;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.uroborosql.data.identifiers.IIdentifier;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.uroborosql.data.identifiers.IIdentifier.IdentifierReplacement;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.uroborosql.data.identifiers.Table;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.Document;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.DocumentPoint;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.contentassist.CompletionProposal;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.contentassist.CompletionProposal.DocReplacement;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.contentassist.IPartContentAssistProcessor;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.contentassist.IPointCompletionProposal;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.contentassist.Replacement;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.contentassist.TextContentAssistProcessor;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.parser.Token;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.parser.Token.TokenRange;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.parser.TokenType;
import jp.co.future.eclipse.uroborosql.plugin.utils.Strings;
import jp.co.future.eclipse.uroborosql.plugin.utils.collection.Iterators;

public enum StatementTypes implements IType {
	SELECT {
		@Override
		public List<IPointCompletionProposal> computeCompletionProposals(DocumentPoint tokenStart, boolean lazy,
				PluginConfig config) {
			//カラムエイリアス有
			Optional<Table> aliasTable = getColumnAtTable(tokenStart, config);
			if (aliasTable.isPresent()) {
				return getColumnCompletionProposals(aliasTable.get(), tokenStart, lazy,
						new IdentifierReplacement<Column>(
								col -> col.buildSelectColumn(0, tokenStart.getReservedCaseFormatter()).toString(),
								col -> col.buildSelectColumn(0, tokenStart.getReservedCaseFormatter())));
			}

			List<IdentifierReplacement<Table>> buildTables = new ArrayList<>();
			boolean soonNext = Iterators.asIteratorFromNext(tokenStart.getToken(), Token::getPrevToken).stream()
					.filter(t -> t.getType().isSqlEnable()).findFirst().filter(t -> isToken(t)).isPresent();
			if (soonNext) {
				buildTables.add(new IdentifierReplacement<>(table -> "(SEL)" + table, table -> {
					return table.buildSelectSql(tokenStart.getReservedCaseFormatter());
				}));
			}

			return getTableCompletionProposals(tokenStart, lazy, config, buildTables);
		}
	},
	FROM {
		@Override
		public List<IPointCompletionProposal> computeCompletionProposals(DocumentPoint tokenStart, boolean lazy,
				PluginConfig config) {
			return getTableCompletionProposals(tokenStart, lazy, config);
		}
	},
	WHERE {
		@Override
		public List<IPointCompletionProposal> computeCompletionProposals(DocumentPoint tokenStart, boolean lazy,
				PluginConfig config) {
			//カラムエイリアス有
			Optional<Table> aliasTable = getColumnAtTable(tokenStart, config);
			if (aliasTable.isPresent()) {
				return getColumnCompletionProposals(aliasTable.get(), tokenStart, lazy, new IdentifierReplacement<>(
						col -> col.buildConditionColumn(0).toString(), col -> col.buildConditionColumn(0)));
			}
			return Collections.emptyList();
		}
	},
	UPDATE {
		@Override
		public List<IPointCompletionProposal> computeCompletionProposals(DocumentPoint tokenStart, boolean lazy,
				PluginConfig config) {
			List<IdentifierReplacement<Table>> buildTables = new ArrayList<>();
			boolean soonNext = Iterators.asIteratorFromNext(tokenStart.getToken(), Token::getPrevToken).stream()
					.filter(t -> t.getType().isSqlEnable()).findFirst().filter(t -> isToken(t)).isPresent();
			if (soonNext) {
				buildTables.add(new IdentifierReplacement<>(table -> "(UPD)" + table, table -> {
					return table.buildUpdateSql(tokenStart.getReservedCaseFormatter());
				}));
			}

			return getTableCompletionProposals(tokenStart, lazy, config, buildTables);
		}
	},
	UPDATE_SET {
		@Override
		public List<IPointCompletionProposal> computeCompletionProposals(DocumentPoint tokenStart, boolean lazy,
				PluginConfig config) {
			//カラムエイリアス有
			Optional<Table> aliasTable = getColumnAtTable(tokenStart, config);
			if (aliasTable.isPresent()) {
				return getColumnCompletionProposals(aliasTable.get(), tokenStart, lazy, new IdentifierReplacement<>(
						col -> col.buildSetColumn(0).toString(), col -> col.buildSetColumn(0)));
			}

			//エイリアスなしのカラム
			Optional<Table> updateTable = getUpdateTable(tokenStart, config);
			if (updateTable.isPresent()) {
				return getColumnCompletionProposals(updateTable.get(), tokenStart, lazy, new IdentifierReplacement<>(
						col -> col.buildSetColumn(0).toString(), col -> col.buildSetColumn(0)));
			}

			return Collections.emptyList();
		}

		@Override
		protected boolean isToken(Token token) {
			if (!token.getString().equalsIgnoreCase("SET")) {
				return false;
			}
			return Token.getPrevSiblings(token).filter(p -> Token.isUpdateWord(p)).findFirst().isPresent();
		}

		private Optional<Table> getUpdateTable(DocumentPoint tokenStart, PluginConfig config) {
			for (Token setToken : Token.getPrevSiblingOrParents(tokenStart.getToken())
					.filter(p -> Token.isSetWord(p))) {
				Token updToken = Token.getPrevSiblings(setToken).filter(p -> Token.isUpdateWord(p)).findFirst()
						.orElse(null);
				if (updToken == null) {
					continue;
				}
				Token tableToken = Token.getBetweenTokens(updToken.getNextToken().get(), setToken.getPrevToken().get())
						.stream().filter(p -> p.getType() == TokenType.SQL_TOKEN).filter(p -> !p.isReservedWord())
						.findFirst().orElse(null);
				if (tableToken == null) {
					continue;
				}
				return getTable(config, tableToken.getString());
			}
			return Optional.empty();
		}
	},
	INSERT_INTO {
		@Override
		public List<IPointCompletionProposal> computeCompletionProposals(DocumentPoint tokenStart, boolean lazy,
				PluginConfig config) {
			//カラムエイリアス有
			Optional<Table> aliasTable = getColumnAtTable(tokenStart, config);
			if (aliasTable.isPresent()) {
				return getColumnCompletionProposals(aliasTable.get(), tokenStart, lazy);
			}

			boolean soonNext = Iterators.asIteratorFromNext(tokenStart.getToken(), Token::getPrevToken).stream()
					.filter(t -> t.getType().isSqlEnable()).findFirst().filter(t -> isToken(t)).isPresent();
			if (soonNext) {
				List<IdentifierReplacement<Table>> buildTables = new ArrayList<>();
				buildTables.add(new IdentifierReplacement<>(table -> "(INS)" + table, table -> {
					return table.buildInsertSql();
				}));
				return getTableCompletionProposals(tokenStart, lazy, config, buildTables);
			}

			List<IPointCompletionProposal> result = new ArrayList<>();
			//エイリアスなしのカラム
			Optional<Table> insertTable = getInsertTable(tokenStart, config);
			if (insertTable.isPresent()) {
				result.addAll(
						getColumnCompletionProposals(insertTable.get(), tokenStart, lazy, new IdentifierReplacement<>(
								col -> col.buildInsertColumn(0).toString(), col -> col.buildInsertColumn(0))));
			}

			//VALUES
			TokenRange insColsParenthesis = findInsertParenthesis(tokenStart.getToken()).orElse(null);
			if (insColsParenthesis != null) {
				result.addAll(getValuesCompletionProposals(tokenStart, lazy, insColsParenthesis));
			}

			//テーブル
			result.addAll(getTableCompletionProposals(tokenStart, lazy, config));
			return result;
		}

		@Override
		protected boolean isToken(Token token) {
			if (!Token.isIntoWord(token)) {
				return false;
			}
			return Token.getPrevSiblings(token).filter(p -> p.getType().isSqlEnable()).findFirst()
					.filter(p -> Token.isInsertWord(p)).isPresent();
		}

		private Optional<Table> getInsertTable(DocumentPoint tokenStart, PluginConfig config) {
			for (Token intoToken : Token.getPrevSiblingOrParents(tokenStart.getToken())
					.filter(p -> Token.isIntoWord(p))) {
				boolean isInsert = Token.getPrevSiblings(intoToken).filter(p -> Token.isInsertWord(p)).findFirst()
						.isPresent();
				if (!isInsert) {
					continue;
				}
				Token tableToken = Token.getNextSiblings(intoToken).filter(p -> p.getType().isSqlEnable()).findFirst()
						.orElse(null);

				if (tableToken == null || tableToken.getType() != TokenType.SQL_TOKEN || tableToken.isReservedWord()) {
					return Optional.empty();
				}
				return getTable(config, tableToken.getString());
			}
			return Optional.empty();
		}

		private List<IPointCompletionProposal> getValuesCompletionProposals(DocumentPoint tokenStart,
				@SuppressWarnings("unused") boolean lazy, TokenRange insColsParenthesis) {

			IPartContentAssistProcessor processor = new TextContentAssistProcessor("VALUES",
					() -> new Replacement(buildValues(insColsParenthesis, tokenStart.getReservedCaseFormatter()),
							false),
					"VALUES(...)", () -> "insert values");

			List<IPointCompletionProposal> result = new ArrayList<>();
			processor.computeCompletionProposal(tokenStart).ifPresent(result::add);
			return result;
		}

		private List<String> buildValues(TokenRange insColsParenthesis,
				Function<String, String> reservedCaseFormatter) {
			List<String> result = new ArrayList<>();
			result.add(reservedCaseFormatter.apply("Values ("));
			boolean first = true;
			for (TokenRange range : Token.getInParenthesis(insColsParenthesis.getStart())) {
				Token token = range.getBetweenTokens().filter(t -> t.getType() == TokenType.SQL_TOKEN).findLast()
						.orElse(null);
				if (token != null) {
					result.add((first ? "\t" : ",\t") + buildValuesValue(token));
				} else {
					result.add((first ? "\t" : ",\t") + "/*?*/''");
				}
				first = false;
			}
			result.add(")");
			return result;
		}
	},
	VALUES(ContentAssistProcessors.TOKEN, ContentAssistProcessors.WHITESPACE) {
		final class ValuesTokenSet {
			@SuppressWarnings("unused")
			private final Token valuesToken;
			private final TokenRange colsParenthesis;
			private final Token valuesOpen;

			ValuesTokenSet(Token valuesToken, TokenRange colsParenthesis, Token valuesOpen) {
				this.valuesToken = valuesToken;
				this.colsParenthesis = colsParenthesis;
				this.valuesOpen = valuesOpen;
			}
		}

		@Override
		public List<IPointCompletionProposal> computeCompletionProposals(DocumentPoint tokenStart, boolean lazy,
				PluginConfig config) {

			if (tokenStart.getToken().getType() == TokenType.WHITESPACE) {
				Optional<ValuesTokenSet> valuesTokenSet = getValuesTokenSet(tokenStart);
				if (valuesTokenSet.isPresent()) {
					Optional<Token> pairToken = calcPairToken(valuesTokenSet.get(), tokenStart.getToken());
					if (pairToken.isPresent()) {
						String s = buildValuesValue(pairToken.get());
						return Arrays.asList(new CompletionProposal(
								new DocReplacement(s, tokenStart.point(), 0, OptionalInt.empty(), false), s,
								"insert pair [" + pairToken.get().getString() + "]"));
					}
				}
			}

			return Collections.emptyList();
		}

		private Optional<Token> calcPairToken(ValuesTokenSet valuesTokenSet, Token target) {
			int index = indexOfValue(valuesTokenSet.valuesOpen, target);

			int colIndex = 0;
			for (TokenRange range : Token.getInParenthesis(valuesTokenSet.colsParenthesis.getStart())) {
				if (colIndex == index) {
					return range.getBetweenTokens().filter(t -> t.getType() == TokenType.SQL_TOKEN).findLast();
				}
				colIndex++;
			}
			return Optional.empty();
		}

		private int indexOfValue(Token open, Token target) {
			int index = 0;
			for (TokenRange range : Token.getInParenthesis(open)) {
				if (target.isBefore(range.getStart()) || target.equals(range.getStart())) {
					return index;
				}
				index++;
			}
			return index - 1;
		}

		private Optional<Token> findValuesToken(Token token) {
			while (token != null) {
				Token first = Token.getPrevSiblings(token).findLast().orElse(token);

				Token valuesOpenCand = first.getPrevToken().orElse(null);
				if (valuesOpenCand == null || !Token.isOpenParenthesis(valuesOpenCand)) {
					token = valuesOpenCand;
					continue;
				}

				Token valuesToken = Token.getPrevSiblings(valuesOpenCand).filter(t -> t.getType().isSqlEnable())
						.findFirst().orElse(null);
				if (valuesToken == null || !Token.isValuesWord(valuesToken)) {
					token = valuesOpenCand;
					continue;
				}
				return Optional.of(valuesToken);
			}
			return Optional.empty();
		}

		private Optional<ValuesTokenSet> getValuesTokenSet(DocumentPoint tokenStart) {
			Token valuesToken = findValuesToken(tokenStart.getToken()).orElse(null);
			if (valuesToken == null) {
				return Optional.empty();
			}

			Token valuesOpenCand = Token.getNextSiblings(valuesToken).filter(t -> t.getType().isSqlEnable()).findFirst()
					.orElse(null);
			if (valuesOpenCand == null || !Token.isOpenParenthesis(valuesOpenCand)) {
				return Optional.empty();
		}

			TokenRange colsParenthesis = findInsertParenthesis(valuesToken).orElse(null);
			if (colsParenthesis == null) {
				return Optional.empty();
			}

			return Optional.of(new ValuesTokenSet(valuesToken, colsParenthesis, valuesOpenCand));
		}

	},
	;

	private final Set<ContentAssistProcessors> targetsContentAssistProcessors;

	StatementTypes(ContentAssistProcessors... targets) {
		if (targets.length == 0) {
			targetsContentAssistProcessors = EnumSet.of(ContentAssistProcessors.TOKEN);
		} else {
			targetsContentAssistProcessors = EnumSet.copyOf(Arrays.asList(targets));
					}
				}

	public static Optional<StatementTypes> within(Token token, ContentAssistProcessors contentAssistProcessors) {
		for (Token prev : Token.getPrevSiblingOrParents(token)
				.filter(p -> p.getType() == TokenType.SQL_TOKEN)) {
			for (StatementTypes type : StatementTypes.values()) {
				if (type.targetsContentAssistProcessors.contains(contentAssistProcessors) && type.isToken(prev)) {
					return Optional.of(type);
			}
			}
		}
		return Optional.empty();
	}

	protected boolean isToken(Token token) {
		return token.getString().equalsIgnoreCase(name());
	}

	protected List<IPointCompletionProposal> getTableCompletionProposals(DocumentPoint tokenStart, boolean lazy,
			PluginConfig config) {
		return getTableCompletionProposals(tokenStart, lazy, config, new HashMap<>());
	}

	protected List<IPointCompletionProposal> getTableCompletionProposals(DocumentPoint tokenStart, boolean lazy,
			PluginConfig config, String name, Function<Table, Replacement> buildReplacement) {
		return getTableCompletionProposals(tokenStart, lazy, config, Maps.of(name, buildReplacement));
	}

	protected List<IPointCompletionProposal> getTableCompletionProposals(DocumentPoint tokenStart, boolean lazy,
			PluginConfig config, Map<String, Function<Table, Replacement>> origBuildReplacements) {
		Map<String, Function<Table, Replacement>> buildReplacements = new HashMap<>(origBuildReplacements);
		buildReplacements.put("", table -> {
			return new Replacement(table.toString(), table.getName().length());
		});
		String text = tokenStart.getRangeText();
		Collection<Table> tables = config.getTables(text, lazy);
		if (!lazy) {
			return tables.stream()
					.map(t -> t.createContentAssistProcessor(buildReplacements))
					.flatMap(List::stream)
					.map(p -> p.computeCompletionProposal(tokenStart))
					.filter(o -> o.isPresent())
					.map(o -> o.get())
					.collect(Collectors.toList());
		} else {
			return tables.stream()
					.map(t -> t.createLazyContentAssistProcessor(buildReplacements))
					.flatMap(List::stream)
					.map(p -> p.computeCompletionProposal(tokenStart))
					.filter(o -> o.isPresent())
					.map(o -> o.get())
					.collect(Collectors.toList());
		}
	}
}

package jp.co.future.eclipse.uroborosql.plugin.contentassist.uroborosql.type;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Function;
import java.util.stream.Collectors;

import jp.co.future.eclipse.uroborosql.plugin.config.PluginConfig;
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
import jp.co.future.eclipse.uroborosql.plugin.utils.Pair;
import jp.co.future.eclipse.uroborosql.plugin.utils.Strings;
import jp.co.future.eclipse.uroborosql.plugin.utils.collection.FluentList;
import jp.co.future.eclipse.uroborosql.plugin.utils.collection.Iterators;

public enum StatementTypes implements IType {
	SELECT {
		@Override
		protected List<IPointCompletionProposal> computeTokenCompletionProposals(Token token, boolean lazy,
				PluginConfig config) {
			List<IdentifierReplacement<Table>> buildTables = new ArrayList<>();
			boolean soonNext = Iterators.asIteratorFromNext(token, Token::getPrevToken).stream()
					.filter(t -> t.getType().isSqlEnable()).findFirst().filter(t -> isToken(t)).isPresent();
			if (soonNext) {
				buildTables.add(new IdentifierReplacement<>(table -> "(SEL)" + table, table -> {
					return table.buildSelectSql(token.getDocument().getReservedCaseFormatter());
				}));
			}

			return getTableCompletionProposals(token.toDocumentPoint(), lazy, config, buildTables);
		}

		@Override
		protected List<IPointCompletionProposal> computeColumnCompletionProposals(Table table, Token token,
				boolean lazy, PluginConfig config) {
			return getColumnCompletionProposals(table, token.toDocumentPoint(), lazy,
					createColumnIdentifierReplacement(token.getDocument()));
		}

		@Override
		protected List<IPointCompletionProposal> computeAllColumnCompletionProposals(Table table, DocumentPoint point,
				boolean lazy, PluginConfig config) {

			return getColumnCompletionProposals(table, point, lazy,
					createColumnIdentifierReplacement(point.getDocument()));
		}

		@Override
		protected List<IPointCompletionProposal> computeWhitespaceCompletionProposals(Token token,
				boolean lazy, PluginConfig config) {
			return Collections.emptyList();
		}

		private IdentifierReplacement<Column> createColumnIdentifierReplacement(Document document) {
			return new IdentifierReplacement<>(
					col -> col.buildSelectColumn(0, document.getReservedCaseFormatter()).toString(),
					col -> col.buildSelectColumn(0, document.getReservedCaseFormatter()));
		}

	},
	FROM {
		@Override
		protected List<IPointCompletionProposal> computeTokenCompletionProposals(Token token, boolean lazy,
				PluginConfig config) {
			return getTableCompletionProposals(token.toDocumentPoint(), lazy, config);
		}

		@Override
		protected List<IPointCompletionProposal> computeColumnCompletionProposals(Table table, Token token,
				boolean lazy, PluginConfig config) {
			return Collections.emptyList();
		}

		@Override
		protected List<IPointCompletionProposal> computeAllColumnCompletionProposals(Table table, DocumentPoint point,
				boolean lazy, PluginConfig config) {
			return Collections.emptyList();
		}

		@Override
		protected List<IPointCompletionProposal> computeWhitespaceCompletionProposals(Token token,
				boolean lazy, PluginConfig config) {
			return Collections.emptyList();
		}

	},
	WHERE {
		@Override
		protected List<IPointCompletionProposal> computeTokenCompletionProposals(Token token, boolean lazy,
				PluginConfig config) {
			return Collections.emptyList();
		}

		@Override
		protected List<IPointCompletionProposal> computeColumnCompletionProposals(Table table, Token token,
				boolean lazy, PluginConfig config) {
			return getColumnCompletionProposals(table, token.toDocumentPoint(), lazy,
					createColumnIdentifierReplacement());
		}

		@Override
		protected List<IPointCompletionProposal> computeAllColumnCompletionProposals(Table table, DocumentPoint point,
				boolean lazy, PluginConfig config) {
			return getColumnCompletionProposals(table, point, lazy, createColumnIdentifierReplacement());
		}

		@Override
		protected List<IPointCompletionProposal> computeWhitespaceCompletionProposals(Token token,
				boolean lazy, PluginConfig config) {
			return Collections.emptyList();
		}

		private IdentifierReplacement<Column> createColumnIdentifierReplacement() {
			return new IdentifierReplacement<>(
					col -> col.buildConditionColumn(0).toString(), col -> col.buildConditionColumn(0));
		}
	},
	UPDATE {
		@Override
		protected List<IPointCompletionProposal> computeTokenCompletionProposals(Token token, boolean lazy,
				PluginConfig config) {
			List<IdentifierReplacement<Table>> buildTables = new ArrayList<>();
			boolean soonNext = Iterators.asIteratorFromNext(token, Token::getPrevToken).stream()
					.filter(t -> t.getType().isSqlEnable()).findFirst().filter(t -> isToken(t)).isPresent();
			if (soonNext) {
				buildTables.add(new IdentifierReplacement<>(table -> "(UPD)" + table, table -> {
					return table.buildUpdateSql(token.getDocument().getReservedCaseFormatter());
				}));
			}

			return getTableCompletionProposals(token.toDocumentPoint(), lazy, config, buildTables);
		}

		@Override
		protected List<IPointCompletionProposal> computeColumnCompletionProposals(Table table, Token token,
				boolean lazy, PluginConfig config) {
			return Collections.emptyList();
		}

		@Override
		protected List<IPointCompletionProposal> computeAllColumnCompletionProposals(Table table, DocumentPoint point,
				boolean lazy, PluginConfig config) {
			return Collections.emptyList();
		}

		@Override
		protected List<IPointCompletionProposal> computeWhitespaceCompletionProposals(Token token,
				boolean lazy, PluginConfig config) {
			return Collections.emptyList();
		}

	},
	UPDATE_SET {
		@Override
		protected List<IPointCompletionProposal> computeTokenCompletionProposals(Token token, boolean lazy,
				PluginConfig config) {
			//エイリアスなしのカラム
			Optional<Table> updateTable = getUpdateTable(token, config);
			if (updateTable.isPresent()) {
				return computeColumnCompletionProposals(updateTable.get(), token, lazy, config);
			}

			return Collections.emptyList();
		}

		@Override
		protected List<IPointCompletionProposal> computeColumnCompletionProposals(Table table, Token token,
				boolean lazy, PluginConfig config) {
			return getColumnCompletionProposals(table, token.toDocumentPoint(), lazy,
					createColumnIdentifierReplacement());
		}

		@Override
		protected List<IPointCompletionProposal> computeAllColumnCompletionProposals(Table table, DocumentPoint point,
				boolean lazy, PluginConfig config) {
			return getColumnCompletionProposals(table, point, lazy, createColumnIdentifierReplacement());
		}

		@Override
		protected List<IPointCompletionProposal> computeWhitespaceCompletionProposals(Token token,
				boolean lazy, PluginConfig config) {
			return Collections.emptyList();
		}

		private IdentifierReplacement<Column> createColumnIdentifierReplacement() {
			return new IdentifierReplacement<>(col -> col.buildSetColumn(0).toString(), col -> col.buildSetColumn(0));
		}

		@Override
		protected boolean isToken(Token token) {
			if (!token.getString().equalsIgnoreCase("SET")) {
				return false;
			}
			return Token.getPrevSiblings(token).filter(p -> Token.isUpdateWord(p)).findFirst().isPresent();
		}

		private Optional<Table> getUpdateTable(Token token, PluginConfig config) {
			for (Token setToken : Token.getPrevSiblingOrParents(token)
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
		protected List<IPointCompletionProposal> computeTokenCompletionProposals(Token token, boolean lazy,
				PluginConfig config) {

			boolean soonNext = Iterators.asIteratorFromNext(token, Token::getPrevToken).stream()
					.filter(t -> t.getType().isSqlEnable()).findFirst().filter(t -> isToken(t)).isPresent();
			if (soonNext) {
				List<IdentifierReplacement<Table>> buildTables = new ArrayList<>();
				buildTables.add(new IdentifierReplacement<>(table -> "(INS)" + table, table -> {
					return table.buildInsertSql();
				}));
				return getTableCompletionProposals(token.toDocumentPoint(), lazy, config, buildTables);
			}

			List<IPointCompletionProposal> result = new ArrayList<>();
			//エイリアスなしのカラム
			Optional<Table> insertTable = getInsertTable(token, config);
			if (insertTable.isPresent()) {
				result.addAll(computeColumnCompletionProposals(insertTable.get(), token, lazy, config));
			}

			//VALUES
			TokenRange insColsParenthesis = findInsertParenthesis(token).orElse(null);
			if (insColsParenthesis != null) {
				result.addAll(getValuesCompletionProposals(token, lazy, insColsParenthesis));
			}

			//テーブル
			result.addAll(getTableCompletionProposals(token.toDocumentPoint(), lazy, config));
			return result;
		}

		@Override
		protected List<IPointCompletionProposal> computeColumnCompletionProposals(Table table, Token token,
				boolean lazy, PluginConfig config) {
			return getColumnCompletionProposals(table, token.toDocumentPoint(), lazy,
					createColumnIdentifierReplacement());
		}

		@Override
		protected List<IPointCompletionProposal> computeAllColumnCompletionProposals(Table table, DocumentPoint point,
				boolean lazy, PluginConfig config) {
			return getColumnCompletionProposals(table, point, lazy, createColumnIdentifierReplacement());
		}

		@Override
		protected List<IPointCompletionProposal> computeWhitespaceCompletionProposals(Token token,
				boolean lazy, PluginConfig config) {
			return Collections.emptyList();
		}

		private IdentifierReplacement<Column> createColumnIdentifierReplacement() {
			return new IdentifierReplacement<>(
					col -> col.buildInsertColumn(0).toString(), col -> col.buildInsertColumn(0));
		}

		@Override
		protected boolean isToken(Token token) {
			if (!Token.isIntoWord(token)) {
				return false;
			}
			return Token.getPrevSiblings(token).filter(p -> p.getType().isSqlEnable()).findFirst()
					.filter(p -> Token.isInsertWord(p)).isPresent();
		}

		private Optional<Table> getInsertTable(Token token, PluginConfig config) {
			for (Token intoToken : Token.getPrevSiblingOrParents(token)
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

		private List<IPointCompletionProposal> getValuesCompletionProposals(Token token,
				@SuppressWarnings("unused") boolean lazy, TokenRange insColsParenthesis) {

			IPartContentAssistProcessor processor = new TextContentAssistProcessor("VALUES",
					() -> new Replacement(
							buildValues(insColsParenthesis, token.getDocument().getReservedCaseFormatter()),
							false),
					"VALUES(...)", () -> "insert values");

			DocumentPoint tokenStart = token.toDocumentPoint();
			List<IPointCompletionProposal> result = new ArrayList<>();
			processor.computeCompletionProposal(tokenStart).ifPresent(result::add);
			return result;
		}

		private List<String> buildValues(TokenRange insColsParenthesis,
				Function<String, String> reservedCaseFormatter) {
			List<String> result = new ArrayList<>();
			result.add(reservedCaseFormatter.apply("Values") + " (");

			List<Pair<Token, Token>> ids = Token.getInParenthesis(insColsParenthesis.getStart())
					.map(range -> findInsertidentifier(range).orElse(null)).collect(Collectors.toList());
			int maxWidths = ids.stream().filter(Objects::nonNull).map(id -> buildValuesValueBind(id.getE1()))
					.mapToInt(s -> Strings.widths(s)).max().orElse(0);

			boolean first = true;
			for (Pair<Token, Token> id : ids) {
				if (id != null) {
					result.add((first ? "\t" : ",\t") + buildValuesValueText(id, maxWidths));
				} else {
					result.add((first ? "\t" : ",\t") + "/*?*/''");
				}
				first = false;
			}
			result.add(")");
			return result;
		}
	},
	VALUES {
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
		protected List<IPointCompletionProposal> computeTokenCompletionProposals(Token token, boolean lazy,
				PluginConfig config) {
			return Collections.emptyList();
		}

		@Override
		protected List<IPointCompletionProposal> computeColumnCompletionProposals(Table table, Token token,
				boolean lazy, PluginConfig config) {
			return Collections.emptyList();
		}

		@Override
		protected List<IPointCompletionProposal> computeAllColumnCompletionProposals(Table table, DocumentPoint point,
				boolean lazy, PluginConfig config) {
			return Collections.emptyList();
		}

		@Override
		protected List<IPointCompletionProposal> computeWhitespaceCompletionProposals(Token token,
				boolean lazy, PluginConfig config) {
			Optional<ValuesTokenSet> valuesTokenSet = getValuesTokenSet(token);
			if (valuesTokenSet.isPresent()) {
				Pair<Token, Token> pairToken = calcPairToken(valuesTokenSet.get(), token).orElse(null);
				if (pairToken != null) {
					String s = buildValuesValueText(pairToken, 0);
					return Arrays.asList(new CompletionProposal(
							new DocReplacement(s, token.getDocument().getUserOffset(), 0, OptionalInt.empty(), false),
							s, "insert pair [" + pairToken.getE1().getString() + "]"));
				}
			}
			return Collections.emptyList();
		}

		private Optional<Pair<Token, Token>> calcPairToken(ValuesTokenSet valuesTokenSet, Token target) {
			int index = indexOfValue(valuesTokenSet.valuesOpen, target);

			int colIndex = 0;
			for (TokenRange range : Token.getInParenthesis(valuesTokenSet.colsParenthesis.getStart())) {
				if (colIndex == index) {
					return findInsertidentifier(range);
				}
				colIndex++;
			}
			return Optional.empty();
		}

		private int indexOfValue(Token open, Token target) {
			int index = 0;
			for (TokenRange range : Token.getInParenthesis(open)) {
				if (target.getStart() <= range.getStart().getStart()) {
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

		private Optional<ValuesTokenSet> getValuesTokenSet(Token token) {
			Token valuesToken = findValuesToken(token).orElse(null);
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

	protected Optional<Pair<Token, Token>> findInsertidentifier(TokenRange range) {
		FluentList<Token> tokens = range.getBetweenTokens();

		Token id = tokens.filter(t -> t.getType() == TokenType.SQL_TOKEN).findLast().orElse(null);
		if (id == null) {
			return Optional.empty();
		}
		int commentIndex = tokens.findLastIndex(t -> t.getType() == TokenType.L_COMMENT).orElse(-1);
		if (commentIndex == -1) {
			return Optional.of(new Pair<>(id, null));
		}
		Token comment = tokens.get(commentIndex);
		if (comment == null || comment.getStart() < id.getStart()) {
			return Optional.of(new Pair<>(id, null));
		}
		//コメントより後ろに記述が無いか
		if (tokens.skip(commentIndex).noneMatch(t -> t.getType().isSqlEnable())) {
			return Optional.of(new Pair<>(id, comment));
		}
		return Optional.of(new Pair<>(id, null));

	}

	public static Optional<StatementTypes> within(Token token) {
		for (Token prev : Token.getPrevSiblingOrParents(token)
				.filter(p -> p.getType() == TokenType.SQL_TOKEN)) {
			for (StatementTypes type : StatementTypes.values()) {
				if (type.isToken(prev)) {
					return Optional.of(type);
				}
			}
		}
		return Optional.empty();
	}

	@Override
	public final List<IPointCompletionProposal> computeCompletionProposals(Token token, boolean lazy,
			PluginConfig config) {
		if (token.getType() == TokenType.WHITESPACE) {
			return computeWhitespaceCompletionProposals(token, lazy, config);
		}

		if (Token.isPeriod(token)) {
			//カラムエイリアス有
			Optional<Table> aliasTable = getColumnAtTable(token, config);
			if (aliasTable.isPresent()) {
				return computeAllColumnCompletionProposals(aliasTable.get(),
						token.getDocument().getUserOffsetDocumentPoint(), lazy, config);
			}
		}

		//カラムエイリアス有
		Optional<Table> aliasTable = getColumnAtTable(token, config);
		if (aliasTable.isPresent()) {
			return computeColumnCompletionProposals(aliasTable.get(), token, lazy, config);
		}

		return computeTokenCompletionProposals(token, lazy, config);
	}

	protected abstract List<IPointCompletionProposal> computeTokenCompletionProposals(Token token,
			boolean lazy,
			PluginConfig config);

	protected abstract List<IPointCompletionProposal> computeColumnCompletionProposals(Table table,
			Token token, boolean lazy, PluginConfig config);

	protected abstract List<IPointCompletionProposal> computeAllColumnCompletionProposals(Table table,
			DocumentPoint point,
			boolean lazy, PluginConfig config);

	protected abstract List<IPointCompletionProposal> computeWhitespaceCompletionProposals(Token token,
			boolean lazy,
			PluginConfig config);

	protected boolean isToken(Token token) {
		return token.getString().equalsIgnoreCase(name());
	}

	protected List<IPointCompletionProposal> getTableCompletionProposals(DocumentPoint point, boolean lazy,
			PluginConfig config) {
		return getTableCompletionProposals(point, lazy, config, Collections.emptyList());
	}

	protected List<IPointCompletionProposal> getColumnCompletionProposals(Table table, DocumentPoint point,
			boolean lazy) {
		return getColumnCompletionProposals(table, point, lazy, Collections.emptyList());
	}

	@SafeVarargs
	protected final List<IPointCompletionProposal> getColumnCompletionProposals(Table table, DocumentPoint point,
			boolean lazy, IdentifierReplacement<Column>... buildReplacements) {
		return getColumnCompletionProposals(table, point, lazy, Arrays.asList(buildReplacements));
	}

	protected List<IPointCompletionProposal> getTableCompletionProposals(DocumentPoint point, boolean lazy,
			PluginConfig config, List<IdentifierReplacement<Table>> origBuildReplacements) {
		String text = point.getRangeText();
		return getIdentifiersCompletionProposals(config.getTables(text, lazy), point, lazy, origBuildReplacements);
	}

	protected List<IPointCompletionProposal> getColumnCompletionProposals(Table table, DocumentPoint point,
			boolean lazy, List<IdentifierReplacement<Column>> origBuildReplacements) {
		return getIdentifiersCompletionProposals(table.getColumns(), point, lazy, origBuildReplacements);
	}

	private <I extends IIdentifier<I>> List<IPointCompletionProposal> getIdentifiersCompletionProposals(
			Collection<I> identifiers, DocumentPoint point,
			boolean lazy, List<IdentifierReplacement<I>> origBuildReplacements) {
		List<IdentifierReplacement<I>> buildReplacements = new ArrayList<>(origBuildReplacements);
		buildReplacements.add(new IdentifierReplacement<>(IIdentifier::toReplacement));
		List<IPointCompletionProposal> result = new ArrayList<>();
		for (I identifier : identifiers) {
			for (IPartContentAssistProcessor processor : lazy
					? identifier.createLazyContentAssistProcessor(buildReplacements)
					: identifier.createContentAssistProcessor(buildReplacements)) {
				processor.computeCompletionProposal(point).ifPresent(result::add);
			}
		}
		return result;
	}

	private Optional<Table> getColumnAtTable(Token token, PluginConfig config) {
		Optional<String> name = getAtName(token);
		if (!name.isPresent()) {
			return Optional.empty();
		}
		return findAliasTable(token.getDocument(), name.get(), config);
	}

	protected Optional<Table> getTable(PluginConfig config, String tableName) {
		return config.getTables(tableName, false).get(tableName);
	}

	protected Optional<TokenRange> findInsertParenthesis(Token valuesToken) {

		Token colsCloseCand = Token.getPrevSiblings(valuesToken).filter(t -> t.getType().isSqlEnable()).findFirst()
				.orElse(null);
		if (colsCloseCand == null || !Token.isCloseParenthesis(colsCloseCand)) {
			return Optional.empty();
		}
		Token colsOpenCand = Token.getPrevSiblings(colsCloseCand).filter(t -> t.getType().isSqlEnable()).findFirst()
				.orElse(null);
		if (colsOpenCand == null || !Token.isOpenParenthesis(colsOpenCand)) {
			return Optional.empty();
		}
		Token intoToken = Token.getPrevSiblings(colsOpenCand).filter(p -> Token.isIntoWord(p)).findFirst()
				.orElse(null);
		if (intoToken == null) {
			return Optional.empty();
		}
		Token insToken = Token.getPrevSiblings(intoToken).filter(p -> p.getType().isSqlEnable()).findFirst()
				.orElse(null);

		if (insToken == null || !Token.isInsertWord(insToken)) {
			return Optional.empty();

		}

		return Optional.of(new TokenRange(colsOpenCand, colsCloseCand));
	}

	protected String buildValuesValueBind(Token token) {
		String colname = token.getNormalizeString();
		return "/*" + Strings.toCamel(colname) + "*/''";
	}

	protected String buildValuesValueText(Pair<Token, Token> tokens, int maxWidths) {
		String s = buildValuesValueBind(tokens.getE1());
		int widths = Strings.widths(s);
		if (maxWidths < widths) {
			maxWidths = widths;
		}

		if (tokens.getE2() != null) {
			s = Strings.rightTabs(s, maxWidths) + "\t" + tokens.getE2().getString().trim();
		}
		return s;
	}

	private Optional<String> getAtName(Token token) {
		Iterator<Token> prevs = Iterators.asIteratorFromNext(token, Token::getPrevToken)
				.filter(t -> t.getType().isSqlEnable());
		if (!prevs.hasNext()) {
			return Optional.empty();
		}

		if (!Token.isPeriod(token)) {
			Token period = prevs.next();
			if (!Token.isPeriod(period)) {
				return Optional.empty();
			}
		}
		if (!prevs.hasNext()) {
			return Optional.empty();
		}
		Token at = prevs.next();
		if (at.isReservedWord()) {
			//予約語はエイリアスではない
			return Optional.empty();
		}
		String name;
		if (at.getType() == TokenType.SQL_TOKEN || at.getType() == TokenType.NAME) {
			name = at.getNormalizeString();
		} else {
			return Optional.empty();
		}
		Token pre = at.getPrevToken().orElse(null);
		//エイリアスの前があるのはエイリアスではない
		if (pre != null && pre.getType().isSqlEnable()) {
			return Optional.empty();
		}

		return Optional.of(name);
	}

	private Optional<Table> findAliasTable(Document document, String alias, PluginConfig config) {
		for (Token token : document.getTokens()) {
			if (token.getType() == TokenType.SQL_TOKEN || token.getType() == TokenType.NAME) {
				if (token.getNormalizeString().equals(alias)) {
					Optional<Table> table = getAliasTable(token, config);
					if (table.isPresent()) {
						return table;
					}
				}
			}
		}
		return Optional.empty();
	}

	private Optional<Table> getAliasTable(Token aliasCand, PluginConfig config) {
		Token next = aliasCand.getNextToken().orElse(null);
		if (next != null && next.getType().isSqlEnable()) {
			//次のtokenが有効ならエイリアスではない
			return Optional.empty();
		}
		Token tableName = Iterators.asIteratorFromNext(aliasCand, Token::getPrevToken).stream()
				.filter(t -> t.getType().isSqlEnable())
				.findFirst().orElse(null);
		if (tableName == null) {
			return Optional.empty();
		}
		if (tableName.isReservedWord()) {
			//予約語
			return Optional.empty();
		}
		String name;
		if (tableName.getType() == TokenType.SQL_TOKEN || tableName.getType() == TokenType.NAME) {
			name = tableName.getNormalizeString();
		} else {
			return Optional.empty();
		}
		Token pre = tableName.getPrevToken().orElse(null);
		//テーブル名の前があるのはテーブル名ではない
		if (pre != null && pre.getType().isSqlEnable()) {
			return Optional.empty();
		}
		return getTable(config, name);
	}

}

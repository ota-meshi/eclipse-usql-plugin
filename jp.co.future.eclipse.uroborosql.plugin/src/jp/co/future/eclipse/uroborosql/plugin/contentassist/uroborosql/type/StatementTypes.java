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
			return within(token).filter(UPDATE::equals).isPresent();
		}
	},
	INSERT_INTO {
		@Override
		public List<IPointCompletionProposal> computeCompletionProposals(DocumentPoint tokenStart, boolean lazy,
				PluginConfig config) {
			int a;
			//TODO COLS

			Map<String, Function<Table, Replacement>> buildTables = new HashMap<>();

			boolean soonNext = Iterators.stream(Iterators.asIterator(tokenStart.getToken(), Token::getPrevToken))
					.filter(t -> t.getType().isSqlEnable())
					.findFirst()
					.filter(t -> isToken(t))
					.isPresent();
			if (soonNext) {
				buildTables.put("(INS)", table -> {
					return table.buildInsertSql(tokenStart.getReservedCaseFormatter());
				});
			}

			return getTableCompletionProposals(tokenStart, lazy, config, buildTables);
		}

		@Override
		protected boolean isToken(Token token) {
			if (!token.getString().equalsIgnoreCase("INTO")) {
				return false;
			}
			return Token.getPrevSibling(token).filter(t -> t.getString().equalsIgnoreCase("INSERT")).isPresent();
		}
	},
	VALUES {
		@Override
		public List<IPointCompletionProposal> computeCompletionProposals(DocumentPoint tokenStart, boolean lazy,
				PluginConfig config) {
			int a;
			return Collections.emptyList();//TODO
		}
	},
	;

	public static Optional<StatementTypes> within(Token token) {
		Token target = token;
		while (true) {
			for (Token prev : Token.getPrevSiblings(target)) {
				if (prev.getType() == TokenType.SQL_TOKEN) {
					for (StatementTypes type : StatementTypes.values()) {
						if (type.isToken(prev)) {
							return Optional.of(type);
						}
					}
				}
				target = prev;
			}
			Optional<Token> parent = target.getPrevToken();
			if (!parent.isPresent()) {
				break;
			}
			target = parent.get();
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

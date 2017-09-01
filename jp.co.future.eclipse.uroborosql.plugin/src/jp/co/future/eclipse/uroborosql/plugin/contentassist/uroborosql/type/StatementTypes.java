package jp.co.future.eclipse.uroborosql.plugin.contentassist.uroborosql.type;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import jp.co.future.eclipse.uroborosql.plugin.config.PluginConfig;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.uroborosql.data.identifiers.Table;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.DocumentPoint;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.contentassist.IPointCompletionProposal;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.contentassist.Replacement;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.parser.Token;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.parser.TokenType;
import jp.co.future.eclipse.uroborosql.plugin.utils.Iterators;
import jp.co.future.eclipse.uroborosql.plugin.utils.Maps;

public enum StatementTypes implements IType {
	SELECT {
		@Override
		public List<IPointCompletionProposal> computeCompletionProposals(DocumentPoint tokenStart, boolean lazy,
				PluginConfig config) {
			//TODO カラム
			int a;

			Map<String, Function<Table, Replacement>> buildTables = new HashMap<>();
			boolean soonNext = Iterators.stream(Iterators.asIterator(tokenStart.getToken(), Token::getPrevToken))
					.filter(t -> t.getType().isSqlEnable())
					.findFirst()
					.filter(t -> isToken(t))
					.isPresent();
			if (soonNext) {
				buildTables.put("(SEL)", table -> {
					return table.buildSelectSql(tokenStart.getReservedCaseFormatter());
				});
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
			int a;
			return Collections.emptyList();//TODO
		}
	},
	UPDATE {
		@Override
		public List<IPointCompletionProposal> computeCompletionProposals(DocumentPoint tokenStart, boolean lazy,
				PluginConfig config) {
			Map<String, Function<Table, Replacement>> buildTables = new HashMap<>();
			boolean soonNext = Iterators.stream(Iterators.asIterator(tokenStart.getToken(), Token::getPrevToken))
					.filter(t -> t.getType().isSqlEnable())
					.findFirst()
					.filter(t -> isToken(t))
					.isPresent();
			if (soonNext) {
				buildTables.put("(UPD)", table -> {
					return table.buildUpdateSql(tokenStart.getReservedCaseFormatter());
				});
			}

			return getTableCompletionProposals(tokenStart, lazy, config, buildTables);
		}
	},
	UPDATE_SET {
		@Override
		public List<IPointCompletionProposal> computeCompletionProposals(DocumentPoint tokenStart, boolean lazy,
				PluginConfig config) {
			int a;
			return Collections.emptyList();//TODO
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

package jp.co.future.eclipse.uroborosql.plugin.contentassist.util.parser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.Document;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.DocumentScanner;

public class SqlParser {

	public static List<Token> parse(Document document) {
		List<Token> result = new ArrayList<>();

		DocumentScanner scanner = new DocumentScanner(document);
		while (scanner.hasNext()) {
			char c = scanner.next();
			TokenType type = TokenType.startOf(c, scanner);
			TokenImpl token = new TokenImpl(document, scanner.index(), type);

			type.scanEnd(scanner);
			token.setEnd(scanner.index());

			result.add(token);
		}
		return result;
	}

	public static Collection<IdentifierNode> parseIdentifiers(List<Token> tokens) {
		IdentifierNode root = new IdentifierNode(null, null);

		Deque<Token> tgtTokens = tokens.stream()
				.filter(t -> t.getType().isSqlEnable())
				.collect(Collectors.toCollection(LinkedList::new));

		while (!tgtTokens.isEmpty()) {
			Token token = tgtTokens.poll();
			if (token.getType() == TokenType.SQL_TOKEN) {
				IdentifierNode node = root.add(token);
				while (!tgtTokens.isEmpty()) {
					Token token2 = tgtTokens.poll();
					if (token2.getType() == TokenType.SYMBOL && token2.getString().equals(".")) {
						if (!tgtTokens.isEmpty()) {
							Token token3 = tgtTokens.poll();
							if (token3.getType() == TokenType.SQL_TOKEN) {
								node = node.add(token3);
								continue;//次のNode
							}
							tgtTokens.addFirst(token3);//戻す
						}
					}
					tgtTokens.addFirst(token2);//戻す
					break;
				}
			}
		}

		return root.children();
	}
}

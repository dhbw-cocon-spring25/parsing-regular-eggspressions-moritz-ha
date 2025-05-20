package de.dhbw.mh.redeggs;

import java.util.HashSet;
import java.util.Set;

/**
 * A parser for regular expressions using recursive descent parsing.
 * This class is responsible for converting a regular expression string into a
 * tree representation of a {@link RegularEggspression}.
 */
public class RecursiveDescentRedeggsParser {

	/**
	 * The symbol factory used to create symbols for the regular expression.
	 */
	protected final SymbolFactory symbolFactory;

	/**
	 * Constructs a new {@code RecursiveDescentRedeggsParser} with the specified
	 * symbol factory.
	 *
	 * @param symbolFactory the factory used to create symbols for parsing
	 */
	public RecursiveDescentRedeggsParser(SymbolFactory symbolFactory) {
		this.symbolFactory = symbolFactory;
	}

	/**
	 * Parses a regular expression string into an abstract syntax tree (AST).
	 * 
	 * This class uses recursive descent parsing to convert a given regular
	 * expression into a tree structure that can be processed or compiled further.
	 * The AST nodes represent different components of the regex such as literals,
	 * operators, and groups.
	 *
	 * @param regex the regular expression to parse
	 * @return the {@link RegularEggspression} representation of the parsed regex
	 * @throws RedeggsParseException if the parsing fails or the regex is invalid
	 */

	public int pos = 0;
	public String regex_string;

	Set<Character> specialChars = new HashSet<>();

	public char lexerPeek(){
		return regex_string.charAt(pos);
	};
	public void lexerConsume(){
		pos++;
	};

	public void lexerExpect(char charCharBinks) throws RedeggsParseException {
		if (lexerPeek() != charCharBinks){
			String exceptionMessage = "Parse exception. You moron. Expected: " + charCharBinks;
			throw new RedeggsParseException(exceptionMessage, pos);
		} else {
			lexerConsume();
		}
	}

	public boolean isLiteral(char charCharBinks){
		return !specialChars.contains(charCharBinks);
	};


	public RegularEggspression regex() throws RedeggsParseException {
		if (isLiteral(lexerPeek()) || lexerPeek() == '(' || lexerPeek() == '['){
			RegularEggspression concat = concat();
			return union(concat);
		} else {
			throw new RedeggsParseException("Parse exception. You moron. Expected: Literal, ( or [", pos);
		}
	}

	public RegularEggspression union(RegularEggspression left) throws RedeggsParseException {
		if (lexerPeek() == '|'){
			lexerConsume();
			RegularEggspression right = concat();
			return new RegularEggspression.Alternation(left, union(right));
		} else if (lexerPeek() == '$' || lexerPeek() == ')') {
			return left;
		} else {
			throw new RedeggsParseException("Parse exception. You moron. Expected: |, $ or )", pos);
		}
	}

	public RegularEggspression concat() throws RedeggsParseException {
		if (isLiteral(lexerPeek()) || lexerPeek() == '(' || lexerPeek() == '['){
			RegularEggspression kleene = kleene();
			return suffix(kleene);
		} else {
			throw new RedeggsParseException("Parse exception. You moron. Expected: Literal, ( or [", pos);
		}
	}

	public RegularEggspression suffix(RegularEggspression left) throws RedeggsParseException {
		if (isLiteral(lexerPeek()) || lexerPeek() == '(' || lexerPeek() == '['){
			RegularEggspression k = kleene();
			return new RegularEggspression.Concatenation(left, suffix(k));
		} else if (lexerPeek() == '|' || lexerPeek() == '$' || lexerPeek() == ')') {
			return left;
		} else {
			throw new RedeggsParseException("Parse exception. You moron. Expected: Literal, (, [, |, $ or )", pos);
		}
	}

	public RegularEggspression kleene() throws RedeggsParseException {
		if (isLiteral(lexerPeek()) || lexerPeek() == '(' || lexerPeek() == '['){
			RegularEggspression base = base();
			return star(base);
		} else {
			throw new RedeggsParseException("Parse exception. You moron. Expected: Literal, ( or [", pos);
		}
	}

	public RegularEggspression star(RegularEggspression base) throws RedeggsParseException {
		if (lexerPeek() == '*'){
			lexerConsume();
			return new RegularEggspression.Star(base);
		} else if (isLiteral(lexerPeek()) || lexerPeek() == '(' || lexerPeek() == '[' || lexerPeek() == '$' || lexerPeek() == ')' || lexerPeek() == '|') {
			return base;
		} else {
		throw new RedeggsParseException("Parse exception. You moron. Expected: *, |, $ or )", pos);
		}
	}

	public RegularEggspression base() throws RedeggsParseException {
		if (isLiteral(lexerPeek())){
			VirtualSymbol v = symbolFactory.newSymbol().include(CodePointRange.single(lexerPeek())).andNothingElse();
			lexerConsume();
			return new RegularEggspression.Literal(v);
		} else if (lexerPeek() == '('){
			lexerConsume();
			RegularEggspression r = regex();
			lexerExpect(')');
			return r;
		} else if (lexerPeek() == '['){
			lexerConsume();
			boolean neg = negation();
			SymbolFactory.Builder inhalt = inhalt(symbolFactory.newSymbol(), neg);
			SymbolFactory.Builder range = range(inhalt, neg);
			lexerExpect(']');
			return new RegularEggspression.Literal(range.andNothingElse());
		} else {
			throw new RedeggsParseException("Parse exception. You moron. Expected: ( or [", pos);
		}
	}

	private boolean negation() throws RedeggsParseException {
		if (lexerPeek() == '^') {
			lexerConsume(); // consume '^'
			return true;
		} else if (isLiteral(lexerPeek())) {
			return false;
		}
		throw new RedeggsParseException("Unexpected symbol '" + lexerPeek() + "' at position " + pos + ".", pos);
	}

	public SymbolFactory.Builder range(SymbolFactory.Builder builder, boolean neg) throws RedeggsParseException {
		if (isLiteral(lexerPeek())) {
			SymbolFactory.Builder inhalt = inhalt(builder, neg);
			return range(inhalt, neg);
		} else if (lexerPeek() == ']') {
			return builder;
		}
		else {
			throw new RedeggsParseException("Parse exception. You moron. Expected: Literal or ]", pos);
		}
	}

	public SymbolFactory.Builder inhalt(SymbolFactory.Builder builder, boolean neg) throws RedeggsParseException {
		char select = lexerPeek();
		if (isLiteral(select)) {
			lexerConsume(); // consume literal
			CodePointRange rest = rest(select);
			if (neg) {
				return builder.exclude(rest);
			} else {
				return builder.include(rest);
			}
		} {
			throw new RedeggsParseException("Parse exception. You moron. Expected: Literal", pos);
		}
	}

	public CodePointRange rest(char c) throws RedeggsParseException {
		char select = lexerPeek();
		if (select == '-') {
			lexerConsume(); //consume '-'
			char consumed = lexerPeek();
			lexerConsume(); //consume upper limit of range
			if(!isLiteral(consumed)){
				throw new RedeggsParseException(
						"Input ended unexpectedly, expected literal at position " + pos + ".",
						pos);
			}
			return CodePointRange.range(c, consumed);
		} else if (isLiteral(lexerPeek()) || lexerPeek() == ']') {
			return CodePointRange.single(c);
		}
		throw new RedeggsParseException("Unexpected symbol '" + lexerPeek() + "' at position " + pos  + ".", pos);
	}

	public RegularEggspression parse(String regex) throws RedeggsParseException {

		this.regex_string = regex + "$";

		specialChars.add('(');
		specialChars.add('[');
		specialChars.add(']');
		specialChars.add(')');
		specialChars.add('$');
		specialChars.add('*');
		specialChars.add('|');

		if (this.regex_string.length() == 2) {
			if (lexerPeek() == 'ε') {
				return new RegularEggspression.EmptyWord();
			} else if (lexerPeek() == '∅') {
				return new RegularEggspression.EmptySet();
			}
		}

		RegularEggspression parsed_regex = regex();

		if (lexerPeek() != '$') {
			throw new RedeggsParseException("Unexpected symbol '" + lexerPeek() + "' at position " + pos + ".",pos);
		}

		return parsed_regex;
	}
}

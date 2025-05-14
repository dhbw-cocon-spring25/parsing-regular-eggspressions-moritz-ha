package de.dhbw.mh.redeggs;

import java.util.HashSet;
import java.util.Set;

import static de.dhbw.mh.redeggs.CodePointRange.range;
import static de.dhbw.mh.redeggs.CodePointRange.single;

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
	public String regex;

	Set<Character> specialChars = new HashSet<>();

	public char lexerPeek(){
		return regex.charAt(pos);
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
	};

	public boolean isLiteral(char charCharBinks){
		return !specialChars.contains(charCharBinks);
	};


	public void regex() throws RedeggsParseException {
		if (isLiteral(lexerPeek()) || lexerPeek() == '(' || lexerPeek() == '['){
			concat();
			union();
		} else {
			throw new RedeggsParseException("Parse exception. You moron. Expected: Literal, ( or [", pos);
		}
	}

	public void union() throws RedeggsParseException {
		if (lexerPeek() == '|'){
			lexerConsume();
			concat();
		} else if (lexerPeek() == '$' || lexerPeek() == ')') {
			return;
		} else {
			throw new RedeggsParseException("Parse exception. You moron. Expected: |, $ or )", pos);
		}
	}

	public void concat() throws RedeggsParseException {
		if (isLiteral(lexerPeek()) || lexerPeek() == '(' || lexerPeek() == '['){
			kleene();
			suffix();
		} else {
			throw new RedeggsParseException("Parse exception. You moron. Expected: Literal, ( or [", pos);
		}
	}

	public void suffix() throws RedeggsParseException {
		if (isLiteral(lexerPeek()) || lexerPeek() == '(' || lexerPeek() == '['){
			kleene();
			suffix();
		} else if (lexerPeek() == '|' || lexerPeek() == '$' || lexerPeek() == ')') {
			return;
		} else {
			throw new RedeggsParseException("Parse exception. You moron. Expected: Literal, (, [, |, $ or )", pos);
		}
	}

	public void kleene() throws RedeggsParseException {
		if (isLiteral(lexerPeek()) || lexerPeek() == '(' || lexerPeek() == '['){
			base();
			star();
		} else {
			throw new RedeggsParseException("Parse exception. You moron. Expected: Literal, ( or [", pos);
		}
	}

	public void star() throws RedeggsParseException {
		if (lexerPeek() == '*'){
			lexerConsume();
		} else if (lexerPeek() == '|' || lexerPeek() == '$' || lexerPeek() == ')') {
			return;
		} else {
		throw new RedeggsParseException("Parse exception. You moron. Expected: *, |, $ or )", pos);
		}
	}

	public void base() throws RedeggsParseException {
		if (isLiteral(lexerPeek())){
			lexerConsume();
		} else if (lexerPeek() == '('){
			lexerConsume();
			regex();
			lexerExpect(')');
		} else if (lexerPeek() == '['){
			lexerConsume();
			inhalt();
			range();
			lexerExpect(']');
		} else {
			throw new RedeggsParseException("Parse exception. You moron. Expected: ( or [", pos);
		}
	}

	public void range() throws RedeggsParseException {
		if (isLiteral(lexerPeek())){
			inhalt();
			range();
		} else if (lexerPeek() == ']') {
			return;
		} else {
			throw new RedeggsParseException("Parse exception. You moron. Expected: Literal or ]", pos);
		}
	}

	public void inhalt() throws RedeggsParseException {
		if (isLiteral(lexerPeek())){
			lexerConsume();
			rest();
		} else {
			throw new RedeggsParseException("Parse exception. You moron. Expected: Literal", pos);
		}
	}

	public void rest() throws RedeggsParseException {
		if (lexerPeek() == '-'){
			lexerConsume();
			lexerConsume();
		} else if (isLiteral(lexerPeek()) || lexerPeek() == ']') {
			return;
		} else {
			throw new RedeggsParseException("Parse exception. You moron. Expected: -,Literal or ]", pos);
		}
	}

	public RegularEggspression parse(String regex) throws RedeggsParseException {

		this.regex = regex + "$";

		specialChars.add('(');
		specialChars.add('[');
		specialChars.add(']');
		specialChars.add(')');
		specialChars.add('$');
		specialChars.add('*');
		specialChars.add('|');

		if (this.regex.length() == 1) {
			if (lexerPeek() == 'ε') {
				return new RegularEggspression.EmptyWord();
			} else if (lexerPeek() == '∅') {
				return new RegularEggspression.EmptySet();
			}
		}

		//todo "Lexer" schreiben -> peek, consume, expect
		//function call für startsymbol

		regex();

		//"eps" oder "emptyset" testen --> falls ja, return new RegularEggspression.EmptySet();

		// Return a dummy Literal RegularExpression for now
		return new RegularEggspression.
	}
}

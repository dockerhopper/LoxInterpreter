package com.craftinginterpreters.lox;
import static com.craftinginterpreters.lox.TokenType.*;

//Fun little datatypes
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Scanner {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private static final Map<String, TokenType> keywords;
    static {
        keywords = new HashMap<>();
        keywords.put("and", AND);
        keywords.put("class", CLASS);
        keywords.put("else", ELSE);
        keywords.put("false", FALSE);
        keywords.put("for", FOR);
        keywords.put("fun", FUN);
        keywords.put("if", IF);
        keywords.put("nil", NIL);
        keywords.put("or", OR);
        keywords.put("print", PRINT);
        keywords.put("return", RETURN);
        keywords.put("super", SUPER);
        keywords.put("this", THIS);
        keywords.put("true", TRUE);
        keywords.put("var", VAR);
        keywords.put("while", WHILE);
    }
    /*
     * start/current fields are offsets that index into the string
     * start field points ot the first char in the lexeme being scanned
     * current will point at hte character currently being considered
     * line tracks what source line current is on, so we can produce tokens that know their location
     */
    private int start = 0;
    private int current = 0;
    private int line = 1;

    //Store raw code as simple string
    Scanner(String source){
        this.source = source;
    }

    /*
     *  have a list ready to fill with tokens we're going to generate.
     * This scanner works through the source code, adding tokens until it runs out of characters
     * Then it appends one final EOF token (end of file), not entirely needed but it makes our parser
     * a little cleaner
     */
    List<Token> scanTokens() {
        while (!isAtEnd()) {
        // We are at the beginning of the next lexeme.
            start = current;
            scanToken();
        }
        tokens.add(new Token(EOF, "", null, line));
        return tokens;
    }

    //helper func to tell if we've consumed all characters in a token
    private boolean isAtEnd() {
        return current >= source.length();    
    }

    //Identifying individual tokens grabbed by our scanTokens() method
    private void scanToken(){
        char c = advance();
        switch(c){
            case '(': addToken(LEFT_PAREN); break;
            case ')': addToken(RIGHT_PAREN); break;
            case '{': addToken(LEFT_BRACE); break;
            case '}': addToken(RIGHT_BRACE); break;
            case ',': addToken(COMMA); break;
            case '.': addToken(DOT); break;
            case '-': addToken(MINUS); break;
            case '+': addToken(PLUS); break;
            case ';': addToken(SEMICOLON); break;
            case '*': addToken(STAR); break;
            case '!':
                addToken(match('=') ? BANG_EQUAL : BANG);
                break;
            case '=':
                addToken(match('=') ? EQUAL_EQUAL : EQUAL);
                break;
            case '<':
                addToken(match('=') ? LESS_EQUAL : LESS);
                break;
            case '>':
                addToken(match('=') ? GREATER_EQUAL : GREATER);
                break;
            //This is for division, requires some handling since it's '/' (comments start with //)
            /*
             * This is similar to the other two-character operators, except that when we find a
             *  second /, we don’t end the token yet. Instead, we keep consuming characters
             *  until we reach the end of the line
             */
            //General strat for handling longer lexemes, after we detect the beginning of one
            //we shunt over to some lexeme-specific code that keeps eating characters until it's at end
            case '/':
                if (match('/')) {
                // A comment goes until the end of the line.
                    while (peek() != '\n' && !isAtEnd()) advance();
                } else {
                    addToken(SLASH);
                }
                break;
            case 'o':
                if (peek() == 'r') {
                    addToken(OR);
                }
                break;
            case ' ':
            case '\r':
            case '\t':
            // Ignore whitespace.
            /*
             * When encountering whitespace, go back to beginning of scan
             * that starts new lexeme after the whitespace char, for newlines, do same thing but incremenet line
             */
            break;
            case '\n':
                line++;
                break;

            //for tackling literals!
            case '"': string(); break;
            
            /*What happens if a user throws a source file containing some characters
            *Lox doesn’t use, like @#^ at our interpreter */
            //This is covering that case.
            default:
            //Would be very tedious to add a case for every decimal digit lol
            if(isDigit(c)){
                number();
            }else if(isAlpha(c)){
                identifier();
            }else {
                Lox.error(line, "Unexpected character.");
                break;
            }
        }
    }
    /*
     * similar with comments, consumed chars till " that ends string
     * also handle running out of input before the string is closed and report an error
     * Oddly, this language Lox, supports mult-line strings. means don't need to update line when we hit a newline inside a string
     * 
     * when creating a token, also produce the actual value that will be used later by the interpreter. here that conversion only requires
     * a substring() to strip off the surrounding quotes.
     */
    private void string() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') line++;
            advance();
        }
        if (isAtEnd()) {
            Lox.error(line, "Unterminated string.");
            return;
        }
        // The closing ".
        advance();

        // Trim the surrounding quotes.
        String value = source.substring(start + 1, current - 1);
        addToken(STRING, value);
    }

    //After scanning an identifier, check to see if it matches anything in the map (our no no list)
    //If so, we use that keyword's token type. Otherwise, it's a regular use-defined indentifier.
    private void identifier() {
        while (isAlphaNumeric(peek())) advance();
        String text = source.substring(start, current);
        TokenType type = keywords.get(text);
        if (type == null) type = IDENTIFIER;
        addToken(type);
    }

    //help for above indentifier()
    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') ||
        (c >= 'A' && c <= 'Z') ||
        c == '_';
    }
    private boolean isAlphaNumeric(char c){
        return isAlpha(c) || isDigit(c);
    }


    /*
     * Very much acting like a conditional advance.
     * We only consume the current character if it's what we're looking for
     */
    /*
     * Using match(), we recognize these lexemes in two stages, when we reach,for example,!
     * we jumpt to its switch case. That means we know the lexeme starts with !. Then we look
     * at the next character to determine if we're on a != or a !. do different lexemes.
     */
    private boolean match(char expected) {
        if (isAtEnd()) return false;
        if (source.charAt(current) != expected) return false;
        current++;
        return true;
    }

    //Sort of like advance, but doesn't consume the character. lookahead, since it only looks at the current unconsumed char
    //we have one char lookahead. smaller # faster scanner will run.
    //Usually the rules of lexical grammar dictate how much lookahead we need.
    //Fortunately most languages only use peek one or two chars ahead;
    private char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }

    //Very simple method
    private boolean isDigit(char c){
        return c>='0' && c<='9'; //cute little cosc111 callback 
    }

    /*consume as many digits as we find for the integer part of the literal. then look for a fractional part
     * which is the decimal point(.) followed by at least one digit. if we do have a fractional part again, 
     */
    private void number() {
        while (isDigit(peek())) advance();
        // Look for a fractional part.
        if (peek() == '.' && isDigit(peekNext())) {
            // Consume the "."
            advance();
            while (isDigit(peek())) advance();
        }
        addToken(NUMBER,
        Double.parseDouble(source.substring(start, current)));
    }

    /*
     * Looking past the decimal point requires a second character of lookahead since we don't want to consume the . until we're sure
     * there is a digit after it, so we add:
     * 
     * Final we convert the lexeme to its numberic value. Our interperter uses Java's Double type ot represent numbers, so we 
     * produce a value of that type. Using java's own parsin method to conver the lexeme to a real java double. could do that
     * but i'm feeling REAL lazy rn as of 25-01-30/.
     */
    private char peekNext() {
        if (current + 1 >= source.length()) return '\0';
        return source.charAt(current + 1);
    }

    //Consumes next character in the source file and returns it. For input
    private char advance() {
        current++;
        return source.charAt(current - 1);
    }
    private void addToken(TokenType type) {
        addToken(type, null);
    }
    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }
}

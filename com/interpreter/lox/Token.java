package com.interpreter.lox;

public class Token {
    /*
     * This is the basis for tracking our errors, error handling basically
     * In our simple interpreter, we only note which line the token appears on
     * More sophisticated implementations include hte column and length too.
     */
    final TokenType type;
    final String lexeme;
    final Object literal;
    final int line;

    Token(TokenType type, String lexeme, Object literal, int line) {
        this.type = type;
        this.lexeme = lexeme;
        this.literal = literal;
        this.line = line;
        }
        public String toString() {
        return type + " " + lexeme + " " + literal;
        }
}

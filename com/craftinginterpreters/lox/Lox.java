package com.craftinginterpreters.lox;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Lox {
    //No error at startup hopefully
    static boolean hadError = false;
    //Before scanning, this is the basic outline of our interpreter, jlox;
    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            System.out.println("Usage: jlox [script]");
            System.exit(64);
        } else if (args.length == 1) {
            runFile(args[0]);
        } else {
            runPrompt();
        }
    }
    //Since jlox is a scripting language, it run directly from the source
    /*
     * Our interpreter supports two ways of running code, starting from the command line, giving a filepath
     * it reads the file and executes it.
     */
    private static void runFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()));
        // Indicate an error in the exit code.
        if(hadError) System.exit(65); //very useful, terminates program with a known error.
        //exit with a non-zero exit code.
    }
    /*
     * You could also run it interactively, running jlox without arguments, dropping you in a prompt
     * where you can execute code one line at a time
     */


    private static void runPrompt() throws IOException {
        //Whole function will just read a line
        //Running into a line == null will break the loop.
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);
        for (;;) {
            System.out.print("> ");
            //reads a line of input from the user on cmd. returns the result.
            //Usually. to kill interactive command-line app, type Control-D. Doing so signals an "end of file"
            //condition to the program, readLine() will then return null
            String line = reader.readLine();
            if (line == null) break;
            run(line);

            //If user makes mistake, won't terminate the entire session.
            //Would be funny tho.
            hadError=false;
        }
    }
    //Both prompt and the file runner are thin wrapper around this core function
    private static void run(String source) {
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();
        // For now, just print the tokens.
        for (Token token : tokens) {
            System.out.println(token);
        }
    }
    /*
     * Note this isn't useful yet since I haven't written an interpreter as of 25-01-30
     */

     /***ERROR HANDLING***/
     //Want a usuable language --> need something to blame the user for
     //Users don't think about the code, they think about their program!

     //Telling user there is an error, certain line of error and message (typeoferror)
     static void error(int line, String message){
        report(line, "",message);
     }
     //Following a standard reporting system
     private static void report(int line, String where, String message){
        System.err.println("[line " + line +"] Error" + where+ ": " +message);
        hadError = true;
     }
     //Practically, this will tell the user the line, the type, and where the error is occuring, "YOU FORGOT A SEMICOLON HERE"
}
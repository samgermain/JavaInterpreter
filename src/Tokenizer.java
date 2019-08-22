

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Sam
 */
public class Tokenizer implements ITokenizer {
    
    private Scanner scanner = null;	//Goes over all characters in the program
    private Lexeme current = null;	//The current character being looked at
    private Lexeme next = null;		//The next character being looked at
    public Lexeme origNext = null;	//The beginning lexeme of the tokenizer
    
    public Scanner scanner() {
    		return scanner;
    }
    
    /**
     * Goes back to the start of the tokenizer
     */
    public void goOrigNext() {
    		current = null;
    		next = origNext;
    }
    
    /**
	 * Opens a file for tokenizing.
	 */
    // edited from throws prop.assignment0.TokenizerException
	public void open(String fileName) throws IOException, TokenizerException{
            scanner = new Scanner();
            scanner.open(fileName);
            
            scanner.moveNext();
            next = extractLexeme();
            origNext = next;
        }
	
	public Tokenizer() {
		
	}
	
	/**
	 * Returns the current token in the stream.
	 */
	public Lexeme current(){
            return current;
        }
        
        public Lexeme next(){
            return next;
        }

        private void consumeWhiteSpaces() throws IOException {
        	while (Character.isWhitespace(scanner.current())){
        	    scanner.moveNext();
        	}}
        
        //turns a character into a lexeme
        private Lexeme extractLexeme() throws IOException, TokenizerException {
            consumeWhiteSpaces();
            
            Character ch = scanner.current();

            StringBuilder builder = new StringBuilder();
            
            
            if (ch == Scanner.EOF){
                return new Lexeme(ch, Token.EOF);
            }else if (Character.isLetter(ch)) { //string of letters become lexeme identifiers
                while (Character.isLetter(scanner.current())){ 
                    builder.append(scanner.current());
                    scanner.moveNext();
                    
                }
	            return new Lexeme(builder.toString(), Token.IDENT);
            } else if (Character.isDigit(ch)) {
                while (Character.isDigit(scanner.current())) {	//Strings of digits are pieced together and not processed by the individual digit
                    builder.append(scanner.current());
                    scanner.moveNext();
                }
                return new Lexeme(builder.toString(), Token.INT_LIT);
            }
           scanner.moveNext();
           //All other lexems are created by checking what the character is specifically
            if (ch == '{' ){
                return new Lexeme('{', Token.LEFT_CURLY);
            } else if (ch == '}' ){
                return new Lexeme('}', Token.RIGHT_CURLY);
            } else if (ch == '(' ){
                return new Lexeme('(', Token.LEFT_PAREN);
            } else if (ch == ')' ){
                return new Lexeme(')', Token.RIGHT_PAREN);
            } else if (ch == '+'){
                return new Lexeme('+', Token.ADD_OP);
            } else if (ch == '-'){
                return new Lexeme('-', Token.SUB_OP);
            } else if (ch == '*'){
                return new Lexeme('*', Token.MULT_OP);
            } else if (ch == '/'){
                return new Lexeme('/', Token.DIV_OP);
            } else if (ch == '='){
                return new Lexeme('=', Token.ASSIGN_OP);
            } else if (ch == ';'){
                return new Lexeme(';', Token.SEMICOLON);
            } else if (ch == null ){
                return new Lexeme(null, Token.NULL);
            } else {
            		//No other characters are receiver by this tokenizer
                throw new TokenizerException("Unknown character: " + String.valueOf(ch));
            }
        }
        
        
	/**
	 * Moves current to the next token in the stream.
	 */
	public void moveNext() throws IOException, TokenizerException{
		if (scanner == null)
			throw new IOException("You have not opened a file.");
		current = next;
        if (next.token() != Token.EOF){
                next = extractLexeme();
        }
        }

	/**
	 * Closes the file and releases any system resources associated with it.
	 */
	public void close() throws IOException{
            if (scanner != null){
                scanner.close();
            }else{
                throw new IOException("You have not opened a file.");
           }
        }
}

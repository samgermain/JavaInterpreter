/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.IOException;
import javax.script.ScriptEngineManager;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

/**
 *
 * @author Sam
 */
public class Parser implements IParser {
    
        public static Tokenizer t = null;
        public static StringBuilder builder = new StringBuilder();
        public static Object[] obArray = new Object[20];
        
        
	/**
	 * Opens a file for parsing.
	 */
	public void open(String fileName) throws IOException, TokenizerException{
            try {
                t = new Tokenizer();			
                t.open(fileName);
                t.moveNext();
            } catch (Exception e){
                System.out.println(e);
            }
        }
	
	/**
	 * Parses a program from file returning a parse tree (the root node of a parse tree).
	 */
//	public INode parse() throws IOException, TokenizerException, ParserException{
        public INode parse() throws IOException, TokenizerException, ParserException{
            if (t == null)
            	throw new IOException("No open file.");

	    return new BlockNode(t);
        }       
        
        /**
         * Represent a block of code enclosed by curly brackets
         * @author Sam
         *
         */
        class BlockNode implements INode {
            Lexeme l = null;		//Represents the first lexeme which should be a left curly
            StatementsNode sNode = null;	//Represents all the assignment statements which happen within the block
            Lexeme r = null;		//Represents the last lexeme which should be a right curly
            
            /**
             * Constructor for a block node
             * @param t The tokenizer containing all the lexemes of the program sent in
             */
            public BlockNode(Tokenizer t) {
                try {
                    l = t.current();
                    t.moveNext();                    
                    if (l.token() != Token.LEFT_CURLY){	//If the first token is not a '{', the block is not formatted right
                        throw new Exception("Function block invalid");
                    }else{
                        sNode = new StatementsNode(t);	//begin to parse your statements
                        r = t.current();		//after all the statments, there should just be a right curly left
                        if (r.token() != Token.RIGHT_CURLY){
                            throw new Exception("Function block invalid");	
                        }
                    }
		        } catch (Exception e) {
                		
                    System.out.println(e);
                }
            }
            
            /**
             * evaluation will happen within the nodes.
             * args is nothing at this point
             */
            public Object evaluate(Object[] args) throws Exception{
            		return sNode.evaluate(obArray);
            } 
            
            /**
             * Builds the part of the parse tree for the curly brackets and the block
             */
            public void buildString(StringBuilder builder, int tabs){
            		builder.append("BlockNode\n");
                builder.append(l.toString() + "\n");
                sNode.buildString(builder, tabs);		//builds the parse tree within the curly brackets
                builder.append(r.toString() + "\n");
                System.out.println(builder.toString());
            }
        }
        
        /**
         * Represents the total of all the assignment statements within a program passed in
         * @author Sam
         */
        class StatementsNode implements INode {
            Lexeme l = null;
            Lexeme n = null;
            AssignmentNode aNode = null;		//The current assignment statement being parsed and evaluated
            StatementsNode sNode = null;		//All succeeding assignment statements
            
            /**
             * Constructor for a Statements node
             * @param t The tokenizer containing all the lexemes of the program sent in
             */
            public StatementsNode(Tokenizer t) {
                try { 
                			aNode = new AssignmentNode(t);	
                			l = t.current();		//represents the first token after the previous assignment
                			n = t.next();		//the second token after the previous assignment
                			if (l.token() != Token.EOF && n.token() != Token.EOF){	//if we have not reached the end of the file
                				sNode = new StatementsNode(t);	//The process more assignments
                			}
                }catch (Exception e) {
                		System.out.println(e);
                }
            }
            
            /**
             * evaluation will happen within the nodes.
             * args contains the values already evaluated
             */
            public Object evaluate(Object[] args) throws Exception {
            		Object returnObject = aNode.evaluate(args);
            		if (sNode != null) {	//if there are any more succeeding assignments
            			return returnObject.toString() + sNode.evaluate(args).toString();	//evaluate these assignments and return all evaluations
            		}else {
            			return returnObject;
            		}
            }
            
            /**
             * Calls buildString methods from other nodes to build the inside of the parse tree
             */
            public void buildString(StringBuilder builder, int tabs){
            		if (aNode != null) {
            			tabAppend(builder,tabs);	//insert the appropriate amount of tabs before printing StatementsNode to the parse tree
            			builder.append("StatementsNode\n");	
            			aNode.buildString(builder, tabs+1);
            		}
                if (sNode != null){
                		sNode.buildString(builder, tabs+1);
                }else {
                		tabAppend(builder,tabs+1);
                		builder.append("StatementsNode\n");
                }
            }
        }
        
        /**
         * Represents an individual assignment statement passed in
         * @author Sam
         */
        class AssignmentNode implements INode {
            Lexeme id = null;	//The left hand side of the equality
            Lexeme eq = null;	//An equality sign
            ExpressionNode eNode = null;	//The expression evaluated in the equality
            Lexeme sc = null;	//A semi colon
            
            /**
             * Constructor for assignment nodes
             * @param t The tokenizer containing all the lexemes of the program sent in
             */
            public AssignmentNode(Tokenizer t) {
                try {
                		id = t.current();
                		eq = t.next();
                		//If the equality looks normal so far before the expression
                    if (id.token() == Token.IDENT && eq.token() == Token.ASSIGN_OP){
                    		t.moveNext();
                        t.moveNext();
                        //parse the expression
                        eNode = new ExpressionNode(t);
                        //After the expression, a semicolon should follow
                        if (t.current().token() == Token.SEMICOLON){
                            sc = t.current();
                            t.moveNext();
                         } else{
                            throw new Exception("Missing Semi Colon");
                        }
                    } else{
                    		//if we are at the end of the file, exit out
                    		if (id.token() == Token.EOF || eq.token() == Token.EOF) {
                    			return;
                    		}
                        throw new Exception("Assignment invalid");
                    }
                } catch (Exception e) {
                    System.out.println(e);
                }
            }
            
            /**
             * Returns what variable are equal to what values, with the values determined by the expression evaluate method
             * args contains the values already evaluated
             */
            public Object evaluate(Object[] args) throws Exception{
            		//Objects with functions that can evaluate strings
            		ScriptEngineManager mgr = new ScriptEngineManager();
                ScriptEngine engine = mgr.getEngineByName("JavaScript");
                //The evaluated value within the expression on the right side of the equation
                String eNodeString = (String.valueOf(eNode.evaluate(args)));
                int length = -1;
                //Looking for the first null variable within args, there is where we store the value calculated so that if other equalities use this value, we can find it
                for (int i=0; length < 0; i++) {
                		if (args[i] == null) {
                			length = i;
                			args[i] = id.value();
                			args[i+1] = eNodeString;
                		}
                }
                return id.value() + " = " + eNodeString + "\n";
            } 
            
            /**
             * Puts the left hand side of equalities, equal operations, and semi colons in the parse tree, and calls the expression nodes buildString to build the rest of the parse tree
             */
            public void buildString(StringBuilder builder, int tabs){
            		
            		tabAppend(builder,tabs);
            		builder.append("AssignmentNode\n");
            		tabAppend(builder,tabs+1);
                builder.append(id.toString() + "\n");
                tabAppend(builder,tabs+1);
                builder.append(eq.toString() + "\n");
                eNode.buildString(builder, tabs+2);
                tabAppend(builder,tabs+1);
                builder.append(sc.toString() + "\n");
            }
        }        
        
        /**
         * Represents a single term node, or an expression node added to, or subtracted from a term node
         * @author Sam
         */
        class ExpressionNode implements INode {
            Lexeme l = null;	//the lexeme the tokenizer is on after processing the termNode
            TermNode tNode = null;
            ExpressionNode eNode = null;
            
            public ExpressionNode(Tokenizer t) {
                try {
                        tNode = new TermNode(t);
                        l = t.current();
                        //The term node should be followed by an addition or subtraction operator, or else nothing
                        if (l.token() == Token.ADD_OP || l.token() == Token.SUB_OP){
                        		t.moveNext();
                            eNode = new ExpressionNode(t);
                       }
                } catch (Exception e) {
                    System.out.println(e);
                }
            }
            
            /**
             * Evaluates arithmetic having to do with addition and subtraction
             * args contains the values already evaluated
             */
            public Object evaluate(Object[] args) throws Exception{
                if (eNode == null) {
                		Object returnAnswer = tNode.evaluate(args);
                		return returnAnswer;
                } else {
                		//Objects with methods that can evaluate arithmetic in strings
                		ScriptEngineManager mgr = new ScriptEngineManager();
                    ScriptEngine engine = mgr.getEngineByName("JavaScript");
                    
                    String tNodeString = String.valueOf(tNode.evaluate(args));
                    String eNodeString = String.valueOf(eNode.evaluate(args));
                    String opString;
                    //The methods for evaluation used don't work well when there are two operators beside eachother
                    if (Float.valueOf(eNodeString) < 0 && l.token() == Token.SUB_OP) {
                    		opString = " + ";
                    		eNodeString = String.valueOf(Math.abs(Float.valueOf(eNodeString)));
                    } else if (Float.valueOf(eNodeString) < 0 && l.token() == Token.ADD_OP) {
                    		opString = "";
                    } else {
                    		opString = String.valueOf(l.value());
                    }
                    //Evaluating the arithmetic inside the expression
                    Object returnValue = engine.eval(tNodeString.concat(opString.concat(eNodeString)));
                    return returnValue;
                }
            }
            
            /**
             * Puts add and subtract arithmetic in the parse tree
             */
            public void buildString(StringBuilder builder, int tabs){
            		tabAppend(builder,tabs-1);	//Indenting with the appropriate amount of tabs
            		builder.append("ExpressionNode\n");
                if (eNode != null){
                    tNode.buildString(builder, tabs+1);
                    tabAppend(builder,tabs);
                    builder.append(l.toString() + "\n");	//add or subtract operator
                    eNode.buildString(builder, tabs+1);
                }else{
                    tNode.buildString(builder, tabs+1);
                }
            }
        }
        
        /**
         * Represents a single factor node, or an term node added to, or subtracted from a factor node
         * @author Sam
         */
        class TermNode implements INode {
            Lexeme l = null;		//The lexeme the tokenizer will be on after the factor node
            FactorNode fNode = null;
            TermNode tNode = null;
            
            /**
             * Constructor the the termNode class
             * @param t The tokenizer containing all the lexemes for the program
             */
            public TermNode(Tokenizer t) {
                try {
                	    fNode = new FactorNode(t);	
                    l = t.current();
                    //The factor node should be followed by a '*' a '/' or nothing
                	    if (l.token() == Token.MULT_OP || l.token() == Token.DIV_OP){
                        t.moveNext();
                        tNode = new TermNode(t);
                     }
                } catch (Exception e) {
                    System.out.println(e);
                }
            }
            
            /**
             * Evaluates arithmetic having to do with multiplication and division
             * args contains the values already evaluated
             */
            public Object evaluate(Object[] args) throws Exception{
            		if (tNode == null) {
            			Object returnValue = fNode.evaluate(args);
            			return returnValue;
            		} else {
            			//Objects with methods that can evaluate arithmetic in strings
            			ScriptEngineManager mgr = new ScriptEngineManager();
                     ScriptEngine engine = mgr.getEngineByName("JavaScript");
                     
                     //Evaluating the division or multiplication arithmetic
                     Object returnValue = engine.eval(String.valueOf(fNode.evaluate(args)).concat(String.valueOf(l.value())).concat(String.valueOf(tNode.evaluate(args))));
                     
                     return returnValue;
            		}
            }
            
            /**
             * Puts multiply and divide arithmetic in the parse tree
             */
            public void buildString(StringBuilder builder, int tabs){
            		tabAppend(builder, tabs-1);	//inserting the appropriate amount of tabs before lines in this node
            		builder.append("TermNode\n");
            		// if there is more than a single factor node
                if (tNode != null){
                    fNode.buildString(builder, tabs+1);
                    tabAppend(builder,tabs);
                    builder.append(l.toString() + "\n");
                    tNode.buildString(builder, tabs+1);
                } else {
                		fNode.buildString(builder, tabs+1);
                }
            }
        }

        /**
         * Represents single identifiers, single integers, or expressions within brackets
         * @author Sam
         */
        class FactorNode implements INode {
            Lexeme l = null;		//will be a left bracket '(', the identifier, or an int_lit
            Lexeme r = null;		//will be a right bracket
            ExpressionNode eNode = null;
            
            /**
             * Constructor for a factor node
             * @param t	The tokenizer containing the lexemes of the program
             */
            public FactorNode(Tokenizer t) {
                try {
                    l = t.current();
                    t.moveNext();
                    //If the current lexeme is a left paren, then this factor is an expression
                    if ( l.token() == Token.LEFT_PAREN ){
                        eNode = new ExpressionNode(t);
                        r = t.current();	//a right paren should follow
                            t.moveNext();
                        if ( r.token() != Token.RIGHT_PAREN){
                                throw new Exception("Missing right parenthesis");
                        }
                    } else if ( l.token() == Token.RIGHT_PAREN){
                            throw new Exception("Missing left parenthesis");
                    }
                } catch (Exception e) {
                    System.out.println(e);
                }
            }
        
             // returns the values of the integers in the program, the values of identifiers or calls evaluation for an expressionNode
            public Object evaluate(Object[] args) throws Exception{
                if (eNode == null) {
                		if (l.token() == Token.IDENT) {
                			//If the token is an identifier, we must check to see if we have the value of this identifier store in args
                			for (int i=0; i<args.length; i++) {
                				if (l.value().equals(args[i])) {
                					//if it is stored return this value
                					Float temp =  Float.valueOf(args[i+1].toString());
                					return temp;
                				}
                			}
                			throw new RuntimeException("The identifier " + l.value() + " hasn't been initialized");
                		}
                		return l.value();
                } else {
                		return eNode.evaluate(args);
                }
            }
            
            /**
             * Puts identifiers and integers into the parse tree
             */
            public void buildString(StringBuilder builder, int tabs){
            		tabAppend(builder,tabs-1);
            		builder.append("FactorNode\n");
            		if (r != null){
            			tabAppend(builder,tabs);
            			builder.append(l.toString() + "\n");
            		    eNode.buildString(builder, tabs+1);	//puts the part of the program contained in expressions in the parse tree
                    tabAppend(builder,tabs);
                    builder.append(r.toString() + "\n");
                } else {
                		tabAppend(builder,tabs);
                    builder.append(l.toString() + "\n");
                }
            }   
        }
        
	/**
	 * Closes the file and releases any system resources associated with it.
	 */
	public void close() throws IOException{
            if (t != null)
                t.close();
        }
	
	//Puts the appropriate amount of tabs into the parse tree depending on how nested the node is
	public void tabAppend(StringBuilder builder, int tabs) {
		for (int i=0; i<=tabs; i++) {
			builder.append("\t");
		}
	}
}

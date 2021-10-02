# JavaInterpreter

An interpreter, consisting of a parser and an evaluator, for the following grammar

```
block = ‘{’ , stmts , ‘}’ ;
stmts = [ assign , stmts ] ;
assign = id , ‘=’ , expr , ‘;’ ;
expr = term , [ ( ‘+’ | ‘−’ ) , expr ] ;
term = factor , [ ( ‘*’ | ‘/’ ) , term ] ;
factor = int | id | ‘(’ , expr , ‘)’ ;
```

The parser takes a list of lexemes/tokens as input, and from that list of lexemes/tokens create a parse tree as output

The evaluator takes a parse tree as input, and return the program-state after execution as output. The program-state is represented by a list of all variables and their values.

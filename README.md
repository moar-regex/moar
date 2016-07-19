# moar
Deterministic Regular Expressions with Backreferences. Uses ANTLR for Pattern compilation.

# Theoretical Background

A Paper explaining the theoretical background is currently being written.

# Supported Syntax

The supported Pattern syntax can easily be seen in the ANTLR [grammar](
https://github.com/s4ke/moar/blob/master/engine/src/main/antlr4/com/github/s4ke/moar/regex/parser/Regex.g4):

# Example

```Java
MoaPattern pattern = MoaPattern.compile("^Deterministic|OrNot$");
MoaMatcher matcher = pattern.matcher("Deterministic");
if ( matcher.matches() ) {
  System.out.println("yay");
}
```

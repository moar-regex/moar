# moar
Deterministic Regular Expressions with Backreferences. Uses Memory Occurence Automata to match the input.

ANTLR is used for Pattern compilation.

## Why?
Java's Patterns are not deterministic and might get you into trouble. All deterministic alternatives don't support backreferences. This library does. This however is no drop in replacement for Java's patterns as some things can not be expressed while keeping the determinism.

## Technical documentation

The technical documentation can be found in the [documentation folder](documentation/implementation.md).

## Theoretical Background

I held a talk at university covering the basics of this library as well. It can be found in the [presentation folder](presentation/index.md)

A Paper explaining the theoretical background is currently being written.

## Supported Syntax

While somewhat similar to Java's Pattern syntax, moar's supported syntax might differ in some cases. The full supported Pattern syntax can easily be seen in the ANTLR [grammar](
https://github.com/s4ke/moar/blob/master/engine/src/main/antlr4/com/github/s4ke/moar/regex/parser/Regex.g4):

## Examples

```Java
MoaPattern pattern = MoaPattern.compile("^Deterministic|OrNot$");
MoaMatcher matcher = pattern.matcher("Deterministic");
if ( matcher.matches() ) {
  System.out.println("yay");
}
```

Or this cool language:

```Java
MoaPattern pattern = MoaPattern.compile("((?<y>\\k<x>)(?<x>\\k<y>a))+");
MoaMatcher matcher = pattern.matcher("aaaa");
if( matcher.matches() ) {
  System.out.println("yay again.");
}
```

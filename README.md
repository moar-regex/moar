# moar
Deterministic Regular Expressions with Backreferences. Uses ANTLR for Pattern compilation.

# Theoretical Background

A Paper explaining the theoretical background is currently being written.

# Example

```Java
MoaPattern pattern = MoaPattern.compile("^Deterministic|OrNot$");
MoaMatcher matcher = pattern.matcher("Deterministic");
if ( matcher.matches() ) {
  System.out.println("yay");
}
```

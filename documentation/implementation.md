# moar Implementation Details

Moar is the first implementation of MOA (Memory Occurence Automata). It aims to support all features provided in Dominik's paper while also shipping with a API that is inspired by Java's Pattern class. However, we only support a subset of Regexes compared to Java Patterns, in order to simplify the Grammar.

## Utility classes/interfaces

### CharSeq

![CharSeq interface](img/CharSeq.png)

The CharSeq interface is an abstraction from the CharSequences that Java normally and enables us to support other inputs as well (for example input from a byte sequence like in the Lucene module which can be found in the git repository). Moar only works with codePoints instead of char's. This means that we can support UTF-32 and helps us with unicode character duplicates.

## How are MOAs represented in Code?

In moar, a MOA (Java class: com.github.s4ke.moar.Moa) consists of a set of Variables (basic placeholders for the Variables used in the MOA, nothing fancy) and a Graph representation of the automaton (com.github.s4ke.moar.moa.edgegraph.EdgeGraph).

### Basic classes

In order to get into how the Graph representation is implemented, we have to explain two basic data classes - Variable and MatchInfo - first.

##### Variable

![The Variable class](img/Variable.png)

Objects of this type just holds the current contents of the Variables and is implemented as basic as it gets.

##### MatchInfo

![The MatchInfo class](img/MatchInfo.png)

MatchInfo objects are used to hold information about where in the input string the matching process is currently at. The fields are:

- string: what character (or in some cases characters) are currently supposed to be matched. The EfficientString wrapper around character sequences allows us to represent subsequences in an efficient manner by only storing the start and end index together with a reference to the original sequence.

- wholeString: the whole input. The CharSeq interface is used so we can wrap away details of what we are matching. This way we can also support for example Byte-Wise character sequences instead of the default Java Strings.

- pos: the current position in the input string

- lastMatch: the index in the string where the last match ended (otherwise this is set to -1)


### The Graph representation

#### States

We will now take a look at the nodes of our MOA Graph, the states:

![The State interface](img/state_interface.png)

(Note: getIdx() is the pendant of the markings in a marked alphabet. This is not really necessary for the implementation, but helps us to identify states faster)

As we can see, the State interface has three different methods that look like they are related to identifying what has to be read in order to go to them during evaluation. However not every method can be used for every type of state. But to explain this we have to go over the different State implementations. After that we give the reasoning behind the three different methods.

##### Static (Basic) States

A static State is the code representation of basic character only states. These states can only contain singular characters.

##### Set States

The theoretical model has no need for these states as it doesn't support character classes. But we want to support these in our Regexes and don't to create unnecessarily big numbers of basic states to represent them. We will give a short explanation of character classes in Regexes later, but for now, we will just think of them as a Set/range of allowed characters.

In our implementation we represent basic Sets (not often needed, mainly for internal things) via the Java Collection Set and Ranges (like [a-ce-z]) by a Google Guava TreeRangeSet which is space efficient as it only stores the ranges of allowed characters instead of all of them. It also already comes with a negation implementation which helps us with negative sets (e.g.: to allow everything but a's).



In our implementation we use `canConsume(EfficientString string) : boolean` for Static and Set states to check whether the current character can be consumed. The implementation for this is just a basic equality (or containment for the Set state) check.

##### Variable States

Variable States are the code representation of backreferences and only need to have the variable name as a member. As the variables can contain input longer than one character we don't have a separate "canConsume" method. In our matching algorithm we first get the character sequence we have to match in order to traverse into a variable state via `getEdgeString(Map<String, Variable>) : EfficientString` and then check for equality in the remaining input.

##### Bound States

Bound states are used to represent boundary checks in Regexes. We can think of them as basic checks like "am I at the beginning of the input" or "am I at the end of the input". For these we use the `canConsume(MatchInfo matchInfo) : boolean` method during matching.

#### Edges

The edges of the MOAs are represented by a simple Edge object in the EdgeGraph:

![EdgeGraph](img/Edges.png)

Every Edge has a set of MemoryActions that determine just like in the theoretical model how the variable state(s) should change if the edge is used.

#### EdgeGraph

The EdgeGraph is the basic in-code representation of the MOA Graph:

![EdgeGraph](img/EdgeGraph.png)

It stores all the states (field: states) with their corresponding Edges (field: edges) which are duplicated into the other *Edges fields for more convenient and faster access during evaluation.

Note: CurStepHolder is an abstraction enables the EdgeGraph to be reused as it doesn't need to store the current state.

It's most prominent methods are:

- `maximalNextTokenLength(CurStateHolder stateHolder, Map<String, Variable> vars) : int`

  This method is used to compute the length of the next character sequence ("token") to be read. This is primarily needed because of the fact that Variable States can have tokens of any length and because we can have epsilon edges to the sink (just like in the theoretical model, the MOA always has a source and a sink, these are special unique State objects which we will talk about later in the Regex chapter which also explains how the MOAs are meant to be created). If there are only transitions to Basic or SetStates this will return 1.

- `step(CurStateHolder stateHolder, MatchInfo mi, Map<String, Variable> vars) : StepResult`

  The EdgeGraph tries to do a step with the current step represented in the stateHolder object with the given info of the MatchInfo object (position, current "token", see above) and the current variable state. Its possible return values are CONSUMED (success), NOT_CONSUMED (the current token was not consumed, this is used for boundary checks which do not consume anything) and REJECTED (no valid transition could be found).

### The Moa class

![Moa class](img/Moa.png)

The Moa class serves as an intermediary entry point for matching. Essentially it could be directly used, but is a bit rough on the edges (therefore it is wrapped into the MoaPattern class, which is explained in the Regexes & Pattern API chapter). Its two most prominent methods are `matcher(CharSeq charSeq) : MoaMatcher` which creates a MoaMatcher object on the given input and the `check(CharSeq charSeq) : boolean` method which immediately checks the input for a complete match.

###  The Matcher

![MoaMatcher interface](img/MoaMatcher.png)

The MoaMatcher interface grants the user more detailed control over how the matching is done. With this interface the user can:

- check for a full-string match with `matches() : boolean`
- check for the next match with `nextMatch() : boolean` (this tries to find the longest sequence in the input that is in the language of the MOA)
- replace the first match of the MOA in the input with `replaceFirst(String replacement) : String` (or all matches with `replaceAll(String replacement) : String`)
- retrieve the variable content via the `getVariableContent(...) : String` methods
- retrieve the start (`getStart() : int`) and the end (`getEnd() : int`) of the last match

It is also meant to be reused (see `reuse(CharSeq charSeq) : MoaMatcher`).

### The MoaPattern

![MoaPattern](img/MoaPattern.png)

This is the main entry class for users and has the same purpose as Java's Pattern class. It doesn't add new functionality over the Moa class, but wraps it into a more beautiful API. It has two separate compile methods that allow for creation from a DSL and from a Regex String. The `build(Moa moa, String regex) : MoaPattern` method is mostly meant for integrators like the JSON export/import module.

## Regexes & Pattern API

Our deterministic Regexes can be translated into code in two separate ways:

### With a Java DSL:

```Java
MoaPattern moa = MoaPattern.compile(
  					// DSL style generation of the Regex
  					Regex.reference( "x" )
                        .bind( "y" )
                        .and( Regex.reference( "y" ).and( "a" ).bind( "x" ) )
                        .plus()
                 );
```

This chaining DSL was initially created to be able to create Regexes without any additional Parser.  It is a in code representation of the deterministic Regexes. For a complete list of the supported methods, take a look at the `com.github.s4ke.moar.regex.Regex` class.

Later, we will explain how this is translated into a MOA.

### With a Java Pattern-like Regex string:

```Java
// building a Regex from a String
MoaPattern moa = MoaPattern.compile("((?<y>\k<x>)(?<x>\k<y>a))+");
```

The parsing of this Regex string is done via ANTLR v4 and internally uses the DSL API (above). The Grammar is the following:

```java
grammar Regex;

/**
 * Grammar for parsing Perl/Java-style Regexes
 * after: http://www.cs.sfu.ca/~cameron/Teaching/384/99-3/regexp-plg.html
 * but with left recursion eliminated
 */

regex:
    EOF
    | startBoundary? union endBoundary? EOF;

startBoundary :
    START
    | prevMatch;
prevMatch : ESC 'G';

endBoundary :
    EOS
    | endOfInput;
endOfInput : ESC 'z';

union :
    concatenation
    | union '|' concatenation;

concatenation :
    basicRegex
    | basicRegex concatenation;

basicRegex :
    star
    | plus
    | orEpsilon
    | elementaryRegex;

star :
    elementaryRegex '*';
plus :
    elementaryRegex '+';
orEpsilon:
    elementaryRegex '?';

elementaryRegex :
    backRef
    | group
    | set
    | charOrEscaped
    | stockSets
    | ANY;

group :
    '(' (capturingGroup | nonCapturingGroup) ')';
capturingGroup : ('?' '<' groupName '>')? union?;
nonCapturingGroup: '?' ':' union?;
groupName : character+;

backRef :
    ESC number
    | ESC 'k' '<' groupName '>';

set :
    positiveSet
    | negativeSet;
positiveSet	: '[' setItems ']';
negativeSet	: '[^' setItems ']';
setItems :
    setItem
    | setItem setItems;
setItem :
    range
    | charOrEscaped;
range :
    charOrEscaped '-' charOrEscaped;

//should these be handled in the TreeListener?
//if so, we could patch stuff easily without changing
//the grammar
stockSets:
    whiteSpace
    | nonWhiteSpace
    | digit
    | nonDigit
    | wordCharacter
    | nonWordCharacter;
whiteSpace : ESC 's';
nonWhiteSpace : ESC 'S';
digit : ESC 'd';
nonDigit : ESC 'D';
wordCharacter : ESC 'w';
nonWordCharacter : ESC 'W';

charOrEscaped :
    character
    | escapeSeq
    | UTF_32_MARKER utf32 UTF_32_MARKER;
character : (UNUSED_CHARS | ZERO | ONE_TO_NINE | 's' | 'S' | 'd' | 'D' | 'w' | 'W' | 'k' | 'z' | 'G' | ':' | '<' | '>' );
escapeSeq : ESC escapee;
escapee : '[' | ']' | '(' | ')'
    | ESC | ANY | EOS | START | UTF_32_MARKER
    | '*' | '+' | '?'
    | '-' ;
utf32 : (character | escapeSeq)+;

number :  ONE_TO_NINE (ZERO | ONE_TO_NINE)*;

ZERO : '0';
ONE_TO_NINE : [1-9];
ESC : '\\';
ANY : '.';
EOS : '$';
START : '^';
UTF_32_MARKER : '~';

UNUSED_CHARS :
    ~('0' .. '9'
    | '[' | ']' | '(' | ')'
    | '\\' | '.' | '$' | '^'
    | '*' | '+' | '?'
    | ':'
    | 's' | 'S' | 'd' | 'D' | 'w' | 'W' | 'k' | 'z' | 'G'
    | '~');
```

The MoaPattern class behaves in this context similar to Java's native Pattern class and wraps the internal MOA logic from the user. It serves as the entry point to build MoaMatcher objects (similar to Java's Matcher) which encapsulate all the matching logic so that the Pattern itself can be reused as its construction can be expensive.

### From Regex to the MOA

### SRC, SNK erklären (im Chapter über die Creation!)

## JSON Serialization

MoaPatterns can be serialized into a human readable format:

```json
{
  "regex":"^(?<toast>[a-z]b[^b]\\w)\\k<toast>.$",
  "vars":["toast"],
  "states":[
    		{"bound":"^","idx":2},
    		{"set":"[a-z]","idx":3},
    		{"name":"b","idx":4},
    		{"set":"[^b]","idx":5},
    		{"set":"\\w","idx":6},
    		{"ref":"toast","idx":7},
    		{"set":".","idx":8},
    		{"bound":"$","idx":9}
  ],
  "edges":[
    		{"from":0,"to":2},
    		{"from":2,"to":3,"memoryActions":["o(toast)"]},
    		{"from":3,"to":4},
    		{"from":4,"to":5},
    		{"from":5,"to":6},
    		{"from":6,"to":7,"memoryActions":["c(toast)"]},
    		{"from":7,"to":8},
    		{"from":8,"to":9},
    		{"from":9,"to":1}
  ]
}
```

Explanation for the JSON fields:

- regex

  This is for documentation purposes only and couldn't be anything else as the Regexes do not have the same expressive power as hand written MOAs.

- vars

  In this field all used vars of the MOA are listed as simple strings.

- states

  In this field the states are listed. "name" represents Basic states, "set" represents Set states, "ref" represents Variable states and "bound" represents boundary states (the list of supported bounds can be found in com.github.s4ke.moar.regex.BoundConstants). The "idx" must be unique and in the range and >= 2 as SRC and SNK are 0 and 1, respectively.

- edges

  In this field we can specify the edges and possible memoryActions (o = open, c = close, r = reset, used like in the example: r(x) - reset the variable x)

Serialization is done via `com.github.s4ke.moar.json.MoarJSONSerializer#toJSON(MoaPattern pattern) : String` and deserialization via `com.github.s4ke.moar.json.MoarJSONSerializer#fromJSON(String json) : MoaPattern`.

This feature is particularly useful as this allows users to not only hand-write MOAs but also enables them to transmit generic MoaPatterns between applications even if no Regex is available. One can also think of an extra application that allows users to create their own MOAs with a GUI which then are exported to this format for later usage.
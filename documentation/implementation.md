# moar Implementation Details

Moar is the first implementation of MOA (Memory Occurence Automata). It aims to support all features provided in Dominik's paper while also shipping with a API that is inspired by Java's Pattern class. However, we only support a subset of Regexes compared to Java Patterns, in order to simplify the Grammar.

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

- string: what character (or in some cases characters) are currently supposed to be matched. The EfficientString class is a wrapper around character sequences that allows us to represent subsequences  in an efficient manner by only storing the start and end index together with a reference to the original sequence.

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

The theoretical model has no need for these states as it doesn't support character classes. But we want to support these in our Regexes and don't to create unnecessarily big numbers of basic states to represent them. We will give a short explanation of character classes in Regexes later, but for now, we will just think of them as a Set of allowed characters.

In our implementation we represent these sets by a Google Guava TreeRangeSet which is space efficient as it only stores the ranges of allowed characters instead of all of them. It also already comes with a negation implementation which helps us with negative sets (e.g.: to allow everything but a's).



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

![EdgeGraph](img/EdgeGraph.png)

- SRC, SNK erklären
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
// this odd separation of unused chars and "used chars" is due to ANTLR
// processing in two phases. At first, only the token rules (in CAPS) are
// and then the rules are used. For normal grammars (for programming languages)
// this is fine, but in our case this means this extra (and ugly) work.
// Due to this, every single char that is to be matched must be tokenized
// (the ones that are not part of a NAMED token rule are just implicitly made
// into their own token rule). Every "non special" char
// (the ones that are not explicitly mentioned) is therefore tokenized
// into UNUSED_CHAR.
// This approach is by far easier than a hand written parser, though.
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
grammar Regex;

/**
 * Grammar for parsing Perl/Java-style Regexes
 * after: http://www.cs.sfu.ca/~cameron/Teaching/384/99-3/regexp-plg.html
 * but with left recursion eliminated
 */

regex:
    EOF
    | START? union EOS? EOF;

union :
    concatenation
    | union '|' concatenation;

concatenation :
    basicRegex
    | basicRegex concatenation;

basicRegex :
    backRef
    | star
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
    group
    | set
    | charOrEscaped
    | stockSets
    | ANY
    | EOS;

group :
    '(' (capturingGroup | nonCapturingGroup) ')';
capturingGroup : ('?' '<' groupName '>')? union?;
nonCapturingGroup: '?' ':' union?;
groupName : character+;

backRef :
    ESC NUMBER
    | ESC '<' groupName '>';

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
    | ESC METACHAR
    | ESC ESC;
character : (UNUSED_CHARS | 's' | 'S' | 'd' | 'D' | 'w' | 'W');

NUMBER : [1-9][0-9]*;
METACHAR : '^' | '$' | '[' | ']' | '(' | ')' | '*' | '+' | '?' | '<' | '>' | ':';
ESC : '\\';
START : '^';
EOS : '$';
ANY : '.';
UNUSED_CHARS :
    ~('\\' | '^' | '$' | '[' | ']' | '(' | ')' | '*' | '+' | '?' | '<' | '>' | ':' | 's' | 'S' | 'd' | 'D' | 'w' | 'W');
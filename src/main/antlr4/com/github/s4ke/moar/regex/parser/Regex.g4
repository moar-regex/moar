grammar Regex;

options {
k=2;
output=AST;
}

/**
 * Grammar for parsing Perl/Java-style Regexes
 * after: http://www.cs.sfu.ca/~cameron/Teaching/384/99-3/regexp-plg.html
 * but with left recursion eliminated
 */

regex:
    EOF
    | START? union EOS? EOF
    ;
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
    | ANY
    | EOS;
group :
    '(' union ')';
set :
    positiveSet
    | negativeSet;
backRef : ESC NUMBER;
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
charOrEscaped :
    CHAR
    | ESC METACHAR
    | ESC ESC;

NUMBER : [1-9][0-9]*;
METACHAR : '^' | '$' | '[' | ']' | '(' | ')' | '*' | '+' | '?';
ESC : '\\';
EOS : '$';
START : '^';
ANY : '.';
CHAR :
    ~('\\' | '^' | '$' | '[' | ']' | '(' | ')' | '*' | '+' | '?');
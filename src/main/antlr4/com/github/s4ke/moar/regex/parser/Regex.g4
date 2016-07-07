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

regex
    : union EOF
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
    | elementaryRegex;
star :
    elementaryRegex '*';
plus :
    elementaryRegex '+';
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
backRef : '\\' NUMBER;
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
    | '\\' METACHAR;

NON_ZERO_DIGIT : ('1'..'9');
DIGIT : ('0'..'9');
NUMBER : NON_ZERO_DIGIT DIGIT*;
METACHAR : '\\' | '^' | '$' | '[' | ']' | '(' | ')' | '*' | '+';
EOS : '$';
ANY : '.';
CHAR :
    ~('\\' | '^' | '$' | '[' | ']' | '(' | ')' | '*' | '+');
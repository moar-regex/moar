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
    : union
    ;
union :
    concatenation
    | union '|' concatenation;
concatenation :
    basicRegex
    | basicRegex concatenation;
basicRegex :
    star
    | plus
    | elementaryRegex;
star :
    elementaryRegex '*';
plus :
    elementaryRegex '+';
elementaryRegex :
    group
    | set
    | backRef
    | ANY
    | EOS
    | CHAR;
group :
    '(' regex ')';
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
    | CHAR;
range :
    CHAR '-' CHAR;

NON_ZERO_DIGIT : ('1'..'9');
DIGIT : ('0'..'9');
NUMBER : NON_ZERO_DIGIT DIGIT*;
METACHAR : '\\' | '^' | '$' | '[' | ']' | '(' | ')' | '*' | '+';
EOS : '$';
ANY : '.';
CHAR :
    ~('\\' | '^' | '$' | '[' | ']' | '(' | ')' | '*' | '+')
    | '\\' METACHAR;
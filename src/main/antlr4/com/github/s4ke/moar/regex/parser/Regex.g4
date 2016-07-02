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
    | ANY
    | EOS
    | CHAR;
group :
    '(' regex ')';
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
    | CHAR;
range :
    CHAR '-' CHAR;

METACHAR : '\\' | '^' | '$' | '[' | ']' | '(' | ')' | '*' | '+';
ESCAPE : '\\';
EOS : '$';
ANY : '.';
CHAR :
    ~('\\' | '^' | '$' | '[' | ']' | '(' | ')' | '*' | '+')
    | ESCAPE METACHAR;
grammar Regex;

/**
 * Grammar for parsing Perl Regexes
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
    basicregex
    | basicregex concatenation;
basicregex :
    star
    | plus
    | elementaryregex;
star :
    elementaryregex '*';
plus :
    elementaryregex '+';
elementaryregex :
    group
    | set
    | ANY
    | EOS
    | CHAR;
group :
    '(' regex ')';
set :
    positiveset
    | negativeset;
positiveset	: '[' setitems ']';
negativeset	: '[^' setitems ']';
setitems :
    setitem
    | setitem setitems;
setitem :
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
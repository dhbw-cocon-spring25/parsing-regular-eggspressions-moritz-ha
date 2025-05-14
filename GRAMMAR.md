# Grammatik RedEggs

```
regex: concat union 

union: '|' concat
union: eps

concat: kleene suffix

suffix: kleene suffix
suffix = eps

kleene: base star

star: '*'
star: eps

base: Literal
base: '(' regex ')'
base: '[' inhalt range ']'

range: inhalt range
range: eps

inhalt: Literal rest

rest: '-' Literal
rest: eps
```

### First → Select → Follow

| rule | first | follow | note | select |
| --- | --- | --- | --- | --- |
| regex: concat union  | Literal, '(’, '[’ | EOF, ')’ |  | Literal, '(’, '[’ |
| union: '|' concat | '|’ | EOF, ')’ |  | '|’ |
| union: eps | eps | EOF, ')’ |  | EOF, ')’ |
| concat: kleene suffix | Literal, '(’, '[’ | '|’, EOF, ')’ |  | Literal, '(’, '[’ |
| suffix: kleene suffix | Literal, '(’, '[’ | '|’, EOF, ')’ |  | Literal, '(’, '[’ |
| suffix = eps | eps | '|’, EOF, ')’ |  | '|’, EOF, ')’ |
| kleene: base star | Literal, '(’, '[’ | '|’, EOF, ')’ |  | Literal, '(’, '[’ |
| star: '*’ | '*’ | '|’, EOF, ')’ |  | '*’ |
| star: eps | eps | '|’, EOF, ')’ |  | '|’, EOF, ')’ |
| base: Literal | Literal | '*’, '|’, EOF, ')’ |  | Literal |
| base: '(' regex ')’ | '(’ | '*’, '|’, EOF, ')’ |  | '(’ |
| base: '[' inhalt range ']’ | '[' | '*’, '|’, EOF, ')’ |  | '[' |
| range: inhalt range | Literal | ‘]’ |  | Literal |
| range: eps | eps | ‘]’ |  | ‘]’ |
| inhalt: Literal rest | Literal | Literal, ‘]’ |  | Literal |
| rest: '-' Literal | '-'  | Literal, ‘]’ |  | '-'  |
| rest: eps | eps | Literal, ‘]’ |  | Literal, ‘]’ |
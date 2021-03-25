# Universal converter
This is simple converter for СКБ Контур.

## How it works
It takes as input the path to the file with the conversion rules. 1 conversion rule looks like "S, T, value"
It deletes cycles from rules and creates the same file with bridges.
Using DFS function it finds answer, if it exists.

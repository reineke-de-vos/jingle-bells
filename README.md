# Jingle Bells (Caculus)

## Main idea

Small calculus working just like electronic tables, but with some differences.

Initially fields (so called "jingles") could have value and formula at the same time. Then user can initiate "bell" on the jingle, and it will lead to the wave  of changes through dependent jingles. Every jingle receiving bell will recalculate it's value if necessary. then send bell to inform other jingles the value was changed. Jingle could also have more than one calculator and choose actual one based on bell conditions.

The vase could be infinite, or it can stop using two mechanisms:
  * Predicate (boolean expression to check if we need recalculate). If value is not recalculated, no bell sent.
  * Ttl (time to live). Every bell has this value, and every jingle sends new bell with decremented value. TTL = -1 means infinite ttl.

Programming 'language' consist of two parts, language itself to describe jingles and their dependencies and command to manage program execution.

## Brief spec

### Simple 'program'

```
int a = 1:
    (b) -> a + b;

int b = 1:
    (a) -> a + b;
```

This code coud be run with the command

```
> bell a ttl 2
```

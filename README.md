# Jingle Bells (Calculus)

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
### Independent jingles

It is not necesary 'listen' jingle to use its value in formula. It means jignle will not recalculate values.

```
int delta = 5;
    (listening list)
int x:
    (listening list) -> expression;
int f:
    (x) -> x + delta;
```

in this case *delta* changes will not initiate *f* changes because f calculator uses x and delta values but listens x changes only.

### Multi-functionn calculator

It is possible to create several calculators.

```
float Celsius ttl 1:
    (Fahrenheit) -> 5 / 9 * (Fahrenheit - 32);
    (Reaumur) -> 1.2 * Reaumur;

float Fahrenheit ttl 1:
    (Celsius) ->  9 / 5 * C + 32;
    (Reaumur) -> Reaumur * 2.25 + 32;

float Reaumur ttl 1:
    (Fahrenheit) -> (Fahrenheit - 32) * 0.44444;
    (Celsius) -> 0.8 * Celsius;
```

This code will recaluculate all converters on one value change. Will not send bell because it's one time operation. User need to set one value and will have both another values automatically.

### Predicates

```
int i = 0;
    (i) -> i + 1;

float epsilon = 0.0001:

float delta:
    (i) -> 1 / (i * i);

float sum = 0:
    (delta) /delta > epsilon/ -> sum + delta;
```

Running this code with command ```bell i``` will calculate sum 1 + (1 / 2<sup>2</sup>) + (1 / 3<sup>2</sup>) + ... (1 / i<sup>2</sup>) till (1 / i<sup>2</sup>) is greater than *epsilon*. There are will not be division by zero because calculators are calling in order they are appeared in code, i.e. *i* will be recalculated first, then delta, then sum, then i again.

### Commands

```load jinglename value```: load value to the jingle

```read jinglename```: read value of the jingle

```listen jinglename```: report current value on every bell

For example, in predicate example, running ```listen sum``` will print current sum on every sum change.

```touch jignlename [ ttl ttlvalue ]```: invoke calculator of the jingle and send bell if necessary. Should works with one-calculator jingles.

```bell jinglename [ ttl ttlvalue ]```: bell targets (dependent jingles) of named jingle

#### Service commands

```stop```: stop the loop if you fall into infinite calculations

```exit```: end session

## Current state

No predicates yet. No formulas. Only *int* type is implemented. Work in progress.

### Future ideas

More types. Array types (at least for int and float to allow vector and matrix calculations).

## Plugins

### Ringer plugin

Plugin to cimmunicate with user. Currently console only plugin is imllemented. Need at least network plugin to run program from browser. Also it's possible to add visual view of array data (diagrams).

### Load plugin

Plugin lo load big amount of default (initial) data from files or network.

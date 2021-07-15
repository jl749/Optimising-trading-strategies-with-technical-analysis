# Optimising-trading-strategies-with-technical-analysis
Your	 task	 is	 to	 implement	 a	 Genetic	 Algorithm	 (GA)	 to	 optimise a	 trading	 strategy	 based	 on	
technical	indicators.	You	will	need	to	first	calculate	the	value	of	different	trading	signals	and	then	
use	a	GA	to	combine	their	recommendations	(BUY,	HOLD	and	SELL	signals).

For	 this	 assessment,	 you	 should	 use	 the	 Unilever.csv	 file,	 which	 was	 given	 to	 you	 during	 the	
practical	related	to	EDDIE – available	on	Moodle.

# Part A: Implementing technical indicators and trading signals

## Task 1: Technical Indicators (10%)

**Task 1a**
Given	the	Moving	Average	(MA),	calculate	the 	12	 days	moving	average.	Once	you	
are	done	with	this,	also	calculate	the	 26	 days	moving	average.

**Task 1b**
Calculate	the	24	days	trade	break	out	rule	(TBR),	given	the	formula	below.

### 𝑇𝐵𝑅(𝐿,𝑡)=

```
𝑃(𝑡)− max {𝑃(𝑡− 1 ),...,𝑃(𝑡−𝐿)}
max {𝑃(𝑡− 1 ),...,𝑃(𝑡−𝐿)}
```
where	 _t_ is	 the	 current	 price,	 _L_ is	 the	 period	 length	 (24	 days),	 and	 max{...}	 would	 return	 the	
maximum	price	in	the	range	of	[ _t– 1_ ,	 _t–L_ ].

**Task 1c**
Calculate	the	29	days	volatility	(VOL),	given	the	formula	below.


### 𝑉𝑜𝑙(𝐿,𝑡)=

### 𝜎(𝑃(𝑡),...,𝑃(𝑡−𝐿+ 1 ))

### 1

### 𝐿∑ 𝑃(𝑡−𝑖)

```
$
#%&
```
where	𝜎 is	the	standard	deviation	for	the	prices	in	the	given	range,	 _t_ is	the	current	price,	and	 _L_
is	the	period	length	(29	days).

**Task 1d**
Calculate	the	 10	 days	momentum	(MOM),	give	the	formula	below.

𝑀𝑂𝑀(𝑥, 𝑡)=𝑃!−𝑃'

where	 _t_ is	the	current	price,	and	 _x_ is	the	number	of	days ( 10	 days).

## Task 2: Trading signals (10%)

**Task 2a**
Use	the	two	MA	indicators	from	above	to	generate	buy	and	sell	signals.	For	each	indicator	entry,	
you	should	compare	MA_1 2	 (1 2	 days	moving	average)	to	MA_ 26	 ( 26	 days	moving	average),	and	
generate	signals	in	the	following	manner:

If	MA_1 2	 >	MA_ 26	 =>	1	(buy)
If	MA_1 2	 <	MA_ 26	 =>	2	(sell)
If	MA_1 2	 =	MA_ 26	 =>	0	(hold)

**Task 2b**
Use	the	TBR	indicator	above	to	generate	signals	in	the	following	manner:

If	TBR_24	>	–0.02	=> 2	(sell)
If	TBR_24	<	–0.02	=>	1	(buy)
If	TBR_24	=	–0.02	=>	0	(hold)

**Task 2c**
Use	the	VOL indicator	above	to	generate	signals	in	the	following	manner:

If	VOL_29	>	0.02	=>	1	(buy)
If	VOL_29	<	0.02	=>	2	(sell)
If	VOL_29	=	0.02	=>	0	(hold)

**Task 2d**
Use	the	MOM indicator	above	to	generate	signals	in	the	following	manner:

If	MOM_ 10	 >	0	=>	1	(buy)
If	MOM_ 10	 <	0	=>	2	(sell)
If	MOM_ 10	 =	0	=>	0	(hold)


# PART B: Genetic Algorithm

## Task 1 (60%)

Use	 a	 GA	 to	 combine	 the	 output	 of	 the	 trading	 signals	 from	 Task	 2a–2d.	 For	 example,	 the	
indicator	may	generate	the	following	signals:

Task	2a	=>	BUY
Task	2b	=>	BUY
Task	2c	=>	HOLD
Task	2d	=>	SELL

In this	case,	you	could	choose	to	BUY	since	the	majority	of	trading	signals	are	recommending	
this	action.	We	say	that	all	signals	have	the	same	weight	in	this	case.

Your	 task	 is	 to	 implement	 a	 GA	 to	 evolve	 a	 set	 of	 weights	 (one	 for	 each	 trading	 signal)	 to	
determine	 an	 optimal	 trading	 action.	 Your	 individual	 representation	 should	 associate	 a	
numeric	weight	(between	0	and	1)	to	each	trading	signal:

0.4	x	Task	2a	=>	BUY
0.2	x	Task	2b	=>	BUY
0.1	x	Task	2c	=>	HOLD
0.8	x	Task	2d	=>	SELL

In	this	case,	the	strategy	decides	to	SELL,	giving	that	it	is	the	signal	associate	with	the	highest	
weight:	BUY	=	0.4	+	0.2,	HOLD	=	0.1	and	SELL	=	0.8.	Your	implementation	should	include:

- **individual	 representation	 [10%]** :	 for	 correct	 representation,	 one	 weight	 value	
    between	0	and	1	per	trading	signal; the	population	should	be	randomly	initialised;
- **fitness	 function	 [25%]** :	 for	 correct	 evaluation	 of	 candidate	 solutions.	 The	 fitness	
    function	should	operate	with	an	initial	budget	of	£ 3 000	and	stock	amount	of	0. For	each	
    value	on	the	training	data,	it	should	generate	a	trading	signal.	Every	BUY	action	should	
    deduct	the	amount	from	the	budget	and	only	be	performed	if	there	is	sufficient	budget,	
    and	increase	the	stock	amount;	every	SELL	action	should	deduct	the	stock	amount	and	
    only	 be performed	 if	 there	 is	 sufficient	 stocks,	 and	 increase	 the	 budget	 accordingly;	
    HOLD	actions	have	no	effect.	The	fitness	of	a	solution	is	the	total	cash	balance	at	the	end	
    of	trading	(budget	+	stock),	where	the	stocks	should	be	converted	to	cash	using	the	last	
    value	of	the	training	period;
- **selection	method	[10%]** :	the	GA	should	use	tournament	selection;
- **genetic	operators	[10%]** :	the	GA	should	use	one	mutation	and	one	crossover	operator	
    of	your	choice.	The	mutation	operator	should	only	generate	new	real	values	between	0	
    and	1;
- **termination	criteria	[5%]** :	a	maximum	number	of	generations	should	indicate	the	end	
    of	the	evolutionary	process.

At	the	end	of	the	evolutionary	process,	the	best	weight	configuration	should	be	returned.


# PART C: Presentation

## Task 1 (20%)

Prepare	powerpoint	slides	to	make	a	5-minute	presentation	discussing your	implementation	and	
results.	Topics	you	should	cover	in	your	slides:

- How	much	you	managed	to	achieve	in	terms	of	the	given	tasks.
- Difficulties	during	your	implementation:	what	has	gone	well,	what	has	gone	wrong.
- Would	you	do	anything	differently	if	you	had	to	re-do the assignment.
- Report	on	your	experimental	results,	including	summary	statistics from	multiple	runs.	
    If	you	haven’t	obtained	any	results	yet,	that	is	fine,	but	you	should	still	mention	that	you	
    don’t	have	any	results.
- Report	on	different	GA	parameters	that	you	might	have	used,	and	how/if	they	affected	
    the	performance	of	the	algorithm.	If	you’ve	tried	different	parameters,	you	should	also	
    present	statistical	analysis	to	support	your	argument.
- Anything	 else	 you	 consider	 useful	 to	 mention,	 e.g.	 any	 additional	 methods	
    implementation	you	decided	to	implement.

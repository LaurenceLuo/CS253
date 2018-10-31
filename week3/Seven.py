#!/usr/bin/env python
import re, sys, operator

# Mileage may vary. If this crashes, make it lower
RECURSION_LIMIT = 9500
# We add a few more, because, contrary to the name,
# this doesn't just rule recursion: it rules the 
# depth of the call stack
sys.setrecursionlimit(RECURSION_LIMIT+10)


def count(word_list, stopwords, wordfreqs):
    word = word_list[0]
    if word not in stopwords:
        if word in wordfreqs:
            wordfreqs[word] += 1
        else:
            wordfreqs[word] = 1

def wf_print(wordfreq):
    (w, c) = wordfreq[0]
    print w, ' - ', c

stop_words = set(open('../stop_words.txt').read().split(','))
words = re.findall('[a-z]{2,}', open(sys.argv[1]).read().lower())
word_freqs = {}

Y = lambda f: (lambda x: (lambda m: f(x(x))(m)))(lambda x: (lambda m: f(x(x))(m)))
for i in range(0, len(words), RECURSION_LIMIT):
    Y(lambda f: lambda wl: lambda sw: lambda wf: None if wl == [] else (count(wl, sw, wf), f(wl[1:])(sw)(wf)))(words[i:i+RECURSION_LIMIT])(stop_words)(word_freqs)

Y = lambda F: F(lambda x: Y(F)(x))
Y(lambda f: lambda wf: None if wf == [] else ( wf_print(wf), f(wf[1:]) ))(sorted(word_freqs.iteritems(), key=operator.itemgetter(1), reverse=True)[:25])


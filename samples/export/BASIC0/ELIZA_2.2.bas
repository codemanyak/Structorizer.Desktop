10 REM Concept and lisp implementation published by Joseph Weizenbaum (MIT): 
20 REM "ELIZA - A Computer Program For the Study of Natural Language Communication Between Man and Machine" - In: 
30 REM Computational Linguistis 1(1966)9, pp. 36-45 
40 REM Revision history: 
50 REM 2016-10-06 Initial version 
60 REM 2017-03-29 Two diagrams updated (comments translated to English) 
70 REM 2017-03-29 More keywords and replies added 
80 REM 2019-03-14 Replies and mapping reorganised for easier maintenance 
90 REM 2019-03-15 key map joined from keyword array and index map 
100 REM 2019-03-28 Keyword "bot" inserted (same reply ring as "computer") 
110 REM Generated by Structorizer 3.30-11 
120 
130 REM Copyright (C) 2018-05-14 ??? 
140 REM License: GPLv3-link 
150 REM GNU General Public License (V 3) 
160 REM https://www.gnu.org/licenses/gpl.html 
170 REM http://www.gnu.de/documents/gpl.de.html 
180 
190 REM  
200 REM program ELIZA
210 REM TODO: add the respective type suffixes to your variable names if required 
220 REM Title information 
230 PRINT "************* ELIZA **************"
240 PRINT "* Original design by J. Weizenbaum"
250 PRINT "**********************************"
260 PRINT "* Adapted for Basic on IBM PC by"
270 PRINT "* - Patricia Danielson"
280 PRINT "* - Paul Hashfield"
290 PRINT "**********************************"
300 PRINT "* Adapted for Structorizer by"
310 PRINT "* - Kay Gürtzig / FH Erfurt 2016"
320 PRINT "* Version: 2.2 (2019-03-28)"
330 PRINT "**********************************"
340 REM Stores the last five inputs of the user in a ring buffer, 
350 REM the first element is the current insertion index 
360 REM TODO: Check indexBase value (automatically generated) 
370 LET indexBase = 0
380 LET history(indexBase + 0) = 0
390 LET history(indexBase + 1) = ""
400 LET history(indexBase + 2) = ""
410 LET history(indexBase + 3) = ""
420 LET history(indexBase + 4) = ""
430 LET history(indexBase + 5) = ""
440 LET replies = setupReplies()
450 LET reflexions = setupReflexions()
460 LET byePhrases = setupGoodByePhrases()
470 LET keyMap = setupKeywords()
480 LET offsets(length(keyMap)-1) = 0
490 LET isGone = false
500 REM Starter 
510 PRINT "Hi! I\'m your new therapist. My name is Eliza. What\'s your problem?"
520 DO
530   INPUT userInput
540   REM Converts the input to lowercase, cuts out interpunctation 
550   REM and pads the string 
560   LET userInput = normalizeInput(userInput)
570   LET isGone = checkGoodBye(userInput, byePhrases)
580   IF NOT isGone THEN
590     LET reply = "Please don\'t repeat yourself!"
600     LET isRepeated = checkRepetition(history, userInput)
610     IF NOT isRepeated THEN
620       LET findInfo = findKeyword(keyMap, userInput)
630       LET keyIndex = findInfo(0)
640       IF keyIndex < 0 THEN
650         REM Should never happen... 
660         LET keyIndex = length(keyMap)-1
670       END IF
680       LET var entry: KeyMapEntry = keyMap(keyIndex)
690       REM Variable part of the reply 
700       LET varPart = ""
710       IF length(entry.keyword) > 0 THEN
720         LET varPart = conjugateStrings(userInput, entry.keyword, findInfo(1), reflexions)
730       END IF
740       LET replyRing = replies(entry.index)
750       LET reply = replyRing(offsets(keyIndex))
760       LET offsets(keyIndex) = (offsets(keyIndex) + 1) % length(replyRing)
770       LET posAster = pos("*", reply)
780       IF posAster > 0 THEN
790         IF varPart = " " THEN
800           LET reply = "You will have to elaborate more for me to help you."
810         ELSE
820           delete(reply, posAster, 1)
830           insert(varPart, reply, posAster)
840         END IF
850       END IF
860       LET reply = adjustSpelling(reply)
870     END IF
880     PRINT reply
890   END IF
900 LOOP UNTIL isGone
910 END
920 REM  
930 REM Cares for correct letter case among others 
940 REM TODO: Add type-specific suffixes where necessary! 
950 FUNCTION adjustSpelling(sentence AS String) AS String
960   REM TODO: add the respective type suffixes to your variable names if required 
970   LET result = sentence
980   LET position = 1
990   DO WHILE (position <= length(sentence)) AND (copy(sentence, position, 1) = " ")
1000     LET position = position + 1
1010   LOOP
1020   IF position <= length(sentence) THEN
1030     LET start = copy(sentence, 1, position)
1040     delete(result, 1, position)
1050     insert(uppercase(start), result, 1)
1060   END IF
1070   DIM arrayd52b53f() AS String = {" i ", " i\'"}
1080   FOR EACH word IN arrayd52b53f
1090     LET position = pos(word, result)
1100     DO WHILE position > 0
1110       delete(result, position+1, 1)
1120       insert("I", result, position+1)
1130       LET position = pos(word, result)
1140     LOOP
1150   NEXT word
1160   RETURN result
1170 END FUNCTION
1180 REM  
1190 REM Checks whether the given text contains some kind of 
1200 REM good-bye phrase inducing the end of the conversation 
1210 REM and if so writes a correspding good-bye message and 
1220 REM returns true, otherwise false 
1230 REM TODO: Add type-specific suffixes where necessary! 
1240 FUNCTION checkGoodBye(text AS String, phrases AS array of array[0..1] of string) AS boolean
1250   REM TODO: add the respective type suffixes to your variable names if required 
1260   FOR EACH pair IN phrases
1270     IF pos(pair(0), text) > 0 THEN
1280       LET saidBye = true
1290       PRINT pair(1)
1300       RETURN true
1310     END IF
1320   NEXT pair
1330   return false
1340 END FUNCTION
1350 REM  
1360 REM Checks whether newInput has occurred among the last 
1370 REM length(history) - 1 input strings and updates the history 
1380 REM TODO: Add type-specific suffixes where necessary! 
1390 FUNCTION checkRepetition(history AS array, newInput AS String) AS boolean
1400   REM TODO: add the respective type suffixes to your variable names if required 
1410   LET hasOccurred = false
1420   IF length(newInput) > 4 THEN
1430     LET currentIndex = history(0);
1440     FOR i = 1 TO length(history)-1
1450       IF newInput = history(i) THEN
1460         LET hasOccurred = true
1470       END IF
1480     NEXT i
1490     LET history(history(0)+1) = newInput
1500     LET history(0) = (history(0) + 1) % (length(history) - 1)
1510   END IF
1520   return hasOccurred
1530 END FUNCTION
1540 REM  
1550 REM TODO: Add type-specific suffixes where necessary! 
1560 FUNCTION conjugateStrings(sentence AS String, key AS String, keyPos AS integer, flexions AS array of array[0..1] of string) AS String
1570   REM TODO: add the respective type suffixes to your variable names if required 
1580   LET result = " " + copy(sentence, keyPos + length(key), length(sentence)) + " "
1590   FOR EACH pair IN flexions
1600     LET left = ""
1610     LET right = result
1620     LET position = pos(pair(0), right)
1630     DO WHILE position > 0
1640       LET left = left + copy(right, 1, position-1) + pair(1)
1650       LET right = copy(right, position + length(pair(0)), length(right))
1660       LET position = pos(pair(0), right)
1670     LOOP
1680     LET result = left + right
1690   NEXT pair
1700   REM Eliminate multiple spaces 
1710   LET position = pos("  ", result)
1720   DO WHILE position > 0
1730     LET result = copy(result, 1, position-1) + copy(result, position+1, length(result))
1740     LET position = pos("  ", result)
1750   LOOP
1760   RETURN result
1770 END FUNCTION
1780 REM  
1790 REM Looks for the occurrence of the first of the strings 
1800 REM contained in keywords within the given sentence (in 
1810 REM array order). 
1820 REM Returns an array of 
1830 REM 0: the index of the first identified keyword (if any, otherwise -1), 
1840 REM 1: the position inside sentence (0 if not found) 
1850 REM TODO: Add type-specific suffixes where necessary! 
1860 FUNCTION findKeyword(keyMap AS const array of KeyMapEntry, sentence AS String) AS array[0..1] of integer
1870   REM TODO: add the respective type suffixes to your variable names if required 
1880   REM Contains the index of the keyword and its position in sentence 
1890   REM TODO: Check indexBase value (automatically generated) 
1900   LET indexBase = 0
1910   LET result(indexBase + 0) = -1
1920   LET result(indexBase + 1) = 0
1930   LET i = 0
1940   DO WHILE (result(0) < 0) AND (i < length(keyMap))
1950     LET var entry: KeyMapEntry = keyMap(i)
1960     LET position = pos(entry.keyword, sentence)
1970     IF position > 0 THEN
1980       LET result(0) = i
1990       LET result(1) = position
2000     END IF
2010     LET i = i+1
2020   LOOP
2030   RETURN result
2040 END FUNCTION
2050 REM  
2060 REM Converts the sentence to lowercase, eliminates all 
2070 REM interpunction (i.e. ',', '.', ';'), and pads the 
2080 REM sentence among blanks 
2090 REM TODO: Add type-specific suffixes where necessary! 
2100 FUNCTION normalizeInput(sentence AS String) AS String
2110   REM TODO: add the respective type suffixes to your variable names if required 
2120   LET sentence = lowercase(sentence)
2130   REM TODO: Specify an appropriate element type for the array! 
2140   DIM array80b588f9() AS FIXME_80b588f9 = {'.', ',', ';', '!', '?'}
2150   FOR EACH symbol IN array80b588f9
2160     LET position = pos(symbol, sentence)
2170     DO WHILE position > 0
2180       LET sentence = copy(sentence, 1, position-1) + copy(sentence, position+1, length(sentence))
2190       LET position = pos(symbol, sentence)
2200     LOOP
2210   NEXT symbol
2220   LET result = " " + sentence + " "
2230   RETURN result
2240 END FUNCTION
2250 REM  
2260 REM TODO: Add type-specific suffixes where necessary! 
2270 FUNCTION setupGoodByePhrases() AS array of array[0..1] of string
2280   REM TODO: add the respective type suffixes to your variable names if required 
2290   REM TODO: Check indexBase value (automatically generated) 
2300   LET indexBase = 0
2310   LET phrases(0)(indexBase + 0) = " shut"
2320   LET phrases(0)(indexBase + 1) = "Okay. If you feel that way I\'ll shut up. ... Your choice."
2330   REM TODO: Check indexBase value (automatically generated) 
2340   LET indexBase = 0
2350   LET phrases(1)(indexBase + 0) = "bye"
2360   LET phrases(1)(indexBase + 1) = "Well, let\'s end our talk for now. See you later. Bye."
2370   return phrases
2380 END FUNCTION
2390 REM  
2400 REM The lower the index the higher the rank of the keyword (search is sequential). 
2410 REM The index of the first keyword found in a user sentence maps to a respective 
2420 REM reply ring as defined in `setupReplies()´. 
2430 REM TODO: Add type-specific suffixes where necessary! 
2440 FUNCTION setupKeywords() AS array of KeyMapEntry
2450   REM TODO: add the respective type suffixes to your variable names if required 
2460   REM The empty key string (last entry) is the default clause - will always be found 
2470   LET keywords(39).keyword = ""
2480   LET keywords(39).index = 29
2490   LET keywords(0).keyword = "can you "
2500   LET keywords(0).index = 0
2510   LET keywords(1).keyword = "can i "
2520   LET keywords(1).index = 1
2530   LET keywords(2).keyword = "you are "
2540   LET keywords(2).index = 2
2550   LET keywords(3).keyword = "you\'re "
2560   LET keywords(3).index = 2
2570   LET keywords(4).keyword = "i don't "
2580   LET keywords(4).index = 3
2590   LET keywords(5).keyword = "i feel "
2600   LET keywords(5).index = 4
2610   LET keywords(6).keyword = "why don\'t you "
2620   LET keywords(6).index = 5
2630   LET keywords(7).keyword = "why can\'t i "
2640   LET keywords(7).index = 6
2650   LET keywords(8).keyword = "are you "
2660   LET keywords(8).index = 7
2670   LET keywords(9).keyword = "i can\'t "
2680   LET keywords(9).index = 8
2690   LET keywords(10).keyword = "i am "
2700   LET keywords(10).index = 9
2710   LET keywords(11).keyword = "i\'m "
2720   LET keywords(11).index = 9
2730   LET keywords(12).keyword = "you "
2740   LET keywords(12).index = 10
2750   LET keywords(13).keyword = "i want "
2760   LET keywords(13).index = 11
2770   LET keywords(14).keyword = "what "
2780   LET keywords(14).index = 12
2790   LET keywords(15).keyword = "how "
2800   LET keywords(15).index = 12
2810   LET keywords(16).keyword = "who "
2820   LET keywords(16).index = 12
2830   LET keywords(17).keyword = "where "
2840   LET keywords(17).index = 12
2850   LET keywords(18).keyword = "when "
2860   LET keywords(18).index = 12
2870   LET keywords(19).keyword = "why "
2880   LET keywords(19).index = 12
2890   LET keywords(20).keyword = "name "
2900   LET keywords(20).index = 13
2910   LET keywords(21).keyword = "cause "
2920   LET keywords(21).index = 14
2930   LET keywords(22).keyword = "sorry "
2940   LET keywords(22).index = 15
2950   LET keywords(23).keyword = "dream "
2960   LET keywords(23).index = 16
2970   LET keywords(24).keyword = "hello "
2980   LET keywords(24).index = 17
2990   LET keywords(25).keyword = "hi "
3000   LET keywords(25).index = 17
3010   LET keywords(26).keyword = "maybe "
3020   LET keywords(26).index = 18
3030   LET keywords(27).keyword = " no"
3040   LET keywords(27).index = 19
3050   LET keywords(28).keyword = "your "
3060   LET keywords(28).index = 20
3070   LET keywords(29).keyword = "always "
3080   LET keywords(29).index = 21
3090   LET keywords(30).keyword = "think "
3100   LET keywords(30).index = 22
3110   LET keywords(31).keyword = "alike "
3120   LET keywords(31).index = 23
3130   LET keywords(32).keyword = "yes "
3140   LET keywords(32).index = 24
3150   LET keywords(33).keyword = "friend "
3160   LET keywords(33).index = 25
3170   LET keywords(34).keyword = "computer"
3180   LET keywords(34).index = 26
3190   LET keywords(35).keyword = "bot "
3200   LET keywords(35).index = 26
3210   LET keywords(36).keyword = "smartphone"
3220   LET keywords(36).index = 27
3230   LET keywords(37).keyword = "father "
3240   LET keywords(37).index = 28
3250   LET keywords(38).keyword = "mother "
3260   LET keywords(38).index = 28
3270   return keywords
3280 END FUNCTION
3290 REM  
3300 REM Returns an array of pairs of mutualy substitutable  
3310 REM TODO: Add type-specific suffixes where necessary! 
3320 FUNCTION setupReflexions() AS array of array[0..1] of string
3330   REM TODO: add the respective type suffixes to your variable names if required 
3340   REM TODO: Check indexBase value (automatically generated) 
3350   LET indexBase = 0
3360   LET reflexions(0)(indexBase + 0) = " are "
3370   LET reflexions(0)(indexBase + 1) = " am "
3380   REM TODO: Check indexBase value (automatically generated) 
3390   LET indexBase = 0
3400   LET reflexions(1)(indexBase + 0) = " were "
3410   LET reflexions(1)(indexBase + 1) = " was "
3420   REM TODO: Check indexBase value (automatically generated) 
3430   LET indexBase = 0
3440   LET reflexions(2)(indexBase + 0) = " you "
3450   LET reflexions(2)(indexBase + 1) = " I "
3460   REM TODO: Check indexBase value (automatically generated) 
3470   LET indexBase = 0
3480   LET reflexions(3)(indexBase + 0) = " your"
3490   LET reflexions(3)(indexBase + 1) = " my"
3500   REM TODO: Check indexBase value (automatically generated) 
3510   LET indexBase = 0
3520   LET reflexions(4)(indexBase + 0) = " i\'ve "
3530   LET reflexions(4)(indexBase + 1) = " you\'ve "
3540   REM TODO: Check indexBase value (automatically generated) 
3550   LET indexBase = 0
3560   LET reflexions(5)(indexBase + 0) = " i\'m "
3570   LET reflexions(5)(indexBase + 1) = " you\'re "
3580   REM TODO: Check indexBase value (automatically generated) 
3590   LET indexBase = 0
3600   LET reflexions(6)(indexBase + 0) = " me "
3610   LET reflexions(6)(indexBase + 1) = " you "
3620   REM TODO: Check indexBase value (automatically generated) 
3630   LET indexBase = 0
3640   LET reflexions(7)(indexBase + 0) = " my "
3650   LET reflexions(7)(indexBase + 1) = " your "
3660   REM TODO: Check indexBase value (automatically generated) 
3670   LET indexBase = 0
3680   LET reflexions(8)(indexBase + 0) = " i "
3690   LET reflexions(8)(indexBase + 1) = " you "
3700   REM TODO: Check indexBase value (automatically generated) 
3710   LET indexBase = 0
3720   LET reflexions(9)(indexBase + 0) = " am "
3730   LET reflexions(9)(indexBase + 1) = " are "
3740   return reflexions
3750 END FUNCTION
3760 REM  
3770 REM This routine sets up the reply rings addressed by the key words defined in 
3780 REM routine `setupKeywords()´ and mapped hitherto by the cross table defined 
3790 REM in `setupMapping()´ 
3800 REM TODO: Add type-specific suffixes where necessary! 
3810 FUNCTION setupReplies() AS array of array of string
3820   REM TODO: add the respective type suffixes to your variable names if required 
3830   var replies: array of array of String
3840   REM We start with the highest index for performance reasons 
3850   REM (is to avoid frequent array resizing) 
3860   REM TODO: Check indexBase value (automatically generated) 
3870   LET indexBase = 0
3880   LET replies(29)(indexBase + 0) = "Say, do you have any psychological problems?"
3890   LET replies(29)(indexBase + 1) = "What does that suggest to you?"
3900   LET replies(29)(indexBase + 2) = "I see."
3910   LET replies(29)(indexBase + 3) = "I'm not sure I understand you fully."
3920   LET replies(29)(indexBase + 4) = "Come come elucidate your thoughts."
3930   LET replies(29)(indexBase + 5) = "Can you elaborate on that?"
3940   LET replies(29)(indexBase + 6) = "That is quite interesting."
3950   REM TODO: Check indexBase value (automatically generated) 
3960   LET indexBase = 0
3970   LET replies(0)(indexBase + 0) = "Don't you believe that I can*?"
3980   LET replies(0)(indexBase + 1) = "Perhaps you would like to be like me?"
3990   LET replies(0)(indexBase + 2) = "You want me to be able to*?"
4000   REM TODO: Check indexBase value (automatically generated) 
4010   LET indexBase = 0
4020   LET replies(1)(indexBase + 0) = "Perhaps you don't want to*?"
4030   LET replies(1)(indexBase + 1) = "Do you want to be able to*?"
4040   REM TODO: Check indexBase value (automatically generated) 
4050   LET indexBase = 0
4060   LET replies(2)(indexBase + 0) = "What makes you think I am*?"
4070   LET replies(2)(indexBase + 1) = "Does it please you to believe I am*?"
4080   LET replies(2)(indexBase + 2) = "Perhaps you would like to be*?"
4090   LET replies(2)(indexBase + 3) = "Do you sometimes wish you were*?"
4100   REM TODO: Check indexBase value (automatically generated) 
4110   LET indexBase = 0
4120   LET replies(3)(indexBase + 0) = "Don't you really*?"
4130   LET replies(3)(indexBase + 1) = "Why don't you*?"
4140   LET replies(3)(indexBase + 2) = "Do you wish to be able to*?"
4150   LET replies(3)(indexBase + 3) = "Does that trouble you*?"
4160   REM TODO: Check indexBase value (automatically generated) 
4170   LET indexBase = 0
4180   LET replies(4)(indexBase + 0) = "Do you often feel*?"
4190   LET replies(4)(indexBase + 1) = "Are you afraid of feeling*?"
4200   LET replies(4)(indexBase + 2) = "Do you enjoy feeling*?"
4210   REM TODO: Check indexBase value (automatically generated) 
4220   LET indexBase = 0
4230   LET replies(5)(indexBase + 0) = "Do you really believe I don't*?"
4240   LET replies(5)(indexBase + 1) = "Perhaps in good time I will*."
4250   LET replies(5)(indexBase + 2) = "Do you want me to*?"
4260   REM TODO: Check indexBase value (automatically generated) 
4270   LET indexBase = 0
4280   LET replies(6)(indexBase + 0) = "Do you think you should be able to*?"
4290   LET replies(6)(indexBase + 1) = "Why can't you*?"
4300   REM TODO: Check indexBase value (automatically generated) 
4310   LET indexBase = 0
4320   LET replies(7)(indexBase + 0) = "Why are you interested in whether or not I am*?"
4330   LET replies(7)(indexBase + 1) = "Would you prefer if I were not*?"
4340   LET replies(7)(indexBase + 2) = "Perhaps in your fantasies I am*?"
4350   REM TODO: Check indexBase value (automatically generated) 
4360   LET indexBase = 0
4370   LET replies(8)(indexBase + 0) = "How do you know you can't*?"
4380   LET replies(8)(indexBase + 1) = "Have you tried?"
4390   LET replies(8)(indexBase + 2) = "Perhaps you can now*."
4400   REM TODO: Check indexBase value (automatically generated) 
4410   LET indexBase = 0
4420   LET replies(9)(indexBase + 0) = "Did you come to me because you are*?"
4430   LET replies(9)(indexBase + 1) = "How long have you been*?"
4440   LET replies(9)(indexBase + 2) = "Do you believe it is normal to be*?"
4450   LET replies(9)(indexBase + 3) = "Do you enjoy being*?"
4460   REM TODO: Check indexBase value (automatically generated) 
4470   LET indexBase = 0
4480   LET replies(10)(indexBase + 0) = "We were discussing you--not me."
4490   LET replies(10)(indexBase + 1) = "Oh, I*."
4500   LET replies(10)(indexBase + 2) = "You're not really talking about me, are you?"
4510   REM TODO: Check indexBase value (automatically generated) 
4520   LET indexBase = 0
4530   LET replies(11)(indexBase + 0) = "What would it mean to you if you got*?"
4540   LET replies(11)(indexBase + 1) = "Why do you want*?"
4550   LET replies(11)(indexBase + 2) = "Suppose you soon got*..."
4560   LET replies(11)(indexBase + 3) = "What if you never got*?"
4570   LET replies(11)(indexBase + 4) = "I sometimes also want*."
4580   REM TODO: Check indexBase value (automatically generated) 
4590   LET indexBase = 0
4600   LET replies(12)(indexBase + 0) = "Why do you ask?"
4610   LET replies(12)(indexBase + 1) = "Does that question interest you?"
4620   LET replies(12)(indexBase + 2) = "What answer would please you the most?"
4630   LET replies(12)(indexBase + 3) = "What do you think?"
4640   LET replies(12)(indexBase + 4) = "Are such questions on your mind often?"
4650   LET replies(12)(indexBase + 5) = "What is it that you really want to know?"
4660   LET replies(12)(indexBase + 6) = "Have you asked anyone else?"
4670   LET replies(12)(indexBase + 7) = "Have you asked such questions before?"
4680   LET replies(12)(indexBase + 8) = "What else comes to mind when you ask that?"
4690   REM TODO: Check indexBase value (automatically generated) 
4700   LET indexBase = 0
4710   LET replies(13)(indexBase + 0) = "Names don't interest me."
4720   LET replies(13)(indexBase + 1) = "I don't care about names -- please go on."
4730   REM TODO: Check indexBase value (automatically generated) 
4740   LET indexBase = 0
4750   LET replies(14)(indexBase + 0) = "Is that the real reason?"
4760   LET replies(14)(indexBase + 1) = "Don't any other reasons come to mind?"
4770   LET replies(14)(indexBase + 2) = "Does that reason explain anything else?"
4780   LET replies(14)(indexBase + 3) = "What other reasons might there be?"
4790   REM TODO: Check indexBase value (automatically generated) 
4800   LET indexBase = 0
4810   LET replies(15)(indexBase + 0) = "Please don't apologize!"
4820   LET replies(15)(indexBase + 1) = "Apologies are not necessary."
4830   LET replies(15)(indexBase + 2) = "What feelings do you have when you apologize?"
4840   LET replies(15)(indexBase + 3) = "Don't be so defensive!"
4850   REM TODO: Check indexBase value (automatically generated) 
4860   LET indexBase = 0
4870   LET replies(16)(indexBase + 0) = "What does that dream suggest to you?"
4880   LET replies(16)(indexBase + 1) = "Do you dream often?"
4890   LET replies(16)(indexBase + 2) = "What persons appear in your dreams?"
4900   LET replies(16)(indexBase + 3) = "Are you disturbed by your dreams?"
4910   REM TODO: Check indexBase value (automatically generated) 
4920   LET indexBase = 0
4930   LET replies(17)(indexBase + 0) = "How do you do ...please state your problem."
4940   REM TODO: Check indexBase value (automatically generated) 
4950   LET indexBase = 0
4960   LET replies(18)(indexBase + 0) = "You don't seem quite certain."
4970   LET replies(18)(indexBase + 1) = "Why the uncertain tone?"
4980   LET replies(18)(indexBase + 2) = "Can't you be more positive?"
4990   LET replies(18)(indexBase + 3) = "You aren't sure?"
5000   LET replies(18)(indexBase + 4) = "Don't you know?"
5010   REM TODO: Check indexBase value (automatically generated) 
5020   LET indexBase = 0
5030   LET replies(19)(indexBase + 0) = "Are you saying no just to be negative?"
5040   LET replies(19)(indexBase + 1) = "You are being a bit negative."
5050   LET replies(19)(indexBase + 2) = "Why not?"
5060   LET replies(19)(indexBase + 3) = "Are you sure?"
5070   LET replies(19)(indexBase + 4) = "Why no?"
5080   REM TODO: Check indexBase value (automatically generated) 
5090   LET indexBase = 0
5100   LET replies(20)(indexBase + 0) = "Why are you concerned about my*?"
5110   LET replies(20)(indexBase + 1) = "What about your own*?"
5120   REM TODO: Check indexBase value (automatically generated) 
5130   LET indexBase = 0
5140   LET replies(21)(indexBase + 0) = "Can you think of a specific example?"
5150   LET replies(21)(indexBase + 1) = "When?"
5160   LET replies(21)(indexBase + 2) = "What are you thinking of?"
5170   LET replies(21)(indexBase + 3) = "Really, always?"
5180   REM TODO: Check indexBase value (automatically generated) 
5190   LET indexBase = 0
5200   LET replies(22)(indexBase + 0) = "Do you really think so?"
5210   LET replies(22)(indexBase + 1) = "But you are not sure you*?"
5220   LET replies(22)(indexBase + 2) = "Do you doubt you*?"
5230   REM TODO: Check indexBase value (automatically generated) 
5240   LET indexBase = 0
5250   LET replies(23)(indexBase + 0) = "In what way?"
5260   LET replies(23)(indexBase + 1) = "What resemblance do you see?"
5270   LET replies(23)(indexBase + 2) = "What does the similarity suggest to you?"
5280   LET replies(23)(indexBase + 3) = "What other connections do you see?"
5290   LET replies(23)(indexBase + 4) = "Could there really be some connection?"
5300   LET replies(23)(indexBase + 5) = "How?"
5310   LET replies(23)(indexBase + 6) = "You seem quite positive."
5320   REM TODO: Check indexBase value (automatically generated) 
5330   LET indexBase = 0
5340   LET replies(24)(indexBase + 0) = "Are you sure?"
5350   LET replies(24)(indexBase + 1) = "I see."
5360   LET replies(24)(indexBase + 2) = "I understand."
5370   REM TODO: Check indexBase value (automatically generated) 
5380   LET indexBase = 0
5390   LET replies(25)(indexBase + 0) = "Why do you bring up the topic of friends?"
5400   LET replies(25)(indexBase + 1) = "Do your friends worry you?"
5410   LET replies(25)(indexBase + 2) = "Do your friends pick on you?"
5420   LET replies(25)(indexBase + 3) = "Are you sure you have any friends?"
5430   LET replies(25)(indexBase + 4) = "Do you impose on your friends?"
5440   LET replies(25)(indexBase + 5) = "Perhaps your love for friends worries you."
5450   REM TODO: Check indexBase value (automatically generated) 
5460   LET indexBase = 0
5470   LET replies(26)(indexBase + 0) = "Do computers worry you?"
5480   LET replies(26)(indexBase + 1) = "Are you talking about me in particular?"
5490   LET replies(26)(indexBase + 2) = "Are you frightened by machines?"
5500   LET replies(26)(indexBase + 3) = "Why do you mention computers?"
5510   LET replies(26)(indexBase + 4) = "What do you think machines have to do with your problem?"
5520   LET replies(26)(indexBase + 5) = "Don't you think computers can help people?"
5530   LET replies(26)(indexBase + 6) = "What is it about machines that worries you?"
5540   REM TODO: Check indexBase value (automatically generated) 
5550   LET indexBase = 0
5560   LET replies(27)(indexBase + 0) = "Do you sometimes feel uneasy without a smartphone?"
5570   LET replies(27)(indexBase + 1) = "Have you had these phantasies before?"
5580   LET replies(27)(indexBase + 2) = "Does the world seem more real for you via apps?"
5590   REM TODO: Check indexBase value (automatically generated) 
5600   LET indexBase = 0
5610   LET replies(28)(indexBase + 0) = "Tell me more about your family."
5620   LET replies(28)(indexBase + 1) = "Who else in your family*?"
5630   LET replies(28)(indexBase + 2) = "What does family relations mean for you?"
5640   LET replies(28)(indexBase + 3) = "Come on, How old are you?"
5650   LET setupReplies = replies
5660   RETURN setupReplies
5670 END FUNCTION

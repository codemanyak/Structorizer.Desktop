<script>
// program ELIZA 
// Generated by Structorizer 3.30-11 

// Copyright (C) 2018-05-14 ??? 
// License: GPLv3-link 
// GNU General Public License (V 3) 
// https://www.gnu.org/licenses/gpl.html 
// http://www.gnu.de/documents/gpl.de.html 

var initDone_KeyMapEntry = false;

// function initialize_KeyMapEntry() 
// Automatically created initialization procedure for KeyMapEntry 
function initialize_KeyMapEntry() {
	if (! initDone_KeyMapEntry) {
		initDone_KeyMapEntry = true;
	}
}

// function adjustSpelling(sentence: string): string 
// Cares for correct letter case among others 
function adjustSpelling(sentence) {
	var word;
	var start;
	var result;
	var position;

	result = sentence;
	position = 1;
	while ((position <= length(sentence)) && (copy(sentence, position, 1) == " ")) {
		position = position + 1;
	}
	if (position <= length(sentence)) {
		start = copy(sentence, 1, position);
		delete(result, 1, position);
		insert(uppercase(start), result, 1);
	}
	for (const word of [" i ", " i\'"]) {
		position = pos(word, result);
		while (position > 0) {
			delete(result, position+1, 1);
			insert("I", result, position+1);
			position = pos(word, result);
		}
	}

	return result;
}

// function checkGoodBye(text: string; phrases: array of array[0..1] of string): boolean 
// Checks whether the given text contains some kind of 
// good-bye phrase inducing the end of the conversation 
// and if so writes a correspding good-bye message and 
// returns true, otherwise false 
function checkGoodBye(text, phrases) {
	var saidBye;
	var pair;

	for (const pair of phrases) {
		if (pos(pair[0], text) > 0) {
			saidBye = true;
			document.write((pair[1]) + "<br/>");
			return true;
		}
	}
	return false;
}

// function checkRepetition(history: array; newInput: string): boolean 
// Checks whether newInput has occurred among the last 
// length(history) - 1 input strings and updates the history 
function checkRepetition(history, newInput) {
	var i;
	var hasOccurred;
	var currentIndex;

	hasOccurred = false;
	if (length(newInput) > 4) {
		currentIndex = history[0];;
		for (i = 1; i <= length(history)-1; i += (1)) {
			if (newInput == history[i]) {
				hasOccurred = true;
			}
		}
		history[history[0]+1] = newInput;
		history[0] = (history[0] + 1) % (length(history) - 1);
	}
	return hasOccurred;
}

// function conjugateStrings(sentence: string; key: string; keyPos: integer; flexions: array of array[0..1] of string): string 
function conjugateStrings(sentence, key, keyPos, flexions) {
	var right;
	var result;
	var position;
	var pair;
	var left;

	result = " " + copy(sentence, keyPos + length(key), length(sentence)) + " ";
	for (const pair of flexions) {
		left = "";
		right = result;
		position = pos(pair[0], right);
		while (position > 0) {
			left = left + copy(right, 1, position-1) + pair[1];
			right = copy(right, position + length(pair[0]), length(right));
			position = pos(pair[0], right);
		}
		result = left + right;
	}
	// Eliminate multiple spaces 
	position = pos("  ", result);
	while (position > 0) {
		result = copy(result, 1, position-1) + copy(result, position+1, length(result));
		position = pos("  ", result);
	}

	return result;
}

// function findKeyword(const keyMap: array of KeyMapEntry; sentence: string): array[0..1] of integer 
// Looks for the occurrence of the first of the strings 
// contained in keywords within the given sentence (in 
// array order). 
// Returns an array of 
// 0: the index of the first identified keyword (if any, otherwise -1), 
// 1: the position inside sentence (0 if not found) 
function findKeyword(keyMap, sentence) {
	initialize_KeyMapEntry();
	
	var keyMap;
	var result;
	var position;
	var i;
	var entry;

	// Contains the index of the keyword and its position in sentence 
	result = [-1, 0];
	i = 0;
	while ((result[0] < 0) && (i < length(keyMap))) {
		entry = keyMap[i];
		position = pos(entry.keyword, sentence);
		if (position > 0) {
			result[0] = i;
			result[1] = position;
		}
		i = i+1;
	}

	return result;
}

// function normalizeInput(sentence: string): string 
// Converts the sentence to lowercase, eliminates all 
// interpunction (i.e. ',', '.', ';'), and pads the 
// sentence among blanks 
function normalizeInput(sentence) {
	var symbol;
	var result;
	var position;

	sentence = lowercase(sentence);
	for (const symbol of ['.', ',', ';', '!', '?']) {
		position = pos(symbol, sentence);
		while (position > 0) {
			sentence = copy(sentence, 1, position-1) + copy(sentence, position+1, length(sentence));
			position = pos(symbol, sentence);
		}
	}
	result = " " + sentence + " ";

	return result;
}

// function setupGoodByePhrases(): array of array[0..1] of string 
function setupGoodByePhrases() {
	var phrases;

	phrases[0] = [" shut", "Okay. If you feel that way I\'ll shut up. ... Your choice."];
	phrases[1] = ["bye", "Well, let\'s end our talk for now. See you later. Bye."];
	return phrases;
}

// function setupKeywords(): array of KeyMapEntry 
// The lower the index the higher the rank of the keyword (search is sequential). 
// The index of the first keyword found in a user sentence maps to a respective 
// reply ring as defined in `setupReplies()´. 
function setupKeywords() {
	initialize_KeyMapEntry();
	
	var keywords;

	// The empty key string (last entry) is the default clause - will always be found 
	keywords[39] = {keyword:"", index:29}
	keywords[0] = {keyword:"can you ", index:0}
	keywords[1] = {keyword:"can i ", index:1}
	keywords[2] = {keyword:"you are ", index:2}
	keywords[3] = {keyword:"you\'re ", index:2}
	keywords[4] = {keyword:"i don't ", index:3}
	keywords[5] = {keyword:"i feel ", index:4}
	keywords[6] = {keyword:"why don\'t you ", index:5}
	keywords[7] = {keyword:"why can\'t i ", index:6}
	keywords[8] = {keyword:"are you ", index:7}
	keywords[9] = {keyword:"i can\'t ", index:8}
	keywords[10] = {keyword:"i am ", index:9}
	keywords[11] = {keyword:"i\'m ", index:9}
	keywords[12] = {keyword:"you ", index:10}
	keywords[13] = {keyword:"i want ", index:11}
	keywords[14] = {keyword:"what ", index:12}
	keywords[15] = {keyword:"how ", index:12}
	keywords[16] = {keyword:"who ", index:12}
	keywords[17] = {keyword:"where ", index:12}
	keywords[18] = {keyword:"when ", index:12}
	keywords[19] = {keyword:"why ", index:12}
	keywords[20] = {keyword:"name ", index:13}
	keywords[21] = {keyword:"cause ", index:14}
	keywords[22] = {keyword:"sorry ", index:15}
	keywords[23] = {keyword:"dream ", index:16}
	keywords[24] = {keyword:"hello ", index:17}
	keywords[25] = {keyword:"hi ", index:17}
	keywords[26] = {keyword:"maybe ", index:18}
	keywords[27] = {keyword:" no", index:19}
	keywords[28] = {keyword:"your ", index:20}
	keywords[29] = {keyword:"always ", index:21}
	keywords[30] = {keyword:"think ", index:22}
	keywords[31] = {keyword:"alike ", index:23}
	keywords[32] = {keyword:"yes ", index:24}
	keywords[33] = {keyword:"friend ", index:25}
	keywords[34] = {keyword:"computer", index:26}
	keywords[35] = {keyword:"bot ", index:26}
	keywords[36] = {keyword:"smartphone", index:27}
	keywords[37] = {keyword:"father ", index:28}
	keywords[38] = {keyword:"mother ", index:28}
	return keywords;
}

// function setupReflexions(): array of array[0..1] of string 
// Returns an array of pairs of mutualy substitutable  
function setupReflexions() {
	var reflexions;

	reflexions[0] = [" are ", " am "];
	reflexions[1] = [" were ", " was "];
	reflexions[2] = [" you ", " I "];
	reflexions[3] = [" your", " my"];
	reflexions[4] = [" i\'ve ", " you\'ve "];
	reflexions[5] = [" i\'m ", " you\'re "];
	reflexions[6] = [" me ", " you "];
	reflexions[7] = [" my ", " your "];
	reflexions[8] = [" i ", " you "];
	reflexions[9] = [" am ", " are "];
	return reflexions;
}

// function setupReplies(): array of array of string 
// This routine sets up the reply rings addressed by the key words defined in 
// routine `setupKeywords()´ and mapped hitherto by the cross table defined 
// in `setupMapping()´ 
function setupReplies() {
	var setupReplies;
	var replies;

	// We start with the highest index for performance reasons 
	// (is to avoid frequent array resizing) 
	replies[29] = ["Say, do you have any psychological problems?", "What does that suggest to you?", "I see.", "I'm not sure I understand you fully.", "Come come elucidate your thoughts.", "Can you elaborate on that?", "That is quite interesting."];
	replies[0] = ["Don't you believe that I can*?", "Perhaps you would like to be like me?", "You want me to be able to*?"];
	replies[1] = ["Perhaps you don't want to*?", "Do you want to be able to*?"];
	replies[2] = ["What makes you think I am*?", "Does it please you to believe I am*?", "Perhaps you would like to be*?", "Do you sometimes wish you were*?"];
	replies[3] = ["Don't you really*?", "Why don't you*?", "Do you wish to be able to*?", "Does that trouble you*?"];
	replies[4] = ["Do you often feel*?", "Are you afraid of feeling*?", "Do you enjoy feeling*?"];
	replies[5] = ["Do you really believe I don't*?", "Perhaps in good time I will*.", "Do you want me to*?"];
	replies[6] = ["Do you think you should be able to*?", "Why can't you*?"];
	replies[7] = ["Why are you interested in whether or not I am*?", "Would you prefer if I were not*?", "Perhaps in your fantasies I am*?"];
	replies[8] = ["How do you know you can't*?", "Have you tried?", "Perhaps you can now*."];
	replies[9] = ["Did you come to me because you are*?", "How long have you been*?", "Do you believe it is normal to be*?", "Do you enjoy being*?"];
	replies[10] = ["We were discussing you--not me.", "Oh, I*.", "You're not really talking about me, are you?"];
	replies[11] = ["What would it mean to you if you got*?", "Why do you want*?", "Suppose you soon got*...", "What if you never got*?", "I sometimes also want*."];
	replies[12] = ["Why do you ask?", "Does that question interest you?", "What answer would please you the most?", "What do you think?", "Are such questions on your mind often?", "What is it that you really want to know?", "Have you asked anyone else?", "Have you asked such questions before?", "What else comes to mind when you ask that?"];
	replies[13] = ["Names don't interest me.", "I don't care about names -- please go on."];
	replies[14] = ["Is that the real reason?", "Don't any other reasons come to mind?", "Does that reason explain anything else?", "What other reasons might there be?"];
	replies[15] = ["Please don't apologize!", "Apologies are not necessary.", "What feelings do you have when you apologize?", "Don't be so defensive!"];
	replies[16] = ["What does that dream suggest to you?", "Do you dream often?", "What persons appear in your dreams?", "Are you disturbed by your dreams?"];
	replies[17] = ["How do you do ...please state your problem."];
	replies[18] = ["You don't seem quite certain.", "Why the uncertain tone?", "Can't you be more positive?", "You aren't sure?", "Don't you know?"];
	replies[19] = ["Are you saying no just to be negative?", "You are being a bit negative.", "Why not?", "Are you sure?", "Why no?"];
	replies[20] = ["Why are you concerned about my*?", "What about your own*?"];
	replies[21] = ["Can you think of a specific example?", "When?", "What are you thinking of?", "Really, always?"];
	replies[22] = ["Do you really think so?", "But you are not sure you*?", "Do you doubt you*?"];
	replies[23] = ["In what way?", "What resemblance do you see?", "What does the similarity suggest to you?", "What other connections do you see?", "Could there really be some connection?", "How?", "You seem quite positive."];
	replies[24] = ["Are you sure?", "I see.", "I understand."];
	replies[25] = ["Why do you bring up the topic of friends?", "Do your friends worry you?", "Do your friends pick on you?", "Are you sure you have any friends?", "Do you impose on your friends?", "Perhaps your love for friends worries you."];
	replies[26] = ["Do computers worry you?", "Are you talking about me in particular?", "Are you frightened by machines?", "Why do you mention computers?", "What do you think machines have to do with your problem?", "Don't you think computers can help people?", "What is it about machines that worries you?"];
	replies[27] = ["Do you sometimes feel uneasy without a smartphone?", "Have you had these phantasies before?", "Does the world seem more real for you via apps?"];
	replies[28] = ["Tell me more about your family.", "Who else in your family*?", "What does family relations mean for you?", "Come on, How old are you?"];
	setupReplies = replies;

	return setupReplies;
}

// = = = = 8< = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = 

// Concept and lisp implementation published by Joseph Weizenbaum (MIT): 
// "ELIZA - A Computer Program For the Study of Natural Language Communication Between Man and Machine" - In: 
// Computational Linguistis 1(1966)9, pp. 36-45 
// Revision history: 
// 2016-10-06 Initial version 
// 2017-03-29 Two diagrams updated (comments translated to English) 
// 2017-03-29 More keywords and replies added 
// 2019-03-14 Replies and mapping reorganised for easier maintenance 
// 2019-03-15 key map joined from keyword array and index map 
// 2019-03-28 Keyword "bot" inserted (same reply ring as "computer") 
initialize_KeyMapEntry();

const replies = setupReplies();
const reflexions = setupReflexions();
const byePhrases = setupGoodByePhrases();
const keyMap = setupKeywords();
var varPart;
// Converts the input to lowercase, cuts out interpunctation 
// and pads the string 
var userInput;
var replyRing;
var reply;
var posAster;
var offsets;
// Should never happen... 
var keyIndex;
var isRepeated;
var isGone;
// Stores the last five inputs of the user in a ring buffer, 
// the first element is the current insertion index 
var history;
var findInfo;
var entry;

// Title information 
document.write(("************* ELIZA **************") + "<br/>");
document.write(("* Original design by J. Weizenbaum") + "<br/>");
document.write(("**********************************") + "<br/>");
document.write(("* Adapted for Basic on IBM PC by") + "<br/>");
document.write(("* - Patricia Danielson") + "<br/>");
document.write(("* - Paul Hashfield") + "<br/>");
document.write(("**********************************") + "<br/>");
document.write(("* Adapted for Structorizer by") + "<br/>");
document.write(("* - Kay Gürtzig / FH Erfurt 2016") + "<br/>");
document.write(("* Version: 2.2 (2019-03-28)") + "<br/>");
document.write(("**********************************") + "<br/>");
// Stores the last five inputs of the user in a ring buffer, 
// the first element is the current insertion index 
history = [0, "", "", "", "", ""];
offsets[length(keyMap)-1] = 0;
isGone = false;
// Starter 
document.write(("Hi! I\'m your new therapist. My name is Eliza. What\'s your problem?") + "<br/>");
do {
	userInput = prompt(String(userInput));
	// Converts the input to lowercase, cuts out interpunctation 
	// and pads the string 
	// Converts the input to lowercase, cuts out interpunctation 
	// and pads the string 
	userInput = normalizeInput(userInput);
	isGone = checkGoodBye(userInput, byePhrases);
	if (! isGone) {
		reply = "Please don\'t repeat yourself!";
		isRepeated = checkRepetition(history, userInput);
		if (! isRepeated) {
			findInfo = findKeyword(keyMap, userInput);
			keyIndex = findInfo[0];
			if (keyIndex < 0) {
				// Should never happen... 
				keyIndex = length(keyMap)-1;
			}
			entry = keyMap[keyIndex];
			// Variable part of the reply 
			varPart = "";
			if (length(entry.keyword) > 0) {
				varPart = conjugateStrings(userInput, entry.keyword, findInfo[1], reflexions);
			}
			replyRing = replies[entry.index];
			reply = replyRing[offsets[keyIndex]];
			offsets[keyIndex] = (offsets[keyIndex] + 1) % length(replyRing);
			posAster = pos("*", reply);
			if (posAster > 0) {
				if (varPart == " ") {
					reply = "You will have to elaborate more for me to help you.";
				}
				else {
					delete(reply, posAster, 1);
					insert(varPart, reply, posAster);
				}
			}
			reply = adjustSpelling(reply);
		}
		document.write((reply) + "<br/>");
	}
} while (! (isGone));
</script>

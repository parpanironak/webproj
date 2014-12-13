from __future__ import print_function
from glob import glob
import re
import os

from stemming.porter2 import step_0, step_1a, step_1b, step_1c, get_r1, get_r2, exceptional_early_exit_post_1a, exceptional_forms, capitalize_consonant_ys, normalize_ys

stopwords = set([u"&","a", "as", "able", "about", "above", "according", "accordingly", "across", "actually",
"after", "afterwards", "again", "against", "aint", "all", "allow", "allows", "almost", "alone", "along", "already", "also",
"although", "always", "am", "among", "amongst", "an", "and", "another", "any", "anybody", "anyhow", "anyone", "anything",
"anyway", "anyways", "anywhere", "apart", "appear", "appreciate", "appropriate", "are", "arent", "around", "as", "aside",
"ask", "asking", "associated", "at", "available", "away", "awfully", "be", "became", "because", "become", "becomes", "becoming",
"been", "before", "beforehand", "behind", "being", "believe", "below", "beside", "besides", "best", "better", "between", "beyond",
"both", "brief", "but", "by", "cmon", "cs", "came", "can", "cant", "cannot", "cant", "cause", "causes", "certain", "certainly",
"changes", "clearly", "co", "com", "come", "comes", "concerning", "consequently", "consider", "considering", "contain", "containing",
"contains", "corresponding", "could", "couldnt", "course", "currently", "definitely", "described", "despite", "did", "didnt", "different",
"do", "does", "doesnt", "doing", "dont", "done", "down", "downwards", "during", "each", "edu", "eg", "eight", "either", "else", "elsewhere",
"enough", "entirely", "especially", "et", "etc", "even", "ever", "every", "everybody", "everyone", "everything", "everywhere", "ex", "exactly",
"example", "except", "far", "few", "ff", "fifth", "first", "five", "followed", "following", "follows", "for", "former", "formerly", "forth", "four",
 "from", "further", "furthermore", "get", "gets", "getting", "given", "gives", "go", "goes", "going", "gone", "got", "gotten", "greetings", "had",
  "hadnt", "happens", "hardly", "has", "hasnt", "have", "havent", "having", "he", "hes", "hello", "help", "hence", "her", "here", "heres", "hereafter",
  "hereby", "herein", "hereupon", "hers", "herself", "hi", "him", "himself", "his", "hither", "hopefully", "how", "howbeit", "however", "i", "id", "ill",
  "im", "ive", "ie", "if", "ignored", "immediate", "in", "inasmuch", "inc", "indeed", "indicate", "indicated", "indicates", "inner", "insofar", "instead",
  "into", "inward", "is", "isnt", "it", "itd", "itll", "its", "its", "itself", "just", "keep", "keeps", "kept", "know", "knows", "known", "last", "lately",
  "later", "latter", "latterly", "least", "less", "lest", "let", "lets", "like", "liked", "likely", "little", "look", "looking", "looks", "ltd",
  "mainly", "many", "may", "maybe", "me", "mean", "meanwhile", "merely", "might", "more", "moreover", "most", "mostly", "much", "must", "my",
  "myself", "name", "namely", "nd", "near", "nearly", "necessary", "need", "needs", "neither", "never", "nevertheless", "new", "next", "nine",
   "no", "nobody", "non", "none", "noone", "nor", "normally", "not", "nothing", "novel", "now", "nowhere", "obviously", "of", "off", "often",
    "oh", "ok", "okay", "old", "on", "once", "one", "ones", "only", "onto", "or", "other", "others", "otherwise", "ought", "our", "ours",
    "ourselves", "out", "outside", "over", "overall", "own", "particular", "particularly", "per", "perhaps", "placed", "please", "plus",
    "possible", "presumably", "probably", "provides", "que", "quite", "qv", "rather", "rd", "re", "really", "reasonably", "regarding",
    "regardless", "regards", "relatively", "respectively", "right", "said", "same", "saw", "say", "saying", "says", "second", "secondly",
    "see", "seeing", "seem", "seemed", "seeming", "seems", "seen", "self", "selves", "sensible", "sent", "serious", "seriously", "seven",
     "several", "shall", "she", "should", "shouldnt", "since", "six", "so", "some", "somebody", "somehow", "someone", "something", "sometime",
     "sometimes", "somewhat", "somewhere", "soon", "sorry", "specified", "specify", "specifying", "still", "sub", "such", "sup", "sure", "ts",
      "take", "taken", "tell", "tends", "th", "than", "thank", "thanks", "thanx", "that", "thats", "thats", "the", "their", "theirs", "them",
      "themselves", "then", "thence", "there", "theres", "thereafter", "thereby", "therefore", "therein", "theres", "thereupon", "these",
      "they", "theyd", "theyll", "theyre", "theyve", "think", "third", "this", "thorough", "thoroughly", "those", "though", "three", "through",
      "throughout", "thru", "thus", "to", "together", "too", "took", "toward", "towards", "tried", "tries", "truly", "try", "trying", "twice",
      "two", "un", "under", "unfortunately", "unless", "unlikely", "until", "unto", "up", "upon", "us", "use", "used", "useful", "uses", "using",
       "usually", "value", "various", "very", "via", "viz", "vs", "want", "wants", "was", "wasnt", "way", "we", "wed", "well", "were", "weve",
       "welcome", "well", "went", "were", "werent", "what", "whats", "whatever", "when", "whence", "whenever", "where", "wheres", "whereafter",
       "whereas", "whereby", "wherein", "whereupon", "wherever", "whether", "which", "while", "whither", "who", "whos", "whoever", "whole", "whom",
       "whose", "why", "will", "willing", "wish", "with", "within", "without", "wont", "wonder", "would", "would", "wouldnt", "yes", "yet", "you",
       "youd", "youll", "youre", "youve", "your", "yours", "yourself", "yourselves", "zero",
        "http","upload","wikimedia","org","wikipedia"
       ])


def stem(word):
    if word in exceptional_forms:
        return exceptional_forms[word]

    word = capitalize_consonant_ys(word)
    r1 = get_r1(word)
    r2 = get_r2(word)
    word = step_0(word)
    #word = step_1a(word)

    # handle some more exceptional forms
    if word in exceptional_early_exit_post_1a:
        return word

    #word = step_1b(word, r1)
    word = normalize_ys(word)
    return word

def deletedir(path):
    filelist = [ f for f in os.listdir(path) ]
    for f in filelist:
        os.remove(path+f)


def is_number(s):
    try:
        float(s)
        return True
    except ValueError:
        return False

def removestopwords(sent):
    sent = sent.lower()
    newsent = ""
    for word in sent.split():
        #word = re.sub(r"[^a-zA-Z0-9","",word)
        if not is_number(word):
            if word not in stopwords :
                if not matcher.search(word):
                    if len(word) > 2:
                        if newsent == "":
                            newsent = word
                        else:
                            newsent = newsent + ' ' + word

    return newsent


def processfile(src, dest):
    srcf = open(src, 'r')
    destf = open(dest, 'a+')

    flag = True
    for line in srcf:
        if flag:
            flag = False
            print(line, file=destf)
            continue
        line = line.lower()
        line = re.sub(r"['][dms]{0,1}", "", line)
        line = re.sub(r"[']", "", line)
        line = re.sub(r"[*]", "", line)
        line = removestopwords(line)
        if len(line) > 1:
            print(line, file=destf)

    destf.close();
    srcf.close();

def start(srcpath, destpath):
    deletedir(destpath)

    filelist = glob(srcpath+u"*")

    for f in filelist:
        allNames = f.split("/")
        filename = allNames[len(allNames)-1]

        srcf = srcpath + filename
        destf = destpath + filename

        processfile(srcf, destf)


#processfile("1.txt","1.out_3")
matcher = re.compile("[^a-zA-Z]")
start('data/wikitextlinux/','data/wikitext/')
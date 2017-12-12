/* Copyright 2016 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.engedu.anagrams;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class AnagramDictionary {

    private static final int MIN_NUM_ANAGRAMS = 5;
    private static final int DEFAULT_WORD_LENGTH = 3;
    private static final int MAX_WORD_LENGTH = 7;

    public ArrayList<String> wordList = new ArrayList<>();
    public HashSet<String> wordSet = new HashSet<>();
    public HashMap<String, ArrayList> lettersToWord = new HashMap<>();

    private Random random = new Random();

    public AnagramDictionary(Reader reader) throws IOException {
        BufferedReader in = new BufferedReader(reader);
        String line;
        while((line = in.readLine()) != null) {
            String word = line.trim();
            String sorted = sortLetters(word);
            if(lettersToWord.containsKey(sorted)) {
                ArrayList<String> a = lettersToWord.get(sorted);
                a.add(word);
                lettersToWord.put(sorted, a);
            } else {
                ArrayList<String> a = new ArrayList<>();
                a.add(word);
                lettersToWord.put(sorted, a);
            }
            wordSet.add(word);
            wordList.add(word);
        }
    }

    public boolean isGoodWord(String word, String base) {
        if(wordSet.contains(word) && !word.contains(base)) return true;
        return false;
    }

    public List<String> getAnagrams(String targetWord) {
        ArrayList<String> result = new ArrayList<String>();
        for(String s:wordList) if(sortLetters(s).equals(sortLetters(targetWord))) result.add(s);
        return result;
    }

    public List<String> getAnagramsWithOneMoreLetter(String word) {
        ArrayList<String> result = new ArrayList<String>();
        for(int i=97; i<123; i++) {
            String s = (char)i + word;
            s = sortLetters(s);
            if(lettersToWord.containsKey(s)) result.addAll(lettersToWord.get(s));
        }
        return result;
    }

    public String pickGoodStarterWord() {
        int idx = (int) (Math.random() * wordList.size());
        String word = wordList.get(idx);
        while(lettersToWord.get(sortLetters(word)).size() < MIN_NUM_ANAGRAMS) {
            idx = (int) (Math.random() * wordList.size());
            word = wordList.get(idx);
        }
        return word;
    }

    public String sortLetters(String s) {
        char temp[] = s.toCharArray();
        Arrays.sort(temp);
        return new String(temp);
    }
}

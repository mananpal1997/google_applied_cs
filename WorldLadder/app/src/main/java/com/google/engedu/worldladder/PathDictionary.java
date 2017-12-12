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

package com.google.engedu.worldladder;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

public class PathDictionary {

    class GraphNode {
        String word;
        ArrayList<String> neighbourhood;
        GraphNode(String word) {
            word = word;
            neighbourhood = new ArrayList<>();
        }
    }

    private static final int MAX_WORD_LENGTH = 4;
    private static HashSet<String> words = new HashSet<>();
    public HashMap<String, GraphNode> neighbour_map = new HashMap<>();

    public PathDictionary(InputStream inputStream) throws IOException {
        if (inputStream == null) {
            return;
        }
        Log.i("Word ladder", "Loading dict");
        BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
        String line = null;
        Log.i("Word ladder", "Loading dict");
        int diff;
        while((line = in.readLine()) != null) {
            String word = line.trim();
            if (word.length() > MAX_WORD_LENGTH) {
                continue;
            }
            GraphNode node = new GraphNode(word);
            neighbour_map.put(word, node);
            for(String s:words) {
                if(s.length() != word.length()) continue;
                diff = 0;
                for(int i=0; i<s.length(); i++) {
                    if(s.charAt(i) != word.charAt(i)) diff++;
                    if(diff > 1) break;
                }
                if(diff == 1) {
                    node.neighbourhood.add(s);
                    if(neighbour_map.containsKey(s)) neighbour_map.get(s).neighbourhood.add(word);
                }
            }
            words.add(word);
        }
    }

    public boolean isWord(String word) {
        return words.contains(word.toLowerCase());
    }

    private ArrayList<String> neighbours(String word) {
        return neighbour_map.get(word).neighbourhood;
    }

    public ArrayList<String> findPath(String start, String end) {
        ArrayDeque<ArrayList<String>> paths_to_explore = new ArrayDeque<>();
        paths_to_explore.add(new ArrayList<>(Arrays.asList(start)));
        HashSet<String> visited = new HashSet<>();
        visited.add(start);

        while(!paths_to_explore.isEmpty()) {
            ArrayList<String> path = paths_to_explore.pollFirst();

            if(path.get(path.size() - 1).equals(end)) return path;
            if(path.size() > 8) continue;

            for(String word:neighbours(path.get(path.size() - 1))) {
                if(visited.contains(word)) continue;
                visited.add(word);

                ArrayList<String> new_path = new ArrayList<>(path);
                new_path.add(word);
                paths_to_explore.add(new_path);
            }
        }

        if(!paths_to_explore.peek().isEmpty()) return paths_to_explore.pollFirst();

        return null;
    }
}

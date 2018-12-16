import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.*;
import java.util.Arrays;
import java.util.function.Function;

class ValueComparator implements Comparator<String> {
    Map<String, Integer> base;
    
    public ValueComparator(Map<String, Integer> base) {
        this.base = base;
    }
    // Note: this comparator imposes orderings that are inconsistent with
    // equals.
    public int compare(String a, String b) {
        if (base.get(a) >= base.get(b)) {
            return -1;
        } else {
            return 1;
        } // returning 0 would merge keys
    }
}

class frequencies1 implements Function<ArrayList<String>, TreeMap<String, Integer> > {
    public TreeMap<String, Integer> apply(ArrayList<String> words) {
        HashMap<String, Integer> map = new HashMap<String, Integer>();
        for (String word:words){
            if (!map.containsKey(word.toLowerCase()))
                map.put(word.toLowerCase(),1);
            else{
                Integer count=map.get(word.toLowerCase());
                map.put(word.toLowerCase(),count+1);
            }
        }
        ValueComparator bvc = new ValueComparator(map);
        TreeMap<String, Integer> sorted_map = new TreeMap<String, Integer>(bvc);
        sorted_map.putAll(map);
        return sorted_map;
    }
}

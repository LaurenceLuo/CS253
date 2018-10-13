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

class TermFrequency {
    public static ArrayList<String> readStopWords(String fileName){
        ArrayList<String> stop_words = new ArrayList<String>();
        try {
            String line;
            File file_stopwords= new File(fileName);//new File("../stop_words.txt");
            FileReader fReader = new FileReader(file_stopwords);
            BufferedReader bReader = new BufferedReader(fReader);
            while ((line = bReader.readLine()) != null) {
                String[] Res = line.split("[\\p{Punct}\\s]+");
                for(String entry: Res){
                    stop_words.add(entry.toLowerCase());
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return stop_words;
    }
    
    public static TreeMap<String, Integer> readSort(String fileName){
        HashMap<String, Integer> map = new HashMap<String, Integer>();
        ValueComparator bvc = new ValueComparator(map);
        TreeMap<String, Integer> sorted_map = new TreeMap<String, Integer>(bvc);
        try {
            String line;
            File file = new File(fileName);
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            StringBuffer stringBuffer = new StringBuffer();
            while ((line = bufferedReader.readLine()) != null) {
                line=line.replaceAll("'s", " ").replaceAll("[^a-zA-Z0-9\\s]", " ");
                String[] Res = line.split("\\s");
                for (String s:Res){
                    if (!map.containsKey(s.toLowerCase()))
                        map.put(s.toLowerCase(),1);
                    else{
                        Integer count=map.get(s.toLowerCase());
                        map.put(s.toLowerCase(),count+1);
                    }
                }
            }
            fileReader.close();
        }catch (IOException e) {
            e.printStackTrace();
        }
        sorted_map.putAll(map);
        return sorted_map;
    }
    
    public static void removePrint(TreeMap<String, Integer> sorted_map, ArrayList<String> stop_words){
        Integer count=1;
        for(Map.Entry<String,Integer> entry : sorted_map.entrySet()){
            if(!stop_words.contains(entry.getKey())&&count<25){
                System.out.println(entry.getKey()+ "  -  " + entry.getValue());
                count++;
            }
        }
    }
    
    public static void main(String[] args) {
        //Read stop words
        ArrayList<String> stop_words = readStopWords("../stop_words.txt");
        
        //Read input file and sort by frequency
        TreeMap<String, Integer> sorted_map=readSort(args[0]);
        
        //Remove stop words and print top 25 entries
        removePrint(sorted_map, stop_words);
    }
}

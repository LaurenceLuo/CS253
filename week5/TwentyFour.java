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

class TFQuarantine {
    public static ArrayList<String> extract_words(String path_to_file){
        ArrayList<String> data = new ArrayList<String>();
        try{
            String line;
            File file = new File(path_to_file);
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            while ((line = bufferedReader.readLine()) != null) {
                line=line.replaceAll("'s", " ").replaceAll("[^a-zA-Z0-9\\s]", " ");
                String[] Res = line.split("\\s");
                for (String s:Res){
                    data.add(s.toLowerCase());
                }
            }
            fileReader.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return data;
    }
    
    public static ArrayList<String> remove_stop_words(ArrayList<String> data){
        ArrayList<String> new_data = new ArrayList<String>();
        try {
            ArrayList<String> stop_words = new ArrayList<String>();
            String line;
            File file= new File("../stop_words.txt");//new File("../stop_words.txt");
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            while ((line = bufferedReader.readLine()) != null) {
                String[] Res = line.split("[\\p{Punct}\\s]+");
                for(String entry: Res){
                    stop_words.add(entry.toLowerCase());
                }
            }
            for(String word : data){
                if(!stop_words.contains(word))
                    new_data.add(word);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return new_data;
    }
    
    public static HashMap<String, Integer> frequencies(ArrayList<String> word_list){
        HashMap<String, Integer> word_freqs=new HashMap<String, Integer>();
        for(String word: word_list){
            if (!word_freqs.containsKey(word.toLowerCase()))
                word_freqs.put(word.toLowerCase(),1);
            else{
                Integer count=word_freqs.get(word.toLowerCase());
                word_freqs.put(word.toLowerCase(),count+1);
            }
        }
        return word_freqs;
    }
    
    public static TreeMap<String, Integer> sort(HashMap<String, Integer> word_freqs){
        ValueComparator bvc = new ValueComparator(word_freqs);
        TreeMap<String, Integer> sorted_map = new TreeMap<String, Integer>(bvc);
        sorted_map.putAll(word_freqs);
        return sorted_map;
    }
    
    public static Runnable top25_freqs(TreeMap<String, Integer> word_freqs) {
        return ()->{
            Integer count=0;
            for(Map.Entry<String,Integer> entry : word_freqs.entrySet()){
                if(count<25){
                    System.out.println(entry.getKey()+ "  -  " + entry.getValue());
                    count++;
                }
            }
        };
    }
}

class TermFrequency{
    public static void main(String[] args) {
        Function<String, ArrayList<String>> extract_words = TFQuarantine::extract_words;
        Function<String, Runnable> execPath = extract_words.
        andThen(TFQuarantine::remove_stop_words).
        andThen(TFQuarantine::frequencies).
        andThen(TFQuarantine::sort).
        andThen(TFQuarantine::top25_freqs);
        execPath.apply(args[0]).run();
    }
}

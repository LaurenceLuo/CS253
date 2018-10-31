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
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

class ValueComparator implements Comparator<String> {
    Map<String, Integer> base;
    public ValueComparator(Map<String, Integer> base) {
        this.base = base;
    }
    public int compare(String a, String b) {
        if (base.get(a) >= base.get(b)) {
            return -1;
        } else {
            return 1;
        }
    }
}

//Add one more abstraction map for all objects use for 12.3
class ThisMap<K, V> extends HashMap<K, V> {
    public final ThisMap<K, V> This = this;
}

class TermFrequency {
    static final Map<String, Object> data_storage_obj = new ThisMap<String, Object>(){
    {
        Consumer<String> extract_words= path_to_file -> {
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
            This.put("data", data);
        };

        This.put("data", new ArrayList<String>());
        This.put("init", extract_words);
        This.put("words", (Supplier<ArrayList<String>>)()->(ArrayList<String>)This.get("data"));
    }
    };
    
    static final Map<String, Object> stop_words_obj = new ThisMap<String, Object>(){
    {
        Runnable load_stop_words=()->{
            ArrayList<String> stop_words = new ArrayList<String>();
            try {
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
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            This.put("stop_words",stop_words);
        };

        This.put("stop_words", new ArrayList<String>());
        This.put("init", load_stop_words);
        This.put("is_stop_word", (Function<String, Boolean>) word ->  ((ArrayList<String>)This.get("stop_words")).contains(word));
        }
    };

    static final Map<String, Object> word_freqs_obj = new ThisMap<String, Object>(){
    {
        Consumer<String> increment_count= word->{
            if (!((HashMap<String, Integer>)This.get("freqs")).containsKey(word.toLowerCase()))
                ((HashMap<String, Integer>)This.get("freqs")).put(word.toLowerCase(),1);
            else{
                Integer count=((HashMap<String, Integer>)This.get("freqs")).get(word.toLowerCase());
                ((HashMap<String, Integer>)This.get("freqs")).put(word.toLowerCase(),count+1);
            }
        };
        This.put("freqs", new HashMap<String, Integer>());
        This.put("increment_count", increment_count);
        This.put("sorted", (Function<HashMap<String, Integer>, TreeMap<String, Integer>>) map ->{
            ValueComparator bvc = new ValueComparator(map);
            TreeMap<String, Integer> sorted_map = new TreeMap<String, Integer>(bvc);
            sorted_map.putAll(map);
            return sorted_map;
        });
    }
    };

    public static void main(String[] args) {
        
        ((Consumer<String>)data_storage_obj.get("init")).accept(args[0]);
        ((Runnable)stop_words_obj.get("init")).run();
        
        for (String word : ((Supplier<ArrayList<String>>) data_storage_obj.get("words")).get()) {
            if (!((Function<String, Boolean>) stop_words_obj.get("is_stop_word")).apply(word)){
                ((Consumer<String>) word_freqs_obj.get("increment_count")).accept(word);
            }
        }
        
        TreeMap<String, Integer> word_freqs=((Function<HashMap<String, Integer>, TreeMap<String, Integer>>)word_freqs_obj.get("sorted")).apply((HashMap<String, Integer>)word_freqs_obj.get("freqs"));
        
        //***12.2
        word_freqs_obj.put("top25", (Consumer<TreeMap<String, Integer>>) (freq) -> {
            Integer count=0;
            for(Map.Entry<String,Integer> entry : freq.entrySet()){
                if(count<25){
                    System.out.println(entry.getKey()+ "  -  " + entry.getValue());
                    count++;
                }
            }
        });
        ((Consumer<TreeMap<String, Integer>>) word_freqs_obj.get("top25")).accept(word_freqs);
        //***
    }
}

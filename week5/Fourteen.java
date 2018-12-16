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
class WordFrequencyFramework{
    private final List<Consumer<String>> _load_event_handlers = new ArrayList<>();
    private final List<Runnable> _dowork_event_handlers = new ArrayList<>();
    private final List<Runnable> _end_event_handlers = new ArrayList<>();
    
    public void register_for_load_event(Consumer<String> handler) {
        _load_event_handlers.add(handler);
    }
    
    public void register_for_dowork_event(Runnable handler) {
        _dowork_event_handlers.add(handler);
    }
    
    public void register_for_end_event(Runnable handler) {
        _end_event_handlers.add(handler);
    }
    
    public void run(String filepath) {
        for(Consumer<String> h: _load_event_handlers){
            h.accept(filepath);
        }
        for(Runnable h: _dowork_event_handlers){
            h.run();
        }
        for(Runnable h: _end_event_handlers){
            h.run();
        }
    }
}

class DataStorage{
    private ArrayList<String> _data = new ArrayList<String>();
    private final StopWordFilter _stop_word_filter;
    private final List<Consumer<String>> _word_event_handlers = new ArrayList<>();
    
    
    public DataStorage(WordFrequencyFramework wfapp, StopWordFilter swf){
        _stop_word_filter=swf;
        wfapp.register_for_load_event(this::_load);
        wfapp.register_for_dowork_event(this::_produce_word);
    }
    
    private void _load(String path_to_file){
        try{
            String line;
            File file = new File(path_to_file);
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            while ((line = bufferedReader.readLine()) != null) {
                line=line.replaceAll("'s", " ").replaceAll("[^a-zA-Z0-9\\s]", " ");
                String[] Res = line.split("\\s");
                for (String s:Res){
                    _data.add(s.toLowerCase());
                }
            }
            fileReader.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void _produce_word(){
        for(String word: _data){
            if(!_stop_word_filter.is_stop_word(word)){
                for(Consumer<String> h: _word_event_handlers){
                    h.accept(word);
                }
            }
        }
    }
    
    public void register_for_word_event(Consumer<String> handler){
        _word_event_handlers.add(handler);
    }
    
}

class StopWordFilter{
    private final ArrayList<String> stop_words = new ArrayList<String>();
    
    public StopWordFilter(WordFrequencyFramework wfapp){
        wfapp.register_for_load_event(this::_load);
    }
    
    private void _load(String path_to_file){
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
    }
    
    public boolean is_stop_word(String word){
        return stop_words.contains(word.toLowerCase());
    }
}

class WordFrequencyCounter{
    private HashMap<String, Integer> map = new HashMap<String, Integer>();
    private TreeMap<String, Integer> sorted_map;
    
    public WordFrequencyCounter(WordFrequencyFramework wfapp, DataStorage data_storage){
        data_storage.register_for_word_event(this::_increment_count);
        wfapp.register_for_end_event(this::_print_freqs);
    }
    
    private void _increment_count(String word){
        if (!map.containsKey(word.toLowerCase()))
            map.put(word.toLowerCase(),1);
        else{
            Integer count=map.get(word.toLowerCase());
            map.put(word.toLowerCase(),count+1);
        }
    }
    
    private void _print_freqs(){
        ValueComparator bvc = new ValueComparator(map);
        sorted_map = new TreeMap<String, Integer>(bvc);
        sorted_map.putAll(map);
        Integer count=0;
        for(Map.Entry<String,Integer> entry : sorted_map.entrySet()){
            if(count<25){
                System.out.println(entry.getKey()+ "  -  " + entry.getValue());
                count++;
            }
        }
    }
}

class ZWordFilter{
    private final ArrayList<String> wordsZ = new ArrayList<String>();
    private final StopWordFilter stop_word_filter;
    
    public ZWordFilter(WordFrequencyFramework wfapp, DataStorage dsapp, StopWordFilter swf){
        stop_word_filter=swf;
        dsapp.register_for_word_event(this::_load);
        wfapp.register_for_end_event(this::_print);
    }
    
    public void _load(String word){
        if(!stop_word_filter.is_stop_word(word)&&word.contains("z")){
            wordsZ.add(word);
        }
    }
    
    public void _print(){
        int count=0;
        for(String word : wordsZ){
            count++;
        }
        System.out.println("The number of non-stop words with the letter z is: " + count);
    }
}

class TermFrequency {
    public static void main(String[] args) {
        WordFrequencyFramework wfapp = new WordFrequencyFramework();
        StopWordFilter stop_word_filter = new StopWordFilter(wfapp);
        DataStorage data_storage = new DataStorage(wfapp, stop_word_filter);
        WordFrequencyCounter word_freq_counter=new WordFrequencyCounter(wfapp, data_storage);
        
        ZWordFilter z_word_filter = new ZWordFilter(wfapp, data_storage, stop_word_filter);
        //data_storage.register_for_word_event(ZWordFilter::_load);
        
        wfapp.run(args[0]);
    }
}

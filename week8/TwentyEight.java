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
import java.util.Queue;
import java.util.LinkedList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;


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

abstract class ActiveWFObject extends Thread{
    
    Queue<String> q = new LinkedList<>();
    boolean _stop=false;;

    public void run() {
        while(!_stop){
            String message = q.peek();
            if (message=="die") {
                _stop = true;
            }
            else {
                List<String> ll = new ArrayList<String>();
                while(!q.isEmpty()){
                    ll.add(q.peek());
                    q.remove();
                }
                String[] qArray = new String[ ll.size() ];
                ll.toArray( qArray );
                if(!ll.isEmpty())
                    _dispatch(qArray);
            }
        }
    }
    protected void send(String message) {
        q.add(message);
    }
    protected void send(String[] messages) {
        for (String m:messages){
            q.add(m);
        }
    }

    abstract protected void _dispatch(String[] message);
}

class DataStorageManager extends ActiveWFObject{
    ArrayList<String> _data = new ArrayList<String>();
    
    protected void _dispatch(String[] message) {
        dispatch(message);
    }
    
    public ArrayList<String> dispatch(String[] message){
        
        if(message[0]=="init"){
            _init(message[1]);
        }
        else if(message[0]=="words"){
            return _words();
        }
        return new ArrayList<String>();
    }
    
    public void _init(String path_to_file){
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
                    //System.out.println(s);
                }
            }
            fileReader.close();
            this.send("die");
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public ArrayList<String> _words(){
        return _data;
    }
}

class StopWordManager extends ActiveWFObject{
    ArrayList<String> stop_words = new ArrayList<String>();
    
    protected void _dispatch(String[] message) {
        dispatch(message);
    }
    
    public boolean dispatch(String[] message){
        if(message[0]=="init"){
            _init();
        }
        else if(message[0]=="is_stop_word"){
            return _is_stop_word(message[1]);
        }
        return false;
    }
    
    public void _init(){
        try {
            String line;
            File file= new File("../stop_words.txt");
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            while ((line = bufferedReader.readLine()) != null) {
                String[] Res = line.split("[\\p{Punct}\\s]+");
                for(String entry: Res){
                    stop_words.add(entry.toLowerCase());
                }
            }
            this.send("die");
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public boolean _is_stop_word(String word){
        return stop_words.contains(word.toLowerCase());
    }
}

class WordFrequencyManager extends ActiveWFObject{
    HashMap<String, Integer> map = new HashMap<String, Integer>();
    TreeMap<String, Integer> sorted_map;
    
    protected void _dispatch(String[] message) {
        dispatch(message);
    }
    
    public TreeMap<String, Integer> dispatch(String[] message){
        if(message[0]=="increment_count"){
            _increment_count(message[1]);
        }
        else if(message[0]=="sorted"){
            return _sorted();
        }
        return new TreeMap<String, Integer>();
    }
    
    public void _increment_count(String word){
        if (!map.containsKey(word.toLowerCase()))
            map.put(word.toLowerCase(),1);
        else{
            Integer count=map.get(word.toLowerCase());
            map.put(word.toLowerCase(),count+1);
        }
    }
    
    public TreeMap<String, Integer>  _sorted(){
        ValueComparator bvc = new ValueComparator(map);
        sorted_map = new TreeMap<String, Integer>(bvc);
        sorted_map.putAll(map);
        return sorted_map;
    }
}

class WordFrequencyController extends ActiveWFObject{
    DataStorageManager _storage_manager;
    StopWordManager _stop_word_manager;
    WordFrequencyManager _word_freq_manager=new WordFrequencyManager();
    
    WordFrequencyController(DataStorageManager _dsm, StopWordManager _swm){
        _storage_manager=_dsm;
        _stop_word_manager=_swm;
    }
    
    protected void _dispatch(String[] message) {
        dispatch(message);
    }
    
    public void dispatch(String[] message){
        if(message[0]=="run"){
            _run();
        }
    }
    
    public void _run(){
        for(String w: _storage_manager.dispatch(new String[]{"words"})){
            if(!_stop_word_manager.dispatch(new String[]{"is_stop_word",w})){
                _word_freq_manager.dispatch(new String[]{"increment_count",w});
            }
        }
       
        TreeMap<String, Integer> word_freqs=_word_freq_manager.dispatch(new String[]{"sorted"});
        Integer count=0;
        for(Map.Entry<String,Integer> entry : word_freqs.entrySet()){
            if(count<25){
                System.out.println(entry.getKey()+ "  -  " + entry.getValue());
                count++;
            }
        }
        this.send("die");
    }
}

class TermFrequency {
    
    public static void main(String[] args) throws InterruptedException  {
        
        StopWordManager stop_word_manager = new StopWordManager();
        stop_word_manager.send("init");
        
        DataStorageManager storage_manager = new DataStorageManager();
        storage_manager.send(new String[]{"init", args[0]});
        
        WordFrequencyController wfcontroller=new WordFrequencyController(storage_manager,stop_word_manager);
        wfcontroller.send(new String[]{"run"});

        ActiveWFObject[] threads = new ActiveWFObject[] { stop_word_manager, storage_manager, wfcontroller };
        for (ActiveWFObject t : threads) {
            t.start();
            t.join();
        }


    }
}

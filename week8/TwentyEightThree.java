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
import java.util.stream.Stream;
import java.util.stream.Collectors;
import java.nio.file.Files;
import java.nio.file.Paths;

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

class StopWordManager extends ActiveWFObject{
    ArrayList<String> stop_words = new ArrayList<String>();
    
    protected void _dispatch(String[] message) {
        dispatch(message);
    }
    
    public boolean dispatch(String[] message){
        if(message[0]=="init"){
            _init();
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

}

class Streaming extends ActiveWFObject{
    StopWordManager stop_words;
    
    Streaming(StopWordManager s_w){
        stop_words=s_w;
    }
    
    protected void _dispatch(String[] message) {
        dispatch(message);
    }
    
    public void dispatch(String[] message){
        if(message[0]=="run"){
            try{
                _run(message[1]);
            }catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    public void _run(String path_to_file) throws IOException {
        
        Map<String,Integer> counts=Files.lines(Paths.get(path_to_file))
        //Read lines
        .map(line -> line.replaceAll("'s", " ").replaceAll("[^a-zA-Z0-9\\s]", " ").split("\\s"))
        .flatMap(Arrays::stream)
        .map(String::toLowerCase)
        //Get non stop words
        .filter(word -> !stop_words.stop_words.contains(word))
        //Count frequency
        .collect(Collectors.toMap(word -> word, word -> 1, (value, one) -> value + one));
        ValueComparator bvc = new ValueComparator(counts);
        TreeMap<String, Integer> sorted_data = new TreeMap<String, Integer>(bvc);
        sorted_data.putAll(counts);
        
        //print
        Integer count=0;
        for(Map.Entry<String,Integer> entry : sorted_data.entrySet()){
            if(count<25){
                System.out.println(entry.getKey()+ "  -  " + entry.getValue());
                count++;
            }
        }
        this.send("die");
    }
}

class TermFrequency{
    
    public static void main(String[] args) throws IOException, InterruptedException  {
        
        StopWordManager stop_word_manager = new StopWordManager();
        stop_word_manager.send("init");
        
        Streaming streaming= new Streaming(stop_word_manager);
        streaming.send(new String[]{"run", args[0]});
        
        Thread[] threads = new Thread[] { stop_word_manager, streaming };
        for (Thread t : threads) {
            t.start();
            t.join();
        }
    }
}

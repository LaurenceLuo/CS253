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
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.LongAdder;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ConcurrentHashMap;

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


class Worker extends Thread {
    CountDownLatch latch;
    
    public Worker(Runnable task, CountDownLatch l) {
        super(task);
        latch = l;
    }
    public void run() {
        try {
            latch.countDown();
            latch.await();
            super.run();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
}

class TermFrequency {
    static ConcurrentLinkedQueue<String> word_space;
    ConcurrentLinkedQueue<Map<String, Integer>> freq_space;
    ConcurrentHashMap<String, LongAdder> word_freqs;
    ArrayList<String> stop_words;
    
    TermFrequency(String path_to_file){
        stop_words=new ArrayList<String>();
        word_space = new ConcurrentLinkedQueue();
        freq_space = new ConcurrentLinkedQueue();
        word_freqs = new ConcurrentHashMap();
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
            fileReader.close();
            file = new File(path_to_file);
            fileReader = new FileReader(file);
            bufferedReader = new BufferedReader(fileReader);
            while ((line = bufferedReader.readLine()) != null) {
                line=line.replaceAll("'s", " ").replaceAll("[^a-zA-Z0-9\\s]", " ");
                String[] Res = line.split("\\s");
                for (String s:Res){
                    word_space.add(s.toLowerCase());
                }
            }
            fileReader.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

    }
    
    public void process_words() {
        Map<String, Integer> map = new HashMap<>();
        String word;
        while ((word = word_space.poll()) != null) {
            if (!stop_words.contains(word)) {
                if (!map.containsKey(word.toLowerCase()))
                    map.put(word.toLowerCase(),1);
                else{
                    Integer count=map.get(word.toLowerCase());
                    map.put(word.toLowerCase(),count+1);
                }
            }
        }
        freq_space.add(map);
    }
    
    public void merge_freqs() {
        Map<String, Integer> word_freq;
        while ((word_freq = freq_space.poll()) != null) {
            for(Map.Entry<String,Integer> entry :word_freq.entrySet()){
                word_freqs.computeIfAbsent(entry.getKey(), k -> new LongAdder()).add(entry.getValue());
            }
        }
    }
    
    public void print(){
        HashMap<String, Integer> map = new HashMap<String, Integer>();
        for(Map.Entry<String,LongAdder> entry : word_freqs.entrySet())
            map.put(entry.getKey(),entry.getValue().intValue());
        
        ValueComparator bvc = new ValueComparator(map);
        TreeMap<String, Integer> sorted_map = new TreeMap<String, Integer>(bvc);
        sorted_map.putAll(map);
       
        Integer count=0;
        for(Map.Entry<String,Integer> entry : sorted_map.entrySet()){
            if(count<25){
                System.out.println(entry.getKey()+ "  -  " + entry.getValue());
                count++;
            }
        }
    }
    
    public static void main(String[] args) throws InterruptedException  {
        TermFrequency tf=new TermFrequency(args[0]);

        Worker[] process_workers = new Worker[5];
        CountDownLatch process_latch = new CountDownLatch(5);
        for (int i = 0; i < 5; i++)
            process_workers[i] = new Worker(tf::process_words, process_latch);
        for (Thread worker : process_workers)
            worker.start();
        for (Thread worker : process_workers)
            worker.join();
        
        //5 threads for merging
        Worker[] merge_workers = new Worker[5];
        CountDownLatch merge_latch = new CountDownLatch(5);
        for (int i = 0; i < 5; i++)
            merge_workers[i] = new Worker(tf::merge_freqs, merge_latch);
        for (Thread worker : merge_workers)
            worker.start();
        for (Thread worker : merge_workers)
            worker.join();
        
        tf.print();
    }
}

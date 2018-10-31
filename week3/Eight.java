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

interface Func {
    public abstract void readRemove(String fileName, Func func);
    public abstract void sort(HashMap<String, Integer> map, Func func);
    public abstract void print(TreeMap<String, Integer> sorted_map, Func func);
}

class No_op implements Func{
    public void readRemove(String fileName, Func func){};
    public void sort(HashMap<String, Integer> map, Func func){};
    public void print(TreeMap<String, Integer> sorted_map, Func func){};
}

class Sort implements Func{
    public void readRemove(String fileName, Func func){};
    public void print(TreeMap<String, Integer> sorted_map, Func func){};
    
    public void sort(HashMap<String, Integer> map, Func func){
        ValueComparator bvc = new ValueComparator(map);
        TreeMap<String, Integer> sorted_map = new TreeMap<String, Integer>(bvc);
        sorted_map.putAll(map);
        func.print(sorted_map, new No_op());
    }
}

class Print implements Func{
    public void readRemove(String fileName, Func func){};
    public void sort(HashMap<String, Integer> map, Func func){};
    
    public void print(TreeMap<String, Integer> sorted_map, Func func){
        Integer count=0;
        for(Map.Entry<String,Integer> entry : sorted_map.entrySet()){
            if(count<25){
                System.out.println(entry.getKey()+ "  -  " + entry.getValue());
                count++;
            }
        }
    }
}

class TermFrequency implements Func{
    public void sort(HashMap<String, Integer> map, Func func){};
    public void print(TreeMap<String, Integer> sorted_map, Func func){};
    
    public void readRemove(String fileName, Func func){
        HashMap<String, Integer> map = new HashMap<String, Integer>();
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
            file = new File(fileName);
            fileReader = new FileReader(file);
            bufferedReader = new BufferedReader(fileReader);
            while ((line = bufferedReader.readLine()) != null) {
                line=line.replaceAll("'s", " ").replaceAll("[^a-zA-Z0-9\\s]", " ");
                String[] Res = line.split("\\s");
                for (String s:Res){
                    if(!stop_words.contains(s.toLowerCase())){
                        if (!map.containsKey(s.toLowerCase()))
                            map.put(s.toLowerCase(),1);
                        else{
                            Integer count=map.get(s.toLowerCase());
                            map.put(s.toLowerCase(),count+1);
                        }
                    }
                }
            }
            fileReader.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        func.sort(map, new Print());
    }
    
    public static void main(String[] args) {
        new TermFrequency().readRemove(args[0], new Sort());
    }
}

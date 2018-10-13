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
    public static HashMap<String, Integer> readRemove(String[] fileNames){
        HashMap<String, Integer> map = new HashMap<String, Integer>();
        ArrayList<String> stop_words = new ArrayList<String>();
        try {
            String line;
            File file= new File(fileNames[1]);//new File("../stop_words.txt");
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            while ((line = bufferedReader.readLine()) != null) {
                String[] Res = line.split("[\\p{Punct}\\s]+");
                for(String entry: Res){
                    stop_words.add(entry.toLowerCase());
                }
            }
            file = new File(fileNames[0]);
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
        
        return map;
    }
    
    public static TreeMap<String, Integer> sort(HashMap<String, Integer> map){
        ValueComparator bvc = new ValueComparator(map);
        TreeMap<String, Integer> sorted_map = new TreeMap<String, Integer>(bvc);
        sorted_map.putAll(map);
        return sorted_map;
    }
    
    public static void print(TreeMap<String, Integer> sorted_map){
        Integer count=1;
        for(Map.Entry<String,Integer> entry : sorted_map.entrySet()){
            if(count<25){
                System.out.println(entry.getKey()+ "  -  " + entry.getValue());
                count++;
            }
        }
    }
    
    public static void main(String[] args) {
        print(sort(readRemove(args)));
    }
}

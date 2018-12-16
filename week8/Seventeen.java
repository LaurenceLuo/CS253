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
import java.lang.reflect.Method;
import java.util.stream.Collectors;
import java.lang.reflect.InvocationTargetException;

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
    public static ArrayList<String> extract_words(String path_to_file){
        ArrayList<String> words = new ArrayList<String>();
        try {
            String line;
            File file = new File(path_to_file);
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            while ((line = bufferedReader.readLine()) != null) {
                line=line.replaceAll("'s", " ").replaceAll("[^a-zA-Z0-9\\s]", " ");
                String[] Res = line.split("\\s");
                for (String s:Res){
                    words.add(s.toLowerCase());
                }
            }
            fileReader.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        
        return words;
    }
    
    public static HashMap<String, Integer> frequency(ArrayList<String> words){
        HashMap<String, Integer> map = new HashMap<String, Integer>();
        ArrayList<String> stop_words = new ArrayList<String>();
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
            for (String word:words){
                if(!stop_words.contains(word.toLowerCase())){
                    if (!map.containsKey(word.toLowerCase()))
                        map.put(word.toLowerCase(),1);
                    else{
                        Integer count=map.get(word.toLowerCase());
                        map.put(word.toLowerCase(),count+1);
                    }
                }
            }
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
        Integer count=0;
        for(Map.Entry<String,Integer> entry : sorted_map.entrySet()){
            if(count<25){
                System.out.println(entry.getKey()+ "  -  " + entry.getValue());
                count++;
            }
        }
    }
    
    public static void main(String[] args) throws InvocationTargetException, IllegalAccessException{
        
        //print(sort(frequency(extract_words(args[0]))));
        
        Method[] methods = TermFrequency.class.getDeclaredMethods();
        Map<String, Method> method_names = Arrays.stream(methods).collect(Collectors.toMap(m -> m.getName(), m -> m));
        Object extract_words = method_names.get("extract_words").invoke(null, args[0]);
        Object frequency = method_names.get("frequency").invoke(null, extract_words);
        Object sorted_data = method_names.get("sort").invoke(null, frequency);
        method_names.get("print").invoke(null, sorted_data);
    }
}

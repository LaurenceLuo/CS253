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

class TermFrequency{
    public static ArrayList<String> load_stop_words(){
        ArrayList<String> stop_words=new ArrayList<String>();
        try{
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
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return stop_words;
    }
    
    public static void streaming(String path_to_file) throws IOException {
        
        ArrayList<String> stop_words=load_stop_words();
        /*HashMap<String,Integer> counts=new HashMap<String,Integer>();
        try{
            String line;
            File file = new File(path_to_file);
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            while ((line = bufferedReader.readLine()) != null) {
                line=line.replaceAll("'s", " ").replaceAll("[^a-zA-Z0-9\\s]", " ");
                String[] Res = line.split("\\s");
                for (String word:Res){
                    if (!stop_words.contains(word.toLowerCase())){
                        if (!counts.containsKey(word.toLowerCase()))
                            counts.put(word.toLowerCase(),1);
                        else{
                            Integer count=counts.get(word.toLowerCase());
                            counts.put(word.toLowerCase(),count+1);
                        }
                    }
                }
            }
            fileReader.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }*/
/***
 Commented area is the same and a more readable version of what the actual streaming is doing
 ***/
        
        Map<String,Integer> counts=Files.lines(Paths.get(path_to_file))
            //Read lines
            .map(line -> line.replaceAll("'s", " ").replaceAll("[^a-zA-Z0-9\\s]", " ").split("\\s"))
            .flatMap(Arrays::stream)
            .map(String::toLowerCase)
            //Get non stop words
            .filter(word -> !stop_words.contains(word))
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
    }
    
    public static void main(String[] args) throws IOException  {
        streaming(args[0]);
        return;
    }
}

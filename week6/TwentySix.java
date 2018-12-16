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

class Column<V>{
    V cells;
}

class All_Column{
    Column<ArrayList<String>> non_stop_words;
    Column<ArrayList<String>> unique_words;
    Column<HashMap<String, Integer>> counts;
    Column<TreeMap<String, Integer>> sorted_data;
    
    void update(Column<ArrayList<String>> all_words,
                Column<ArrayList<String>> stop_words){

        non_stop_words=new Column<ArrayList<String>>();
        non_stop_words.cells=new ArrayList<String>();
        for(String word : all_words.cells){
            if(!stop_words.cells.contains(word))
                non_stop_words.cells.add(word);
        }
        
        unique_words=new Column<ArrayList<String>>();
        unique_words.cells=new ArrayList<String>();
        for(String word : non_stop_words.cells){
            if(!unique_words.cells.contains(word))
                unique_words.cells.add(word);
        }
        
        counts=new Column<HashMap<String, Integer>>();
        counts.cells=new HashMap<String, Integer>();
        for(String word: non_stop_words.cells){
            if (!counts.cells.containsKey(word.toLowerCase()))
                counts.cells.put(word.toLowerCase(),1);
            else{
                Integer count=counts.cells.get(word.toLowerCase());
                counts.cells.put(word.toLowerCase(),count+1);
            }
        }
        
        sorted_data=new Column<TreeMap<String, Integer>>();
        sorted_data.cells=new TreeMap<String, Integer>();
        ValueComparator bvc = new ValueComparator(counts.cells);
        sorted_data.cells = new TreeMap<String, Integer>(bvc);
        sorted_data.cells.putAll(counts.cells);
    }
    
}

class TermFrequency{
    public static Column<ArrayList<String>> load_words(String path_to_file){
        Column<ArrayList<String>> all_words=new Column<ArrayList<String>>();
        all_words.cells=new ArrayList<String>();
        try{
            String line;
            File file = new File(path_to_file);
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            while ((line = bufferedReader.readLine()) != null) {
                line=line.replaceAll("'s", " ").replaceAll("[^a-zA-Z0-9\\s]", " ");
                String[] Res = line.split("\\s");
                for (String s:Res){
                    all_words.cells.add(s.toLowerCase());
                }
            }
            fileReader.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return all_words;
    }
    public static Column<ArrayList<String>> load_stop_words(){
        Column<ArrayList<String>> stop_words=new Column<ArrayList<String>>();
        stop_words.cells=new ArrayList<String>();
        try{
            String line;
            File file= new File("../stop_words.txt");//new File("../stop_words.txt");
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            while ((line = bufferedReader.readLine()) != null) {
                String[] Res = line.split("[\\p{Punct}\\s]+");
                for(String entry: Res){
                    stop_words.cells.add(entry.toLowerCase());
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return stop_words;
    }
    
    
    public static void main(String[] args) {
        
        Column<ArrayList<String>> all_words=load_words(args[0]);
        Column<ArrayList<String>> stop_words=load_stop_words();
        
        All_Column all_columns=new All_Column();
        //Update the columns with formulas
        all_columns.update(all_words,stop_words);
        
        Integer count=0;
        for(Map.Entry<String,Integer> entry : all_columns.sorted_data.cells.entrySet()){
            if(count<25){
                System.out.println(entry.getKey()+ "  -  " + entry.getValue());
                count++;
            }
        }
        return;
    }
}

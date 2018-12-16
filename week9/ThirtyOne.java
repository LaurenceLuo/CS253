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
    public int compare(String a, String b) {
        if (base.get(a) >= base.get(b)) {
            return -1;
        } else {
            return 1;
        } // returning 0 would merge keys
    }
}

class TermFrequency {
    public static ArrayList<String> readStopWords(){
        ArrayList<String> stop_words = new ArrayList<String>();
        try {
            String line;
            File file_stopwords= new File("../stop_words.txt");
            FileReader fReader = new FileReader(file_stopwords);
            BufferedReader bReader = new BufferedReader(fReader);
            while ((line = bReader.readLine()) != null) {
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
    
    public static ArrayList<ArrayList<String> > partition(String fileName, Integer nlines){
        ArrayList<ArrayList<String> > partitions=new ArrayList<ArrayList<String> >();
        try {
            ArrayList<String> part=new ArrayList<String>();
            String line;
            File file = new File(fileName);
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            StringBuffer stringBuffer = new StringBuffer();
            Integer count=0;
            while ((line = bufferedReader.readLine()) != null) {
                if(count.equals(nlines)){
                    partitions.add(part);
                    part=new ArrayList<String>();
                    count=0;
                }
                if(!line.isEmpty()){
                    part.add(line);
                    count++;
                }
            }
            fileReader.close();
        }catch (IOException e) {
            e.printStackTrace();
        }
        return partitions;
    }

    public static ArrayList<ArrayList<String> > split_words(ArrayList<ArrayList<String> > partitions, ArrayList<String> stop_words){
        ArrayList<ArrayList<String> > new_partitions=new ArrayList<ArrayList<String> >();
        for(ArrayList<String> part: partitions){
            ArrayList<String> new_part=new ArrayList<String>();
            for(String line: part){
                line=line.replaceAll("'s", " ").replaceAll("[^a-zA-Z0-9\\s]", " ");
                String[] Res = line.split("\\s");
                for (String s:Res){
                    if (!stop_words.contains(s.toLowerCase())){
                        new_part.add(s.toLowerCase());
                        
                    }
                }
            }
            new_partitions.add(new_part);
        }
        return new_partitions;
    }
    
    //Regroup into 5 groups between a-e, f-j, k-o, p-t, u-z
    public static ArrayList<ArrayList<String> > regroup(ArrayList<ArrayList<String> > partitions){
        ArrayList<ArrayList<String> > new_partitions=new ArrayList<>();
        for(int i=0;i<5;i++)
            new_partitions.add(new ArrayList<String>());
        for(ArrayList<String> part: partitions){
            for(String word: part){
                if(word.charAt(0)>='a'&&word.charAt(0)<='e')
                    new_partitions.get(0).add(word);
                else if(word.charAt(0)>='f'&&word.charAt(0)<='j')
                    new_partitions.get(1).add(word);
                else if(word.charAt(0)>='k'&&word.charAt(0)<='o')
                    new_partitions.get(2).add(word);
                else if(word.charAt(0)>='p'&&word.charAt(0)<='t')
                    new_partitions.get(3).add(word);
                else if(word.charAt(0)>='u'&&word.charAt(0)<='z')
                    new_partitions.get(4).add(word);
            }
        }
        return new_partitions;
    }
    
    public static HashMap<String, Integer> count_words(ArrayList<ArrayList<String> > mapping){
        HashMap<String, Integer> map = new HashMap<String, Integer>();
        for(ArrayList<String> group: mapping){
            for(String word: group){
                if (!map.containsKey(word.toLowerCase()))
                    map.put(word.toLowerCase(),1);
                else{
                    Integer count=map.get(word.toLowerCase());
                    map.put(word.toLowerCase(),count+1);
                }
            }
        }
        return map;
    }
    
    public static TreeMap<String, Integer> sort(HashMap<String, Integer> word_freq){
        ValueComparator bvc = new ValueComparator(word_freq);
        TreeMap<String, Integer> sorted_map = new TreeMap<String, Integer>(bvc);
        sorted_map.putAll(word_freq);
        return sorted_map;
    }
    
    public static void removePrint(TreeMap<String, Integer> sorted_map, ArrayList<String> stop_words){
        Integer count=1;
        for(Map.Entry<String,Integer> entry : sorted_map.entrySet()){
            if(!stop_words.contains(entry.getKey())&&count<25){
                System.out.println(entry.getKey()+ "  -  " + entry.getValue());
                count++;
            }
        }
    }
    
    public static void main(String[] args) {
        ArrayList<String> stop_words=readStopWords();
        ArrayList<ArrayList<String> > partitions=partition(args[0], 200);
        ArrayList<ArrayList<String> > new_partitions=split_words(partitions, stop_words);
        ArrayList<ArrayList<String> > mapping=regroup(new_partitions);
        HashMap<String, Integer> word_freq=count_words(mapping);
        TreeMap<String, Integer> sorted_freq=sort(word_freq);
        
        Integer count=0;
        for(Map.Entry<String,Integer> entry : sorted_freq.entrySet()){
            if(count<25){
                System.out.println(entry.getKey()+ "  -  " + entry.getValue());
                count++;
            }
        }
        
    }
}

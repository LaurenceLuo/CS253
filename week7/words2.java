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

class words2 implements Function<String, ArrayList<String>> {
    public ArrayList<String> apply(String path_to_file) {
        ArrayList<String> stop_words = new ArrayList<String>();
        String line;
        try{
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
        ArrayList<String> all_words = new ArrayList<String>();
        try {
            File file = new File(path_to_file);
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            while ((line = bufferedReader.readLine()) != null) {
                line=line.replaceAll("'s", " ").replaceAll("[^a-zA-Z0-9\\s]", " ");
                String[] Res = line.split("\\s");
                for (String s:Res)
                    all_words.add(s.toLowerCase());
            }
            fileReader.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        ArrayList<String> words=new ArrayList<String>();
        for (String word:all_words){
            if (!stop_words.contains(word.toLowerCase())){
                words.add(word.toLowerCase());
            }
        }
        return words;
    }
}

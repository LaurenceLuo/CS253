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
import java.util.Properties;
import java.io.InputStream;
import java.io.FileNotFoundException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;
import java.util.function.Function;

class TermFrequency {

    static Function<String, ArrayList<String>> words_obj;
    static Function<ArrayList<String>, TreeMap<String, Integer>> frequencies_obj;
    
    public static void loadPlugins() throws IOException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        Properties prop = new Properties();
        String propFileName = "config.properties";
        InputStream inputStream = TermFrequency.class.getClassLoader().getResourceAsStream(propFileName);
        if (inputStream != null) {
            prop.load(inputStream);
        } else {
            throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
        }
        String words_name = prop.getProperty("words");
        String frequency_name = prop.getProperty("frequencies");
        File file = new File("");
        try {
            //using ClassLoader as the external class importer for words1 and frequencies1
            URL url = file.toURL();
            URL[] urls = new URL[]{url};
            ClassLoader cl = new URLClassLoader(urls);
            words_obj = (Function<String, ArrayList<String>>)cl.loadClass(words_name).newInstance();
            frequencies_obj = (Function<ArrayList<String>, TreeMap<String, Integer>>)cl.loadClass(frequency_name).newInstance();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) throws IOException, ClassNotFoundException, IllegalAccessException, InstantiationException{
        
        loadPlugins();
        TreeMap<String, Integer> sorted_words=frequencies_obj.apply(words_obj.apply(args[0]));
        
        Integer count=0;
        for(Map.Entry<String,Integer> entry : sorted_words.entrySet()){
            if(count<25)
                System.out.println(entry.getKey()+ "  -  " + entry.getValue());
            count++;
        }
    }
}

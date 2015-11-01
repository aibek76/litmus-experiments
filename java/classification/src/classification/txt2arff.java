package classification;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import com.google.common.base.Joiner;

import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;

public class txt2arff {

    private static Boolean isValidFileName(String fileName) {
        Boolean result = false;
        // make sure we accept *_w2v.txt files only
        String[] parts = fileName.split("\\.");
        if (parts.length == 2) {
            String fName = parts[0];
            String fExt = parts[1];
            if (fExt.equals("txt")) {
                parts = fName.split("_");
                if (parts.length >= 2) {
                    if (parts[parts.length - 1].equals("w2v"))
                        result = true;
                }
            }
        }
        return result;
    }

    private static String getOutputFileName(String fileName) {
        // output file name will be <name>.arff
        String[] parts = fileName.split("\\.");
        String fName = parts[0];
        String fOut = fName + ".arff";
        return fOut;
    }
    
    private static Instances createInstances() {
        ArrayList<Attribute> atts;
        ArrayList<String> attClass;
        Instances data;
        
        // 1. set up attributes
        atts = new ArrayList<Attribute>();
        // attribute: item_id
        atts.add(new Attribute("item_id", (ArrayList<String>) null));
        // attributes: Word2Vec dimensions
        for (int i=0; i<300; i++) {
            atts.add(new Attribute("feature"+Integer.toString(i)));
        }
        // attribute: class
        attClass = new ArrayList<String>();
        attClass.add("relevant");
        attClass.add("irrelevant");
        atts.add(new Attribute("class", attClass));

        // 2. create Instances object
        data = new Instances("MyRelation", atts, 0);

        return data;
    }

    private static void addVector(Instances data, String[] row, HashMap<String, String> labels) {
        double[] vals;
        ArrayList<String> attClass;
        
        String itemId = row[0];
        if (!labels.containsKey(itemId)) {
            System.out.println("There is no label for itemId " + itemId);
            return;
        }

        vals = new double[data.numAttributes()];
        vals[0] = data.attribute(0).addStringValue(itemId);
        for (int i=0; i<300; i++) {
            vals[i+1] = Double.valueOf(row[i+1]);
        }
        // initialize class attribute's values
        attClass = new ArrayList<String>();
        attClass.add("relevant");
        attClass.add("irrelevant");
        // set the value of the class attribute
        vals[data.numAttributes()-1] = attClass.indexOf(labels.get(itemId));

        data.add(new DenseInstance(1.0, vals));
    }
    
    private static String getFileNameLabels(String fileName) {
        // output file name will be <name>_labels.arff
        String[] parts = fileName.split("\\.");
        String fExt = parts[1];
        parts = parts[0].split("_");
        String fOut = Joiner.on("_").join(
                Arrays.copyOfRange(parts, 0, parts.length - 1))
                + "_labels." + fExt;
        return fOut;
    }
    
    private static HashMap<String, String> getLabels(String fileName) {
        HashMap<String, String> map = new HashMap<String, String>();
        
        try (BufferedReader br = new BufferedReader(
                new FileReader(fileName))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.trim().split("\t");
                String itemId = parts[0];
                String label = parts[1];
                map.put(itemId, label);
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        return map;
    }

    private static void traverse(String inDir, String outDir) {
        for (File file : new File(inDir).listFiles()) {
            if (!isValidFileName(file.getName()))
                continue;
            String fOut = getOutputFileName(file.getName());
            Instances data = createInstances();
            HashMap<String, String> labels = getLabels(getFileNameLabels(file.getName()));
            try (PrintWriter writer = new PrintWriter(fOut, "UTF-8")) {
                try (BufferedReader br = new BufferedReader(
                        new FileReader(file))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        addVector(data, line.trim().split("\t"), labels);
                    }
                    writer.print(data.toString());
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            } catch (FileNotFoundException | UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        final long t0 = System.nanoTime();

        String inDir = System.getProperty("user.dir");
        String outDir = System.getProperty("user.dir");
        if (args.length > 0) {
            inDir = args[0];
        }
        if (args.length > 1) {
            outDir = args[1];
        }

        // traverse the *_orig.txt and *_labels.txt files in a given directory
        traverse(inDir, outDir);

        // print execution time
        final long duration = (System.nanoTime() - t0);
        System.out.println("milliseconds = "
                + Long.toString(duration / 1000 / 1000));
    }

}

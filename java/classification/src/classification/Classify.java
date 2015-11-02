package classification;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;

import weka.classifiers.meta.FilteredClassifier;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

import com.google.common.base.Joiner;

public class Classify {

    public static FilteredClassifier loadModel(String fileName) {
        // deserialize model
        FilteredClassifier cls = null;
        try {
            cls = (FilteredClassifier) weka.core.SerializationHelper
                    .read(fileName);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return cls;
    }

    public static void classify(FilteredClassifier cls, String fnIn,
            String fnOut) {
        DataSource source;
        Instances test = null;
        ArrayList<String> attClass;
        try {
            source = new DataSource(fnIn);
            test = source.getDataSet();
            if (test.classIndex() == -1)
                test.setClassIndex(test.numAttributes() - 1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        int N = test.numInstances();
        // initialize class attribute's values
        attClass = new ArrayList<String>();
        attClass.add("relevant");
        attClass.add("irrelevant");
        try (PrintWriter writer = new PrintWriter(fnOut, "UTF-8")) {
            for (int i = 0; i < N; i++) {
                try {
                    int pred = (int) cls.classifyInstance(test.instance(i));
                    String label = attClass.get(pred);
                    String item_id = test.instance(i).stringValue(0);
                    writer.println(String.format("%s\t%s", item_id, label));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (FileNotFoundException | UnsupportedEncodingException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
    }

    private static Boolean isValidFileName(String fileName) {
        Boolean result = false;
        // make sure we accept *_test_w2v.arff files only
        String[] parts = fileName.split("\\.");
        if (parts.length == 2) {
            String fName = parts[0];
            String fExt = parts[1];
            if (fExt.equals("arff")) {
                parts = fName.split("_");
                if (parts.length >= 3) {
                    if (parts[parts.length - 2].equals("test") && parts[parts.length - 1].equals("w2v"))
                        result = true;
                }
            }
        }
        return result;
    }

    private static String getOutputFileName(String fileName) {
        // output file name will be *_class.txt
        String[] parts = fileName.split("\\.");
        String fName = parts[0];
        String fOut = fName + "_class.txt";
        return fOut;
    }

    private static String getFileNameModel(String fileName) {
        // output file name will be <name>_train.model
        String[] parts = fileName.split("\\.");
        parts = parts[0].split("_");
        String fOut = Joiner.on("_").join(
                Arrays.copyOfRange(parts, 0, parts.length - 2))
                + "_train_w2v.model";
        return fOut;
    }

    private static void traverse(String inDir, String outDir) {
        for (File file : new File(inDir).listFiles()) {
            if (!isValidFileName(file.getName()))
                continue;
            String fnOut = getOutputFileName(file.getName());
            String fnModel = getFileNameModel(file.getName());
            FilteredClassifier cls = loadModel(fnModel);
            classify(cls, file.getName(), fnOut);
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

        // traverse the *_train.arff files in a given directory
        traverse(inDir, outDir);

        // print execution time
        final long duration = (System.nanoTime() - t0);
        System.out.println("milliseconds = "
                + Long.toString(duration / 1000 / 1000));
    }

}

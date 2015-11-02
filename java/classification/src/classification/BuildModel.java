package classification;

import java.io.File;
import weka.classifiers.functions.SMO;
import weka.classifiers.meta.FilteredClassifier;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.unsupervised.attribute.Remove;

public class BuildModel {

    public static void buildModel(String fnData, String fnModel) {
        // filter
        Remove rm = new Remove();
        rm.setAttributeIndices("1"); // remove item_id
        // classifier
        SMO smo = new SMO();
        // meta-classifier
        FilteredClassifier fc = new FilteredClassifier();
        fc.setFilter(rm);
        fc.setClassifier(smo);

        // train
        DataSource source = null;
        try {
            source = new DataSource(fnData);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Instances data = null;
        try {
            data = source.getDataSet();
        } catch (Exception e) {
            e.printStackTrace();
        }
        // setting class attribute if the data format does not provide this
        // information
        if (data.classIndex() == -1)
            data.setClassIndex(data.numAttributes() - 1);
        try {
            fc.buildClassifier(data);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // serialize model
        try {
            weka.core.SerializationHelper.write(fnModel, fc);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private static Boolean isValidFileName(String fileName) {
        Boolean result = false;
        // make sure we accept *_train_w2v.arff files only
        String[] parts = fileName.split("\\.");
        if (parts.length == 2) {
            String fName = parts[0];
            String fExt = parts[1];
            if (fExt.equals("arff")) {
                parts = fName.split("_");
                if (parts.length >= 3) {
                    if (parts[parts.length - 2].equals("train") && parts[parts.length - 1].equals("w2v"))
                        result = true;
                }
            }
        }
        return result;
    }

    private static String getOutputFileName(String fileName) {
        // output file name will be <name>.model
        String[] parts = fileName.split("\\.");
        String fName = parts[0];
        String fOut = fName + ".model";
        return fOut;
    }

    private static void traverse(String inDir, String outDir) {
        for (File file : new File(inDir).listFiles()) {
            if (!isValidFileName(file.getName()))
                continue;
            String fOut = getOutputFileName(file.getName());
            buildModel(file.getName(), fOut);
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

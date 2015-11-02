This project provides code to run LITMUS experiments. It works with files as opposed to data stores.

Here is an overview of the data flow represented as files:

1. original files:
-sample_train.txt
-sample_test.txt

2. geotagging:
-sample_train_nlp.txt
-sample_train_geo.txt
-sample_test_nlp.txt
-sample_test_geo.txt

3. annotation:
-sample_train_labels.txt
-sample_test_labels.txt

4. classification:
-sample_train_w2v.txt
-sample_train_w2v.arff
-sample_train_w2v.model
-sample_test_w2v.txt
-sample_test_w2v.arff
-sample_test_w2v_class.txt

5. ranking:
-sample_test_w2v_rank.txt

Step 1. is the input to the system. There are two kinds of files - train and test. Each file consists of multiple lines. Each line is a JSON formatted string of the data returned by social networks API, plus an additional field "stream_type", which should be equal to "Twitter", "Instagram" or "YouTube".

Step 2. extracts the mentions of geographical locations in the texts based on NER approach. Then it retrieves the corresponding geographic coordinates based on Google Maps Geocoding API and computes cells. See ReadMe_Geotagging_Component.txt for details.

Step 3. represents the annotation step, which is performed outside of LITMUS.

Step 4. determines the relevance of the texts to landslide as a natural disaster based on machine learning classificaton. Word2Vec representation is used as features for classification. The classification algorithm is SVM implemented in Weka. See ReadMe_Classification_Component.txt for details.

Step 5. computes a landslide score for each non-empty cell based on the ranking strategy.
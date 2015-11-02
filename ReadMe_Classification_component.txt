The classification component performs the following actions:
    1) converts texts to Word2Vec vectors,
    2) converts TXT files to ARFF format,
    3) builds an SVM model based the training data,
    4) classifies Word2Vec vectors based on the given model.

Step 1) is implemented in Python, file generate_w2v.py. It expects GoogleNews-vectors-negative300.bin from the word2vec project available here: https://code.google.com/p/word2vec/. Note, that the GoogleNews-vectors-negative300.bin.gz file should be uncompressed. This module uses word2vec implementation from the gensim library, which is available here: https://radimrehurek.com/gensim/models/word2vec.html. It expects input files named as "<name>_labels.txt", e.g. "sample_train_labels.txt" and "sample_test_labels.txt". The program generates output files named as "<name>_w2v.txt".

Step 2) is implemented in Java, file txt2arff.java. It makes use of the Weka library, which should be automatically downloaded based on the pom.xml. It expects input files named as "<name>_w2v.txt", e.g. "sample_train_w2v.txt" and "sample_test_w2v.txt". The program generates output files named as "<name>.arff".

Step 3) is implemented in Java, file BuildModel.java. It makes use of the Weka library, which should be automatically downloaded based on the pom.xml. It expects input files named as "<name>_train_w2v.arff", e.g. "sample_train_w2v.arff". The program generates output files named as "<name>.model", e.g. "sample_train_w2v.model".

Step 4) is implemented in Java, file Classify.java. It makes use of the Weka library, which should be automatically downloaded based on the pom.xml. It expects files named as "<name>_w2v.arff", e.g. "sample_test_w2v.arff". The program generates output files named as "<name>_class.txt", e.g. "sample_test_w2v.class".


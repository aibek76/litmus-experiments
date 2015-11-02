from datetime import datetime
from time import time
import numpy as np

class DeepTextAnalyzer(object):
    def __init__(self, word2vec_model):
        """
        Construct a DeepTextAnalyzer using the input Word2Vec model
        :param word2vec_model: a trained Word2Vec model
        """
        self._model = word2vec_model

    def txt2vectors(self, txt, is_html):
        """
        Convert input text into an iterator that returns the corresponding vector representation of each
        word in the text, if it exists in the Word2Vec model
        :param txt: input text
        :param is_html: if True, then extract the text from the input HTML
        :return: iterator of vectors created from the words in the text using the Word2Vec model.
        """
        from utils import get_words_in_string
        words = get_words_in_string(txt)
        words = [w for w in words if w in self._model]
        if len(words) != 0:
            for w in words:
                yield self._model[w]


    def txt2avg_vector(self, txt, is_html):
        """
        Calculate the average vector representation of the input text
        :param txt: input text
        :param is_html: is the text is a HTML
        :return the average vector of the vector representations of the words in the text  
        """
        vectors = self.txt2vectors(txt,is_html=is_html)
        vectors_sum = next(vectors, None)
        if vectors_sum is None:
            return None
        count =1.0
        for v in vectors:
            count += 1
            vectors_sum = np.add(vectors_sum,v)

        #calculate the average vector and replace +infy and -inf with numeric values 
        avg_vector = np.nan_to_num(vectors_sum/count)
        return avg_vector

def w2v_get_model():
    import gensim
    t0 = time()
    model = gensim.models.word2vec.Word2Vec.load_word2vec_format('./GoogleNews-vectors-negative300.bin', binary=True)
    print 'loading model: %s' % (time() - t0)
    return model

def w2v_vectors(dta, item_id, text, f):
    vector = dta.txt2avg_vector(text, is_html=False)
    if vector is None:
        print 'Coult not generate Word2Vec vector for %s' % text
        return
    vector = [0.0 if v==None else str(v) for v in vector]
    f.write('%s\t%s\n' % (item_id, '\t'.join(vector)))

def is_valid_fname(fname):
    result = False
    # make sure we accept *_nlp.txt files only
    parts = fname.split('.')
    if len(parts)==2:
        name, ext = parts
        if ext=='txt':
            parts = name.split('_')
            if len(parts)>=2:
                if parts[-1]=='labels':
                    result = True
    return result

def get_fn_geo(fname):
    # replace _labels with _geo in *_labels.txt
    name, ext = fname.split('.')
    parts = name.split('_')
    fout = '_'.join(parts[:-1])+'_geo.'+ext
    return fout

def get_fn_orig(fname):
    # remove _labels from *_labels.txt
    name, ext = fname.split('.')
    parts = name.split('_')
    fout = '_'.join(parts[:-1])+'.'+ext
    return fout

def get_texts(fname):
    from json import loads
    texts = {}
    for line in open(fname):
        data = loads(line)
        item_id = text = None
        if data['stream_type']=='Twitter':
            item_id = data['id_str']
            text = data['text']
        elif data['stream_type']=='Instagram':
            item_id = data['id']
            text = data['caption']['text']
        elif data['stream_type']=='YouTube':
            item_id = data['id']['videoId']
            text = data['snippet']['title']
        if item_id==None or text==None:
            continue
        texts[item_id] = text
    return texts

def get_output_fname(fname):
    # replace _labels with _w2v in *_labels.txt
    name, ext = fname.split('.')
    parts = name.split('_')
    fout = '_'.join(parts[:-1])+'_w2v.'+ext
    return fout

def traverse(in_dir, out_dir):
    from os import path, walk
    
    model = w2v_get_model()
    dta = DeepTextAnalyzer(model)

    for root, _, fileList in walk(in_dir):
        for fname in fileList:
            if not is_valid_fname(fname):
                continue
            fout = get_output_fname(fname)
            fpath = path.join(out_dir, fout)
            with open(fpath, 'w') as f:
                fn_geo = get_fn_geo(path.join(root, fname))
                print 'fn_geo: %s' % fn_geo
                fn_orig = get_fn_orig(path.join(root, fname))
                print 'fn_orig: %s' % fn_orig
                if not path.isfile(fn_orig):
                    continue
                texts = get_texts(fn_orig)
                for line in open(fn_geo):
                    parts = line.split('\t')
                    if len(parts) != 4:
                        continue
                    item_id = parts[0]
                    if item_id not in texts:
                        continue
                    text = texts[item_id]
                    w2v_vectors(dta, item_id, text, f)

if __name__ == '__main__':
    from os.path import dirname, realpath
    import sys
    print datetime.today()
    t0 = time()

    # in_dir and out_dir are set to the current directory by default
    in_dir = out_dir = dirname(realpath("__file__"))
    if len(sys.argv)==3:
        in_dir = sys.argv[1]
        out_dir = sys.argv[2]

    traverse(in_dir, out_dir)

    print time() - t0


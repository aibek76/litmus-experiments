The geotagging components performs the following actions:
    1) extracts "location" entity from texts,
    2) determines geographic coordinates (latitude and longitude) based on locations,
    3) computes cell based on geographic coordinates.

Step 1) is implemented by the geoNLP project written in Java. It uses Stanford CoreNLP library to extract "location" entity from texts. The input files should be named as "<name>_orig.txt". Each line is a JSON formatted string of the data returned by social networks API, plus an additional field "stream_type", which should be equal to "Twitter", "Instagram" or "YouTube". See "sample_Twitter_orig.txt" for an example of the input file. The program will generate output files named as "<name>_nlp.txt".

Steps 2) and 3) are implemented in Python, file geocode.py. The input files should be named as "<name>_nlp.txt". See "sample_Twitter_nlp.txt" for an example of the input file. The program will generate output files named as "<name>_geo.txt". geocode.py uses Google Maps Geocoding API, which requires an API key. Follow its documentation to get a key and specify it in the "config.json" file accordingly.


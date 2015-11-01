from datetime import datetime
import codecs, json, time

# load configuration file
config = json.load(codecs.open('config.json', encoding='utf-8'))

def load_locations():
    from os.path import exists
    if not exists(config['locations']):
        return {}
    locations = {}
    for line in open(config['locations']):
        addr, lat, lng = line.rstrip().split('\t')
        if lat == 'None':
            lat = None
        if lng == 'None':
            lng = None
        locations[addr] = {'lat': lat, 'lng': lng}
    return locations

def store_address(address, lat, lng):
    with open(config['locations'], 'a') as f:
        f.write('%s\t%s\t%s\n' % (address, lat, lng))

def google_geocode(address):
    # make a Google Geocode API call
    import httplib, urllib

    host = 'maps.googleapis.com'
    params = {'address': address, 'key': config['google_application_key']}
    url = '/maps/api/geocode/json?'+urllib.urlencode(params)
    req = httplib.HTTPSConnection(host)
    req.putrequest('GET', url)
    req.putheader('Host', host)
    req.endheaders()
    return req.getresponse()

def lookup_address(locations, address):
    if address in locations:
        return locations[address]['lat'], locations[address]['lng']

    lat = lng = None
    resp = google_geocode(address)
    if resp.status==200:
        result = json.load(resp, encoding='UTF-8')
        if 'results' in result:
            results = result['results']
            if len(results) > 0:
                item = results[0]
                if 'geometry' in item:
                    geometry = item['geometry']
                    if 'location' in geometry:
                        location = geometry['location']
                        lat = location['lat']
                        lng = location['lng']
    locations[address] = {'lat': lat, 'lng': lng}
    store_address(address, lat, lng)
    return lat, lng

def is_valid_fname(fname):
    result = False
    # make sure we accept *_nlp.txt files only
    parts = fname.split('.')
    if len(parts)==2:
        name, ext = parts
        if ext=='txt':
            parts = name.split('_')
            if len(parts)>=2:
                if parts[-1]=='nlp':
                    result = True
    return result

def save_location(f, item_id, lat, lng, cell):
    f.write('%s\t%s\t%s\t%s\n' % (item_id, lat, lng, cell))

def get_output_fname(fname):
    name, ext = fname.split('.')
    parts = name.split('_')
    fout = '_'.join(parts[:-1])+'_geo.'+ext
    return fout

def traverse(in_dir, out_dir):
    from os import path, walk
    from utils import generate_cell
    
    locations = load_locations()

    for root, _, fileList in walk(in_dir):
        for fname in fileList:
            if not is_valid_fname(fname):
                continue
            fout = get_output_fname(fname)
            fpath = path.join(out_dir, fout)
            with open(fpath, 'w') as f:
                for line in open(path.join(root, fname)):
                    parts = line.rstrip().split('\t')
                    if len(parts) != 2: continue
                    item_id, address = parts
                    lat, lng = lookup_address(locations, address)
                    cell = generate_cell(lat, lng)
                    save_location(f, item_id, lat, lng, cell)

if __name__ == '__main__':
    from os.path import dirname, realpath
    import sys
    print datetime.today()
    t0 = time.time()
    
    # in_dir and out_dir are set to the current directory by default
    in_dir = out_dir = dirname(realpath("__file__"))
    if len(sys.argv)==3:
        in_dir = sys.argv[1]
        out_dir = sys.argv[2]
    traverse(in_dir, out_dir)

    print time.time() - t0


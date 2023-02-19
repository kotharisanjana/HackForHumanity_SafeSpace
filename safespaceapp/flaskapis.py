from flask import Flask, request
from flask_cors import CORS, cross_origin
import pandas as pd
import numpy as np
from sklearn.cluster import DBSCAN
import json
import requests
from sklearn.metrics.pairwise import haversine_distances

app = Flask(__name__)
CORS(app)

def computeClusterInfo(df):
    d = {'latitude':'centroidLat', 'longitude':'centroidLong', 'clusterId':'personCount'}
    # Find mean and count of cluster
    cluster_info = df.groupby(['clusterId']).agg({'latitude':'mean', 'longitude':'mean', 'clusterId':'count'}).rename(columns=d).reset_index()

    # Find diameter of every cluster
    labels = cluster_info['clusterId'].unique()
    for label in labels:
        temp = cluster_info[cluster_info.clusterId == label]
        coordinates = temp[['centroidLat', 'centroidLong']].to_numpy()
        result = haversine_distances(coordinates)
        result = result*6371
        diameter = max([max(x) for x in result])
        idx = cluster_info[cluster_info.clusterId == label].index.values.astype(int)[0]
        cluster_info.loc[idx, 'radius'] = diameter/2

    return cluster_info

@app.route('/clusters', methods=['POST'])
@cross_origin()
def getClusters():
    if request.method == 'POST':
        coordinates = request.data.decode('utf8')
        coordinates = json.loads(coordinates)

        df = pd.DataFrame(coordinates)
        df = df.iloc[:, 1:-1]
        df = np.radians(df)

        kms_per_radian = 6371.0088
        epsilon = 0.1/kms_per_radian
        min_samples = 1
        dbscan = DBSCAN(eps=epsilon, min_samples=min_samples, algorithm='ball_tree', metric='haversine').fit(df)
        df['clusterId'] = dbscan.labels_
        df['clusterId'] = df['clusterId'].apply(str)

        cluster_info = computeClusterInfo(df)
        
        return cluster_info.to_json(orient='records')


@app.route('/nearbyopenplaces', methods=['POST'])
@cross_origin()
def getNearbyOpenPlaces():
        if request.method == 'POST':
            location = request.data.decode('utf8')
            location = json.loads(location)

            lat, long = (str(location['latitude']), str(location['longitude']))
            radius = 1000
            open_now = True
            # Add in config file
            api_key = 'AIzaSyCVTSwLXsgH-84isnXT6j0-clRiJBSjRR0'

            url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location={lat},{long}&radius={radius}&open_now={open_now}&key={api_key}".format(lat=lat, long=long, radius=radius, open_now=open_now, api_key=api_key)
            response = requests.get(url)
            data = response.json()

            response_list = []
            places_nearby = data['results']
            for place in places_nearby:
                if 'opening_hours' in place:
                    if 'open_now' in place['opening_hours']:
                        response_list.append({'name':place['name'], 'latitude':place['geometry']['location']['lat'], 'longitude':place['geometry']['location']['lng']})

            response_json = json.dumps(response_list)

            return response_json



if __name__=='__main__':
    app.run(host='0.0.0.0', debug=True)
# SafeSpace
## Inspiration
Walking on empty roads can be a source of unease and discomfort for many people, especially when they find themselves in unfamiliar areas. Women, in particular, are at a higher risk of being followed or harassed by stalkers, making them feel even more vulnerable and insecure. The first reaction of any person in such a situation is to walk towards a place where there are people, as being among a crowd provides a sense of security. However, apps like Google Maps do not display information about the number of people on a particular street nor does it suggest if a particular area is considered safe. To address this issue, we have developed an app that uses the number of people in a particular area to suggest safe spaces to users when they find themselves on an empty street.

## What it does
The App has the following three major features:
1) Group comfort: When you launch our app, it displays a map with safe spaces marked as clusters. Each cluster is denoted by a marker that shows the number of people in that area. By using this information, users can make informed decisions about where to go next based on the level of population in a particular area.
2) Open Establishments: Our app displays open stores on the map view, which can be a helpful option for someone in distress. These stores typically have CCTV surveillance and an employee present, providing an added layer of security. Unlike Google Maps, which requires users to tap on each store to check whether it's open or not, our app shows open stores directly on the map view.
3) Distress Mode: Our app includes a Distress Mode feature, which allows users to send a message with their location to chosen contacts and even call 911 with the press of a single button in case of an emergency or if they feel like they are being followed by an unknown person while walking alone.
## Development Journey

### Frontend _(Android)_

The Android integrates with Google Maps SDK which provides a map fragment to which the app loads. Once the map fragment is ready, it loads the user's current location (provided by GPS) and drops an avatar-like marker to represent the user. 

Concurrently, a list of all markers are fetched from the BFF over a `GET` request and then rendered on the map fragment along with the tallies of the number of people in the clusters represented by each of the markers. On checking the _Show Open Stores_ checkbox, a list of all stores open at that very point in time are fetched from the BFF over a similar `GET` request. All API requests are furnished by the Volley library for Android.

Each of the markers allow clicking on them, doing which takes the user to the Google Maps app with the map view centered on the location of the marker in question. The user can then choose to fulfill their flow outside the SafeSpace app _(such as navigate to the marker)_.

The app makes use of `SharedPreferences` for trivial data caching, such as the user's choice of their top 3 emergency contacts as well as a unique ID generated at the time of installing the app.

**Tech Stacks:** Google Maps SDK, `SharedPreferences`, Volley

### Backend _(BFF)_

The BFF was developed using Spring Boot and Java. Controller classes were written that take care of various fulfillment activities as required by the frontend Android app.

This module is in charge of managing data points flowing in from various Android devices across users. For every data point that flows in, the BFF uses an algorithm that computes the _new_ mean of the point with a previously cached point from the same user. This is only if the former point is within the desired maximum cluster radius, and if not, the new data point is simply cached.

The controller also contains a cron-like interface that, using a time-saving Spring annotation `@Scheduled`, polls the Flask backend _(explained below)_ with the recently updated set of data points for each user. The Flask backend then does its _magic_ for clustering the set of points and returning the list of clusters, their centroids and the number of data points within each cluster.

In the real world, every data point is a user and every cluster represents a group of people that are physically close to each other, thereby being present in the same geofence.

Finally, the backend makes use of the Twilio API to interface with WhatsApp to send out emergency messages to the top 3 contacts selected by the user. This occurs every time the user enables the _Distress Mode_ from within the app.

**Tech Stacks:** Spring Boot, Twilio API, Flask, scikit-learn, Google Nearby Search API

## Laudable Accomplishments

We are proud of developing an end-to-end system, comprising of an Android app, a backend system, and some machine learning, all the way from ideation to implementation and live demo in just 24 hours. We challenged ourselves to solve and circumvent complex problems and wade through various technologies and platforms.

In particular, we are proud about how we have managed to handle various operations on top of a simple map view, such as dropping and removing custom markers, handling marker click operations and map callbacks. Furthermore, we are happy that we managed to establish the connection from our backend system to WhatsApp for emergency alerting.

## What's next for SafeSpace
1. Bring SafeSpace on iOS
2. Provide analysis reports about various locations based on historical interactions of users with the application.
3. Getting real time votes from people in those locations about the safety of the place to highlight clusters based on safety levels in real time.

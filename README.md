# android-api-demo

This is the final project for my cloud/mobile development class. It's a cloud-backed Android app that allows users to create accounts and keep track of their weightlifting records. It was written over the course of about 1 week, and any silly features (like using the camera and playing the song "Eye of the Tiger") are requirements of the project, and demonstrate poor usability principles, by design.

I wrote a <a href="http://seifert-mobile.appspot.com">simple public API</a>, hosted on Google App Engine. It does not include caching (this was not covered in class), but otherwise conforms to most RESTful principles.

The purpose of the project was to create an application that interacts meaningfully with an API of our own construction; the focus was on demonstrating different types of POST, GET, PUT, and DELETE requests, and considerations like aesthetics were not emphasized.

If you'd like to view the application in action, I have a ~5 minute demo of the application running <a href="http://web.engr.oregonstate.edu/~seiferjo/CS496/finalDemo.mp4">here</a>.

As with most of my other school assignments, I want to emphasize that this is not a secure app by any means. The API relies largely on security by obscurity, and data is not encrypted, so I would advise against using any "real" passwords or usernames when testing this software.

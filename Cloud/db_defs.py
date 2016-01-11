from google.appengine.ext import ndb

class User(ndb.Model):
	name = ndb.StringProperty(required=True)
	password = ndb.StringProperty(required=True)
	sessions = ndb.StringProperty(repeated=True)
	current_weight = ndb.IntegerProperty()
	target_weight = ndb.IntegerProperty()
	
class Session(ndb.Model):
	user = ndb.StringProperty()
	date = ndb.StringProperty() # was date property, but cannot be easily JSONified. Used as primary key, since won't lift twice on one day
	lifts = ndb.StringProperty(repeated=True)
	
class Lift(ndb.Model):
	user = ndb.StringProperty()
	session = ndb.StringProperty()
	liftName = ndb.StringProperty(required=True)
	weight = ndb.IntegerProperty()
	reps = ndb.IntegerProperty()
	sets = ndb.IntegerProperty()
	# crushedIt = ndb.BooleanProperty() # dumb joke, removed
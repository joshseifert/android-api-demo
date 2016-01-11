import json
import webapp2
from google.appengine.ext import ndb
import db_defs

# All entities are part of the same parent group. Limits number of writes per second, but I 
# don't see that being an issue for an api of this scale.
PK = ndb.Key("default-group", "base-data")

# Simple helper function to reduce redundancy in classes
def error_message(self, err_code, err_message):
	self.response.status = err_code
	self.response.write(err_message)
	return

# Simple description of how to use the API, if the user browses to the API's main page.
class MainPage(webapp2.RequestHandler):
	def get(self):
		instructionString = """
			You may access the resources with the following extensions: \n\n\
			/user - POST - create a new user \n\
			/user/&lt;uid&gt; - GET - get info on a specific user \n\
			/user/&lt;uid&gt; - PUT - change info on a specific user \n\
			/user/&lt;uid&gt; - DELETE - delete a specific user \n\n\
			/user/&lt;uid&gt;/session - POST - add a new lifting session for specific user \n\
			/user/&lt;uid&gt;/session/&lt;sid&gt; - GET - get info on a specific lifting session \n\
			/user/&lt;uid&gt;/session/&lt;sid&gt; - PUT - change info on a specific lifting session \n\
			/user/&lt;uid&gt;/session/&lt;sid&gt; - DELETE - delete a specific lifting session \n\n\
			/user/&lt;uid&gt;/session/&lt;sid&gt;/lift - POST - add a new lift to a specific session \n\
			/user/&lt;uid&gt;/session/&lt;sid&gt;/lift/&lt;lid&gt; - GET - get info on a lift from a specific session \n\
			/user/&lt;uid&gt;/session/&lt;sid&gt;/lift/&lt;lid&gt; - PUT - change info on a lift from a specific session \n\
			/user/&lt;uid&gt;/session/&lt;sid&gt;/lift/&lt;lid&gt; - DELETE - delete a lift from a specific session \n\
		"""
		self.response.write(instructionString.replace("\n", "<br />"))

# The "User" class handles all of the API calls related to the User entity.
		
class User(webapp2.RequestHandler):

	# Gets the information for a specific user. In the mobile application, this occurs once the user 
	# logs in. On the main page, perform a GET request on the logged in user to retrieve information on 
	# their current weight and goals, and a list of weightlifting sessions in the datastore.
	def get(self, **kwargs):
	
		if 'application/json' not in self.request.accept:
			error_message(self, 406, "Error: API only accepts JSON requests at this time.")
			return
			
		if 'uid' in kwargs:
			q = db_defs.User.query(db_defs.User.name == kwargs['uid']).fetch()
			if not q:
				error_message(self, 404, "User not found.")
				return
			self.response.write(json.dumps([x.to_dict() for x in q], indent=4, separators=(',', ': ')))
		else:
			error_message(self, 406, "Must specify a user name to search")
			return
	
	# POST can either log user in, or create new user
	def post(self):
	
		if 'application/json' not in self.request.accept:
			error_message(self, 406, "Error: API only accepts JSON requests at this time.")
			return
			
		login = self.request.get("login", default_value=False)
		
		# Attempt to log in
		if login:
		
			name = self.request.get("name", default_value=None)
			if not name:
				error_message(self, 406, "You must include a user name.")
				return
				
			password = self.request.get("password", default_value=None)
			if not password:
				error_message(self, 406, "You must include a password.")
				return
			
			# If there is a User entity that matches both the username and password, log in
			q = db_defs.User.query(db_defs.User.name == name and db_defs.User.password == password).get()
			if q:
				self.response.write("SUCCESS")
			else:
				self.response.status = 406
				self.response.write("FAIL")
				
		# Add new user. Used to register a new account.
		else:
		
			name = self.request.get("name", default_value=None)
			if not name:
				error_message(self, 406, "You must include a user name.")
				return
			elif db_defs.User.query(db_defs.User.name == name).fetch():
				error_message(self, 406, "User name taken.")
				return
				
			password = self.request.get("password", default_value=None)
			if not password:
				error_message(self, 406, "You must include a password.")
				return
			elif len(password) < 4:
				error_message(self, 406, "Password must be at least 4 characters.")
				return
			
			# From the mobile application, current weight and target weight are never passed during 
			# account registration, but this API is designed to be used besides with just that one app.
			current_weight = self.request.get("current_weight", default_value=None)
			if current_weight:
				try:
					current_weight = int(current_weight)
				except:
					error_message(self, 400, "Current weight field must be an integer.")
					return
					
			target_weight = self.request.get("target_weight", default_value=None)
			if target_weight:
				try:
					target_weight = int(target_weight)
				except:
					error_message(self, 400, "Target weight field must be an integer.")
					return
			
			sessions = self.request.get_all("sessions[]", default_value=None)
				
			new_user = db_defs.User(parent=PK)
			new_user.name = name
			new_user.password = password
			new_user.current_weight = current_weight
			new_user.target_weight = target_weight
			new_user.sessions = sessions
			
			key = new_user.put()
			out = new_user.to_dict()
			self.response.write(json.dumps(out, indent=4, separators=(',', ': ')))
		
	# Deletes the user account. Not used in the mobile application.	
	def delete(self, **kwargs):
	
		if 'application/json' not in self.request.accept:
			error_message(self, 406, "Error: API only accepts JSON requests at this time.")
			return

		if 'uid' in kwargs:
			q = db_defs.User.query(db_defs.User.name == kwargs['uid']).get()
			if not q:
				error_message(self, 404, "User not found.")
				return
			ukey = q.key.get()
			ukey.key.delete()
			self.response.write("User %s deleted" % kwargs['uid'])
		else:
			error_message(self, 400, "You must include the name of the user to delete.")
	
	# Edits the user account. Allows the user to specify their current / target weights, as well 
	# as change the password. Specific sessions may be changed here, or by editing the sessions themselves.
	def put(self, **kwargs):
	
		if 'application/json' not in self.request.accept:
			error_message(self, 406, "Error: API only accepts JSON requests at this time.")
			return

		if 'uid' in kwargs:
			q = db_defs.User.query(db_defs.User.name == kwargs['uid']).get()
			if not q:
				error_message(self, 404, "User not found.")
				return
			
			password = self.request.get("password", default_value=None)
			if not password:
				error_message(self, 406, "You must include a password.")
				return
			elif len(password) < 4:
				error_message(self, 406, "Password must be at least 4 characters.")
				return
			
			current_weight = self.request.get("current_weight", default_value=None)
			if current_weight:
				try:
					current_weight = int(current_weight)
				except:
					error_message(self, 400, "Current weight field must be an integer.")
					return
				
			target_weight = self.request.get("target_weight", default_value=None)
			if target_weight:
				try:
					target_weight = int(target_weight)
				except:
					error_message(self, 400, "Target weight field must be an integer.")
					return
			
			sessions = self.request.get_all("sessions[]", default_value=None)
			if sessions:
				for session in sessions:
					if session not in q.sessions:
						q.sessions.append(session)
						
			q.password = password	
			q.current_weight = current_weight
			q.target_weight = target_weight
			
			key = q.put()
			out = q.to_dict()
			self.response.write(json.dumps(out, indent=4, separators=(',', ': ')))				
		else:
			error_message(self, 400, "You must include the name of the user to edit.")					
				

# All API calls regarding the session entity.
class Session(webapp2.RequestHandler):

	def get(self, **kwargs):
		if 'application/json' not in self.request.accept:
			error_message(self, 406, "Error: API only accepts JSON requests at this time.")
			return
	
		# Must include the user's name when calling a session, so you get the session specific to that user
		if not 'uid' in kwargs:
			error_message(self, 406, "Must log in with valid username.")
			return
			
		# Get all sessions for user. Not used in mobile application
		if not 'sid' in kwargs:
			q = db_defs.Session.query(db_defs.Session.user == kwargs['uid']).fetch()
		# Get specific session for user. Used when user specifies a session date.
		else:
			q = db_defs.Session.query(db_defs.Session.user == kwargs['uid'] and db_defs.Session.date == kwargs['sid']).fetch()
			if not q:
				error_message(self, 404, "Error: Session not found.")
				return
		self.response.write(json.dumps([x.to_dict() for x in q], indent=4, separators=(',',': ')))

	# Create a new session. 
	def post(self, **kwargs):
		if 'application/json' not in self.request.accept:
			error_message(self, 406, "Error: API only accepts JSON requests at this time.")
			return
		
		if not 'uid' in kwargs:
			error_message(self, 406, "Must log in with valid username.")
			return		
		
		date = self.request.get("date", default_value=None)
		if not date:
			error_message(self, 406, "You must include a date.")
			return
				
		lifts = self.request.get_all("lifts[]", default_value=None)
			
		# Create new session entity
		new_session = db_defs.Session(parent=PK)
		new_session.user = kwargs['uid']
		new_session.date = date
		new_session.lifts = lifts
			
		#Add date of session to user's profile, verify it is unique
		q = db_defs.User.query(db_defs.User.name == kwargs['uid']).get()
		if date in q.sessions:
			error_message(self, 406, "Bro, you already lifted on that date!")
			return			
		q.sessions.append(date)
		q.put()
		
		# Add new session to datastore
		key = new_session.put()
		out = new_session.to_dict()
		self.response.write(json.dumps(out, indent=4, separators=(',', ': ')))
			
	# Delete a specific session
	def delete(self, **kwargs):
		if 'application/json' not in self.request.accept:
			error_message(self, 406, "Error: API only accepts JSON requests at this time.")
			return
		
		if not 'uid' in kwargs:
			error_message(self, 406, "Must log in with valid username.")
			return
	
		if not 'sid' in kwargs:
			error_message(self, 406, "Must include a session date to delete. yyyy-mm-dd.")
			return
	
		# Find session based on user name and date
		sq = db_defs.Session.query(db_defs.Session.user == kwargs['uid'] and db_defs.Session.date == kwargs['sid']).get()
		if not sq:
			error_message(self, 404, "Session not found.")
			return
			
		# Remove session from user's session list
		uq = db_defs.User.query(db_defs.User.name == kwargs['uid']).get()
		uq.sessions.remove(kwargs['sid'])
		uq.put()		
		
		# Delete the session
		skey = sq.key.get()
		result = skey.key.delete()
		
		self.response.status = 200
		self.response.write("Session %s deleted" % kwargs['sid'])
	
	# Edit the session. May not edit the "user" attribute, since this identifies who the session belongs to.
	def put(self, **kwargs):
		if 'application/json' not in self.request.accept:
			error_message(self, 406, "Error: API only accepts JSON requests at this time.")
			return
		
		if not 'uid' in kwargs:
			error_message(self, 406, "Must log in with valid username.")
			return
	
		if not 'sid' in kwargs:
			error_message(self, 406, "Must include a session date to modify. yyyy-mm-dd.")
			return		

		# The only attribute you may edit in the session is the date. Modify or delete lifts via the 'lifts' URI
		date = self.request.get("date", default_value=None)
		if not date:
			error_message(self, 406, "You must include a date.")
			return
			
		# Find session based on user name and date
		sq = db_defs.Session.query(db_defs.Session.user == kwargs['uid'] and db_defs.Session.date == kwargs['sid']).get()
		if not sq:
			error_message(self, 404, "Session not found.")
			return
			
		# Edit session listing in user's session list
		uq = db_defs.User.query(db_defs.User.name == kwargs['uid']).get()
		uq.sessions.remove(kwargs['sid'])
		uq.sessions.append(date)
		uq.put()
		
		# Edit session entity itself
		sq.date = date
		key = sq.put()
		out = sq.to_dict()
		self.response.status = 200
		self.response.write("Session %s modified" % kwargs['sid'])
		
# All API calls regarding the Lift entity, in a many-one relationship with sessions.		
class Lift(webapp2.RequestHandler):

	# Get a specific lift from a specific session. May not get all lifts at once.
	def get(self, **kwargs):
	
		if 'application/json' not in self.request.accept:
			error_message(self, 406, "Error: API only accepts JSON requests at this time.")
			return
	
		# Get specific user
		if not 'uid' in kwargs:
			error_message(self, 406, "Must log in with valid username.")
			return
			
		# Get specific session for user
		if not 'sid' in kwargs:
			error_message(self, 406, "Must include a session date. yyyy-mm-dd.")
			return
		
		# Get specific lift for session
		if not 'lid' in kwargs:
			error_message(self, 406, "Must include the lift name.")
			return

		# To verify uniqueness, has to match on user AND session AND lift name.
		q = db_defs.Lift.query((db_defs.Lift.user == kwargs['uid']) and (db_defs.Lift.session == kwargs['sid']) and (db_defs.Lift.liftName == kwargs['lid'])).fetch()
		if not q:
			error_message(self, 404, "Error: Lift not found.")
			return
		self.response.write(json.dumps([x.to_dict() for x in q], indent=4, separators=(',',': ')))
		
	def post(self, **kwargs):
	
		if 'application/json' not in self.request.accept:
			error_message(self, 406, "Error: API only accepts JSON requests at this time.")
			return
	
		if not 'uid' in kwargs:
			error_message(self, 406, "Must log in with valid username.")
			return
			
		# Get specific session for user
		if not 'sid' in kwargs:
			error_message(self, 406, "Must include a session date. yyyy-mm-dd.")
			return
			
		
		liftName = self.request.get("lift", default_value=None)
		if not liftName:
			error_message(self, 406, "You must include the name of the lift.")
			return
			
		weight = self.request.get("weight", default_value=None)
		if weight:
			try:
				weight = int(weight)
			except:
				error_message(self, 406, "Weight lifted must be an integer.")
				return
			
		sets = self.request.get("sets", default_value=None)
		if sets:
			try:
				sets = int(sets)
			except:
				error_message(self, 406, "Number of sets lifted must be an integer.")
				return
			
		reps = self.request.get("reps", default_value=None)
		if reps:
			try:
				reps = int(reps)
			except:
				error_message(self, 406, "Number of reps lifted must be an integer.")
				return
				
		new_lift = db_defs.Lift(parent=PK)
		new_lift.user = kwargs['uid']
		new_lift.session = kwargs['sid']
		new_lift.liftName = liftName
		new_lift.weight = weight
		new_lift.sets = sets
		new_lift.reps = reps
			
		#Add lift name to session profile, make sure it is unique
		q = db_defs.Session.query(db_defs.Session.user == kwargs['uid'] and db_defs.Session.date == kwargs['sid']).get()
		if liftName in q.lifts:
			error_message(self, 406, "Bro, you already recorded that lift! Use PUT to edit.")
			return			
		q.lifts.append(liftName)
		q.put()
		
		# Add new session to datastore
		key = new_lift.put()
		out = new_lift.to_dict()
		self.response.write(json.dumps(out, indent=4, separators=(',', ': ')))
		
	def delete(self, **kwargs):
	
		if 'application/json' not in self.request.accept:
			error_message(self, 406, "Error: API only accepts JSON requests at this time.")
			return
	
		if not 'uid' in kwargs:
			error_message(self, 406, "Must log in with valid username.")
			return
			
		# Get specific session for user
		if not 'sid' in kwargs:
			error_message(self, 406, "Must include a session date to delete. yyyy-mm-dd.")
			return
		
		# Get specific lift for session
		if not 'lid' in kwargs:
			error_message(self, 406, "Must include the lift name to delete.")
			return
			
		# Find lift based on user name, date, lift name
		
		lq = db_defs.Lift.query(db_defs.Lift.user == kwargs['uid'] and db_defs.Lift.session == kwargs['sid'] and db_defs.Lift.liftName == kwargs['lid']).get()
		if not lq:
			error_message(self, 404, "Lift not found.")
			return
			
		# Remove lift from session list
		sq = db_defs.Session.query(db_defs.Session.user == kwargs['uid'] and db_defs.Session.date == kwargs['sid']).get()
		sq.lifts.remove(kwargs['lid'])
		sq.put()		
		
		# Delete the session
		lkey = lq.key.get()
		lkey.key.delete()
		self.response.status = 200
		self.response.write("Lift %s deleted" % kwargs['lid'])

	def put(self, **kwargs):
		
		if 'application/json' not in self.request.accept:
			error_message(self, 406, "Error: API only accepts JSON requests at this time.")
			return
	
		if not 'uid' in kwargs:
			error_message(self, 406, "Must log in with valid username.")
			return
			
		# Get specific session for user
		if not 'sid' in kwargs:
			error_message(self, 406, "Must include a session date to edit. yyyy-mm-dd.")
			return
		
		# Get specific lift for session
		if not 'lid' in kwargs:
			error_message(self, 406, "Must include the lift name to edit.")
			return
		
		liftName = self.request.get("lift", default_value=None)
		if not liftName:
			error_message(self, 406, "You must include a new lift name.")
			return			
		
		weight = self.request.get("weight", default_value=None)
		if weight:
			try:
				weight = int(weight)
			except:
				error_message(self, 406, "Weight lifted must be an integer.")
				return
			
		sets = self.request.get("sets", default_value=None)
		if sets:
			try:
				sets = int(sets)
			except:
				error_message(self, 406, "Number of sets lifted must be an integer.")
				return
			
		reps = self.request.get("reps", default_value=None)
		if reps:
			try:
				reps = int(reps)
			except:
				error_message(self, 406, "Number of reps lifted must be an integer.")
				return
			
		# Find lift based on user name, session date, and lift name
		lq = db_defs.Lift.query(db_defs.Lift.user == kwargs['uid'] and db_defs.Lift.session == kwargs['sid'] and db_defs.Lift.liftName == kwargs['lid']).get()
		if not lq:
			error_message(self, 404, "Lift not found.")
			return
			
		# Edit lift listing in session lift list
		sq = db_defs.Session.query(db_defs.Session.user == kwargs['uid'] and db_defs.Session.date == kwargs['sid']).get()
		sq.lifts.remove(kwargs['lid'])
		sq.lifts.append(liftName)
		sq.put()
		
		# Edit Lift
		lq.liftName = liftName
		lq.weight = weight
		lq.sets = sets
		lq.reps = reps
		key = lq.put()
		out = lq.to_dict()
		self.response.status = 200
		self.response.write("Lift %s modified" % kwargs['lid'])	

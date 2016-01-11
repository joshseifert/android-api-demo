import webapp2
import api_handler

app = webapp2.WSGIApplication([], debug=True)

app.router.add(webapp2.Route(r'/', api_handler.MainPage))
app.router.add(webapp2.Route(r'/user', api_handler.User))
app.router.add(webapp2.Route(r'/user/<uid>', api_handler.User))
app.router.add(webapp2.Route(r'/user/<uid>/session', api_handler.Session))
app.router.add(webapp2.Route(r'/user/<uid>/session/<sid>', api_handler.Session))
app.router.add(webapp2.Route(r'/user/<uid>/session/<sid>/lift', api_handler.Lift))
app.router.add(webapp2.Route(r'/user/<uid>/session/<sid>/lift/<lid>', api_handler.Lift))
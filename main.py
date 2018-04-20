#!/usr/bin/env python

# [START imports]
from google.appengine.ext import ndb
from google.appengine.api import urlfetch
import webapp2
import json
import urllib
# [END imports]


# [START data]

class ModelUtils(object):
	def to_dict(self):
		result = super(ModelUtils,self).to_dict()
		result['key'] =self.key.id() #get the key as a string
		return result
		
		
class Gift(ModelUtils, ndb.Model):
	id = ndb.StringProperty()
	santa = ndb.StringProperty()
	recipient = ndb.StringProperty(required=True)
	gift = ndb.StringProperty(required=True)
	giftwrapped = ndb.StringProperty(required=True)
	price = ndb.StringProperty(required=True)
	
# [END data]

# [START Gift requests]
class GiftHandler(webapp2.RequestHandler):

	#Create Gift
	def post(self):
		gift_data = json.loads(self.request.body) #get request body
		header_data = (self.request.headers)
		if 'Idtoken' in header_data:
			coded = header_data['Idtoken']
			#self.response.write(coded)
			url = "https://www.googleapis.com/oauth2/v3/tokeninfo?id_token=%s" % coded
			decoded = urlfetch.fetch(url=url, method=urlfetch.GET)
			googleDict = json.loads(decoded.content) #load json to dict
			#self.response.write(googleDict)
			sub = googleDict['sub']
			#self.response.write(sub)
			if gift_data['recipient'] != "":	#validation for required fields			
				if gift_data['gift'] != "":		
					if gift_data['giftwrapped'] != "":	
						if gift_data['price'] != "":		
							new_gift = Gift(recipient=gift_data['recipient'], gift=gift_data['gift'], giftwrapped=gift_data['giftwrapped'], price=gift_data['price']) #create gift object & set field values
							new_gift.put()
							new_gift.id = new_gift.key.urlsafe()	#set auto-generated value
							new_gift.put()							#store data in database
							new_gift.santa = sub		#set default value
							new_gift.put()				#store data in database
							gift_dict = new_gift.to_dict() 		#convert to dictionary
							gift_dict['self'] = '/gifts/' + new_gift.key.urlsafe()	#create url for gift
							self.response.write(json.dumps(gift_dict, indent=4, sort_keys=False)) #return json response
							self.response.set_status(201) #return status
						else:
							self.abort(403) #error status if missing required field
					else:
						self.abort(403) #error status if missing required field
				else:
					self.abort(403) #error status if missing required field
			else:
					self.abort(403) #error status if missing required field
		else:
			self.abort(403) #error status if missing required field
	
	#View Gift(s)
	def get(self, gift_id=None):
		header_data = (self.request.headers)
		if 'Idtoken' in header_data:
			coded = header_data['Idtoken']
			url = "https://www.googleapis.com/oauth2/v3/tokeninfo?id_token=%s" % coded
			decoded = urlfetch.fetch(url=url, method=urlfetch.GET)
			googleDict = json.loads(decoded.content) #load json to dict
			sub = googleDict['sub']
			if gift_id:		#single gift object (unused in this iteration)
				g = ndb.Key(urlsafe=gift_id).get()  #get gift with requested id
				if g.key.kind() == "Gift":		#id is for gift object *
					g_d = g.to_dict()				#convert object to dict
					g_d['self'] = '/gifts/' + g.key.urlsafe()	#create url for gift
					self.response.write(json.dumps(g_d, indent=4, sort_keys=False))	#return json response
				else:							#id is not for gift object
					self.response.set_status(404)				#error response if invalid id
					self.response.write("Unable to locate gift")
			else:		#all gifts for user
				santa = sub
				#self.response.write(santa)
				gifts = [] 				#gifts list	*
				query = Gift.query(Gift.santa == santa)	#creates a query object- find all gifts
				for gift in query:		#loop through each gift in query results
					g = gift.key.get()	#get gift with requested key/id
					g_d = g.to_dict()	#convert object to dict
					g_d['self'] = '/gifts/' + gift.key.urlsafe()	#create url for gift		
					gifts.append(g_d)	#add to list
				self.response.write(json.dumps(gifts, indent=4, sort_keys=False))	#return json response
		else:
			self.abort(403) #error status if missing required field
					
	#Delete Gift
	def delete(self, gift_id=None):
		if gift_id:
			query = Gift.query(Gift.id == gift_id) #creates a query object- find gift with requested id
			for gift in query:
				g = ndb.Key(urlsafe=gift_id).get()	#get gift with requested id
				g.key.delete()				#delete gift
			self.response.set_status(204) #response if and when gift deleted 
		else:
			self.response.set_status(400)	#error response if no id
			self.response.write("Error: request not available")
	
	#Modify Gift
	def patch(self, gift_id=None):
		if gift_id:
			g = ndb.Key(urlsafe=gift_id).get()	#get gift with requested id
			if g.key.kind() == "Gift":
				gift_data = json.loads(self.request.body)	#get request body
				if gift_data.get('recipient'):		#change recipient if requested *
					g.recipient = gift_data['recipient']
				if gift_data.get('gift'): 		#change gift if requested	
					g.gift = gift_data['gift']
				if gift_data.get('giftwrapped'):		#change giftwrapped if requested
					g.giftwrapped = gift_data['giftwrapped']
				if gift_data.get('price'):		#change price if requested
					g.price = gift_data['price']
				else:
					self.response.set_status(400)	#error response if other field entered
					self.response.write("Error: request not available")
				g.put()								#update database
				g = ndb.Key(urlsafe=gift_id).get()		#get gift with requested id
				g_d = g.to_dict()						#convert object to dict
				g_d['self'] = '/gifts/' + g.key.urlsafe()			#create url for gift
				self.response.write(json.dumps(g_d, indent=4, sort_keys=False))	#return json response
			else:							#id is not for gift
				self.response.set_status(404)				#error response if invalid id
				self.response.write("Unable to locate gift")
		else:
			self.response.set_status(400)	#error response if no id
			self.response.write("Error: request not available")
			
		
		
	#Replace Gift	 (unused in this iteration)		
	def put(self, gift_id=None):
		null = None
		if gift_id:
			g = ndb.Key(urlsafe=gift_id).get()			#get gift with requested id
			if g.key.kind() == "Gift":		#id is for gift
				gift_data = json.loads(self.request.body)	#get request body
				if 'recipient' in gift_data:						#check that recipient is in request body and change *
					g.recipient = gift_data['recipient']
				else:										#if recipient is missing return error
					self.abort(403)
				if 'gift' in gift_data:						#check that gift is in request body and change
					g.gift = gift_data['gift']
				else:										#if gift is missing return error
					self.abort(403)							
				if 'price' in gift_data:					#check that price is in request body and change
					g.price = gift_data['price']
				else:										#if price is missing return error
					self.abort(403)								
				g.put()										#update database
				g = ndb.Key(urlsafe=gift_id).get()				#get gift with requested id
				g_d = g.to_dict()							#convert object to dict
				g_d['self'] = '/gifts/' + g.key.urlsafe()				#create url for gift
				self.response.write(json.dumps(g_d, indent=4, sort_keys=True))		#return json response
			else:											#id is not for gift
				self.response.set_status(404)				#error response if invalid id
				self.response.write("Unable to locate gift")
		else:
			self.response.set_status(400)					#error response if no id
			self.response.write("Error: request not available")
			
# [END Gift requests]


# [START main_page]
class MainPage(webapp2.RequestHandler):

    def get(self):
        self.response.write("Santa's List")
		
# [END main_page]

# [START app]
allowed_methods = webapp2.WSGIApplication.allowed_methods
new_allowed_methods = allowed_methods.union(('PATCH',))
webapp2.WSGIApplication.allowed_methods = new_allowed_methods

app = webapp2.WSGIApplication([
    ('/', MainPage),
	('/gifts', GiftHandler),
	('/gifts/([A-z0-9\-]+)', GiftHandler), #*
], debug=True)
# [END app]


#Sources:
#https://stackoverflow.com/questions/16850136/ndb-to-dict-method-does-not-include-objects-key/16850717#16850717
#Code used with permission from: https://media.oregonstate.edu/media/t/0_nph1lmls 
#TAs and Instructor *


























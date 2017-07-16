;(function() {
	if (typeof console  != "undefined") 
		if (typeof console.log != 'undefined')
			console.olog = console.log;
		else
			console.olog = function() {};

	console.log = function(message) {
		console.olog(message);
		$('#logDiv').append('<p>' + message + '</p>');
	};
	console.error = console.debug = console.info =  console.log

	
	var Account = Backbone.Model.extend({
		defaults: {
			accountId: 0,
			siteName: "",
			siteUrl: "",
			userName: "",
			password: "",
			encrypted: false,
			email: ""
		},
		initialize: function() {
			console.log("creating account ");
			this.on("change", function() {
				console.log("account changed");
			})
		},
		
		validate: function(attributes) {
			if(attributes.siteName === undefined) {
				return "remember to set siteName";
			}
			if(attributes.siteUrl === undefined) {
				return "remember to set siteUrl";
			}
			if(attributes.userName === undefined) {
				return "remember to set userName";
			}
			if(attributes.password === undefined) {
				return "remember to set password";
			}
		}
		
	});
	
	var account1 = new Account();
})();
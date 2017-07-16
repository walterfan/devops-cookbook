function loadlinks() {
	
    var aLinkList = [
            {
                    id: 1,
                    title: "sina",
                    url: "http://www.sina.com.cn",
                    tags: "portal,news",
                    category: "site"
            },
            {
                    id: 2,
                    title: "ifeng",
                    url: "http://www.ifeng.com",
                    tags: "news",
                    category: "site"
            },
            {
                    id: 3,
                    title: "infoq",
                    url: "http://www.infoq.com/cn",
                    tags: "technique",
                    category: "site"
            }
    ];
	
	var CLink = Backbone.Model.extend({
		defaults: {
			id: 0,
			title: "",
			url: "",
			tags: "",
			category: "site"
		} 
	});
	
	var CLinks = Backbone.Collection.extend({
		model: CLink
	});
	
	var CLinkView = Backbone.View.extend({
		el: "#linkTab>tbody",
		
		initialize: function() {
			this.template = _.template($('#link-tpl').html());
		},
		
		render: function() {
			var curList = this.collection.models;
			/*
			this.el.innerHTML = "<ul>";
			console.log(this.el.innerHTML);
			for(var i=0; i<curList.length; i++) {
				console.log(curList[i].get("title"));
				this.el.innerHTML += "<li>" + curList[i].get("title") + "</li><br>";
			}
			this.el.innerHTML += "</ul>";	
			console.log("see: "+ this.el.innerHTML);
			*/
		
			for(var i=0; i<curList.length; i++) {
				console.log(curList[i].get("title"));
				this.el.innerHTML += this.template(curList[i].toJSON());
			}
	
		}
		
	});
	
	var aLinks = new CLinks(aLinkList);
	var aView = new CLinkView({collection: aLinks});
	aView.render();
}

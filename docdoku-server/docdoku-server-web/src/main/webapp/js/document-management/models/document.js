define([
	"i18n",
	"common/date",
	"collections/document_iteration"
], function (
	i18n,
	date,
	DocumentIterationList
) {
	var Document = Backbone.Model.extend({
		initialize: function () {
            var doc = this;

            this.className = "Document";

            if (kumo.isNotEmpty(this.id)){
                var index = this.id.lastIndexOf("-");
                if (index >0 ){
                    this.set("docKey", this.id);

                    //the name may contain some '-'
                    this.set("name", this.id.substring(0, index));
                    this.set("version", this.id.substring(index+1, this.id.length));

                    //setting doc object to iterations
                    _.each(this.iterations.models, function(iteration){
                        kumo.assert(iteration.className=="DocumentIteration");
                        iteration.set("document",  doc);
                    });


                }else{
                    console.error("Document id "+this.id+" should contain '-'");
                }
            }


            //this.set("workspace", APP_CONFIG.workspaceId);


			_.bindAll(this);
		},
		parse: function(data) {

			this.iterations = new DocumentIterationList(data.documentIterations);
			this.iterations.document = this;

            //setting lastiteration
			if (data.documentIterations.length) {
				this.lastIteration = this.iterations.get(data.documentIterations[data.documentIterations.length - 1].iteration);
				data.lastIteration = this.lastIteration;
			}


			data.documentIterations = this.iterations;
			return data;
		},

        getDocKey : function(){
            if (kumo.isEmpty(this.get("docKey"))){
                //looks like being a problem just after the creation of a document
                console.error("Creation of a document ; docKey not set, sending back the id");
                return this.id;
            }else{
                return this.get("docKey");
            }

        },

        getName : function(){
            return this.get("name");
        },

        getVersion : function(){
          return this.get("version");
        },

        getWorkspace : function(){
          return this.get("workspaceId");
        },

        getUrl : function(){
          return this.url();
        },

        getLastIteration : function(){
           return this.get("lastIteration");
        },

        getIterations : function(){
          return this.get("documentIterations");
        },



        /**
         * @deprecated ; use this.getUrl()
         * TODO : track and remove calls to this function
         */
		url: function() {

			if (this.get("id")) {
				return  "/api/workspaces/" + this.getWorkspace()+ "/documents/"+this.getDocKey();
			} else if (this.collection) {
                console.log("Creation of a document ; we send back the url of the folder collection");
				return this.collection.url;
			}
		},
		checkout: function () {
			$.ajax({
				context: this,
				type: "PUT",
				url: this.url() + "/checkout",
				success: function () {
					this.fetch();
				}
			});
		},
		undocheckout: function () {
			$.ajax({
				context: this,
				type: "PUT",
				url: this.url() + "/undocheckout",
				success: function () {
					this.fetch();
				}
			});
		},
		checkin: function () {
			$.ajax({
				context: this,
				type: "PUT",
				url: this.url() + "/checkin",
				success: function () {
					this.fetch();
				}
			});
		}
	});
	return Document;
});

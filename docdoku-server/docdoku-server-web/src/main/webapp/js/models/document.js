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

            var idSplit = this.id.split("-");
            kumo.assert(idSplit.length == 2, "the id should be in the form document-VERSION");
            this.set("docKey", this.id);
            this.set("name", idSplit[0]);
            this.set("version", idSplit[1]);
            this.set("workspace", APP_CONFIG.workspaceId);

            _.each(this.iterations.models, function(iteration){
                kumo.assert(iteration.className=="DocumentIteration");
                iteration.set("document",  doc);
            });

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
            return this.get("docKey");
        },

        getName : function(){
            return this.get("name");
        },

        getVersion : function(){
          return this.get("version");
        },

        getWorkspace : function(){
          return this.get("workspace");
        },

        getUrl : function(){
          return this.url();
        },

        /**
         * @deprecated ; use this.getUrl()
         * TODO : track and remove calls to this function
         */
		url: function() {

			if (this.get("id")) {
				return  "/api/workspaces/" + this.getWorkspace()+ "/documents/"+this.getDocKey();
			} else if (this.collection) {
                console.log("We are here in document.url with no id ;");
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

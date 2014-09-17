/*global define*/
define(['backbone','common-objects/models/product_baseline','common-objects/models/document_baseline'],function(Backbone, ProductBaseline,DocumentBaseline){
	'use strict';
	var Baselines = Backbone.Collection.extend({
		initialize:function(attributes, options){
			if(options){
				this.type = (options.type) ? options.type : 'product';
				if(this.type==='product') {
					this.productId = options.productId;
				}
			}
		},

		model: function(attrs, options){
			switch (options.collection.type){
				case 'document' : return new DocumentBaseline(attrs,options);
				case 'product' : return new ProductBaseline(attrs,options);
				default : return null;
			}
		},

		url: function(){
			switch (this.type){
				case 'document' :
					return APP_CONFIG.contextPath + '/api/workspaces/' + APP_CONFIG.workspaceId + '/document-baselines/';
				case 'product' :
					if(this.productId){
						return APP_CONFIG.contextPath + '/api/workspaces/' + APP_CONFIG.workspaceId + '/products/' + this.productId + '/baselines';
					}
					return APP_CONFIG.contextPath + '/api/workspaces/' + APP_CONFIG.workspaceId + '/products/baselines';
				default : return "";
			}
		}

	});

	return Baselines;
});
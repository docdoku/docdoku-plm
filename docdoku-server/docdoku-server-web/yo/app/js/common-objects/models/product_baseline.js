/*global $,define*/

define(['common-objects/models/baseline'],
function(Baseline){
	'use strict';
	var ProductBaseline = Baseline.extend({
		getType:function(){
			return this.get('type');
		},
		isReleased:function(){
			return this.get('type')==='RELEASED';
		},
		getBaselinedParts:function(){
			return this.get('baselinedParts');
		},
		getConfigurationItemId: function(){
			return this.get('configurationItemId');
		},
		setConfigurationItemId: function(configurationItemId){
			this.set('configurationItemId',configurationItemId);
		},
		getBaselinePartsWithReference:function(ref,callback){
			var baselinedParts=null;
			$.getJSON(this.url()+'/parts?q='+ref)
				.success(function(data){
					baselinedParts=data;
					if(callback && callback.success){
						callback.success(data);
					}
				})
				.error(function(data){
					if(callback && callback.error){
						callback.error(data);
					}
				});
			return baselinedParts;
		},

		duplicate:function(args){
			var _this = this;
			$.ajax({
				type: 'POST',
				url: this.url()+'/duplicate',
				data: JSON.stringify(args.data),
				contentType: 'application/json; charset=utf-8',
				success: function(data){
					var duplicatedBaseline = new ProductBaseline(data);
					duplicatedBaseline.setConfigurationItemId(_this.getConfigurationItemId());
					_this.collection.add(duplicatedBaseline);
					args.success(duplicatedBaseline);
				},
				error: args.error
			});
		}
	});

	return ProductBaseline;
});
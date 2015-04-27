/*global define*/

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
        setBaselinedParts:function(baselinedParts){
            return this.set('baselinedParts',baselinedParts);
        },
		getConfigurationItemId: function(){
			return this.get('configurationItemId');
		},
		setConfigurationItemId: function(configurationItemId){
			this.set('configurationItemId',configurationItemId);
		},
        getSubstituteLinks:function(){
            return this.get('substituteLinks');
        },
        setSubstituteLinks:function(substituteLinks){
            this.set('substituteLinks',substituteLinks);
        },
        getOptionalUsageLinks:function(){
            return this.get('optionalUsageLinks');
        },
        setOptionalUsageLinks:function(optionalUsageLinks){
            this.set('optionalUsageLinks',optionalUsageLinks);
        },
        getAuthor:function(){
            return this.get('author').name;
        },
        getAuthorLogin: function () {
            return this.get('author').login;
        }
	});

	return ProductBaseline;
});

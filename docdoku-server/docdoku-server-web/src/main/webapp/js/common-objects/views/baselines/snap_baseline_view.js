'use strict';
define([
	'common-objects/collections/baselines',
    'text!common-objects/templates/baselines/snap_baseline_view.html',
    'i18n!localization/nls/baseline-strings'
], function (Baselines,template, i18n) {
    var SnapLatestBaselineView = Backbone.View.extend({
        events: {
            'submit #baseline_creation_form' : 'onSubmitForm',
            'hidden #baseline_creation_modal': 'onHidden'
        },

        template: Mustache.compile(template),

        initialize: function() {
            _.bindAll(this);
	        this.isProduct=false;
	        if(this.options && this.options.type) {
		        this.isProduct = this.options.type==='RELEASED' || this.options.type==='LATEST' || this.options.type==='PRODUCT';
	        }else if(!this.collection){
				this.collection =new Baselines({},{type:'document'});
	        }
        },

        render: function() {
	        var data = {
		        i18n: i18n,
		        isProduct: this.isProduct
	        };
	        if(this.isProduct){
		        data.isReleased=this.options.type==='RELEASED';
		        data.isLatest=this.options.type==='LATEST';
	        }
            this.$el.html(this.template(data));
            this.bindDomElements();
            if(this.isProduct){
                this.$inputBaselineType.val(this.options.type);
            }
            return this;
        },

        bindDomElements:function(){
            this.$modal = this.$('#baseline_creation_modal');
            this.$inputBaselineName = this.$('#inputBaselineName');
            this.$inputBaselineDescription = this.$('#inputBaselineDescription');
	        if(this.isProduct) {
		        this.$inputBaselineType = this.$('#inputBaselineType');
	        }
        },

        onSubmitForm: function(e) {
	        var data = {
		        name:this.$inputBaselineName.val(),
		        description:this.$inputBaselineDescription.val()
	        };
	        var callbacks = {
		        success: this.onBaselineCreated,
		        error: this.onError
	        };
	        if(this.isProduct){
		        data.type=this.$inputBaselineType.val();
		        this.model.createBaseline(data,callbacks);
	        }else{
				var baselinesCollection = this.collection;
		        baselinesCollection.create(data,callbacks);
	        }

            e.preventDefault();
            e.stopPropagation();
            return false ;
        },

        onBaselineCreated: function() {
            this.closeModal();
        },

        onError: function(error) {
            alert(i18n.CREATION_ERROR + ' : ' + error.responseText);
        },

        openModal: function() {
            this.$modal.modal('show');
        },

        closeModal: function() {
            this.$modal.modal('hide');
        },

        onHidden: function() {
            this.remove();
        }
    });

    return SnapLatestBaselineView;
});
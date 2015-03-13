/*global define,App*/
define([
	'backbone',
	'mustache',
	'common-objects/collections/baselines',
    'common-objects/collections/product_instances',
	'text!templates/baselines/baseline_select.html'
], function (Backbone, Mustache, Baselines, ProductInstances, template) {
	'use strict';

	var BaselineSelectView = Backbone.View.extend({
		events:{
			'change select' : 'onSelectorChanged',
			'click button.newBaseline':'createBaseline',
			'click button.deleteBaseline':'deleteBaseline'
		},

		initialize:function(){
			this.type = (this.options && this.options.type) ? this.options.type : 'product';
            if(!this.baselineCollection){
                this.baselineCollection = new Baselines({},{type : this.type,productId : App.config.productId});
                this.listenToOnce(this.baselineCollection,'reset',this.onBaselineCollectionReset);
            }
            if(!this.productInstanceCollection){
                this.productInstanceCollection = new ProductInstances({},{productId : App.config.productId});
                this.listenToOnce(this.productInstanceCollection,'reset',this.onProductInstanceCollectionReset);
            }
		},

		render:function(){
			this.$el.html(Mustache.render(template, {i18n:App.config.i18n}));
			this.bindDomElements();
			this.hideMenu();
            this.$selectBaselineSpec.hide();
            this.$selectProdInstSpec.hide();
			this.baselineCollection.fetch({reset:true});
            this.productInstanceCollection.fetch({reset:true});
			return this ;
		},

		bindDomElements:function(){
			this.$selectConfSpec = this.$('#config_spec_type_selector_list');
			this.$selectBaselineSpec = this.$('#baseline_selector_list');
			this.$selectProdInstSpec = this.$('#product_instance1_selector_list');
			this.$menu = this.$('.ConfigSpecSelector-menu');
			this.$newBaselineBtn = this.$('.btn.newBaseline');
			this.$deleteBaselineBtn = this.$('.btn.deleteBaseline');
			if(App.config.configSpec==='latest' || App.config.configSpec==='released'){
				this.$deleteBaselineBtn.attr('disabled', 'disabled');
				this.$deleteBaselineBtn.hide();
			}
		},

		onBaselineCollectionReset:function(){
			this.onBaselineCollectionChange();
			this.listenTo(this.baselineCollection,'change',this.onBaselineCollectionChange);
		},

		onBaselineCollectionChange:function(){
			var that = this ;
            this.onCollectionChange();
            if(this.$selectBaselineSpec) {
                this.$selectBaselineSpec.find('option').remove();
                this.$selectBaselineSpec.append('<option disabled>'+App.config.i18n.BASELINE+'</option>');
                this.baselineCollection.each(function(baseline){
                    that.$selectBaselineSpec.append('<option value="'+baseline.getId()+'">'+baseline.getName()+'</option>');
                });
            }
            this.selectCurrentBaseline();
		},

        onProductInstanceCollectionReset:function(){
            this.onProductInstanceCollectionChange();
            this.listenTo(this.productInstanceCollection,'change',this.onProductInstanceCollectionChange);
        },

        onProductInstanceCollectionChange:function(){
            var that = this ;
            this.onCollectionChange();
            if(this.$selectProdInstSpec) {
                this.$selectProdInstSpec.find('option').remove();
                this.$selectProdInstSpec.append('<option disabled>'+App.config.i18n.SERIAL_NUMBER+'</option>');
                this.productInstanceCollection.each(function(productInstance){
                    that.$selectProdInstSpec.append('<option value="pi-'+productInstance.getSerialNumber()+'">'+productInstance.getSerialNumber()+'</option>');
                });
            }
            this.selectCurrentBaseline();
        },

        onCollectionChange:function(){
            if(this.$selectConfSpec) {
                this.$selectConfSpec.find('option').remove();

                this.$selectConfSpec.append('<option value="latest">'+App.config.i18n.LATEST_SHORT+'</option>');
                if(this.baselineCollection && this.baselineCollection.length > 0){
                    this.$selectConfSpec.append('<option value="baseline">'+App.config.i18n.BASELINE+'</option>');
                }
                if(this.productInstanceCollection && this.productInstanceCollection.length > 0){
                    this.$selectConfSpec.append('<option value="serial-number">'+App.config.i18n.SERIAL_NUMBER+'</option>');
                }

                // Todo https://github.com/docdoku/docdoku-plm/issues/321
                //this.baselineCollection.getLastReleaseRevision({
                //    success: function (lastReleaseRevision) {
                //        var alreadyExist = false;
                //        that.$selectConfSpec.find('option').each(function(){
                //            if (this.value === 'released') {
                //                alreadyExist = true;
                //                return false;
                //            }
                //        });
                //        if (lastReleaseRevision && !alreadyExist) {
                //            that.$selectConfSpec.prepend('<option value="released">' + App.config.i18n.RELEASED_SHORT + '</option>');
                //        }
                //    }
                //});
            }
        },

		onSelectorChanged:function(e){
            this.$selectConfSpec.show();
			if(this.$selectConfSpec[0].value==='latest' || this.$selectConfSpec[0].value==='released'){
                this.$selectBaselineSpec.hide();
                this.$selectProdInstSpec.hide();
                this.trigger('config_spec:changed', e.target.value);
			}else if(this.$selectConfSpec[0].value==='baseline'){
                this.$selectBaselineSpec.show();
                this.$selectProdInstSpec.hide();
                this.trigger('config_spec:changed', this.$selectBaselineSpec[0].value);
			}else if(this.$selectConfSpec[0].value==='serial-number'){
                this.$selectBaselineSpec.hide();
                this.$selectProdInstSpec.show();
                this.trigger('config_spec:changed', this.$selectProdInstSpec[0].value);
            }
            //this.trigger('config_spec:changed', e.target.value);
		},

		createBaseline:function(){
            //Todo Allow to add baseline on product-structure
            //var snapBaselineView;
            //SnapBaselineView = new SnapBaselineView({type: 'PRODUCT', collection: this.baselineCollection});
            //$('body').append(snapBaselineView.render().el);
            //snapBaselineView.openModal();
		},

		deleteBaseline:function(){
			//Todo Allow to delete baseline on product-structure
			//var that = this;
            //bootbox.confirm(App.config.i18n.DELETE_SELECTION_QUESTION, function(result){
            //    if(result){
            //        that.baselineCollection.each(function(baseline){
            //            if(parseInt(that.$selectConfSpec.val(),10)===baseline.getId()){
            //                baseline.destroy({
            //                    dataType: 'text', // server doesn't send a json hash in the response body
            //                    success:function(){
            //                        that.$selectConfSpec.find('option[value='+baseline.getId()+']').remove();
            //                        that.$selectConfSpec.val('latest').change();
            //                    },
            //                    error:function(model,err){
            //                        alert(err.responseText);
            //                    }});
            //            }
            //        });
            //    }
            //});
		},

		showMenu: function(){
			this.$menu.show();
		},

		hideMenu: function(){
			this.$menu.hide();
		},

        selectCurrentBaseline:function(){
            if(App.config.configSpec){
                if(App.config.configSpec==='latest' || App.config.configSpec==='released'){
                    this.$selectConfSpec.val(App.config.configSpec);
                }else if(App.config.configSpec.indexOf('pi-')===0){
                    this.$selectProdInstSpec.val(App.config.configSpec);
                }else{
                    this.$selectBaselineSpec.val(App.config.configSpec);
                }
            }
        },

        refresh:function(){
            if(App.config.configSpec){
                if(App.config.configSpec==='latest' || App.config.configSpec==='released'){
                    this.$selectConfSpec.val(App.config.configSpec);
                    this.$selectBaselineSpec.hide();
                    this.$selectProdInstSpec.hide();
                }else if(App.config.configSpec.indexOf('pi-')===0){
                    this.$selectConfSpec.val('serial-number');
                    this.$selectProdInstSpec.val(App.config.configSpec).show();
                    this.$selectBaselineSpec.hide();
                }else{
                    this.$selectConfSpec.val('baseline');
                    this.$selectBaselineSpec.val(App.config.configSpec).show();
                    this.$selectProdInstSpec.hide();
                }
            }
        }

	});

	return BaselineSelectView;
});

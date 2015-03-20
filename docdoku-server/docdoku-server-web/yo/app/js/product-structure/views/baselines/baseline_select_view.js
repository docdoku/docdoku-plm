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
			'change #config_spec_type_selector_list' : 'onTypeChanged',
			'change #latest_selector_list' : 'changeLatest',
			'change #baseline_selector_list' : 'changeBaseline',
			'change #product_instance1_selector_list' : 'changeInstance'
		},

        availableFilters:['wip','latest','latest-released'],

        type:'product',

		initialize:function(){
            this.baselineCollection = new Baselines({},{type : this.type, productId : App.config.productId});
            this.listenToOnce(this.baselineCollection,'reset',this.onBaselineCollectionReset);
            this.productInstanceCollection = new ProductInstances({},{productId : App.config.productId});
            this.listenToOnce(this.productInstanceCollection,'reset',this.onProductInstanceCollectionReset);
		},

		render:function(){

			this.$el.html(Mustache.render(template, {i18n:App.config.i18n}));
			this.bindDomElements();

            this.$selectBaselineSpec.hide();
            this.$selectProdInstSpec.hide();

			this.baselineCollection.fetch({reset:true});
            this.productInstanceCollection.fetch({reset:true});

            if(_.contains(this.availableFilters,App.config.configSpec)){
                this.$selectLatestFilter.val(App.config.configSpec);
            }

			return this ;
		},

		bindDomElements:function(){
            // Main selector
			this.$selectConfSpec = this.$('#config_spec_type_selector_list');
            // Sub selectors
			this.$selectLatestFilter = this.$('#latest_selector_list');
			this.$selectBaselineSpec = this.$('#baseline_selector_list');
			this.$selectProdInstSpec = this.$('#product_instance1_selector_list');
		},

		onBaselineCollectionReset:function(){

            var selected;

            this.baselineCollection.each(function(baseline){
                this.$selectBaselineSpec.append('<option value="'+baseline.getId()+'">'+baseline.getName()+'</option>');
                if(App.config.configSpec === ''+baseline.getId()){
                    selected = baseline;
                }
            },this);

            this.$selectConfSpec.find('[value="baseline"]').prop('disabled',!this.baselineCollection.size());

            if(selected){
                this.$selectConfSpec.val('baseline');
                this.$selectProdInstSpec.hide();
                this.$selectLatestFilter.hide();
                this.$selectBaselineSpec.val(selected.getId()).show();
                this.setDescription(selected.getDescription());
            }
		},

        onProductInstanceCollectionReset:function(){
            var selected;
            this.productInstanceCollection.each(function(productInstance){
                this.$selectProdInstSpec.append('<option value="pi-'+productInstance.getSerialNumber()+'">'+productInstance.getSerialNumber()+'</option>');
                if(App.config.configSpec === 'pi-' + productInstance.getSerialNumber()){
                    selected = productInstance;
                }
            },this);

            this.$selectConfSpec.find('[value="serial-number"]').prop('disabled',!this.productInstanceCollection.size());

            if(selected){
                this.$selectConfSpec.val('serial-number');
                this.$selectBaselineSpec.hide();
                this.$selectLatestFilter.hide();
                this.$selectProdInstSpec.val(selected.getSerialNumber()).show();
                this.setDescription('');
            }
        },

        onTypeChanged:function(e){
            this.$selectConfSpec.show();

            var selectedType = this.$selectConfSpec.val();

			if(selectedType==='latest-filters'){
                this.changeLatest();
			}else if(selectedType==='baseline'){
                this.changeBaseline();
			}else if(selectedType==='serial-number'){
                this.changeInstance();
            }
		},

        changeLatest:function(){
            this.$selectBaselineSpec.hide();
            this.$selectProdInstSpec.hide();
            this.$selectLatestFilter.show();
            this.trigger('config_spec:changed', this.$selectLatestFilter.val());
            this.setDescription('');
        },
        changeBaseline:function(){
            this.$selectProdInstSpec.hide();
            this.$selectLatestFilter.hide();
            this.$selectBaselineSpec.show();
            this.trigger('config_spec:changed', this.$selectBaselineSpec.val());
            var baseline = this.baselineCollection.findWhere({id:parseInt(this.$selectBaselineSpec.val(),10)});
            this.setDescription(baseline ? baseline.getDescription() : '');
        },
        changeInstance:function(){
            this.$selectBaselineSpec.hide();
            this.$selectLatestFilter.hide();
            this.$selectProdInstSpec.show();
            this.trigger('config_spec:changed', this.$selectProdInstSpec.val());
            this.setDescription('');
        },

        setDescription:function(desc){
            this.$('.description').text(desc);
        }

	});

	return BaselineSelectView;
});

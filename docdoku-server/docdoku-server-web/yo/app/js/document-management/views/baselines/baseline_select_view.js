/*global define,bootbox,App,window*/
define([
	'backbone',
	'mustache',
	'common-objects/collections/baselines',
	'text!templates/baselines/baseline_select.html',
	'views/baselines/baseline_creation_view'
], function (Backbone, Mustache, Baselines, template, BaselineCreationView) {
	'use strict';

	var BaselineSelectView = Backbone.View.extend({
		events:{
			'change select' : 'onSelectorChanged',
			'click button.newBaseline':'createBaseline',
			'click button.deleteBaseline':'deleteBaseline'
		},

		initialize:function(){
			this.type = 'document';
			if(!this.collection){
				var data = {
					type : this.type
				};
				this.collection = new Baselines({},data);
			}
			this.listenToOnce(this.collection,'reset',this.onCollectionReset);
		},

		render:function(){
			this.$el.html(Mustache.render(template, {i18n:App.config.i18n}));
			this.bindDomElements();
			this.hideMenu();
			this.collection.fetch({reset:true});
			return this ;
		},

		bindDomElements:function(){
			this.$select = this.$('select');
			this.$menu = this.$('.ConfigSpecSelector-menu');
			this.$newBaselineBtn = this.$('.btn.newBaseline');
			this.$deleteBaselineBtn = this.$('.btn.deleteBaseline');
			if(App.config.documentConfigSpec==='latest' || App.config.documentConfigSpec==='released'){
				this.$deleteBaselineBtn.attr('disabled', 'disabled');
				this.$deleteBaselineBtn.hide();
			}
		},

		onCollectionReset:function(){
			this.onCollectionChange();
			this.listenTo(this.collection,'change',this.onCollectionChange);
		},

		onCollectionChange:function(){
			var that = this ;
            if(this.$select){
                this.$select.find('option').remove();

                this.collection.getLastReleaseRevision({
                    success : function (lastReleaseRevision){
                        if(lastReleaseRevision){
                            that.$select.prepend('<option value="released">'+App.config.i18n.RELEASED_SHORT+'</option>');
                        }
                    }
                });

                this.$select.append('<option value="latest">'+App.config.i18n.LATEST_SHORT+'</option>');
                this.collection.each(function(baseline){
                    that.$select.append('<option value="'+baseline.getId()+'">'+baseline.getName()+'</option>');
                });
            }
            if(App.config.documentConfigSpec){
                this.$select.val(App.config.documentConfigSpec);
            }
		},

		onSelectorChanged:function(e){
			this.trigger('config_spec:changed', e.target.value);
			if(e.target.value==='latest' || e.target.value==='released'){
				this.$deleteBaselineBtn.attr('disabled', 'disabled');
				this.$newBaselineBtn.removeAttr('disabled');
				this.$deleteBaselineBtn.hide();
			}else{
				this.$deleteBaselineBtn.show();
				this.$deleteBaselineBtn.removeAttr('disabled');
				this.$newBaselineBtn.attr('disabled', 'disabled');
			}
		},

		createBaseline:function(){
			var baselineCreationView = new BaselineCreationView({collection: this.collection});
            window.document.body.appendChild(baselineCreationView.render().el);
            baselineCreationView.openModal();
		},

		deleteBaseline:function(){
			var that = this;

            bootbox.confirm(App.config.i18n.DELETE_SELECTION_QUESTION, function(result){
                if(result){
                    that.collection.each(function(baseline){
                        if(parseInt(that.$select.val(),10)===baseline.getId()){
                            baseline.destroy({
                                dataType: 'text', // server doesn't send a json hash in the response body
                                success:function(){
                                    that.$select.find('option[value='+baseline.getId()+']').remove();
                                    that.$select.val('latest').change();
                                },
                                error:function(model,err){
                                    window.alert(err.responseText);
                                }
                            });
                        }
                    });
                }
            });
		},

		showMenu: function(){
			this.$menu.show();
		},

		hideMenu: function(){
			this.$menu.hide();
		}

	});

	return BaselineSelectView;
});

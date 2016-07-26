/*global define,App*/
define([
    'backbone',
    'mustache',
    'common-objects/models/workspace',
	'common-objects/collections/baselines',
	'views/baselines/baseline_select_view',
    'text!templates/content.html'
], function (Backbone, Mustache, Workspace, Baselines, BaselineSelectView, template) {
	'use strict';
    var AppView = Backbone.View.extend({

        el: '#content',

        events: {
	        'click button.newBaseline':'createBaseline'
        },

        initialize: function () {
	        App.config.documentConfigSpec = 'latest';
            this.model = new Workspace({id: App.config.workspaceId});
        },

        render: function () {

            this.$el.html(Mustache.render(template, {model: this.model, i18n: App.config.i18n}));
            App.$documentManagementMenu = this.$('#document-management-menu');
            App.$documentManagementContent = this.$('#document-management-content');

            this.bindDomElements();
	        this.renderSubView();

            App.$documentManagementMenu.customResizable({
                containment: this.$el
            });
	        this.listenEvents();
            this.$el.show();
            return this;
        },

        bindDomElements:function(){
            this.$linksNav = this.$('.nav-header.links-nav');
        },

	    renderSubView:function(){
		    this.baselinesCollection = new Baselines({},{type:'document'});
		    App.baselineSelectView = new BaselineSelectView({el:'#config_spec_container',type: 'document', collection: this.baselinesCollection}).render();
            App.baselineSelectView.showMenu();
		},

	    listenEvents:function(){
            App.baselineSelectView.on('config_spec:changed', this.onConfigSpecChange,this);
	    },

	    onConfigSpecChange:function(configSpec){
		    App.router.navigate(App.config.workspaceId+'/configspec/'+configSpec+'/folders', {trigger:true, replace:false});
		},

	    isReadOnly:function(){
			return App.config.documentConfigSpec!=='latest';
	    }
    });

    return AppView;
});

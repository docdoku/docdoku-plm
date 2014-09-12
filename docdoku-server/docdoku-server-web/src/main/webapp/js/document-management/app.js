/*global APP_CONFIG*/
'use strict';
define([
    'common-objects/models/workspace',
	'common-objects/views/baselines/baseline_select_view',
    'text!templates/content.html',
    'i18n!localization/nls/document-management-strings',
	'common-objects/collections/baselines'
], function (Workspace, BaselineSelectView, template, i18n, Baselines) {
    var AppView = Backbone.View.extend({

        el: $("#content"),

        events: {
	        'click button.newBaseline':'createBaseline'
        },

        template:Mustache.compile(template),

        initialize: function() {
	        APP_CONFIG.configSpec = 'latest';
            this.model = new Workspace({id: APP_CONFIG.workspaceId});
        },

        render:function(){
            this.$el.html(
                this.template({model: this.model,
                               i18n:i18n})
            );
            this.bindDomElements();
	        this.renderSubView();
            this.$documentManagementMenu.customResizable({
                containment: this.$el
            });
	        this.listenEvents();
            return this;
        },

        bindDomElements:function(){
            this.$documentManagementMenu = this.$('#document-management-menu');
        },

	    renderSubView:function(){
		    this.baselinesCollection = new Baselines({},{type:'document'});
		    this.baselineSelectView = new BaselineSelectView({el:'#config_spec_container',type: 'document', collection: this.baselinesCollection}).render();
		    this.baselineSelectView.showMenu();
	    },

	    listenEvents:function(){
		    this.baselineSelectView.on('config_spec:changed', this.onConfigSpecChange,this);
	    },

	    onConfigSpecChange:function(configSpec){
		    require('router').getInstance().navigate('configspec/'+configSpec+'/folders', {trigger:true, replace:true});
	    },

	    isReadOnly:function(){
			return APP_CONFIG.configSpec!=='latest';
	    }

    });

    return AppView;
});
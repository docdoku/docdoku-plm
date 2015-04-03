/*global _,define,App*/
define([
    'backbone',
    'mustache',
    'text!templates/baselines/baseline_configuration_list.html',
    'models/configuration'
], function (Backbone, Mustache, template,Configuration) {
	'use strict';
    var BaselineConfigurationsView = Backbone.View.extend({

        tagName: 'div',

        events:{
            'change select':'onConfigurationChanged'
        },

        initialize: function () {
            _.bindAll(this);
        },

        render: function () {
            this.$el.html(Mustache.render(template, {i18n: App.config.i18n}));
            this.$select = this.$('select');
            return this;
        },

        renderList:function(configurations){
            this.configurations = configurations;
            this.$select.html('<option>'+App.config.i18n.NONE+'</option>');
            _.each(configurations,this.addOption,this);
        },

        addOption:function(data){
            var configuration = new Configuration(data);
            this.$select.append('<option value="'+configuration.getId()+'">'+configuration.getName()+'</option>');
        },

        onConfigurationChanged:function(){
            this.trigger('configuration:changed', _.findWhere(this.configurations,{id:parseInt(this.$select.val(),10)}));
        }

    });

    return BaselineConfigurationsView;
});

/*global _,define,App*/
define([
    'backbone',
    'mustache',
    'text!templates/baselines/baseline_choice_list.html',
    'views/baselines/baseline_choice_list_item',
    'models/path_choice'
], function (Backbone, Mustache, template, BaselineChoiceItemView, PathChoice) {
	'use strict';
    var BaselineChoicesView = Backbone.View.extend({

        tagName: 'div',

        className: 'choices-list',

        initialize: function () {
            _.bindAll(this);
            this.choices = [];
            this.choicesViews = [];
        },

        getChoices:function(){
            var choices = [];
            _.each(this.choicesViews,function(view){
                if(view.model.retained){
                    choices.push(view.getChoice());
                }
            },this);
            return choices;
        },

        render: function () {
            this.$el.html(Mustache.render(template, {i18n: App.config.i18n}));
            this.$list = this.$('.choices');
            return this;
        },

        renderList:function(choices){
            this.clear();

            this.choices  = choices.map(function(choice){
                return new PathChoice(choice);
            });

            this.choices.sort(function(a,b){
                return a.getKey() < b.getKey() ? -1:1;
            });

            _.each(this.choices,this.addChoiceItemView,this);
        },

        addChoiceItemView:function(choice){
            var view = new BaselineChoiceItemView({model:choice, removable:this.options.removableItems}).render();
            this.choicesViews.push(view);
            this.$list.append(view.$el);
        },

        clear:function(){
            this.choices = [];
            this.choicesViews = [];
            this.removeSubviews();
            this.$list.empty();
        },

        removeSubviews: function () {
            _(this.choicesViews).invoke('remove');
        },

        updateFromConfiguration:function(configuration){
            _.invoke(this.choicesViews,'resetNominal');
            if(configuration){
                _.each(configuration.substituteLinks,this.checkLink);
                _.each(configuration.optionalUsageLinks,this.checkLink);
            }
        },

        checkLink:function(link){
            this.$('[data-path="'+link+'"]').click();
        }

    });

    return BaselineChoicesView;
});

/*global _,define,App*/
define([
    'backbone',
    'mustache',
    'text!templates/baselines/baseline_choice_list.html',
    'views/baselines/baseline_choice_list_item',
    'models/path_choice',
], function (Backbone, Mustache, template, BaselineChoiceItemView, PathChoice) {
	'use strict';
    var BaselineChoicesView = Backbone.View.extend({

        tagName: 'div',

        className: 'baseline-choices-list',

        initialize: function () {
            _.bindAll(this);
            this.choices = [];
            this.choicesViews = [];
        },

        getChoices:function(){
            var choices = [];
            _.each(this.choicesViews,function(view){
                choices.push(view.getChoice());
            },this);
            return choices;
        },

        render: function () {
            this.$el.html(Mustache.render(template, {i18n: App.config.i18n}));
            this.$list = this.$('.baseline-choices');

            return this;
        },

        renderList:function(choices){
            this.choicesViews = [];
            this.choices  = choices;
            this.choices.sort(function(a,b){
                return a.partRevisionsKeys.join() < b.partRevisionsKeys.join() ? -1:1;
            });
            _.each(this.choices,this.addChoiceItemView,this);
        },

        addChoiceItemView:function(choice){
            var view = new BaselineChoiceItemView({model:new PathChoice(choice)}).render();
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
            _(this.baselinedPartsViews).invoke('remove');
        }

    });

    return BaselineChoicesView;
});

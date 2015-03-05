define([
    'backbone',
    'mustache',
    'text!common-objects/templates/lov/lov_item.html',
    'text!common-objects/templates/lov/lov_possible_value.html',
], function (Backbone, Mustache, template, itemTemplate) {
    'use strict';
    var LOVItemView = Backbone.View.extend({

        events:{
            'click .deleteLovItem': 'removeItem',
            'click .addLOVValue': 'addValueInList',
            'click .expandIcon': 'showEditMode',
            'click .deleteLovItemPossibleValue': 'deletePossibleValue'
        },

        className: 'lovItem ui-sortable well',

        isExpand:false,

        lovListDiv : '',

        initialize: function () {
            this.isExpand = this.options.isExpand;
        },

        render: function () {
            this.$el.html(Mustache.render(template,{
                i18n: App.config.i18n,
                model: this.model,
                isExpand: this.isExpand
            }));
            if(this.isExpand){
                this.$el.addClass('edition');
            }
            this.lovListDiv = this.$('.lovValues');

            _.each(this.model.getLOVValues(), this.addPossibleValueView.bind(this));

            //Reorganise possible value order
            var oldIndex = null;
            var that = this;
            this.lovListDiv.sortable({
                handle: ".sortable-handler",
                placeholder: "list-item well highlight",
                start: function(event, ui){
                    oldIndex = ui.item.index();
                },
                stop: function(event, ui) {
                    var newIndex = ui.item.index();
                    that.model.getLOVValues().move(oldIndex, newIndex);
                    oldIndex = null;
                }
            });

            return this;
        },

        removeItem:function(){
            this.trigger('remove');
            this.remove();
        },

        deletePossibleValue:function(event){
            var possibleValueView = event.currentTarget.parentElement;
            var indexOfPossibleValue = $(possibleValueView).index();
            possibleValueView.remove();
            this.model.getLOVValues().splice(indexOfPossibleValue, 1);
            debugger;
        },

        addPossibleValueView:function(possibleValue){
            this.lovListDiv.append(Mustache.render(itemTemplate,{
                i18n: App.config.i18n,
                possibleValue:possibleValue
            }));
        },

        addValueInList:function(){
            var newPossibleValue = {name:"", value:""};
            this.model.getLOVValues().push(newPossibleValue);
            this.addPossibleValueView(newPossibleValue);
        },

        showEditMode:function(){
            this.isExpand = !this.isExpand;
            this.$el.toggleClass('edition');
        }

    });

    return LOVItemView;
});

/*global define,App,_*/
define([
    'backbone',
    'mustache',
    'text!common-objects/templates/lov/lov_item.html',
    'common-objects/views/lov/lov_possible_value'
], function (Backbone, Mustache, template, LOVPossibleValueView) {
    'use strict';
    var LOVItemView = Backbone.View.extend({

        events:{
            'click .deleteLovItem': 'removeItem',
            'click .addLOVValue': 'addValueInList',
            'click .expandIcon': 'showEditMode',
            'blur .lovItemNameInput': 'onItemNameChanged'
        },

        className: 'lovItem ui-sortable well',

        isExpand:false,

        $lovListDiv : '',

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
            if(this.model.getLOVName()){
                this.$el.addClass('isOldItem');
            }
            this.$lovListDiv = this.$('.lovValues');

            _.each(this.model.getLOVValues(), this.addPossibleValueView.bind(this));

            //Reorganise possible value order
            var oldIndex = null;
            var that = this;
            this.$lovListDiv.sortable({
                handle: '.sortable-handler',
                placeholder: 'list-item well highlight',
                start: function(event, ui){
                    oldIndex = ui.item.index();
                },
                stop: function(event, ui) {
                    var newIndex = ui.item.index();
                    that.arrayMove(that.model.getLOVValues(),oldIndex, newIndex);
                    oldIndex = null;
                }
            });

            this.$('input[required]').customValidity(App.config.i18n.REQUIRED_FIELD);

            return this;
        },

        removeItem:function(){
            this.trigger('remove');
            this.remove();
        },

        deletePossibleValue:function(possibleValueView){
            var indexInModel = _.indexOf(this.model.getLOVValues(),possibleValueView.model);
            this.model.getLOVValues().splice(indexInModel, 1);
            this.onNumberOfPossibleValueChanged();
        },

        addPossibleValueView:function(possibleValue){
            var possibleValueView = new LOVPossibleValueView({
                model:possibleValue
            });

            possibleValueView.on('remove', this.deletePossibleValue.bind(this, possibleValueView));
            possibleValueView.render();
            this.$lovListDiv.append(possibleValueView.$el);
            this.onNumberOfPossibleValueChanged();
        },

        addValueInList:function(){
            var newPossibleValue = {name:'', value:''};
            this.model.getLOVValues().push(newPossibleValue);
            this.addPossibleValueView(newPossibleValue);
        },

        showEditMode:function(){
            this.isExpand = !this.isExpand;
            this.$el.toggleClass('edition');
        },

        onNumberOfPossibleValueChanged:function(){
            this.$('.lovNumberOfValue').html(this.model.getNumberOfValue());
        },

        onItemNameChanged: function(){
            var newName = this.$('.lovItemNameInput').val();
            this.$('.lovItemName').html(newName);
            this.model.setLOVName(newName);
        },

        arrayMove: function (array, oldIndex, newIndex) {
            array.splice(newIndex, 0, array.splice(oldIndex, 1)[0]);
        }

    });

    return LOVItemView;
});

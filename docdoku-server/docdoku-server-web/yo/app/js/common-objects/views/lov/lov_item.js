define([
    'backbone',
    'mustache',
    'text!common-objects/templates/lov/lov_item.html'
], function (Backbone, Mustache, template) {
    'use strict';
    var LOVItemView = Backbone.View.extend({

        events:{
            'click .deleteLovItem': 'removeItem',
            'click .addLOVValue': 'addValueInList',
            'click .expandIcon': 'showEditMode',
            'drop': 'drop'
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
            return this;
        },

        drop: function(event, index) {
            this.$el.trigger('update-sort', [this.model, index]);
        },

        removeItem:function(){
            this.remove();
        },

        addValueInList:function(){

        },

        showEditMode:function(){
            this.$el.toggleClass('edition');
        }
    });

    return LOVItemView;
});

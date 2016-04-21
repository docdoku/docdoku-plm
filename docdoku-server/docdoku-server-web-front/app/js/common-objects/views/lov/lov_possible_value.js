/*global define,App*/
define([
    'backbone',
    'mustache',
    'text!common-objects/templates/lov/lov_possible_value.html'
], function (Backbone, Mustache, template){
    'use strict';
    var LOVPossibleValueView = Backbone.View.extend({

        events:{
            'blur .lovItemNameValueNameInput' : 'onNameChanged',
            'blur .lovItemNameValueValueInput' : 'onValueChanged',
            'click .deleteLovItemPossibleValue': 'onDeleteView'
        },

        className : 'lovPossibleValue',

        nameInput: null,

        valueInput: null,

        initialize: function () {

        },

        render: function () {
            this.$el.html(Mustache.render(template, {
                i18n: App.config.i18n,
                possibleValue: this.model
            }));

            this.nameInput = this.$('.lovItemNameValueNameInput');
            this.valueInput = this.$('.lovItemNameValueValueInput');
            this.$('input[required]').customValidity(App.config.i18n.REQUIRED_FIELD);
            return this;
        },

        onNameChanged: function(){
            this.model.name = this.nameInput.val();
        },

        onValueChanged: function(){
            this.model.value = this.valueInput.val();
        },

        onDeleteView: function(){
            this.trigger('remove');
            this.remove();
        }

    });

    return LOVPossibleValueView;
});

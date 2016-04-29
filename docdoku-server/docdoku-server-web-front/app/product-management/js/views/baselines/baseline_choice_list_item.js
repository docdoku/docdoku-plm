/*global _,define,App*/
define([
    'backbone',
    'mustache',
    'text!templates/baselines/baseline_choice_list_item.html',
    'common-objects/views/part/part_modal_view',
    'common-objects/models/part'
], function (Backbone, Mustache, template, PartModalView, Part) {
    'use strict';
    var BaselineChoiceListItemView = Backbone.View.extend({

        tagName: 'div',

        className: 'control-group',

        events: {
            'change input[type=radio]': 'changeChoice',
            'change .baseline-choice-optional': 'changeOptional',
            'change .choice-retain': 'toggleRetain',
            'click [data-part-key]':'openPartView'
        },

        template: Mustache.parse(template),

        initialize: function () {
            _.bindAll(this);
            this.model.retained = !this.options.removable;
        },

        render: function () {
            this.$el.html(Mustache.render(template, {
                model: this.model,
                i18n: App.config.i18n,
                removable:this.options.removable
            }));

            this.$nominalLink = this.$('.nominal-link');
            this.$nominalLink.prop('checked',true);

            this.optional = false;
            this.choice = this.$nominalLink.val();
            this.defaultChoice = this.choice;

            this.$('.fa-long-arrow-right').last().remove();

            if(this.options.removable){
                this.$el.toggleClass('not-retained', !this.model.retained);
            }

            return this;
        },
        getChoice:function(){
            if(this.choice === this.defaultChoice){
                return null;
            }
            if(this.optional){
                return {optional:true,path:this.model.getResolvedPathAsString()+'-'+this.defaultChoice};
            }
            return {path:this.model.getResolvedPathAsString()+'-'+this.choice};
        },
        changeChoice:function(e){
            this.choice = e.target.value;

            if(this.options.removable){
                this.model.retained = this.choice!==this.defaultChoice;
                this.$el.toggleClass('not-retained', !this.model.retained);
            }
        },
        changeOptional:function(e){
            this.optional = e.target.checked;
            this.$el.toggleClass('optional',e.target.checked);
            var radios = this.$('input[type=radio]');
            if(e.target.checked){
                radios.prop('disabled',true);
                radios.prop('checked',false);
                this.choice = null;
            }else{
                radios.prop('disabled',false);
                radios.first().prop('checked',true);
                this.choice = radios.first().val();
            }

            if(this.options.removable){
                this.model.retained = this.choice!==this.defaultChoice;
                this.$el.toggleClass('not-retained', !this.model.retained);
            }
        },
        resetNominal:function(){
            var optionalCheckbox = this.$('.baseline-choice-optional');
            if(optionalCheckbox.is(':checked')){
                optionalCheckbox.click();
            }
            this.$nominalLink.click();
        },

        openPartView:function(e){
            this.$el.trigger('close-modal-request');
            setTimeout(function(){
                var part = new Part({partKey: e.target.dataset.partKey});
                part.fetch().success(function () {
                    var partModalView = new PartModalView({
                        model: part
                    });
                    partModalView.show();
                });
            },500);
        }
    });

    return BaselineChoiceListItemView;
});

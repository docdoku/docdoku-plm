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
            'change input[type=checkbox]': 'changeOptional',
            'click .model-ref':'openPartView'
        },

        template: Mustache.parse(template),

        initialize: function () {
            _.bindAll(this);
        },

        render: function () {
            this.$el.html(Mustache.render(template, {
                model: this.model,
                i18n: App.config.i18n
            }));

            var radio = this.$('input[type=radio]').first();
            radio.prop('checked',true);
            this.optional = false;
            this.choice = radio.val();
            this.defaultChoice = this.choice;

            this.$('.fa-chevron-right').last().remove();

            return this;
        },
        getChoice:function(){
            if(this.choice === this.defaultChoice){
                return null;
            }
            if(this.optional){
                return {optional:true,path:this.model.getPath()}
            }

            var paths = this.model.getPaths();
            paths.pop();
            paths.push(this.choice);
            return {path:paths.join('-')};
        },
        changeChoice:function(e){
            this.choice = e.target.value;
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

/*global define,App*/
define([
    'backbone',
    'mustache',
    'text!common-objects/templates/part/part_effectivity.html'
], function (Backbone, Mustache, template) {

    'use strict';

    var PartEffectivityView = Backbone.View.extend({

        initialize: function () {
            this.selectedPartUsageLinkView = null;
            this.effectivities = null;

            this.model.getEffectivities().then(function(data) {
                this.effectivities = data;
            });
        },

        render:function(){
            this.$el.html(Mustache.render(template, {
                i18n: App.config.i18n,
                editMode: this.options.editMode,
                handleSubstitutes:this.handleSubstitutes,
                model:this.model.attributes,
                component:this.component,
                effectivities:this.effectivities
            }));

            return this;
        }

    });

    return PartEffectivityView;

});

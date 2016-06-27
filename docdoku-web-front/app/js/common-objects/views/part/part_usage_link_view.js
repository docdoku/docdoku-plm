/*global define,App*/
define([
    'backbone',
    'common-objects/views/part/part_link_view',
    'common-objects/views/part/part_substitute_link_view'
], function (Backbone, PartLinkView, PartSubstituteLinkView) {

    'use strict';

    var PartUsageLinkView = PartLinkView.extend({

        className:'component part-usage-link',

        handleSubstitutes:true,

        initialize:function(){
            PartLinkView.prototype.initialize.apply(this, arguments);
            this.substitutes = new Backbone.Collection(this.model.get('substitutes'));
            this.substitutes.bind('add',this.addPartSubstituteLink.bind(this));
            this.substitutes.bind('remove',this.removePartSubstituteLink.bind(this));
            this.events['click >.part-substitute-links-count']='toggleSubstitutes';
        },

        render:function(){
            PartLinkView.prototype.render.apply(this, arguments);
            this.initSubstituteViews();
            this.updateSubstitutesCount();
            return this;
        },

        getComponent:function(){
            return this.model.get('component');
        },

        initSubstituteViews:function(){
            this.substitutes.each(this.addPartSubstituteLinkView.bind(this));
        },

        addPartSubstituteLink:function(partSubstituteLink){
            this.addPartSubstituteLinkView(partSubstituteLink);
            this.updateSubstitutes();
            this.onModelChanged();
            this.$el.addClass('substitutes-opened');
        },

        addPartSubstituteLinkView:function(partSubstituteLink){
            var partSubstituteLinkView = new PartSubstituteLinkView({
                model:partSubstituteLink,
                editMode:this.options.editMode
            }).render();

            this.$('.part-substitute-links').append(partSubstituteLinkView.$el);
            this.listenTo(partSubstituteLink,'change',this.updateSubstitutes.bind(this));
        },

        removePartSubstituteLink : function(){
            this.updateSubstitutes();
            this.onModelChanged();
        },

        updateSubstitutes:function(){
            this.model.set('substitutes',this.substitutes.models);
            this.updateSubstitutesCount();
        },

        updateSubstitutesCount:function(){
            var substitutesCount = this.substitutes.size();
            this.$('.part-substitute-links-count').text(substitutesCount + ' ' + (substitutesCount <= 1 ? App.config.i18n.PART_SUBSTITUTE : App.config.i18n.PARTS_SUBSTITUTES));
        },

        toggleSubstitutes:function(){
            this.$el.toggleClass('substitutes-opened');
        }

    });

    return PartUsageLinkView;
});

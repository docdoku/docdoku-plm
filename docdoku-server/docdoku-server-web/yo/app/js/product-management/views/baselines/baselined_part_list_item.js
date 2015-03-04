/*global _,define,App*/
define([
    'backbone',
    'mustache',
    'text!templates/baselines/baselined_part_list_item.html',
    'common-objects/models/part'
], function (Backbone, Mustache, template, Part) {
    'use strict';
    var BaselinedPartListItemView = Backbone.View.extend({
        tagName: 'li',

        className: 'baselined-part-item',

        events: {
            'change input[type=radio]': 'changeChoice',
            'click .release': 'release'
        },

        template: Mustache.parse(template),

        initialize: function () {
            _.bindAll(this);
            this.editMode = (this.options.editMode) ? this.options.editMode : false;
            this.availableIterations = _(this.model.getAvailableIterations());
        },

        render: function () {
            this.$el.html(Mustache.render(template, {
                model: this.model,
                i18n: App.config.i18n,
                editMode:this.editMode
            }));
            this.$loader = this.$('.loader');
            this.$loader.hide();
            this.$release = this.$('.release');
            this.checkFirstInput();
            return this;
        },

        checkFirstInput:function(){
            this.$('input[type=radio]').first().prop('checked',true)
        },

        changeChoice:function(e){
            if (e.target.value) {
                var versionIteration = e.target.value.split('-');
                var version = versionIteration[0];
                var iteration = versionIteration[1];
                this.model.setVersion(version);
                this.model.setIteration(iteration);
            }
        },

        release:function(){
            this.$loader.show();
            this.$release.hide();
            var part = new Part({partKey: this.model.getNumber()+'-'+this.model.getVersion()});
            part.release().success(this.onReleased).error(this.onReleaseError);
        },
        onReleased:function(){
            this.$loader.hide();
            _.findWhere(this.model.getAvailableIterations(),{version:this.model.getVersion()}).released = true;
            this.render();
        },
        onReleaseError:function(){
            this.$loader.hide();
            this.$release.show();
        }
    });

    return BaselinedPartListItemView;
});

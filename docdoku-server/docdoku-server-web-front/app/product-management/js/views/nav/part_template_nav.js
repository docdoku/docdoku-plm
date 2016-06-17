/*global define,App*/
define([
    'backbone',
    'mustache',
    'common-objects/common/singleton_decorator',
    'text!templates/nav/part_template_nav.html',
    'views/part-template/part_template_content'
], function (Backbone, Mustache, singletonDecorator, template, PartTemplateContentView) {
    'use strict';
    var PartTemplateNavView = Backbone.View.extend({
        el: '#part-template-nav',

        initialize: function () {
            this.render();
        },

        render: function () {
            this.$el.html(Mustache.render(template, {i18n: App.config.i18n, workspaceId: App.config.workspaceId}));
        },

        setActive: function () {
            if (App.$productManagementMenu) {
                App.$productManagementMenu.find('.active').removeClass('active');
            }
            this.$el.find('.nav-list-entry').first().addClass('active');
        },

        showContent: function () {
            this.setActive();
            this.cleanView();
            if(!this.partTemplateContentView){
                this.partTemplateContentView = new PartTemplateContentView();
            }
            this.partTemplateContentView.render();
            App.$productManagementContent.html(this.partTemplateContentView.el);
        },

        cleanView: function () {
            if (this.partTemplateContentView) {
                this.partTemplateContentView.undelegateEvents();
                App.$productManagementContent.html('');
            }
        }

    });

    PartTemplateNavView = singletonDecorator(PartTemplateNavView);
    return PartTemplateNavView;

});

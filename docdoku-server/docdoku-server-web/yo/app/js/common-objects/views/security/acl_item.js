/*global define,App,_*/
define([
    'backbone',
    'mustache',
    'text!common-objects/templates/security/acl_entry_item.html'
], function (Backbone, Mustache, template) {

    'use strict';

    var ACLItemView = Backbone.View.extend({

        events: {
            'change input[type=radio]': 'change'
        },

        initialize: function () {
            _.bindAll(this);
        },

        change: function (e) {
            this.model.set('permission', e.target.value);
        },

        render: function () {
            var permission = App.config.i18n[this.model.getPermission()];
            this.$el.html(Mustache.render(template, {acl: this.model, i18n: App.config.i18n, editMode: this.options.editMode, permission: permission, radioName: this.model.key() + '-radio-' + this.cid}));
            return this;
        }

    });

    return ACLItemView;
});

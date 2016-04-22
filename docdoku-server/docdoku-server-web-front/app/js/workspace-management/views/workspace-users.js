/*global define,App*/
define([
    'backbone',
    'mustache',
    'text!templates/workspace-users.html',
    'common-objects/models/workspace'
], function (Backbone, Mustache, template, Workspace) {
    'use strict';

    var WorkspaceUsersView = Backbone.View.extend({

        events: {
            'click .read-only':'readOnly',
            'change .toggle-checkboxes':'toggleCheckboxes',
            'change .toggle-checkbox':'toggleCheckbox'
        },

        initialize: function () {
        },

        render: function () {
            var _this = this;
            Workspace.getUsersMemberships(App.config.workspaceId).then(function(memberships){
                _this.memberships = memberships;
                _this.$el.html(Mustache.render(template, {
                    i18n: App.config.i18n,
                    memberships:memberships
                }));
            });
            return this;
        },

        toggleCheckbox:function(e){
            console.log('change ' + e.target)
        },

        toggleCheckboxes:function(e){
            this.$('tbody > tr > td:nth-child(1) > input[type="checkbox"]').prop('checked', e.target.checked).trigger('change');
        }

    });

    return WorkspaceUsersView;
});

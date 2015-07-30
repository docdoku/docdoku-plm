/*global _,define,App*/
define([
    'backbone',
    'common-objects/collections/reachable_users',
    'modules/coworkers-access-module/views/coworkers_item_view'
], function (Backbone, Users, CoWorkersItemView) {
	'use strict';
    var CoWorkersAccessModuleView = Backbone.View.extend({

        el: '#coworkers_access_module',

        initialize: function () {
            var that = this;
            var users = new Users();
            this._coworkersItemViews = [];

            var $ul = this.$('#coworkers_access_module_entries');

            users.fetch({reset: true, success: function () {
                _.each(users.models, function (user) {
                    if (user.attributes.login !== App.config.login) {
                        var cwiv = new CoWorkersItemView({
                            model: user.attributes
                        });

                        that._coworkersItemViews.push(cwiv);

                        $ul.append(cwiv.render().el);
                    }
                });
            }});

            this.$el.show();

            this.$('#coworkers_access_module_toggler').click(function () {
                if (!$ul.is(':visible')) {
                    that.refreshAvailabilities();
                }
            });

            Backbone.Events.on('collaboration:invite', this.collaborativeInvite, this);

            return this;
        },

        refreshAvailabilities: function () {
            _.each(this._coworkersItemViews, function (view) {
                view.refreshAvailability();
            });
        },

        render: function () {
            return this;
        },

        collaborativeInvite: function () {
            this.$('#coworkers_access_module_toggler').click();
            this.$('.fa-globe').removeClass('corworker-action-disable').addClass('corworker-action');
        }
    });

    return CoWorkersAccessModuleView;
});

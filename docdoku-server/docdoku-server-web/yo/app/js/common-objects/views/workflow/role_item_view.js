/*global define*/
define(
    [
        'backbone',
        "mustache",
        "text!common-objects/templates/workflow/role_item.html"
    ],
    function (Backbone, Mustache, template) {

        var RoleItemView = Backbone.View.extend({



            className: "well roles-item",

            events: {
                "click .fa-times": "removeAndNotify",
                "change select": "changeModel"
            },

            initialize: function () {
                _.bindAll(this);

                if (!this.options.nullable && !this.model.get("defaultUserMapped")) {
                    this.model.set("defaultUserMapped", {login: this.options.userList.first().getLogin()});
                }
            },

            render: function () {
                this.$el.html(Mustache.render(template, {i18n: APP_CONFIG.i18n, model: this.model}));
                this.$select = this.$("select");
                this.fillUserList();
                return this;
            },

            fillUserList: function () {
                var self = this;

                if (this.options.nullable) {
                    this.$select.append("<option value=''></option>");
                }

                this.options.userList.each(function (user) {
                    var selected = "";
                    if (self.model.getMappedUserLogin() == user.get("login")) {
                        selected = " selected";
                    }
                    self.$select.append("<option value='" + user.get("login") + "'" + selected + ">" + user.get("name") + "</option>");
                });

            },

            removeAndNotify: function () {
                if (this.options.removable) {
                    this.remove();
                    this.trigger("view:removed");
                } else {
                    alert(APP_CONFIG.i18n.ALERT_ROLE_IN_USE);
                }

            },

            changeModel: function () {
                var userDTO = null;
                if (this.$select.val()) {
                    userDTO = {login: this.$select.val()};
                }
                this.model.set({defaultUserMapped: userDTO});
            }

        });

        return RoleItemView;

    });
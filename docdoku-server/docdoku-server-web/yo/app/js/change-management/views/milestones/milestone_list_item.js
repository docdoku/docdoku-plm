define([
    'backbone',
    "mustache",
    "text!templates/milestones/milestone_list_item.html",
    "views/milestones/milestone_edition"
], function (Backbone, Mustache, template, MilestoneEditionView) {
    var MilestoneListItemView = Backbone.View.extend({

        events: {
            "click input[type=checkbox]": "selectionChanged",
            "click td.milestone_title": "openEditionView"
        },

        tagName: "tr",

        initialize: function () {
            this._isChecked = false;
            this.listenTo(this.model, 'change', this.render);
        },

        render: function () {
            this.$el.html(Mustache.render(template, {model: this.model, i18n: APP_CONFIG.i18n}));
            this.$checkbox = this.$("input[type=checkbox]");
            this.trigger("rendered", this);
            return this;
        },

        selectionChanged: function () {
            this._isChecked = this.$checkbox.prop("checked");
            this.trigger("selectionChanged", this);
        },

        isChecked: function () {
            return this._isChecked;
        },

        check: function () {
            this.$checkbox.prop("checked", true);
            this._isChecked = true;
            this.trigger("selectionChanged", this);
        },

        unCheck: function () {
            this.$checkbox.prop("checked", false);
            this._isChecked = false;
            this.trigger("selectionChanged", this);
        },

        openEditionView: function () {
            var that = this;
            this.model.fetch();
            var editionView = new MilestoneEditionView({
                collection: that.collection,
                model: that.model
            });
            window.document.body.appendChild(editionView.render().el);
            editionView.openModal();
        }
    });

    return MilestoneListItemView;
});
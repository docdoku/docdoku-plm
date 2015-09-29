/*global _,define,App,$*/
define([
    'backbone',
    'mustache',
    'require',
    'text!common-objects/templates/alert.html'
], function (Backbone, Mustache, require, alertTemplate) {
	'use strict';
    var BaseView = Backbone.View.extend({

        modelEvents: {
            'change': 'modelChange',
            'sync': 'modelSync',
            'destroy': 'modelDestroy'
        },
        collectionEvents: {
            'reset': 'collectionReset',
            'add': 'collectionAdd',
            'remove': 'collectionRemove'
        },
        initialize: function () {

            // Owned events
            this.events = {};

            // Owned child views
            this.subViews = {};

            // Collection creation from factory
            if (_.isFunction(this.collection)) {
                this.collection = this.collection();
            }

            // Bindings
            _.bindAll(this);
            this.bindModel();
            this.bindCollection();
        },
        destroy: function () {
            this.deleteSubViews();
            this.undelegateEvents();
            this.unbindCollection();
            this.unbindModel();
            this.unbind();
            if (this.parentView) {
                delete this.parentView.subViews[this.cid];
            }
            if (_.isFunction(this.destroyed)) {
                this.destroyed();
            }
        },
        destroyed: function () {
            this.remove();
        },
        clear: function () {
            this.$el.html('');
        },
        addSubView: function (view) {
            view.parentView = this;
            this.subViews[view.cid] = view;
            if (_.isFunction(this.viewAdded)) {
                this.viewAdded(view);
            }
            return view;
        },
        deleteSubViews: function () {
            _.each(_.values(this.subViews), function (view) {
                if (_.isFunction(view.destroy)) {
                    view.destroy();
                }else{
                    view.remove();
                }
            });
        },
        _eventsBindings: function (options) {
            var target = options.target;
            var events = options.events;
            var action = options.action;
	        var that = this;
	        _.each(events, function(key,evt){
		        if (key in that && that[key]) {
			        target[action](evt, that[key]);
		        }
	        });
        },
        bindModel: function () {
            if (this.model) {
                this._eventsBindings({
                    target: this.model,
                    events: this.modelEvents,
                    action: 'bind'
                });
            }
        },
        bindCollection: function () {
            if (this.collection) {
                this._eventsBindings({
                    target: this.collection,
                    events: this.collectionEvents,
                    action: 'bind'
                });
            }
        },
        unbindModel: function () {
            if (this.model) {
                this._eventsBindings({
                    target: this.model,
                    events: this.modelEvents,
                    action: 'unbind'
                });
            }
        },
        unbindCollection: function () {
            if (this.collection) {
                this._eventsBindings({
                    target: this.collection,
                    events: this.collectionEvents,
                    action: 'unbind'
                });
            }
        },
        render: function () {
            this.deleteSubViews();
            var html = '';
            if (this.template) {
                var partials = this.partials ? this.partials : null;
                html = Mustache.render(this.template, this.renderData(), partials);
            }
            this.$el.html(html);
            if (_.isFunction(this.rendered)) {
                this.rendered();
            }
            return this;
        },
        renderData: function () {
            var data = {};
            data.i18n = App.config.i18n;
            data.workspaceId = App.config.workspaceId;
            data.view = this.viewToJSON();
            if (this.model) {
                data.model = this.modelToJSON();
            }
            if (this.collection) {
                data.collection = this.collectionToJSON();
            }
            if (this.templateExtraData) {
                _.extend(data, this.templateExtraData);
            }
            return data;
        },
        viewToJSON: function () {
            return {
                cid: this.cid
            };
        },
        modelToJSON: function () {
            return this.model.toJSON ?
                this.model.toJSON() :
                this.model;
        },
        collectionToJSON: function () {
            return this.collection.toJSON ?
                this.collection.toJSON() :
                this.collection;
        },
        alert: function (options) {
            // AlertView not used to resolve circular dependency
            var titles = {
                'error': App.config.i18n.ERROR
            };
            options.title = options.title ? options.title : titles[options.type];
            var html = Mustache.render(alertTemplate, {
                model: {
                    type: options.type,
                    title: options.title,
                    message: options.message
                }
            });
            $('#alerts-' + this.cid).html(html);
        }
    });
    return BaseView;
});

/*global define,App*/
define([
    'backbone',
    'mustache',
    'text!templates/part-permalink.html',
    'views/part-revision',
    'common-objects/views/not-found'
], function (Backbone, Mustache, template, PartRevisionView, NotFoundView) {
	'use strict';

    var AppView = Backbone.View.extend({

        el: '#content',

        render: function () {
            this.$el.html(Mustache.render(template, {
                i18n: App.config.i18n
            })).show();
            this.$notifications = this.$('.notifications');
            return this;
        },

        onPartFetched:function(part){
            this.$('.part-revision').html(new PartRevisionView().render(part).$el, null);
        },

        showPartRevision:function(workspace, partNumber, partVersion){
            $.getJSON(App.config.contextPath + '/api/shared/' +  workspace + '/parts/'+partNumber+'-'+partVersion)
                .then(this.onPartFetched.bind(this), this.onError.bind(this));
        },

        showSharedEntity:function(uuid){
            this.uuid = uuid;
            $.getJSON(App.config.contextPath + '/api/shared/' + uuid + '/parts')
                .then(this.onSharedEntityFetched.bind(this), this.onSharedEntityError.bind(this));
        },

        onSharedEntityFetched:function(part){
            this.$('.part-revision').html(new PartRevisionView().render(part, this.uuid).$el);
        },

        onSharedEntityError:function(err){
            if(err.status == 404){
                this.$el.html(new NotFoundView().render(err).$el);
            }
        },

        onError:function(err){
            if(err.status == 404){
                this.$el.html(new NotFoundView().render(err).$el);
            }
            else if(err.status === 403 || err.status === 401){
                window.location.href = App.config.contextPath + '/?denied=true&originURL=' + encodeURIComponent(window.location.pathname + window.location.hash);
            }
        }

    });

    return AppView;
});

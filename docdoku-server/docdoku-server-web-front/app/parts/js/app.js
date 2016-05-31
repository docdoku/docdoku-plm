/*global define,App*/
define([
    'backbone',
    'mustache',
    'text!templates/part-permalink.html',
    'views/part-revision'
], function (Backbone, Mustache, template, PartRevisionView) {
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
            this.$('.part-revision').html(new PartRevisionView().render(part).$el);
        },

        showPartRevision:function(workspace, partNumber, partVersion){
            this.options.workspace = workspace;
            this.options.partNumber = partNumber;
            this.options.partVersion = partVersion;
            $.getJSON(App.config.contextPath + '/api/shared/' +  this.options.workspace + '/parts/'+this.options.partNumber+'-'+this.options.partVersion)
                .then(this.onPartFetched.bind(this), this.onError.bind(this));
        },

        onError:function(err){
            if(err.status === 403 || err.status === 401){
                window.location.href = App.config.contextPath + '/?denied=true&originURL=' + encodeURIComponent(window.location.pathname + window.location.hash);
            }
        }

    });

    return AppView;
});

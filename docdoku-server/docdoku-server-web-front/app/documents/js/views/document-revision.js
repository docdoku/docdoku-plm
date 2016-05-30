/*global define,App*/
define([
    'backbone',
    'mustache',
    'text!templates/document-revision.html'
], function (Backbone, Mustache, template) {
    'use strict';

    var DocumentRevisionView = Backbone.View.extend({
        render: function (document) {

            var _this = this;

            var lastIteration = document.documentIterations[document.documentIterations.length-1];

            document.encodedRoutePath = encodeURIComponent(document.routePath);

            this.$el.html(Mustache.render(template, {
                i18n: App.config.i18n,
                contextPath:App.config.contextPath,
                document:document,
                lastIteration:lastIteration
            })).show();

            this.$accordion = this.$('#tab-document-files > .accordion');

            _.each(lastIteration.attachedFiles,function(file){
                $.get(App.config.contextPath+'/api/viewer/document?fileName='+encodeURIComponent(file)).then(function(data){
                    _this.$accordion.append(data);
                });
            });
            return this;
        }
    });

    return DocumentRevisionView;
});

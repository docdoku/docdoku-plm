/*global define,App*/
define([
    'backbone',
    'mustache',
    'text!templates/document-revision.html',
    'common-objects/utils/date'
], function (Backbone, Mustache, template, date) {
    'use strict';

    var DocumentRevisionView = Backbone.View.extend({
        render: function (document, uuid) {

            this.uuid = uuid;
            this.document = document;

            var _this = this;

            var lastIteration = document.documentIterations[document.documentIterations.length-1];

            document.creationDate = date.formatTimestamp(
                App.config.i18n._DATE_FORMAT,
                document.creationDate
            );

            document.releaseDate = date.formatTimestamp(
                App.config.i18n._DATE_FORMAT,
                document.releaseDate
            );

            document.obsoleteDate = date.formatTimestamp(
                App.config.i18n._DATE_FORMAT,
                document.obsoleteDate
            );

            lastIteration.creationDate = date.formatTimestamp(
                App.config.i18n._DATE_FORMAT,
                lastIteration.creationDate
            );

            lastIteration.checkInDate = date.formatTimestamp(
                App.config.i18n._DATE_FORMAT,
                lastIteration.checkInDate
            );

            lastIteration.modificationDate = date.formatTimestamp(
                App.config.i18n._DATE_FORMAT,
                lastIteration.modificationDate
            );

            document.encodedRoutePath = encodeURIComponent(document.routePath);

            this.$el.html(Mustache.render(template, {
                i18n: App.config.i18n,
                contextPath:App.config.contextPath,
                document:document,
                lastIteration:lastIteration
            })).show();

            date.dateHelper(this.$('.date-popover'));

            this.$accordion = this.$('#tab-document-files > .accordion');

            _.each(lastIteration.attachedFiles,function(binaryResource){
                var url = App.config.contextPath+'/api/viewer?';
                if(uuid){
                    url+= 'uuid=' + encodeURIComponent(uuid) + '&';
                }
                $.get(url+'fileName='+encodeURIComponent(binaryResource.fullName)).then(function(data){
                    _this.$accordion.append(data);
                });
            });
            return this;
        }
    });

    return DocumentRevisionView;
});

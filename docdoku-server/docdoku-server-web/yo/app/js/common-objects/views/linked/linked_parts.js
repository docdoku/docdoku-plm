/*global $,_,define,App*/
define([
    'backbone',
    'mustache',
    'common-objects/collections/linked/linked_part_collection',
    'common-objects/views/linked/linked_part',
    'text!common-objects/templates/linked/linked_items.html'
], function (Backbone, Mustache, LinkedPartCollection, LinkedPartView, template) {
	'use strict';
    var LinkedPartsView = Backbone.View.extend({

        tagName: 'div',
        className: 'linked-items-view',

        initialize: function () {
            this.searchResults = [];
            this._subViews = [];
            var self = this;
            this.$el.on('remove', function () {
                _(self._subViews).invoke('remove');
            });
        },

        render: function () {
            var self = this;

            this.$el.html(Mustache.render(template,
                {
                    i18n: App.config.i18n,
                    editMode: this.options.editMode,
                    label: App.config.i18n.ADD_PART,
                    view: this
                }
            ));

            this.bindDomElements();
            this.bindTypeahead();

            this.collection.each(function (linkedPart) {
                self.addLinkView(linkedPart);
            });

            return this;
        },

        bindDomElements: function () {
            this.partReferenceInput = this.$('.linked-items-reference-typehead');
            this.linksUL = this.$('#linked-items-' + this.cid);
        },

        addLinkView: function (linkedPart) {
            var linkView = new LinkedPartView({
                editMode: this.options.editMode,
                model: linkedPart
            }).render();

            this._subViews.push(linkView);
            this.linksUL.append(linkView.el);
        },

        bindTypeahead: function () {
            var self = this;
            var itemsLimit = 15;

            this.partReferenceInput.typeahead({
                items: itemsLimit,

                source: function (query, process) {
                    $.getJSON(App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/parts/parts_last_iter?q=' + query + '&l=' + itemsLimit, function (data) {

                        self.searchResults = new LinkedPartCollection(data);

                        // Remove parts that are already linked
                        var partsToRemove = [];
                        self.searchResults.each(
                            function (partIteration) {
                                var linkedPart = self.collection.find(
                                    function (linkedPart) {
                                        return linkedPart.getPartKey() === partIteration.getPartKey();
                                    });
                                if (!_.isUndefined(linkedPart)) {
                                    partsToRemove.push(partIteration);
                                }
                            }
                        );
                        self.searchResults.remove(partsToRemove);

                        process(self.searchResults.map(function (partLastIter) {
                            return partLastIter.getDisplayPartKey();
                        }));
                    });
                },

                sorter: function (partsLastIterPartKey) {
                    return partsLastIterPartKey.sort();
                },

                updater: function (partLastIterPartKey) {
                    var linkedPart = self.searchResults.find(function (partLastIter) {
                        return partLastIter.getDisplayPartKey() === partLastIterPartKey;
                    });
                    linkedPart.collection.remove(linkedPart);
                    self.collection.add(linkedPart);

                    self.addLinkView(linkedPart);
                }
            });
        }

    });
    return LinkedPartsView;
});

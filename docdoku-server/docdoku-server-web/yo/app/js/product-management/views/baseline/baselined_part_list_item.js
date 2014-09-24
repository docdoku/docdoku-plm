/*global define*/
'use strict';
define([
    'backbone',
    "mustache",
    "text!templates/baseline/baselined_part_list_item.html"
], function (Backbone, Mustache, template) {

    var BaselinedPartListItemView = Backbone.View.extend({

        tagName: "li",

        className: "baselined-part-item",

        events: {
            "change .iteration-input": "changeIteration",
            "change .version-input": "changeVersion",
            "change input.exclude-input": "excludePart"
        },

        template: Mustache.parse(template),

        initialize: function () {
            this.isForBaseline = (this.options.isForBaseline) ? this.options.isForBaseline : false;
            this.isLocked = (this.options.isLocked) ? this.options.isLocked : false;
            this.availableIterations = _(this.model.getAvailableIterations());
        },

        render: function () {
            var data = {
                model: this.model,
                i18n: App.config.i18n
            };
            if (this.isForBaseline) {
                data.released = this.options.released;
                data.isForBaseline = this.isForBaseline;
            }
            this.$el.html(Mustache.render(template, data));
            this.bindDomElements();
            this.fillInputs();
            return this;
        },

        bindDomElements: function () {
            this.$versionInput = this.$el.find('.version-input');
            this.$iterationInput = this.$el.find('.iteration-input');
        },

        fillInputs: function () {
            var _this = this;
            var availableVersion = [];
            this.availableIterations.each(function (optionI) {
                availableVersion.push({
                    version: optionI.version,
                    released: optionI.released
                });
            });
            availableVersion = _.uniq(availableVersion);
            _(availableVersion).each(function (optionV) {
                if (!_this.isForBaseline || !_this.options.released || optionV.released) {
                    _this.$versionInput.append('<option value="' + optionV.version + '" >' + optionV.version + '</option>');
                }
            });
            this.$versionInput.on('change', this, this.fillIteration.bind(this));
            this.$versionInput.val(this.model.getVersion());
            if (_this.isLocked) {
                this.$versionInput.prop('disabled', 'disabled');
                this.$iterationInput.prop('disabled', 'disabled');
            } else if (_this.isForBaseline && _this.options.released) {
                this.$iterationInput.prop('disabled', 'disabled');

            }
            this.fillIteration();
        },

        fillIteration: function () {
            var _this = this;
            var iteration = 1;
            var version = this.$versionInput.val();
            this.$iterationInput.html("");
            this.availableIterations.each(function (optionI) {
                if (optionI.version === version) {
                    iteration = optionI.lastIteration;
                    if (_this.isForBaseline && _this.options.released) {
                        _this.$iterationInput.append('<option value="' + iteration + '">' + iteration + '</option>');
                    } else {
                        _(_.range(1, iteration + 1)).each(function (value) {
                            _this.$iterationInput.append('<option value="' + value + '">' + value + '</option>');
                        });
                    }
                }
            });
            this.$iterationInput.val(_this.model.getIteration());
        },

        changeIteration: function (e) {
            if (e.target.value) {
                this.model.setIteration(e.target.value);
            }
        },
        changeVersion: function (e) {
            if (e.target.value) {
                this.model.setVersion(e.target.value);
            }
        },
        excludePart: function (e) {
            if (e.target.checked) {
                this.$el.css("opacity", 0.5);
            } else {
                this.$el.css("opacity", 1);
            }
            this.model.setExcluded(e.target.checked);
        }

    });

    return BaselinedPartListItemView;
});
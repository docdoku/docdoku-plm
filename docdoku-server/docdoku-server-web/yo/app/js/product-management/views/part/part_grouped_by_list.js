/*global _,define,App*/
define([
    'backbone',
    'mustache',
    'text!templates/part/part_grouped_by_list.html',
    'views/part/part_grouped_by_list_item'
], function (Backbone, Mustache, template, PartGroupedByListItemView){
    'use strict';
    var PartGroupedByView = Backbone.View.extend({

        events: {

        },

        initialize: function () {

        },

        render: function () {

            var a = {
                "ELEC_05":[{"number":"AAA","date":"05","a":"ELEC"}],
                "STRU_05":[{"number":"BBB","date":"05","a":"STRU"},{"number":"CCC","date":"05","a":"STRU"},{"number":"DDD","date":"05","a":"STRU"}],
                "CAB_06":[{"number":"EEE","date":"06","a":"CAB"},{"number":"FFF","date":"06","a":"CAB"},{"number":"AAA","date":"06","a":"CAB"},{"number":"BBB","date":"06","a":"CAB"}]
            };

            this.$el.html(Mustache.render(template, {
                i18n: App.config.i18n,
                columns:['number', 'date', 'a'],
                groups: _.keys(a)
            }));

            var self = this;
            _.each(_.keys(a),function(key){
                var values = a[key];
                _.each(values, function(item){
                    var itemView = new PartGroupedByListItemView({
                        item : item
                    }).render();
                    self.$('.items-'+key).append(itemView.el);
                });
            });

            return this;
        }
    });

    return PartGroupedByView;
});

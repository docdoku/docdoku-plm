/*global define,App,nv,d3*/
define([
    'backbone',
    'mustache',
    'text!templates/admin-dashboard.html',
    'common-objects/models/admin',
    'charts-helpers'
], function (Backbone, Mustache, template, Admin, ChartsHelpers) {
    'use strict';

    var AdminDashboardView = Backbone.View.extend({

        events: {},

        initialize: function () {
        },

        render: function () {
            this.$el.html(Mustache.render(template, {
                i18n: App.config.i18n
            }));

            Admin.getDiskSpaceUsageStats()
                .then(this.constructDiskUsageChart.bind(this));

            var onData = function (data) {
                return data;
            };

            $.when(
                Admin.getUsersStats().then(onData),
                Admin.getDocumentsStats().then(onData),
                Admin.getProductsStats().then(onData),
                Admin.getPartsStats().then(onData)
            ).then(this.constructEntitiesChart.bind(this));

            return this;
        },

        constructDiskUsageChart: function (diskUsage) {

            var diskUsageData = [];
            var totalDiskUsage = 0;

            for (var key in diskUsage) {
                //if(diskUsage[key]){
                diskUsageData.push({key: key, y: diskUsage[key], f: ChartsHelpers.bytesToSize(diskUsage[key])});
                totalDiskUsage += diskUsage[key];
                // }
            }

            var $chart = this.$('#admin_disk_usage_chart');
            $chart.parent().find('span.total').html(ChartsHelpers.bytesToSize(totalDiskUsage));
            var width = $chart.width();
            var height = $chart.height();
            $chart.find('svg')
                .attr('width', '100%')
                .attr('height', '100%')
                .attr('viewBox', '0 0 ' + Math.min(width, height) + ' ' + Math.min(width, height))
                .attr('preserveAspectRatio', 'xMinYMin')
                .attr('transform', 'translate(' + Math.min(width, height) / 2 + ',' + Math.min(width, height) / 2 + ')');

            nv.addGraph(function () {

                var chart = nv.models.pieChart()
                    .x(function(d) { return d.key; })
                    .y(function(d) { return d.y; })
                    .width(width)
                    .height(height)
                    .showTooltipPercent(true);

                chart.tooltip.contentGenerator(function (obj) {
                    return ChartsHelpers.diskUsageTooltip(obj.data.key, obj.data.f);
                });


                d3.select('#admin_disk_usage_chart svg')
                    .datum(diskUsageData)
                    .transition().duration(1200)
                    .call(chart);
                nv.utils.windowResize(chart.update);

                return chart;
            });
        },

        constructEntitiesChart: function (usersStats, docsStats, productsStats, partsStats) {

            var usersData = [], docsData = [], partsData = [], productsData = [];

            var total = 0;
            var populate = function (stats, data) {
                for (var key in stats) {
                    if (stats[key]) {
                        data.push({key: key, y: stats[key]});
                        total++;
                    }
                }
            };

            populate(usersStats, usersData);
            populate(docsStats, docsData);
            populate(productsStats, productsData);
            populate(partsStats, partsData);

            if (total) {

                var $chart = this.$('#admin_users_chart');
                var width = $chart.width();
                var height = $chart.height();
                $chart.find('svg')
                    .attr('width', '100%')
                    .attr('height', '100%')
                    .attr('viewBox', '0 0 ' + Math.min(width, height) + ' ' + Math.min(width, height))
                    .attr('preserveAspectRatio', 'xMinYMin')
                    .attr('transform', 'translate(' + Math.min(width, height) / 2 + ',' + Math.min(width, height) / 2 + ')');


                nv.addGraph(function () {
                    var chart = nv.models.multiBarHorizontalChart()
                        .x(function (d) {
                            return d.key;
                        })
                        .y(function (d) {
                            return d.y;
                        })
                        .showValues(true)
                        /*.tooltips(false)*/
                        .showControls(true);

                    chart.yAxis
                        .tickFormat(d3.format(',f'));

                    d3.select('#admin_users_chart svg')
                        .datum([
                            {key: App.config.i18n.USERS, values: usersData},
                            {key: App.config.i18n.PRODUCTS, values: productsData},
                            {key: App.config.i18n.PARTS, values: partsData},
                            {key: App.config.i18n.DOCUMENTS, values: docsData}
                        ])
                        .transition().duration(500)
                        .call(chart);

                    nv.utils.windowResize(chart.update);

                    return chart;
                });
            }

        }

    });

    return AdminDashboardView;
});

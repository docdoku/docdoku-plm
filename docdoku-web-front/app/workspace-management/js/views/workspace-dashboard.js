/*global define,App,nv,d3*/
define([
    'backbone',
    'mustache',
    'text!templates/workspace-dashboard.html',
    'common-objects/models/workspace',
    'charts-helpers'
], function (Backbone, Mustache, template, Workspace, ChartsHelpers) {
    'use strict';

    var MAX_DAYS = 30;

    function calculateDaysSinceTimestamp(timestamp) {
        var days = parseInt(((((new Date().getTime() - timestamp) / 1000) / 60) / 60) / 24);
        return days < MAX_DAYS ? days + 1 : MAX_DAYS;
    }

    var WorkspaceDashboardView = Backbone.View.extend({

        events: {},

        initialize: function () {
        },

        render: function () {
            this.$el.html(Mustache.render(template, {
                i18n: App.config.i18n,
                workspace: _.findWhere(App.config.workspaces.administratedWorkspaces, {id: App.config.workspaceId})
            }));

            Workspace.getDiskUsageStats(App.config.workspaceId)
                .then(this.constructDiskUsageChart.bind(this));

            Workspace.getUsersStats(App.config.workspaceId)
                .then(this.constructUsersCharts.bind(this));

            Workspace.getStatsOverView(App.config.workspaceId)
                .then(this.constructEntitiesChart.bind(this));

            Workspace.getCheckedOutDocumentsStats(App.config.workspaceId)
                .then(this.constructCheckedOutDocsCharts.bind(this));

            Workspace.getCheckedOutPartsStats(App.config.workspaceId)
                .then(this.constructCheckedOutPartsCharts.bind(this));

            return this;
        },

        constructDiskUsageChart: function (diskUsage) {

            var diskUsageData = [];
            var totalDiskUsage = 0;

            var translates = {
                documents: App.config.i18n.DOCUMENTS,
                documentTemplates: App.config.i18n.DOCUMENT_TEMPLATES,
                parts: App.config.i18n.PARTS,
                partTemplates: App.config.i18n.PART_TEMPLATES
            };

            for (var key in diskUsage) {
                if(diskUsage[key]){
                    diskUsageData.push({key: translates[key], y: diskUsage[key], f: ChartsHelpers.bytesToSize(diskUsage[key])});
                }
                totalDiskUsage += diskUsage[key];
            }

            if (diskUsageData.length === 0) {
                diskUsageData.push({key: App.config.i18n.NO_DATA, y: 100, f: App.config.i18n.NO_DATA});
            }

            var $chart = this.$('#disk_usage_chart');
            var width = $chart.width();
            var height = $chart.height();
            $chart.find('svg')
                .attr('width', '100%')
                .attr('height', '100%')
                .attr('viewBox', '0 0 ' + Math.min(width, height) + ' ' + Math.min(width, height))
                .attr('preserveAspectRatio', 'xMinYMin')
                .attr('transform', 'translate(' + Math.min(width, height) / 2 + ',' + Math.min(width, height) / 2 + ')');

            $chart.parent().find('span.total').html(ChartsHelpers.bytesToSize(totalDiskUsage));

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


                d3.select('#disk_usage_chart svg')
                    .datum(diskUsageData)
                    .transition().duration(1200)
                    .call(chart);

                return chart;
            });
        },

        constructEntitiesChart: function (entitiesCount) {

            var entitiesData = [];

            var usersCount = entitiesCount.users;
            var documentsCount = entitiesCount.documents;
            var productsCount = entitiesCount.products;
            var partsCount = entitiesCount.parts;

            // TODO translate
            entitiesData.push({key: App.config.i18n.USERS, y: usersCount});
            entitiesData.push({key: App.config.i18n.DOCUMENTS, y: documentsCount});
            entitiesData.push({key: App.config.i18n.PRODUCTS, y: productsCount});
            entitiesData.push({key: App.config.i18n.PARTS, y: partsCount});


            var $chart = this.$('#entities_chart');
            var width = $chart.width();
            var height = $chart.height();
            $chart.find('svg')
                .attr('width', '100%')
                .attr('height', '100%')
                .attr('viewBox', '0 0 ' + Math.min(width, height) + ' ' + Math.min(width, height))
                .attr('preserveAspectRatio', 'xMinYMin')
                .attr('transform', 'translate(' + Math.min(width, height) / 2 + ',' + Math.min(width, height) / 2 + ')');

            nv.addGraph(function () {
                var chart = nv.models.discreteBarChart()
                    .x(function (d) {
                        return d.key;
                    })
                    .y(function (d) {
                        return d.y;
                    })
                    .staggerLabels(true)
                    /*.tooltips(false)*/
                    .showValues(true);

                chart.yAxis.tickFormat(d3.format('.f'));

                d3.select('#entities_chart svg')
                    .datum([{key: 'entities', values: entitiesData}])
                    .transition().duration(500)
                    .call(chart);

                nv.utils.windowResize(chart.update);

                return chart;
            });
        },

        constructCheckedOutDocsCharts: function (cod) {

            var codData = [];
            var totalCod = 0;

            for (var user in cod) {
                var documents = cod[user];
                var mapDayDoc = {};
                var userData = {
                    key: user,
                    values: []
                };
                for (var i = 0; i < MAX_DAYS + 1; i++) {
                    mapDayDoc[i] = 0;
                }
                for (var j = 0; j < documents.length; j++) {
                    mapDayDoc[calculateDaysSinceTimestamp(documents[j].date)]++;
                    totalCod++;
                }
                for (var day in mapDayDoc) {
                    if (mapDayDoc[day] > 0) {
                        userData.values.push({
                            x: day,
                            y: mapDayDoc[day],
                            size: mapDayDoc[day]
                        });
                    }
                }
                codData.push(userData);
            }

            var $chart = this.$('#cod_chart');
            var width = $chart.width();
            var height = $chart.height();

            $chart.find('svg')
                .attr('width', '100%')
                .attr('height', '100%')
                .attr('viewBox', '0 0 ' + Math.min(width, height) + ' ' + Math.min(width, height))
                .attr('preserveAspectRatio', 'xMinYMin')
                .attr('transform', 'translate(' + Math.min(width, height) / 2 + ',' + Math.min(width, height) / 2 + ')');
            $chart.parent().find('span.total').html(totalCod);

            if (codData.length) {

                nv.addGraph(function () {
                    var chart = nv.models.scatterChart()
                        .showDistX(true)
                        .showDistY(true)
                        .forceX([0, MAX_DAYS])
                        .forceY([0, null])
                        .color(d3.scale.category10().range());

                    chart.xAxis.tickFormat(d3.format('.f'));
                    chart.xAxis.axisLabel(App.config.i18n.CHART_AXIS_DAYS_NUMBER);
                    chart.yAxis.tickFormat(d3.format('.f'));
                    chart.yAxis.axisLabel(App.config.i18n.CHART_AXIS_DOCUMENTS_NUMBER);

                    d3.select('#cod_chart svg')
                        .datum(codData)
                        .transition().duration(500)
                        .call(chart);

                    nv.utils.windowResize(chart.update);

                    return chart;
                });

            } else {
                $chart.html(App.config.i18n.NO_DATA);
            }


        },

        constructCheckedOutPartsCharts: function (cop) {

            var copData = [];
            var totalCop = 0;
            for (var user in cop) {
                var parts = cop[user];
                var mapDayPart = {};
                var userData = {
                    key: user,
                    values: []
                };
                for (var i = 0; i < MAX_DAYS + 1; i++) {
                    mapDayPart[i] = 0;
                }
                for (var j = 0; j < parts.length; j++) {
                    mapDayPart[calculateDaysSinceTimestamp(parts[j].date)]++;
                    totalCop++;
                }
                for (var day in mapDayPart) {
                    if (mapDayPart[day] > 0) {
                        userData.values.push({
                            x: day,
                            y: mapDayPart[day],
                            size: mapDayPart[day]
                        });
                    }
                }
                copData.push(userData);
            }

            var $chart = this.$('#cop_chart');
            var width = $chart.width();
            var height = $chart.height();
            $chart.find('svg')
                .attr('width', '100%')
                .attr('height', '100%')
                .attr('viewBox', '0 0 ' + Math.min(width, height) + ' ' + Math.min(width, height))
                .attr('preserveAspectRatio', 'xMinYMin')
                .attr('transform', 'translate(' + Math.min(width, height) / 2 + ',' + Math.min(width, height) / 2 + ')');

            $chart.parent().find('span.total').html(totalCop);

            if (copData.length) {
                nv.addGraph(function () {
                    var chart = nv.models.scatterChart()
                        .showDistX(true)
                        .showDistY(true)
                        .forceX([0, MAX_DAYS])
                        .forceY([0, null])
                        .color(d3.scale.category10().range());

                    chart.xAxis.tickFormat(d3.format('.f'));
                    chart.xAxis.axisLabel(App.config.i18n.CHART_AXIS_DAYS_NUMBER);
                    chart.yAxis.tickFormat(d3.format('.f'));
                    chart.yAxis.axisLabel(App.config.i18n.CHART_AXIS_PARTS_NUMBER);

                    d3.select('#cop_chart svg')
                        .datum(copData)
                        .transition().duration(500)
                        .call(chart);

                    nv.utils.windowResize(chart.update);

                    return chart;
                });

            } else {
                $chart.html(App.config.i18n.NO_DATA);
            }
        },

        constructUsersCharts: function (usersInWorkspace, usersStats) {

            var usersAndGroupData = [];
            var translates = {
                users: App.config.i18n.USERS,
                activeusers: App.config.i18n.ACTIVE_USERS,
                inactiveusers: App.config.i18n.INACTIVE_USERS,
                groups: App.config.i18n.GROUPS,
                activegroups: App.config.i18n.ACTIVE_GROUPS,
                inactivegroups: App.config.i18n.INACTIVE_GROUPS
            };
            // TODO translate keys
            for (var key in usersStats) {
                usersAndGroupData.push({key: translates[key], y: usersStats[key]});
            }

            nv.addGraph(function () {
                var chart = nv.models.discreteBarChart()
                    .x(function (d) {
                        return d.key;
                    })
                    .y(function (d) {
                        return d.y;
                    })
                    .staggerLabels(true)
                    /*.tooltips(true)*/
                    .showValues(true);

                chart.yAxis.tickFormat(d3.format('.f'));

                d3.select('#users_chart svg')
                    .datum([{key: 'entities', values: usersAndGroupData}])
                    .transition().duration(500)
                    .call(chart);

                nv.utils.windowResize(chart.update);
                return chart;
            });

        }
    });

    return WorkspaceDashboardView;
});

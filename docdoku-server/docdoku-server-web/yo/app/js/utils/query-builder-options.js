/*global _,define,App*/
define(function () {

    /**
     *
     * Jquery Query builder doc : http://mistic100.github.io/jQuery-QueryBuilder
     *
     * Available operators

     equal
     not_equal
     in
     not_in
     less
     less_or_equal
     greater
     greater_or_equal
     between
     begins_with
     not_begins_with
     contains
     not_contains
     ends_with
     not_ends_with
     is_empty
     is_not_empty
     is_null
     is_not_null

     * Available types

     string
     integer
     double
     date
     time
     datetime
     boolean

     * Available inputs

     text
     textarea
     radio
     checkbox
     select
     custom fn (rule, input_name) -> returns html string.


     *
     * */

    var stringDefaultOps = ['equal', 'not_equal', 'contains', 'not_contains', 'begins_with', 'not_begins_with', 'ends_with', 'not_ends_with'];
    var dateOperators = ['equal', 'not_equal', 'less', 'less_or_equal', 'greater', 'greater_or_equal', 'between'];

    var filters = [];

    // Part

    filters.push({
        id: 'p.name',
        label: 'Name',
        type: 'string',
        operators: stringDefaultOps
    });

    filters.push({
        id: 'p.number',
        label: 'Number',
        type: 'string',
        operators: stringDefaultOps
    });

    filters.push({
        id: 'p.type',
        label: 'Type',
        type: 'string',
        operators: stringDefaultOps
    });

    filters.push({
        id: 'p.author',
        label: 'Author',
        type: 'string',
        operators: stringDefaultOps
    });

    filters.push({
        id: 'p.date',
        label: 'Modification date',
        type: 'date',
        operators: dateOperators
    });

    filters.push({
        id: 'p.life_cycle_state',
        label: 'Life cycle state',
        type: 'string',
        operators: stringDefaultOps
    });

    // Product instances

    filters.push({
        id: 'pi.serial',
        label: 'Serial number',
        type: 'string',
        operators: stringDefaultOps
    });

    filters.push({
        id: 'pi.name',
        label: 'Name',
        type: 'string',
        operators: stringDefaultOps
    });


    return {
        filters : filters,
        fields : [
            {name:App.config.i18n.PART_NUMBER, value:'p.id'},
            {name:App.config.i18n.PART_NAME, value:'p.number'},
            {name:App.config.i18n.AUTHOR, value:'p.author'},
            {name:App.config.i18n.TYPE, value:'p.type'},
            {name:App.config.i18n.MODIFICATION_DATE, value:'p.date'},
            {name:App.config.i18n.LIFECYCLE_STATE, value:'p.life_cycle_state'},
            {name:App.config.i18n.SERIAL_NUMBER, value:'p.serial'},
            {name:App.config.i18n.PRODUCT_NAME, value:'pi.name'}
        ]
    };

});

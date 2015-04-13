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


    $.fn.queryBuilder.defaults({ lang: {
        "add_rule": "",
        "add_group": "",
        "delete_rule": "",
        "delete_group": "",
        "conditions": {
            "AND": App.config.i18n.QUERY_BUILDER_CONDITION_AND,
            "OR": App.config.i18n.QUERY_BUILDER_CONDITION_OR
        },
        "operators": {
            "equal": App.config.i18n.QUERY_BUILDER_OPERATORS_EQUALS,
            "not_equal": App.config.i18n.QUERY_BUILDER_OPERATORS_NOT_EQUALS,
            "in": App.config.i18n.QUERY_BUILDER_OPERATORS_IN,
            "not_in": App.config.i18n.QUERY_BUILDER_OPERATORS_NOT_IN,
            "less": App.config.i18n.QUERY_BUILDER_OPERATORS_LESS,
            "less_or_equal": App.config.i18n.QUERY_BUILDER_OPERATORS_LESS_OR_EQUAL,
            "greater": App.config.i18n.QUERY_BUILDER_OPERATORS_GREATER,
            "greater_or_equal": App.config.i18n.QUERY_BUILDER_OPERATORS_GREATER_OR_EQUAL,
            "between": App.config.i18n.QUERY_BUILDER_OPERATORS_BETWEEN,
            "begins_with": App.config.i18n.QUERY_BUILDER_OPERATORS_BEGIN_WITH,
            "not_begins_with": App.config.i18n.QUERY_BUILDER_OPERATORS_NOT_BEGIN_WITH,
            "contains": App.config.i18n.QUERY_BUILDER_OPERATORS_CONTAINS,
            "not_contains": App.config.i18n.QUERY_BUILDER_OPERATORS_NOT_CONTAINS,
            "ends_with": App.config.i18n.QUERY_BUILDER_OPERATORS_ENDS_WITH,
            "not_ends_with": App.config.i18n.QUERY_BUILDER_OPERATORS_NOT_ENDS_WITH,
            "is_empty": App.config.i18n.QUERY_BUILDER_OPERATORS_IS_EMPTY,
            "is_not_empty": App.config.i18n.QUERY_BUILDER_OPERATORS_IS_NOT_EMPTY,
            "is_null": App.config.i18n.QUERY_BUILDER_OPERATORS_IS_NULL,
            "is_not_null": App.config.i18n.QUERY_BUILDER_OPERATORS_IS_NOT_NULL
        },
        "errors": {
            "no_filter": App.config.i18n.QUERY_BUILDER_ERRORS_NO_FILTER,
            "empty_group": App.config.i18n.QUERY_BUILDER_ERRORS_EMPTY_GROUP,
            "radio_empty": App.config.i18n.QUERY_BUILDER_ERRORS_RADIO_EMPTY,
            "checkbox_empty": App.config.i18n.QUERY_BUILDER_ERRORS_CHECKBOX_EMPTY,
            "select_empty": App.config.i18n.QUERY_BUILDER_ERRORS_SELECT_EMPTY,
            "string_empty": App.config.i18n.QUERY_BUILDER_ERRORS_STRING_EMPTY,
            "string_exceed_min_length": App.config.i18n.QUERY_BUILDER_ERRORS_STRING_EXCEED_MIN_LENGTH,
            "string_exceed_max_length": App.config.i18n.QUERY_BUILDER_ERRORS_STRING_EXCEED_MAX_LENGTH,
            "string_invalid_format": App.config.i18n.QUERY_BUILDER_ERRORS_STRING_INVALID_FORMAT,
            "number_nan": App.config.i18n.QUERY_BUILDER_ERRORS_NUMBER_NAN,
            "number_not_integer": App.config.i18n.QUERY_BUILDER_ERRORS_NUMBER_NOT_INTEGER,
            "number_not_double": App.config.i18n.QUERY_BUILDER_ERRORS_NUMBER_NOT_DOUBLE,
            "number_exceed_min": App.config.i18n.QUERY_BUILDER_ERRORS_NUMBER_EXCEED_MIN,
            "number_exceed_max": App.config.i18n.QUERY_BUILDER_ERRORS_NUMBER_EXCEED_MAX,
            "number_wrong_step": App.config.i18n.QUERY_BUILDER_ERRORS_NUMBER_WRONG_STEP,
            "datetime_empty": App.config.i18n.QUERY_BUILDER_ERRORS_DATETIME_EMPTY,
            "datetime_invalid": App.config.i18n.QUERY_BUILDER_ERRORS_DATETIME_INVALID,
            "datetime_exceed_min": App.config.i18n.QUERY_BUILDER_ERRORS_DATETIME_EXCEED_MIN,
            "datetime_exceed_max": App.config.i18n.QUERY_BUILDER_ERRORS_DATETIME_EXCEED_MAX,
            "boolean_not_valid": App.config.i18n.QUERY_BUILDER_ERRORS_BOOLEAN_NOT_VALID,
            "operator_not_multiple": App.config.i18n.QUERY_BUILDER_ERRORS_OPERATOR_NOT_MULTIPLE
        }
    }});

    var stringDefaultOps = ['equal', 'not_equal', 'contains', 'not_contains', 'begins_with', 'not_begins_with', 'ends_with', 'not_ends_with'];
    var dateOperators = ['equal', 'not_equal', 'less', 'less_or_equal', 'greater', 'greater_or_equal', 'between'];

    var filters = [];

    // Part

    filters.push({
        id: 'p.name',
        label: App.config.i18n.PART_NAME,
        type: 'string',
        operators: stringDefaultOps
    });

    filters.push({
        id: 'p.number',
        label: App.config.i18n.PART_NUMBER,
        type: 'string',
        operators: stringDefaultOps
    });

    filters.push({
        id: 'p.type',
        label: App.config.i18n.TYPE,
        type: 'string',
        operators: stringDefaultOps
    });

    filters.push({
        id: 'p.author',
        label: App.config.i18n.AUTHOR,
        type: 'string',
        operators: stringDefaultOps
    });

    filters.push({
        id: 'p.date',
        label: App.config.i18n.MODIFICATION_DATE,
        type: 'date',
        operators: dateOperators
    });

    filters.push({
        id: 'p.life_cycle_state',
        label: App.config.i18n.LIFECYCLE_STATE,
        type: 'string',
        operators: stringDefaultOps
    });

    // Product instances

    filters.push({
        id: 'pi.serial',
        label: App.config.i18n.SERIAL_NUMBER,
        type: 'string',
        operators: stringDefaultOps
    });

    filters.push({
        id: 'pi.name',
        label: App.config.i18n.PRODUCT_NAME,
        type: 'string',
        operators: stringDefaultOps
    });


    return {
        filters : filters,
        fields : [
            {name:App.config.i18n.PART_NUMBER, value:'p.number'},
            {name:App.config.i18n.PART_NAME, value:'p.name'},
            {name:App.config.i18n.AUTHOR, value:'p.author'},
            {name:App.config.i18n.TYPE, value:'p.type'},
            {name:App.config.i18n.MODIFICATION_DATE, value:'p.date'},
            {name:App.config.i18n.LIFECYCLE_STATE, value:'p.life_cycle_state'},
            {name:App.config.i18n.SERIAL_NUMBER, value:'p.serial'},
            {name:App.config.i18n.PRODUCT_NAME, value:'pi.name'}
        ]
    };

});

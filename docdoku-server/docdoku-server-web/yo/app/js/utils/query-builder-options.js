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

     *
     * */

    var stringDefaultOps = ['equal', 'not_equal', 'contains', 'not_contains', 'begins_with', 'not_begins_with', 'ends_with', 'not_ends_with'];
    var dateOperators = ['equal', 'not_equal', 'less', 'less_or_equal', 'greater', 'greater_or_equal', 'between'];

    var filters = [];

    filters.push({
        id: 'name',
        label: 'Name',
        type: 'string',
        operators: stringDefaultOps
    });

    filters.push({
        id: 'number',
        label: 'Number',
        type: 'string',
        operators: stringDefaultOps
    });

    filters.push({
        id: 'type',
        label: 'Type',
        type: 'string',
        operators: stringDefaultOps
    });

    filters.push({
        id: 'author',
        label: 'Author',
        type: 'string',
        operators: stringDefaultOps
    });

    filters.push({
        id: 'date',
        label: 'Date',
        type: 'date',
        operators: dateOperators
    });


    filters.push({
        id: 'category',
        label: 'Category',
        type: 'integer',
        input: 'select',
        values: {
            1: 'Books',
            2: 'Movies',
            3: 'Music',
            4: 'Tools',
            5: 'Goodies',
            6: 'Clothes'
        },
        operators: ['equal', 'not_equal', 'in', 'not_in', 'is_null', 'is_not_null']
    });


    return {
        filters : filters,
        fields : []
    };

});

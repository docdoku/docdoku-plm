/*global module,__utils__,exports,require*/
'use strict';
// Todo mutualise method
var Tools = {
    assertExist : function(context, selector, successMessage, failMessage){
        var exists = context.evaluate(function() {
            return __utils__.exists(selector);
        });
        context.echo(exists);
        if(!exists){
            context.test.fail(failMessage);
            context.exit(failMessage);
        }
        context.evaluate(function(){__utils__.log(successMessage, 'info');});
    },

    assertExistAndClick : function(context, selector, successMessage, failMessage){
        context.assertExist(context, selector, successMessage, failMessage);
        context.click(selector);
    },

    assertExistAddFill : function(context, inputSelector, successMessage, failMessage, value){
        context.assertExist(context, inputSelector, successMessage, failMessage);
        context.sendKeys(inputSelector, value);
    }
};
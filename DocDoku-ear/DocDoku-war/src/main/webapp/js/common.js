// A Singleton decorator.
// To be used with _.wrap
// var Foo = function () {};
// Foo = singletonDecorator(Foo);
var singletonDecorator = function (constructor) {
	constructor.getInstance = function () {
		if (!constructor._instance) {
			constructor._instance = new constructor();
			constructor.apply(constructor._instance, arguments);
		}
		return constructor._instance;
	}
	return constructor;
};

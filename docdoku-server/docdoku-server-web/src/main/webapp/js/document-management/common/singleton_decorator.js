define(function () {
	// A Singleton decorator.
	// requires underscrore.js
	// var Foo = function () {};
	// Foo = singletonDecorator(Foo);
	var singletonDecorator = function (constructor) {
		constructor.getInstance = function () {
			if (!constructor._instance) {
				constructor._instance = _.extend(constructor.prototype, {});
				constructor.apply(constructor._instance, arguments);
			}
			return constructor._instance;
		}
		return constructor;
	};
	return singletonDecorator;
});

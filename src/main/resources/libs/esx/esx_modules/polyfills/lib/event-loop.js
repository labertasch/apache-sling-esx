/**
 * adapted from:  https://github.com/m4r71n/sass.js-rhino-nashorn/blob/master/resources/nashorn-shims.js
 */

module.exports = (function(context) {
    'use strict';

    var Timer = Java.type('java.util.Timer');
    var Phaser = Java.type('java.util.concurrent.Phaser');

    var timer = new Timer('jsEventLoop', false);
    var phaser = new Phaser();

    var onTaskFinished = function() {
        phaser.arriveAndDeregister();
    };

    context.setTimeout = function(fn, millis /* [, args...] */) {
        var args = [].slice.call(arguments, 2, arguments.length);

        var phase = phaser.register();
        var canceled = false;
        timer.schedule(function() {
            if (canceled) {
                return;
            }

            try {
                fn.apply(context, args);
            } catch (e) {
                print(e);
            } finally {
                onTaskFinished();
            }
        }, millis);

        return function() {
            onTaskFinished();
            canceled = true;
        };
    };

    context.clearTimeout = function(cancel) {
        cancel();
    };

    context.setInterval = function(fn, delay /* [, args...] */) {
        var args = [].slice.call(arguments, 2, arguments.length);

        var cancel = null;

        var loop = function() {
            cancel = context.setTimeout(loop, delay);
            fn.apply(context, args);
        };

        cancel = context.setTimeout(loop, delay);
        return function() {
            cancel();
        };
    };

    context.clearInterval = function(cancel) {
        cancel();
    };

});
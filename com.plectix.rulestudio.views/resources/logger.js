/************************************/
/* Debugging and profiling          */

// Debugging could toggle based on rails env
(function($){
  // alias $.log to console.log if available, else alias to an empty function
  // console.log in IE8 is not a prototype based object, so we need to wrap it
  // around a Javascript function object.
  $.log = ( window.console && window.console.log ) ?
    function() { console.log.apply(this, arguments); } : function() {};
  $.log.info = ( window.console && window.console.info ) ?
    function() { console.info.apply(this, arguments); } : function() {};
  $.log.warn = ( window.console && window.console.warn ) ?
    function() { console.warn.apply(this, arguments); } : function() {};
  $.log.error = ( window.console && window.console.error ) ?
    function() { console.error.apply(this, arguments); } : function() {};

  $.log.debugging = true;
  $.log.profiling = true;

  var d = new Date();
  var startEventTime = d.getTime();
  var lastEventTime = startEventTime;

  $.bench = function(msg, fn, args, scope){
    var startTime= new Date();
    var rv = fn.apply(scope || window, args);
    var endTime = new Date();
    var duration = endTime.getTime() - startTime.getTime();
    $.log("[" + duration + " ms] " + msg);
    return rv;
  };
})(jQuery);

// This ensures that console.log won't break javascript on browsers
// which don't define it. This is currently called by our actionscript logger
if (typeof console == "undefined") {
  console = { log: function() {} };
}

/*! jquery.swfobject.license.txt *//*

jQuery SWFObject Plugin v1.0.5 <http://jquery.thewikies.com/swfobject/>
Copyright (c) 2009 Jonathan Neal
This software is released under the MIT License <http://www.opensource.org/licenses/mit-license.php>
This software is released under the GPL License <http://www.opensource.org/licenses/gpl-2.0.php>

SWFObject v2.1 <http://code.google.com/p/swfobject/>
Copyright (c) 2007-2009 Geoff Stearns, Michael Williams, and Bobby van der Sluis
This software is released under the MIT License <http://www.opensource.org/licenses/mit-license.php>

jQuery v1.3.2 <http://jquery.com/>
Copyright (c) 2009 John Resig
This software is released under the MIT License <http://www.opensource.org/licenses/mit-license.php>
This software is released under the GPL License <http://www.opensource.org/licenses/gpl-2.0.php>

*/

(function ($) {
    var doc = document,
    object = 'object',
    win = window,
    x = '';

    /* $.flashPlayerVersion */
    $.flashPlayerVersion = (function () {
        var flashVersion,
        activeX,
        errorA,
        errorB,
        fp6Crash = false,
        shockwaveFlash = 'ShockwaveFlash.ShockwaveFlash';

        /* If Internet Explorer */
        if (!(flashVersion = navigator.plugins['Shockwave Flash'])) {
            try {
                activeX = new ActiveXObject(shockwaveFlash + '.7');
            }
            catch (errorA) {
                try {
                    activeX = new ActiveXObject(shockwaveFlash + '.6');
                    flashVersion = [6, 0, 21];
                    activeX.AllowScriptAccess = 'always';
                }
                catch (errorB) {
                    if (flashVersion && flashVersion[0] === 6) {
                        fp6Crash = true;
                    }
                }
                if (!fp6Crash) {
                    try {
                        activeX = new ActiveXObject(shockwaveFlash);
                    }
                    catch (errorC) {
                        flashVersion = 'X 0,0,0';
                    }
                }
            }
            if (!fp6Crash && activeX) {
                try {
                    /* Will crash fp6.0.21/23/29 */
                    flashVersion = activeX.GetVariable('$version');
                } catch (errorD) {}
            }
        }

        /* If NOT Internet Explorer */
        else {
            flashVersion = flashVersion.description;
        }

        /* Return flash version */
        flashVersion = flashVersion.match(/^[A-Za-z\s]*?(\d+)(\.|,)(\d+)(\s+r|,)(\d+)/);
        return [flashVersion[1] * 1, flashVersion[3] * 1, flashVersion[5] * 1];
    }());

    /* $.flashExpressInstaller */
    $.flashExpressInstaller = 'expressInstall.swf';

    /* $.hasFlashPlayer */
    $.hasFlashPlayer = ($.flashPlayerVersion[0] !== 0);

    /* $.hasFlashPlayerVersion */
    $.hasFlashPlayerVersion = function (options) {
        var flashVersion = $.flashPlayerVersion;
        options = (/string|number/.test(typeof options)) ? options.toString().split('.') : options;
        options = [options.major || parseInt(options[0]) || flashVersion[0], options.minor || parseInt(options[1]) || flashVersion[1], options.release || parseInt(options[2]) || flashVersion[2]];

        /* Return true or false */
        return ($.hasFlashPlayer && (options[0] < flashVersion[0] || (options[0] === flashVersion[0] && (options[1] < flashVersion[1] || (options[1] === flashVersion[1] && options[2] <= flashVersion[2])))));
    };

    /* $.flash */
    $.flash = function (options) {
        /* Check if Flash is installed, return false if it isn't */
        if (!$.hasFlashPlayer) {
            return false;
        }

        var movieFilename = options.swf || x,
        paramAttributes = options.params || {},
        buildDOM = doc.createElement('body'),
        aArr,
        bArr,
        cArr,
        dArr,
        a,
        b,
        c,
        d;

        /* Set the default height and width if not already set */
        options.height = options.height || 180;
        options.width = options.width || 320;

        /* Inject ExpressInstall if "hasVersion" is requested and the version requirement is not met */
        if (options.hasVersion && !$.hasFlashPlayerVersion(options.hasVersion)) {
            $.extend(options, {
                id: 'SWFObjectExprInst',
                height: options.height,
                width: options.width
            });
            movieFilename = options.expressInstaller || $.flashExpressInstaller;
            paramAttributes = {
                flashvars: {
                    MMredirectURL: location.href,
                    MMplayerType: ($.browser.msie && $.browser.win) ? 'ActiveX': 'PlugIn',
                    MMdoctitle: doc.title.slice(0, 47) + ' - Flash Player Installation'
                }
            };
        }

        /* Append as a param if specified separately */
        if (typeof paramAttributes === object) {
            /* flashvars */
            if (options.flashvars) {
                paramAttributes.flashvars = options.flashvars;
            }

            /* wmode */
            if (options.wmode) {
                paramAttributes.wmode = options.wmode;
            }
        }

        /* Delete the reformatted constructors */
        for (a in (b = ['expressInstall', 'flashvars', 'hasVersion', 'params', 'swf', 'wmode'])) {
            delete options[b[a]];
        }

        /* Create the OBJECT tag attributes */
        aArr = [];
        for (a in options) {
            if (typeof options[a] === object) {
                bArr = [];
                for (b in options[a]) {
                    bArr.push(b.replace(/([A-Z])/, '-$1').toLowerCase() + ':' + options[a][b] + ';');
                }
                options[a] = bArr.join(x);
            }
            aArr.push(a + '="' + options[a] + '"');
        }
        options = aArr.join(' ');

        /* Create the PARAM tags */
        if (typeof paramAttributes === object) {
            aArr = [];
            for (a in paramAttributes) {
                if (typeof paramAttributes[a] === object) {
                    bArr = [];
                    for (b in paramAttributes[a]) {
                        if (typeof paramAttributes[a][b] === object) {
                            cArr = [];
                            for (c in paramAttributes[a][b]) {
                                if (typeof paramAttributes[a][b][c] === object) {
                                    dArr = [];
                                    for (d in paramAttributes[a][b][c]) {
                                        dArr.push([d.replace(/([A-Z])/, '-$1').toLowerCase(), ':', paramAttributes[a][b][c][d], ';'].join(x));
                                    }
                                    paramAttributes[a][b][c] = dArr.join(x);
                                }
                                cArr.push([c, '{', paramAttributes[a][b][c], '}'].join(x));
                            }
                            paramAttributes[a][b] = cArr.join(x);
                        }
                        bArr.push([b, '=', win.escape(paramAttributes[a][b])].join(x));
                    }
                    paramAttributes[a] = bArr.join('&amp;');
                }
                aArr.push(['<PARAM NAME="', a, '" VALUE="', paramAttributes[a], '">'].join(x));
            }
            paramAttributes = aArr.join(x);
        }

        /* Unify the visual display between all browsers */
        if (!(/style=/.test(options))) {
            options += ' style="vertical-align:text-top;"';
        }
        if (!(/style=(.*?)vertical-align/.test(options))) {
            options = options.replace(/style="/, 'style="vertical-align:text-top;');
        }

        /* Specify the object and param tags between browsers */
        if ($.browser.msie) {
            options += ' classid="clsid:D27CDB6E-AE6D-11cf-96B8-444553540000"';
            paramAttributes = '<PARAM NAME="movie" VALUE="' + movieFilename + '">' + paramAttributes;
        } else {
            options += ' type="application/x-shockwave-flash" data="' + movieFilename + '"';
        }

        /* Return the jQuery'd flash OBJECT */
        buildDOM.innerHTML = ['<OBJECT ', options, '>', paramAttributes, '</OBJECT>'].join(x);
        return $(buildDOM.firstChild);
    };

    /* $.fn.flash */
    $.fn.flash = function (options) {
        /* Check if Flash is installed, return the jQuery node if it isn't */
        if (!$.hasFlashPlayer) {
            return this;
        }

        var a = 0,
        each;

        /* Each */
        while ((each = this.eq(a++))[0]) {
            each.html($.flash($.extend({},
            options)));
            if (doc.getElementById('SWFObjectExprInst')) {
                a = this.length;
            }
        }

        /* Return the jQuery node */
        return this;
    };
}(jQuery));


angular
    .module('eArkPlatform.init', ['ngMaterial'])
    .constant('USER_ROLES', {
        admin: 'ADMIN',
        standard: 'STANDARD'
    })
    .constant('PATTERNS', {
        fileName: /^[a-zA-Z0-9_\-,!@#$%^&()=+ ]+$/,
        phone: /^[+]?[0-9\- ]+$/
    })
    .constant('APP_CONFIG', {
        appName: 'EARK-CMIS-BROWSER',
        logoSrc: './app/assets/images/logo.gif'
    })
    .constant('BRIDGE_URI',{
        serviceProxy:'http://eark.magenta.dk:9090/cmis-bridge'
    })
    .filter('urlEncode', function(){
        return function(input){
            if (input)
                return window.encodeURIComponent(input);
            return "";
        }
    });
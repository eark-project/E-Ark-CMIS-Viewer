angular
    .module('eArkPlatform.init', ['ngMaterial'])
    .constant('USER_ROLES', {
        admin: 'ADMIN',
        standard: 'STANDARD'
        //guest: 'guest' we don't want this type of user as of yet
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
        serviceProxy:'http://localhost:9090'
    })
    .filter('urlEncode', function(){
        return function(input){
            if (input)
                return window.encodeURIComponent(input)

            return "";
        }
    });
angular.module('eArkPlatform.common.directives')
    .directive('commonError', CommonErrorDirective);

/**
 * Wrapper directive for ng-messages
 *
 *
 */
function CommonErrorDirective() {
    return {
        link: function (scope, elem, attrs) {
            // body...
        },
        restrict: 'E',
        replace: true,
        scope: {
            form: '=',
            field: '='
        },
        template: '<div ng-messages="field.$error" ng-show="(form.$submitted || field.$touched) && field.$invalid" role="alert">' +
        '<div ng-messages-include="app/src/common/directives/errors/error-messages.html"></div>' +
        '</div>'
    }
}
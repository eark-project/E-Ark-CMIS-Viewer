/*
 * The Errors module supplies a shorthand for you to display error
 * messages in the GUI.
 *
 * HOW TO USE:
 *
 * 1. Add <error-msg></error-msg> directive in any template where you
 *    want your error messages to appear.
 *
 * 2. In your controller, reference the 'errorService' service
 *
 * 3. Use service method 'displayErrorMsg()' in your controller like so:
 *    errorService.displayErrorMsg( 'My error message' );
 *
 * Error messages will appear in the GUI using the error-msg directive.
 *
 */
angular.module('eArkPlatform.errors', ['ngMaterial', 'pascalprecht.translate']);
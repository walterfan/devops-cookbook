'use strict';

$(function() {

    $('#login-form-link').click(function(e) {
        $("#login-form").delay(100).fadeIn(100);
        $("#register-form").fadeOut(100);
        $('#register-form-link').removeClass('active');
        $(this).addClass('active');
        e.preventDefault();
    });
    $('#register-form-link').click(function(e) {
        $("#register-form").delay(100).fadeIn(100);
        $("#login-form").fadeOut(100);
        $('#login-form-link').removeClass('active');
        $(this).addClass('active');
        e.preventDefault();
    });

});

// Defining angularjs application.
var myApp = angular.module('myApp', []);
// Controller function and passing $http service and $scope var.
myApp.controller('myController', function($scope, $http) {
    // create a blank object to handle form data.
    $scope.user = {};
    // calling our submit function.
    $scope.submitRegisterForm = function() {
        var postData = {
            username:$scope.user.username,
            email: $scope.user.email,
            password: $scope.user.password,
            passwordConfirmation: $scope.user.passwordConfirmation
        };
        $http({
            method  : 'POST',
            url     : '/checklist/api/v1/users/register',
            data    : postData,
            headers : {'Content-Type': 'application/json'}
        })
            .success(function(data) {
                if (data.errors) {
                    // Showing errors.
                    $scope.errors = data.errors;
                } else {
                    $scope.message = data.message;
                }
            });
    };

    $scope.submitLoginrForm = function() {
        var postData = {
            email: $scope.user.email,
            password: $scope.user.password,
        };
        $http({
            method  : 'POST',
            url     : '/checklist/api/v1/users/login',
            data    : postData,
            headers : {'Content-Type': 'application/json'}
        })
            .success(function(data) {
                if (data.errors) {
                    // Showing errors.
                    $scope.errors = data.errors;
                } else {
                    $scope.message = data.message;
                }
            });
    };
});